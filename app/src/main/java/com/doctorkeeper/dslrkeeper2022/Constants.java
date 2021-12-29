package com.doctorkeeper.dslrkeeper2022;

public class Constants {
    public static final class Invoke{
        public static final String VIDEO_RECORD = "video.record";
        public static final String UPLOAD_FILE_PATH = "upload.file.path";
        public static final String UPLOAD_FILE_KIND = "upload.file.kind";
        public static final String UPLOAD_FILE_NAME = "upload.file.name";
        public static final String UPLOAD_FILE_TYPE = "upload.file.type";
        public static final String UPLOAD_MESSAGE_CALLBACK = "upload.message.callback";
    }

    public static final class m5{
        public static final String BASE_URL = "http://api.doctorkeeper.com:7818";
    }

    public static final class bcnc{
        public static final String BASE_URL = "http://211.252.85.83:3100";
    }

    public static final class Upload{
        public static final String FILE_UPLOAD_SUCCESS = "file.upload.success";
        public static final String FILE_UPLOAD_FAIL = "file.upload.fail";
        public static final String CHAIN_CREATE_SUCCESS = "chain.create.success";
        public static final String CHAIN_CREATE_FAIL = "chain.create.fail";
        public static final String READ_FILE_UPLOAD_SUCCESS = "파일 업로드 성공";
        public static final String READ_FILE_UPLOAD_FAIL = "파일 업로드 실패";

    }

    public static final class Storage{
//        public static final String BASE_URL = "https://ssproxy.ucloudbiz.olleh.com/v1/AUTH_3de4a999-ff5c-416a-ad30-77fcd4f9383f";
          public static final String BASE_URL = "http://ssproxy.ucloudbiz.olleh.com/v1/AUTH_8c4583d1-b030-4cc2-8e65-7e747563dbeb";

    }

    public static final class Chain{
        public static final String BASE_URL = "http://211.43.13.203:3000";
        public static final String CREATE = "/api/v1/proof/create";
    }

    public static final class Notification{
        public static final String NOTIFICATION_TITLE = "File Upload";
        public static final int  NOTIFICATION_PICTURE_ID = 2010;
        public static final int  NOTIFICATION_VIDEO_ID = 1010;
        public static final int  NOTIFICATION_RETRY_ID = 201;
    }

    public static final class EMRAPI{
        public static final String BASE_URL = "http://211.252.87.177:8080";
        public static final String  LOGIN = "/emrmng/api/getLogin";
        public static final String  SEARCH_PATIENT = "/emrmng/api/v1/custs";
        public static final String  REG_PHOTO = "/emrmng/api/v1/custs/photos";
        public static final String  FIND_PHOTOS = "/emrmng/api/v1/custs/photos";
        public static final String  FIND_PHOTOS_ALL = "/emrmng/api/v1/custs/photos/all";
        public static final String  INSERT_PATIENT = "/emrmng/api/v1/custs";

        public static final int  NOTIFICATION_VIDEO_ID = 1010;
        public static final String DATA = "data";
        public static final String CODE = "code";

        public static final String CODE_200 = "200";
        public static final String CODE_401 = "401";
        public static final String CODE_403 = "403";

        public static final String UNDEFINED = "undefined";

        public static final String UID = "userId";
        public static final String CUST_NO = "custNo";
        public static final String CHART_NO = "chrtNo";
        public static final String P_IDX = "pageIdx";
        public static final String P_SIZE = "pageSize";
        public static final String CUST_NM = "custNm";

    }

    public static final long DELAY_TIME_MILLIS = 3000;
    public static final boolean FILE_N_DB_DELETE = true;  //디버그, 파일 및 db 안지우기..
    public static final String MESSENGER = "com.thinoo.drcamlink.services.extra.picture.messenger";
    public static final String MESSENGER_RETRY = "com.thinoo.drcamlink.services.extra.picture.messenger.retry";
    public static boolean PATIENT_HAS_MANY_IMAGES = false;
}
