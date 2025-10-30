package com.example.uhfproject.model

data class HttpResultPager<T>(
    val total: Int,
    val msg: String,
    val code: Int,
    val rows: T? = null,
)

data class HttpResult<T>(
    var code: Int = 0,
    var msg: String? = "",
    var data: T? = null
)

data class LoginResult<T>(
    val code: Int = 0,
    val msg: String? = "",
    val token: String? = ""
)
data class UserInfoVo(
    val code: Int = 0,
    val msg: String? = "",
    val user: UserInfo? = null
)
data class UserInfo(
    val userId: String,
    val userName: String,
    val nickName: String,
)

data class CommonModel(val isSuccess: Boolean, val message: String)

data class LocalInboundRecord(
    val epc: String,
    val scanIndex: Int,
    val site: String, //site is generalized for specific application case "postOffice" for future code reuse
    val category: Int // 1=match, 2=other office, 3=no record
)