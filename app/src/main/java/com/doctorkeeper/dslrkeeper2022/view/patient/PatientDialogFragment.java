package com.doctorkeeper.dslrkeeper2022.view.patient;

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
import com.doctorkeeper.dslrkeeper2022.Constants;
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


public class PatientDialogFragment extends DialogFragment {

    private final String TAG = PatientDialogFragment.class.getSimpleName();
    private ArrayList<HashMap<String, String>> patientInfoList;
    private PatientDialogAdapter adapter;
    private ListView patientListView;

    private ProgressBar patient_list_progressBar;
    private boolean patientInsertExtraOption = false;
    private boolean patientSearchDisplayExtraOption = false;
    private String name;
    private String chartNumber;
    private TextView nameTextView;
    private TextView chartNumberTextView;
    private Boolean fixedLandscapeExtraOption;

    public static PatientDialogFragment newInstance() {
        Bundle args = new Bundle();

        PatientDialogFragment f = new PatientDialogFragment();
        f.setArguments(args);
        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        patientInsertExtraOption = SmartFiPreference.getSfInsertPatientOpt(getActivity());
        patientSearchDisplayExtraOption = SmartFiPreference.getSfInsertPatientOpt(getActivity());
        //환자검색버튼을 누르면 해당 다이얼로그 호출
        if(SmartFiPreference.getSfToken(getActivity()).equals(Constants.EMRAPI.UNDEFINED)){
            showLoginDialog();
        }

        getDialog().setTitle("환자 검색");
        final View view = inflater.inflate(R.layout.activity_search_patient, container, false);
        PatientDialogFragment.this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        nameTextView = (TextView) view.findViewById(R.id.search_name);
        chartNumberTextView = (TextView) view.findViewById(R.id.search_chartNumber);

        adapter = new PatientDialogAdapter(getActivity());
        patientListView = (ListView) view.findViewById(R.id.patient_list);
        patientListView.setAdapter(adapter);
        patient_list_progressBar = (ProgressBar) view.findViewById(R.id.patient_list_progressBar);

        view.findViewById(R.id.btn_search_patient).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if(MadamfiveAPI.getNetworkStatus(MadamfiveAPI.getActivity())){
                    patient_list_progressBar.setVisibility(View.VISIBLE);

                    String keyword = "";
                    name = nameTextView.getText().toString();
                    chartNumber = chartNumberTextView.getText().toString();
                    boolean searchName = true;

                    if (name != null && name.length() != 0) {
                        keyword = name;
                    }

                    if (chartNumber != null && chartNumber.length() != 0) {
                        keyword = chartNumber;
                        searchName = false;
                    }
                    if (keyword == null || keyword.length() == 0) {
                        Toast.makeText(getActivity(), "환자명 또는 차트번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                        patient_list_progressBar.setVisibility(View.INVISIBLE);
                    }

                    nameTextView.clearFocus();
                    chartNumberTextView.clearFocus();
                    final String loginCheck = keyword;

                    searchPatientOpt(name, chartNumber);
                }else {
                    Toast.makeText(MadamfiveAPI.getActivity(), getString(R.string.check_network), Toast.LENGTH_SHORT).show();
                }
            }
        });

        patientListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                HashMap<String, String> patientInfo = (HashMap<String, String>) adapterView.getItemAtPosition(i);
                String name = patientInfo.get("name");
                Toast.makeText(getActivity(), name + "님이 선택되었습니다", Toast.LENGTH_LONG).show();
//
//                SmartFiPreference.setPatientId(getActivity(), patientInfo.get("categoryId"));
//                SmartFiPreference.setSfPatientCustNo(getActivity(),patientInfo.get("custNo"));
                SmartFiPreference.setSfPatientName(getActivity(), name);
                SmartFiPreference.setPatientChart(getActivity(),patientInfo.get("chrtNo"));
                Log.i(TAG, "getSfPatientName: " + SmartFiPreference.getSfPatientName(MadamfiveAPI.getActivity()));
                Log.i(TAG, "getPatientChart: " + SmartFiPreference.getPatientChart(MadamfiveAPI.getActivity()));
