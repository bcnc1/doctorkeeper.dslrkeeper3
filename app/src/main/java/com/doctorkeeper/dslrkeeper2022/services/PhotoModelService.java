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
package com.doctorkeeper.dslrkeeper2022.services;


import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.orm.query.Condition;
import com.orm.query.Select;
import com.doctorkeeper.dslrkeeper2022.models.PhotoModel;
import com.doctorkeeper.dslrkeeper2022.util.SmartFiPreference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.doctorkeeper.dslrkeeper2022.madamfive.MadamfiveAPI.getActivity;


public class PhotoModelService {

    private final static String TAG = PhotoModelService.class.getSimpleName();

    public static List<PhotoModel> findAll() {
        return (ArrayList<PhotoModel>) Select.from(PhotoModel.class)
                .orderBy("created desc").list();
    }

    public static ArrayList<PhotoModel> findImageListOld() {
        return (ArrayList<PhotoModel>) Select.from(PhotoModel.class)
                .orderBy("created asc")
                .list();
    }

    public static void deletePhotoModel(Long id){
        PhotoModel photoModel = PhotoModel.findById(PhotoModel.class, id);
        photoModel.delete();
    }

    public static PhotoModel getPhotoModel(Long id){
        PhotoModel photoModel = PhotoModel.findById(PhotoModel.class, id);
        long id1 = photoModel.getId();
        return photoModel;
    }


    public static Long getPhotoModelIdByName(String fileName){
        Log.w(TAG," 서치값 = "+fileName);
        //List<PhotoModel> photoModel = PhotoModel.findWithQuery(PhotoModel.class, "Select * from PhotoModel where filename = ?",fileName);
        List<PhotoModel> photoModel = Select.from(PhotoModel.class)
                .where(Condition.prop("filename").eq(fileName))
                .list();

        if(photoModel.size() >0){
            Log.w(TAG,"id : "+photoModel.get(0).getId());
            return photoModel.get(0).getId();
        }
        return Long.valueOf(-1);
    }

    public static void  deleteFileNPhotoModel(PhotoModel pm){
        boolean isOriDelete = false, isThumbDelete = false;

        String filePath = pm.getFullpath();
        File f = new File(filePath);
//        if(f.exists()){
//            Log.w(TAG," 파일 존재");
//        }
        if(f.delete()){
            Log.d(TAG,"원본파일 삭제 성공");
            isOriDelete = true;
        }else{
            Log.d(TAG,"원본파일 삭제 실패");
        }

        String thumbPath = pm.getThumbpath();
        File fthumb = new File(thumbPath);
        if(fthumb.delete()){
            Log.d(TAG,"thumbnail 삭제 성공");
            isThumbDelete = true;
        }else{
            Log.d(TAG,"섬네일 삭제 실패");
        }

        if(isOriDelete && isThumbDelete){
            pm.delete();

            Log.d(TAG,"DB 삭제 완료");
        }

    }

//    public static PhotoModel saveThumb(Bitmap bitmap, String filename, final int mode) {
//        File file = new File(getActivity().getExternalFilesDir(Environment.getExternalStorageState()), "/drcam/");
//
//        if (!file.isDirectory()) {
//            file.mkdir();
//        }
//
//        FileOutputStream outStream = null;
//
//        try {
//
//            Log.d(TAG, "파일저장 패스 = "+file.getAbsolutePath()+filename);
//            outStream = new FileOutputStream(file.getAbsolutePath()+filename); //파일저장
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
//            outStream.close();
//        } catch (FileNotFoundException e) { // <10>
//            Log.e(TAG,e.toString());
//        } catch (IOException e) {
//            Log.e(TAG,e.toString());
//        }
//
//
//        final PhotoModel photoModel = new PhotoModel();
//        photoModel.setThumbpath(file.getAbsolutePath()+filename);
//
//
//    }

    public static void makeDir(Context con, final String subDir){
        File file = new File(con.getExternalFilesDir(Environment.getExternalStorageState()), subDir);

        if (!file.isDirectory()) {
            file.mkdir();
        }
    }

