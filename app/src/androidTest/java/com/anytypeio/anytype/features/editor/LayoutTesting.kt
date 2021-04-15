package com.anytypeio.anytype.features.editor

import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Layout
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.cover.CoverType
import com.anytypeio.anytype.emojifier.data.DefaultDocumentEmojiIconProvider
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.page.cover.CoverColor
import com.anytypeio.anytype.ui.page.PageFragment
import com.anytypeio.anytype.utils.*
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LayoutTesting : EditorTestSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val args = bundleOf(PageFragment.ID_KEY to root)

    private val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            style = Block.Content.Text.Style.TITLE,
            text = "Object's layout testing",
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    private val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    private val paragraph = Block(
        id = MockDataFactory.randomUuid(),
        fields = Block.Fields.empty(),
        children = emptyList(),
        content = Block.Content.Text(
            text = "Testing started...",
            marks = emptyList(),
            style = Block.Content.Text.Style.P
        )
    )

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun shouldRenderToDoLayoutWithoutCoverAndIconContainer() {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        val details = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
                        "layout" to Layout.TODO.code.toDouble()
                    )
                )
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()

        stubOpenDocument(
            document = document,
            details = details
        )

        // TESTING

        launch(args)

        R.id.recycler.rVMatcher().apply {
            onItemView(0, R.id.cover).checkIsNotDisplayed()
            onItemView(0, R.id.documentIconContainer).checkIsNotDisplayed()
            onItemView(0, R.id.title).checkHasText(title.content<Block.Content.Text>().text)
        }
    }

    @Test
    fun shouldRenderToDoLayoutWithCheckedCheckbox() {

        // SETUP

        val checkedTitle = title.copy(
            content = title.content<Block.Content.Text>().copy(
                isChecked = true
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, checkedTitle, paragraph)

        val details = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
                        "layout" to Layout.TODO.code.toDouble()
                    )
                )
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()

        stubOpenDocument(
            document = document,
            details = details
        )

        // TESTING

        launch(args)

        R.id.recycler.rVMatcher().apply {
            onItemView(0, R.id.cover).checkIsNotDisplayed()
            onItemView(0, R.id.todoTitleCheckbox).checkIsSelected()
            onItemView(0, R.id.documentIconContainer).checkIsNotDisplayed()
            onItemView(0, R.id.title).checkHasText(title.content<Block.Content.Text>().text)
        }
    }

    @Test
    fun shouldRenderToDoLayoutWithCoverButWithoutIcon() {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        val details = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
                        "layout" to Layout.TODO.code.toDouble(),
                        "coverType" to CoverType.COLOR.code.toDouble(),
                        "coverId" to CoverColor.BLUE.code,
                    )
                )
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()

        stubOpenDocument(
            document = document,
            details = details
        )

        // TESTING

        launch(args)

        R.id.recycler.rVMatcher().apply {
            onItemView(0, R.id.cover).checkIsDisplayed()
            onItemView(0, R.id.documentIconContainer).checkIsNotDisplayed()
            onItemView(0, R.id.title).checkHasText(title.content<Block.Content.Text>().text)
        }
    }

    @Test
    fun shouldRenderProfileLayoutWithoutCoverButWithAvatar() {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        val details = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
                        "layout" to Layout.PROFILE.code.toDouble()
                    )
                )
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()

        stubOpenDocument(
            document = document,
            details = details
        )

        // TESTING

        launch(args)

        R.id.recycler.rVMatcher().apply {
            onItemView(0, R.id.cover).checkIsNotDisplayed()
            onItemView(0, R.id.imageText).checkHasText(title.content<Block.Content.Text>().text.first().toString())
            onItemView(0, R.id.title).checkHasText(title.content<Block.Content.Text>().text)
        }
    }

    @Test
    fun shouldRenderProfileLayoutWithCoverAndAvatar() {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        val details = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
                        "layout" to Layout.PROFILE.code.toDouble(),
                        "coverType" to CoverType.COLOR.code.toDouble(),
                        "coverId" to CoverColor.BLUE.code,
                    )
                )
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()

        stubOpenDocument(
            document = document,
            details = details
        )

        // TESTING

        launch(args)

        R.id.recycler.rVMatcher().apply {
            onItemView(0, R.id.cover).checkIsDisplayed()
            onItemView(0, R.id.imageText).checkHasText(title.content<Block.Content.Text>().text.first().toString())
            onItemView(0, R.id.title).checkHasText(title.content<Block.Content.Text>().text)
        }
    }

    @Test
    fun shouldRenderBasicLayoutWithoutCoverButWithAvatar() {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        val details = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
                        "layout" to Layout.BASIC.code.toDouble()
                    )
                )
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()

        stubOpenDocument(
            document = document,
            details = details
        )

        // TESTING

        launch(args)

        R.id.recycler.rVMatcher().apply {
            onItemView(0, R.id.cover).checkIsNotDisplayed()
            onItemView(0, R.id.emojiIcon).checkIsDisplayed()
            onItemView(0, R.id.title).checkHasText(title.content<Block.Content.Text>().text)
        }
    }

    @Test
    fun shouldRenderBasicLayoutWithCoverAndAvatar() {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        val details = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
                        "layout" to Layout.BASIC.code.toDouble(),
                        "coverType" to CoverType.COLOR.code.toDouble(),
                        "coverId" to CoverColor.BLUE.code,
                    )
                )
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()

        stubOpenDocument(
            document = document,
            details = details
        )

        // TESTING

        launch(args)

        R.id.recycler.rVMatcher().apply {
            onItemView(0, R.id.cover).checkIsDisplayed()
            onItemView(0, R.id.emojiIcon).checkIsDisplayed()
            onItemView(0, R.id.title).checkHasText(title.content<Block.Content.Text>().text)
        }
    }
}