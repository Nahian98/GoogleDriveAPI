package com.android.googledriveapi.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.googledriveapi.R
import com.android.googledriveapi.model.BoxFiles
import com.android.googledriveapi.model.BoxFolders
import com.android.googledriveapi.model.BoxItems
import com.android.googledriveapi.model.Songs


class BoxAdapter(private val items: List<BoxItems>) :
    RecyclerView.Adapter<BoxAdapter.ItemViewHolder>() {
    var onItemClick: ((BoxItems) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val folderView = inflater.inflate(R.layout.list_item, parent, false)
        return ItemViewHolder(folderView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.nameTextView.text = item.name
        holder.modifiedByTextView.text = item.modifiedBy
        if (item is BoxFiles) {
            holder.imageView.setImageResource(R.drawable.ic_music_folder)
        } else if (item is BoxFolders) {
            holder.imageView.setImageResource(R.drawable.ic_folder_64)
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nameTextView: TextView
        var modifiedByTextView: TextView
        var imageView: ImageView

        init {
            nameTextView = itemView.findViewById(R.id.item_name_text_view)
            modifiedByTextView = itemView.findViewById(R.id.modified_by_text_view)
            imageView = itemView.findViewById(R.id.item_image_view)
        }
    }
}



