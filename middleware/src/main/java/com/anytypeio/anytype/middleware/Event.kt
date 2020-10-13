package com.anytypeio.anytype.middleware

sealed class Event {

    class AccountAdd(
        val index: Int,
        val id: String,
        val name: String
    ) : Event()

}