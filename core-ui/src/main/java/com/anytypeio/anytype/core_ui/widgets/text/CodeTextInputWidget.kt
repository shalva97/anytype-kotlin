package com.anytypeio.anytype.core_ui.widgets.text

import android.content.Context
import android.text.InputType.*
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.anytypeio.anytype.core_ui.tools.DefaultTextWatcher
import timber.log.Timber

class CodeTextInputWidget : AppCompatEditText {

    private val watchers: MutableList<TextWatcher> = mutableListOf()

    var selectionWatcher: ((IntRange) -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup()
    }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        setup()
    }

    private fun setup() {
        enableEditMode()
    }

    fun enableEditMode() {
        setRawInputType(TYPE_CLASS_TEXT or TYPE_TEXT_FLAG_MULTI_LINE or TYPE_TEXT_FLAG_NO_SUGGESTIONS)
        setHorizontallyScrolling(false)
        maxLines = Integer.MAX_VALUE
        setTextIsSelectable(true)
    }

    fun enableReadMode() {
        setRawInputType(TYPE_NULL)
        maxLines = Integer.MAX_VALUE
        setHorizontallyScrolling(false)
        setTextIsSelectable(false)
    }

    override fun addTextChangedListener(watcher: TextWatcher) {
        watchers.add(watcher)
        super.addTextChangedListener(watcher)
    }

    override fun removeTextChangedListener(watcher: TextWatcher) {
        watchers.remove(watcher)
        super.removeTextChangedListener(watcher)
    }

    fun clearTextWatchers() {
        watchers.forEach { super.removeTextChangedListener(it) }
        watchers.clear()
    }

    fun pauseTextWatchers(block: () -> Unit) = synchronized(this) {
        lockTextWatchers()
        block()
        unlockTextWatchers()
    }

    private fun lockTextWatchers() {
        watchers.forEach { watcher ->
            if (watcher is DefaultTextWatcher) watcher.lock()
        }
    }

    private fun unlockTextWatchers() {
        watchers.forEach { watcher ->
            if (watcher is DefaultTextWatcher) watcher.unlock()
        }
    }

    /**
     * Send selection event only for blocks in focus state
     */
    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        if (isFocused) {
            Timber.d("New selection: $selStart - $selEnd")
            selectionWatcher?.invoke(selStart..selEnd)
        }
        super.onSelectionChanged(selStart, selEnd)
    }
}