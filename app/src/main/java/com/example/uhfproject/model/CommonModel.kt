package com.example.uhfproject.model

data class HttpResultPager<T>(
    var total: Int,
    var msg: String,
    var code: Int,
    var rows: T? = null,
)

data class HttpResult<T>(
    var code: Int = 0,
    var msg: String? = "",
    var data: T? = null
)

