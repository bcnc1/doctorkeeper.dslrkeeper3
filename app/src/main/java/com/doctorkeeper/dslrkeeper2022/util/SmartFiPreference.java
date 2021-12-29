package com.doctorkeeper.dslrkeeper2022.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.doctorkeeper.dslrkeeper2022.Constants;

public class SmartFiPreference {
    public static final String SF_TOKEN = "smartfi.token";
    public static final String SF_HOSPITAL_ID = "smartfi.hospital.id";
    public static final String SF_DOCTOR_ID = "smartfi.doctor.id";
    public static final String SF_PATIENT_ID = "smartfi.patient.id";
    public static final String SF_PATIENT_CHART = "smartfi.patient.chart";
    public static final String SF_DOCTOR_PW = "smartfi.doctor.password";
//    public static final String SF_PATIENT_CATEGORYID = "smartfi.patient.categoryid";

    public static final String SF_PATIENT_CUST_NO = "smartfi.patient.cust.no";
    public static final String SF_PATIENT_NAME = "smartfi.patient.name";
    public static final String SF_DOCTOR_NAME = "smartfi.doctor.name";
    public static final String SF_DOCTOR_NUMBER = "smartfi.doctor.number";

    public static final String DISPLAY_EXTRA = "smartfi.display.extra";
    public static final String INSERT_PATIENT = "smartfi.patient.insert";
    public static final String INSERT_DOCTOR = "smartfi.insert.doctor";
    public static final String SHOOT_DISPLAY = "smartfi.shoot.display";
    public static final String SHOOT_PORTRAIT = "smartfi.shoot.portrait";
    public static final String DISPLAY_LANDSCAPE = "smartfi.display.landscape";

    public static void defaultPreference(Context context){

    }

    public static String getSfToken(Context con){
        return getString(con, SF_TOKEN, Constants.EMRAPI.UNDEFINED);
    }

    public static void setSfToken(Context con, String tk){
        setString(con, SF_TOKEN, tk);
    }

    public static final String getHospitalId(Context con){
        //000000000001
        return getString(con, SF_HOSPITAL_ID, Constants.EMRAPI.UNDEFINED);
    }

    public static void setHospitalId(Context con, String id){
        setString(con, SF_HOSPITAL_ID, id);
    }

    public static final String getDoctorId(Context con){
        return getString(con, SF_DOCTOR_ID, Constants.EMRAPI.UNDEFINED);
    }

    public static void setDoctorId(Context con, String id){
        setString(con, SF_DOCTOR_ID, id);
    }

    public static final String getPatientId(Context con){
        return getString(con, SF_PATIENT_ID, "sf-patient");
    }

    public static void setPatientId(Context con, String id){
        setString(con, SF_PATIENT_ID, id);
    }

    public static final String getPatientChart(Context con){
        return getString(con, SF_PATIENT_CHART, "101010");
    }

    public static void setPatientChart(Context con, String chartNum){
        setString(con, SF_PATIENT_CHART, chartNum);
    }


    public static final String getSfDoctorPw(Context con){
        return getString(con, SF_DOCTOR_PW, "");
    }

    public static void setSfDoctorPw(Context con, String pw){
        setString(con, SF_DOCTOR_PW, pw);
    }

    public static final String getSfPatientCustNo(Context con){
        return getString(con, SF_PATIENT_CUST_NO, "");
    }

    public static void setSfPatientCustNo(Context con, String no){
        setString(con, SF_PATIENT_CUST_NO, no);
    }

    public static final String getSfPatientName(Context con){
        return getString(con, SF_PATIENT_NAME, "");
    }

    public static void setSfPatientName(Context con, String name){
        setString(con, SF_PATIENT_NAME, name);
    }

    public static final String getSfDoctorName(Context con){
        return getString(con, SF_DOCTOR_NAME, "");
    }

    public static void setSfDoctorName(Context con, String name){
        setString(con, SF_DOCTOR_NAME, name);
    }

    public static final String getSfDoctorNumber(Context con){
        return getString(con, SF_DOCTOR_NUMBER, "");
    }

    public static void setSfDoctorNumber(Context con, String name){
        setString(con, SF_DOCTOR_NUMBER, name);
    }

//    public static final String getSfPatientCategoryid(Context con){
//        return getString(con, SF_PATIENT_CATEGORYID, "");
//    }
//
//    public static void setSfPatientCategoryid(Context con, String name){
//        setString(con, SF_PATIENT_CATEGORYID, name);
//    }

    //
    public static void setInt(Context context, String key, int value){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getInt(Context context, String key, int default_value){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(key, default_value);
    }

    public static void setBoolean(Context context, String key, boolean value){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBoolean(Context context, String key, boolean default_value){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(key, default_value);
    }

    public static void setString(Context context, String key, String value){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getString(Context context, String key, String default_value){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(key, default_value);
    }

    // Save options
    public static final Boolean getSfInsertPatientOpt(Context con){
        return getBoolean(con, INSERT_PATIENT, false);
    }

    public static void setSfInsertPatientOpt(Context con, boolean is){
        setBoolean(con, INSERT_PATIENT, is);
    }

    public static final Boolean getSfDisplayExtraOpt(Context con){
        return getBoolean(con, DISPLAY_EXTRA, false);
    }

    public static void setSfDisplayExtraOpt(Context con, boolean is){
        setBoolean(con, DISPLAY_EXTRA, is);
    }

    public static final Boolean getSfInsertDoctorOpt(Context con){
        return getBoolean(con, INSERT_DOCTOR, false);
    }

    public static void setSfInsertDoctorOpt(Context con, boolean is){
        setBoolean(con, INSERT_DOCTOR, is);
    }

    public static final Boolean getSfShootDisplayOpt(Context con){
        return getBoolean(con, SHOOT_DISPLAY, false);
    }

    public static void setSfShootDisplayOpt(Context con, boolean is){
        setBoolean(con, SHOOT_DISPLAY, is);
    }

    public static final Boolean getSfShootPortraitOpt(Context con){
        return getBoolean(con, SHOOT_PORTRAIT, false);
    }

    public static void setSfShootPortraitOpt(Context con, boolean is){
        setBoolean(con, SHOOT_PORTRAIT, is);
    }

    public static final Boolean getSfDisplayLandscapeOpt(Context con){
        return getBoolean(con, DISPLAY_LANDSCAPE, false);
    }

    public static void setSfDispalyLandscapeOpt(Context con, boolean is){
        setBoolean(con, DISPLAY_LANDSCAPE, is);
    }

}
