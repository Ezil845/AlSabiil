package al.sabil.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import al.sabil.model.SunnahItem
import al.sabil.repository.SunnahRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SunnahViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SunnahRepository(application)
    private val settingsManager = al.sabil.data.SettingsManager(application)

    private val hijriOffset = settingsManager.settingsFlow.map { it.hijriOffset }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val sunnahItems: StateFlow<List<SunnahItem>> = hijriOffset.map { offset ->
        repository.getSunnahItems(offset)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val checkedItems: StateFlow<Set<String>> = combine(
        repository.getSunnahStatusFlow(),
        sunnahItems
    ) { statusMap, items ->
        items.filter { item ->
            val lastUpdated = statusMap[item.id]
            lastUpdated != null && repository.isTaskValidForToday(lastUpdated, item.category)
        }.map { it.id }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val progressString: StateFlow<String> = combine(checkedItems, sunnahItems) { checked, all ->
        if (all.isEmpty()) "0/0" else "${checked.size}/${all.size}"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0/0")

    fun toggleItem(itemId: String, isChecked: Boolean) {
        viewModelScope.launch {
            repository.toggleSunnahItem(itemId, isChecked)
        }
    }
}
