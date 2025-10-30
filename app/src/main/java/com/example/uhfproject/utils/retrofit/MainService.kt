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
     * 登录方法
     */
    @POST("login")
    suspend fun loginNet(@Body body: LoginBody) : LoginResult<Any>

    /**
     * 获取用户信息
     */
    @POST("getInfo")
    suspend fun getInfoNet() : UserInfoVo

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
    @POST("pon/inbound")
    suspend fun inboundPonNet(@Body body: PONInboundDTO) : HttpResult<PonInboundPromptsVO>

    /**
     * 查询快递单号列表
     */
    @POST("post/getTrackingIdByEPC")
    suspend fun getTrackingIdByEPCNet(@Body epcList: List<String>) : HttpResult<List<ExcelDownloadVO>>

    /**
     * 查询快递单号列表
     */
    @POST("pon/getTrackingIdByEPC")
    suspend fun getPonTrackingIdByEPCNet(@Body epcList: List<String>) : HttpResult<List<TrackingVO>>

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
                                     @Query("pageNum") pageNum: Int,
                                     @Query("pageSize") pageSize: Int) : HttpResultPager<List<ExcelDownloadVO>>

    /**
     * 查询快递单号列表
     */
    @GET("pon/getTrackingIdList")
    suspend fun getPonItemByTrackingIdNet(@Query("trackingId") trackingId: String,
                                       @Query("pageNum") pageNum: Int,
                                       @Query("pageSize") pageSize: Int) : HttpResultPager<List<PONExcelDownloadVO>>

    /**
     * 获取相关统计数量
     */
    @GET("dashboard/getStatistics")
    suspend fun getStatisticsNet() : HttpResult<DashboardNumberVO>
    /**
     * 获取相关统计数量
     */
    @GET("pon/getStatistics")
    suspend fun getPonStatisticsNet() : HttpResult<StatisticsVO>

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

    /**
     * 查询快递单号列表
     */
    @GET("post/getTrackingIdListNoPage")
    suspend fun getAllListNet(@Query("status") status: Int) : HttpResult<List<ExcelDownloadVO>>

    @GET("post/getBeatData")
    suspend fun getBeatDataNet(@Query("beatPrefix") beatPrefix: String): HttpResult<OutboundVerifyVO>

    @GET("post/getLifeCycleByEPC")
    suspend fun getLifeCycleByEPCNet(@Query("epc") epc: String): HttpResult<LifeCycleVO>

    @GET("pon/getLifeCycleByEPC")
    suspend fun getPonLifeCycleByEPCNet(@Query("epc") epc: String): HttpResult<PONLifeCycleVO>

    @POST("post/bind")
    suspend fun bindNet(@Body body: List<BindBody>): HttpResult<Any>
    @POST("pon/bind")
    suspend fun bindPonNet(@Body body: List<PONBindVO>): HttpResult<Any>

    @GET("site/getSiteList")
    suspend fun getSiteListNet(): HttpResult<List<UserSiteDTO>>

    @GET("site/getSiteByUserId")
    suspend fun getSiteByUserIdNet(): HttpResult<UserSiteDTO>

    @GET("pon/getBoundAndInboundCount")
    suspend fun getBoundAndInboundCountNet(@Query("siteId") siteId: Int): HttpResult<BoundAndInboundVO>

    @GET("pon/getBoundDataBySiteId")
    suspend fun getBoundDataBySiteIdNet(@Query("siteId") siteId: Int): HttpResult<List<TrackingVO>>

    @GET("site/getPermsByUserId")
    suspend fun getPermsByUserIdNet(): HttpResult<Boolean>

}