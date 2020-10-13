package com.anytypeio.anytype.middleware.converters

import anytype.Events
import anytype.model.Localstore
import anytype.model.Models.Account
import anytype.model.Models.Block
import com.anytypeio.anytype.data.auth.model.*
import com.google.protobuf.Struct
import com.google.protobuf.Value
import timber.log.Timber

fun Events.Event.Account.Show.toAccountEntity(): AccountEntity {
    return AccountEntity(
        id = account.id,
        name = account.name,
        color = if (account.avatar.avatarCase == Account.Avatar.AvatarCase.COLOR)
            account.avatar.color
        else null
    )
}

fun Block.fields(): BlockEntity.Fields = BlockEntity.Fields().also { result ->
    fields.fieldsMap.forEach { (key, value) ->
        result.map[key] = when (val case = value.kindCase) {
            Value.KindCase.NUMBER_VALUE -> value.numberValue
            Value.KindCase.STRING_VALUE -> value.stringValue
            else -> throw IllegalStateException("$case is not supported.")
        }
    }
}

fun Events.SmartBlockType.entity(): BlockEntity.Content.Smart.Type = when (this) {
    Events.SmartBlockType.Archive -> BlockEntity.Content.Smart.Type.ARCHIVE
    Events.SmartBlockType.Page -> BlockEntity.Content.Smart.Type.PAGE
    Events.SmartBlockType.Breadcrumbs -> BlockEntity.Content.Smart.Type.BREADCRUMBS
    Events.SmartBlockType.Home -> BlockEntity.Content.Smart.Type.HOME
    Events.SmartBlockType.ProfilePage -> BlockEntity.Content.Smart.Type.PROFILE
    else -> throw IllegalStateException("Unexpected smart block type: $this")
}

fun Struct.fields(): BlockEntity.Fields = BlockEntity.Fields().also { result ->
    fieldsMap.forEach { (key, value) ->
        result.map[key] = when (val case = value.kindCase) {
            Value.KindCase.NUMBER_VALUE -> value.numberValue
            Value.KindCase.STRING_VALUE -> value.stringValue
            Value.KindCase.BOOL_VALUE -> value.boolValue
            else -> throw IllegalStateException("$case is not supported.")
        }
    }
}

fun Block.text(): BlockEntity.Content.Text = BlockEntity.Content.Text(
    text = text.text,
    marks = text.marks.marksList.marks(),
    style = text.style.entity(),
    isChecked = text.checked,
    color = if (text.color.isNotEmpty()) text.color else null,
    backgroundColor = if (backgroundColor.isNotEmpty()) backgroundColor else null,
    align = if (align != null) align.entity() else null
)

fun List<Block.Content.Text.Mark>.marks(): List<BlockEntity.Content.Text.Mark> = map { mark ->
    BlockEntity.Content.Text.Mark(
        range = IntRange(mark.range.from, mark.range.to),
        param = if (mark.param.isNotEmpty()) mark.param else null,
        type = when (mark.type) {
            Block.Content.Text.Mark.Type.Bold -> {
                BlockEntity.Content.Text.Mark.Type.BOLD
            }
            Block.Content.Text.Mark.Type.Italic -> {
                BlockEntity.Content.Text.Mark.Type.ITALIC
            }
            Block.Content.Text.Mark.Type.Strikethrough -> {
                BlockEntity.Content.Text.Mark.Type.STRIKETHROUGH
            }
            Block.Content.Text.Mark.Type.Underscored -> {
                BlockEntity.Content.Text.Mark.Type.UNDERSCORED
            }
            Block.Content.Text.Mark.Type.Keyboard -> {
                BlockEntity.Content.Text.Mark.Type.KEYBOARD
            }
            Block.Content.Text.Mark.Type.TextColor -> {
                BlockEntity.Content.Text.Mark.Type.TEXT_COLOR
            }
            Block.Content.Text.Mark.Type.BackgroundColor -> {
                BlockEntity.Content.Text.Mark.Type.BACKGROUND_COLOR
            }
            Block.Content.Text.Mark.Type.Link -> {
                BlockEntity.Content.Text.Mark.Type.LINK
            }
            Block.Content.Text.Mark.Type.Mention -> {
                BlockEntity.Content.Text.Mark.Type.MENTION
            }
            else -> throw IllegalStateException("Unexpected mark type: ${mark.type.name}")
        }
    )
}