//                SmartFiPreference.setSfPatientCustNo(getActivity(), patientInfo.get("custNo"));
//                SmartFiPreference.setPatientChart(getActivity(),patientInfo.get("chartNumber"));
//                SmartFiPreference.setSfPatientName(getActivity(),name);

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, DSLRFragment.newInstance());
//                ft.addToBackStack(null);
                ft.commit();

                dismiss();
            }
        });

        return view;
    }

    private void searchPatientOpt(final String searchName, final String searchChart) {

        ArrayList<HashMap<String, String>> patientInfoList = new ArrayList<HashMap<String, String>>();

        if (searchChart == null || searchChart.length() == 0 && searchName == null || searchName.length() == 0) {
            Toast.makeText(getActivity(), "이름 또는 차트번호를 입력해 주세요", Toast.LENGTH_SHORT).show();
            patient_list_progressBar.setVisibility(View.INVISIBLE);
            return;

        }

        MadamfiveAPI.getPatientList(searchName, searchChart, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                super.onSuccess(statusCode, headers, response);

                Log.i(TAG, "statusCode: " + statusCode);
                Log.i(TAG, "response.length(): " + response.length());
                Log.i(TAG, "patientInsertExtraOption:  " + patientInsertExtraOption);
                if (patientInsertExtraOption && response.length() == 0) {
                    addPatientInfo(searchName, searchChart);
                } else if (!patientInsertExtraOption && response.length() == 0) {
                    Toast toast = Toast.makeText(getActivity(), "해당 환자가 없습니다", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                patient_list_progressBar.setVisibility(View.INVISIBLE);
                for(int i=0;i<response.length();i++){
                    HashMap<String,String> h = new HashMap<>();
                    try {
                        JSONObject j = response.getJSONObject(i);
                        h.put("name", j.getString("name").trim());
                        h.put("chrtNo", j.getString("chrtNo").trim());
                        patientInfoList.add(h);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapter.setItems(patientInfoList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.v(TAG,responseString);
                patient_list_progressBar.setVisibility(View.INVISIBLE);
                Toast toast = Toast.makeText(getActivity(), "no Patient", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

    }

    private void searchPatient(final String searchName, final String searchChart) {

        MadamfiveAPI.searchPatient(searchName, searchChart, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                Log.i(TAG, "searchPatient onStart:");
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                patient_list_progressBar.setVisibility(View.INVISIBLE);

                try {
                    JSONObject response = new JSONObject(responseString);
                    JSONArray patientArray = response.getJSONArray("categories");

                    if (patientInsertExtraOption == true && patientArray.length() == 0) {
                        addPatientInfo(searchName, searchChart);
                    } else {
                        ArrayList<HashMap<String, String>> patientInfoList = new ArrayList<HashMap<String, String>>();
                        for (int i = 0; i < patientArray.length(); i++) {
                            JSONObject patientObject = patientArray.getJSONObject(i);
                            Log.i(TAG, "Inside patientObject : " + patientObject.toString());
                            HashMap<String, String> patientInfo = new HashMap<>();
                            if(patientSearchDisplayExtraOption) {

                                patientInfo.put("name", patientObject.getString("name").trim());
                                patientInfo.put("chartNumber", patientObject.getString("parentId"));
                                patientInfo.put("categoryId", patientObject.getString("id"));
                                patientInfo.put("customerNumber", patientObject.getString("description"));
                                try {
                                    JSONObject userData = patientObject.getJSONObject("userData");
                                    patientInfo.put("birthDate", userData.getString("birthDate"));
                                }catch(Exception e){
                                }
                            }else{
                                patientInfo.put("name", patientObject.getString("name").trim());
                                patientInfo.put("chartNumber", patientObject.getString("parentId"));
                                patientInfo.put("categoryId", patientObject.getString("id"));
                                patientInfo.put("customerNumber", patientObject.getString("description"));
                            }
//                            Log.i(TAG, "Inside HashMap : " + patientInfo.toString());
                            patientInfoList.add(patientInfo);
                        }
//                        Log.i(TAG, "list received! === length:" + patientInfoList.size());
                        adapter.setItems(patientInfoList);
                        adapter.notifyDataSetChanged();
                    }

                } catch (Exception e) {
                }

                if (statusCode == 400) {
                    Toast toast = Toast.makeText(getActivity(), "해당 환자가 없습니다", Toast.LENGTH_LONG);
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

    private void addPatientInfo(String inputName, String inputChart) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("스마트파이");
        builder.setMessage("해당 환자가 없습니다. 추가하시겠습니까?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, int which) {
                final String name = nameTextView.getText().toString();
                final String chartNumber = chartNumberTextView.getText().toString();

                if (name == null || name.length() == 0) {
                    Toast.makeText(getActivity(), "이름을 입력해 주세요", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }

                if (chartNumber == null || chartNumber.length() == 0) {
                    Toast.makeText(getActivity(), "차트번호를 입력해 주세요", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }

                MadamfiveAPI.insertPatient(BlabAPI.getContext(), name, chartNumber, new JsonHttpResponseHandler() {
                @Override
                public void onStart() {
                    Log.i(TAG, " onStart: insertPatient ");
                }
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // If the response is JSONObject instead of expected JSONArray
                    Log.i(TAG, "HTTPb:" + statusCode + response.toString());
                    if (statusCode == 202) {
                        Toast.makeText(getActivity(), "차트번호 중복", Toast.LENGTH_SHORT).show();
                    }
                    if (statusCode == 200) {
                        try {
                            Toast.makeText(getActivity(), name + "님이 선택되었습니다", Toast.LENGTH_SHORT).show();
                            SmartFiPreference.setSfPatientName(getActivity(), name);
                            SmartFiPreference.setPatientChart(getActivity(),chartNumber);
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


            }
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
        fixedLandscapeExtraOption = SmartFiPreference.getSfDisplayLandscapeOpt(MadamfiveAPI.getActivity());
//        if(fixedLandscapeExtraOption){
//            height = (int) (metrics.heightPixels * .90);
//            width= (int) (metrics.widthPixels * .60);
//        }else {
//            height = (int) (metrics.heightPixels * .60);
//            width= (int) (metrics.widthPixels * .85);
//        }
//        window.setLayout(width, height);
//        window.setGravity(Gravity.CENTER);
        window.setLayout(550, 900);
        window.setGravity(Gravity.CENTER);
    }

    private void showLoginDialog() {

        SmartFiPreference.setDoctorId(getActivity(), Constants.EMRAPI.UNDEFINED);
        SmartFiPreference.setSfDoctorPw(getActivity(),Constants.EMRAPI.UNDEFINED);

        FragmentTransaction changelogTx = getFragmentManager().beginTransaction();
        LoginDialogFragment loginDialogFragment = LoginDialogFragment.newInstance();
        changelogTx.add(loginDialogFragment, "Login");
        changelogTx.commit();

    }

}
