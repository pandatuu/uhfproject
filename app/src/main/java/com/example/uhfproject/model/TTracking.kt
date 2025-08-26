package com.example.uhfproject.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.LocalDateTime

/**
 * 物流编号表
 */
@Parcelize
data class TTracking(

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
     * 表格上传时间
     */
    val uploadTime: LocalDateTime? = null,

    /**
     * rfid打印时间
     */
    val rfidPrintTime: LocalDateTime? = null,

    /**
     * 创建时间
     */
    val createTime: LocalDateTime? = null,

    /**
     * 创建人
     */
    val createBy: String? = null,

    /**
     * 更新时间
     */
    val updateTime: LocalDateTime? = null,

    /**
     * 更新人
     */
    val updateBy: String? = null,

    /**
     * 预留字段1
     */
    val reservedField1: String? = null,

    /**
     * 预留字段2
     */
    val reservedField2: String? = null
) : Parcelable