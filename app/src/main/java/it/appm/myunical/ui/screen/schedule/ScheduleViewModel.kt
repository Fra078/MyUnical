package it.appm.myunical.ui.screen.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.appm.myunical.data.MyUnical
import it.appm.myunical.model.Evento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModel : ViewModel() {
    private val _selectedMonth : MutableStateFlow<CalendarInfo>
    val selectedMonth : StateFlow<CalendarInfo>
    private val _isRefreshing : MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isRefreshing : StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _events = MutableStateFlow<List<EventItem>>(emptyList())
    val events = _events.asStateFlow()

    init {
        val calendar = Calendar.getInstance()
        _selectedMonth = MutableStateFlow(
            CalendarInfo(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH)+1,
                calendar.get(Calendar.YEAR)
            )
        )
        selectedMonth = _selectedMonth.asStateFlow()

        viewModelScope.launch(Dispatchers.Default) {
            selectedMonth.flatMapLatest {
                MyUnical.dataRepository.getSchedule(it.year, it.month)
            }.mapLatest { eventi ->
                buildList {
                    eventi.forEachIndexed { index, evento ->
                        if (eventi.getOrNull(index-1)?.day != evento.day)
                            add(EventItem.Day(evento.year, evento.month, evento.day))
                        add(EventItem.Event(evento.title, evento.location, evento.startOffset, evento.endOffset))
                    }
                }
            }.collect(_events)
        }

        viewModelScope.launch(Dispatchers.IO) {
            selectedMonth.collectLatest {
                updateSchedule(it)
            }
        }
    }

    fun goBack(){
        _selectedMonth.getAndUpdate {
            val newYear = if (it.month == 1) it.year - 1 else it.year
            val newMonth = if (it.month == 1) 12 else it.month - 1
            it.copy(newYear, newMonth)
        }
    }

    fun goForward(){
        _selectedMonth.getAndUpdate {
            val newYear = if (it.month == 12) it.year + 1 else it.year
            val newMonth = if (it.month == 12) 1 else it.month + 1
            it.copy(newYear, newMonth)
        }
    }

    suspend fun updateSchedule(calendarInfo: CalendarInfo){
        _isRefreshing.value = true
        MyUnical.dataRepository.aggiornaSchedule(calendarInfo.year, calendarInfo.month)
        _isRefreshing.value = false
    }

    fun forceUpdate(){
        viewModelScope.launch(Dispatchers.IO) {
            updateSchedule(selectedMonth.value)
        }
    }

    data class CalendarInfo(val year: Int, val month: Int, val currentYear: Int)

    sealed class EventItem {
        data class Day(val year: Int, val month: Int, val day: Int) : EventItem() {
            override fun toString(): String {
                val dayOfWeek = LocalDate.of(year, month, day).dayOfWeek
                    .getDisplayName(TextStyle.FULL, Locale.getDefault())
                return "$dayOfWeek $day"
            }
        }
        data class Event(
            val title: String,
            val location: String,
            val startOffset: Int,
            val endOffset: Int
        ) : EventItem()
    }
}