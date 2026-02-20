package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.bumptech.glide.Glide

class ConversationsAdapter(
    private var conversations: List<Conversation>,
    private val onConversationClick: (Conversation) -> Unit,
    private val onConversationMenuClick: (anchor: View, conversation: Conversation) -> Unit
) : RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder>() {

    inner class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ShapeableImageView = itemView.findViewById(R.id.profileImage)
        val userName: TextView = itemView.findViewById(R.id.userName)
        val lastMessage: TextView = itemView.findViewById(R.id.lastMessage)
        val timestamp: TextView = itemView.findViewById(R.id.timestamp)
        val unreadBadge: TextView = itemView.findViewById(R.id.unreadBadge)
        val conversationMenuButton: ImageView = itemView.findViewById(R.id.conversationMenuButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversation = conversations[position]

        holder.userName.text = conversation.otherUserName
        holder.lastMessage.text = conversation.lastMessage
        holder.timestamp.text = formatTimestamp(conversation.timestamp)

        if (conversation.otherUserProfileImage.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(conversation.otherUserProfileImage)
                .placeholder(ThemeManager.getDefaultAvatarRes(holder.itemView.context))
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(ThemeManager.getDefaultAvatarRes(holder.itemView.context))
        }

        // ✅ FIXED: Using correct field name 'unreadBadge' from Conversation data class
        if (conversation.unreadBadge > 0) {
            holder.unreadBadge.visibility = View.VISIBLE
            holder.unreadBadge.text = conversation.unreadBadge.toString()
        } else {
            holder.unreadBadge.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onConversationClick(conversation)
        }
        holder.conversationMenuButton.setOnClickListener {
            onConversationMenuClick(it, conversation)
        }
    }

    override fun getItemCount(): Int = conversations.size

    fun updateConversations(newConversations: List<Conversation>) {
        conversations = newConversations
        notifyDataSetChanged()
    }

    private fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m"
            diff < 86400000 -> "${diff / 3600000}h"
            diff < 604800000 -> "${diff / 86400000}d"
            else -> "${diff / 604800000}w"
        }
    }
}
