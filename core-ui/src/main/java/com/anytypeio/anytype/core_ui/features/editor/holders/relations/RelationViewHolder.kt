package com.anytypeio.anytype.core_ui.features.editor.holders.relations

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.features.page.BlockViewHolder
import com.anytypeio.anytype.core_ui.widgets.GridCellFileItem
import com.anytypeio.anytype.core_ui.widgets.RelationObjectItem
import com.anytypeio.anytype.core_ui.widgets.text.TagWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.page.editor.ThemeColor
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import kotlinx.android.synthetic.main.item_document_relation_file.view.*
import kotlinx.android.synthetic.main.item_document_relation_object.view.*
import kotlinx.android.synthetic.main.item_document_relation_tag.view.*
import timber.log.Timber

sealed class RelationViewHolder(view: View) : BlockViewHolder(view), BlockViewHolder.IndentableHolder {

    fun setBackgroundColor(color: String? = null) {
        Timber.d("Setting background color: $color")
        if (color != null) {
            val value = ThemeColor.values().find { value -> value.title == color }
            if (value != null)
                itemView.setBackgroundColor(value.background)
            else
                Timber.e("Could not find value for background color: $color")
        } else {
            itemView.background = null
        }
    }

    class Placeholder(view: View) : RelationViewHolder(view) {

        val icon: View get() = itemView.findViewById(R.id.relationIcon)

        fun bind(item: BlockView.Relation.Placeholder) = with(itemView) {
            findViewById<LinearLayout>(R.id.placeholderContainer).isSelected = item.isSelected
            indentize(item)
        }
        override fun indentize(item: BlockView.Indentable) {
            val indent = dimen(R.dimen.indent)
            icon.updateLayoutParams<LinearLayout.LayoutParams> {
                this.marginStart = item.indent * indent
            }
        }
    }

    class Default(view: View) : RelationViewHolder(view) {
        fun bind(item: DocumentRelationView) = with(itemView) {
            findViewById<TextView>(R.id.tvRelationTitle).text = item.name
            findViewById<TextView>(R.id.tvRelationValue).text = item.value
        }

        override fun indentize(item: BlockView.Indentable) {
            val title = itemView.findViewById<TextView>(R.id.tvRelationTitle)
            val indent = dimen(R.dimen.indent) * item.indent
            title.updatePadding(left = indent)
        }
    }

    class Status(view: View) : RelationViewHolder(view) {
        fun bind(item: DocumentRelationView.Status) {
            itemView.findViewById<TextView>(R.id.tvRelationTitle).text = item.name
            itemView.findViewById<TextView>(R.id.tvRelationValue).apply {
                if (item.status.isNotEmpty()) {
                    val status = item.status.first()
                    text = status.status
                    val color = ThemeColor.values().find { v -> v.title == status.color }
                    if (color != null) {
                        setTextColor(color.text)
                    } else {
                        setTextColor(context.color(R.color.default_filter_status_text_color))
                    }
                } else {
                    text = null
                }
            }
        }

        override fun indentize(item: BlockView.Indentable) {
            val title = itemView.findViewById<TextView>(R.id.tvRelationTitle)
            val indent = dimen(R.dimen.indent) * item.indent
            title.updatePadding(left = indent)
        }
    }

    class Tags(view: View) : RelationViewHolder(view) {

        fun bind(item: DocumentRelationView.Tags) = with(itemView) {
            findViewById<TextView>(R.id.tvRelationTitle).text = item.name
            for (i in 0..MAX_VISIBLE_TAGS_INDEX) getViewByIndex(i)?.gone()
            item.tags.forEachIndexed { index, tagView ->
                when (index) {
                    in 0..MAX_VISIBLE_TAGS_INDEX -> {
                        getViewByIndex(index)?.let { view ->
                            view.setup(tagView.tag, tagView.color)
                        }
                    }
                }
            }
        }

        private fun getViewByIndex(index: Int): TagWidget? = when (index) {
            0 -> itemView.tag0
            1 -> itemView.tag1
            2 -> itemView.tag2
            3 -> itemView.tag3
            4 -> itemView.tag4
            5 -> itemView.tag5
            else -> null
        }

        companion object {
            const val MAX_VISIBLE_TAGS_INDEX = 5
        }

        override fun indentize(item: BlockView.Indentable) {
            val title = itemView.findViewById<TextView>(R.id.tvRelationTitle)
            val indent = dimen(R.dimen.indent) * item.indent
            title.updatePadding(left = indent)
        }
    }

    class Object(view: View) : RelationViewHolder(view) {

        fun bind(item: DocumentRelationView.Object) {
            itemView.findViewById<TextView>(R.id.tvRelationTitle).text = item.name
            for (i in 0..MAX_VISIBLE_OBJECTS_INDEX) getViewByIndex(i)?.gone()
            item.objects.forEachIndexed { index, objectView ->
                when (index) {
                    in 0..MAX_VISIBLE_OBJECTS_INDEX -> {
                        getViewByIndex(index)?.let { view ->
                            view.visible()
                            view.setup(
                                name = objectView.name,
                                emoji = objectView.emoji,
                                image = objectView.image
                            )
                        }
                    }
                }
            }
        }

        override fun indentize(item: BlockView.Indentable) {
            val title = itemView.findViewById<TextView>(R.id.tvRelationTitle)
            val indent = dimen(R.dimen.indent) * item.indent
            title.updatePadding(left = indent)
        }

        private fun getViewByIndex(index: Int): RelationObjectItem? = when (index) {
            0 -> itemView.obj0
            1 -> itemView.obj1
            2 -> itemView.obj2
            3 -> itemView.obj3
            else -> null
        }

        companion object {
            const val MAX_VISIBLE_OBJECTS_INDEX = 3
        }
    }

    class File(view: View) : RelationViewHolder(view) {

        fun bind(item: DocumentRelationView.File) = with(itemView) {
            findViewById<TextView>(R.id.tvRelationTitle).text = item.name
            item.files.forEachIndexed { index, fileView ->
                when (index) {
                    in 0..MAX_VISIBLE_FILES_INDEX -> {
                        getViewByIndex(index)?.let { view ->
                            view.visible()
                            view.setup(name = fileView.name, mime = fileView.mime)
                        }
                    }
                }
            }
        }

        private fun getViewByIndex(index: Int): GridCellFileItem? = when (index) {
            0 -> itemView.file0
            1 -> itemView.file1
            2 -> itemView.file2
            else -> null
        }

        companion object {
            const val MAX_VISIBLE_FILES_INDEX = 2
        }

        override fun indentize(item: BlockView.Indentable) {
            val title = itemView.findViewById<TextView>(R.id.tvRelationTitle)
            val indent = dimen(R.dimen.indent) * item.indent
            title.updatePadding(left = indent)
        }
    }
}