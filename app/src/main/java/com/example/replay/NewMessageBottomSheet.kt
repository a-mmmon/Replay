package com.example.replay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NewMessageBottomSheet : BottomSheetDialogFragment() {

    private lateinit var searchInput: EditText
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var usersAdapter: UsersAdapter
    private var onUserSelectedListener: ((UserProfile) -> Unit)? = null

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
        loadUsers()
    }

    private fun setupRecyclerView() {
        usersAdapter = UsersAdapter(emptyList()) { user ->
            onUserSelectedListener?.invoke(user)
            dismiss()
        }
        usersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        usersRecyclerView.adapter = usersAdapter
    }

    private fun loadUsers() {
        // Load users from your data source
        val users = getSampleUsers()
        usersAdapter.updateUsers(users)
    }

    private fun getSampleUsers(): List<UserProfile> {
        // Replace with actual data loading
        return listOf(
            UserProfile("1", "Taylor Swift", "@taylorswift", ""),
            UserProfile("2", "Ariana Grande", "@arianagrande", ""),
            UserProfile("3", "BTS", "@bts_official", ""),
            UserProfile("4", "Ed Sheeran", "@edsheeran", "")
        )
    }

    fun setOnUserSelectedListener(listener: (UserProfile) -> Unit) {
        onUserSelectedListener = listener
    }
}