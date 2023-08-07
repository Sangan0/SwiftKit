package com.mozhimen.componentk.camerak.camerax.helpers

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.camera.camera2.internal.Camera2CameraInfoImpl
import androidx.camera.core.*
import androidx.camera.extensions.ExtensionMode
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.slider.Slider
import com.mozhimen.basick.elemk.java.util.bases.BaseHandlerExecutor
import com.mozhimen.basick.lintk.optin.OptInFieldCall_Close
import com.mozhimen.basick.utilk.bases.BaseUtilK
import com.mozhimen.basick.utilk.android.util.et
import com.mozhimen.basick.utilk.androidx.lifecycle.runOnMainScope
import com.mozhimen.componentk.camerak.camerax.annors.ACameraKXFacing
import com.mozhimen.componentk.camerak.camerax.annors.ACameraKXFormat
import com.mozhimen.componentk.camerak.camerax.commons.ICameraKX
import com.mozhimen.componentk.camerak.camerax.commons.ICameraKXCaptureListener
import com.mozhimen.componentk.camerak.camerax.commons.ICameraXKFrameListener
import com.mozhimen.componentk.camerak.camerax.commons.ICameraKXListener
import com.mozhimen.componentk.camerak.camerax.cons.CCameraKXRotation
import com.mozhimen.componentk.camerak.camerax.cons.ECameraKXTimer
import com.mozhimen.componentk.camerak.camerax.mos.MCameraKXConfig
import com.mozhimen.componentk.camerak.camerax.temps.OtherCameraFilter
import com.mozhimen.underlayk.logk.LogK
import kotlinx.coroutines.delay
import java.util.concurrent.ExecutionException
import kotlin.properties.Delegates

/**
 * @ClassName CameraXKDelegate
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/1/3 1:17
 * @Version 1.0
 */
class CameraKXDelegate : ICameraKX, BaseUtilK() {

    private var _cameraXKListener: ICameraKXListener? = null
    private var _cameraXKCaptureListener: ICameraKXCaptureListener? = null
    private var _cameraXKFrameListener: ICameraXKFrameListener? = null

    private var _hdrCameraSelector: CameraSelector? = null
    private var _imageCapture: ImageCapture? = null
    private var _imageAnalysis: ImageAnalysis? = null

    private var _format = ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888
    private var _selectedTimer = ECameraKXTimer.OFF
    private var _isSingleCamera = false

    //////////////////////////////////////////////////////////////////////////////////////////////

    internal var aspectRatio: Int = AspectRatio.RATIO_16_9
    internal var rotation = CCameraKXRotation.ROTATION_90

    //////////////////////////////////////////////////////////////////////////////////////////////

    private lateinit var _owner: LifecycleOwner
    private lateinit var _analyzerThread: HandlerThread

    //////////////////////////////////////////////////////////////////////////////////////////////
    internal lateinit var slider: Slider
    internal lateinit var previewView: PreviewView
    internal lateinit var preview: Preview

    /**
     * 选择器显示所选择的闪光模式(开、关或自动)
     * Selector showing which flash mode is selected (on, off or auto)
     */
    private var _flashMode by Delegates.observable(ImageCapture.FLASH_MODE_OFF) { _, _, new ->
        when (new) {
            ImageCapture.FLASH_MODE_ON -> _cameraXKListener?.onCameraFlashOn()
            ImageCapture.FLASH_MODE_AUTO -> _cameraXKListener?.onCameraFlashAuto()
            else -> _cameraXKListener?.onCameraFlashOff()
        }
    }

    /**
     * 显示相机选择的选择器(正面或背面)
     * Selector showing which camera is selected (front or back)
     */
    private var _lensFacingSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var _lensFacing = CameraSelector.LENS_FACING_FRONT

    /**
     * 选择器显示是否启用hdr(只有当设备的摄像头在硬件层面支持hdr时才会工作)
     * Selector showing is hdr enabled or not (will work, only if device's camera supports hdr on hardware level)
     */
    private var _isOpenHdr = false
    private var _captureBitmap: Bitmap? = null

    @OptIn(OptInFieldCall_Close::class)
    private val _onImageCaptureCallback = object : ImageCapture.OnImageCapturedCallback() {
        @SuppressLint("UnsafeOptInUsageError")
        override fun onCaptureSuccess(image: ImageProxy) {
            Log.d(TAG, "onCaptureSuccess: ${image.format} ${image.width}x${image.height}")
            when (image.format) {
                ImageFormat.YUV_420_888 -> {
                    _captureBitmap = image.yuv420888ImageProxy2JpegBitmap()
                    Log.d(TAG, "onCaptureSuccess: YUV_420_888")
                }

                ImageFormat.JPEG -> {
                    _captureBitmap = image.jpegImageProxy2JpegBitmap()
                    Log.d(TAG, "onCaptureSuccess: JPEG")
                }

                ImageFormat.FLEX_RGBA_8888 -> {
                    _captureBitmap = image.rgba8888ImageProxy2Rgba8888Bitmap()
                    Log.d(TAG, "onCaptureSuccess: FLEX_RGBA_8888")
                }
            }
            _captureBitmap?.let {
                _cameraXKCaptureListener?.onCaptureSuccess(it, image.imageInfo.rotationDegrees)
            }
            image.close()
        }

        override fun onError(e: ImageCaptureException) {
            LogK.et(TAG, "OnImageCapturedCallback onError ImageCaptureException ${e.message}")
            _cameraXKCaptureListener?.onCaptureFail()
            e.printStackTrace()
            e.message?.et(TAG)
        }
    }
    private val _imageAnalyzer: ImageAnalysis.Analyzer = ImageAnalysis.Analyzer { imageProxy ->
        this._cameraXKFrameListener?.invoke(imageProxy)
    }

