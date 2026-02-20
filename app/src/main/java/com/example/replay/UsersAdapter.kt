package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

class UsersAdapter(
    private var users: List<UserProfile>,
    private val onUserClick: (UserProfile) -> Unit
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ShapeableImageView = itemView.findViewById(R.id.profileImage)
        val username: TextView = itemView.findViewById(R.id.username)
        val handle: TextView = itemView.findViewById(R.id.handle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        holder.username.text = user.username.ifBlank { "User" }
        holder.handle.text = when {
            user.handle.isNotBlank() -> user.handle
            user.email.isNotBlank() -> "@${user.email.substringBefore("@")}"
            else -> "@${user.username.ifBlank { "user" }}"
        }

        if (user.profileImage.isNotBlank()) {
            Glide.with(holder.itemView.context)
                .load(user.profileImage)
                .placeholder(ThemeManager.getDefaultAvatarRes(holder.itemView.context))
                .error(ThemeManager.getDefaultAvatarRes(holder.itemView.context))
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(
                ThemeManager.getDefaultAvatarRes(holder.itemView.context)
            )
        }

        holder.itemView.setOnClickListener {
            onUserClick(user)
        }
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<UserProfile>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
