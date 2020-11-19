package com.anytypeio.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.ext.content
import com.anytypeio.anytype.presentation.page.editor.actions.ActionItemType
import com.anytypeio.anytype.presentation.page.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.page.editor.control.ControlPanelState.Toolbar
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.TXT
import com.jraska.livedata.test
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorQuickStartingScrollAndMoveTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

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

    @Test
    fun `should enter scroll-and-move via action menu where one paragraph is already selected`() {

        // SETUP

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, a.id, b.id)
        )

        val document = listOf(page, header, title, a, b)

        stubOpenDocument(document)
        stubInterceptEvents()

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        val controlPanelTestObserver = vm.controlPanelViewState.test()
        val viewStateTestObserver = vm.state.test()

        vm.apply {
            onBlockFocusChanged(id = b.id, hasFocus = true)
            onBlockToolbarBlockActionsClicked()
            onActionMenuItemClicked(id = b.id, action = ActionItemType.SAM)
        }

        // VERIFYING

        controlPanelTestObserver.assertValue(
            ControlPanelState(
                navigationToolbar = Toolbar.Navigation(isVisible = false),
                mainToolbar = Toolbar.Main(isVisible = false),
                mentionToolbar = Toolbar.MentionToolbar(
                    isVisible = false,
                    cursorCoordinate = null,
                    mentionFilter = null,
                    mentionFrom = null
                ),
                multiSelect = Toolbar.MultiSelect(
                    isVisible = true,
                    count = 1,
                    isScrollAndMoveEnabled = true
                ),
                stylingToolbar = Toolbar.Styling(isVisible = false)
            )
        )

        viewStateTestObserver.assertValue(
            ViewState.Success(
                listOf(
                    BlockView.Title.Document(
                        id = title.id,
                        text = title.content<TXT>().text,
                        mode = BlockView.Mode.READ,
                    ),
                    BlockView.Text.Numbered(
                        id = a.id,
                        text = a.content<TXT>().text,
                        number = 1,
                        mode = BlockView.Mode.READ,
                        isSelected = false
                    ),
                    BlockView.Text.Paragraph(
                        id = b.id,
                        text = b.content<TXT>().text,
                        mode = BlockView.Mode.READ,
                        isSelected = true
                    )
                )
            )
        )
    }
}