package com.example.uhfproject.utils.retrofit

import androidx.lifecycle.LiveData
import com.example.uhfproject.model.*
import com.example.uhfproject.ui.update.LatestVersionDto
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
    @POST("login")
    suspend fun loginNet(@Body body: LoginBody) : LoginResult<Any>

    /**
     * 查询快递单号列表
     */
    @GET("post/getTrackingIdList")
    suspend fun getTrackingIdListNet(@Query("status") status: Int,
                                     @Query("pageNum") pageNum: Int,
                                     @Query("pageSize") pageSize: Int) : HttpResultPager<List<ExcelDownloadVO>>

    /**
     * 入库
     */
    @POST("post/inbound")
    suspend fun inboundNet(@Body body: List<String>) : HttpResult<InfoPromptsVO>

    /**
     * 查询快递单号列表
     */
    @GET("post/getTrackingIdByEPC")
    suspend fun getTrackingIdByEPCNet(@Query("epcList") epcList: List<String>) : HttpResult<List<ExcelDownloadVO>>

    /**
     * 出库
     */
    @POST("post/outbound")
    suspend fun outboundNet(@Body body: List<String>) : HttpResult<InfoPromptsVO>


    /**
     * 查询快递单号列表
     */
    @GET("post/getTrackingIdList")
    suspend fun getItemByTrackingIdNet(@Query("trackingId") trackingId: String,
                                    @Query("status") status: Int,
                                     @Query("pageNum") pageNum: Int,
                                     @Query("pageSize") pageSize: Int) : HttpResultPager<List<ExcelDownloadVO>>

    /**
     * 获取相关统计数量
     */
    @GET("dashboard/getStatistics")
    suspend fun getStatisticsNet() : HttpResult<DashboardNumberVO>

    /**
     * 根据DB获取排行榜数据
     */
    @GET("dashboard/getTop")
    suspend fun getTopNet() : HttpResult<List<DashboardTopVO>>

    /**
     * 获取待入库数据
     */
    @GET("dashboard/getPendingInbound")
    suspend fun getPendingInboundNet(@Query("pageNum") pageNum: Int,
                                     @Query("pageSize") pageSize: Int) : HttpResultPager<List<ExcelDownloadVO>>

    /**
     * 获取剩余出库数据
     */
    @GET("dashboard/getPendingOutbound")
    suspend fun getPendingOutboundNet(@Query("pageNum") pageNum: Int,
                                      @Query("pageSize") pageSize: Int) : HttpResultPager<List<ExcelDownloadVO>>

    @GET("version/ver/getVersion")
    fun getVersionNet(@Query("serverName") name: String = "POST_APP"): LiveData<HttpResult<LatestVersionDto>>
}