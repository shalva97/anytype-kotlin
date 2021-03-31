package com.anytypeio.anytype.core_ui.features.dataview

import android.view.View
import android.view.ViewGroup
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.AbstractAdapter
import com.anytypeio.anytype.core_ui.common.AbstractViewHolder
import kotlinx.android.synthetic.main.item_viewer_title.view.*

class ViewerTitleAdapter(items: List<String> = listOf()) : AbstractAdapter<String>(items) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder<String> {
        return Holder(view = inflate(parent, R.layout.item_viewer_title))
    }

    class Holder(view: View) : AbstractViewHolder<String>(view) {
        override fun bind(item: String) {
            itemView.tvTitle.text = item
        }
    }
}