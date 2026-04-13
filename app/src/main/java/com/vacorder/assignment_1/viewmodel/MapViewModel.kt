package com.vacorder.assignment_1.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vacorder.assignment_1.data.FileStorageHelper
import com.vacorder.assignment_1.data.PhotoMarker
import com.vacorder.assignment_1.data.RouteSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val fileHelper = FileStorageHelper(application)

    private val _routes = MutableStateFlow<List<RouteSession>>(emptyList())
    val routes: StateFlow<List<RouteSession>> = _routes.asStateFlow()

    private val _photos = MutableStateFlow<List<PhotoMarker>>(emptyList())
    val photos: StateFlow<List<PhotoMarker>> = _photos.asStateFlow()

    private val _selectedLabel = MutableStateFlow<String?>(null)
    val selectedLabel: StateFlow<String?> = _selectedLabel.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _routes.value = fileHelper.loadAllRoutes()
            _photos.value = fileHelper.loadAllPhotoMarkers()
        }
    }

    fun selectLabel(label: String?) {
        _selectedLabel.value = label
    }

    fun availableLabels(): List<String> {
        val fromRoutes = _routes.value.map { it.label }
        val fromPhotos = _photos.value.map { it.label }
        return (fromRoutes + fromPhotos).distinct().sorted()
    }
}
