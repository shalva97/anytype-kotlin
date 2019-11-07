package com.agileburo.anytype.ui.auth.account

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ext.invisible
import com.agileburo.anytype.core_utils.ext.visible
import com.agileburo.anytype.presentation.auth.model.SelectAccountView
import com.agileburo.anytype.presentation.auth.model.SelectAccountView.Companion.ADD_NEW_PROFILE
import com.agileburo.anytype.presentation.auth.model.SelectAccountView.Companion.PROFILE
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_choose_profile_profile.view.*
import java.io.ByteArrayInputStream

class SelectAccountAdapter(
    private val views: MutableList<SelectAccountView>,
    private val onAddNewProfileClicked: () -> Unit,
    private val onProfileClicked: (SelectAccountView.AccountView) -> Unit
) : RecyclerView.Adapter<SelectAccountAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            PROFILE -> {
                ViewHolder.ProfileHolder(
                    view = inflater.inflate(R.layout.item_choose_profile_profile, parent, false)
                )
            }
            ADD_NEW_PROFILE -> {
                ViewHolder.AddNewProfileViewHolder(
                    view = inflater.inflate(R.layout.item_choose_profile_add, parent, false)
                )
            }
            else -> throw IllegalStateException("Unknown view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int = views[position].getViewType()

    override fun getItemCount(): Int = views.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.ProfileHolder -> {
                holder.bind(
                    model = views[position] as SelectAccountView.AccountView,
                    onProfileClicked = onProfileClicked
                )
            }
            is ViewHolder.AddNewProfileViewHolder -> {
                holder.bind(
                    onAddNewProfileClicked = onAddNewProfileClicked
                )
            }
        }
    }

    fun update(update: List<SelectAccountView>) {
        views.apply {
            clear()
            addAll(update)
        }
        notifyDataSetChanged()
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class ProfileHolder(view: View) : ViewHolder(view) {

            private val name = itemView.name
            private val placeholder = itemView.avatarPlaceholder
            private val avatar = itemView.avatar

            fun bind(
                model: SelectAccountView.AccountView,
                onProfileClicked: (SelectAccountView.AccountView) -> Unit
            ) {
                name.text = model.name
                itemView.setOnClickListener { onProfileClicked(model) }

                if (model.image != null) {
                    placeholder.invisible()

                    val stream = ByteArrayInputStream(model.image)
                    val bitmap = BitmapFactory.decodeStream(stream)

                    Glide
                        .with(avatar)
                        .load(bitmap)
                        .centerInside()
                        .circleCrop()
                        .into(avatar)

                    stream.close()

                } else {
                    avatar.invisible()
                    placeholder.apply {
                        visible()
                        text = model.name.first().toUpperCase().toString()
                    }
                }

            }
        }

        class AddNewProfileViewHolder(view: View) : ViewHolder(view) {

            fun bind(
                onAddNewProfileClicked: () -> Unit
            ) {
                itemView.setOnClickListener { onAddNewProfileClicked() }
            }
        }
    }

}