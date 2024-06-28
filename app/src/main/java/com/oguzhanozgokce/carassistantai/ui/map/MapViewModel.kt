package com.oguzhanozgokce.carassistantai.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.oguzhanozgokce.carassistantai.data.model.LocationModel
import com.oguzhanozgokce.carassistantai.data.repos.MapRepository

class MapViewModel(private val repository: MapRepository) : ViewModel() {
    private val _location = MutableLiveData<LocationModel>()
    val location: LiveData<LocationModel> get() = _location

}