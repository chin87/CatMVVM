package com.example.catmvvm.service.api

import com.example.catmvvm.model.CatFact
import retrofit2.http.GET

interface CatFactService {
    @GET("fact")
    suspend fun getCatFact(): CatFact
}