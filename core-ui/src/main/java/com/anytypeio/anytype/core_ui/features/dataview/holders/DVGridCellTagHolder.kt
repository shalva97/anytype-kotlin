package com.anytypeio.anytype.core_ui.features.dataview.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.setDrawableColor
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.page.editor.ThemeColor
import com.anytypeio.anytype.presentation.sets.model.CellView
import kotlinx.android.synthetic.main.item_viewer_grid_cell_tag.view.*

class DVGridCellTagHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(cell: CellView.Tag) {
        for (i in 0..MAX_VISIBLE_TAGS_INDEX) getViewByIndex(i)?.gone()
        cell.tags.forEachIndexed { index, tagView ->
            when (index) {
                in 0..MAX_VISIBLE_TAGS_INDEX -> {
                    getViewByIndex(index)?.let { view ->
                        view.visible()
                        view.text = tagView.tag
                        val color = ThemeColor.values().find { v -> v.title == tagView.color }
                        if (color != null) {
                            view.background.setDrawableColor(color.background)
                            view.setTextColor(color.text)
                        } else {
                            view.background.setDrawableColor(itemView.context.color(R.color.default_filter_tag_background_color))
                            view.setTextColor(itemView.context.color(R.color.default_filter_tag_text_color))
                        }
                    }
                }
            }
        }
    }

    private fun getViewByIndex(index: Int): TextView? = when (index) {
        0 -> itemView.tag0
        1 -> itemView.tag1
        2 -> itemView.tag2
        3 -> itemView.tag3
        4 -> itemView.tag4
        else -> null
    }

    companion object {
        const val MAX_VISIBLE_TAGS_INDEX = 4
    }
}