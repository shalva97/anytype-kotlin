package com.anytypeio.anytype.presentation.page

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_ui.common.Alignment
import com.anytypeio.anytype.core_ui.common.Markup
import com.anytypeio.anytype.core_ui.features.page.BlockDimensions
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.features.page.pattern.DefaultPatternMatcher
import com.anytypeio.anytype.core_ui.features.page.styling.StylingEvent
import com.anytypeio.anytype.core_ui.features.page.styling.StylingMode
import com.anytypeio.anytype.core_ui.features.page.styling.StylingType
import com.anytypeio.anytype.core_ui.model.StyleConfig
import com.anytypeio.anytype.core_ui.state.ControlPanelState
import com.anytypeio.anytype.core_ui.widgets.ActionItemType
import com.anytypeio.anytype.core_utils.tools.Counter
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.block.interactor.*
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.block.model.Position
import com.anytypeio.anytype.domain.clipboard.Copy
import com.anytypeio.anytype.domain.clipboard.Paste
import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.download.DownloadFile
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.event.model.Event
import com.anytypeio.anytype.domain.event.model.Payload
import com.anytypeio.anytype.domain.ext.content
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.*
import com.anytypeio.anytype.domain.page.bookmark.SetupBookmark
import com.anytypeio.anytype.domain.page.navigation.GetListPages
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.page.editor.Command
import com.anytypeio.anytype.presentation.page.editor.Interactor
import com.anytypeio.anytype.presentation.page.editor.Orchestrator
import com.anytypeio.anytype.presentation.page.editor.ViewState
import com.anytypeio.anytype.presentation.page.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.page.selection.SelectionStateHolder
import com.anytypeio.anytype.presentation.page.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.util.Bridge
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.TXT
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
open class PageViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var openPage: OpenPage

    @Mock
    lateinit var closePage: ClosePage

    @Mock
    lateinit var interceptEvents: InterceptEvents

    @Mock
    lateinit var createBlock: CreateBlock

    @Mock
    lateinit var updateText: UpdateText

    @Mock
    lateinit var updateCheckbox: UpdateCheckbox

    @Mock
    lateinit var duplicateBlock: DuplicateBlock

    @Mock
    lateinit var unlinkBlocks: UnlinkBlocks

    @Mock
    lateinit var updateTextStyle: UpdateTextStyle

    @Mock
    lateinit var updateTextColor: UpdateTextColor

    @Mock
    lateinit var updateLinkMark: UpdateLinkMarks

    @Mock
    lateinit var removeLinkMark: RemoveLinkMark

    @Mock
    lateinit var mergeBlocks: MergeBlocks

    @Mock
    lateinit var splitBlock: SplitBlock

    @Mock
    lateinit var getListPages: GetListPages

    @Mock
    lateinit var createPage: CreatePage

    @Mock
    lateinit var updateAlignment: UpdateAlignment

    @Mock
    lateinit var updateBackgroundColor: UpdateBackgroundColor

    @Mock
    lateinit var downloadFile: DownloadFile

    @Mock
    lateinit var uploadBlock: UploadBlock

    @Mock
    lateinit var paste: Paste

    @Mock
    lateinit var copy: Copy

    @Mock
    lateinit var undo: Undo

    @Mock
    lateinit var redo: Redo

    @Mock
    lateinit var setupBookmark: SetupBookmark

    @Mock
    lateinit var createDocument: CreateDocument

    @Mock
    lateinit var createNewDocument: CreateNewDocument

    @Mock
    lateinit var archiveDocument: ArchiveDocument

    @Mock
    lateinit var replaceBlock: ReplaceBlock

    @Mock
    lateinit var updateTitle: UpdateTitle

    @Mock
    lateinit var move: Move

    @Mock
    lateinit var turnIntoDocument: TurnIntoDocument

    @Mock
    lateinit var gateway : Gateway

    @Mock
    lateinit var analytics: Analytics

    lateinit var vm: PageViewModel

    private lateinit var builder: UrlBuilder

    val root = MockDataFactory.randomUuid()

    val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            text = MockDataFactory.randomString(),
            style = Block.Content.Text.Style.TITLE,
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        builder = UrlBuilder(gateway)
    }

    @Test
    fun `should not start observing events when view model is initialized`() {
        buildViewModel()
        verifyZeroInteractions(interceptEvents)
    }

    @Test
    fun `should start opening page when requested`() {
        val param = OpenPage.Params(id = root)

        stubInterceptEvents()
        buildViewModel()
        stubOpenPage(context = root)

        vm.onStart(root)

        runBlockingTest { verify(openPage, times(1)).invoke(param) }
    }

    @Test
    fun `should dispatch a page to UI when this view model receives an appropriate command`() {

        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(
                    type = Block.Content.Smart.Type.PAGE
                ),
                children = listOf(header.id, child)
            ),
            header,
            title,
            paragraph
        )

        stubOpenPage(
            context = root,
            events = listOf(
                Event.Command.ShowBlock(
                    root = root,
                    blocks = page,
                    context = root
                )
            )
        )

        stubInterceptEvents()

        buildViewModel(builder)

        vm.onStart(root)

        val expected = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Document(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    id = paragraph.id,
                    text = paragraph.content<Block.Content.Text>().text,
                    backgroundColor = paragraph.content<Block.Content.Text>().backgroundColor
                )
            )
        )

        vm.state.test().assertValue(expected)
    }

    @Test
    fun `should close page when the system back button is pressed`() {

        val root = MockDataFactory.randomUuid()

        stubOpenPage(root)
        stubInterceptEvents()

        buildViewModel()

        vm.onStart(root)

        verifyZeroInteractions(closePage)

        vm.onSystemBackPressed(editorHasChildrenScreens = false)

        runBlockingTest {
            verify(closePage, times(1)).invoke(any())
        }
    }

    @Test
    fun `should emit an approprtiate navigation command when the page is closed`() {

        val response = Either.Right(Unit)

        stubInterceptEvents()
        stubClosePage(response)
        buildViewModel()

        val testObserver = vm.navigation.test()

        verifyZeroInteractions(closePage)

        vm.onSystemBackPressed(editorHasChildrenScreens = false)

        testObserver
            .assertHasValue()
            .assertValue { value -> value.peekContent() == AppNavigation.Command.Exit }
    }

    @Test
    fun `should not emit any navigation command if there is an error while closing the page`() {

        val root = MockDataFactory.randomUuid()

        val error = Exception("Error while closing this page")

        val response = Either.Left(error)

        stubOpenPage(root)
        stubClosePage(response)
        stubInterceptEvents()
        buildViewModel()

        vm.onStart(root)

        val testObserver = vm.navigation.test()

        verifyZeroInteractions(closePage)

        vm.onSystemBackPressed(editorHasChildrenScreens = false)

        testObserver.assertNoValue()
    }

    @Test
    fun `should update block when its text changes`() {

        val blockId = MockDataFactory.randomUuid()
        val pageId = root
        val text = MockDataFactory.randomString()

        stubInterceptEvents()
        buildViewModel()
        stubOpenPage(context = pageId)
        stubUpdateText()

        vm.onStart(pageId)
        vm.onTextChanged(id = blockId, text = text, marks = emptyList())

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(updateText, times(1)).invoke(
                argThat { this.context == pageId && this.target == blockId && this.text == text }
            )
        }
    }

    @Test
    fun `should debounce values when dispatching text changes`() {

        val blockId = MockDataFactory.randomUuid()
        val pageId = MockDataFactory.randomUuid()
        val text = MockDataFactory.randomString()

        stubObserveEvents()
        stubUpdateText()
        stubOpenPage(context = pageId)
        buildViewModel()

        vm.onStart(pageId)

        vm.onTextChanged(id = blockId, text = text, marks = emptyList())
        vm.onTextChanged(id = blockId, text = text, marks = emptyList())
        vm.onTextChanged(id = blockId, text = text, marks = emptyList())

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        vm.onTextChanged(id = blockId, text = text, marks = emptyList())

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(updateText, times(2)).invoke(
                argThat { this.context == pageId && this.target == blockId && this.text == text }
            )
        }
    }

    @Test
    fun `should add a new block to the already existing one when this view model receives an appropriate command`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(
                    type = Block.Content.Smart.Type.PAGE
                ),
                children = listOf(header.id, child)
            ),
            header,
            title,
            paragraph
        )

        val added = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        stubObserveEvents(
            flow {
                delay(100)
                emit(
                    listOf(
                        Event.Command.ShowBlock(
                            root = root,
                            blocks = page,
                            context = root
                        )
                    )
                )
                delay(100)
                emit(
                    listOf(
                        Event.Command.UpdateStructure(
                            context = root,
                            id = root,
                            children = listOf(header.id, child, added.id)
                        )
                    )
                )
                emit(
                    listOf(
                        Event.Command.AddBlock(
                            blocks = listOf(added),
                            context = root
                        )
                    )
                )
            }
        )

        stubOpenPage()
        buildViewModel(builder)
        vm.onStart(root)

        coroutineTestRule.advanceTime(200)

        val expected =
            ViewState.Success(
                listOf(
                    BlockView.Title.Document(
                        isFocused = false,
                        id = title.id,
                        text = title.content<TXT>().text
                    ),
                    BlockView.Text.Paragraph(
                        id = paragraph.id,
                        text = paragraph.content.asText().text,
                        backgroundColor = paragraph.content<Block.Content.Text>().backgroundColor
                    ),
                    BlockView.Text.Paragraph(
                        id = added.id,
                        text = added.content.asText().text,
                        backgroundColor = added.content<Block.Content.Text>().backgroundColor
                    )
                )
            )

        vm.state.test().assertValue(expected)
    }

    @Test
    fun `should start creating a new block if user clicked create-text-block-button`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomString()

        val smart = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(child)
        )

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        stubOpenPage(
            context = root,
            events = listOf(
                Event.Command.ShowBlock(
                    context = root,
                    blocks = listOf(smart, paragraph),
                    root = root
                )
            )
        )

        stubCreateBlock(root)

        buildViewModel()

        vm.onStart(root)

        vm.onBlockFocusChanged(id = paragraph.id, hasFocus = true)

        vm.onAddTextBlockClicked(style = Block.Content.Text.Style.P)

        runBlockingTest {
            verify(createBlock, times(1)).invoke(any())
        }
    }

    @Test
    fun `should update block text without dispatching it to UI when we receive an appropriate event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(
                    type = Block.Content.Smart.Type.PAGE
                ),
                children = listOf(header.id, child)
            ),
            header,
            title,
            paragraph
        )

        val text = MockDataFactory.randomString()

        interceptEvents.stub {
            onBlocking { build(any()) } doReturn flow {
                delay(100)
                emit(
                    listOf(
                        Event.Command.ShowBlock(
                            root = root,
                            blocks = page,
                            context = root
                        )
                    )
                )
                delay(100)
                emit(
                    listOf(
                        Event.Command.UpdateBlockText(
                            text = text,
                            id = child,
                            context = root
                        )
                    )
                )
            }
        }

        stubOpenPage()
        buildViewModel(builder)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        val beforeUpdate = ViewState.Success(
            listOf(
                BlockView.Title.Document(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    id = paragraph.id,
                    text = paragraph.content.asText().text,
                    backgroundColor = paragraph.content<Block.Content.Text>().backgroundColor
                )
            )
        )

        vm.state.test().assertValue(beforeUpdate)

        coroutineTestRule.advanceTime(200)

        val afterUpdate = beforeUpdate.copy()

        vm.state.test().assertValue(afterUpdate)
    }

    @Test
    fun `should emit loading state when starting opening a page`() {

        val root = MockDataFactory.randomUuid()

        stubOpenPage()
        stubObserveEvents()
        buildViewModel()

        val testObserver = vm.state.test()

        vm.onStart(root)

        testObserver.assertValue(ViewState.Loading)
    }

    @Test
    fun `should apply two different markup actions`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P,
                color = "red",
                backgroundColor = "yellow"
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, child)
        )

        val blocks = listOf(
            header,
            title,
            page,
            paragraph
        )

        interceptEvents.stub {
            onBlocking { build(any()) } doReturn flow {
                delay(100)
                emit(
                    listOf(
                        Event.Command.ShowBlock(
                            root = root,
                            blocks = blocks,
                            context = root
                        )
                    )
                )
            }
        }

        stubOpenPage()

        stubUpdateText()

        buildViewModel(builder)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        val firstTimeRange = 0..3
        val firstTimeMarkup = StylingEvent.Markup.Bold

        vm.onBlockFocusChanged(
            hasFocus = true,
            id = paragraph.id
        )

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = firstTimeRange
        )

        vm.onEditorContextMenuStyleClicked(
            selection = firstTimeRange
        )

        vm.onStylingToolbarEvent(event = firstTimeMarkup)

        val firstTimeExpected = ViewState.Success(
            listOf(
                BlockView.Title.Document(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    isFocused = true,
                    id = paragraph.id,
                    text = paragraph.content.asText().text,
                    color = paragraph.content<Block.Content.Text>().color,
                    backgroundColor = paragraph.content<Block.Content.Text>().backgroundColor,
                    marks = listOf(
                        Markup.Mark(
                            type = Markup.Type.BOLD,
                            param = null,
                            from = firstTimeRange.first(),
                            to = firstTimeRange.last()
                        )
                    )
                )
            )
        )

        vm.state.test().apply {
            assertHasValue()
            assertValue(firstTimeExpected)
        }

        val secondTimeRange = 0..5
        val secondTimeMarkup = StylingEvent.Markup.Italic

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = secondTimeRange
        )

        vm.onStylingToolbarEvent(event = secondTimeMarkup)

        val secondTimeExpected = ViewState.Success(
            listOf(
                BlockView.Title.Document(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    isFocused = true,
                    id = paragraph.id,
                    text = paragraph.content.asText().text,
                    color = paragraph.content<Block.Content.Text>().color,
                    backgroundColor = paragraph.content<Block.Content.Text>().backgroundColor,
                    marks = listOf(
                        Markup.Mark(
                            type = Markup.Type.BOLD,
                            param = null,
                            from = firstTimeRange.first(),
                            to = firstTimeRange.last()
                        ),
                        Markup.Mark(
                            type = Markup.Type.ITALIC,
                            param = null,
                            from = secondTimeRange.first(),
                            to = secondTimeRange.last()
                        )
                    )
                )
            )
        )

        vm.state.test().apply {
            assertHasValue()
            assertValue(secondTimeExpected)
        }
    }

    @Test
    fun `should apply two markup actions of the same markup type`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P,
                color = "red",
                backgroundColor = "yellow"
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, child)
        )

        val blocks = listOf(
            header,
            title,
            page,
            paragraph
        )

        val events = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = blocks,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(events)
        stubOpenPage()
        buildViewModel(builder)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        val firstTimeRange = 0..3
        val firstTimeMarkup = StylingEvent.Markup.Bold

        vm.onBlockFocusChanged(
            hasFocus = true,
            id = paragraph.id
        )

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = firstTimeRange
        )

        vm.onEditorContextMenuStyleClicked(
            selection = firstTimeRange
        )

        vm.onStylingToolbarEvent(event = firstTimeMarkup)

        val firstTimeExpected = ViewState.Success(
            listOf(
                BlockView.Title.Document(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    isFocused = true,
                    id = paragraph.id,
                    text = paragraph.content.asText().text,
                    color = paragraph.content<Block.Content.Text>().color,
                    backgroundColor = paragraph.content<Block.Content.Text>().backgroundColor,
                    marks = listOf(
                        Markup.Mark(
                            type = Markup.Type.BOLD,
                            param = null,
                            from = firstTimeRange.first(),
                            to = firstTimeRange.last()
                        )
                    )
                )
            )
        )

        vm.state.test().apply {
            assertHasValue()
            assertValue(firstTimeExpected)
        }

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = 3..3
        )

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = 0..0
        )

        val secondTimeRange = 0..5
        val secondTimeMarkup = StylingEvent.Markup.Bold

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = secondTimeRange
        )

        vm.onEditorContextMenuStyleClicked(
            selection = secondTimeRange
        )

        vm.onStylingToolbarEvent(event = secondTimeMarkup)

        val secondTimeExpected = ViewState.Success(
            listOf(
                BlockView.Title.Document(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    isFocused = true,
                    id = paragraph.id,
                    text = paragraph.content.asText().text,
                    color = paragraph.content<Block.Content.Text>().color,
                    backgroundColor = paragraph.content<Block.Content.Text>().backgroundColor,
                    marks = listOf(
                        Markup.Mark(
                            type = Markup.Type.BOLD,
                            param = null,
                            from = secondTimeRange.first(),
                            to = secondTimeRange.last()
                        )
                    )
                )
            )
        )

        vm.state.test().apply {
            assertHasValue()
            assertValue(secondTimeExpected)
        }
    }

    @Test
    fun `should dispatch texts changes and markup even if only markup is changed`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P,
                color = "red",
                backgroundColor = "yellow"
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                Block.Content.Smart.Type.PAGE
            ),
            children = listOf(child)
        )

        val blocks = listOf(
            page,
            paragraph
        )

        val events = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = blocks,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(events)
        stubUpdateText()
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        val range = 0..3
        val markup = StylingEvent.Markup.Bold

        vm.onBlockFocusChanged(
            hasFocus = true,
            id = paragraph.id
        )

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = range
        )

        vm.onEditorContextMenuStyleClicked(
            selection = range
        )

        vm.onStylingToolbarEvent(markup)

        val marks = listOf(
            Block.Content.Text.Mark(
                type = Block.Content.Text.Mark.Type.BOLD,
                param = null,
                range = range
            )
        )

        runBlockingTest {
            verify(updateText, times(1)).invoke(
                params = eq(
                    UpdateText.Params(
                        target = paragraph.id,
                        marks = marks,
                        context = page.id,
                        text = paragraph.content.asText().text
                    )
                )
            )
        }
    }

    @Test
    fun `test changes from UI do not trigger re-rendering`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, child)
        )

        val blocks = listOf(
            page,
            header,
            title,
            paragraph
        )

        val events = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = blocks,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(events)
        stubOpenPage()
        buildViewModel(builder)
        stubUpdateText()

        val testObserver = vm.state.test()

        vm.onStart(root)

        testObserver.assertValue(ViewState.Loading)

        coroutineTestRule.advanceTime(100)

        val state = ViewState.Success(
            listOf(
                BlockView.Title.Document(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    id = paragraph.id,
                    text = paragraph.content.asText().text,
                    backgroundColor = paragraph.content<Block.Content.Text>().backgroundColor
                )
            )
        )

        testObserver.assertValue(state).assertHistorySize(2)

        val userInput = MockDataFactory.randomString()

        val range = 0..3

        val marks = listOf(
            Block.Content.Text.Mark(
                type = Block.Content.Text.Mark.Type.BOLD,
                param = null,
                range = range
            )
        )

        vm.onTextChanged(
            id = paragraph.id,
            marks = marks,
            text = userInput
        )

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        testObserver.assertValue(state).assertHistorySize(2)
    }

    @Test
    fun `should update text inside view state when user changed text`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val initialText = ""

        val initialContent = Block.Content.Text(
            text = initialText,
            marks = emptyList(),
            style = Block.Content.Text.Style.P
        )

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = initialContent,
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
            children = listOf(child)
        )

        val blocks = listOf(
            page,
            paragraph
        )

        val events = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = blocks,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(events)
        stubOpenPage()

        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        val userInput = MockDataFactory.randomString()

        vm.onTextChanged(id = paragraph.id, text = userInput, marks = emptyList())

        val contentAfterChange = Block.Content.Text(
            text = userInput,
            marks = emptyList(),
            style = Block.Content.Text.Style.P
        )

        val paragraphAfterChange = paragraph.copy(
            content = contentAfterChange
        )

        val expected = listOf(
            page,
            paragraphAfterChange
        )

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        assertEquals(
            expected = expected,
            actual = vm.blocks
        )
    }

    @Test
    fun `should dispatch text changes including markup to the middleware`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val initialText = ""

        val initialContent = Block.Content.Text(
            text = initialText,
            marks = emptyList(),
            style = Block.Content.Text.Style.P
        )

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = initialContent,
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
            children = listOf(child)
        )

        val blocks = listOf(
            page,
            paragraph
        )

        interceptEvents.stub {
            onBlocking { build() } doReturn flow {
                delay(100)
                emit(
                    listOf(
                        Event.Command.ShowBlock(
                            root = root,
                            blocks = blocks,
                            context = root
                        )
                    )
                )
            }
        }

        stubOpenPage()

        stubUpdateText()

        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        val userInput = MockDataFactory.randomString()
        val marks = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.BOLD
            )
        )

        vm.onTextChanged(id = paragraph.id, text = userInput, marks = marks)

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(updateText, times(1)).invoke(
                params = eq(
                    UpdateText.Params(
                        target = paragraph.id,
                        text = userInput,
                        marks = marks,
                        context = page.id
                    )
                )
            )
        }
    }

    @Test
    fun `should receive initial control panel state when view model is initialized`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(root = root, child = child)

        val flow: Flow<List<Event.Command>> = flow {
            delay(1000)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(1001)

        val expected = ControlPanelState.init()

        vm.controlPanelViewState.test().assertValue(expected)
    }

    @Test
    fun `should dispatch open-add-block-panel command on add-block-toolbar-clicked event`() {

        // SETUP

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(root = root, child = child)

        val flow: Flow<List<Event.Command>> = flow {
            delay(1000)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(1001)

        // TESTING

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        val commands = vm.commands.test()

        vm.onAddBlockToolbarClicked()

        val result = commands.value()

        assertEquals(
            expected = Command.OpenAddBlockPanel,
            actual = result.peekContent()
        )
    }

    @Test
    fun `should add a header-one block on add-header-one event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, child)
        )

        val style = Block.Content.Text.Style.H1

        val new = Block(
            id = MockDataFactory.randomString(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = style
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val flow: Flow<List<Event>> = flow {
            delay(500)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = listOf(page, header, title, paragraph),
                        context = root
                    )
                )
            )
            delay(500)
            emit(
                listOf(
                    Event.Command.UpdateStructure(
                        context = root,
                        id = root,
                        children = listOf(header.id, child, new.id)
                    )
                )
            )
            emit(
                listOf(
                    Event.Command.AddBlock(
                        blocks = listOf(new),
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()

        buildViewModel(builder)

        vm.onStart(root)

        coroutineTestRule.advanceTime(500)

        val testObserver = vm.state.test()

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(
                    BlockView.Title.Document(
                        isFocused = false,
                        id = title.id,
                        text = title.content<TXT>().text
                    ),
                    BlockView.Text.Paragraph(
                        id = paragraph.id,
                        text = paragraph.content<Block.Content.Text>().text,
                        backgroundColor = paragraph.content<Block.Content.Text>().backgroundColor
                    )
                )
            )
        )

        coroutineTestRule.advanceTime(500)

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(
                    BlockView.Title.Document(
                        isFocused = false,
                        id = title.id,
                        text = title.content<TXT>().text
                    ),
                    BlockView.Text.Paragraph(
                        id = paragraph.id,
                        text = paragraph.content<Block.Content.Text>().text,
                        backgroundColor = paragraph.content<Block.Content.Text>().backgroundColor
                    ),
                    BlockView.Text.Header.One(
                        id = new.id,
                        text = new.content<Block.Content.Text>().text,
                        backgroundColor = new.content<Block.Content.Text>().backgroundColor,
                        indent = 0
                    )
                )
            )
        )
    }

    @Test
    fun `should start duplicating focused block when requested`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(root = root, child = child)

        val events: Flow<List<Event.Command>> = flow {
            delay(1000)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubOpenPage()
        stubObserveEvents(events)
        buildViewModel()
        stubDuplicateBlock(
            newBlockId = MockDataFactory.randomString(),
            root = root
        )


        vm.onStart(root)

        coroutineTestRule.advanceTime(1001)

        vm.onBlockFocusChanged(id = child, hasFocus = true)

        vm.onActionMenuItemClicked(id = child, action = ActionItemType.Duplicate)

        runBlockingTest {
            verify(duplicateBlock, times(1)).invoke(
                params = eq(
                    DuplicateBlock.Params(
                        original = child,
                        context = root
                    )
                )
            )
        }
    }

    @Test
    fun `should start deleting focused block when requested`() {

        // SETUP

        val child = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )
        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, child.id)
        )

        val doc = listOf(page, header, title, child)

        val events: Flow<List<Event.Command>> = flow {
            delay(1000)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = doc,
                        context = root
                    )
                )
            )
        }

        stubOpenPage()
        stubUnlinkBlocks(root = root)
        stubObserveEvents(events)
        buildViewModel()

        // TESTING

        vm.onStart(root)

        coroutineTestRule.advanceTime(1001)

        vm.onBlockFocusChanged(id = child.id, hasFocus = true)
        vm.onActionMenuItemClicked(id = child.id, action = ActionItemType.Delete)

        runBlockingTest {
            verify(unlinkBlocks, times(1)).invoke(
                params = eq(
                    UnlinkBlocks.Params(
                        context = root,
                        targets = listOf(child.id)
                    )
                )
            )
        }
    }

    @Test
    fun `should delete the first block when the delete-block event received for the first block, then rerender the page`() {

        val pageOpenedDelay = 100L
        val blockDeletedEventDelay = 100L

        val root = MockDataFactory.randomUuid()

        val firstChild = Block(
            id = "FIRST CHILD",
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "FIRST CHILD TEXT",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val secondChild = Block(
            id = "SECOND CHILD",
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "SECOND CHILD TEXT",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, firstChild.id, secondChild.id)
        )

        val doc = listOf(page, header, title, firstChild, secondChild)

        val events: Flow<List<Event.Command>> = flow {
            delay(pageOpenedDelay)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = doc,
                        context = root
                    )
                )
            )
            delay(blockDeletedEventDelay)
            emit(
                listOf(
                    Event.Command.DeleteBlock(
                        targets = listOf(firstChild.id),
                        context = root
                    )
                )
            )
        }

        stubOpenPage()
        stubObserveEvents(events)
        buildViewModel(builder)
        stubUnlinkBlocks(root)

        vm.onStart(root)

        coroutineTestRule.advanceTime(pageOpenedDelay)

        val testObserver = vm.state.test()

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(
                    BlockView.Title.Document(
                        isFocused = false,
                        id = title.id,
                        text = title.content<TXT>().text
                    ),
                    BlockView.Text.Paragraph(
                        id = firstChild.id,
                        text = firstChild.content<Block.Content.Text>().text,
                        backgroundColor = firstChild.content<Block.Content.Text>().backgroundColor
                    ),
                    BlockView.Text.Paragraph(
                        id = secondChild.id,
                        text = secondChild.content<Block.Content.Text>().text,
                        backgroundColor = secondChild.content<Block.Content.Text>().backgroundColor
                    )
                )
            )
        )

        vm.onBlockFocusChanged(id = firstChild.id, hasFocus = true)
        vm.onActionMenuItemClicked(id = firstChild.id, action = ActionItemType.Delete)

        assertEquals(expected = 5, actual = vm.blocks.size)

        coroutineTestRule.advanceTime(blockDeletedEventDelay)

        assertEquals(expected = 4, actual = vm.blocks.size)

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(
                    BlockView.Title.Document(
                        isFocused = true,
                        id = title.id,
                        text = title.content<TXT>().text,
                        cursor = title.content<TXT>().text.length
                    ),
                    BlockView.Text.Paragraph(
                        id = secondChild.id,
                        text = secondChild.content<Block.Content.Text>().text,
                        backgroundColor = secondChild.content<Block.Content.Text>().backgroundColor
                    )
                )
            )
        )
    }

    @Test
    fun `should start deleting the target block on empty-block-backspace-click event`() {

        val child = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )


        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, child.id)
        )

        val doc = listOf(page, header, title, child)

        val events: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = doc,
                        context = root
                    )
                )
            )
        }

        stubOpenPage()
        stubObserveEvents(events)
        buildViewModel()
        stubUnlinkBlocks(root)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(child.id, true)
        vm.onEmptyBlockBackspaceClicked(child.id)

        verifyBlocking(unlinkBlocks, times(1)) {
            invoke(
                params = eq(
                    UnlinkBlocks.Params(
                        context = root,
                        targets = listOf(child.id)
                    )
                )
            )
        }
    }

    private fun stubUnlinkBlocks(root: String) {
        unlinkBlocks.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = emptyList()
                )
            )
        }
    }

    @Test
    fun `should not proceed with deleting the title block on empty-block-backspace-click event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child,
            style = Block.Content.Text.Style.TITLE
        )

        val events: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubOpenPage()
        stubObserveEvents(events)
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        vm.onEmptyBlockBackspaceClicked(child)

        verify(unlinkBlocks, never()).invoke(
            scope = any(),
            params = any(),
            onResult = any()
        )
    }

    @Test
    fun `should proceed with creating a new block on end-line-enter-press event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child,
            style = Block.Content.Text.Style.TITLE
        )

        val events: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubOpenPage()
        stubObserveEvents(events)
        stubCreateBlock(root)
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        vm.onEndLineEnterClicked(
            id = child,
            marks = emptyList(),
            text = page.last().content<Block.Content.Text>().text
        )

        runBlockingTest {
            verify(createBlock, times(1)).invoke(
                params = eq(
                    CreateBlock.Params(
                        context = root,
                        target = child,
                        position = Position.BOTTOM,
                        prototype = Block.Prototype.Text(style = Block.Content.Text.Style.P)
                    )
                )
            )
        }
    }

    @Test
    fun `should start updating text style of the focused block on turn-into-option-clicked event`() {

        val root = MockDataFactory.randomUuid()
        val firstChild = MockDataFactory.randomUuid()
        val secondChild = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithTwoTextBlocks(
            root = root,
            firstChild = firstChild,
            firstChildStyle = Block.Content.Text.Style.TITLE,
            secondChild = secondChild,
            secondChildStyle = Block.Content.Text.Style.P
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        stubUpdateTextStyle()

        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = secondChild,
            hasFocus = true
        )

        val newStyle = Block.Content.Text.Style.H1

        vm.onTurnIntoStyleClicked(style = newStyle)

        runBlockingTest {
            verify(updateTextStyle, times(1)).invoke(
                params = eq(
                    UpdateTextStyle.Params(
                        context = root,
                        targets = listOf(secondChild),
                        style = newStyle
                    )
                )
            )
        }
    }

    @Test
    fun `should start updating the target block's color on color-toolbar-option-selected event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child,
            style = Block.Content.Text.Style.TITLE
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        stubUpdateTextColor(root)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        val color = MockDataFactory.randomString()

        vm.onToolbarTextColorAction(color = color, id = child)

        runBlockingTest {
            verify(updateTextColor, times(1)).invoke(
                params = eq(
                    UpdateTextColor.Params(
                        context = root,
                        target = child,
                        color = color
                    )
                )
            )
        }
    }

    @Test
    fun `should start creating a new paragraph on endline-enter-pressed event inside a quote block`() {

        val style = Block.Content.Text.Style.QUOTE
        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child,
            style = style
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        stubCreateBlock(root)
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        vm.onEndLineEnterClicked(
            id = child,
            text = page.last().content<Block.Content.Text>().text,
            marks = emptyList()
        )

        runBlockingTest {
            verify(createBlock, times(1)).invoke(
                params = eq(
                    CreateBlock.Params(
                        context = root,
                        target = child,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.P
                        ),
                        position = Position.BOTTOM
                    )
                )
            )
        }
    }

    @Test
    fun `should turn a list item with empty text into a paragraph on endline-enter-pressed event`() {

        val root = MockDataFactory.randomUuid()
        val firstChild = MockDataFactory.randomUuid()
        val secondChild = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithTwoTextBlocks(
            root = root,
            firstChild = firstChild,
            secondChild = secondChild,
            firstChildStyle = Block.Content.Text.Style.TITLE,
            secondChildStyle = Block.Content.Text.Style.BULLET,
            secondChildText = ""
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        stubUpdateTextStyle()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = secondChild,
            hasFocus = true
        )

        vm.onEndLineEnterClicked(
            id = secondChild,
            text = "",
            marks = emptyList()
        )

        runBlockingTest {

            verify(createBlock, never()).invoke(
                scope = any(),
                params = any(),
                onResult = any()
            )

            verify(updateTextStyle, times(1)).invoke(
                params = eq(
                    UpdateTextStyle.Params(
                        targets = listOf(secondChild),
                        style = Block.Content.Text.Style.P,
                        context = root
                    )
                )
            )
        }
    }

    @Test
    fun `should send update text style intent when is list and empty`() {
        // SETUP

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()
        stubUpdateText()
        stubSplitBlocks(root)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        val index = MockDataFactory.randomInt()

        val text = MockDataFactory.randomString()

        vm.onTextChanged(
            id = child,
            text = text,
            marks = emptyList()
        )

        vm.onEnterKeyClicked(
            target = child,
            text = text,
            marks = emptyList(),
            range = 0..0
        )

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(updateText, times(1)).invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = text,
                        target = child,
                        marks = emptyList()
                    )
                )
            )
        }

        runBlockingTest {
            verify(splitBlock, times(1)).invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = page[1],
                        range = 0..0,
                        isToggled = null
                    )
                )
            )
        }
    }

    @Test
    fun `should preserve text style while splitting`() {

        // SETUP

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val style = Block.Content.Text.Style.BULLET

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(
                    type = Block.Content.Smart.Type.PAGE
                ),
                children = listOf(child)
            ),
            Block(
                id = child,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    marks = emptyList(),
                    style = style
                ),
                children = emptyList()
            )
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()
        stubUpdateText()
        stubSplitBlocks(root)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        val index = MockDataFactory.randomInt()

        val text = MockDataFactory.randomString()

        vm.onTextChanged(
            id = child,
            text = text,
            marks = emptyList()
        )

        vm.onEnterKeyClicked(
            target = child,
            text = text,
            marks = emptyList(),
            range = 1..1
        )

        runBlockingTest {
            verify(splitBlock, times(1)).invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = page[1],
                        range = 1..1,
                        isToggled = null
                    )
                )
            )
        }

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun `should proceed with creating a new page on on-plus-button-clicked event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onPlusButtonPressed()

        verify(createPage, times(1)).invoke(
            scope = any(),
            params = eq(CreatePage.Params.insideDashboard()),
            onResult = any()
        )
    }

    @Test
    fun `should start downloading file`() {

        val root = MockDataFactory.randomUuid()
        val file = MockBlockFactory.makeFileBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(
                    Block.Content.Smart.Type.PAGE
                ),
                children = listOf(title.id, file.id)
            ),
            title,
            file
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel(builder)

        stubDownloadFile()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.startDownloadingFile(id = file.id)

        runBlockingTest {
            verify(downloadFile, times(1)).invoke(
                params = eq(
                    DownloadFile.Params(
                        name = file.content<Block.Content.File>().name.orEmpty(),
                        url = builder.file(
                            hash = file.content<Block.Content.File>().hash
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `should create a new text block after currently focused block`() {

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        stubCreateBlock(root)
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onAddBlockToolbarClicked()

        vm.onAddTextBlockClicked(
            style = Block.Content.Text.Style.P
        )

        runBlockingTest {
            verify(createBlock, times(1)).invoke(
                params = eq(
                    CreateBlock.Params(
                        context = root,
                        target = title.id,
                        position = Position.BOTTOM,
                        prototype = Block.Prototype.Text(Block.Content.Text.Style.P)
                    )
                )
            )
        }
    }

    @Test
    fun `should create a new page block after currently focused block`() {
        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()
        val title = MockBlockFactory.makeTitleBlock()
        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )
        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }
        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()
        vm.onStart(root)
        coroutineTestRule.advanceTime(100)
        // TESTING
        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )
        vm.onAddBlockToolbarClicked()
        vm.onAddNewPageClicked()
        runBlockingTest {
            verify(createDocument, times(1)).invoke(
                params = eq(
                    CreateDocument.Params(
                        context = root,
                        target = title.id,
                        position = Position.BOTTOM
                    )
                )
            )
        }
    }

    @Test
    fun `should create a new bookmark block after currently focused block`() {

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()
        stubCreateBlock(root)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onAddBlockToolbarClicked()

        vm.onAddBookmarkBlockClicked()

        runBlockingTest {
            verify(createBlock, times(1)).invoke(
                params = eq(
                    CreateBlock.Params(
                        context = root,
                        target = title.id,
                        position = Position.BOTTOM,
                        prototype = Block.Prototype.Bookmark
                    )
                )
            )
        }
    }

    @Test
    fun `should create a new line divider block after currently focused block`() {

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        stubObserveEvents()
        stubOpenPage(
            events = listOf(
                Event.Command.ShowBlock(
                    root = root,
                    blocks = page,
                    context = root
                )
            )
        )
        stubCreateBlock(root = root)
        buildViewModel()

        vm.onStart(root)

        // TESTING

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onAddBlockToolbarClicked()

        vm.onAddDividerBlockClicked(type = Block.Content.Divider.Type.LINE)

        runBlockingTest {
            verify(createBlock, times(1)).invoke(
                params = eq(
                    CreateBlock.Params(
                        context = root,
                        target = title.id,
                        position = Position.BOTTOM,
                        prototype = Block.Prototype.DividerLine
                    )
                )
            )
        }
    }

    @Test
    fun `should create a new dots divider block after currently focused block`() {

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        stubObserveEvents()
        stubOpenPage(
            events = listOf(
                Event.Command.ShowBlock(
                    root = root,
                    blocks = page,
                    context = root
                )
            )
        )
        stubCreateBlock(root = root)
        buildViewModel()

        vm.onStart(root)

        // TESTING

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onAddBlockToolbarClicked()

        vm.onAddDividerBlockClicked(type = Block.Content.Divider.Type.DOTS)

        runBlockingTest {
            verify(createBlock, times(1)).invoke(
                params = eq(
                    CreateBlock.Params(
                        context = root,
                        target = title.id,
                        position = Position.BOTTOM,
                        prototype = Block.Prototype.DividerDots
                    )
                )
            )
        }
    }

    @Test
    fun `should proceed with undo`() {

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage(context = root)
        buildViewModel()

        undo.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = emptyList()
                )
            )
        }

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onActionUndoClicked()

        runBlockingTest {
            verify(undo, times(1)).invoke(
                params = eq(
                    Undo.Params(context = root)
                )
            )
        }
    }

    @Test
    fun `should proceed with redo`() {

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        redo.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = emptyList()
                )
            )
        }

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onActionRedoClicked()

        runBlockingTest {
            verify(redo, times(1)).invoke(
                params = eq(
                    Redo.Params(context = root)
                )
            )
        }
    }

    @Test
    fun `should start archiving document on on-archive-this-page-clicked event`() {

        // SETUP

        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(
                    Block.Content.Smart.Type.PAGE
                ),
                children = listOf(title.id)
            ),
            title
        )

        stubInterceptEvents()

        stubOpenPage(
            events = listOf(
                Event.Command.ShowBlock(
                    root = root,
                    blocks = page,
                    context = root
                )
            )
        )

        stubArchiveDocument()

        buildViewModel()

        vm.onStart(root)

        // TESTING

        vm.onArchiveThisPageClicked()

        runBlockingTest {
            verify(archiveDocument, times(1)).invoke(
                params = eq(
                    ArchiveDocument.Params(
                        context = root,
                        targets = listOf(root),
                        isArchived = true
                    )
                )
            )
        }
    }

    @Test
    fun `should start closing page after successful archive operation`() {

        // SETUP

        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(title.id)
            ),
            title
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        stubClosePage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        stubArchiveDocument()
        stubClosePage()

        // TESTING

        vm.onArchiveThisPageClicked()

        runBlockingTest {
            verify(archiveDocument, times(1)).invoke(
                params = eq(
                    ArchiveDocument.Params(
                        context = root,
                        targets = listOf(root),
                        isArchived = true
                    )
                )
            )

            verify(closePage, times(1)).invoke(
                params = eq(
                    ClosePage.Params(
                        id = root
                    )
                )
            )
        }
    }

    private fun stubArchiveDocument(
        params: ArchiveDocument.Params =
            ArchiveDocument.Params(
                context = root,
                targets = listOf(root),
                isArchived = true
            )
    ) {
        archiveDocument.stub {
            onBlocking { invoke(params = params) } doReturn Either.Right(Unit)
        }
    }

    @Test
    fun `should convert paragraph to numbered list without any delay when regex matches`() {

        // SETUP

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        stubReplaceBlock(root = root)
        buildViewModel()
        stubReplaceBlock(root)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val update = "1. "

        vm.onTextBlockTextChanged(
            BlockView.Text.Paragraph(
                id = paragraph.id,
                marks = emptyList(),
                text = update
            )
        )

        runBlockingTest {
            verify(replaceBlock, times(1)).invoke(
                params = eq(
                    ReplaceBlock.Params(
                        context = root,
                        target = paragraph.id,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.NUMBERED
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `should ignore create-numbered-list-item pattern and update text with delay`() {

        // SETUP

        val root = MockDataFactory.randomUuid()
        val numbered = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            ),
            children = emptyList()
        )
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(title.id, numbered.id)
            ),
            title,
            numbered
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val update = "1. "

        vm.onTextChanged(
            id = numbered.id,
            marks = numbered.content<Block.Content.Text>().marks,
            text = update
        )

        runBlockingTest {
            verify(updateText, never()).invoke(
                params = any()
            )

            verify(replaceBlock, never()).invoke(
                params = any()
            )
        }

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(replaceBlock, never()).invoke(
                params = any()
            )

            verify(updateText, times(1)).invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        target = numbered.id,
                        marks = numbered.content<Block.Content.Text>().marks,
                        text = update
                    )
                )
            )
        }
    }

    @Test
    fun `should not update text while processing paragraph-to-numbered-list editor pattern`() {

        // SETUP

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()
        stubUpdateText()
        stubReplaceBlock(root)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val update = "1. "

        vm.onTextBlockTextChanged(
            BlockView.Text.Paragraph(
                id = paragraph.id,
                marks = emptyList(),
                text = update
            )
        )

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verify(updateText, never()).invoke(
            scope = any(),
            params = any(),
            onResult = any()
        )
    }

    @Test
    fun `should update focus after block duplication`() {

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(paragraph.id)
            ),
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val newBlockId = MockDataFactory.randomUuid()

        stubDuplicateBlock(newBlockId, root)

        val focus = vm.focus.test()

        focus.assertValue { id -> id.isEmpty() }

        vm.onBlockFocusChanged(
            id = paragraph.id,
            hasFocus = true
        )

        focus.assertValue { id -> id == paragraph.id }

        vm.onActionMenuItemClicked(id = paragraph.id, action = ActionItemType.Duplicate)

        runBlockingTest {
            verify(duplicateBlock, times(1)).invoke(
                params = eq(
                    DuplicateBlock.Params(
                        context = root,
                        original = paragraph.id
                    )
                )
            )
        }

        verifyNoMoreInteractions(duplicateBlock)

        focus.assertValue { id -> id == newBlockId }
    }

    private fun stubDuplicateBlock(newBlockId: String, root: String) {
        duplicateBlock.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Pair(
                    newBlockId,
                    Payload(
                        context = root,
                        events = emptyList()
                    )
                )
            )
        }
    }

    @Test
    fun `should start updating title on title-text-changed event with delay`() {

        // SETUP

        val root = MockDataFactory.randomUuid()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(title.id)
            ),
            title
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        stubUpdateTitle()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val update = MockDataFactory.randomString()

        vm.onTitleTextChanged(update)

        runBlockingTest {
            verify(updateTitle, never()).invoke(
                params = any()
            )
        }

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(updateTitle, times(1)).invoke(
                params = any()
            )
        }
    }

    @Test
    fun `should enter multi-select mode and select blocks`() {

        // SETUP

        val paragraphs = listOf(
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(header.id) + paragraphs.map { it.id }
            )
        ) + listOf(header, title) + paragraphs

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val testObserver = vm.state.test()

        val titleView = BlockView.Title.Document(
            id = title.id,
            text = title.content<TXT>().text,
            isFocused = false
        )

        val initial = listOf(
            paragraphs[0].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[1].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[2].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            }
        )

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))

        vm.onEnterMultiSelectModeClicked()

        coroutineTestRule.advanceTime(150)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.map { view ->
                    view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onTextInputClicked(target = paragraphs[0].id)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.mapIndexed { i, view ->
                    if (i == 0)
                        view.copy(mode = BlockView.Mode.READ, isSelected = true)
                    else
                        view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onTextInputClicked(target = paragraphs[1].id)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.mapIndexed { i, view ->
                    if (i == 0 || i == 1)
                        view.copy(mode = BlockView.Mode.READ, isSelected = true)
                    else
                        view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onTextInputClicked(target = paragraphs[2].id)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.mapIndexed { i, view ->
                    if (i == 0 || i == 1 || i == 2)
                        view.copy(mode = BlockView.Mode.READ, isSelected = true)
                    else
                        view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onTextInputClicked(target = paragraphs[0].id)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.mapIndexed { i, view ->
                    if (i == 1 || i == 2)
                        view.copy(mode = BlockView.Mode.READ, isSelected = true)
                    else
                        view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onTextInputClicked(target = paragraphs[1].id)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.mapIndexed { i, view ->
                    if (i == 2)
                        view.copy(mode = BlockView.Mode.READ, isSelected = true)
                    else
                        view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onTextInputClicked(target = paragraphs[2].id)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.map { view ->
                    view.copy(mode = BlockView.Mode.READ)
                }
            )
        )
    }

    @Test
    fun `should exit multi-select mode and unselect blocks`() {

        // SETUP

        val root = MockDataFactory.randomUuid()

        val paragraphs = listOf(
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(header.id) + paragraphs.map { it.id }
            )
        ) + listOf(header, title) + paragraphs

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val testObserver = vm.state.test()

        val titleView = BlockView.Title.Document(
            id = title.id,
            text = title.content<TXT>().text,
            isFocused = false
        )

        val initial = listOf(
            paragraphs[0].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            }
        )

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))

        vm.onEnterMultiSelectModeClicked()

        coroutineTestRule.advanceTime(150)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.map { view ->
                    view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onTextInputClicked(target = paragraphs[0].id)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.mapIndexed { i, view ->
                    if (i == 0)
                        view.copy(mode = BlockView.Mode.READ, isSelected = true)
                    else
                        view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onExitMultiSelectModeClicked()

        coroutineTestRule.advanceTime(300)

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))
    }

    private fun stubClosePage(
        response: Either<Throwable, Unit> = Either.Right(Unit)
    ) {
        closePage.stub {
            onBlocking { invoke(any()) } doReturn response
        }
    }

    private fun stubSplitBlocks(root: String) {
        splitBlock.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Pair(
                    MockDataFactory.randomString(),
                    Payload(
                        context = root,
                        events = emptyList()
                    )
                )
            )
        }
    }

    fun stubOpenPage(
        context: Id = MockDataFactory.randomString(),
        events: List<Event> = emptyList()
    ) {
        openPage.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Result.Success(
                    Payload(
                        context = context,
                        events = events
                    )
                )
            )
        }
    }

    fun stubObserveEvents(flow: Flow<List<Event>> = flowOf()) {
        interceptEvents.stub {
            onBlocking { build(any()) } doReturn flow
        }
    }

    fun stubInterceptEvents(
        params: InterceptEvents.Params = InterceptEvents.Params(context = root),
        flow: Flow<List<Event>> = flowOf()
    ) {
        interceptEvents.stub {
            onBlocking { build(params) } doReturn flow
        }
    }

    private fun stubUpdateText() {
        updateText.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }
    }

    private fun stubReplaceBlock(root: String) {
        replaceBlock.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Pair(
                    MockDataFactory.randomString(),
                    Payload(
                        context = root,
                        events = emptyList()
                    )
                )
            )
        }
    }

    private fun stubCreateBlock(root: String) {
        createBlock.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Pair(
                    MockDataFactory.randomString(), Payload(
                        context = root,
                        events = listOf()
                    )
                )
            )
        }
    }

    private fun stubUpdateTitle() {
        updateTitle.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }
    }

    private fun stubDownloadFile() {
        downloadFile.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }
    }

    private fun stubUpdateTextColor(root: String) {
        updateTextColor.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = emptyList()
                )
            )
        }
    }

    private fun stubUpdateTextStyle(
        payload: Payload = Payload(
            context = MockDataFactory.randomUuid(),
            events = emptyList()
        )
    ) {
        updateTextStyle.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(payload)
        }
    }

    fun buildViewModel(urlBuilder: UrlBuilder = builder) {

        val storage = Editor.Storage()
        val proxies = Editor.Proxer()
        val memory = Editor.Memory(
            selections = SelectionStateHolder.Default()
        )

        vm = PageViewModel(
            getListPages = getListPages,
            openPage = openPage,
            closePage = closePage,
            createPage = createPage,
            interceptEvents = interceptEvents,
            updateLinkMarks = updateLinkMark,
            removeLinkMark = removeLinkMark,
            reducer = DocumentExternalEventReducer(),
            urlBuilder = urlBuilder,
            renderer = DefaultBlockViewRenderer(
                urlBuilder = urlBuilder,
                toggleStateHolder = ToggleStateHolder.Default(),
                counter = Counter.Default()
            ),
            archiveDocument = archiveDocument,
            createDocument = createDocument,
            createNewDocument = createNewDocument,
            analytics = analytics,
            orchestrator = Orchestrator(
                createBlock = createBlock,
                replaceBlock = replaceBlock,
                updateTextColor = updateTextColor,
                duplicateBlock = duplicateBlock,
                downloadFile = downloadFile,
                undo = undo,
                redo = redo,
                updateTitle = updateTitle,
                updateText = updateText,
                updateCheckbox = updateCheckbox,
                updateTextStyle = updateTextStyle,
                updateBackgroundColor = updateBackgroundColor,
                mergeBlocks = mergeBlocks,
                uploadBlock = uploadBlock,
                splitBlock = splitBlock,
                unlinkBlocks = unlinkBlocks,
                memory = memory,
                stores = storage,
                proxies = proxies,
                textInteractor = Interactor.TextInteractor(
                    proxies = proxies,
                    stores = storage,
                    matcher = DefaultPatternMatcher()
                ),
                updateAlignment = updateAlignment,
                setupBookmark = setupBookmark,
                paste = paste,
                copy = copy,
                move = move,
                turnIntoDocument = turnIntoDocument,
                analytics = analytics
            ),
            bridge = Bridge()
        )
    }

    @Test
    fun `should enter multi select mode and selections should be empty`() {
        // SETUP

        val root = MockDataFactory.randomUuid()

        val paragraphs = listOf(
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(header.id) + paragraphs.map { it.id }
            )
        ) + listOf(header, title) + paragraphs

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val testObserver = vm.state.test()

        val titleView = BlockView.Title.Document(
            id = title.id,
            text = title.content<TXT>().text,
            isFocused = false
        )

        val initial = listOf(
            paragraphs[0].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[1].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[2].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            }
        )

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))

        vm.onEnterMultiSelectModeClicked()

        coroutineTestRule.advanceTime(150)

        assertEquals(
            expected = 0,
            actual = vm.currentSelection().size
        )
    }

    @Test
    fun `should be two selected blocks in multi select mode`() {
        // SETUP

        val root = MockDataFactory.randomUuid()

        val paragraphs = listOf(
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(
                    type = Block.Content.Smart.Type.PAGE
                ),
                children = listOf(header.id) + paragraphs.map { it.id }
            )
        ) + listOf(header, title) + paragraphs

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val testObserver = vm.state.test()

        val titleView = BlockView.Title.Document(
            id = title.id,
            text = title.content<TXT>().text,
            isFocused = false
        )

        val initial = listOf(
            paragraphs[0].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[1].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[2].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            }
        )

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))

        vm.onEnterMultiSelectModeClicked()

        coroutineTestRule.advanceTime(150)

        assertEquals(
            expected = 0,
            actual = vm.currentSelection().size
        )

        vm.onTextInputClicked(target = paragraphs[1].id)
        vm.onTextInputClicked(target = paragraphs[2].id)

        assertEquals(
            expected = 2,
            actual = vm.currentSelection().size
        )
    }

    @Test
    fun `should be zero selected blocks after done click`() {
        // SETUP

        val root = MockDataFactory.randomUuid()

        val paragraphs = listOf(
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(header.id) + paragraphs.map { it.id }
            )
        ) + listOf(header, title) + paragraphs

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val testObserver = vm.state.test()

        val titleView = BlockView.Title.Document(
            id = title.id,
            text = title.content<TXT>().text,
            isFocused = false
        )

        val initial = listOf(
            paragraphs[0].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[1].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[2].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            }
        )

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))

        vm.onEnterMultiSelectModeClicked()

        coroutineTestRule.advanceTime(150)

        assertEquals(
            expected = 0,
            actual = vm.currentSelection().size
        )

        vm.onTextInputClicked(target = paragraphs[1].id)
        vm.onTextInputClicked(target = paragraphs[2].id)

        assertEquals(
            expected = 2,
            actual = vm.currentSelection().size
        )

        vm.onExitMultiSelectModeClicked()

        coroutineTestRule.advanceTime(300)

        assertEquals(
            expected = 0,
            actual = vm.currentSelection().size
        )
    }

    @Test
    fun `should be four selected blocks after select all click`() {

        // SETUP

        val root = MockDataFactory.randomUuid()

        val paragraphs = listOf(
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(header.id) + paragraphs.map { it.id }
            )
        ) + listOf(header, title) + paragraphs

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val testObserver = vm.state.test()

        val titleView = BlockView.Title.Document(
            id = title.id,
            text = title.content<TXT>().text,
            isFocused = false
        )

        val initial = listOf(
            paragraphs[0].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[1].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[2].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[3].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            }
        )

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))

        vm.onEnterMultiSelectModeClicked()

        coroutineTestRule.advanceTime(150)

        assertEquals(
            expected = 0,
            actual = vm.currentSelection().size
        )

        vm.onMultiSelectModeSelectAllClicked()

        coroutineTestRule.advanceTime(300)

        assertEquals(
            expected = 4,
            actual = vm.currentSelection().size
        )
    }

    @Test
    fun `should be zero selected blocks after unselect all click`() {
        // SETUP

        val root = MockDataFactory.randomUuid()

        val paragraphs = listOf(
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(
                    Block.Content.Smart.Type.PAGE
                ),
                children = listOf(header.id) + paragraphs.map { it.id }
            )
        ) + listOf(header, title) + paragraphs

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val testObserver = vm.state.test()

        val titleView = BlockView.Title.Document(
            id = title.id,
            text = title.content<TXT>().text,
            isFocused = false
        )

        val initial = listOf(
            paragraphs[0].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[1].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[2].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[3].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            }
        )

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))

        vm.onEnterMultiSelectModeClicked()

        coroutineTestRule.advanceTime(150)

        assertEquals(
            expected = 0,
            actual = vm.currentSelection().size
        )

        vm.onTextInputClicked(target = paragraphs[2].id)
        vm.onTextInputClicked(target = paragraphs[3].id)

        assertEquals(
            expected = 2,
            actual = vm.currentSelection().size
        )

        vm.onMultiSelectModeSelectAllClicked()

        coroutineTestRule.advanceTime(300)

        assertEquals(
            expected = 0,
            actual = vm.currentSelection().size
        )
    }

    @Test
    fun `should update style toolbar when no blocks in focus`() {

        val id1 = MockDataFactory.randomUuid()
        val id2 = MockDataFactory.randomUuid()
        val blocks = listOf(
            Block(
                id = id1,
                content = Block.Content.Text(
                    marks = listOf(
                        Block.Content.Text.Mark(
                            range = 0..7, type = Block.Content.Text.Mark.Type.BOLD
                        )
                    ),
                    text = "Foo Bar",
                    style = Block.Content.Text.Style.P,
                    align = Block.Align.AlignCenter
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = id2,
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = blocks.map { it.id }
            )
        ) + blocks

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)


        // TESTING

        val stateBefore = vm.controlPanelViewState.value

        assertNotNull(stateBefore)

        assertTrue(stateBefore.navigationToolbar.isVisible)
        assertFalse(stateBefore.stylingToolbar.isVisible)

        vm.onActionMenuItemClicked(blocks[0].id, ActionItemType.Style)

        vm.controlPanelViewState.test().assertValue(
            ControlPanelState(
                navigationToolbar = ControlPanelState.Toolbar.Navigation(
                    isVisible = false
                ),
                mainToolbar = ControlPanelState.Toolbar.Main(
                    isVisible = false
                ),
                stylingToolbar = ControlPanelState.Toolbar.Styling(
                    isVisible = true,
                    mode = StylingMode.BLOCK,
                    target = ControlPanelState.Toolbar.Styling.Target(
                        id = blocks[0].id,
                        text = blocks[0].content.asText().text,
                        color = null,
                        background = null,
                        alignment = Alignment.CENTER,
                        marks = listOf(
                            Markup.Mark(0, 7, Markup.Type.BOLD)
                        )
                    ),
                    config = StyleConfig(
                        visibleTypes = listOf(
                            StylingType.STYLE,
                            StylingType.TEXT_COLOR,
                            StylingType.BACKGROUND
                        ),
                        enabledAlignment = listOf(
                            Alignment.START,
                            Alignment.CENTER,
                            Alignment.END
                        ),
                        enabledMarkup = listOf(
                            Markup.Type.BOLD,
                            Markup.Type.ITALIC,
                            Markup.Type.STRIKETHROUGH,
                            Markup.Type.KEYBOARD,
                            Markup.Type.LINK
                        )
                    ),
                    props = ControlPanelState.Toolbar.Styling.Props(
                        isBold = true,
                        isItalic = false,
                        isStrikethrough = false,
                        isCode = false,
                        isLinked = false,
                        alignment = Alignment.CENTER,
                        color = null,
                        background = null
                    )
                ),
                multiSelect = ControlPanelState.Toolbar.MultiSelect(
                    isVisible = false
                ),
                mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset()
            )
        )

        vm.onStylingToolbarEvent(event = StylingEvent.Markup.Italic)

        vm.controlPanelViewState.test().assertValue(
            ControlPanelState(
                navigationToolbar = ControlPanelState.Toolbar.Navigation(
                    isVisible = false
                ),
                mainToolbar = ControlPanelState.Toolbar.Main(
                    isVisible = false
                ),
                stylingToolbar = ControlPanelState.Toolbar.Styling(
                    isVisible = true,
                    mode = StylingMode.BLOCK,
                    target = ControlPanelState.Toolbar.Styling.Target(
                        id = blocks[0].id,
                        text = blocks[0].content.asText().text,
                        color = null,
                        background = null,
                        alignment = Alignment.CENTER,
                        marks = listOf(
                            Markup.Mark(0, 7, Markup.Type.BOLD),
                            Markup.Mark(0, 7, Markup.Type.ITALIC)
                        )
                    ),
                    config = StyleConfig(
                        visibleTypes = listOf(
                            StylingType.STYLE,
                            StylingType.TEXT_COLOR,
                            StylingType.BACKGROUND
                        ),
                        enabledAlignment = listOf(
                            Alignment.START,
                            Alignment.CENTER,
                            Alignment.END
                        ),
                        enabledMarkup = listOf(
                            Markup.Type.BOLD,
                            Markup.Type.ITALIC,
                            Markup.Type.STRIKETHROUGH,
                            Markup.Type.KEYBOARD,
                            Markup.Type.LINK
                        )
                    ),
                    props = ControlPanelState.Toolbar.Styling.Props(
                        isBold = true,
                        isItalic = true,
                        isStrikethrough = false,
                        isCode = false,
                        isLinked = false,
                        alignment = Alignment.CENTER,
                        color = null,
                        background = null
                    )
                ),
                multiSelect = ControlPanelState.Toolbar.MultiSelect(
                    isVisible = false
                ),
                mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset()
            )
        )
    }

    @Test
    fun `should clear focus and selection after main toolbar action clicked `() {

        val id1 = MockDataFactory.randomUuid()
        val blocks = listOf(
            Block(
                id = id1,
                content = Block.Content.Text(
                    marks = listOf(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P,
                    align = Block.Align.AlignLeft
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = blocks.map { it.id }
            )
        ) + blocks

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)


        // TESTING

        val stateBefore = vm.controlPanelViewState.value

        assertNotNull(stateBefore)

        assertTrue(stateBefore.navigationToolbar.isVisible)
        assertFalse(stateBefore.stylingToolbar.isVisible)

        vm.onSelectionChanged(blocks[0].id, 0..3)

        vm.onMeasure(blocks[0].id, BlockDimensions(0, 0, 0, 0, 0, 0))

        val focus = vm.focus.test()

        focus.assertValue { id -> id.isEmpty() }

        vm.controlPanelViewState.test().assertValue(
            ControlPanelState.init()
        )
    }

    @Test
    fun `should show navigation toolbar after close style toolbar when opened from action menu`() {

        val id1 = MockDataFactory.randomUuid()
        val blocks = listOf(
            Block(
                id = id1,
                content = Block.Content.Text(
                    marks = listOf(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P,
                    align = Block.Align.AlignLeft
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = blocks.map { it.id }
            )
        ) + blocks

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)


        // TESTING

        val stateBefore = vm.controlPanelViewState.value

        assertNotNull(stateBefore)

        assertTrue(stateBefore.navigationToolbar.isVisible)
        assertFalse(stateBefore.stylingToolbar.isVisible)

        vm.onSelectionChanged(blocks[0].id, 0..3)

        vm.onMeasure(blocks[0].id, BlockDimensions(0, 0, 0, 0, 0, 0))

        vm.focus.test().assertValue { id -> id.isEmpty() }

        vm.controlPanelViewState.test().assertValue(
            ControlPanelState.init()
        )

        vm.onActionMenuItemClicked(blocks[0].id, ActionItemType.Style)

        vm.focus.test().assertValue { id -> id.isEmpty() }

        vm.onCloseBlockStyleToolbarClicked()

        vm.focus.test().assertValue { id -> id.isEmpty() }

        vm.controlPanelViewState.test().assertValue(
            ControlPanelState(
                navigationToolbar = ControlPanelState.Toolbar.Navigation(
                    isVisible = true
                ),
                mainToolbar = ControlPanelState.Toolbar.Main(
                    isVisible = false
                ),
                stylingToolbar = ControlPanelState.Toolbar.Styling.reset(),
                multiSelect = ControlPanelState.Toolbar.MultiSelect(
                    isVisible = false
                ),
                mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset()
            )
        )
    }

    @Test
    fun `should show main toolbar after close style toolbar when focused is true`() {

        val id1 = MockDataFactory.randomUuid()
        val blocks = listOf(
            Block(
                id = id1,
                content = Block.Content.Text(
                    marks = listOf(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P,
                    align = Block.Align.AlignLeft
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = blocks.map { it.id }
            )
        ) + blocks

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)


        // TESTING

        val stateBefore = vm.controlPanelViewState.value

        assertNotNull(stateBefore)

        assertTrue(stateBefore.navigationToolbar.isVisible)
        assertFalse(stateBefore.stylingToolbar.isVisible)

        vm.onBlockFocusChanged(blocks[0].id, true)

        vm.onSelectionChanged(blocks[0].id, 0..3)

        vm.onEditorContextMenuStyleClicked(0..3)

        vm.focus.test().assertValue { id -> id.isNotEmpty() }

        vm.onCloseBlockStyleToolbarClicked()

        vm.focus.test().assertValue { id -> id.isNotEmpty() }

        vm.controlPanelViewState.test().assertValue(
            ControlPanelState(
                navigationToolbar = ControlPanelState.Toolbar.Navigation(
                    isVisible = false
                ),
                mainToolbar = ControlPanelState.Toolbar.Main(
                    isVisible = true
                ),
                stylingToolbar = ControlPanelState.Toolbar.Styling.reset(),
                multiSelect = ControlPanelState.Toolbar.MultiSelect(
                    isVisible = false
                ),
                mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset()
            )
        )
    }
}