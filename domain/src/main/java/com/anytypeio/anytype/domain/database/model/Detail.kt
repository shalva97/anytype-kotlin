package com.anytypeio.anytype.domain.database.model

sealed class Detail(open val id: String) {

    data class Title(
        override val id: String,
        val name: String,
        val show: Boolean
    ) : Detail(id)

    data class Text(
        override val id: String,
        val name: String,
        val show: Boolean
    ) : Detail(id)

    data class Number(
        override val id: String,
        val name: String,
        val show: Boolean
    ) :
        Detail(id)

    data class Date(
        override val id: String,
        val name: String,
        val show: Boolean
    ) : Detail(id)

    data class Select(
        override val id: String,
        val name: String,
        val select: Set<String> = emptySet(),
        val show: Boolean
    ) :
        Detail(id)

    data class Multiple(
        override val id: String,
        val name: String,
        val multiSelect: Set<String> = emptySet(),
        val show: Boolean
    ) : Detail(id)

    data class Person(
        override val id: String,
        val name: String,
        val accounts: Set<Value>,
        val show: Boolean
    ) :
        Detail(id)

    data class File(
        override val id: String,
        val name: String,
        val show: Boolean
    ) :
        Detail(id)

    data class Bool(
        override val id: String,
        val name: String,
        val show: Boolean
    ) :
        Detail(id)

    data class Link(
        override val id: String,
        val name: String,
        val show: Boolean
    ) :
        Detail(id)

    data class Email(
        override val id: String,
        val name: String,
        val show: Boolean
    ) :
        Detail(id)

    data class Phone
        (
        override val id: String,
        val name: String,
        val show: Boolean
    ) :
        Detail(id)
}

data class Value(val name: String)