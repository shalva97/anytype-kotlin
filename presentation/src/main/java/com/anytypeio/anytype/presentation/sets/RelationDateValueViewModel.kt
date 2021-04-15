package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

const val TIME_FORMAT_DEFAULT = "dd MMMM yyyy"

class RelationDateValueViewModel(
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider
) : ViewModel() {

    val commands = MutableSharedFlow<DateValueCommand>(0)
    private val _views = MutableStateFlow(DateValueView())
    val views: StateFlow<DateValueView> = _views

    fun onStart(relationId: Id, objectId: String) {
        val relation = relations.get(relationId)
        val values = values.get(objectId)
        setName(relation.name)
        setDate(timeInSeconds = getTimestampFromValue(values[relationId]))
    }

    fun onTodayClicked() {
        val calendar = Calendar.getInstance()
        val timeInSeconds = calendar.timeInMillis / 1000
        setDate(timeInSeconds = timeInSeconds)
    }

    fun onTomorrowClicked() {
        val calendar = Calendar.getInstance().apply { add(Calendar.DATE, 1) }
        val timeInSeconds = calendar.timeInMillis / 1000
        setDate(timeInSeconds = timeInSeconds)
    }

    fun onYesterdayClicked() {
        val calendar = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        val timeInSeconds = calendar.timeInMillis / 1000
        setDate(timeInSeconds = timeInSeconds)
    }

    fun onExactDayClicked() {
        viewModelScope.launch {
            commands.emit(
                DateValueCommand.OpenDatePicker(
                    timeInSeconds = views.value.timeInSeconds
                )
            )
        }
    }

    fun onClearClicked() {
        viewModelScope.launch {
            commands.emit(DateValueCommand.DispatchResult(timeInSeconds = null))
        }
    }

    fun onActionClicked() {
        viewModelScope.launch {
            commands.emit(
                DateValueCommand.DispatchResult(
                    timeInSeconds = views.value.timeInSeconds?.toDouble()
                )
            )
        }
    }

    private fun setName(name: String?) {
        _views.value = views.value.copy(
            title = name
        )
    }

    fun setDate(timeInSeconds: Long?) {
        if (timeInSeconds != null) {
            val exactDay = Calendar.getInstance()
            exactDay.timeInMillis = timeInSeconds * 1000
            val today = getTodayTimeUnit()
            val tomorrow = getTomorrowTimeUnit()
            val yesterday = getYesterdayTimeUnit()
            val isToday = exactDay.isSameDay(today)
            val isTomorrow = exactDay.isSameDay(tomorrow)
            val isYesterday = exactDay.isSameDay(yesterday)
            var exactDayFormat: String? = null
            if (!isToday && !isTomorrow && !isYesterday) {
                exactDayFormat = timeInSeconds.timeInSecondsFormat(TIME_FORMAT_DEFAULT)
            }
            _views.value = views.value.copy(
                isToday = isToday,
                isYesterday = isYesterday,
                isTomorrow = isTomorrow,
                exactDayFormat = exactDayFormat,
                timeInSeconds = timeInSeconds
            )
        } else {
            _views.value = views.value.copy(
                isToday = false,
                isYesterday = false,
                isTomorrow = false,
                exactDayFormat = null,
                timeInSeconds = null
            )
        }
    }

    private fun getTimestampFromValue(value: Any?): Long? = when (value) {
        is Double -> value.toLong()
        is Long -> value
        null -> null
        else -> throw IllegalArgumentException("Relation date value wrong format:$this")
    }

    class Factory(
        private val relations: ObjectRelationProvider,
        private val values: ObjectValueProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RelationDateValueViewModel(relations, values) as T
        }
    }
}

sealed class DateValueCommand {
    data class DispatchResult(val timeInSeconds: Double?) : DateValueCommand()
    data class OpenDatePicker(val timeInSeconds: Long?) : DateValueCommand()
}

data class DateValueView(
    val title: String? = null,
    val isToday: Boolean = false,
    val isYesterday: Boolean = false,
    val isTomorrow: Boolean = false,
    val exactDayFormat: String? = null,
    val timeInSeconds: Long? = null
)