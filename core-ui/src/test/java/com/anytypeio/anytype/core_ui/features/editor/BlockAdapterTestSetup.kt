package com.anytypeio.anytype.core_ui.features.editor

import android.content.Context
import android.text.Editable
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_ui.features.page.BlockAdapter
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.tools.ClipboardInterceptor
import com.nhaarman.mockitokotlin2.mock

open class BlockAdapterTestSetup {

    val clipboardInterceptor: ClipboardInterceptor = mock()

    val context: Context = ApplicationProvider.getApplicationContext()

    fun buildAdapter(
        views: List<BlockView>,
        onFocusChanged: (String, Boolean) -> Unit = { _, _ -> },
        onTitleTextChanged: (Editable) -> Unit = {},
        onTextBlockTextChanged: (BlockView.Text) -> Unit = {},
        onTextInputClicked: (String) -> Unit = {},
        onSplitLineEnterClicked: (String, Editable, IntRange) -> Unit = { _, _, _ ->},
        onTextChanged: (String, Editable) -> Unit = { _, _ -> },
        onToggleClicked: (String) -> Unit = {},
        onEmptyBlockBackspaceClicked: (String) -> Unit = {},
        onNonEmptyBlockBackspaceClicked: (String, Editable) -> Unit = { _, _ -> },
        onCheckboxClicked: (BlockView.Text.Checkbox) -> Unit = {}
    ): BlockAdapter {
        return BlockAdapter(
            blocks = views,
            onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
            onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
            onSplitLineEnterClicked = onSplitLineEnterClicked,
            onTextChanged = onTextChanged,
            onCheckboxClicked = onCheckboxClicked,
            onFocusChanged = onFocusChanged,
            onSelectionChanged = { _, _ -> },
            onTextInputClicked = onTextInputClicked,
            onPageIconClicked = {},
            onProfileIconClicked = {},
            onTogglePlaceholderClicked = {},
            onToggleClicked = onToggleClicked,
            onTextBlockTextChanged = onTextBlockTextChanged,
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