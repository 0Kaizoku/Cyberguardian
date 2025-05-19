package com.cyberguardianapp.api

import com.cyberguardianapp.model.AnalyzeAppRequest
import com.cyberguardianapp.model.RiskResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface BackendApi {
    @POST("api/analyzeApp")
    fun analyzeApp(@Body request: AnalyzeAppRequest): Call<RiskResponse>
} 