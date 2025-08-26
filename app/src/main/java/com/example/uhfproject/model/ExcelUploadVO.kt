package com.example.uhfproject.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 快递单号上传表
 */
@Parcelize
data class ExcelUploadVO(
    /**
     * 快递单号
     */
    val TrackingNumber: String,
    /**
     * 邮政编码
     */
    val PostCode: String,
    /**
     * 区号
     */
    val BitCode: String
) : Parcelable