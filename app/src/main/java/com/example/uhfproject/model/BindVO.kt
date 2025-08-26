package com.example.uhfproject.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 绑定表
 */
@Parcelize
data class BindVO(
    /**
     * 快递单号
     */
    val trackingId: String,

    /**
     * EPC编码
     */
    val epc: String

) : Parcelable