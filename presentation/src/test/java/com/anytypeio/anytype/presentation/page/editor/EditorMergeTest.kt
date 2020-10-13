package com.anytypeio.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.domain.block.interactor.MergeBlocks
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.ext.content
import com.anytypeio.anytype.presentation.page.PageViewModel
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verifyBlocking
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorMergeTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should update text and proceed with merging the first paragraph with the second on non-empty-block-backspace-pressed event`() {

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

        val first = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val second = Block(
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
            children = listOf(header.id, first.id, second.id)
        )

        val doc = listOf(page, header, title, first, second)

        stubInterceptEvents()
        stubOpenDocument(doc)
        stubMergeBlocks(root)
        stubUpdateText()

        val vm = buildViewModel()

        vm.onStart(root)

        vm.onBlockFocusChanged(
            id = second.id,
            hasFocus = true
        )

        val text = MockDataFactory.randomString()

        vm.onTextChanged(
            id = second.id,
            marks = emptyList(),
            text = text
        )

        vm.onNonEmptyBlockBackspaceClicked(
            id = second.id,
            marks = emptyList(),
            text = text
        )

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(updateText, times(1)) {
            invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = text,
                        marks = emptyList(),
                        target = second.id
                    )
                )
            )
        }

        verifyBlocking(mergeBlocks, times(1)) {
            invoke(
                params = eq(
                    MergeBlocks.Params(
                        context = root,
                        pair = Pair(first.id, second.id)
                    )
                )
            )
        }
    }

    @Test
    fun `should merge two text blocks from two different divs`() {

        // SETUP

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val div1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            children = listOf(a.id),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.DIV
            )
        )

        val div2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            children = listOf(b.id),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.DIV
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
            children = listOf(div1.id, div2.id)
        )

        val doc = listOf(page, div1, div2, a, b)

        stubOpenDocument(doc)
        stubInterceptEvents()
        stubUpdateText()
        stubMergeBlocks(root)

        val vm = buildViewModel()

        vm.onStart(root)

        vm.onBlockFocusChanged(b.id, true)

        vm.onNonEmptyBlockBackspaceClicked(
            id = b.id,
            text = b.content<Block.Content.Text>().text,
            marks = b.content<Block.Content.Text>().marks
        )

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(mergeBlocks, times(1)) {
            invoke(
                params = eq(
                    MergeBlocks.Params(
                        context = root,
                        pair = Pair(a.id, b.id)
                    )
                )
            )
        }
    }

    @Test
    fun `should merge two text blocks from the first of two divs`() {

        // SETUP

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val c = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val d = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val div1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            children = listOf(a.id, b.id),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.DIV
            )
        )

        val div2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            children = listOf(c.id, d.id),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.DIV
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
            children = listOf(div1.id, div2.id)
        )

        val doc = listOf(page, div1, div2, a, b, c, d)

        stubOpenDocument(doc)
        stubInterceptEvents()
        stubUpdateText()
        stubMergeBlocks(root)

        val vm = buildViewModel()

        vm.onStart(root)

        vm.onBlockFocusChanged(d.id, true)

        vm.onNonEmptyBlockBackspaceClicked(
            id = d.id,
            text = d.content<Block.Content.Text>().text,
            marks = d.content<Block.Content.Text>().marks
        )

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(mergeBlocks, times(1)) {
            invoke(
                params = eq(
                    MergeBlocks.Params(
                        context = root,
                        pair = Pair(c.id, d.id)
                    )
                )
            )
        }
    }

    @Test
    fun `should not merge text block with the previous block if this previous block is not a text block`() {

        // SETUP

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Divider(type = Block.Content.Divider.Type.LINE),
            children = emptyList()
        )

        val b = Block(
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
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
            children = listOf(a.id, b.id)
        )

        val doc = listOf(page, a, b)

        stubOpenDocument(doc)
        stubInterceptEvents()
        stubUpdateText()
        stubMergeBlocks(root)

        val vm = buildViewModel()

        vm.onStart(root)

        vm.onBlockFocusChanged(b.id, true)

        vm.onNonEmptyBlockBackspaceClicked(
            id = b.id,
            text = b.content<Block.Content.Text>().text,
            marks = b.content<Block.Content.Text>().marks
        )

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyZeroInteractions(mergeBlocks)
    }
}