    //region open fun
    override fun setCameraXListener(listener: ICameraKXListener) {
        this._cameraXKListener = listener
    }

    override fun setCameraXCaptureListener(listener: ICameraKXCaptureListener) {
        this._cameraXKCaptureListener = listener
    }

    override fun initCameraX(owner: LifecycleOwner, config: MCameraKXConfig) {
        _owner = owner
        _lensFacing = config.facing
        _lensFacingSelector = when (config.facing) {
            ACameraKXFacing.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
            else -> CameraSelector.DEFAULT_BACK_CAMERA
        }
        _format = when (config.format) {
            ACameraKXFormat.RGBA_8888 -> ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
            else -> ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888
        }
    }

    override fun initCameraX(owner: LifecycleOwner) {
        initCameraX(owner, MCameraKXConfig())
    }

    @Throws(Exception::class)
    override fun startCameraX() {
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(_context)
            cameraProviderFuture.addListener({
                val cameraProvider: ProcessCameraProvider?
                try {
                    cameraProvider = cameraProviderFuture.get()
                } catch (e: InterruptedException) {
                    _cameraXKListener?.onCameraStartFail(e.message ?: "")
                    LogK.et(TAG, "startCamera InterruptedException ${e.message ?: ""}")
                    return@addListener
                } catch (e: ExecutionException) {
                    _cameraXKListener?.onCameraStartFail(e.message ?: "")
                    LogK.et(TAG, "startCamera ExecutionException ${e.message ?: ""}")
                    return@addListener
                }

                val localCameraProvider: ProcessCameraProvider = cameraProvider
                    ?: throw IllegalStateException("Camera initialization failed.")

                //图像捕获的配置 The Configuration of image capture
                _imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY) // setting to have pictures with highest quality possible (may be slow)
                    .setFlashMode(_flashMode) // set capture flash
                    .setTargetAspectRatio(aspectRatio) // set the capture aspect ratio
                    .setTargetRotation(rotation) // set the capture rotation
                    .build()

                //Hdr
                //checkForHdrExtensionAvailability(cameraProvider)

                //图像分析的配置 The Configuration of image analyzing
                _imageAnalysis = ImageAnalysis.Builder()
                    .setTargetAspectRatio(aspectRatio) // set the analyzer aspect ratio
                    .setTargetRotation(rotation) // set the analyzer rotation
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // in our analysis, we care about the latest image
                    .setOutputImageFormat(_format)
                    .build()
                    .also {
                        setCameraXKAnalyzer(it)
                    }

                // Unbind the use-cases before rebinding them
                localCameraProvider.unbindAll()

                // Bind all use cases to the camera with lifecycle
                previewView.post {
                    bindToLifecycle(localCameraProvider, preview, previewView, slider)
                }
            }, ContextCompat.getMainExecutor(_context))
        } catch (e: Exception) {
            e.printStackTrace()
            e.message?.et(TAG)
        }
    }

    override fun setCameraXFrameListener(listener: ICameraXKFrameListener) {
        this._cameraXKFrameListener = listener
    }

    override fun changeHdr(isOpen: Boolean) {
        if (_isOpenHdr != isOpen) {
            _isOpenHdr = !_isOpenHdr
            startCameraX()
        }
    }

    override fun changeFlash(@ImageCapture.FlashMode flashMode: Int) {
        _flashMode = flashMode
        _imageCapture?.flashMode = _flashMode
    }

    override fun changeCountDownTimer(timer: ECameraKXTimer) {
        _selectedTimer = timer
    }

    override fun changeCameraXFacing(@ACameraKXFacing facing: Int) {
        if (_isSingleCamera) return
        val cameraSelector = when (facing) {
            ACameraKXFacing.FRONT -> CameraSelector.DEFAULT_BACK_CAMERA
            else -> CameraSelector.DEFAULT_FRONT_CAMERA
        }
        if (_lensFacingSelector != cameraSelector) {
            _lensFacingSelector = if (_lensFacingSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            startCameraX()
        }
    }

    override fun startCapture() {
        _owner.runOnMainScope {
            // Show a timer based on user selection
            when (_selectedTimer) {
                ECameraKXTimer.S3 -> for (i in 3 downTo 1) delay(1000)
                ECameraKXTimer.S10 -> for (i in 10 downTo 1) delay(1000)
                else -> {}
            }
            captureImage()
        }
    }

    fun onFrameFinished() {
        if (this::_analyzerThread.isInitialized && !_analyzerThread.isInterrupted) _analyzerThread.interrupt()
    }
    //endregion

    @SuppressLint("RestrictedApi")
    @Throws(Exception::class)
    private fun bindToLifecycle(localCameraProvider: ProcessCameraProvider, preview: Preview, previewView: PreviewView, slider: Slider) {
        if (localCameraProvider.availableCameraInfos.size == 1) {
            _isSingleCamera = true
            val cameraInfo: Camera2CameraInfoImpl = (localCameraProvider.availableCameraInfos[0] as Camera2CameraInfoImpl)
            Log.d(TAG, "bindToLifecycle: cameraInfo $cameraInfo _lensFacing ${cameraInfo.cameraSelector.lensFacing} id ${cameraInfo.cameraId}")
            _lensFacingSelector.cameraFilterSet.clear()
            _lensFacingSelector.cameraFilterSet.add(OtherCameraFilter(cameraInfo.cameraId))
        }
        localCameraProvider.bindToLifecycle(
            _owner, // current lifecycle owner
            /*_hdrCameraSelector ?: */
            _lensFacingSelector, // either front or back facing
            preview, // camera preview use case
            _imageCapture!!, // image capture use case
            _imageAnalysis!!, // image analyzer use case
        ).apply {
            // Init camera exposure control
            cameraInfo.exposureState.run {
                val lower = exposureCompensationRange.lower
                val upper = exposureCompensationRange.upper

                slider.run {
                    valueFrom = lower.toFloat()
                    valueTo = upper.toFloat()
                    stepSize = 1f
                    value = exposureCompensationIndex.toFloat()

                    addOnChangeListener { _, value, _ ->
                        cameraControl.setExposureCompensationIndex(value.toInt())
                    }
                }
            }
        }

        // Attach the viewfinder's surface provider to preview use case
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    private fun captureImage() {
        val localImageCapture = _imageCapture ?: return
        localImageCapture.takePicture(
            ContextCompat.getMainExecutor(_context), _onImageCaptureCallback// the executor, on which the task will run
        )
    }

    /**
     * 为HDR创建供应商扩展
     * Create a Vendor Extension for HDR
     * @param cameraProvider CameraProvider
     */
    private fun checkForHdrExtensionAvailability(cameraProvider: CameraProvider) {
        //为HDR创建供应商扩展 Create a Vendor Extension for HDR
        val extensionsManagerFuture = ExtensionsManager.getInstanceAsync(_context, cameraProvider)
        extensionsManagerFuture.addListener(
            {
                val extensionsManager = extensionsManagerFuture.get() ?: return@addListener
                val isAvailable = extensionsManager.isExtensionAvailable(_lensFacingSelector, ExtensionMode.HDR)

                //检查是否有扩展可用 check for any extension availability
                Log.d(TAG, "checkForHdrExtensionAvailability: AUTO " + extensionsManager.isExtensionAvailable(_lensFacingSelector, ExtensionMode.AUTO))
                Log.d(TAG, "checkForHdrExtensionAvailability: HDR " + extensionsManager.isExtensionAvailable(_lensFacingSelector, ExtensionMode.HDR))
                Log.d(TAG, "checkForHdrExtensionAvailability: FACE RETOUCH " + extensionsManager.isExtensionAvailable(_lensFacingSelector, ExtensionMode.FACE_RETOUCH))
                Log.d(TAG, "checkForHdrExtensionAvailability: BOKEH " + extensionsManager.isExtensionAvailable(_lensFacingSelector, ExtensionMode.BOKEH))
                Log.d(TAG, "checkForHdrExtensionAvailability: NIGHT " + extensionsManager.isExtensionAvailable(_lensFacingSelector, ExtensionMode.NIGHT))
                Log.d(TAG, "checkForHdrExtensionAvailability: NONE " + extensionsManager.isExtensionAvailable(_lensFacingSelector, ExtensionMode.NONE))

                //检查分机是否在设备上可用 Check if the extension is available on the device
                if (!isAvailable) {
                    _cameraXKListener?.onCameraHDRCheck(false)
                } else if (_isOpenHdr) {
                    //如果是，如果HDR是由用户打开的，则打开 If yes, turn on if the HDR is turned on by the user
                    _cameraXKListener?.onCameraHDROpen()
                    _hdrCameraSelector = extensionsManager.getExtensionEnabledCameraSelector(_lensFacingSelector, ExtensionMode.HDR)
                }
            }, ContextCompat.getMainExecutor(_context)
        )
    }

    private fun setCameraXKAnalyzer(imageAnalysis: ImageAnalysis) {
        //使用工作线程进行图像分析，以防止故障 Use a worker thread for image analysis to prevent glitches
        _cameraXKFrameListener?.let {
            _analyzerThread = HandlerThread("CameraXKLuminosityAnalysis").apply { start() }
            imageAnalysis.setAnalyzer(BaseHandlerExecutor(Handler(_analyzerThread.looper)), _imageAnalyzer)
        }
    }
}