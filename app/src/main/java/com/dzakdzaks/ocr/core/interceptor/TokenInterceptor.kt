package com.dzakdzaks.ocr.core.interceptor

import com.dzakdzaks.ocr.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .header(BuildConfig.KEY_FIELD, BuildConfig.KEY_VALUE)
            .header(BuildConfig.HOST_FIELD, BuildConfig.HOST_VALUE)
            .build()
        return chain.proceed(request)
    }
}
