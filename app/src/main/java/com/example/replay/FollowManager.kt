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

        val currentFollowingCountRef = database.getReference("users")
            .child(currentUserId)
            .child("following")

        val targetFollowersCountRef = database.getReference("users")
            .child(targetUserId)
            .child("followers")

        val writeTask = if (follow) {
            myFollowingRef.setValue(true)
                .continueWithTask {
                    targetFollowersRef.setValue(true)
                }
        } else {
            myFollowingRef.removeValue()
                .continueWithTask {
                    targetFollowersRef.removeValue()
                }
        }

        writeTask.addOnSuccessListener {
            val delta = if (follow) 1 else -1
            currentFollowingCountRef.runTransaction(CountTransaction(delta))
            targetFollowersCountRef.runTransaction(CountTransaction(delta))
            onComplete(true, null)
        }.addOnFailureListener { error ->
            onComplete(false, error.message)
        }
    }

    fun loadMutualFollowIds(currentUserId: String, onResult: (Set<String>) -> Unit) {
        if (currentUserId.isBlank()) {
            onResult(emptySet())
            return
        }

        val userFollowsRef = database.getReference("userFollows").child(currentUserId)

        userFollowsRef.child("following").get().addOnSuccessListener { followingSnapshot ->
            val followingIds = followingSnapshot.children
                .mapNotNull { it.key }
                .toSet()

            userFollowsRef.child("followers").get().addOnSuccessListener { followersSnapshot ->
                val followerIds = followersSnapshot.children
                    .mapNotNull { it.key }
                    .toSet()

                onResult(followingIds.intersect(followerIds))
            }.addOnFailureListener {
                onResult(emptySet())
            }
        }.addOnFailureListener {
            onResult(emptySet())
        }
    }

    private class CountTransaction(
        private val delta: Int
    ) : Transaction.Handler {
        override fun doTransaction(currentData: MutableData): Transaction.Result {
            val current = (currentData.getValue(Int::class.java) ?: 0)
            val next = (current + delta).coerceAtLeast(0)
            currentData.value = next
            return Transaction.success(currentData)
        }

        override fun onComplete(
            error: DatabaseError?,
            committed: Boolean,
            currentData: com.google.firebase.database.DataSnapshot?
        ) = Unit
    }
}
