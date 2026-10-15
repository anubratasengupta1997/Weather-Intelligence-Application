package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.RecentCityEntity
import com.example.domain.WeatherRepository
import com.example.domain.model.CityLocation
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

@OptIn(FlowPreview::class)
class WeatherViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _autocompleteResults = MutableStateFlow<List<CityLocation>>(emptyList())
    val autocompleteResults: StateFlow<List<CityLocation>> = _autocompleteResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _temperatureUnit = MutableStateFlow(TemperatureUnit.CELSIUS)
    val temperatureUnit: StateFlow<TemperatureUnit> = _temperatureUnit.asStateFlow()

    private val _selectedCity = MutableStateFlow<CityLocation?>(null)
    val selectedCity: StateFlow<CityLocation?> = _selectedCity.asStateFlow()

    val popularCities = repository.popularCities

    val recentCities: StateFlow<List<RecentCityEntity>> = repository.recentCities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var searchDebounceJob: Job? = null

    init {
        // Load default city (New York) on app startup
        val defaultCity = popularCities.first()
        loadWeatherForCity(defaultCity)

        // Observe search query with debounce for autocomplete
        viewModelScope.launch {
            _searchQuery
                .debounce(350)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.trim().length >= 2) {
                        performAutocomplete(query.trim())
                    } else {
                        _autocompleteResults.value = emptyList()
                        _isSearching.value = false
                    }
                }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        if (newQuery.trim().length >= 2) {
            _isSearching.value = true
        } else {
            _isSearching.value = false
            _autocompleteResults.value = emptyList()
        }
    }

    private suspend fun performAutocomplete(query: String) {
        try {
            val results = repository.searchCities(query)
            _autocompleteResults.value = results
        } catch (e: Exception) {
            _autocompleteResults.value = emptyList()
        } finally {
            _isSearching.value = false
        }
    }

    fun searchCityDirectly(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        _isSearching.value = true
        viewModelScope.launch {
            try {
                val results = repository.searchCities(trimmed)
                if (results.isEmpty()) {
                    _uiState.value = WeatherUiState.CityNotFound(trimmed)
                    _autocompleteResults.value = emptyList()
                } else {
                    _autocompleteResults.value = emptyList()
                    loadWeatherForCity(results.first())
                }
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(
                    message = "Unable to find city. Please verify your internet connection and try again."
                )
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun selectCity(city: CityLocation) {
        _searchQuery.value = ""
        _autocompleteResults.value = emptyList()
        _isSearching.value = false
        loadWeatherForCity(city)
    }

    fun selectRecentCity(recent: RecentCityEntity) {
        val city = CityLocation(
            id = recent.id,
            name = recent.name,
            admin1 = recent.admin1,
            country = recent.country,
            countryCode = recent.countryCode,
            latitude = recent.latitude,
            longitude = recent.longitude
        )
        selectCity(city)
    }

    fun loadWeatherForCity(city: CityLocation) {
        _selectedCity.value = city
        _uiState.value = WeatherUiState.Loading

        viewModelScope.launch {
            try {
                val data = repository.fetchWeatherDashboard(city)
                _uiState.value = WeatherUiState.Success(data = data)
            } catch (e: IOException) {
                _uiState.value = WeatherUiState.Error(
                    message = "Network connection issue. Please check your network and tap Retry."
                )
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(
                    message = "Failed to load weather forecast: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    fun refreshWeather() {
        val currentCity = _selectedCity.value ?: return
        val currentData = (_uiState.value as? WeatherUiState.Success)?.data

        if (currentData != null) {
            _uiState.value = WeatherUiState.Success(data = currentData, isRefreshing = true)
        } else {
            _uiState.value = WeatherUiState.Loading
        }

        viewModelScope.launch {
            try {
                val data = repository.fetchWeatherDashboard(currentCity)
                _uiState.value = WeatherUiState.Success(data = data)
            } catch (e: Exception) {
                if (currentData != null) {
                    _uiState.value = WeatherUiState.Success(data = currentData, isRefreshing = false)
                } else {
                    _uiState.value = WeatherUiState.Error("Refresh failed. Check network connection.")
                }
            }
        }
    }

    fun toggleTemperatureUnit() {
        _temperatureUnit.value = if (_temperatureUnit.value == TemperatureUnit.CELSIUS) {
            TemperatureUnit.FAHRENHEIT
        } else {
            TemperatureUnit.CELSIUS
        }
    }

    fun removeRecentCity(entity: RecentCityEntity) {
        viewModelScope.launch {
            repository.removeRecentCity(entity)
        }
    }

    fun clearAllRecent() {
        viewModelScope.launch {
            repository.clearAllRecent()
        }
    }

    class Factory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WeatherViewModel(repository) as T
        }
    }
}
