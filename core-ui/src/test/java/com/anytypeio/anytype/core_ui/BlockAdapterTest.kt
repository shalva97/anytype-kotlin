package com.anytypeio.anytype.core_ui

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Editable
import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_ui.common.ThemeColor
import com.anytypeio.anytype.core_ui.features.editor.holders.error.FileError
import com.anytypeio.anytype.core_ui.features.editor.holders.error.PictureError
import com.anytypeio.anytype.core_ui.features.editor.holders.error.VideoError
import com.anytypeio.anytype.core_ui.features.editor.holders.media.File
import com.anytypeio.anytype.core_ui.features.editor.holders.media.Picture
import com.anytypeio.anytype.core_ui.features.editor.holders.media.Video
import com.anytypeio.anytype.core_ui.features.editor.holders.other.Page
import com.anytypeio.anytype.core_ui.features.editor.holders.other.Title.Document
import com.anytypeio.anytype.core_ui.features.editor.holders.placeholders.BookmarkPlaceholder
import com.anytypeio.anytype.core_ui.features.editor.holders.placeholders.FilePlaceholder
import com.anytypeio.anytype.core_ui.features.editor.holders.placeholders.PicturePlaceholder
import com.anytypeio.anytype.core_ui.features.editor.holders.placeholders.VideoPlaceholder
import com.anytypeio.anytype.core_ui.features.editor.holders.text.*
import com.anytypeio.anytype.core_ui.features.editor.holders.upload.FileUpload
import com.anytypeio.anytype.core_ui.features.editor.holders.upload.PictureUpload
import com.anytypeio.anytype.core_ui.features.editor.holders.upload.VideoUpload
import com.anytypeio.anytype.core_ui.features.page.*
import com.anytypeio.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.BACKGROUND_COLOR_CHANGED
import com.anytypeio.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.CURSOR_CHANGED
import com.anytypeio.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.FOCUS_CHANGED
import com.anytypeio.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.READ_WRITE_MODE_CHANGED
import com.anytypeio.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.SELECTION_CHANGED
import com.anytypeio.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.TEXT_CHANGED
import com.anytypeio.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.TEXT_COLOR_CHANGED
import com.anytypeio.anytype.core_ui.tools.ClipboardInterceptor
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.hexColorCode
import com.nhaarman.mockitokotlin2.mock
import kotlinx.android.synthetic.main.item_block_bookmark_placeholder.view.*
import kotlinx.android.synthetic.main.item_block_checkbox.view.*
import kotlinx.android.synthetic.main.item_block_page.view.*
import kotlinx.android.synthetic.main.item_block_toggle.view.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit
import kotlin.test.*

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class BlockAdapterTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val clipboardInterceptor : ClipboardInterceptor = mock()

    @Test
    fun `should return transparent hex code when int color value is zero`() {

        val transparentColor = 0

        val actual = transparentColor.hexColorCode()

        assertEquals(
            expected = "#00000000",
            actual = actual
        )
    }

    @Test
    fun `should create paragraph view holder`() {

        val paragraph = BlockView.Text.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        assertEquals(
            expected = Paragraph::class,
            actual = holder::class
        )
    }

    @Test
    fun `should set text for paragraph holder`() {

        // Setup

        val paragraph = BlockView.Text.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        val text = holder.content.text.toString()

        assertEquals(
            expected = paragraph.text,
            actual = text
        )
    }

    @Test
    fun `should set text color for paragraph holder`() {

        // Setup

        val paragraph = BlockView.Text.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            color = ThemeColor.BLUE.title
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        val color = holder.content.currentTextColor

        assertEquals(
            expected = ThemeColor.BLUE.text,
            actual = color
        )
    }

    @Test
    fun `should update paragraph holder with new text`() {

        // Setup

        val paragraph = BlockView.Text.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val updated = paragraph.copy(
            text = MockDataFactory.randomString()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        // Testing

        assertEquals(
            expected = paragraph.text,
            actual = holder.content.text.toString()
        )

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(TEXT_CHANGED)
                )
            ),
            onSelectionChanged = { _, _ -> },
            onTextChanged = {},
            clicked = {}
        )

        assertEquals(
            expected = updated.text,
            actual = holder.content.text.toString()
        )
    }

    @Test
    fun `should update paragraph background color`() {

        // Setup

        val paragraph = BlockView.Text.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val updated = paragraph.copy(
            backgroundColor = ThemeColor.PURPLE.title
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        // Testing

        assertEquals(
            expected = paragraph.text,
            actual = holder.content.text.toString()
        )

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(BACKGROUND_COLOR_CHANGED)
                )
            ),
            onSelectionChanged = { _, _ -> },
            onTextChanged = {},
            clicked = {}
        )

        assertEquals(
            expected = ThemeColor.PURPLE.background,
            actual = (holder.root.background as ColorDrawable).color
        )
    }

    @Test
    fun `should update paragraph holder with new text color`() {

        // Setup

        val paragraph = BlockView.Text.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            color = ThemeColor.BLUE.title
        )

        val updated = paragraph.copy(
            color = ThemeColor.GREEN.title
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        // Testing

        assertEquals(
            expected = ThemeColor.BLUE.text,
            actual = holder.content.currentTextColor
        )

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(TEXT_COLOR_CHANGED)
                )
            ),
            onSelectionChanged = { _, _ -> },
            onTextChanged = {},
            clicked = {}
        )

        assertEquals(
            expected = ThemeColor.GREEN.text,
            actual = holder.content.currentTextColor
        )
    }

    @Test
    fun `should update paragraph cursor`() {

        // Setup

        val paragraph = BlockView.Text.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            cursor = null
        )

        val updated = paragraph.copy(
            cursor = 2
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        // Testing

        assertEquals(
            expected = paragraph.text,
            actual = holder.content.text.toString()
        )

        assertEquals(
            expected = 0,
            actual = holder.content.selectionStart
        )

        assertEquals(
            expected = 0,
            actual = holder.content.selectionEnd
        )

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(CURSOR_CHANGED)
                )
            ),
            onSelectionChanged = { _, _ -> },
            onTextChanged = {},
            clicked = {}
        )

        assertEquals(
            expected = 2,
            actual = holder.content.selectionStart
        )

        assertEquals(
            expected = 2,
            actual = holder.content.selectionEnd
        )
    }

    @Test
    fun `should update title cursor`() {

        // Setup

        val title = BlockView.Title.Document(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isFocused = false,
            cursor = null
        )

        val updated = title.copy(
            cursor = 2,
            isFocused = true
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Document)

        // Testing

        assertEquals(
            expected = title.text,
            actual = holder.content.text.toString()
        )

        holder.processPayloads(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(FOCUS_CHANGED, CURSOR_CHANGED)
                )
            )
        )

        assertEquals(
            expected = 2,
            actual = holder.content.selectionStart
        )

        assertEquals(
            expected = 2,
            actual = holder.content.selectionEnd
        )
    }

    @Test
    fun `should not trigger on-text-changed event when binding data to paragraph holder`() {

        // Setup

        val events = mutableListOf<Pair<String, String>>()

        val paragraph = BlockView.Text.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(
            views = views,
            onTextChanged = { id, editable ->
                events.add(Pair(id, editable.toString()))
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        assertEquals(
            expected = emptyList<Pair<String, String>>(),
            actual = events
        )
    }

    @Test
    fun `should not trigger on-text-changed event when updating paragraph text with change payload`() {

        // Setup

        val events = mutableListOf<Pair<String, String>>()

        val paragraph = BlockView.Text.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val updated = paragraph.copy(
            text = MockDataFactory.randomString()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(
            views = views,
            onTextChanged = { id, editable ->
                events.add(Pair(id, editable.toString()))
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(TEXT_CHANGED)
                )
            ),
            item = updated,
            onSelectionChanged = { _, _ -> },
            onTextChanged = {},
            clicked = {}
        )

        assertEquals(
            expected = emptyList<Pair<String, String>>(),
            actual = events
        )
    }

    @Test
    fun `should create title view holder`() {

        val title = BlockView.Title.Document(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isFocused = MockDataFactory.randomBoolean()
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        assertEquals(
            expected = Document::class,
            actual = holder::class
        )
    }

    @Test
    fun `should set text for title holder`() {

        // Setup

        val title = BlockView.Title.Document(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isFocused = MockDataFactory.randomBoolean()
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Document)

        val text = holder.content.text.toString()

        assertEquals(
            expected = title.text,
            actual = text
        )
    }

    @Test
    fun `should update title holder with new text`() {

        // Setup

        val title = BlockView.Title.Document(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isFocused = MockDataFactory.randomBoolean()
        )

        val updated = title.copy(
            text = MockDataFactory.randomString()
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Document)

        // Testing

        assertEquals(
            expected = title.text,
            actual = holder.content.text.toString()
        )

        holder.processPayloads(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(TEXT_CHANGED)
                )
            )
        )

        assertEquals(
            expected = updated.text,
            actual = holder.content.text.toString()
        )
    }

    @Test
    fun `should call back when title view gets focused`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val title = BlockView.Title.Document(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isFocused = false
        )

        val views = listOf(title)

        val adapter = buildAdapter(
            views = views,
            onFocusChanged = { id, hasFocus ->
                events.add(Pair(id, hasFocus))
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Document)

        // Testing

        assertEquals(
            expected = false,
            actual = holder.content.hasFocus()
        )

        holder.content.requestFocus()

        assertEquals(
            expected = listOf(
                Pair(title.id, true)
            ),
            actual = events
        )
    }

    @Test
    fun `should not trigger on-text-changed event when binding data to title holder`() {

        // Setup

        val events = mutableListOf<Pair<String, String>>()

        val title = BlockView.Title.Document(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isFocused = MockDataFactory.randomBoolean()
        )

        val views = listOf(title)

        val adapter = buildAdapter(
            views = views,
            onTextChanged = { id, editable ->
                events.add(Pair(id, editable.toString()))
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Document)

        Robolectric.getForegroundThreadScheduler().apply {
            advanceBy(BlockViewHolder.FOCUS_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        }

        assertEquals(
            expected = emptyList<Pair<String, String>>(),
            actual = events
        )
    }

    @Test
    fun `should apply indent to paragraph view`() {

        val paragraph = BlockView.Text.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(paragraph)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.bindViewHolder(holder, 0)

        check(holder is Paragraph)

        val actual = holder.content.paddingLeft

        val expected =
            holder.dimen(R.dimen.default_document_content_padding_start) + paragraph.indent * holder.dimen(
                R.dimen.indent
            )

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to header-one view`() {

        val view = BlockView.Text.Header.One(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_ONE)

        adapter.bindViewHolder(holder, 0)

        check(holder is HeaderOne)

        val actual = holder.content.paddingLeft

        val expected =
            holder.dimen(R.dimen.default_document_content_padding_start) + view.indent * holder.dimen(
                R.dimen.indent
            )

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to header-two view`() {

        val view = BlockView.Text.Header.Two(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_TWO)

        adapter.bindViewHolder(holder, 0)

        check(holder is HeaderTwo)

        val actual = holder.content.paddingLeft

        val expected =
            holder.dimen(R.dimen.default_document_content_padding_start) + view.indent * holder.dimen(
                R.dimen.indent
            )

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to header-three view`() {

        val view = BlockView.Text.Header.Three(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_THREE)

        adapter.bindViewHolder(holder, 0)

        check(holder is HeaderThree)

        val actual = holder.content.paddingLeft

        val expected =
            holder.dimen(R.dimen.default_document_content_padding_start) + view.indent * holder.dimen(
                R.dimen.indent
            )

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to checkbox view`() {

        val view = BlockView.Text.Checkbox(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_CHECKBOX)

        adapter.bindViewHolder(holder, 0)

        check(holder is Checkbox)

        val actual = holder.itemView.checkboxIcon.paddingLeft

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to toggle view`() {

        val view = BlockView.Text.Toggle(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt(),
            toggled = MockDataFactory.randomBoolean(),
            backgroundColor = null,
            color = null,
            isFocused = false,
            marks = emptyList()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TOGGLE)

        adapter.bindViewHolder(holder, 0)

        check(holder is Toggle)

        val actual =
            (holder.itemView.guideline.layoutParams as ConstraintLayout.LayoutParams).guideBegin

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to file placeholder view`() {

        val view = BlockView.MediaPlaceholder.File(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_FILE_PLACEHOLDER)

        adapter.bindViewHolder(holder, 0)

        check(holder is FilePlaceholder)

        val actual = holder.itemView.marginLeft

        val expected =
            holder.dimen(R.dimen.bookmark_default_margin_start) + view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to file error view`() {

        val view = BlockView.Error.File(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_FILE_ERROR)

        adapter.bindViewHolder(holder, 0)

        check(holder is FileError)

        val actual = holder.itemView.marginLeft

        val expected =
            holder.dimen(R.dimen.bookmark_default_margin_start) + view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to file upload view`() {

        val view = BlockView.Upload.File(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_FILE_UPLOAD)

        adapter.bindViewHolder(holder, 0)

        check(holder is FileUpload)

        val actual = holder.itemView.marginLeft

        val expected =
            holder.dimen(R.dimen.bookmark_default_margin_start) + view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to video view`() {

        val view = BlockView.Media.Video(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt(),
            hash = MockDataFactory.randomString(),
            url = MockDataFactory.randomString(),
            mime = MockDataFactory.randomString(),
            name = MockDataFactory.randomString(),
            size = MockDataFactory.randomLong()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_VIDEO)

        adapter.bindViewHolder(holder, 0)

        check(holder is Video)

        val actual = holder.itemView.marginLeft

        val expected =
            holder.dimen(R.dimen.bookmark_default_margin_start) + view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to video placeholder view`() {

        val view = BlockView.MediaPlaceholder.Video(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_VIDEO_PLACEHOLDER)

        adapter.bindViewHolder(holder, 0)

        check(holder is VideoPlaceholder)

        val actual = holder.itemView.marginLeft

        val expected =
            holder.dimen(R.dimen.bookmark_default_margin_start) + view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to video upload view`() {

        val view = BlockView.Upload.Video(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_VIDEO_UPLOAD)

        adapter.bindViewHolder(holder, 0)

        check(holder is VideoUpload)

        val actual = holder.itemView.marginLeft

        val expected =
            holder.dimen(R.dimen.bookmark_default_margin_start) + view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to video error view`() {

        val view = BlockView.Error.Video(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_VIDEO_ERROR)

        adapter.bindViewHolder(holder, 0)

        check(holder is VideoError)

        val actual = holder.itemView.marginLeft

        val expected =
            holder.dimen(R.dimen.bookmark_default_margin_start) + view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to page view`() {

        val view = BlockView.Page(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt(),
            emoji = null,
            isEmpty = MockDataFactory.randomBoolean(),
            image = null
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PAGE)

        adapter.bindViewHolder(holder, 0)

        check(holder is Page)

        val actual =
            (holder.itemView.pageGuideline.layoutParams as ConstraintLayout.LayoutParams).guideBegin

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

//    Turned off the test, because Robolectric is not working with MateralCardView
//    @Test
//    fun `should apply indent to bookmark view`() {
//
//        val view = BlockView.Bookmark.View(
//            id = MockDataFactory.randomUuid(),
//            indent = MockDataFactory.randomInt(),
//            description = MockDataFactory.randomString(),
//            title = MockDataFactory.randomString(),
//            faviconUrl = MockDataFactory.randomString(),
//            imageUrl = MockDataFactory.randomString(),
//            url = MockDataFactory.randomString()
//        )
//
//        val views = listOf(view)
//
//        val recycler = RecyclerView(context).apply {
//            layoutManager = LinearLayoutManager(context)
//        }
//
//        val adapter = buildAdapter(views)
//
//        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_BOOKMARK)
//
//        adapter.bindViewHolder(holder, 0)
//
//        check(holder is BlockViewHolder.Bookmark)
//
//        val actual = (holder.itemView.bookmarkRoot).marginLeft
//
//        val expected = view.indent * holder.dimen(R.dimen.indent) + holder.dimen(R.dimen.dp_12)
//
//        assertEquals(expected, actual)
//    }

    @Test
    fun `should apply indent to bookmark placeholder view`() {

        val view = BlockView.MediaPlaceholder.Bookmark(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder =
            adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_BOOKMARK_PLACEHOLDER)

        adapter.bindViewHolder(holder, 0)

        check(holder is BookmarkPlaceholder)

        val actual = holder.itemView.root.marginLeft

        val expected = view.indent * holder.dimen(R.dimen.indent)+ holder.dimen(R.dimen.bookmark_default_margin_start)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to picture view`() {

        val view = BlockView.Media.Picture(
            id = MockDataFactory.randomUuid(),
            hash = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt(),
            mime = MockDataFactory.randomString(),
            name = MockDataFactory.randomString(),
            size = MockDataFactory.randomLong(),
            url = MockDataFactory.randomString()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PICTURE)

        adapter.bindViewHolder(holder, 0)

        check(holder is Picture)

        val actual = holder.itemView.marginLeft

        val expected =
            holder.dimen(R.dimen.bookmark_default_margin_start) + view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to picture placeholder view`() {

        val view = BlockView.MediaPlaceholder.Picture(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder =
            adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PICTURE_PLACEHOLDER)

        adapter.bindViewHolder(holder, 0)

        check(holder is PicturePlaceholder)

        val actual = holder.itemView.marginLeft

        val expected =
            holder.dimen(R.dimen.bookmark_default_margin_start) + view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to picture error view`() {

        val view = BlockView.Error.Picture(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PICTURE_ERROR)

        adapter.bindViewHolder(holder, 0)

        check(holder is PictureError)

        val actual = holder.itemView.marginLeft

        val expected =
            holder.dimen(R.dimen.bookmark_default_margin_start) + view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to picture upload view`() {

        val view = BlockView.Upload.Picture(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PICTURE_UPLOAD)

        adapter.bindViewHolder(holder, 0)

        check(holder is PictureUpload)

        val actual = holder.itemView.marginLeft

        val expected =
            holder.dimen(R.dimen.bookmark_default_margin_start) + view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply focus to title block with payload change`() {

        // Setup

        val title = BlockView.Title.Document(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isFocused = false
        )

        val updated = title.copy(
            isFocused = true
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Document)

        // Testing

        assertEquals(
            expected = false,
            actual = holder.content.hasFocus()
        )

        holder.processPayloads(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(FOCUS_CHANGED)
                )
            )
        )

        assertEquals(
            expected = true,
            actual = holder.content.hasFocus()
        )
    }

    @Test
    fun `should remove focus from title block with payload change`() {

        // Setup

        val title = BlockView.Title.Document(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isFocused = true
        )

        val updated = title.copy(
            isFocused = false
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Document)

        // Testing

        assertEquals(
            expected = true,
            actual = holder.content.hasFocus()
        )

        holder.processPayloads(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(FOCUS_CHANGED)
                )
            )
        )

        assertEquals(
            expected = false,
            actual = holder.content.hasFocus()
        )
    }

    @Test
    fun `should trigger title-text-changed callback`() {

        // Setup

        val events = mutableListOf<Editable>()

        val title = BlockView.Title.Document(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isFocused = true
        )

        val views = listOf(title)

        val adapter = buildAdapter(
            views = views,
            onTitleTextChanged = { editable ->
                events.add(editable)
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Document)

        // Testing

        assertTrue { events.isEmpty() }

        holder.content.setText(MockDataFactory.randomString())

        assertTrue { events.size == 1 && events.first() == holder.content.text }
    }

    /**
     * Test is broken because of holder.adapterPosition method, will be fixes
     *
     */
//    @Test
//    fun `should trigger enter-end-line-title event`() {
//
//        // Setup
//
//        var triggered = false
//
//        val txt = MockDataFactory.randomString()
//
//        val title = BlockView.Title.Document(
//            text = MockDataFactory.randomString(),
//            id = MockDataFactory.randomUuid(),
//            cursor = txt.length,
//            isFocused = true
//        )
//
//        val views = listOf(title)
//
//        val adapter = buildAdapter(
//            views = views,
//            onSplitLineEnterClicked = { _, _, _ -> triggered = true }
//        )
//
//        val recycler = RecyclerView(context).apply {
//            layoutManager = LinearLayoutManager(context)
//            this.adapter = adapter
//        }
//
//        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)
//
//        adapter.onBindViewHolder(holder, 0)
//
//        check(holder is Document)
//
//        // Testing
//
//        assertTrue { !triggered }
//
//        holder.content.onEditorAction(EditorInfo.IME_ACTION_GO)
//
//        assertTrue { triggered }
//    }

    @Test
    fun `paragraph holder should be in read mode`() {

        // Setup

        val paragraph = BlockView.Text.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.READ
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = holder.content.isTextSelectable
        )

        assertNull(holder.content.selectionWatcher)
    }

    @Test
    fun `paragraph holder should have its content selected or not based on state`() {

        // Setup

        val firstParagraph = BlockView.Text.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.READ,
            isSelected = true
        )

        val secondParagraph = BlockView.Text.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.READ,
            isSelected = false
        )

        val views = listOf(firstParagraph, secondParagraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val firstHolder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)
        val secondHolder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        // Testing

        adapter.onBindViewHolder(firstHolder, 0)
        adapter.onBindViewHolder(secondHolder, 1)

        check(firstHolder is Paragraph)
        check(secondHolder is Paragraph)

        assertTrue { firstHolder.content.isSelected }
        assertFalse { secondHolder.content.isSelected }
    }

    @Test
    fun `paragraph holder should be in edit mode`() {

        // Setup

        val paragraph = BlockView.Text.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = holder.content.isTextSelectable
        )

        assertNotNull(holder.content.selectionWatcher)
    }

    @Test
    fun `paragraph holder should enter read mode after being in edit mode`() {

        // Setup

        val paragraph = BlockView.Text.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT
        )

        val updated = paragraph.copy(
            mode = BlockView.Mode.READ
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        // Testing

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = holder.content.isTextSelectable
        )

        assertNotNull(holder.content.selectionWatcher)

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(READ_WRITE_MODE_CHANGED)
                )
            ),
            onSelectionChanged = { _, _ -> },
            onTextChanged = {},
            clicked = {}
        )

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = holder.content.isTextSelectable
        )

        assertNull(holder.content.selectionWatcher)
    }

    @Test
    fun `title holder should be in read mode`() {

        // Setup

        val title = BlockView.Title.Document(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.READ,
            isFocused = false
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Document)

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = holder.content.isTextSelectable
        )

        assertNull(holder.content.selectionWatcher)
    }

    @Test
    fun `title holder should be in edit mode`() {

        // Setup

        val title = BlockView.Title.Document(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            isFocused = false
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Document)

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = holder.content.isTextSelectable
        )
    }

    @Test
    fun `title holder should enter read mode after being in edit mode`() {

        // Setup

        val title = BlockView.Title.Document(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            isFocused = false
        )

        val updated = title.copy(
            mode = BlockView.Mode.READ
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Document)

        // Testing

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = holder.content.isTextSelectable
        )

        holder.processPayloads(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(READ_WRITE_MODE_CHANGED)
                )
            )
        )

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = holder.content.isTextSelectable
        )
    }

    @Test
    fun `header holders should be in read mode`() {

        // Setup

        val h1 = BlockView.Text.Header.One(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.READ,
            indent = 0
        )

        val h2 = BlockView.Text.Header.Two(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.READ,
            indent = 0
        )

        val h3 = BlockView.Text.Header.Three(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.READ,
            indent = 0
        )

        val views: List<BlockView> = listOf(h1, h2, h3)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val h1Holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_ONE)
        val h2Holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_TWO)
        val h3Holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_THREE)

        // Testing

        adapter.onBindViewHolder(h1Holder, 0)
        adapter.onBindViewHolder(h2Holder, 1)
        adapter.onBindViewHolder(h3Holder, 2)

        check(h1Holder is HeaderOne)
        check(h2Holder is HeaderTwo)
        check(h3Holder is HeaderThree)

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = h1Holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = h1Holder.content.isTextSelectable
        )

        assertNull(h1Holder.content.selectionWatcher)

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = h2Holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = h2Holder.content.isTextSelectable
        )

        assertNull(h2Holder.content.selectionWatcher)

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = h3Holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = h3Holder.content.isTextSelectable
        )

        assertNull(h3Holder.content.selectionWatcher)
    }

    @Test
    fun `headerHolders holder should have its content selected or not based on state`() {

        // Setup

        val h1Selected = BlockView.Text.Header.One(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.READ,
            isSelected = true,
            indent = 0
        )

        val h1Notselected = BlockView.Text.Header.One(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.READ,
            isSelected = false,
            indent = 0
        )

        val h2Selected = BlockView.Text.Header.Two(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.READ,
            isSelected = true,
            indent = 0
        )

        val h2Notselected = BlockView.Text.Header.Two(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.READ,
            isSelected = false,
            indent = 0
        )

        val h3Selected = BlockView.Text.Header.Three(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.READ,
            isSelected = true,
            indent = 0
        )

        val h3Notselected = BlockView.Text.Header.Three(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.READ,
            isSelected = false,
            indent = 0
        )

        val views: List<BlockView> = listOf(
            h1Selected,
            h1Notselected,
            h2Selected,
            h2Notselected,
            h3Selected,
            h3Notselected
        )

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val h1SelectedHolder =
            adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_ONE)
        val h1NotSelectedHolder =
            adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_ONE)
        val h2SelectedHolder =
            adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_TWO)
        val h2NotSelectedHolder =
            adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_TWO)
        val h3SelectedHolder =
            adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_THREE)
        val h3NotSelectedHolder =
            adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_THREE)

        // Testing

        adapter.onBindViewHolder(h1SelectedHolder, 0)
        adapter.onBindViewHolder(h1NotSelectedHolder, 1)
        adapter.onBindViewHolder(h2SelectedHolder, 2)
        adapter.onBindViewHolder(h2NotSelectedHolder, 3)
        adapter.onBindViewHolder(h3SelectedHolder, 4)
        adapter.onBindViewHolder(h3NotSelectedHolder, 5)

        check(h1SelectedHolder is HeaderOne)
        check(h1NotSelectedHolder is HeaderOne)
        check(h2SelectedHolder is HeaderTwo)
        check(h2NotSelectedHolder is HeaderTwo)
        check(h3SelectedHolder is HeaderThree)
        check(h3NotSelectedHolder is HeaderThree)

        assertTrue { h1SelectedHolder.content.isSelected }
        assertFalse { h1NotSelectedHolder.content.isSelected }
        assertTrue { h2SelectedHolder.content.isSelected }
        assertFalse { h2NotSelectedHolder.content.isSelected }
        assertTrue { h3SelectedHolder.content.isSelected }
        assertFalse { h3NotSelectedHolder.content.isSelected }
    }

    @Test
    fun `header holders should be in edit mode`() {

        // Setup

        val h1 = BlockView.Text.Header.One(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            indent = 0
        )

        val h2 = BlockView.Text.Header.Two(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            indent = 0
        )

        val h3 = BlockView.Text.Header.Three(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            indent = 0
        )

        val views: List<BlockView> = listOf(h1, h2, h3)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val h1Holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_ONE)
        val h2Holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_TWO)
        val h3Holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_THREE)

        // Testing

        adapter.onBindViewHolder(h1Holder, 0)
        adapter.onBindViewHolder(h2Holder, 1)
        adapter.onBindViewHolder(h3Holder, 2)

        check(h1Holder is HeaderOne)
        check(h2Holder is HeaderTwo)
        check(h3Holder is HeaderThree)

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = h1Holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = h1Holder.content.isTextSelectable
        )

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = h2Holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = h2Holder.content.isTextSelectable
        )

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = h3Holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = h3Holder.content.isTextSelectable
        )
    }

    @Test
    fun `header holders should enter read mode after being in edit mode`() {

        // Setup

        val h1 = BlockView.Text.Header.One(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            indent = 0
        )

        val h2 = BlockView.Text.Header.Two(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            indent = 0
        )

        val h3 = BlockView.Text.Header.Three(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            indent = 0
        )

        val h1Updated = h1.copy(
            mode = BlockView.Mode.READ
        )

        val h2Updated = h2.copy(
            mode = BlockView.Mode.READ
        )

        val h3Updated = h3.copy(
            mode = BlockView.Mode.READ
        )

        val views: List<BlockView> = listOf(h1, h2, h3)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val h1Holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_ONE)
        val h2Holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_TWO)
        val h3Holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_THREE)

        adapter.onBindViewHolder(h1Holder, 0)
        adapter.onBindViewHolder(h2Holder, 1)
        adapter.onBindViewHolder(h3Holder, 2)

        check(h1Holder is HeaderOne)
        check(h2Holder is HeaderTwo)
        check(h3Holder is HeaderThree)

        // Testing

        // H1

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = h1Holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = h1Holder.content.isTextSelectable
        )

        h1Holder.processChangePayload(
            item = h1Updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(READ_WRITE_MODE_CHANGED)
                )
            ),
            onSelectionChanged = { _, _ -> },
            onTextChanged = {},
            clicked = {}
        )

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = h1Holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = h1Holder.content.isTextSelectable
        )

        // H2

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = h2Holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = h2Holder.content.isTextSelectable
        )

        h2Holder.processChangePayload(
            item = h2Updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(READ_WRITE_MODE_CHANGED)
                )
            ),
            onSelectionChanged = { _, _ -> },
            onTextChanged = {},
            clicked = {}
        )

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = h2Holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = h2Holder.content.isTextSelectable
        )

        // H3

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = h3Holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = h3Holder.content.isTextSelectable
        )

        h3Holder.processChangePayload(
            item = h3Updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(READ_WRITE_MODE_CHANGED)
                )
            ),
            onSelectionChanged = { _, _ -> },
            onTextChanged = {},
            clicked = {}
        )

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = h3Holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = h3Holder.content.isTextSelectable
        )
    }

    @Test
    fun `highlight holder should be in read mode`() {

        // Setup

        val highlight = BlockView.Text.Highlight(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.READ,
            indent = 0,
            color = null,
            backgroundColor = null
        )

        val views = listOf(highlight)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HIGHLIGHT)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Highlight)

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = holder.content.isTextSelectable
        )

        assertNull(holder.content.selectionWatcher)
    }

    @Test
    fun `should update highlight holder with new text`() {

        // Setup

        val highlight = BlockView.Text.Highlight(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            color = null,
            backgroundColor = null
        )

        val updated = highlight.copy(
            text = MockDataFactory.randomString()
        )

        val views = listOf(highlight)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HIGHLIGHT)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Highlight)

        // Testing

        assertEquals(
            expected = highlight.text,
            actual = holder.content.text.toString()
        )

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(TEXT_CHANGED)
                )
            ),
            onSelectionChanged = { _, _ -> },
            onTextChanged = {},
            clicked = {}
        )

        assertEquals(
            expected = updated.text,
            actual = holder.content.text.toString()
        )
    }

    @Test
    fun `highlight holder should be in edit mode`() {

        // Setup

        val highlight = BlockView.Text.Highlight(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            color = null,
            backgroundColor = null
        )

        val views = listOf(highlight)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HIGHLIGHT)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Highlight)

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = holder.content.isTextSelectable
        )
    }

    @Test
    fun `highlight holder should enter read mode after being in edit mode`() {

        // Setup

        val highlight = BlockView.Text.Highlight(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            color = null,
            backgroundColor = null
        )

        val updated = highlight.copy(
            mode = BlockView.Mode.READ
        )

        val views = listOf(highlight)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HIGHLIGHT)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Highlight)

        // Testing

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = holder.content.isTextSelectable
        )

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(READ_WRITE_MODE_CHANGED)
                )
            ),
            onSelectionChanged = { _, _ -> },
            onTextChanged = {},
            clicked = {}
        )

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = holder.content.isTextSelectable
        )

        assertNull(holder.content.selectionWatcher)
    }

    @Test
    fun `bullet holder should be in read mode`() {

        // Setup

        val bullet = BlockView.Text.Bulleted(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.READ,
            indent = 0
        )

        val views = listOf(bullet)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_BULLET)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Bulleted)

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = holder.content.isTextSelectable
        )

        assertNull(holder.content.selectionWatcher)
    }

    @Test
    fun `bullet holder should be in edit mode`() {

        // Setup

        val bullet = BlockView.Text.Bulleted(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(bullet)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_BULLET)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Bulleted)

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = holder.content.isTextSelectable
        )
    }

    @Test
    fun `bullet holder should enter read mode after being in edit mode`() {

        // Setup

        val bullet = BlockView.Text.Bulleted(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            indent = 0
        )

        val updated = bullet.copy(
            mode = BlockView.Mode.READ
        )

        val views = listOf(bullet)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_BULLET)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Bulleted)

        // Testing

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = holder.content.isTextSelectable
        )

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(READ_WRITE_MODE_CHANGED)
                )
            ),
            onSelectionChanged = { _, _ -> },
            onTextChanged = {},
            clicked = {}
        )

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = holder.content.isTextSelectable
        )

        assertNull(holder.content.selectionWatcher)
    }

    @Test
    fun `checkbox holder should be in read mode`() {

        // Setup

        val checkbox = BlockView.Text.Checkbox(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.READ,
            indent = 0
        )

        val views = listOf(checkbox)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_CHECKBOX)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Checkbox)

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = holder.content.isTextSelectable
        )

        assertNull(holder.content.selectionWatcher)
    }

    @Test
    fun `checkbox holder should be in edit mode`() {

        // Setup

        val checkbox = BlockView.Text.Checkbox(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            indent = 0
        )

        val views = listOf(checkbox)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_CHECKBOX)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Checkbox)

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = holder.content.isTextSelectable
        )
    }

    @Test
    fun `checkbox holder should enter read mode after being in edit mode`() {

        // Setup

        val checkbox = BlockView.Text.Checkbox(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            indent = 0
        )

        val updated = checkbox.copy(
            mode = BlockView.Mode.READ
        )

        val views = listOf(checkbox)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_CHECKBOX)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Checkbox)

        // Testing

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = holder.content.isTextSelectable
        )

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(READ_WRITE_MODE_CHANGED)
                )
            ),
            onSelectionChanged = { _, _ -> },
            onTextChanged = {},
            clicked = {}
        )

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = holder.content.isTextSelectable
        )

        assertNull(holder.content.selectionWatcher)
    }

    @Test
    fun `numbered holder should be in read mode`() {

        // Setup

        val numbered = BlockView.Text.Numbered(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.READ,
            indent = 0,
            number = MockDataFactory.randomInt()
        )

        val views = listOf(numbered)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_NUMBERED)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Numbered)

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = holder.content.isTextSelectable
        )

        assertNull(holder.content.selectionWatcher)
    }

    @Test
    fun `numbered holder should be in edit mode`() {

        // Setup

        val numbered = BlockView.Text.Numbered(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            indent = 0,
            number = MockDataFactory.randomInt()
        )

        val views = listOf(numbered)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_NUMBERED)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Numbered)

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = holder.content.isTextSelectable
        )
    }

    @Test
    fun `numbered holder should enter read mode after being in edit mode`() {

        // Setup

        val numbered = BlockView.Text.Numbered(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            indent = MockDataFactory.randomInt(),
            number = MockDataFactory.randomInt()
        )

        val updated = numbered.copy(
            mode = BlockView.Mode.READ
        )

        val views = listOf(numbered)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_NUMBERED)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Numbered)

        // Testing

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = holder.content.isTextSelectable
        )

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(READ_WRITE_MODE_CHANGED)
                )
            ),
            onSelectionChanged = { _, _ -> },
            onTextChanged = {},
            clicked = {}
        )

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = holder.content.isTextSelectable
        )

        assertNull(holder.content.selectionWatcher)
    }

    @Test
    fun `toggle holder should be in read mode`() {

        // Setup

        val toggle = BlockView.Text.Toggle(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.READ,
            indent = 0,
            isFocused = false,
            marks = emptyList()
        )

        val views = listOf(toggle)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TOGGLE)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Toggle)

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = holder.content.isTextSelectable
        )

        assertNull(holder.content.selectionWatcher)
    }

    @Test
    fun `toggle holder should be in edit mode`() {

        // Setup

        val toggle = BlockView.Text.Toggle(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            indent = 0,
            backgroundColor = null,
            color = null,
            isFocused = false,
            marks = emptyList()
        )

        val views = listOf(toggle)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TOGGLE)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is Toggle)

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = holder.content.isTextSelectable
        )
    }

    @Test
    fun `toggle holder should enter read mode after being in edit mode`() {

        // Setup

        val toggle = BlockView.Text.Toggle(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            indent = MockDataFactory.randomInt(),
            isFocused = false,
            marks = emptyList()
        )

        val updated = toggle.copy(
            mode = BlockView.Mode.READ
        )

        val views = listOf(toggle)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TOGGLE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Toggle)

        // Testing

        assertEquals(
            expected = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = true,
            actual = holder.content.isTextSelectable
        )

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(READ_WRITE_MODE_CHANGED)
                )
            ),
            onSelectionChanged = { _, _ -> },
            onTextChanged = {},
            clicked = {}
        )

        assertEquals(
            expected = InputType.TYPE_NULL,
            actual = holder.content.inputType
        )

        assertEquals(
            expected = false,
            actual = holder.content.isTextSelectable
        )

        assertNull(holder.content.selectionWatcher)
    }

    @Test
    fun `should select file based on state`() {

        // Setup

        val views = listOf(
            BlockView.Media.File(
                id = MockDataFactory.randomString(),
                hash = MockDataFactory.randomString(),
                indent = MockDataFactory.randomInt(),
                mime = MockDataFactory.randomString(),
                size = MockDataFactory.randomLong(),
                name = MockDataFactory.randomString(),
                url = MockDataFactory.randomString(),
                isSelected = false
            ),
            BlockView.Media.File(
                id = MockDataFactory.randomString(),
                hash = MockDataFactory.randomString(),
                indent = MockDataFactory.randomInt(),
                mime = MockDataFactory.randomString(),
                size = MockDataFactory.randomLong(),
                name = MockDataFactory.randomString(),
                url = MockDataFactory.randomString(),
                isSelected = true
            )
        )

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val firstHolder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_FILE)
        val secondHolder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_FILE)
        adapter.onBindViewHolder(firstHolder, 0)
        adapter.onBindViewHolder(secondHolder, 1)

        check(firstHolder is File)
        check(secondHolder is File)

        // Testing

        assertTrue { !firstHolder.itemView.isSelected }
        assertTrue { secondHolder.itemView.isSelected }
    }

    @Test
    fun `should update selected state for file`() {

        // Setup

        val file = BlockView.Media.File(
            id = MockDataFactory.randomString(),
            hash = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt(),
            mime = MockDataFactory.randomString(),
            size = MockDataFactory.randomLong(),
            name = MockDataFactory.randomString(),
            url = MockDataFactory.randomString(),
            isSelected = false
        )

        val updated = file.copy(isSelected = true)

        val views = listOf(file)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_FILE)
        adapter.onBindViewHolder(holder, 0)

        check(holder is File)

        // Testing

        assertTrue { !holder.itemView.isSelected }

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(SELECTION_CHANGED)
                )
            ),
            item = updated
        )

        assertTrue { holder.itemView.isSelected }
    }

    @Test
    fun `should update selected state for page`() {

        // Setup

        val file = BlockView.Page(
            id = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt(),
            isSelected = false,
            emoji = null,
            image = null
        )

        val updated = file.copy(isSelected = true)

        val views = listOf(file)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PAGE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Page)

        // Testing

        assertTrue { !holder.itemView.isSelected }

        adapter.updateWithDiffUtil(
            items = listOf(updated)
        )

        val changes: MutableList<Any> = mutableListOf(
            BlockViewDiffUtil.Payload(changes = listOf(SELECTION_CHANGED))
        )

        adapter.onBindViewHolder(holder, 0, changes)

        assertTrue { holder.itemView.isSelected }
    }


