package com.example.uhfproject.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 出入库信息提示
 */
@Parcelize
data class InfoPromptsVO(
    /**
     * 提示信息
     */
    val msg: String? = null,
    /**
     * 本次成功的快递单号集合
     */
    val successList: List<String>? = null,
    /**
     * 本次失败的数量
     */
    val failNum: Int? = null,
    /**
     * 本次未绑定的epc集合
     */
    val notBindEpcList: List<String>? = null,
    /**
     * 本次重复的快递单号集合
     */
    val repeatedTrackingIdList: List<String>? = null
) : Parcelable