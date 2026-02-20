package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(
    private var notifications: List<NotificationItem>,
    private val onNotificationClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ShapeableImageView = itemView.findViewById(R.id.profileImage)
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val timestampText: TextView = itemView.findViewById(R.id.timestampText)
        val unreadDot: View = itemView.findViewById(R.id.unreadDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = notifications[position]
        holder.messageText.text = item.message
        holder.timestampText.text = formatTimestamp(item.timestamp)
        holder.unreadDot.visibility = if (item.isRead) View.GONE else View.VISIBLE

        if (item.actorProfileImage.isNotBlank()) {
            Glide.with(holder.itemView.context)
                .load(item.actorProfileImage)
                .placeholder(ThemeManager.getDefaultAvatarRes(holder.itemView.context))
                .error(ThemeManager.getDefaultAvatarRes(holder.itemView.context))
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(
                ThemeManager.getDefaultAvatarRes(holder.itemView.context)
            )
        }

        holder.itemView.setOnClickListener { onNotificationClick(item) }
    }

    override fun getItemCount(): Int = notifications.size

    fun updateNotifications(newItems: List<NotificationItem>) {
        notifications = newItems
        notifyDataSetChanged()
    }

    private fun formatTimestamp(timestamp: Long): String {
        if (timestamp <= 0L) return ""
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            diff < 604_800_000 -> "${diff / 86_400_000}d ago"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
        }
    }
}
