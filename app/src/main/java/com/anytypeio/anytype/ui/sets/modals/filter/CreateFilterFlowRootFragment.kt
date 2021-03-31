package com.anytypeio.anytype.ui.sets.modals.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.tools.BottomSheetSharedTransition
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.presentation.sets.filter.CreateFilterFlowViewModel
import com.anytypeio.anytype.presentation.sets.filter.CreateFilterFlowViewModel.Step
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.ui.sets.modals.ViewerBottomSheetRootFragment

class CreateFilterFlowRootFragment : BaseBottomSheetFragment(), CreateFilterFlow {

    private val ctx: String get() = arg(CTX_KEY)

    val vm by lazy { CreateFilterFlowViewModel() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_viewer_bottom_sheet_root, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lifecycleScope.subscribe(vm.step) { step ->
            when (step) {
                is Step.SelectRelation -> transitToSelection()
                is Step.CreateFilter -> transitToCreation(step)
            }
        }
    }

    override fun onRelationSelected(ctx: Id, relation: SimpleRelationView) {
        vm.onRelationSelected(ctx = ctx, relation = relation.key, format = relation.format)
    }

    override fun onFilterCreated() {
        dismiss()
    }

    private fun transitToCreation(step: Step.CreateFilter) {
        val fr = when (step.type) {
            Step.CreateFilter.Type.INPUT_FIELD -> {
                CreateFilterFromInputFieldValueFragment.new(
                    ctx = step.ctx,
                    relation = step.relation
                )
            }
            else -> {
                CreateFilterFromSelectedValueFragment.new(
                    ctx = step.ctx,
                    relation = step.relation
                )
            }
        }

        val currentFragmentRoot = childFragmentManager.fragments[0].requireView()

        childFragmentManager
            .beginTransaction()
            .apply {
                addSharedElement(currentFragmentRoot, currentFragmentRoot.transitionName)
                setReorderingAllowed(true)
                fr.sharedElementEnterTransition = BottomSheetSharedTransition()
            }
            .replace(R.id.container, fr)
            .addToBackStack(fr.javaClass.name)
            .commit()
    }

    private fun transitToSelection() {
        val fr = SelectFilterRelationFragment.new(ctx)
        childFragmentManager
            .beginTransaction()
            .add(R.id.container, fr)
            .addToBackStack(ViewerBottomSheetRootFragment.TAG_ROOT)
            .commit()
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        fun new(ctx: Id): CreateFilterFlowRootFragment = CreateFilterFlowRootFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx)
        }

        private const val CTX_KEY = "arg.create-filter-flow-root.ctx"
    }
}