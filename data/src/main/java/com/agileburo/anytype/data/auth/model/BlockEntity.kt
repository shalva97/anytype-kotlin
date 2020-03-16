package com.agileburo.anytype.data.auth.model

/**
 * Block's data layer representation.
 */
data class BlockEntity(
    val id: String,
    val children: List<String>,
    val content: Content,
    val fields: Fields
) {
    data class Fields(val map: MutableMap<String?, Any?> = mutableMapOf())

    sealed class Content {

        data class Text(
            val text: String,
            val style: Style,
            val marks: List<Mark>,
            val isChecked: Boolean? = null,
            val color: String? = null,
            val backgroundColor: String? = null
        ) : Content() {

            data class Mark(
                val range: IntRange,
                val type: Type,
                val param: Any?
            ) {
                enum class Type {
                    STRIKETHROUGH,
                    KEYBOARD,
                    ITALIC,
                    BOLD,
                    UNDERSCORED,
                    LINK,
                    TEXT_COLOR,
                    BACKGROUND_COLOR
                }
            }

            enum class Style {
                P, H1, H2, H3, H4, TITLE, QUOTE, CODE_SNIPPET, BULLET, NUMBERED, TOGGLE, CHECKBOX
            }
        }

        data class Layout(val type: Type) : Content() {
            enum class Type { ROW, COLUMN }
        }

        data class Image(
            val path: String
        ) : Content()

        data class Icon(
            val name: String
        ) : Content()

        data class Dashboard(val type: Type) : Content() {
            enum class Type { MAIN_SCREEN, ARCHIVE }
        }

        data class Page(val style: Style) : Content() {
            enum class Style { EMPTY, TASK, SET }
        }

        data class Link(
            val target: String,
            val type: Type,
            val fields: Fields
        ) : Content() {
            enum class Type { PAGE, DATA_VIEW, DASHBOARD, ARCHIVE }
        }

        data class File(
            val hash: String,
            val name: String,
            val type: Type,
            val mime: String,
            val size: Long,
            val added: Long,
            val state: State
        ) : Content() {
            enum class Type { NONE, FILE, IMAGE, VIDEO }
            enum class State { EMPTY, UPLOADING, DONE, ERROR }
        }

        data class Bookmark(
            val url: String,
            val title: String?,
            val description: String?,
            val image: String?,
            val favicon: String?
        ) : Content()

        object Divider : Content()
    }

    sealed class Prototype {
        class Text(
            val style: Content.Text.Style
        ) : Prototype()

        data class Page(
            val style: Content.Page.Style
        ) : Prototype()

        object Divider : Prototype()
    }
}