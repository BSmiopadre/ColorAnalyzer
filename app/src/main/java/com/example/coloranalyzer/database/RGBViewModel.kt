package com.example.coloranalyzer.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RGBViewModel(private val repository: RGBRepository): ViewModel() {

    val allData : LiveData<List<RGB>> = repository.allData.asLiveData()

    // insert new RGB in the db
    fun insert (rgb: RGB) = viewModelScope.launch {
        repository.insert(rgb)
    }

    // delete 5 minutes old data
    fun deleteOldData(timeLimit: Long) = viewModelScope.launch {
        repository.deleteOldData(timeLimit)
    }
}

class RGBViewModelFactory (private val repository: RGBRepository): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(RGBViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return RGBViewModel(repository) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}