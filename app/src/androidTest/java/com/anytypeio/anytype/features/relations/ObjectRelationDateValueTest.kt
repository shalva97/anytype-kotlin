package com.anytypeio.anytype.features.relations

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_utils.ext.timeInSecondsFormat
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectValueProvider
import com.anytypeio.anytype.presentation.sets.ObjectRelationDateValueViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.TIME_FORMAT_DEFAULT
import com.anytypeio.anytype.ui.relations.ObjectRelationDateValueFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import kotlinx.coroutines.flow.MutableStateFlow
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import java.util.*

@RunWith(AndroidJUnit4::class)
@LargeTest
class ObjectRelationDateValueTest {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    val root = MockDataFactory.randomUuid()

    private val state = MutableStateFlow(ObjectSet.init())
    private val session = ObjectSetSession()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        TestObjectRelationDateValueFragment.testVmFactory =
            ObjectRelationDateValueViewModel.Factory(
                relations = DataViewObjectRelationProvider(state),
                values = DataViewObjectValueProvider(state, session)
            )
    }

    @Test
    fun shouldSetNullDateValue() {

        // SETUP

        val relationText = "Birth date"
        val valueDate: Long? = null

        val target = MockDataFactory.randomUuid()

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            name = relationText,
            format = Relation.Format.DATE,
            source = Relation.Source.values().random()
        )

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relation.key to valueDate
        )

        state.value = ObjectSet(
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        relations = listOf(relation),
                        viewers = listOf(viewer),
                        source = MockDataFactory.randomUuid()
                    )
                )
            ),
            viewerDb = mapOf(
                viewer.id to ObjectSet.ViewerData(
                    records = listOf(record),
                    total = 1
                )
            )
        )

        launchFragment(
            bundleOf(
                ObjectRelationDateValueFragment.CONTEXT_ID to root,
                ObjectRelationDateValueFragment.RELATION_ID to relation.key,
                ObjectRelationDateValueFragment.OBJECT_ID to target,
                ObjectRelationDateValueFragment.FLOW_KEY to ObjectRelationDateValueFragment.FLOW_DATAVIEW
            )
        )

        onView(withId(R.id.ivTodayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivYesterdayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTomorrowCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText("")))
    }

    @Test
    fun shouldSetTodayDateValue() {

        // SETUP

        val relationText = "Birth date"
        val valueDate: Long = System.currentTimeMillis() / 1000

        val target = MockDataFactory.randomUuid()

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            name = relationText,
            format = Relation.Format.DATE,
            source = Relation.Source.values().random()
        )

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relation.key to valueDate
        )

        state.value = ObjectSet(
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        relations = listOf(relation),
                        viewers = listOf(viewer),
                        source = MockDataFactory.randomUuid()
                    )
                )
            ),
            viewerDb = mapOf(
                viewer.id to ObjectSet.ViewerData(
                    records = listOf(record),
                    total = 1
                )
            )
        )

        launchFragment(
            bundleOf(
                ObjectRelationDateValueFragment.CONTEXT_ID to root,
                ObjectRelationDateValueFragment.RELATION_ID to relation.key,
                ObjectRelationDateValueFragment.OBJECT_ID to target,
                ObjectRelationDateValueFragment.FLOW_KEY to ObjectRelationDateValueFragment.FLOW_DATAVIEW
            )
        )

        onView(withId(R.id.ivTodayCheck)).check(matches(isDisplayed()))
        onView(withId(R.id.ivYesterdayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTomorrowCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText("")))
    }

    @Test
    fun shouldSetTomorrowDateValue() {

        // SETUP

        val relationText = "Birth date"
        val calendar = Calendar.getInstance().apply { add(Calendar.DATE, 1) }
        val valueDate: Long = calendar.timeInMillis / 1000

        val target = MockDataFactory.randomUuid()

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            name = relationText,
            format = Relation.Format.DATE,
            source = Relation.Source.values().random()
        )

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relation.key to valueDate
        )

        state.value = ObjectSet(
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        relations = listOf(relation),
                        viewers = listOf(viewer),
                        source = MockDataFactory.randomUuid()
                    )
                )
            ),
            viewerDb = mapOf(
                viewer.id to ObjectSet.ViewerData(
                    records = listOf(record),
                    total = 1
                )
            )
        )

        launchFragment(
            bundleOf(
                ObjectRelationDateValueFragment.CONTEXT_ID to root,
                ObjectRelationDateValueFragment.RELATION_ID to relation.key,
                ObjectRelationDateValueFragment.OBJECT_ID to target,
                ObjectRelationDateValueFragment.FLOW_KEY to ObjectRelationDateValueFragment.FLOW_DATAVIEW
            )
        )

        onView(withId(R.id.ivTomorrowCheck)).check(matches((isDisplayed())))
        onView(withId(R.id.ivTodayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivYesterdayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText("")))
    }

    @Test
    fun shouldSetYesterdayDateValue() {

        // SETUP

        val relationText = "Birth date"
        val calendar = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        val valueDate: Long = calendar.timeInMillis / 1000

        val target = MockDataFactory.randomUuid()

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            name = relationText,
            format = Relation.Format.DATE,
            source = Relation.Source.values().random()
        )

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relation.key to valueDate
        )

        state.value = ObjectSet(
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        relations = listOf(relation),
                        viewers = listOf(viewer),
                        source = MockDataFactory.randomUuid()
                    )
                )
            ),
            viewerDb = mapOf(
                viewer.id to ObjectSet.ViewerData(
                    records = listOf(record),
                    total = 1
                )
            )
        )

        launchFragment(
            bundleOf(
                ObjectRelationDateValueFragment.CONTEXT_ID to root,
                ObjectRelationDateValueFragment.RELATION_ID to relation.key,
                ObjectRelationDateValueFragment.OBJECT_ID to target,
                ObjectRelationDateValueFragment.FLOW_KEY to ObjectRelationDateValueFragment.FLOW_DATAVIEW
            )
        )

        onView(withId(R.id.ivYesterdayCheck)).check(matches((isDisplayed())))
        onView(withId(R.id.ivTodayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTomorrowCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText("")))
    }

    @Test
    fun shouldSetExactDayDateValue() {

        // SETUP

        val relationText = "Birth date"
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 45) }
        val valueDate: Long = calendar.timeInMillis / 1000

        val target = MockDataFactory.randomUuid()

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            name = relationText,
            format = Relation.Format.DATE,
            source = Relation.Source.values().random()
        )

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relation.key to valueDate
        )

        state.value = ObjectSet(
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        relations = listOf(relation),
                        viewers = listOf(viewer),
                        source = MockDataFactory.randomUuid()
                    )
                )
            ),
            viewerDb = mapOf(
                viewer.id to ObjectSet.ViewerData(
                    records = listOf(record),
                    total = 1
                )
            )
        )

        launchFragment(
            bundleOf(
                ObjectRelationDateValueFragment.CONTEXT_ID to root,
                ObjectRelationDateValueFragment.RELATION_ID to relation.key,
                ObjectRelationDateValueFragment.OBJECT_ID to target,
                ObjectRelationDateValueFragment.FLOW_KEY to ObjectRelationDateValueFragment.FLOW_DATAVIEW
            )
        )

        val exactDateFormat = valueDate.timeInSecondsFormat(TIME_FORMAT_DEFAULT)
        onView(withId(R.id.ivYesterdayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTodayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTomorrowCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches((isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText(exactDateFormat)))
    }

    @Test
    fun shouldSetExactDayDateValueAndThenUpdateWithDatePicker() {

        // SETUP

        val relationText = "Birth date"
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 45) }
        val valueDate: Long = calendar.timeInMillis / 1000

        val target = MockDataFactory.randomUuid()

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            name = relationText,
            format = Relation.Format.DATE,
            source = Relation.Source.values().random()
        )

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relation.key to valueDate
        )

        state.value = ObjectSet(
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        relations = listOf(relation),
                        viewers = listOf(viewer),
                        source = MockDataFactory.randomUuid()
                    )
                )
            ),
            viewerDb = mapOf(
                viewer.id to ObjectSet.ViewerData(
                    records = listOf(record),
                    total = 1
                )
            )
        )

        val fragment = launchFragment(
            bundleOf(
                ObjectRelationDateValueFragment.CONTEXT_ID to root,
                ObjectRelationDateValueFragment.RELATION_ID to relation.key,
                ObjectRelationDateValueFragment.OBJECT_ID to target,
                ObjectRelationDateValueFragment.FLOW_KEY to ObjectRelationDateValueFragment.FLOW_DATAVIEW
            )
        )

        val exactDateFormat = valueDate.timeInSecondsFormat(TIME_FORMAT_DEFAULT)
        onView(withId(R.id.ivYesterdayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTodayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTomorrowCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches((isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText(exactDateFormat)))

        val dateUpdate = Calendar.getInstance().apply { add(Calendar.DATE, 17) }
        val valueUpdate: Long = dateUpdate.timeInMillis / 1000
        fragment.onFragment {
            it.onPickDate(valueUpdate)
        }

        val updatedDateFormat = valueUpdate.timeInSecondsFormat(TIME_FORMAT_DEFAULT)
        onView(withId(R.id.ivTomorrowCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTodayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivYesterdayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches((isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText(updatedDateFormat)))
    }

    @Test
    fun shouldSetExactDayDateValueAndThenUpdateToTodayTomorrowAndYesterday() {

        // SETUP

        val relationText = "Birth date"
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 45) }
        val valueDate: Long = calendar.timeInMillis / 1000

        val target = MockDataFactory.randomUuid()

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            name = relationText,
            format = Relation.Format.DATE,
            source = Relation.Source.values().random()
        )

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relation.key to valueDate
        )

        state.value = ObjectSet(
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        relations = listOf(relation),
                        viewers = listOf(viewer),
                        source = MockDataFactory.randomUuid()
                    )
                )
            ),
            viewerDb = mapOf(
                viewer.id to ObjectSet.ViewerData(
                    records = listOf(record),
                    total = 1
                )
            )
        )

        val fragment = launchFragment(
            bundleOf(
                ObjectRelationDateValueFragment.CONTEXT_ID to root,
                ObjectRelationDateValueFragment.RELATION_ID to relation.key,
                ObjectRelationDateValueFragment.OBJECT_ID to target,
                ObjectRelationDateValueFragment.FLOW_KEY to ObjectRelationDateValueFragment.FLOW_DATAVIEW
            )
        )

        val exactDateFormat = valueDate.timeInSecondsFormat(TIME_FORMAT_DEFAULT)
        onView(withId(R.id.ivYesterdayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTodayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTomorrowCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches((isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText(exactDateFormat)))

        fragment.onFragment { it.vm.onTodayClicked() }

        onView(withId(R.id.ivTomorrowCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTodayCheck)).check(matches((isDisplayed())))
        onView(withId(R.id.ivYesterdayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText("")))

        fragment.onFragment { it.vm.onTomorrowClicked() }

        onView(withId(R.id.ivTomorrowCheck)).check(matches((isDisplayed())))
        onView(withId(R.id.ivTodayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivYesterdayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText("")))

        fragment.onFragment { it.vm.onYesterdayClicked() }

        onView(withId(R.id.ivTomorrowCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTodayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivYesterdayCheck)).check(matches((isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText("")))
    }


    private fun launchFragment(args: Bundle): FragmentScenario<TestObjectRelationDateValueFragment> {
        return launchFragmentInContainer<TestObjectRelationDateValueFragment>(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}