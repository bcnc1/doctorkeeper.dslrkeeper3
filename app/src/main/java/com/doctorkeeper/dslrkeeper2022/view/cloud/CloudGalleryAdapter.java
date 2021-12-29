package com.doctorkeeper.dslrkeeper2022.view.cloud;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.doctorkeeper.dslrkeeper2022.Constants;
import com.doctorkeeper.dslrkeeper2022.R;
import com.doctorkeeper.dslrkeeper2022.madamfive.MadamfiveAPI;
import com.doctorkeeper.dslrkeeper2022.util.SmartFiPreference;
import com.doctorkeeper.dslrkeeper2022.view.AspectRatioImageView;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import com.bumptech.glide.Glide;

import static com.loopj.android.http.AsyncHttpClient.log;


public class CloudGalleryAdapter extends BaseAdapter {

    private static final String TAG = "CloudGalleryAdapter";

    public static class ViewHolder {
        HashMap<String,String> photo;
        AspectRatioImageView image1;
//        ImageView image1;
        TextView filename;
        TextView date;
        public TextView dslr;
        boolean done;

        ImageView thumbView;
        ProgressBar progressBar;
    }

//    private List<PhotoModel> items;
    private List<HashMap<String,String>> items;
    private final LayoutInflater inflater;
    String accessToken;
    private Context mContext;
    private boolean reversed;
    private int handles[] = new int[0];

    public CloudGalleryAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext=context;
    }

    public void setItems(List<HashMap<String,String>> items) {
        this.items = items;
//        Log.w(TAG,"갯수 = "+items);
        notifyDataSetChanged();
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
        if (items==null) {
            return 0;
        }
        return items.size();
    }

    public void setHandles(int handles[]) {
        this.handles = handles;
        notifyDataSetChanged();
    }

    public int getItemHandle(int position) {
//        return handles[reversed ? handles.length - position - 1 : position];
        return position;
    }

    @Override
    public HashMap<String,String> getItem(int position) {
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
//            holder.dslr = (TextView) view.findViewById(R.id.textview_dslr);
        }

        final ViewHolder holder = (ViewHolder) view.getTag();
        holder.photo = getItem(position);
//        log.i("holder.photo.get(\"fileName\")", String.valueOf(holder.photo.get("fileName")));
        String filename = holder.photo.get("fileName");
        holder.progressBar.setVisibility(View.INVISIBLE);
//        accessToken = SmartFiPreference.getSfToken(getContext());

        String token = SmartFiPreference.getSfToken(MadamfiveAPI.getActivity());
        String hostipalId = SmartFiPreference.getHospitalId(MadamfiveAPI.getActivity());
        String imageUrl = Constants.Storage.BASE_URL+"/"+hostipalId+"/"+filename;
        GlideUrl glideUrl = new GlideUrl(imageUrl, new LazyHeaders.Builder()
                .addHeader("X-Auth-Token", token).build());
        Glide.with(mContext).load(glideUrl).centerCrop().into(holder.image1);
        holder.image1.setExpectedDimensions(120, 120);

        String d1 = holder.photo.get("fileName");
        holder.date.setText("");
        try {
            String[] d2 = d1.split("_");
            String d3 = d2[2];
            String d4 = d3.substring(5, 15);
            String d5 = d4.replaceAll("-", " ");
            String d6 = d5.replaceFirst(" ", "-");
            String d7 = d6.substring(0, 8) + ":" + d6.substring(8, 10);
            holder.date.setText(d7);
        }catch(Exception e){}
        holder.done = false;

        return view;
    }

}
