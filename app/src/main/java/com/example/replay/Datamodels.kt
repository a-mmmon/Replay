package com.example.replay

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Unified Data Models for Replay App
 * Contains all data classes in ONE place to avoid redeclaration errors
 */

// ============================================
// MESSAGING DATA CLASSES
// ============================================

@Parcelize
data class Conversation(
    val conversationId: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val otherUserProfileImage: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L,
    val unreadBadge: Int = 0,
    val messages: List<Message> = emptyList()
) : Parcelable

@Parcelize
data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false
) : Parcelable

// ============================================
// USER DATA CLASSES
// ============================================

@Parcelize
data class UserProfile(
    val userId: String = "",
    val username: String = "",
    val handle: String = "",                // ✅ ADDED: Handle field (e.g., "@taylorswift")
    val profileImage: String = "",
    val bio: String = "",
    val followers: Int = 0,
    val following: Int = 0
) : Parcelable

// ============================================
// POST DATA CLASSES
// ============================================

@Parcelize
data class Post(
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfileImage: String = "",
    val caption: String = "",
    val imageUrl: String = "",
    val likes: Int = 0,
    val comments: Int = 0,
    val timestamp: Long = 0L,
    val music: ITunesSong? = null
) : Parcelable