//    Turned off the test, because Robolectric is not working with MateralCardView
//    @Test
//    fun `should update selected state for bookmark`() {
//
//        // Setup
//
//        val bookmark = BlockView.Bookmark.View(
//            id = MockDataFactory.randomString(),
//            description = MockDataFactory.randomString(),
//            faviconUrl = MockDataFactory.randomString(),
//            imageUrl = MockDataFactory.randomString(),
//            indent = MockDataFactory.randomInt(),
//            title = MockDataFactory.randomString(),
//            url = MockDataFactory.randomString(),
//            isSelected = false
//        )
//
//        val updated = bookmark.copy(isSelected = true)
//
//        val views = listOf(bookmark)
//
//        val adapter = buildAdapter(views)
//
//        val recycler = RecyclerView(context).apply {
//            layoutManager = LinearLayoutManager(context)
//            this.adapter = adapter
//        }
//
//        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_BOOKMARK)
//
//        adapter.onBindViewHolder(holder, 0)
//
//        check(holder is BlockViewHolder.Bookmark)
//
//        // Testing
//
//        assertTrue { !holder.itemView.isSelected }
//
//        adapter.updateWithDiffUtil(
//            items = listOf(updated)
//        )
//
//        val changes: MutableList<Any> = mutableListOf(
//            BlockViewDiffUtil.Payload(changes = listOf(SELECTION_CHANGED))
//        )
//
//        adapter.onBindViewHolder(holder, 0, changes)
//
//        assertTrue { holder.itemView.isSelected }
//    }

    private fun buildAdapter(
        views: List<BlockView>,
        onSplitLineEnterClicked: (String, Editable, IntRange) -> Unit = { _, _, _ -> },
        onFocusChanged: (String, Boolean) -> Unit = { _, _ -> },
        onTitleTextChanged: (Editable) -> Unit = {},
        onTextChanged: (String, Editable) -> Unit = { _, _ -> }
    ): BlockAdapter {
        return BlockAdapter(
            blocks = views,
            onNonEmptyBlockBackspaceClicked = { _, _ -> },
            onEmptyBlockBackspaceClicked = {},
            onSplitLineEnterClicked = onSplitLineEnterClicked,
            onTextChanged = onTextChanged,
            onCheckboxClicked = {},
            onFocusChanged = onFocusChanged,
            onSelectionChanged = { _, _ -> },
            onTextInputClicked = {},
            onPageIconClicked = {},
            onProfileIconClicked = {},
            onTogglePlaceholderClicked = {},
            onToggleClicked = {},
            onTextBlockTextChanged = {},
            onTitleTextChanged = onTitleTextChanged,
            onContextMenuStyleClick = {},
            onTitleTextInputClicked = {},
            onClickListener = {},
            clipboardInterceptor = clipboardInterceptor,
            onMentionEvent = {},
            onBackPressedCallback = { false }
        )
    }
}