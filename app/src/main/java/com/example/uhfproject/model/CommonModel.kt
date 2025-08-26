package com.example.uhfproject.model

data class HttpPager<T>(
    var total: Int,
    var msg: String,
    var code: Int,
    var rows: List<T>,
)