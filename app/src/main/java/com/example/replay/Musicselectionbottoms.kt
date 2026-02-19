package com.example.replay

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MusicSelectionBottomSheet : BottomSheetDialogFragment() {

    private lateinit var searchInput: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: MusicSelectionAdapter

    private val songs = mutableListOf<ITunesSong>()
    private var onMusicSelected: ((ITunesSong) -> Unit)? = null

    fun setOnMusicSelectedListener(listener: (ITunesSong) -> Unit) {
        onMusicSelected = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_music_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchInput = view.findViewById(R.id.searchInput)
        recyclerView = view.findViewById(R.id.musicRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        setupRecyclerView()
        loadDefaultSongs()
        setupSearch()
    }

    private fun setupRecyclerView() {
        adapter = MusicSelectionAdapter(songs) { song ->
            onMusicSelected?.invoke(song)
            dismiss()
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun loadDefaultSongs() {
        searchSongs("top hits 2024")
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.length >= 2) {
                    searchSongs(query)
                } else if (query.isEmpty()) {
                    loadDefaultSongs()
                }
            }
        })
    }

    private fun searchSongs(query: String) {
        if (!isAdded) return
        progressBar.visibility = View.VISIBLE

        RetrofitClient.api.searchSongs(query, limit = 25)
            .enqueue(object : Callback<ITunesResponse> {
                override fun onResponse(
                    call: Call<ITunesResponse>,
                    response: Response<ITunesResponse>
                ) {
                    if (!isAdded) return
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val results = response.body()?.results ?: emptyList()
                        songs.clear()
                        songs.addAll(results)
                        adapter.notifyDataSetChanged()
                        Log.d("MusicSelection", "Loaded ${results.size} songs for '$query'")
                    } else {
                        loadFallback()
                    }
                }

                override fun onFailure(call: Call<ITunesResponse>, t: Throwable) {
                    if (!isAdded) return
                    progressBar.visibility = View.GONE
                    Log.e("MusicSelection", "Failed to load songs", t)
                    loadFallback()
                }
            })
    }

    private fun loadFallback() {
        songs.clear()
        songs.addAll(SampleData.sampleSongs)
        adapter.notifyDataSetChanged()
        Toast.makeText(requireContext(), "Showing offline songs", Toast.LENGTH_SHORT).show()
    }
}