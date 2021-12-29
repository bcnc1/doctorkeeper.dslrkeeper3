/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doctorkeeper.dslrkeeper2022.view.dslr;

import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.doctorkeeper.dslrkeeper2022.R;
import com.doctorkeeper.dslrkeeper2022.madamfive.MadamfiveAPI;
import com.doctorkeeper.dslrkeeper2022.models.PhotoModel;
import com.doctorkeeper.dslrkeeper2022.view.AspectRatioImageView;
import com.doctorkeeper.dslrkeeper2022.view.log_in.LoginDialogFragment;

import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class DSLRPhotoAdapter extends BaseAdapter {

    public static class ViewHolder {
        PhotoModel photo;
        AspectRatioImageView image1;
        TextView filename;
        TextView date;
        public TextView dslr;
        boolean done;

        ImageView thumbView;
        ProgressBar progressBar;
    }

    private List<PhotoModel> items;
    private final LayoutInflater inflater;

    private int handles[] = new int[0];

    private boolean reversed;

    public DSLRPhotoAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setItems(List<PhotoModel> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setReverseOrder(boolean reversed) {
        if (this.reversed == reversed) {
            return;
        }
        this.reversed = reversed;
        notifyDataSetChanged();
    }

    public void setHandles(int handles[]) {
        this.handles = handles;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (items==null) {
            return 0;
        }
        return items.size();
    }


    @Override
    public PhotoModel getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            view = inflater.inflate(R.layout.dslr_item, parent, false);
            ViewHolder holder = new ViewHolder();
            view.setTag(holder);
            holder.image1 = (AspectRatioImageView) view.findViewById(R.id.image1);
            holder.filename = (TextView) view.findViewById(R.id.filename_field);
            holder.date = (TextView) view.findViewById(R.id.date_field);
            holder.progressBar = (ProgressBar) view.findViewById(R.id.thumb_uploading);
            holder.thumbView = (ImageView) view.findViewById(R.id.thumb_uploaded);
            holder.dslr = (TextView) view.findViewById(R.id.textview_dslr);
        }

        final ViewHolder holder = (ViewHolder) view.getTag();
        holder.photo = getItem(position);

        if (holder.photo.getMode()==1) {
            holder.dslr.setVisibility(View.VISIBLE);
        }
        else {
            holder.dslr.setVisibility(View.INVISIBLE);
        }
        holder.progressBar.setVisibility(View.INVISIBLE);
        holder.thumbView.setVisibility(View.VISIBLE);

        Bitmap thumbImage = BitmapFactory.decodeFile(holder.photo.getFullpath() + "_thumb");

        holder.image1.setImageBitmap(thumbImage);
        holder.image1.setExpectedDimensions(120, 120);

        holder.filename.setText(holder.photo.getFilename());

        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String createdDate = df.format(holder.photo.getCreated());
        holder.date.setText(createdDate);
        holder.done = false;

        if (holder.photo.getUploaded() == false) {
            holder.thumbView.setVisibility(View.VISIBLE);

        } else {
            holder.thumbView.setVisibility(View.INVISIBLE);

        }
        holder.thumbView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (MadamfiveAPI.getAccessToken() == null) {
                    showLoginDialog();
                }
                else {
                    savePost(holder.photo, holder);
                }
            }
        });


        return view;
    }



    public void showLoginDialog() {

        FragmentTransaction changelogTx = MadamfiveAPI.getActivity().getFragmentManager().beginTransaction();
        LoginDialogFragment loginDialogFragment = LoginDialogFragment.newInstance();
        changelogTx.add(loginDialogFragment, "로그인");
        changelogTx.commit();

    }

    private void savePost(final PhotoModel photoModel, final DSLRPhotoAdapter.ViewHolder holder) {


        holder.progressBar.setVisibility(View.VISIBLE);
        holder.thumbView.setVisibility(View.INVISIBLE);
        try {
            MadamfiveAPI.createPost(photoModel.getFullpath(), photoModel.getMode().toString(), new JsonHttpResponseHandler() {
                @Override
                public void onStart() {
                    Log.i("CAMERA", "onStart2:");

                }

                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                    Log.d("CAMERA", "HTTP21:" + statusCode + responseString);
                    photoModel.setUploaded(true);
                    photoModel.save();
                    holder.progressBar.setVisibility(View.INVISIBLE);
                    holder.thumbView.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                    // If the response is JSONObject instead of expected JSONArray
                    Log.d("CAMERA", "HTTP22:" + statusCode + response.toString());
                    holder.progressBar.setVisibility(View.INVISIBLE);
                }


                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String response, Throwable th) {
                    // If the response is JSONObject instead of expected JSONArray
                    Log.d("CAMERA", "HTTP22:" + statusCode + response);
                    holder.thumbView.setVisibility(View.VISIBLE);
                    holder.progressBar.setVisibility(View.INVISIBLE);
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
