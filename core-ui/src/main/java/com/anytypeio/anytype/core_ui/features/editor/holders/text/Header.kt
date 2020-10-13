package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.text.Editable
import android.view.View
import androidx.core.view.updatePadding
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.page.*
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen

abstract class Header(
    view: View
) : Text(view), TextBlockHolder, BlockViewHolder.IndentableHolder {

    abstract val header: TextInputWidget

    fun bind(
        block: BlockView.Text.Header,
        onTextBlockTextChanged: (BlockView.Text) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        clicked: (ListenerType) -> Unit,
        onMentionEvent: (MentionEvent) -> Unit,
        onSplitLineEnterClicked: (String, Editable, IntRange) -> Unit,
        onEmptyBlockBackspaceClicked: (String) -> Unit,
        onNonEmptyBlockBackspaceClicked: (String, Editable) -> Unit,
        onTextInputClicked: (String) -> Unit,
        onBackPressedCallback: () -> Boolean
    ) = super.bind(
        item = block,
        onSelectionChanged = onSelectionChanged,
        onTextChanged = { _, editable ->
            block.apply {
                text = editable.toString()
                marks = editable.marks()
            }
            onTextBlockTextChanged(block)
        },
        onFocusChanged = onFocusChanged,
        clicked = clicked,
        onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
        onSplitLineEnterClicked = onSplitLineEnterClicked,
        onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
        onTextInputClicked = onTextInputClicked,
        onBackPressedCallback = onBackPressedCallback
    ).also {
        setupMentionWatcher(onMentionEvent)
    }

    override fun indentize(item: BlockView.Indentable) {
        header.updatePadding(
            left = dimen(R.dimen.default_document_content_padding_start) + item.indent * dimen(R.dimen.indent)
        )
    }
}