package com.doctorkeeper.dslrkeeper2022.view.patient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.doctorkeeper.dslrkeeper2022.R;
import com.doctorkeeper.dslrkeeper2022.madamfive.MadamfiveAPI;
import com.doctorkeeper.dslrkeeper2022.util.SmartFiPreference;

import java.util.HashMap;
import java.util.List;

//import static com.doctorkeeper.dslrkeeper.madamfive.MadamfiveAPI.patientSearchDisplayExtraOption;


public class PatientDialogAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<HashMap<String,String>> items;
    private TextView patient_name;
    private TextView patient_chartNumber;
    private Boolean patientSearchDisplayExtraOption;

    public PatientDialogAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setItems(List<HashMap<String,String>> items) {
        this.items = items;
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
    public HashMap<String,String> getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        view = inflater.inflate(R.layout.patient_item, viewGroup, false);

        patient_name = (TextView) view.findViewById(R.id.patient_name);
        patient_chartNumber = (TextView) view.findViewById(R.id.patient_chartNumber);

        patientSearchDisplayExtraOption = SmartFiPreference.getSfDisplayExtraOpt(MadamfiveAPI.getActivity());
        HashMap<String, String> patientInfo = getItem(i);
        if(patientSearchDisplayExtraOption){
            patient_name.setText(patientInfo.get("name"));
            if(patientInfo.get("birthDate") == null){
                patient_chartNumber.setText("");
            }else{
                patient_chartNumber.setText(patientInfo.get("birthDate"));
            }
        }else {
            String name = patientInfo.get("name");
//            Log.i("TAG", "name : " + name);
            if(name==null){
                name = "";
            }
            patient_name.setText(name);

            String chartNumber = patientInfo.get("chrtNo");
//            Log.i("TAG", "chartNumber : " + chartNumber);
            if(chartNumber==null){
                chartNumber = "";
            }

            patient_chartNumber.setText(chartNumber);
        }
        if(getCount()==0)   patient_name.setText("결과 없음");

        return view;
    }
}
