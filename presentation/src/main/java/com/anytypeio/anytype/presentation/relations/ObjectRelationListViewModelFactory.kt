package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.dataview.interactor.ObjectRelationList
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.page.Editor
import com.anytypeio.anytype.presentation.page.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.util.Dispatcher

class ObjectRelationListViewModelFactory(
    private val stores: Editor.Storage,
    private val urlBuilder: UrlBuilder,
    private val objectRelationList: ObjectRelationList,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDetail: UpdateDetail,
    private val detailModificationManager: DetailModificationManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ObjectRelationListViewModel(
            stores = stores,
            urlBuilder = urlBuilder,
            objectRelationList = objectRelationList,
            dispatcher = dispatcher,
            updateDetail = updateDetail,
            detailModificationManager = detailModificationManager
        ) as T
    }
}