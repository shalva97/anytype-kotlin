package com.anytypeio.anytype.presentation.page.editor

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BlockDimensions(
    val left: Int, val top: Int, val bottom: Int, val right: Int, val width: Int, val height: Int
) : Parcelable