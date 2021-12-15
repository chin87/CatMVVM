package com.example.catmvvm.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catmvvm.model.CatFact
import com.example.catmvvm.service.repository.CatFactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatFactViewModel @Inject constructor(
    private val catFactRepository: CatFactRepository
) : ViewModel() {

    private val _catFact = MutableLiveData<CatFact>()
    val catFact: LiveData<CatFact> = _catFact

    fun loadCatFact() {
        viewModelScope.launch {
            try {
                _catFact.value = catFactRepository.getCatFact()
            } catch (e: Exception) {
                // Retrofit error
                e.printStackTrace()
            }
        }
    }
}