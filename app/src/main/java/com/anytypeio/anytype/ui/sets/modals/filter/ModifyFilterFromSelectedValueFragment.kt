package com.anytypeio.anytype.ui.sets.modals.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.extensions.relationIcon
import com.anytypeio.anytype.core_ui.features.sets.CreateFilterAdapter
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.filter.FilterViewModel
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.ui.sets.modals.DatePickerFragment
import com.anytypeio.anytype.ui.sets.modals.DatePickerFragment.DatePickerReceiver
import com.anytypeio.anytype.ui.sets.modals.PickFilterConditionFragment
import kotlinx.android.synthetic.main.fragment_create_or_update_filter.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

open class ModifyFilterFromSelectedValueFragment : BaseBottomSheetFragment(),
    UpdateConditionActionReceiver, DatePickerReceiver {

    private val ctx: String get() = arg(CTX_KEY)
    private val relation: String get() = arg(RELATION_KEY)
    private val index: Int get() = arg(IDX_KEY)

    @Inject
    lateinit var factory: FilterViewModel.Factory

    private val vm: FilterViewModel by viewModels { factory }

    private val createFilterAdapter by lazy {
        CreateFilterAdapter(
            onItemClicked = vm::onFilterItemClicked
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_or_update_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnBottomAction.setText(R.string.apply)
        rvViewerFilterRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = createFilterAdapter
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(drawable(R.drawable.divider_relations))
                }
            )
        }
        with(lifecycleScope) {
            subscribe(tvFilterCondition.clicks()) {
                vm.onConditionClicked()
            }
            subscribe(btnBottomAction.clicks()) {
                vm.onModifyApplyClicked(ctx = ctx)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(lifecycleScope) {
            subscribe(vm.relationState.filterNotNull()) {
                if (it.format == ColumnView.Format.DATE) {
                    searchBar.gone()
                    tvOptionCount.gone()
                }
                tvRelationName.text = it.title
                ivRelationIcon.setImageResource(it.format.relationIcon(true))
            }
            subscribe(vm.optionCountState) { tvOptionCount.text = it.toString() }
            subscribe(vm.isCompleted) { isCompleted -> if (isCompleted) dismiss() }
            subscribe(vm.conditionState) {
                tvFilterCondition.text = it?.condition?.title
                if (it?.isFilterValueEnabled == true) {
                    searchBar.visible()
                    tvOptionCount.visible()
                } else {
                    searchBar.gone()
                    tvOptionCount.gone()
                }
            }
            val queries = searchRelationInput.textChanges()
                .onStart { emit(searchRelationInput.text.toString()) }
            val views = vm.filterValueListState
                .combine(queries) { views, query ->
                    views.filter {
                        it.text.contains(query, true)
                    }
                }
            subscribe(views) { createFilterAdapter.update(it) }
            subscribe(vm.commands) { observeCommands(it) }
        }
    }

    private fun observeCommands(commands: FilterViewModel.Commands) {
        when (commands) {
            is FilterViewModel.Commands.OpenDatePicker -> {
                DatePickerFragment.new(commands.timeInMillis)
                    .show(childFragmentManager, null)
            }
            is FilterViewModel.Commands.OpenConditionPicker -> {
                PickFilterConditionFragment.new(
                    ctx = ctx,
                    mode = PickFilterConditionFragment.MODE_MODIFY,
                    type = commands.type,
                    index = commands.index
                )
                    .show(childFragmentManager, null)
            }
        }
    }

    override fun update(condition: Viewer.Filter.Condition) {
        vm.onConditionUpdate(condition)
    }

    override fun onPickDate(timeInSeconds: Long) {
        vm.onExactDayPicked(timeInSeconds)
    }

    override fun onStart() {
        super.onStart()
        vm.onStart(relation, index)
        expand()
    }

    override fun injectDependencies() {
        componentManager().modifyFilterComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().modifyFilterComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id, relation: Id, index: Int): ModifyFilterFromSelectedValueFragment = ModifyFilterFromSelectedValueFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx, RELATION_KEY to relation, IDX_KEY to index)
        }

        const val CTX_KEY = "arg.modify-filter-relation.ctx"
        const val RELATION_KEY = "arg.modify-filter-relation.relation"
        const val IDX_KEY = "arg.modify-filter-relation.index"
    }
}