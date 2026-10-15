package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.WeatherDatabase
import com.example.data.network.OpenMeteoApiService
import com.example.domain.WeatherRepository
import com.example.ui.WeatherUiState
import com.example.ui.WeatherViewModel
import com.example.ui.components.CityNotFoundView
import com.example.ui.components.CurrentWeatherHero
import com.example.ui.components.Forecast7DayView
import com.example.ui.components.NetworkErrorView
import com.example.ui.components.PlanningRecommendationsView
import com.example.ui.components.SearchBarWithAutocomplete
import com.example.ui.components.TemperatureTrendChart
import com.example.ui.components.WeatherMetricsGrid
import com.example.ui.theme.WeatherIntelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val apiService = OpenMeteoApiService.create()
        val database = WeatherDatabase.getInstance(applicationContext)
        val repository = WeatherRepository(
            apiService = apiService,
            recentCityDao = database.recentCityDao()
        )

        setContent {
            WeatherIntelTheme {
                val viewModel: WeatherViewModel = viewModel(
                    factory = WeatherViewModel.Factory(repository)
                )
                WeatherAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAppScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val autocompleteResults by viewModel.autocompleteResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val recentCities by viewModel.recentCities.collectAsState()
    val tempUnit by viewModel.temperatureUnit.collectAsState()
    val popularCities = viewModel.popularCities

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("app_header_title")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = "Weather Intel Icon",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Weather Intel",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Live Open-Meteo Intelligence",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Live",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 700.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Search Bar with Autocomplete suggestions and recent searches
                SearchBarWithAutocomplete(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onSearchSubmit = viewModel::searchCityDirectly,
                    autocompleteSuggestions = autocompleteResults,
                    isSearching = isSearching,
                    recentCities = recentCities,
                    popularCities = popularCities,
                    onSelectCity = viewModel::selectCity,
                    onSelectRecent = viewModel::selectRecentCity,
                    onRemoveRecent = viewModel::removeRecentCity,
                    onClearAllRecent = viewModel::clearAllRecent
                )

                // 2. Weather Dashboard Content based on UI State
                AnimatedContent(
                    targetState = uiState,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "weather_ui_state_transition"
                ) { state ->
                    when (state) {
                        is WeatherUiState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(380.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(48.dp),
                                        strokeWidth = 3.5.dp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Fetching live weather intelligence...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        is WeatherUiState.Success -> {
                            val data = state.data
                            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                // Hero Weather Card
                                CurrentWeatherHero(
                                    location = data.location,
                                    current = data.current,
                                    todayForecast = data.dailyForecasts.firstOrNull(),
                                    unit = tempUnit,
                                    isRefreshing = state.isRefreshing,
                                    onRefresh = viewModel::refreshWeather,
                                    onToggleUnit = viewModel::toggleTemperatureUnit
                                )

                                // Highlights Grid Cards
                                WeatherMetricsGrid(
                                    current = data.current,
                                    todayForecast = data.dailyForecasts.firstOrNull(),
                                    unit = tempUnit
                                )

                                // 7-Day Forecast Cards
                                Forecast7DayView(
                                    forecasts = data.dailyForecasts,
                                    unit = tempUnit
                                )

                                // Temperature Analytics Line Chart
                                TemperatureTrendChart(
                                    forecasts = data.dailyForecasts,
                                    unit = tempUnit
                                )

                                // Actionable Planning Recommendations
                                PlanningRecommendationsView(
                                    planning = data.planning
                                )

                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }

                        is WeatherUiState.CityNotFound -> {
                            CityNotFoundView(
                                searchedQuery = state.searchedQuery,
                                popularCities = popularCities,
                                onSelectCity = viewModel::selectCity
                            )
                        }

                        is WeatherUiState.Error -> {
                            NetworkErrorView(
                                message = state.message,
                                onRetry = viewModel::refreshWeather
                            )
                        }
                    }
                }
            }
        }
    }
}
