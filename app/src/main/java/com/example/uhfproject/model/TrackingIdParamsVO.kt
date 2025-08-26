package com.example.uhfproject.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.LocalDateTime

/**
 * 出入库信息提示
 */
@Parcelize
data class TrackingIdParamsVO(

    /**
     * 快递单号
     */
    val trackingId: String? = null,

    /**
     * 邮编
     */
    val postCode: String? = null,

    /**
     * 区号
     */
    val bitCode: String? = null,

    /**
     * epc编码
     */
    val epc: String? = null,

    /**
     * 状态（0.未绑定 1.已绑定 2.在库  3.出库）
     */
    val status: Int? = null,

    /**
     * 表格上传开始时间
     */
    val uploadStartTime: LocalDateTime? = null,

    /**
     * 表格上传结束时间
     */
    val uploadEndTime: LocalDateTime? = null,

    /**
     * rfid打印开始时间
     */
    val rfidPrintStartTime: LocalDateTime? = null,

    /**
     * rfid打印结束时间
     */
    val rfidPrintEndTime: LocalDateTime? = null,

    /**
     * 入库开始时间
     */
    val inboundStartTime: LocalDateTime? = null,

    /**
     * 入库结束时间
     */
    val inboundEndTime: LocalDateTime? = null,

    /**
     * 出库开始时间
     */
    val outboundStartTime: LocalDateTime? = null,

    /**
     * 出库结束时间
     */
    val outboundEndTime: LocalDateTime? = null,

    /**
     * 快递单号排序（0.升序 1.降序 2.默认不排序）
     */
    val trackingIdSort: Int? = null,

    /**
     * 邮编排序（0.升序 1.降序 2.默认不排序）
     */
    val postCodeSort: Int? = null,

    /**
     * 区号排序（0.升序 1.降序 2.默认不排序）
     */
    val bitCodeSort: Int? = null
) : Parcelable