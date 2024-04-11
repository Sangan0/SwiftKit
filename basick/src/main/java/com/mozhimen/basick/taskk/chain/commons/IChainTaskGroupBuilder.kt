package com.mozhimen.basick.taskk.chain.commons

import com.mozhimen.basick.taskk.chain.bases.BaseChainTask

/**
 * @ClassName IChainKBuilder
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Version 1.0
 */
interface IChainTaskGroupBuilder {
    fun add(id: String): IChainTaskGroupBuilder
    fun dependOn(id: String): IChainTaskGroupBuilder
    fun build(): BaseChainTask
}