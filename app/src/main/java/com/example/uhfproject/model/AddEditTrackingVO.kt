package com.example.uhfproject.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @Author LiShuai
 * @Date 2025-08-25 10:29
 * @Description 添加或者编辑快递单号
 * @Version 1.0
 */
@Parcelize
data class AddEditTrackingVO(
    /**
     * 快递单号
     */
    val trackingId: String,
    /**
     * 邮编
     */
    val postCode: String,
    /**
     * 区号
     */
    val bitCode: String
) : Parcelable {
    interface Add
    interface Edit
}