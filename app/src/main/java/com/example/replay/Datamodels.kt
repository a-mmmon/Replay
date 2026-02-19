package com.example.replay

import android.os.Parcelable
import com.google.firebase.database.PropertyName
import kotlinx.parcelize.Parcelize

// ============================================
// MESSAGING DATA CLASSES
// ============================================

@Parcelize
data class Conversation(
    var conversationId: String = "",
    var otherUserId: String = "",
    var otherUserName: String = "",
    var otherUserProfileImage: String = "",
    var lastMessage: String = "",
    var timestamp: Long = 0L,
    var unreadBadge: Int = 0,
    var messages: List<Message> = emptyList()
) : Parcelable

@Parcelize
data class Message(
    var messageId: String = "",
    var senderId: String = "",
    var receiverId: String = "",
    var text: String = "",
    var timestamp: Long = 0L,
    var isRead: Boolean = false
) : Parcelable

// ============================================
// USER DATA CLASSES
// ============================================

@Parcelize
data class UserProfile(
    var userId: String = "",
    var username: String = "",
    var handle: String = "",
    var email: String = "",

    @get:PropertyName("profileImage")
    @set:PropertyName("profileImage")
    var profileImage: String = "",

    var bio: String = "",
    var followers: Int = 0,
    var following: Int = 0
) : Parcelable

// ============================================
// POST DATA CLASSES
// ============================================

@Parcelize
data class Post(
    var postId: String = "",
    var userId: String = "",
    var username: String = "",

    @get:PropertyName("userProfileImage")
    @set:PropertyName("userProfileImage")
    var userProfileImage: String = "",

    var caption: String = "",
    var imageUrl: String = "",
    var likes: Int = 0,
    var comments: Int = 0,
    var timestamp: Long = 0L,
    var music: ITunesSong? = null
) : Parcelable