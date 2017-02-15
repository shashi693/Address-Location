package com.avenueinfotech.yourlocation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by suken on 17-11-2016.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    Context context;
    ArrayList<Bitmap> images = new ArrayList<Bitmap>();

    public MyAdapter(Context context) {
        this.context = context;
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_01));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_02));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_03));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_04));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_05));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_06));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_07));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_08));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_09));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_10));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_11));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_12));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_13));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_14));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_15));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_16));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_17));


    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {
        holder.imageView.setImageBitmap(images.get(position));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.my_imageView);
        }
    }
}
