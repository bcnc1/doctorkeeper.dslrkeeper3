package com.doctorkeeper.dslrkeeper2022.view.cloud;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.doctorkeeper.dslrkeeper2022.R;
import com.doctorkeeper.dslrkeeper2022.madamfive.MadamfiveAPI;
import com.doctorkeeper.dslrkeeper2022.view.BaseFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import static com.loopj.android.http.AsyncHttpClient.log;


public class CloudGalleryFragment extends BaseFragment implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener{

    private final Handler handler = new Handler();

    @BindView(R.id.cloud_list)
    GridView galleryView;

    private CloudGalleryAdapter cloudGalleryAdapter;

    private SimpleDateFormat formatParser;
    private int currentScrollState;
    HashMap<String,String> pictureMap;

    @BindView(R.id.cloud_empty_textview)
    TextView emptyView;

    private ArrayList<HashMap<String,String>> imageInfoList;
    private final String TAG = CloudGalleryFragment.class.getSimpleName();

    public static CloudGalleryFragment newInstance() {
        CloudGalleryFragment f = new CloudGalleryFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cloud_gallery_frag, container, false);
        ButterKnife.bind(this, view);

        cloudGalleryAdapter = new CloudGalleryAdapter(getActivity());
        getImagesList();
        enableUi(true);
        return view;
    }

    public void enableUi(final boolean enabled) {
        galleryView.setEnabled(enabled);
//        Log.i(TAG, "Cloud enableUi..." + enabled);
        if (getActivity()==null)
            return;

        (getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
            if (cloudGalleryAdapter==null)
                return;

            cloudGalleryAdapter.setItems(imageInfoList);
            galleryView.setAdapter(cloudGalleryAdapter);
            }
        });
        galleryView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        pictureMap = new HashMap<>();
        pictureMap = cloudGalleryAdapter.getItem(position);
//        log.i("pictureMap", String.valueOf(pictureMap));
        if(pictureMap.get("cameraKind") == "Video"){
            Toast.makeText(getActivity(), "비디오파일은 미리보기가 제공되지 않습니다!", Toast.LENGTH_SHORT).show();
        } else{
            String imageUrl = pictureMap.get("fileName");
            String imageGuid = pictureMap.get("guid");
//            log.i("imageUrl", imageUrl);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.cloud_detail_container, CloudPictureFragment.newInstance(cloudGalleryAdapter.getItemHandle(position), imageUrl, imageGuid), null);
            ft.addToBackStack(null);
            ft.commit();
        }

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        currentScrollState = scrollState;

        switch (scrollState) {
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE: {
                for (int i = 0; i < galleryView.getChildCount(); ++i) {
                    View child = view.getChildAt(i);
                    if (child == null) {
                        continue;
                    }
                    CloudGalleryAdapter.ViewHolder holder = (CloudGalleryAdapter.ViewHolder) child.getTag();
                    if (!holder.done) {
                        holder.done = true;
//                        camera.retrieveImageInfo(this, holder.objectHandle);
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
    }


    private void getImagesList(){

        imageInfoList = new ArrayList<HashMap<String, String>>();
        MadamfiveAPI.getImageLists(MadamfiveAPI.getContext(), new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                Log.v(TAG,"response length:::"+response.length());
                for(int i=0;i<response.length();i++){
                    try {
                        JSONObject j = response.getJSONObject(i);
                        HashMap<String,String> h = new HashMap<>();
                        h.put("fileName",j.getString("name"));
                        h.put("created",j.getString("last_modified"));
                        h.put("bytes",j.getString("bytes"));
                        imageInfoList.add(h);
                    }catch(Exception e){}
                }
                Collections.sort(imageInfoList, new Comparator<HashMap< String,String >>() {
                    @Override
                    public int compare(HashMap<String, String> lhs,
                                       HashMap<String, String> rhs) {
                        String firstValue = lhs.get("created");
                        String secondValue = rhs.get("created");
                        return secondValue.compareTo(firstValue);
                    }
                });
//                if(imageInfoList.size()>20){
//                    imageInfoList = (ArrayList<HashMap<String, String>>) imageInfoList.subList(0,20);
//                }
                cloudGalleryAdapter.setItems(imageInfoList);
                cloudGalleryAdapter.notifyDataSetChanged();
//                Log.i("imageInfoList", String.valueOf(imageInfoList));

                Log.i("CloudFragment","list received! === length:"+imageInfoList.size());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.v(TAG,"responseString:::"+responseString);
            }
        });

        Log.i("List in CloudFragment",imageInfoList.size()+"");
    }


}
