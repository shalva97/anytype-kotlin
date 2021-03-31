package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_utils.ext.withLatestFrom
import com.anytypeio.anytype.presentation.common.BaseListViewModel
import com.anytypeio.anytype.presentation.relations.simpleRelations
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Inherit this class in order to enable search-for-relations feature.
 */
abstract class SearchRelationViewModel(
    private val objectSetState: StateFlow<ObjectSet>,
    private val session: ObjectSetSession
) : BaseListViewModel<SimpleRelationView>() {

    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)

    private val query = Channel<String>()

    init {
        // Initializing views before any query.
        viewModelScope.launch {
            _views.value = objectSetState.value.simpleRelations(session.currentViewerId).filter { !it.isHidden }
        }
        // Searching and mapping views based on query changes.
        viewModelScope.launch {
            query
                .consumeAsFlow()
                .withLatestFrom(objectSetState) { query, state ->
                    val relations = state.simpleRelations(session.currentViewerId)
                    if (query.isEmpty()) {
                        relations
                    } else {
                        relations.filter { relation ->
                            relation.title.contains(query, ignoreCase = true)
                        }
                    }
                }
                .mapLatest { relations -> relations.filter { !it.isHidden } }
                .collect { _views.value = it }
        }
    }

    fun onSearchQueryChanged(txt: String) {
        viewModelScope.launch { query.send(txt) }
    }
}