package com.example.uhfproject.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.LocalDateTime

/**
 * 入库表
 */
@Parcelize
data class TInbound(
    /**
     * 自增id
     */
    val id: Long? = null,
    /**
     * 快递单号
     */
    val trackingId: String? = null,
    /**
     * 入库时间
     */
    val inboundTime: LocalDateTime? = null,
    /**
     * 创建人
     */
    val createBy: String? = null,
    /**
     * 创建时间
     */
    val createTime: LocalDateTime? = null,
    /**
     * 更新人
     */
    val updateBy: String? = null,
    /**
     * 更新时间
     */
    val updateTime: LocalDateTime? = null
): Parcelable