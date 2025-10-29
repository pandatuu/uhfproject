package com.example.uhfproject.utils

object ConvertUtils {
    private val hexChars = "0123456789ABCDEF".toCharArray()

    fun bytesToHexString(bytes: ByteArray): String {
        val result = StringBuilder(bytes.size * 2)
        for (byte in bytes) {
            val value = byte.toInt()
            val high = (value and 0xF0) ushr 4
            val low = value and 0x0F
            result.append(hexChars[high])
            result.append(hexChars[low])
        }
        return result.toString()
    }
    fun hexStringToByteArray(hex: String): ByteArray {
        val len = hex.length
        if (len % 2 != 0) {
            return ByteArray(0)
        }
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            try {
                data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            } catch (e: Exception) {
                return ByteArray(0)
            }
            i += 2
        }
        return data
    }
}