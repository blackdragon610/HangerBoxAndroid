package com.hanger_box.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hanger_box.R;
import com.hanger_box.common.Common;
import com.hanger_box.models.ItemModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class FavoriteRecyclerViewAdapter extends RecyclerView.Adapter<FavoriteRecyclerViewAdapter.ViewHolder> {

    private ArrayList<HashMap> list;
    private LayoutInflater mInflater;
    private FavoriteRecyclerViewAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    public FavoriteRecyclerViewAdapter(Context context, ArrayList<HashMap> data) {
        this.mInflater = LayoutInflater.from(context);
        this.list = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public FavoriteRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.favorite_cell, parent, false);
        return new FavoriteRecyclerViewAdapter.ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull FavoriteRecyclerViewAdapter.ViewHolder holder, int position) {
        HashMap selectedItem = list.get(position);
        ItemModel item1 = (ItemModel) selectedItem.get("item_1");
        ItemModel item2 = (ItemModel) selectedItem.get("item_2");
        Picasso.with(Common.currentActivity)
                .load(item1.getImage())
                .resize(500, 500)
                .centerCrop()
                .into(holder.itemImage1);
        Picasso.with(Common.currentActivity)
                .load(item2.getImage())
                .resize(500, 500)
                .centerCrop()
                .into(holder.itemImage2);
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return list.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView itemImage1;
        ImageView itemImage2;

        ViewHolder(View itemView) {
            super(itemView);
            itemImage1 = itemView.findViewById(R.id.item_image1);
            itemImage2 = itemView.findViewById(R.id.item_image2);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    HashMap getItem(int id) {
        return list.get(id);
    }

    // allows clicks events to be caught
    public void setItemClickListener(FavoriteRecyclerViewAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}