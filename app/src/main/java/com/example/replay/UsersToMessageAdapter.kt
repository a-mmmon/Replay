package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.bumptech.glide.Glide

class UsersToMessageAdapter(
    private var users: List<UserProfile>,
    private val onUserClick: (UserProfile) -> Unit
) : RecyclerView.Adapter<UsersToMessageAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ShapeableImageView = itemView.findViewById(R.id.profileImage)
        val userName: TextView = itemView.findViewById(R.id.userName)
        // Removed userBio - if your layout doesn't have this ID, it will cause errors
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_to_message, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        holder.userName.text = user.username  // ✅ FIXED: userName → username
        // Removed setting userBio

        // Load profile image
        if (user.profileImage.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(user.profileImage)
                .placeholder(R.drawable.ic_android_placeholder)
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_android_placeholder)
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