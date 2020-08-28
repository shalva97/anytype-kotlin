package com.agileburo.anytype.presentation.mapper

import com.agileburo.anytype.core_ui.common.Alignment
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.navigation.PageLinkView
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.model.UiBlock
import com.agileburo.anytype.core_ui.widgets.toolbar.adapter.Mention
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.config.DebugSettings
import com.agileburo.anytype.domain.dashboard.model.HomeDashboard
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.navigation.PageInfo
import com.agileburo.anytype.presentation.desktop.DashboardView
import com.agileburo.anytype.presentation.settings.EditorSettings

fun Block.Content.File.toPictureView(
    id: String,
    urlBuilder: UrlBuilder,
    indent: Int,
    mode: BlockView.Mode
): BlockView = when (state) {
    Block.Content.File.State.EMPTY -> BlockView.MediaPlaceholder.Picture(
        id = id,
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.UPLOADING -> BlockView.Upload.Picture(
        id = id,
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.DONE -> BlockView.Media.Picture(
        id = id,
        size = size,
        name = name,
        mime = mime,
        hash = hash,
        url = urlBuilder.image(hash),
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.ERROR -> BlockView.Error.Picture(
        id = id,
        indent = indent,
        mode = mode
    )
    else -> throw IllegalStateException("Unexpected state: $state")
}

fun Block.Content.File.toVideoView(
    id: String,
    urlBuilder: UrlBuilder,
    indent: Int,
    mode: BlockView.Mode
): BlockView = when (state) {
    Block.Content.File.State.EMPTY -> BlockView.MediaPlaceholder.Video(
        id = id,
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.UPLOADING -> BlockView.Upload.Video(
        id = id,
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.DONE -> BlockView.Media.Video(
        id = id,
        size = size,
        name = name,
        mime = mime,
        hash = hash,
        url = urlBuilder.video(hash),
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.ERROR -> BlockView.Error.Video(
        id = id,
        indent = indent,
        mode = mode
    )
    else -> throw IllegalStateException("Unexpected state: $state")
}

fun Block.Content.File.toFileView(
    id: String,
    urlBuilder: UrlBuilder,
    indent: Int,
    mode: BlockView.Mode
): BlockView = when (state) {
    Block.Content.File.State.EMPTY -> BlockView.MediaPlaceholder.File(
        id = id,
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.UPLOADING -> BlockView.Upload.File(
        id = id,
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.DONE -> BlockView.Media.File(
        id = id,
        size = size,
        name = name,
        mime = mime,
        hash = hash,
        url = urlBuilder.video(hash),
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.ERROR -> BlockView.Error.File(
        id = id,
        indent = indent,
        mode = mode
    )
    else -> throw IllegalStateException("Unexpected state: $state")
}

fun Block.Align.toView(): Alignment = when (this) {
    Block.Align.AlignLeft -> Alignment.START
    Block.Align.AlignCenter -> Alignment.CENTER
    Block.Align.AlignRight -> Alignment.END
}

fun Block.Content.Text.marks(): List<Markup.Mark> = marks.mapNotNull { mark ->
    when (mark.type) {
        Block.Content.Text.Mark.Type.ITALIC -> {
            Markup.Mark(
                from = mark.range.first,
                to = mark.range.last,
                type = Markup.Type.ITALIC
            )
        }
        Block.Content.Text.Mark.Type.BOLD -> {
            Markup.Mark(
                from = mark.range.first,
                to = mark.range.last,
                type = Markup.Type.BOLD
            )
        }
        Block.Content.Text.Mark.Type.STRIKETHROUGH -> {
            Markup.Mark(
                from = mark.range.first,
                to = mark.range.last,
                type = Markup.Type.STRIKETHROUGH
            )
        }
        Block.Content.Text.Mark.Type.TEXT_COLOR -> {
            Markup.Mark(
                from = mark.range.first,
                to = mark.range.last,
                type = Markup.Type.TEXT_COLOR,
                param = checkNotNull(mark.param)
            )
        }
        Block.Content.Text.Mark.Type.LINK -> {
            Markup.Mark(
                from = mark.range.first,
                to = mark.range.last,
                type = Markup.Type.LINK,
                param = checkNotNull(mark.param)
            )
        }
        Block.Content.Text.Mark.Type.BACKGROUND_COLOR -> {
            Markup.Mark(
                from = mark.range.first,
                to = mark.range.last,
                type = Markup.Type.BACKGROUND_COLOR,
                param = checkNotNull(mark.param)
            )
        }
        Block.Content.Text.Mark.Type.KEYBOARD -> {
            Markup.Mark(
                from = mark.range.first,
                to = mark.range.last,
                type = Markup.Type.KEYBOARD
            )
        }
        Block.Content.Text.Mark.Type.MENTION -> {
            Markup.Mark(
                from = mark.range.first,
                to = mark.range.last,
                type = Markup.Type.MENTION,
                param = mark.param
            )
        }
        else -> null
    }
}

fun HomeDashboard.toView(
    builder: UrlBuilder
): List<DashboardView.Document> = children.mapNotNull { id ->
    blocks.find { block -> block.id == id }?.let { model ->
        when (val content = model.content) {
            is Block.Content.Link -> {
                if (content.type == Block.Content.Link.Type.PAGE) {
                    if (details.details[content.target]?.isArchived != true) {
                        DashboardView.Document(
                            id = model.id,
                            target = content.target,
                            title = details.details[content.target]?.name,
                            emoji = details.details[content.target]?.iconEmoji?.let { name ->
                                if (name.isNotEmpty())
                                    name
                                else
                                    null
                            },
                            image = details.details[content.target]?.iconImage?.let { name ->
                                if (name.isNotEmpty())
                                    builder.image(name)
                                else
                                    null
                            }
                        )
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
            else -> null
        }
    }
}

fun UiBlock.style(): Block.Content.Text.Style = when (this) {
    UiBlock.TEXT -> Block.Content.Text.Style.P
    UiBlock.HEADER_ONE -> Block.Content.Text.Style.H1
    UiBlock.HEADER_TWO -> Block.Content.Text.Style.H2
    UiBlock.HEADER_THREE -> Block.Content.Text.Style.H3
    UiBlock.HIGHLIGHTED -> Block.Content.Text.Style.QUOTE
    UiBlock.CHECKBOX -> Block.Content.Text.Style.CHECKBOX
    UiBlock.BULLETED -> Block.Content.Text.Style.BULLET
    UiBlock.NUMBERED -> Block.Content.Text.Style.NUMBERED
    UiBlock.TOGGLE -> Block.Content.Text.Style.TOGGLE
    UiBlock.CODE -> Block.Content.Text.Style.CODE_SNIPPET
    else -> throw IllegalStateException("Could not extract style from block: $this")
}

fun DebugSettings.toView(): EditorSettings =
    EditorSettings(customContextMenu = this.isAnytypeContextMenuEnabled)

fun PageInfo.toView(urlBuilder: UrlBuilder): PageLinkView = PageLinkView(
    id = id,
    title = fields.name.orEmpty(),
    subtitle = snippet.orEmpty(),
    image = fields.toImageView(urlBuilder),
    emoji = fields.toEmojiView()
)

fun Block.Fields.toImageView(urlBuilder: UrlBuilder): String? = this.iconImage.let { url ->
    if (url.isNullOrBlank()) null else urlBuilder.image(url)
}

fun Block.Fields.toEmojiView(): String? = this.iconEmoji.let { emoji ->
    if (emoji.isNullOrBlank()) null else emoji
}

fun PageInfo.toMentionView() = Mention(
    id = id,
    title = fields.getName(),
    image = fields.iconImage,
    emoji = fields.iconEmoji
)

fun Block.Fields.getName(): String =
    this.name.let { name ->
        if (name.isNullOrBlank()) "Untitled" else name
    }

fun Markup.Mark.mark(): Block.Content.Text.Mark = when (type) {
    Markup.Type.BOLD -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.BOLD
    )
    Markup.Type.ITALIC -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.ITALIC
    )
    Markup.Type.STRIKETHROUGH -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.STRIKETHROUGH
    )
    Markup.Type.TEXT_COLOR -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.TEXT_COLOR,
        param = param
    )
    Markup.Type.BACKGROUND_COLOR -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.BACKGROUND_COLOR,
        param = param
    )
    Markup.Type.LINK -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.LINK,
        param = param
    )
    Markup.Type.KEYBOARD -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.KEYBOARD
    )
    Markup.Type.MENTION -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.MENTION,
        param = param
    )
}