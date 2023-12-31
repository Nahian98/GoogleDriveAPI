package com.android.googledriveapi.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.googledriveapi.R
import com.android.googledriveapi.databinding.ListItemBinding
import com.android.googledriveapi.model.Songs

class SongsAdapter(
    val context: Context,
    private var songs: MutableList<Songs>
) : RecyclerView.Adapter<SongsAdapter.ViewHolder>(){
    var onItemClick: ((Songs) -> Unit)? = null
    class ViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]

        if (song.fileType == "folder"){
            holder.binding.itemImageView.setImageResource(R.drawable.ic_music_folder)
        } else {
            holder.binding.itemImageView.setImageResource(R.drawable.ic_music_file)
        }

        holder.binding.itemNameTextView.text = song.name

        holder.binding.root.setOnClickListener {
            onItemClick?.invoke(song)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(newSongs: MutableList<Songs>) {
        songs.clear()
        songs.addAll(newSongs)
        songs = songs.toSet().toMutableList()
        notifyDataSetChanged()
    }
}