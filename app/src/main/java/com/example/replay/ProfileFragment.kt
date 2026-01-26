package com.example.replay

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    private lateinit var imgProfile: ImageView

    // 🔹 Image picker launcher
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imgProfile.setImageURI(it)
                saveImageUri(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgProfile = view.findViewById(R.id.imgProfile)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvHandle = view.findViewById<TextView>(R.id.tvHandle)
        val tvPosts = view.findViewById<TextView>(R.id.tvPosts)
        val tvLikes = view.findViewById<TextView>(R.id.tvLikes)
        val tvStreak = view.findViewById<TextView>(R.id.tvStreak)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // Fake user data
        tvName.text = "Uri"
        tvHandle.text = "@uri"
        tvPosts.text = "3\nPosts"
        tvLikes.text = "12\nLikes"
        tvStreak.text = "5\nStreak"

        loadSavedImage()

        // 📸 Tap image to change
        imgProfile.setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnLogout.setOnClickListener {
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
        }
    }

    // 💾 Save image URI
    private fun saveImageUri(uri: Uri) {
        val prefs = requireContext()
            .getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

        prefs.edit()
            .putString("profile_image", uri.toString())
            .apply()
    }

    // 🔁 Load saved image
    private fun loadSavedImage() {
        val prefs = requireContext()
            .getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

        val uriString = prefs.getString("profile_image", null)
        uriString?.let {
            imgProfile.setImageURI(Uri.parse(it))
        }
    }
}
