package com.anytypeio.anytype.core_ui.features.dataview.modals

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.sets.filter.FilterClick
import com.anytypeio.anytype.presentation.sets.model.FilterView
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_ADD
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_DATE
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_EMAIL
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_NUMBER
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_OBJECT
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_PHONE
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_STATUS
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_TAG
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_TEXT
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_TEXT_SHORT
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_URL
import kotlinx.android.synthetic.main.item_dv_viewer_filter_date.view.*
import kotlinx.android.synthetic.main.item_dv_viewer_filter_number.view.*
import kotlinx.android.synthetic.main.item_dv_viewer_filter_object.view.*
import kotlinx.android.synthetic.main.item_dv_viewer_filter_status.view.*
import kotlinx.android.synthetic.main.item_dv_viewer_filter_tag.view.*
import kotlinx.android.synthetic.main.item_dv_viewer_filter_text.view.*

class FilterByAdapter(
    private var items: List<FilterView> = listOf(),
    private val click: (FilterClick) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun update(newItems: List<FilterView>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            HOLDER_TEXT, HOLDER_TEXT_SHORT, HOLDER_URL, HOLDER_PHONE, HOLDER_EMAIL -> {
                val view = inflater.inflate(R.layout.item_dv_viewer_filter_text, parent, false)
                FilterTextViewHolder(view).apply {
                    itemView.iconRemoveText.setOnClickListener {
                        click(FilterClick.Remove(bindingAdapterPosition))
                    }
                    itemView.setOnClickListener {
                        click(FilterClick.Value(bindingAdapterPosition))
                    }
                }
            }
            HOLDER_NUMBER -> {
                val view =
                    inflater.inflate(R.layout.item_dv_viewer_filter_number, parent, false)
                FilterNumberViewHolder(view).apply {
                    itemView.iconRemoveNumber.setOnClickListener {
                        click(FilterClick.Remove(bindingAdapterPosition))
                    }
                    itemView.setOnClickListener {
                        click(FilterClick.Value(bindingAdapterPosition))
                    }
                }
            }
            HOLDER_STATUS -> {
                val view = inflater.inflate(R.layout.item_dv_viewer_filter_status, parent, false)
                FilterStatusViewHolder(view).apply {
                    itemView.iconRemoveStatus.setOnClickListener {
                        click(FilterClick.Remove(bindingAdapterPosition))
                    }
                    itemView.setOnClickListener {
                        click(FilterClick.Value(bindingAdapterPosition))
                    }
                }
            }
            HOLDER_TAG -> {
                val view = inflater.inflate(R.layout.item_dv_viewer_filter_tag, parent, false)
                FilterTagViewHolder(view).apply {
                    itemView.iconRemoveTag.setOnClickListener {
                        click(FilterClick.Remove(bindingAdapterPosition))
                    }
                    itemView.setOnClickListener {
                        click(FilterClick.Value(bindingAdapterPosition))
                    }
                }
            }
            HOLDER_ADD -> {
                val view = inflater.inflate(R.layout.item_dv_viewer_filter_add, parent, false)
                FilterAddViewHolder(view).apply {
                    itemView.setOnClickListener {
                        click(FilterClick.Value(bindingAdapterPosition))
                    }
                }
            }
            HOLDER_DATE -> {
                val views = inflater.inflate(R.layout.item_dv_viewer_filter_date, parent, false)
                FilterDateViewHolder(views).apply {
                    itemView.setOnClickListener {
                        click(FilterClick.Value(bindingAdapterPosition))
                    }
                    itemView.iconRemoveDate.setOnClickListener {
                        click(FilterClick.Remove(bindingAdapterPosition))
                    }
                }
            }
            HOLDER_OBJECT -> {
                val views = inflater.inflate(R.layout.item_dv_viewer_filter_object, parent, false)
                FilterObjectViewHolder(views).apply {
                    itemView.iconRemoveObject.setOnClickListener {
                        click(FilterClick.Remove(bindingAdapterPosition))
                    }
                    itemView.setOnClickListener {
                        click(FilterClick.Value(bindingAdapterPosition))
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FilterTextViewHolder -> {
                holder.bind(items[position] as FilterView.Expression)
            }
            is FilterNumberViewHolder -> {
                holder.bind(items[position] as FilterView.Expression.Number)
            }
            is FilterStatusViewHolder -> {
                holder.bind(items[position] as FilterView.Expression.Status)
            }
            is FilterTagViewHolder -> {
                holder.bind(items[position] as FilterView.Expression.Tag)
            }
            is FilterDateViewHolder -> {
                holder.bind(items[position] as FilterView.Expression.Date)
            }
            is FilterObjectViewHolder -> {
                holder.bind(items[position] as FilterView.Expression.Object)
            }
            is FilterAddViewHolder -> { }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is FilterView.Expression.Text -> HOLDER_TEXT
        is FilterView.Expression.Number -> HOLDER_NUMBER
        is FilterView.Expression.Date -> HOLDER_DATE
        is FilterView.Expression.Status -> HOLDER_STATUS
        is FilterView.Expression.Tag -> HOLDER_TAG
        is FilterView.Expression.Object -> HOLDER_OBJECT
        is FilterView.Expression.Email -> HOLDER_EMAIL
        is FilterView.Expression.Phone -> HOLDER_PHONE
        is FilterView.Expression.TextShort -> HOLDER_TEXT_SHORT
        is FilterView.Expression.Url -> HOLDER_URL
        FilterView.Add -> HOLDER_ADD
    }
}