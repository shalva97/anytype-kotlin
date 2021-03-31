package com.anytypeio.anytype.core_ui.features.dataview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.sets.model.ViewerTabView
import kotlinx.android.synthetic.main.item_dv_viewer_tab.view.*

@Deprecated("Outdated.")
class ViewerTabItemAdapter(
    private var tabs: List<ViewerTabView> = emptyList(),
    private val onViewerTabClicked: (String) -> Unit,
    private val onAddNewViewerClicked: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int = tabs.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position >= tabs.size)
            PLUS_TYPE
        else
            TAB_TYPE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TabHolder) holder.bind(tabs[position])
    }

    fun update(update: List<ViewerTabView>) {
        tabs = update
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TAB_TYPE -> {
                val view = inflater.inflate(R.layout.item_dv_viewer_tab, parent, false)
                TabHolder(view).apply {
                    itemView.setOnClickListener {
                        onViewerTabClicked(tabs[bindingAdapterPosition].id)
                    }
                }
            }
            PLUS_TYPE -> {
                val view = inflater.inflate(R.layout.item_dv_viewer_tab_plus, parent, false)
                NewTabHolder(view).apply {
                    itemView.setOnClickListener {
                        onAddNewViewerClicked()
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    class TabHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val title = itemView.tvTabTitle

        fun bind(item: ViewerTabView) {
            title.text = item.name
            title.isActivated = item.isActive
        }
    }

    class NewTabHolder(view: View) : RecyclerView.ViewHolder(view)

    companion object {
        const val TAB_TYPE = 0
        const val PLUS_TYPE = 1
    }
}
