package com.example.replay

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationsActivity : BaseThemedActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var emptyView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val currentUserId get() = auth.currentUser?.uid.orEmpty()

    private var notificationsRef: DatabaseReference? = null
    private var notificationsListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        backButton = findViewById(R.id.backButton)
        emptyView = findViewById(R.id.emptyView)
        recyclerView = findViewById(R.id.notificationsRecyclerView)

        backButton.setOnClickListener { finish() }

        adapter = NotificationAdapter(emptyList()) { notification ->
            if (notification.actorUserId.isNotBlank()) {
                startActivity(Intent(this, UserProfileActivity::class.java).apply {
                    putExtra("user_id", notification.actorUserId)
                    putExtra("username", notification.actorUsername)
                })
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        observeNotifications()
        markAllAsRead()
    }

    override fun onDestroy() {
        super.onDestroy()
        val ref = notificationsRef
        val listener = notificationsListener
        if (ref != null && listener != null) {
            ref.removeEventListener(listener)
        }
    }

    private fun observeNotifications() {
        if (currentUserId.isBlank()) return
        val ref = database.getReference("notifications").child(currentUserId)
        notificationsRef = ref

        notificationsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.map { child ->
                    NotificationItem(
                        notificationId = child.child("notificationId").getValue(String::class.java)
                            ?: child.key.orEmpty(),
                        actorUserId = child.child("actorUserId").getValue(String::class.java).orEmpty(),
                        actorUsername = child.child("actorUsername").getValue(String::class.java)
                            .orEmpty(),
                        actorProfileImage = child.child("actorProfileImage")
                            .getValue(String::class.java).orEmpty(),
                        type = child.child("type").getValue(String::class.java).orEmpty(),
                        postId = child.child("postId").getValue(String::class.java).orEmpty(),
                        message = child.child("message").getValue(String::class.java).orEmpty(),
                        timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L,
                        isRead = child.child("isRead").getValue(Boolean::class.java) == true
                    )
                }.sortedByDescending { it.timestamp }

                adapter.updateNotifications(items)
                emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) = Unit
        }

        ref.addValueEventListener(notificationsListener as ValueEventListener)
    }

    private fun markAllAsRead() {
        if (currentUserId.isBlank()) return
        database.getReference("notifications").child(currentUserId).get()
            .addOnSuccessListener { snapshot ->
                val updates = hashMapOf<String, Any>()
                snapshot.children.forEach { child ->
                    val id = child.key ?: return@forEach
                    updates["$id/isRead"] = true
                }
                if (updates.isNotEmpty()) {
                    database.getReference("notifications").child(currentUserId).updateChildren(updates)
                }
            }
    }
}
