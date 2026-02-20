package com.example.replay

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NewMessageBottomSheet : BottomSheetDialogFragment() {

    private lateinit var searchInput: EditText
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var usersAdapter: UsersAdapter

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val currentUserId get() = auth.currentUser?.uid ?: ""

    private var onUserSelected: ((UserProfile) -> Unit)? = null
    private val allUsers = mutableListOf<UserProfile>()

    fun setOnUserSelectedListener(listener: (UserProfile) -> Unit) {
        onUserSelected = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_new_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchInput = view.findViewById(R.id.searchInput)
        usersRecyclerView = view.findViewById(R.id.usersRecyclerView)
        setupRecyclerView()
        loadAllUsersFromFirebase()
        setupSearch()
    }

    private fun setupRecyclerView() {
        usersAdapter = UsersAdapter(mutableListOf()) { user ->
            onUserSelected?.invoke(user)
            dismiss()
        }
        usersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        usersRecyclerView.adapter = usersAdapter
    }

    private fun loadAllUsersFromFirebase() {
        FollowManager.loadMutualFollowIds(currentUserId) { allowedUserIds ->
            database.getReference("users")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!isAdded) return
                        allUsers.clear()
                        for (child in snapshot.children) {
                            val user = child.getValue(UserProfile::class.java) ?: continue
                            if (user.userId == currentUserId) continue
                            if (allowedUserIds.contains(user.userId)) {
                                allUsers.add(user)
                            }
                        }
                        usersAdapter.updateUsers(allUsers)

                        if (allUsers.isEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                "No mutual follows yet. Follow each other to message.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        Log.d("NewMessageBottomSheet", "Loaded ${allUsers.size} messageable users")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("NewMessageBottomSheet", "Failed to load users: ${error.message}")
                    }
                })
        }
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                val filtered = if (query.isEmpty()) allUsers
                else allUsers.filter {
                    it.username.lowercase().contains(query) || it.handle.lowercase().contains(query)
                }
                usersAdapter.updateUsers(filtered)
            }
        })
    }
}
