package com.example.catmvvm.service.repository

import com.example.catmvvm.model.CatFact
import com.example.catmvvm.service.api.CatFactService

class CatFactRepository(
    private val catFactService: CatFactService
) {
    suspend fun getCatFact(): CatFact = catFactService.getCatFact()
}