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
    val handle: String = "",
    val email: String = "",
    val profileImage: String = "",
    val bio: String = "",
    val followers: Int = 0,
    val following: Int = 0,
    val streakCount: Int = 0,
    val lastActiveDate: Long = 0L
    // NOTE: No-arg constructor is required by Firebase — default values provide this ✅
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
    val reposts: Int = 0,
    val timestamp: Long = 0L,
    val music: ITunesSong? = null,
    val isRepost: Boolean = false,
    val originalPostId: String = "",
    val repostedByUsername: String = "",

    val songTitle: String? = null,
    val songArtist: String? = null,
    val songImageUrl: String? = null
) : Parcelable

// ============================================
// COMMENT DATA CLASS
// ============================================

@Parcelize
data class Comment(
    val commentId: String = "",
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfileImage: String = "",
    val commentText: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

// ============================================
// REPOST DATA CLASS
// ============================================

@Parcelize
data class Repost(
    val repostId: String = "",
    val originalPostId: String = "",
    val originalUserId: String = "",
    val repostedByUserId: String = "",
    val repostedByUsername: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable