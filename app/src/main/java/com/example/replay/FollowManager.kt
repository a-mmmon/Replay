package com.example.replay

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.DatabaseError

object FollowManager {

    private val database = FirebaseDatabase.getInstance()

    fun checkRelationship(
        currentUserId: String,
        targetUserId: String,
        onResult: (isFollowing: Boolean, followsBack: Boolean) -> Unit
    ) {
        if (currentUserId.isBlank() || targetUserId.isBlank()) {
            onResult(false, false)
            return
        }

        val myFollowingRef = database.getReference("userFollows")
            .child(currentUserId)
            .child("following")
            .child(targetUserId)

        val theirFollowingRef = database.getReference("userFollows")
            .child(targetUserId)
            .child("following")
            .child(currentUserId)

        myFollowingRef.get().addOnSuccessListener { myFollowSnap ->
            val isFollowing = myFollowSnap.getValue(Boolean::class.java) == true
            theirFollowingRef.get().addOnSuccessListener { theirFollowSnap ->
                val followsBack = theirFollowSnap.getValue(Boolean::class.java) == true
                onResult(isFollowing, followsBack)
            }.addOnFailureListener {
                onResult(isFollowing, false)
            }
        }.addOnFailureListener {
            onResult(false, false)
        }
    }

    fun canMessage(
        currentUserId: String,
        targetUserId: String,
        onResult: (Boolean) -> Unit
    ) {
        checkRelationship(currentUserId, targetUserId) { isFollowing, followsBack ->
            onResult(isFollowing && followsBack)
        }
    }

    fun setFollowStatus(
        currentUserId: String,
        targetUserId: String,
        follow: Boolean,
        onComplete: (success: Boolean, error: String?) -> Unit
    ) {
        if (currentUserId.isBlank() || targetUserId.isBlank() || currentUserId == targetUserId) {
            onComplete(false, "Invalid users")
            return
        }

        val myFollowingRef = database.getReference("userFollows")
            .child(currentUserId)
            .child("following")
            .child(targetUserId)

        val targetFollowersRef = database.getReference("userFollows")
            .child(targetUserId)
            .child("followers")
            .child(currentUserId)

        val writeTask = if (follow) {
            myFollowingRef.setValue(true).continueWithTask { targetFollowersRef.setValue(true) }
        } else {
            myFollowingRef.removeValue().continueWithTask { targetFollowersRef.removeValue() }
        }

        writeTask.addOnSuccessListener {
            // ✅ After writing, re-count the actual list size and sync to user profile
            syncFollowingCount(currentUserId)
            syncFollowersCount(targetUserId)
            onComplete(true, null)
        }.addOnFailureListener { error ->
            onComplete(false, error.message)
        }
    }

    // ✅ Count actual following list and write it to users/{userId}/following
    private fun syncFollowingCount(userId: String) {
        database.getReference("userFollows").child(userId).child("following")
            .get()
            .addOnSuccessListener { snapshot ->
                val count = snapshot.childrenCount.toInt()
                database.getReference("users").child(userId).child("following").setValue(count)
            }
    }

    // ✅ Count actual followers list and write it to users/{userId}/followers
    private fun syncFollowersCount(userId: String) {
        database.getReference("userFollows").child(userId).child("followers")
            .get()
            .addOnSuccessListener { snapshot ->
                val count = snapshot.childrenCount.toInt()
                database.getReference("users").child(userId).child("followers").setValue(count)
            }
    }

    fun loadMutualFollowIds(currentUserId: String, onResult: (Set<String>) -> Unit) {
        if (currentUserId.isBlank()) {
            onResult(emptySet())
            return
        }

        val userFollowsRef = database.getReference("userFollows").child(currentUserId)

        userFollowsRef.child("following").get().addOnSuccessListener { followingSnapshot ->
            val followingIds = followingSnapshot.children.mapNotNull { it.key }.toSet()

            userFollowsRef.child("followers").get().addOnSuccessListener { followersSnapshot ->
                val followerIds = followersSnapshot.children.mapNotNull { it.key }.toSet()
                onResult(followingIds.intersect(followerIds))
            }.addOnFailureListener {
                onResult(emptySet())
            }
        }.addOnFailureListener {
            onResult(emptySet())
        }
    }
}