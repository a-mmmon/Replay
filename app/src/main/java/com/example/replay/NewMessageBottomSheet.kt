package com.example.replay

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
    private lateinit var usersAdapter: UsersToMessageAdapter

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val currentUserId get() = auth.currentUser?.uid ?: ""

    private var onUserSelected: ((UserProfile) -> Unit)? = null
    private val allUsers = mutableListOf<UserProfile>()

    fun setOnUserSelectedListener(listener: (UserProfile) -> Unit) {
        onUserSelected = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_new_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchInput = view.findViewById(R.id.searchInput)
        usersRecyclerView = view.findViewById(R.id.usersRecyclerView)

        setupRecyclerView()
        loadAllUsersFromFirebase()   // ← Load real users
        setupSearch()
    }

    private fun setupRecyclerView() {
        usersAdapter = UsersToMessageAdapter(mutableListOf()) { user ->
            onUserSelected?.invoke(user)
            dismiss()
        }
        usersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        usersRecyclerView.adapter = usersAdapter
    }

    // ─── FIREBASE: Load all real users (except current user) ─────────────────
    private fun loadAllUsersFromFirebase() {
        database.getReference("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    allUsers.clear()

                    for (child in snapshot.children) {
                        val user = child.getValue(UserProfile::class.java) ?: continue
                        // Don't show current user in search results
                        if (user.userId != currentUserId) {
                            allUsers.add(user)
                        }
                    }

                    usersAdapter.updateUsers(allUsers)
                    Log.d("NewMessageBottomSheet", "Loaded ${allUsers.size} users")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("NewMessageBottomSheet", "Failed to load users: ${error.message}")
                }
            })
    }

    // ─── Search/filter users by username ─────────────────────────────────────
    private fun setupSearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                if (query.isEmpty()) {
                    usersAdapter.updateUsers(allUsers)
                } else {
                    val filtered = allUsers.filter { user ->
                        user.username.lowercase().contains(query) ||
                                user.handle.lowercase().contains(query)
                    }
                    usersAdapter.updateUsers(filtered)
                }
            }
        })
    }
}