    //mode: 카메라(0), dslr(1), 비디오(2)
    public static PhotoModel addPhotoModel( final  Context con , final String sourcePath, final String thumbPath, String filename, final int mode) {

        long filesize;
        File f = new File(sourcePath);
        filesize = f.length();

        final PhotoModel photoModel = new PhotoModel();

        photoModel.setFullpath(sourcePath);
        photoModel.setThumbpath(thumbPath);
        photoModel.setFilename(filename);
        photoModel.setMode(mode);
        photoModel.setCreated(new Date());
        photoModel.setFileSize(filesize);
        photoModel.setCustNo(SmartFiPreference.getPatientChart(con));
        photoModel.setCustName(SmartFiPreference.getSfPatientName(con));
        photoModel.setCategoryId(SmartFiPreference.getPatientId(con));

        photoModel.save();

        return photoModel;
    }

    public static PhotoModel addPhotoModelWithRawName( final  Context con , final String sourcePath, final String thumbPath, String filename, String rawName, final int mode) {

        long filesize;
        File f = new File(sourcePath);
        filesize = f.length();

        final PhotoModel photoModel = new PhotoModel();

        photoModel.setFullpath(sourcePath);
        photoModel.setThumbpath(thumbPath);
        photoModel.setFilename(filename);
        photoModel.setRawfileName(rawName);
        photoModel.setMode(mode);
        photoModel.setCreated(new Date());
        photoModel.setFileSize(filesize);
//        photoModel.setCustNo(SmartFiPreference.getSfPatientCustNo(con));
        photoModel.setCustNo(SmartFiPreference.getPatientChart(con));
        photoModel.setCustName(SmartFiPreference.getSfPatientName(con));
        photoModel.setCategoryId(SmartFiPreference.getPatientId(con));
        photoModel.save();

        return photoModel;
    }

    public static PhotoModel savePhoto(byte[] bytes, String filename, final int mode) {

        Log.w(TAG, "savePhoto =## ");

        File file = new File(getActivity().getExternalFilesDir(Environment.getExternalStorageState()), "/drcam/");

        if (!file.isDirectory()) {
            file.mkdir();
        }

        FileOutputStream outStream = null;

        try {
            // Write to SD Card
//            outStream = new FileOutputStream(folder + filename);
            Log.d(TAG, "파일저장 패스 = "+file.getAbsolutePath()+filename);
            outStream = new FileOutputStream(file.getAbsolutePath()+filename); //파일저장
            outStream.write(bytes);
            outStream.close();
        } catch (FileNotFoundException e) { // <10>
            Log.i("PhotoModelService",e.toString());
        } catch (IOException e) {
            Log.i("PhotoModelService",e.toString());
        } finally {
        }

//        if(mode == 1) {
//
//            Bitmap thumbBitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeByteArray(bytes, 0, bytes.length), 120, 120);
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//
////        String thumbfilename = "";
////        int dot = filename.indexOf('.');
////        thumbfilename = filename.substring(0,dot);
////        thumbfilename = thumbfilename + "_thumb.JPG";
//
//            try {
//                // Write to SD Card
//
//                outStream = new FileOutputStream(folder + filename + "_thumb");
//                outStream.write(baos.toByteArray());
//                outStream.close();
//            } catch (FileNotFoundException e) { // <10>
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//            }
//        }

        final PhotoModel photoModel = new PhotoModel();
//        photoModel.setFullpath(folder + filename);
        Log.d(TAG, "path = "+file.getAbsolutePath() + filename);

        photoModel.setFullpath(file.getAbsolutePath() + filename);
        photoModel.setFilename(filename);
        photoModel.setUploaded(true);
        photoModel.setMode(mode); // CAMERA
 //       photoModel.setTargetId("");
//        photoModel.setThumb(bytes);
//        photoModel.setBitmap(bytes);
 //       photoModel.setTargetName("");
        photoModel.setCreated(new Date());
        photoModel.save();




        return photoModel;
    }

//    public static PhotoModel saveVideo(byte[] bytes, String filename, final int mode) {
//        return photoModel;
//    }
}
