package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.text.SpannableStringBuilder
import android.view.View
import android.widget.TextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.Markup
import com.anytypeio.anytype.core_ui.common.Span
import com.anytypeio.anytype.core_ui.features.page.*
import com.anytypeio.anytype.core_ui.widgets.text.EditorLongClickListener
import com.anytypeio.anytype.core_utils.ext.VALUE_ROUNDED
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.emojifier.Emojifier
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_block_page_archived.view.*
import java.lang.Exception

class PageArchive(view: View) : BlockViewHolder(view), BlockViewHolder.IndentableHolder,
    SupportNesting {

    private val untitled = itemView.resources.getString(R.string.untitled)
    private val archived = itemView.resources.getString(R.string.archived)
    private val icon = itemView.pageIcon
    private val emoji = itemView.linkEmoji
    private val image = itemView.linkImage
    private val title = itemView.pageTitle
    private val guideline = itemView.pageGuideline

    fun bind(
        item: BlockView.PageArchive,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)

        itemView.isSelected = item.isSelected

        title.enableReadMode()
        val text = if (item.text.isNullOrEmpty()) {
            SpannableStringBuilder("$untitled $archived").apply {
                setSpan(
                    Span.Keyboard(VALUE_ROUNDED),
                    untitled.length + 1,
                    untitled.length + 1 + archived.length,
                    Markup.DEFAULT_SPANNABLE_FLAG
                )
            }
        } else {
            SpannableStringBuilder("${item.text} $archived").apply {
                setSpan(
                    Span.Keyboard(VALUE_ROUNDED),
                    item.text!!.length + 1,
                    item.text!!.length + 1 + archived.length,
                    Markup.DEFAULT_SPANNABLE_FLAG
                )
            }
        }

        title.setText(text, TextView.BufferType.SPANNABLE)

        when {
            item.emoji != null -> {
                image.setImageDrawable(null)
                try {
                    Glide
                        .with(emoji)
                        .load(Emojifier.uri(item.emoji))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(emoji)
                } catch (e: Exception) {
                    icon.setImageResource(R.drawable.ic_block_page_without_emoji)
                }
            }
            item.image != null -> {
                image.visible()
                Glide
                    .with(image)
                    .load(item.image)
                    .centerInside()
                    .circleCrop()
                    .into(image)
            }
            item.isEmpty -> {
                icon.setImageResource(R.drawable.ic_block_empty_page)
                image.setImageDrawable(null)
            }
            else -> {
                icon.setImageResource(R.drawable.ic_block_page_without_emoji)
                image.setImageDrawable(null)
            }
        }

        title.setOnClickListener { clicked(ListenerType.Page(item.id)) }
        title.setOnLongClickListener(
            EditorLongClickListener(
                t = item.id,
                click = { onBlockLongClick(itemView, it, clicked) }
            )
        )
    }

    override fun indentize(item: BlockView.Indentable) {
        guideline.setGuidelineBegin(
            item.indent * dimen(R.dimen.indent)
        )
    }

    fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.PageArchive) { "Expected a page archive block, but was: $item" }
        payloads.forEach { payload ->
            if (payload.changes.contains(BlockViewDiffUtil.SELECTION_CHANGED)) {
                itemView.isSelected = item.isSelected
            }
        }
    }
}