fun Block.layout(): BlockEntity.Content.Layout = BlockEntity.Content.Layout(
    type = when (layout.style) {
        Block.Content.Layout.Style.Column -> BlockEntity.Content.Layout.Type.COLUMN
        Block.Content.Layout.Style.Row -> BlockEntity.Content.Layout.Type.ROW
        Block.Content.Layout.Style.Div -> BlockEntity.Content.Layout.Type.DIV
        Block.Content.Layout.Style.Header -> BlockEntity.Content.Layout.Type.HEADER
        else -> throw IllegalStateException("Unexpected layout style: ${layout.style}")
    }
)

fun Block.link(): BlockEntity.Content.Link = BlockEntity.Content.Link(
    type = when (link.style) {
        Block.Content.Link.Style.Page -> BlockEntity.Content.Link.Type.PAGE
        Block.Content.Link.Style.Dataview -> BlockEntity.Content.Link.Type.DATA_VIEW
        Block.Content.Link.Style.Archive -> BlockEntity.Content.Link.Type.ARCHIVE
        Block.Content.Link.Style.Dashboard -> BlockEntity.Content.Link.Type.DASHBOARD
        else -> throw IllegalStateException("Unexpected link style: ${link.style}")
    },
    target = link.targetBlockId,
    fields = BlockEntity.Fields().also { result ->
        link.fields.fieldsMap.forEach { (key, value) ->
            result.map[key] = when (val case = value.kindCase) {
                Value.KindCase.NUMBER_VALUE -> value.numberValue
                Value.KindCase.STRING_VALUE -> value.stringValue
                Value.KindCase.BOOL_VALUE -> value.boolValue
                else -> throw IllegalStateException("$case is not supported.")
            }
        }
    }
)

fun Block.divider(): BlockEntity.Content.Divider = BlockEntity.Content.Divider(
    type = when (div.style) {
        Block.Content.Div.Style.Line -> BlockEntity.Content.Divider.Type.LINE
        Block.Content.Div.Style.Dots -> BlockEntity.Content.Divider.Type.DOTS
        else -> throw IllegalStateException("Unexpected div style: ${div.style}")
    }
)

fun Block.icon(): BlockEntity.Content.Icon = BlockEntity.Content.Icon(
    name = icon.name
)

fun Block.file(): BlockEntity.Content.File = BlockEntity.Content.File(
    hash = file.hash,
    name = file.name,
    size = file.size,
    mime = file.mime,
    type = file.type.entity(),
    state = file.state.entity()
)

fun Block.Align.entity(): BlockEntity.Align = when (this) {
    Block.Align.AlignLeft -> BlockEntity.Align.AlignLeft
    Block.Align.AlignCenter -> BlockEntity.Align.AlignCenter
    Block.Align.AlignRight -> BlockEntity.Align.AlignRight
    else -> throw IllegalStateException("Unexpected align type: $this")
}

fun Block.Content.File.Type.entity(): BlockEntity.Content.File.Type = when (this) {
    Block.Content.File.Type.File -> BlockEntity.Content.File.Type.FILE
    Block.Content.File.Type.Image -> BlockEntity.Content.File.Type.IMAGE
    Block.Content.File.Type.Video -> BlockEntity.Content.File.Type.VIDEO
    Block.Content.File.Type.None -> BlockEntity.Content.File.Type.NONE
    else -> throw IllegalStateException("Unexpected file type: $this")
}

fun Block.Content.File.State.entity(): BlockEntity.Content.File.State = when (this) {
    Block.Content.File.State.Done -> BlockEntity.Content.File.State.DONE
    Block.Content.File.State.Empty -> BlockEntity.Content.File.State.EMPTY
    Block.Content.File.State.Uploading -> BlockEntity.Content.File.State.UPLOADING
    Block.Content.File.State.Error -> BlockEntity.Content.File.State.ERROR
    else -> throw IllegalStateException("Unexpected file state: $this")
}

