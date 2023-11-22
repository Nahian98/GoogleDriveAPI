package com.android.googledriveapi.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.android.googledriveapi.R;
import com.android.googledriveapi.model.BoxFiles;
import com.android.googledriveapi.model.BoxFolders;
import com.android.googledriveapi.model.BoxItems;

import java.util.List;

public class BoxItemAdapter extends RecyclerView.Adapter<BoxItemAdapter.ItemViewHolder> {
    private final List<BoxItems> items;

    public BoxItemAdapter(List<BoxItems> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View folderView = inflater.inflate(com.android.googledriveapi.R.layout.list_item, parent, false);

        return new ItemViewHolder(folderView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        BoxItems item = items.get(position);
        holder.nameTextView.setText(item.getName());
        holder.modifiedByTextView.setText(item.getModifiedBy());
        if (item instanceof BoxFiles) {
            holder.imageView.setImageResource(R.drawable.ic_music_folder);
        }
        else if (item instanceof BoxFolders) {
            holder.imageView.setImageResource(R.drawable.ic_folder_64);
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView modifiedByTextView;
        public ImageView imageView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.item_name_text_view);
            modifiedByTextView = itemView.findViewById(R.id.modified_by_text_view);
            imageView = itemView.findViewById(R.id.item_image_view);
        }
    }
}
