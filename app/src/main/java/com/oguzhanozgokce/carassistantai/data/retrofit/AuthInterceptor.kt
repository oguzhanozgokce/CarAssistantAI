package com.oguzhanozgokce.carassistantai.data.retrofit

import com.oguzhanozgokce.carassistantai.common.Constant.OPENAI_API_KEY
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $OPENAI_API_KEY")
            .build()
        return chain.proceed(request)
    }
}