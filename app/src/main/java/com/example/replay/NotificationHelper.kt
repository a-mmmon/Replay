package com.example.replay

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object NotificationHelper {

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun sendFollowNotification(actorUserId: String, targetUserId: String) {
        if (actorUserId.isBlank() || targetUserId.isBlank() || actorUserId == targetUserId) return
        loadActorProfile(actorUserId) { actor ->
            val actorName = actor?.username?.ifBlank { "Someone" } ?: "Someone"
            pushNotification(
                targetUserId = targetUserId,
                actorUserId = actorUserId,
                actorUsername = actorName,
                actorProfileImage = actor?.profileImage.orEmpty(),
                type = "follow",
                postId = "",
                message = "$actorName followed you."
            )
        }
    }

    fun sendPostInteractionNotification(
        actorUserId: String,
        postId: String,
        type: String
    ) {
        if (actorUserId.isBlank() || postId.isBlank()) return

        database.getReference("posts").child(postId).get()
            .addOnSuccessListener { postSnap ->
                val post = postSnap.getValue(Post::class.java) ?: return@addOnSuccessListener
                val targetUserId = post.userId
                if (targetUserId.isBlank() || targetUserId == actorUserId) return@addOnSuccessListener

                loadActorProfile(actorUserId) { actor ->
                    val actorName = actor?.username?.ifBlank { "Someone" } ?: "Someone"
                    val message = when (type) {
                        "like" -> "$actorName liked your post."
                        "comment" -> "$actorName commented on your post."
                        "repost" -> "$actorName reposted your post."
                        else -> "$actorName interacted with your post."
                    }

                    pushNotification(
                        targetUserId = targetUserId,
                        actorUserId = actorUserId,
                        actorUsername = actorName,
                        actorProfileImage = actor?.profileImage.orEmpty(),
                        type = type,
                        postId = postId,
                        message = message
                    )
                }
            }
    }

    private fun loadActorProfile(actorUserId: String, onResult: (UserProfile?) -> Unit) {
        database.getReference("users").child(actorUserId).get()
            .addOnSuccessListener { snap ->
                val profile = snap.getValue(UserProfile::class.java)
                if (profile != null) {
                    onResult(profile)
                } else {
                    val fallbackName = if (actorUserId == auth.currentUser?.uid) {
                        auth.currentUser?.email?.substringBefore("@") ?: "Someone"
                    } else {
                        "Someone"
                    }
                    onResult(UserProfile(userId = actorUserId, username = fallbackName))
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    private fun pushNotification(
        targetUserId: String,
        actorUserId: String,
        actorUsername: String,
        actorProfileImage: String,
        type: String,
        postId: String,
        message: String
    ) {
        val ref = database.getReference("notifications").child(targetUserId).push()
        val id = ref.key ?: return
        val item = NotificationItem(
            notificationId = id,
            actorUserId = actorUserId,
            actorUsername = actorUsername,
            actorProfileImage = actorProfileImage,
            type = type,
            postId = postId,
            message = message,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        ref.setValue(item)
    }
}
