package com.agileburo.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.reactive.clicks
import kotlinx.android.synthetic.main.widget_page_bottom_toolbar.view.*

class PageBottomToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    fun searchClicks() = btnSearch.clicks()
    fun navigationClicks() = btnNavigation.clicks()
    fun addPageClick() = btnAddDoc.clicks()

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_page_bottom_toolbar, this, true)
    }
}