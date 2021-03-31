package com.anytypeio.anytype.core_models

/**
 * @property [format] format of the underlying data
 * @property [name] pretty name
 * @property [source] defines where the underlying data is stored
 * @property [isReadOnly] editable by user or not
 * @property [isHidden] whether this type is internal (not displayed to user)
 */
data class Relation(
    val key: String,
    val name: String,
    val format: Format,
    val source: Source,
    val isHidden: Boolean = false,
    val isReadOnly: Boolean = false,
    val isMulti: Boolean = false,
    val selections: List<Option> = emptyList(),
    val objectTypes: List<String> = emptyList(),
    val defaultValue: Any? = null
) {

    enum class Format {
        SHORT_TEXT,
        LONG_TEXT,
        NUMBER,
        STATUS,
        TAG,
        DATE,
        FILE,
        CHECKBOX,
        URL,
        EMAIL,
        PHONE,
        EMOJI,
        OBJECT,
        RELATIONS
    }

    enum class Source {
        DETAILS, DERIVED, ACCOUNT
    }

    data class Option(
        val id: String,
        val text: String,
        val color: String
    )
}