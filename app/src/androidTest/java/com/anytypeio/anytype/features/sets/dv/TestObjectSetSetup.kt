package com.anytypeio.anytype.features.sets.dv

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.dataview.interactor.*
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.emojifier.data.DefaultDocumentEmojiIconProvider
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.sets.ObjectSetRecordCache
import com.anytypeio.anytype.presentation.sets.ObjectSetReducer
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModelFactory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.flow.emptyFlow
import org.mockito.Mock
import org.mockito.MockitoAnnotations

abstract class TestObjectSetSetup {

    private lateinit var openObjectSet: OpenObjectSet
    private lateinit var addDataViewRelation: AddDataViewRelation
    private lateinit var updateDataViewViewer: UpdateDataViewViewer
    private lateinit var updateDataViewRecord: UpdateDataViewRecord
    private lateinit var updateText: UpdateText
    private lateinit var createDataViewRecord: CreateDataViewRecord
    private lateinit var closeBlock: CloseBlock
    private lateinit var setActiveViewer: SetActiveViewer

    lateinit var urlBuilder: UrlBuilder

    @Mock
    lateinit var repo: BlockRepository
    @Mock
    lateinit var gateway: Gateway
    @Mock
    lateinit var interceptEvents: InterceptEvents

    private val session = ObjectSetSession()
    private val reducer = ObjectSetReducer()
    private val dispatcher: Dispatcher<Payload> = Dispatcher.Default()
    private val objectSetRecordCache = ObjectSetRecordCache()

    val ctx : Id = MockDataFactory.randomUuid()

    abstract val title : Block

    val header get() = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    val defaultDetails = Block.Details(
        mapOf(
            ctx to Block.Fields(
                mapOf(
                    "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random()
                )
            )
        )
    )

    open fun setup() {
        MockitoAnnotations.initMocks(this)

        addDataViewRelation = AddDataViewRelation(repo)
        updateText = UpdateText(repo)
        openObjectSet = OpenObjectSet(repo)
        createDataViewRecord = CreateDataViewRecord(repo)
        updateDataViewRecord = UpdateDataViewRecord(repo)
        updateDataViewViewer = UpdateDataViewViewer(repo)
        setActiveViewer = SetActiveViewer(repo)
        closeBlock = CloseBlock(repo)
        urlBuilder = UrlBuilder(gateway)

        TestObjectSetFragment.testVmFactory = ObjectSetViewModelFactory(
            openObjectSet = openObjectSet,
            closeBlock = closeBlock,
            addDataViewRelation = addDataViewRelation,
            interceptEvents = interceptEvents,
            updateDataViewViewer = updateDataViewViewer,
            setActiveViewer = setActiveViewer,
            createDataViewRecord = createDataViewRecord,
            updateDataViewRecord = updateDataViewRecord,
            updateText = updateText,
            urlBuilder = urlBuilder,
            session = session,
            dispatcher = dispatcher,
            reducer = reducer,
            objectSetRecordCache = objectSetRecordCache
        )
    }

    fun stubInterceptEvents() {
        interceptEvents.stub {
            onBlocking { build(any()) } doReturn emptyFlow()
        }
    }

    fun stubOpenObjectSet(
        set: List<Block>,
        details: Block.Details = Block.Details(),
        relations: List<Relation> = emptyList()
    ) {
        repo.stub {
            onBlocking { openObjectSet(ctx) } doReturn Payload(
                context = ctx,
                events = listOf(
                    Event.Command.ShowBlock(
                        context = ctx,
                        root = ctx,
                        details = details,
                        blocks = set,
                        relations = relations
                    )
                )
            )
        }
    }

    fun stubOpenObjectSetWithRecord(
        set: List<Block>,
        details: Block.Details = Block.Details(),
        relations: List<Relation> = emptyList(),
        dataview: Id,
        viewer: Id,
        total: Int,
        records: List<DVRecord>,
        objectTypes: List<ObjectType>
    ) {
        repo.stub {
            onBlocking { openObjectSet(ctx) } doReturn Payload(
                context = ctx,
                events = listOf(
                    Event.Command.ShowBlock(
                        context = ctx,
                        root = ctx,
                        details = details,
                        blocks = set,
                        relations = relations,
                        objectTypes = objectTypes
                    ),
                    Event.Command.DataView.SetRecords(
                        context = ctx,
                        id = dataview,
                        view = viewer,
                        total = total,
                        records = records
                    )
                )
            )
        }
    }

    fun launchFragment(args: Bundle): FragmentScenario<TestObjectSetFragment> {
        return launchFragmentInContainer<TestObjectSetFragment>(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}