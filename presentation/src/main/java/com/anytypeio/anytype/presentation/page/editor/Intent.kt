package com.anytypeio.anytype.presentation.page.editor

import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.block.model.Position
import com.anytypeio.anytype.domain.common.Id

sealed class Intent {

    sealed class Document : Intent() {

        class Undo(
            val context: Id
        ) : Document()

        class Redo(
            val context: Id
        ) : Document()

        class UpdateTitle(
            val context: Id,
            val title: String
        ) : Document()

        class Move(
            val context: Id,
            val target: Id,
            val targetContext: Id,
            val blocks: List<Id>,
            val position: Position
        ) : Document()

        class TurnIntoDocument(
            val context: Id,
            val targets: List<Id>
        ) : Document()
    }

    sealed class CRUD : Intent() {

        class Replace(
            val context: Id,
            val target: Id,
            val prototype: Block.Prototype
        ) : CRUD()

        class Create(
            val context: Id,
            val target: Id,
            val position: Position,
            val prototype: Block.Prototype
        ) : CRUD()

        class Duplicate(
            val context: Id,
            val target: Id
        ) : CRUD()

        class Unlink(
            val context: Id,
            val targets: List<Id>,
            val previous: Id?,
            val cursor: Int? = null,
            val next: Id?,
            val effects: List<SideEffect> = emptyList()
        ) : CRUD()
    }

    sealed class Clipboard : Intent() {
        class Paste(
            val context: Id,
            val focus: Id,
            val selected: List<Id>,
            val range: IntRange
        ) : Clipboard()
        class Copy(
            val context: Id,
            val range: IntRange?,
            val blocks: List<Block>
        ) : Clipboard()
    }

    sealed class Text : Intent() {

        class UpdateColor(
            val context: Id,
            val target: Id,
            val color: String
        ) : Text()

        class UpdateBackgroundColor(
            val context: Id,
            val targets: List<Id>,
            val color: String
        ) : Text()

        class Split(
            val context: Id,
            val block: Block,
            val range: IntRange,
            val isToggled: Boolean?
        ) : Text()

        class Merge(
            val context: Id,
            val previous: Id,
            val previousLength: Int?,
            val pair: Pair<Id, Id>
        ) : Text()

        class UpdateStyle(
            val context: Id,
            val targets: List<Id>,
            val style: Block.Content.Text.Style
        ) : Text()

        class UpdateCheckbox(
            val context: Id,
            val target: Id,
            val isChecked: Boolean
        ) : Text()

        class UpdateText(
            val context: Id,
            val target: Id,
            val text: String,
            val marks: List<Block.Content.Text.Mark>
        ) : Text()

        class Align(
            val context: Id,
            val target: Id,
            val alignment: Block.Align
        ) : Text()
    }

    sealed class Media : Intent() {

        class DownloadFile(
            val url: String,
            val name: String
        ) : Media()

        class Upload(
            val context: Id,
            val target: Id,
            val url: String,
            val filePath: String
        ) : Media()
    }

    sealed class Bookmark : Intent() {

        class SetupBookmark(
            val context: Id,
            val target: Id,
            val url: String
        ) : Bookmark()
    }
}