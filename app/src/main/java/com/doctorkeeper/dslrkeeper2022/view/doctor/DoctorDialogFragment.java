package com.doctorkeeper.dslrkeeper2022.view.doctor;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
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

import com.doctorkeeper.dslrkeeper2022.madamfive.BlabAPI;
import com.doctorkeeper.dslrkeeper2022.view.dslr.DSLRFragment;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.doctorkeeper.dslrkeeper2022.R;
import com.doctorkeeper.dslrkeeper2022.madamfive.MadamfiveAPI;
import com.doctorkeeper.dslrkeeper2022.util.SmartFiPreference;
import com.doctorkeeper.dslrkeeper2022.view.log_in.LoginDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

import static com.doctorkeeper.dslrkeeper2022.madamfive.MadamfiveAPI.selectedDoctor;

public class DoctorDialogFragment extends DialogFragment {

    private final String TAG = DoctorDialogFragment.class.getSimpleName();
    private DoctorDialogAdapter adapter;
    private ListView doctorListView;
    private String doctorName;
    private String doctorNumber;
    private ProgressBar doctor_list_progressBar;
    private TextView nameTextView;
    private TextView chartNumberTextView;

    public static DoctorDialogFragment newInstance() {
        Bundle args = new Bundle();

        DoctorDialogFragment f = new DoctorDialogFragment();
        f.setArguments(args);
        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ArrayList<HashMap<String, String>> DoctorInfoList = new ArrayList<HashMap<String, String>>();
        if (MadamfiveAPI.getAccessToken() == null) {
            showLoginDialog();
        }
        if (MadamfiveAPI.getBoardId() == null) {
            showLoginDialog();
        }

        getDialog().setTitle("의사 검색");
        final View view = inflater.inflate(R.layout.activity_search_doctor2, container, false);

        DoctorDialogFragment.this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);


        nameTextView = (TextView) view.findViewById(R.id.search_doctorname);
        chartNumberTextView = (TextView) view.findViewById(R.id.search_doctorNumber);

        adapter = new DoctorDialogAdapter(getActivity());
        doctorListView = (ListView) view.findViewById(R.id.doctor_list);
        doctorListView.setAdapter(adapter);
        doctor_list_progressBar = view.findViewById(R.id.doctor_list_progressBar);

//        read_patientSearchDisplayExtraOption();
//        read_patientInsertExtraOption();

        view.findViewById(R.id.btn_search_doctor).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick");
//                doctor_list_progressBar.setVisibility(View.VISIBLE);

                String keyword = "";
                doctorName = nameTextView.getText().toString();
                doctorNumber = chartNumberTextView.getText().toString();
                Log.i(TAG, "doctorName : " + doctorName);
                Log.i(TAG, "doctorNumber : " + doctorNumber);
                if ((doctorName == null || doctorName.length() == 0)) {
                    Toast toast = Toast.makeText(getActivity(), "의사명을 입력해주세요", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, -100);
                    toast.show();
//                    doctor_list_progressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                nameTextView.clearFocus();
                chartNumberTextView.clearFocus();
                MadamfiveAPI.getDoctorList(doctorName, doctorNumber, new JsonHttpResponseHandler() {
                    @Override
                    public void onStart() {
                        Log.i(TAG, "onStart:");
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        doctor_list_progressBar.setVisibility(View.INVISIBLE);
                        // If the response is JSONObject instead of expected JSONArray
                        Log.i(TAG, "HTTPb:" + statusCode + response.toString());
                    }
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                        // If the response is JSONObject instead of expected JSONArray
                        Log.i(TAG, "HTTPc:" + statusCode + response.toString());
                        Log.i(TAG, "statusCode: " + statusCode);
                        Log.i(TAG, "response.length(): " + response.length());

                        if (DSLRFragment.doctorSelectExtraOption && response.length() == 0) {
                            addDoctorInfo(doctorName, doctorNumber);
//                            doctor_list_progressBar.setVisibility(View.INVISIBLE);
//                        Toast toast = Toast.makeText(getActivity(), "해당 의사가 없습니다", Toast.LENGTH_LONG);
//                        toast.setGravity(Gravity.CENTER, 0, 0);
//                        toast.show();
                        }

                        for(int i=0;i<response.length();i++){
                            HashMap<String,String> h = new HashMap<>();
                            try {
                                JSONObject j = response.getJSONObject(i);
                                h.put("name", j.getString("name").trim());
                                h.put("doctorNumber", j.getString("licenseNo").trim());
                                DoctorInfoList.add(h);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        adapter.setItems(DoctorInfoList);
                        adapter.notifyDataSetChanged();

//                        doctor_list_progressBar.setVisibility(View.INVISIBLE);
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

    private void addDoctorInfo(String inputName, String inputNumber) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("스마트파이");
        builder.setMessage("해당 의사가 없습니다. 추가하시겠습니까?");

        builder.setPositiveButton("YES", (dialog, which) -> {


            if (doctorName == null || doctorName.length() == 0) {
                Toast.makeText(getActivity(), "의사이름을 입력해 주세요", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            if (doctorNumber == null || doctorNumber.length() == 0) {
                Toast.makeText(getActivity(), "의사번호를 입력해 주세요", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }
            final String doctorName = nameTextView.getText().toString();
            final String doctorNumber = chartNumberTextView.getText().toString();
            Log.i(TAG, "Doctor name : " + doctorName);
            Log.i(TAG, "Doctor number : " + doctorNumber);
            MadamfiveAPI.insertDoctor(BlabAPI.getContext(), doctorName, doctorNumber, new JsonHttpResponseHandler() {
                @Override
                public void onStart() {
                    Log.i(TAG, "onStart: Insert Patient");
                }
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // If the response is JSONObject instead of expected JSONArray
                    Log.i(TAG, "HTTPb:" + statusCode + response.toString());
                    if (statusCode == 202) {
                        Toast.makeText(getActivity(), "의사번호 중복", Toast.LENGTH_SHORT).show();
                    }
                    if (statusCode == 200) {
                        try {
                            Toast.makeText(getActivity(), "진료의사 : " + doctorName, Toast.LENGTH_SHORT).show();
                            SmartFiPreference.setSfDoctorName(getActivity(), doctorName);
                            SmartFiPreference.setSfDoctorNumber(getActivity(),doctorNumber);
                            dismiss();
                            dialog.dismiss();

                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.replace(R.id.fragment_container, DSLRFragment.newInstance());
                            ft.commit();

                        } catch (Exception e) {
                        }
                    }
                }
            });


        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
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
