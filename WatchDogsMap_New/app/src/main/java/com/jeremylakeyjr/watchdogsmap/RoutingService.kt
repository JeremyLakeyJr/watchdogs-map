package com.jeremylakeyjr.watchdogsmap

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RoutingService {
    @GET("routing/{profile}/{coordinates}")
    suspend fun getRoute(
        @Path("profile") profile: String,
        @Path("coordinates") coordinates: String,
        @Query("key") apiKey: String
    ): retrofit2.Response<Any>
}
