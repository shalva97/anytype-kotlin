package com.anytypeio.anytype.ui.sets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_ui.features.dataview.modals.FilterByAdapter
import com.anytypeio.anytype.core_ui.layout.DividerVerticalItemDecoration
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.filter.ViewerFilterCommand
import com.anytypeio.anytype.presentation.sets.filter.ViewerFilterViewModel
import com.anytypeio.anytype.ui.sets.modals.ViewerBottomSheetRootFragment
import com.anytypeio.anytype.ui.sets.modals.filter.CreateFilterFlowRootFragment
import com.anytypeio.anytype.ui.sets.modals.filter.ModifyFilterFromInputFieldValueFragment
import com.anytypeio.anytype.ui.sets.modals.filter.ModifyFilterFromSelectedValueFragment
import kotlinx.android.synthetic.main.fragment_filter.*
import javax.inject.Inject

class ViewerFilterFragment : BaseBottomSheetFragment() {

    private val ctx get() = argString(CONTEXT_ID_KEY)

    private val filterAdapter by lazy {
        FilterByAdapter(
            click = { click -> vm.onFilterClicked(ctx, click) }
        )
    }

    @Inject
    lateinit var factory: ViewerFilterViewModel.Factory
    private val vm: ViewerFilterViewModel by viewModels { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_filter, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = filterAdapter
        with(lifecycleScope) {
            subscribe(vm.commands) { observeCommands(it) }
            subscribe(resetBtn.clicks()) { vm.onResetButtonClicked(ctx) }
            subscribe(doneBtn.clicks()) { vm.onDoneButtonClicked() }
            subscribe(editBtn.clicks()) { vm.onEditButtonClicked() }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(lifecycleScope) {
            subscribe(vm.views) { filterAdapter.update(it) }
            subscribe(vm.screenState) { render(it) }
        }
    }

    private fun render(state: ViewerFilterViewModel.ScreenState) {
        when (state) {
            ViewerFilterViewModel.ScreenState.LIST -> {
                editBtn.visible()
                resetBtn.visible()
                doneBtn.invisible()
                removeDivider()
                recycler.addItemDecoration(
                    DividerVerticalItemDecoration(
                        divider = requireContext().drawable(R.drawable.divider_filter_list),
                        isShowInLastItem = false
                    ),
                    0
                )
            }
            ViewerFilterViewModel.ScreenState.EDIT -> {
                doneBtn.visible()
                editBtn.invisible()
                resetBtn.invisible()
                removeDivider()
                recycler.addItemDecoration(
                    DividerVerticalItemDecoration(
                        divider = requireContext().drawable(R.drawable.divider_filter_edit),
                        isShowInLastItem = false
                    ),
                    0
                )
            }
        }
    }

    private fun removeDivider() {
        if (recycler.itemDecorationCount > 0) recycler.removeItemDecorationAt(0)
    }

    private fun observeCommands(command: ViewerFilterCommand) {
        when (command) {
            is ViewerFilterCommand.Modal.ShowRelationList -> {
                val fr = CreateFilterFlowRootFragment.new(ctx)
                fr.show(parentFragmentManager, null)
            }
            is ViewerFilterCommand.Apply -> dispatchResultAndDismiss(command)
            is ViewerFilterCommand.BackToCustomize -> exitToCustomizeScreen()
            is ViewerFilterCommand.Modal.UpdateInputValueFilter -> {
                val fr = ModifyFilterFromInputFieldValueFragment.new(
                    ctx = ctx,
                    relation = command.relation,
                    index = command.filterIndex
                )
                fr.show(childFragmentManager, fr.javaClass.canonicalName)
            }
            is ViewerFilterCommand.Modal.UpdateSelectValueFilter -> {
                val fr = ModifyFilterFromSelectedValueFragment.new(
                    ctx = ctx,
                    relation = command.relation,
                    index = command.filterIndex
                )
                fr.show(childFragmentManager, fr.javaClass.canonicalName)
            }
        }
    }

    private fun exitToCustomizeScreen() {
        withParent<ViewerBottomSheetRootFragment> { transitToCustomize() }
    }

    private fun dispatchResultAndDismiss(command: ViewerFilterCommand.Apply) {
        withParent<ViewerBottomSheetRootFragment> { dispatchResultFiltersAndDismiss(command.filters) }
    }

    override fun injectDependencies() {
        componentManager().viewerFilterComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().viewerFilterComponent.release(ctx)
    }

    companion object {
        const val CONTEXT_ID_KEY = "arg.viewer.filters.context"

        fun new(ctx: Id) = ViewerFilterFragment().apply {
            arguments = bundleOf(CONTEXT_ID_KEY to ctx)
        }
    }
}