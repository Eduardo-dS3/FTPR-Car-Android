package com.example.myapitest.service

import com.example.myapitest.model.Car
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("car") suspend fun postCar(@Body car: Car): Car
}