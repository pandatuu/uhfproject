package com.example.uhfproject.utils.retrofit

import com.example.uhfproject.model.*

class ApiRepository {

    private val service = RetrofitClient.createService<MainService>()

    suspend fun getTrackingIdByEPCRep(epcList: List<String>): APIResult<List<ExcelDownloadVO>> {
        return safeNetworkInvoke {
            service.getTrackingIdByEPCNet(epcList)
        }
    }

    suspend fun inboundRep(body: List<String>): APIResult<InfoPromptsVO> {
        return safeNetworkInvoke {
            service.inboundNet(body)
        }
    }

    suspend fun outboundRep(body: List<String>): APIResult<InfoPromptsVO> {
        return safeNetworkInvoke {
            service.outboundNet(body)
        }
    }

    suspend fun getTrackingIdListRep(pageNum: Int, sortPageSize: Int): APIResult<List<ExcelDownloadVO>> {
        return safeNetWorkPagerInvoke {
            service.getTrackingIdListNet(2, pageNum, sortPageSize)
        }
    }

    suspend fun getItemByTrackingIdRep(tracking: String, pageNum: Int, sortPageSize: Int): APIResult<List<ExcelDownloadVO>> {
        return safeNetWorkPagerInvoke {
            service.getItemByTrackingIdNet(tracking, pageNum, sortPageSize)
        }
    }

    suspend fun getStatisticsRep(): APIResult<DashboardNumberVO> {
        return safeNetworkInvoke {
            service.getStatisticsNet()
        }
    }

    suspend fun getTopRep(): APIResult<List<DashboardTopVO>> {
        return safeNetworkInvoke {
            service.getTopNet()
        }
    }

    suspend fun getBoundListRep(status: Int): APIResult<List<ExcelDownloadVO>> {
        return safeNetworkInvoke {
            service.getAllListNet(status)
        }
    }

    suspend fun getBeatDataRep(beatPrefix: String): APIResult<OutboundVerifyVO> {
        return safeNetworkInvoke {
            service.getBeatDataNet(beatPrefix)
        }
    }

    suspend fun getLifeCycleByEPCRep(epc: String): APIResult<LifeCycleVO> {
        return safeNetworkInvoke {
            service.getLifeCycleByEPCNet(epc)
        }
    }

    suspend fun bindRep(body: List<BindBody>): APIResult<Any> {
        return safeNetworkInvoke {
            service.bindNet(body)
        }
    }

    suspend fun getPendingInboundRep(pageNum: Int, sortPageSize: Int): APIResult<List<ExcelDownloadVO>> {
        return safeNetWorkPagerInvoke {
            service.getPendingInboundNet(pageNum, sortPageSize)
        }
    }

    suspend fun getPendingOutboundRep(pageNum: Int, sortPageSize: Int): APIResult<List<ExcelDownloadVO>> {
        return safeNetWorkPagerInvoke {
            service.getPendingOutboundNet(pageNum, sortPageSize)
        }
    }

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { ApiRepository() }
    }
}
