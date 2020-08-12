package com.agileburo.anytype.core_ui.features.editor

import android.content.Context
import android.text.Editable
import androidx.test.core.app.ApplicationProvider
import com.agileburo.anytype.core_ui.features.page.BlockAdapter
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.tools.ClipboardInterceptor
import com.nhaarman.mockitokotlin2.mock

open class BlockAdapterTestSetup {

    val clipboardInterceptor: ClipboardInterceptor = mock()

    val context: Context = ApplicationProvider.getApplicationContext()

    fun buildAdapter(
        views: List<BlockView>,
        onFocusChanged: (String, Boolean) -> Unit = { _, _ -> },
        onTitleTextChanged: (Editable) -> Unit = {},
        onEndLineEnterTitleClicked: (Editable) -> Unit = {},
        onTextChanged: (String, Editable) -> Unit = { _, _ -> }
    ): BlockAdapter {
        return BlockAdapter(
            blocks = views,
            onNonEmptyBlockBackspaceClicked = { _, _ -> },
            onEmptyBlockBackspaceClicked = {},
            onSplitLineEnterClicked = { _, _, _ -> },
            onEndLineEnterClicked = { _, _ -> },
            onTextChanged = onTextChanged,
            onCheckboxClicked = {},
            onFocusChanged = onFocusChanged,
            onSelectionChanged = { _, _ -> },
            onFooterClicked = {},
            onTextInputClicked = {},
            onPageIconClicked = {},
            onProfileIconClicked = {},
            onTogglePlaceholderClicked = {},
            onToggleClicked = {},
            onParagraphTextChanged = { _, _ -> },
            onTitleTextChanged = onTitleTextChanged,
            onEndLineEnterTitleClicked = onEndLineEnterTitleClicked,
            onMarkupActionClicked = { _, _ -> },
            onTitleTextInputClicked = {},
            onClickListener = {},
            clipboardInterceptor = clipboardInterceptor,
            onMentionEvent = {}
        )
    }
}