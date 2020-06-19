package com.rstudios.simplesortingtask;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class ListRecyclerAdapter extends RecyclerView.Adapter<ViewHolder> {
    private Context context;
    private ArrayList<ListItem> arrayList;

    public ListRecyclerAdapter(Context context, ArrayList<ListItem> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_list,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListItem listItem=arrayList.get(position);
        holder.title.setText(listItem.getTitle());
        holder.desc.setText(listItem.getDesc());
        holder.source.setText(listItem.getSource());
        Glide.with(context).load(listItem.getImgUrl()).placeholder(R.drawable.ic_image).centerCrop().into(holder.imageView);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context,NewsActivity.class);
                intent.putExtra("url",listItem.getPageUrl());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}

class ViewHolder extends RecyclerView.ViewHolder{
    TextView title,desc,source;
    ShapeableImageView imageView;
    View view;
    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        title=itemView.findViewById(R.id.item_title);
        desc=itemView.findViewById(R.id.item_desc);
        source=itemView.findViewById(R.id.item_source);
        imageView=itemView.findViewById(R.id.item_image);
        view=itemView;
    }
}