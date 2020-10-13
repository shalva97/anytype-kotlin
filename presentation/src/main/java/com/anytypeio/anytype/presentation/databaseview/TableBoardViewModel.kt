package com.anytypeio.anytype.presentation.databaseview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.domain.database.interactor.GetDatabase
import com.anytypeio.anytype.presentation.databaseview.mapper.toPresentation
import com.anytypeio.anytype.presentation.databaseview.models.Table
import timber.log.Timber

class TableBoardViewModel(
    private val getDatabase: GetDatabase
) : ViewStateViewModel<ViewState<Table>>() {

    fun onViewCreated() {
        stateData.postValue(ViewState.Init)
    }

    fun getDatabaseView(id: String) {
        getDatabase.invoke(viewModelScope, GetDatabase.Params(id)) { result ->
            result.either(
                fnL = { e -> Timber.e("Error while getting database for id=$id ${e.message}") },
                fnR = { stateData.postValue(ViewState.Success(it.toPresentation())) }
            )
        }
    }
}

@Suppress("UNCHECKED_CAST")
class TableBoardViewModelFactory(
    private val getDatabase: GetDatabase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TableBoardViewModel(getDatabase = getDatabase) as T
    }
}