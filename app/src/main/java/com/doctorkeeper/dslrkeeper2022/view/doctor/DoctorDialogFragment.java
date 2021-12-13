package com.doctorkeeper.dslrkeeper2022.view.doctor;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.doctorkeeper.dslrkeeper2022.view.dslr.DSLRFragment;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.doctorkeeper.dslrkeeper2022.R;
import com.doctorkeeper.dslrkeeper2022.madamfive.MadamfiveAPI;
import com.doctorkeeper.dslrkeeper2022.util.SmartFiPreference;
import com.doctorkeeper.dslrkeeper2022.view.log_in.LoginDialogFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.doctorkeeper.dslrkeeper2022.madamfive.MadamfiveAPI.selectedDoctor;

public class DoctorDialogFragment extends DialogFragment {

    private final String TAG = DoctorDialogFragment.class.getSimpleName();
    private DoctorDialogAdapter adapter;
    private ListView doctorListView;

    private ProgressBar doctor_list_progressBar;

    public static DoctorDialogFragment newInstance() {
        Bundle args = new Bundle();

        DoctorDialogFragment f = new DoctorDialogFragment();
        f.setArguments(args);
        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (MadamfiveAPI.getAccessToken() == null) {
            showLoginDialog();
        }
        if (MadamfiveAPI.getBoardId() == null) {
            showLoginDialog();
        }

        getDialog().setTitle("의사 검색");
        final View view = inflater.inflate(R.layout.activity_search_doctor2, container, false);

        DoctorDialogFragment.this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        final TextView nameTextView;
        final TextView chartNumberTextView;

        nameTextView = (TextView) view.findViewById(R.id.search_doctorname);
        chartNumberTextView = (TextView) view.findViewById(R.id.search_doctorNumber);

        adapter = new DoctorDialogAdapter(getActivity());
        doctorListView = (ListView) view.findViewById(R.id.doctor_list);
        doctorListView.setAdapter(adapter);
        doctor_list_progressBar = (ProgressBar) view.findViewById(R.id.doctor_list_progressBar);

//        read_patientSearchDisplayExtraOption();
//        read_patientInsertExtraOption();

        view.findViewById(R.id.btn_search_doctor).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

//                doctor_list_progressBar.setVisibility(View.VISIBLE);

                String keyword = "";
                final String name = nameTextView.getText().toString();
                final String chartNumber = chartNumberTextView.getText().toString();

                nameTextView.clearFocus();
                chartNumberTextView.clearFocus();

                MadamfiveAPI.searchDoctor(new JsonHttpResponseHandler() {
                    @Override
                    public void onStart() {
                        Log.i(TAG, "onStart:");
                    }

                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                        Log.i(TAG, "HTTPa:" + statusCode + responseString);
//                        doctor_list_progressBar.setVisibility(View.INVISIBLE);

                        try {
                            JSONObject response = new JSONObject(responseString);
                            JSONArray patientArray = response.getJSONArray("posts");

                            if (patientArray.length() == 0) {

                                Toast toast = Toast.makeText(getActivity(), "해당 의사가 없습니다", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, -100);
                                toast.show();

                            } else {

                                ArrayList<HashMap<String, String>> doctorInfoList = new ArrayList<HashMap<String, String>>();
                                Log.i(TAG, "Doctorlist received! === length:" + patientArray.length());

                                for (int i = 0; i < patientArray.length(); i++) {
                                    JSONObject patientObject = patientArray.getJSONObject(i);
                                    HashMap<String, String> doctorInfo = new HashMap<>();
                                    doctorInfo.put("name", patientObject.getString("title").trim());
                                    doctorInfo.put("doctorNumber", patientObject.getString("content"));

                                    if (name != null && name.length() != 0) {
                                        if(doctorInfo.get("name").contains(name)){
                                            doctorInfoList.add(doctorInfo);
                                            Log.i(TAG, "Inside HashMap : " + doctorInfo.toString());
                                        }
                                    }else if (chartNumber != null && chartNumber.length() != 0) {
                                        if(doctorInfo.get("doctorNumber").contains(chartNumber)){
                                            doctorInfoList.add(doctorInfo);
                                        }
                                    }else {
                                        doctorInfoList.add(doctorInfo);
                                    }
                                }
                                adapter.setItems(doctorInfoList);
                                adapter.notifyDataSetChanged();
                            }

                        } catch (Exception e) {
                        }

                        if (statusCode == 400) {
                            Toast toast = Toast.makeText(getActivity(), "해당 의사가 없습니다", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        } else {
                        }

                    }

                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                        // If the response is JSONObject instead of expected JSONArray
                        Log.i(TAG, "HTTPb:" + statusCode + response.toString());
                    }
                });
            }
        });

        doctorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                HashMap<String, String> p = (HashMap<String, String>) adapterView.getItemAtPosition(i);
                selectedDoctor = p;

                String name = selectedDoctor.get("name");
                Toast.makeText(getActivity(), "진료 의사 : "+name , Toast.LENGTH_LONG).show();
                String doctorNumber = selectedDoctor.get("doctorNumber");

                SmartFiPreference.setSfDoctorName(getActivity(),name);
                SmartFiPreference.setSfDoctorNumber(getActivity(),doctorNumber);

                FragmentTransaction ft = getFragmentManager().beginTransaction();
//                ft.replace(R.id.fragment_container, PhoneCameraFragment.newInstance());
                ft.replace(R.id.fragment_container, DSLRFragment.newInstance());
                ft.commit();

                dismiss();
            }
        });

        return view;
    }

    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = (int) (metrics.widthPixels * .85);
        int height = (int) (metrics.heightPixels * .60);
        window.setLayout(width, height);
        window.setGravity(Gravity.CENTER);
    }

    private void showLoginDialog() {

        FragmentTransaction changelogTx = getFragmentManager().beginTransaction();
        LoginDialogFragment loginDialogFragment = LoginDialogFragment.newInstance();
        changelogTx.add(loginDialogFragment, "Login");
        changelogTx.commit();

    }

}