fun Block.bookmark(): BlockEntity.Content.Bookmark = BlockEntity.Content.Bookmark(
    url = bookmark.url.ifEmpty { null },
    description = bookmark.description.ifEmpty { null },
    title = bookmark.title.ifEmpty { null },
    image = bookmark.imageHash.ifEmpty { null },
    favicon = bookmark.faviconHash.ifEmpty { null }
)

fun List<Block>.blocks(
    types: Map<String, Events.SmartBlockType> = emptyMap()
): List<BlockEntity> = mapNotNull { block ->
    when (block.contentCase) {
        Block.ContentCase.TEXT -> {
            BlockEntity(
                id = block.id,
                children = block.childrenIdsList.toList(),
                fields = block.fields(),
                content = block.text()
            )
        }
        Block.ContentCase.LAYOUT -> {
            BlockEntity(
                id = block.id,
                children = block.childrenIdsList,
                fields = block.fields(),
                content = block.layout()
            )
        }
        Block.ContentCase.LINK -> {
            BlockEntity(
                id = block.id,
                children = block.childrenIdsList,
                fields = block.fields(),
                content = block.link()
            )
        }
        Block.ContentCase.DIV -> {
            BlockEntity(
                id = block.id,
                children = emptyList(),
                fields = block.fields(),
                content = block.divider()
            )
        }
        Block.ContentCase.FILE -> {
            BlockEntity(
                id = block.id,
                children = block.childrenIdsList,
                fields = block.fields(),
                content = block.file()
            )
        }
        Block.ContentCase.ICON -> {
            BlockEntity(
                id = block.id,
                children = block.childrenIdsList,
                fields = block.fields(),
                content = block.icon()
            )
        }
        Block.ContentCase.BOOKMARK -> {
            BlockEntity(
                id = block.id,
                children = block.childrenIdsList,
                fields = block.fields(),
                content = block.bookmark()
            )
        }
        Block.ContentCase.SMARTBLOCK -> {
            BlockEntity(
                id = block.id,
                children = block.childrenIdsList,
                content = BlockEntity.Content.Smart(
                    type = types[block.id]?.entity() ?: throw IllegalStateException("Type missing")
                ),
                fields = block.fields()
            )
        }
        else -> {
            Timber.d("Ignoring content type: ${block.contentCase}")
            null
        }
    }
}

fun Block.Content.Text.Style.entity(): BlockEntity.Content.Text.Style = when (this) {
    Block.Content.Text.Style.Paragraph -> BlockEntity.Content.Text.Style.P
    Block.Content.Text.Style.Header1 -> BlockEntity.Content.Text.Style.H1
    Block.Content.Text.Style.Header2 -> BlockEntity.Content.Text.Style.H2
    Block.Content.Text.Style.Header3 -> BlockEntity.Content.Text.Style.H3
    Block.Content.Text.Style.Title -> BlockEntity.Content.Text.Style.TITLE
    Block.Content.Text.Style.Quote -> BlockEntity.Content.Text.Style.QUOTE
    Block.Content.Text.Style.Marked -> BlockEntity.Content.Text.Style.BULLET
    Block.Content.Text.Style.Numbered -> BlockEntity.Content.Text.Style.NUMBERED
    Block.Content.Text.Style.Toggle -> BlockEntity.Content.Text.Style.TOGGLE
    Block.Content.Text.Style.Checkbox -> BlockEntity.Content.Text.Style.CHECKBOX
    Block.Content.Text.Style.Code -> BlockEntity.Content.Text.Style.CODE_SNIPPET
    else -> throw IllegalStateException("Unexpected text style: $this")
}

