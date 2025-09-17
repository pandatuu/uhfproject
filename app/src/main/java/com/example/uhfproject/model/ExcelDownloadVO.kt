package com.example.uhfproject.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * 下载数据
 */
@Parcelize
data class ExcelDownloadVO(
    val position: Int? = null,

    val sn: Long? = null,

    val trackingNumber: String? = null,

    @SerializedName("postCode")
    val postalCode: String? = null,

    @SerializedName("bitCode")
    val beat: String? = null,

    val reservedField1: String? = null,

    val epc: String? = null,

    val db: String? = null,

    /**
     * 0=pending for printing,1=bound already,2=inbound,3=outbound
     */
    val status: Int? = null,

    val uploadTime: String? = null,

    val rfidPrintTime: String? = null,

    val inboundDate: String? = null,

    val outboundDate: String? = null
) : Parcelable