package com.example.uhfproject.ui

import com.example.uhfproject.model.BindVO
import com.example.uhfproject.model.ExcelDownloadVO
import com.example.uhfproject.model.HttpPager
import com.example.uhfproject.model.TTracking
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


/**
 * 入库页
 * 1.查询
 * 2.排序
 * 3.提交
 * 出库页
 * 1.查询
 * 2.排序
 * 3.提交
 * Inventory
 * 1.查询
 * 2.排序
 */
interface MainService {

    /**
     * 查询快递单号列表
     */
    @GET("post/getTrackingIdList")
    suspend fun getTrackingIdListNet() : HttpPager<List<TTracking>>

    /**
     * 入库
     */
    @POST("http://101.43.218.72:8080/post/inbound")
    suspend fun inboundNet(@Body body: List<String>) : Any

    /**
     * 查询快递单号列表
     */
    @GET("http://101.43.218.72:8080/post/getTrackingIdByEPC")
    suspend fun getTrackingIdByEPCNet(@Query("epcList") epcList: List<String>) : ExcelDownloadVO

    /**
     * 出库
     */
    @POST("http://101.43.218.72:8080/post/outbound")
    suspend fun outboundNet(@Body body: List<String>) : Any


}