fun BlockEntity.Content.Text.Style.toMiddleware(): Block.Content.Text.Style = when (this) {
    BlockEntity.Content.Text.Style.P -> Block.Content.Text.Style.Paragraph
    BlockEntity.Content.Text.Style.H1 -> Block.Content.Text.Style.Header1
    BlockEntity.Content.Text.Style.H2 -> Block.Content.Text.Style.Header2
    BlockEntity.Content.Text.Style.H3 -> Block.Content.Text.Style.Header3
    BlockEntity.Content.Text.Style.TITLE -> Block.Content.Text.Style.Title
    BlockEntity.Content.Text.Style.QUOTE -> Block.Content.Text.Style.Quote
    BlockEntity.Content.Text.Style.BULLET -> Block.Content.Text.Style.Marked
    BlockEntity.Content.Text.Style.NUMBERED -> Block.Content.Text.Style.Numbered
    BlockEntity.Content.Text.Style.TOGGLE -> Block.Content.Text.Style.Toggle
    BlockEntity.Content.Text.Style.CHECKBOX -> Block.Content.Text.Style.Checkbox
    BlockEntity.Content.Text.Style.CODE_SNIPPET -> Block.Content.Text.Style.Code
    else -> throw IllegalStateException("Unexpected text style: $this")
}

fun BlockEntity.Content.File.State.toMiddleware(): Block.Content.File.State = when (this) {
    BlockEntity.Content.File.State.EMPTY -> Block.Content.File.State.Empty
    BlockEntity.Content.File.State.ERROR -> Block.Content.File.State.Error
    BlockEntity.Content.File.State.UPLOADING -> Block.Content.File.State.Uploading
    BlockEntity.Content.File.State.DONE -> Block.Content.File.State.Done
}

fun BlockEntity.Content.File.Type.toMiddleware(): Block.Content.File.Type = when (this) {
    BlockEntity.Content.File.Type.NONE -> Block.Content.File.Type.None
    BlockEntity.Content.File.Type.FILE -> Block.Content.File.Type.File
    BlockEntity.Content.File.Type.IMAGE -> Block.Content.File.Type.Image
    BlockEntity.Content.File.Type.VIDEO -> Block.Content.File.Type.Video
}

fun PositionEntity.toMiddleware(): Block.Position = when (this) {
    PositionEntity.NONE -> Block.Position.None
    PositionEntity.LEFT -> Block.Position.Left
    PositionEntity.RIGHT -> Block.Position.Right
    PositionEntity.TOP -> Block.Position.Top
    PositionEntity.BOTTOM -> Block.Position.Bottom
    PositionEntity.INNER -> Block.Position.Inner
}

fun BlockEntity.Align.toMiddleware(): Block.Align = when (this) {
    BlockEntity.Align.AlignLeft -> Block.Align.AlignLeft
    BlockEntity.Align.AlignCenter -> Block.Align.AlignCenter
    BlockEntity.Align.AlignRight -> Block.Align.AlignRight
}

fun Localstore.PageInfo.toEntity(): DocumentInfoEntity = DocumentInfoEntity(
    id = id,
    fields = details.fields(),
    hasInboundLinks = hasInboundLinks,
    snippet = snippet,
    type = pageType.let { type ->
        when (type) {
            Localstore.PageInfo.Type.Page -> DocumentInfoEntity.Type.PAGE
            Localstore.PageInfo.Type.Archive -> DocumentInfoEntity.Type.ARCHIVE
            Localstore.PageInfo.Type.ProfilePage -> DocumentInfoEntity.Type.PROFILE_PAGE
            Localstore.PageInfo.Type.Home -> DocumentInfoEntity.Type.HOME
            Localstore.PageInfo.Type.Set -> DocumentInfoEntity.Type.SET
            else -> throw IllegalStateException("Unexpected page info type: $type")
        }
    }
)

fun Localstore.PageLinksInfo.toEntity() = PageLinksEntity(
    inbound = inboundList.map { it.toEntity() },
    outbound = outboundList.map { it.toEntity() }
)

fun Localstore.PageInfoWithLinks.toEntity() = PageInfoWithLinksEntity(
    id = id,
    docInfo = info.toEntity(),
    links = links.toEntity()
)