/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doctorkeeper.dslrkeepervegas.view.sdcard;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.doctorkeeper.dslrkeepervegas.R;
import com.doctorkeeper.dslrkeepervegas.view.AspectRatioImageView;


public class GalleryAdapter extends BaseAdapter {

    private static final String TAG = "GalleryAdapter";

    public static class ViewHolder {
        int objectHandle;
        AspectRatioImageView image1;
        TextView filename;
        TextView dimension;
        TextView date;
        boolean done;
        TextView sdcard_image_upload_check;
        TextView sdcard_image_need_upload;
    }

    private int handles[] = new int[0];
    private final LayoutInflater inflater;
    private int thumbWidth;
    private int thumbHeight;
    private boolean reversed=true;
    private final GalleryFragment galleryFragment;


    public GalleryAdapter(Context context, GalleryFragment galleryFragment) {
        this.galleryFragment = galleryFragment;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setHandles(int handles[]) {
        Log.w(TAG,"setHandles = "+handles.length);
        this.handles = handles;
        notifyDataSetChanged();

//        for(int k=0;k<handles.length;k++) {
//            multiSelectionMap.put(handles[k], false);
//        }
    }

    public void setReverseOrder(boolean reversed) {
        if (this.reversed == reversed) {
            return;
        }
        this.reversed = reversed;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {

// 50개씩만 읽어오는 장치
        if(handles.length>30)     return 30;
        return handles.length;
    }

    public int getItemHandle(int position) {
        return handles[reversed ? handles.length - position - 1 : position];
    }

    @Override
    public Integer getItem(int position) {
        return handles[reversed ? handles.length - position - 1 : position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Log.d(TAG,"getView");

        View view = convertView;

        final ViewHolder holder;

        if (view == null) {
            Log.d(TAG,"getView => view == null");
            view = inflater.inflate(R.layout.gallery_list_item, parent, false);
            holder = new ViewHolder();

            holder.image1 = (AspectRatioImageView) view.findViewById(R.id.image1);

            holder.date = (TextView) view.findViewById(R.id.date_field);

            holder.sdcard_image_upload_check = (TextView) view.findViewById(R.id.sdcard_image_upload_check);
            holder.sdcard_image_need_upload = (TextView) view.findViewById(R.id.sdcard_image_need_upload);
            view.setTag(holder);
        } else{
            holder = (ViewHolder)view.getTag();
        }


        holder.image1.setImageBitmap(null);
        holder.image1.setExpectedDimensions(thumbWidth, thumbHeight);
        holder.objectHandle = getItemHandle(position);
        holder.date.setText("");
        holder.done = false;

        galleryFragment.onNewListItemCreated(holder);
        return view;
    }

    public void setThumbDimensions(int width, int height) {
        this.thumbWidth = width;
        this.thumbHeight = height;
    }





}
