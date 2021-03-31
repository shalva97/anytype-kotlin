package com.anytypeio.anytype.features.relations

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewRelationOption
import com.anytypeio.anytype.domain.dataview.interactor.AddTagToDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.RemoveTagFromDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewRecord
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.page.editor.ThemeColor
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectValueProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectTypeProvider
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetObjectRelationValueViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.database.modals.ObjectRelationValueFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.anytypeio.anytype.utils.TestUtils.withRecyclerView
import com.anytypeio.anytype.utils.WithTextColor
import com.anytypeio.anytype.utils.WithTextColorRes
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import kotlinx.coroutines.flow.MutableStateFlow
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
@LargeTest
class DisplayRelationTagValueTest {

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var dispatcher: Dispatcher<Payload>

    private lateinit var addRelationOption: AddDataViewRelationOption
    private lateinit var removeTagFromDataViewRecord: RemoveTagFromDataViewRecord
    private lateinit var addTagToDataViewRecord: AddTagToDataViewRecord
    private lateinit var updateDataViewRecord: UpdateDataViewRecord
    private lateinit var updateDetail: UpdateDetail
    private lateinit var urlBuilder: UrlBuilder

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val root = MockDataFactory.randomUuid()
    private val state = MutableStateFlow(ObjectSet.init())
    private val session = ObjectSetSession()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        addRelationOption = AddDataViewRelationOption(repo)
        addTagToDataViewRecord = AddTagToDataViewRecord(repo)
        removeTagFromDataViewRecord = RemoveTagFromDataViewRecord(repo)
        updateDataViewRecord = UpdateDataViewRecord(repo)
        updateDetail = UpdateDetail(repo)
        urlBuilder = UrlBuilder(gateway)
        TestObjectSetObjectRelationValueFragment.testVmFactory = ObjectSetObjectRelationValueViewModel.Factory(
            relations = DataViewObjectRelationProvider(state),
            values = DataViewObjectValueProvider(state, session),
            details = object: ObjectDetailProvider {
                override fun provide(): Map<Id, Block.Fields> = state.value.details
            },
            types = object: ObjectTypeProvider {
                override fun provide(): List<ObjectType> = state.value.objectTypes
            },
            removeTagFromRecord = removeTagFromDataViewRecord,
            urlBuilder = urlBuilder,
            dispatcher = dispatcher,
            updateDataViewRecord = updateDataViewRecord
        )
    }

    @Test
    fun shouldDisplayEditButtonAndPlusButton() {
        // SETUP

        val relation = MockDataFactory.randomUuid()
        val target = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        state.value = ObjectSet(
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        relations = listOf(
                            Relation(
                                key = relation,
                                isMulti = true,
                                name = MockDataFactory.randomString(),
                                format = Relation.Format.TAG,
                                source = Relation.Source.values().random()
                            )
                        ),
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

        // TESTING

        launchFragment(
            bundleOf(
                ObjectRelationValueFragment.CTX_KEY to root,
                ObjectRelationValueFragment.RELATION_KEY to relation,
                ObjectRelationValueFragment.TARGET_KEY to target
            )
        )

        // Checking that the buttons are invisible

        onView(withId(R.id.btnEditOrDone)).apply {
            check(matches(isDisplayed()))
        }

        onView(withId(R.id.btnAddValue)).apply {
            check(matches(isDisplayed()))
        }
    }

    @Test
    fun shouldSetRelationName() {

        // SETUP

        val name = "Tag"

        val relation = MockDataFactory.randomUuid()
        val target = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        state.value = ObjectSet(
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        relations = listOf(
                            Relation(
                                key = relation,
                                isMulti = true,
                                name = name,
                                format = Relation.Format.TAG,
                                source = Relation.Source.values().random()
                            )
                        ),
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

        // TESTING

        launchFragment(
            bundleOf(
                ObjectRelationValueFragment.CTX_KEY to root,
                ObjectRelationValueFragment.RELATION_KEY to relation,
                ObjectRelationValueFragment.TARGET_KEY to target
            )
        )

        // Checking that the name is set

        onView(withId(R.id.tvTagOrStatusRelationHeader)).apply {
            check(matches(withText(name)))
        }
    }

    @Test
    fun shouldRenderTwoLastTagsFromDvWithFilterContainerInvisible() {

        // SETUP

        val option1Color = ThemeColor.values().random()
        val option2Color = ThemeColor.values().random()

        val option1 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Architect",
            color = option1Color.title
        )

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Manager",
            color = option2Color.title
        )

        val option3 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Developer",
            color = ""
        )

        val relationKey = MockDataFactory.randomUuid()
        val target = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relationKey to listOf(option2.id, option3.id)
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        state.value = ObjectSet(
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        relations = listOf(
                            Relation(
                                key = relationKey,
                                defaultValue = null,
                                isHidden = false,
                                isReadOnly = false,
                                isMulti = true,
                                name = MockDataFactory.randomString(),
                                source = Relation.Source.values().random(),
                                format = Relation.Format.TAG,
                                selections = listOf(option1, option2, option3)
                            )
                        ),
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

        // TESTING

        launchFragment(
            bundleOf(
                ObjectRelationValueFragment.CTX_KEY to root,
                ObjectRelationValueFragment.RELATION_KEY to relationKey,
                ObjectRelationValueFragment.TARGET_KEY to target
            )
        )

        val rvMatcher = withRecyclerView(R.id.recycler)

        onView(withId(R.id.filterInputContainer)).apply {
            check(matches(not(isDisplayed())))
        }

        onView(rvMatcher.atPositionOnView(0, R.id.tvTagName)).apply {
            check(matches(withText(option2.text)))
            check(matches(WithTextColor(option2Color.text)))
        }

        onView(rvMatcher.atPositionOnView(1, R.id.tvTagName)).apply {
            check(matches(withText(option3.text)))
            check(matches(WithTextColorRes(R.color.default_filter_tag_text_color)))
        }
    }

    @Test
    fun shouldRenderOnlyOneTagWithDefaultColor() {

        // SETUP

        val option1 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Architect",
            color = ""
        )

        val relationKey = MockDataFactory.randomUuid()
        val target = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relationKey to listOf(option1.id)
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        state.value = ObjectSet(
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        relations = listOf(
                            Relation(
                                key = relationKey,
                                defaultValue = null,
                                isHidden = false,
                                isReadOnly = false,
                                isMulti = true,
                                name = MockDataFactory.randomString(),
                                source = Relation.Source.values().random(),
                                format = Relation.Format.TAG,
                                selections = listOf(option1)
                            )
                        ),
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

        // TESTING

        launchFragment(
            bundleOf(
                ObjectRelationValueFragment.CTX_KEY to root,
                ObjectRelationValueFragment.RELATION_KEY to relationKey,
                ObjectRelationValueFragment.TARGET_KEY to target
            )
        )

        val rvMatcher = withRecyclerView(R.id.recycler)

        onView(rvMatcher.atPositionOnView(0, R.id.tvTagName)).apply {
            check(matches(WithTextColorRes(R.color.default_filter_tag_text_color)))
        }
    }

    @Test
    fun shouldRenderEmptyState() {

        // SETUP

        val option1 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "TAG1",
            color = MockDataFactory.randomString()
        )

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "TAG 2",
            color = MockDataFactory.randomString()
        )

        val option3 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "TAG 3",
            color = MockDataFactory.randomString()
        )

        val relationKey = MockDataFactory.randomUuid()
        val target = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        state.value = ObjectSet(
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        relations = listOf(
                            Relation(
                                key = relationKey,
                                defaultValue = null,
                                isHidden = false,
                                isReadOnly = false,
                                isMulti = true,
                                name = MockDataFactory.randomString(),
                                source = Relation.Source.values().random(),
                                format = Relation.Format.TAG,
                                selections = listOf(option1, option2, option3)
                            )
                        ),
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

        // TESTING

        launchFragment(
            bundleOf(
                ObjectRelationValueFragment.CTX_KEY to root,
                ObjectRelationValueFragment.RELATION_KEY to relationKey,
                ObjectRelationValueFragment.TARGET_KEY to target
            )
        )

        val rvMatcher = withRecyclerView(R.id.recycler)

        onView(rvMatcher.atPositionOnView(0, R.id.tvEmptyMessage)).apply {
            check(matches(isDisplayed()))
        }
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestObjectSetObjectRelationValueFragment> {
        return launchFragmentInContainer<TestObjectSetObjectRelationValueFragment>(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}