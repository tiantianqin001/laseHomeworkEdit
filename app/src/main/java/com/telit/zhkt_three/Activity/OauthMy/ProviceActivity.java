package com.telit.zhkt_three.Activity.OauthMy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.SslError;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.telit.zhkt_three.Activity.HomeScreen.LoginActivity;
import com.telit.zhkt_three.Activity.HomeScreen.MainActivity;
import com.telit.zhkt_three.Constant.Constant;
import com.telit.zhkt_three.Constant.UrlUtils;
import com.telit.zhkt_three.Fragment.CircleProgressDialogFragment;
import com.telit.zhkt_three.JavaBean.StudentInfo;
import com.telit.zhkt_three.MyApplication;
import com.telit.zhkt_three.R;
import com.telit.zhkt_three.Utils.AndroidBug5497Workaround;
import com.telit.zhkt_three.Utils.BuriedPointUtils;
import com.telit.zhkt_three.Utils.Jpush.JpushApply;
import com.telit.zhkt_three.Utils.MD5Utils;
import com.telit.zhkt_three.Utils.OkHttp3_0Utils;
import com.telit.zhkt_three.Utils.QZXTools;
import com.telit.zhkt_three.Utils.UserUtils;
import com.telit.zhkt_three.Utils.VersionUtils;
import com.telit.zhkt_three.Utils.eventbus.EventBus;
import com.telit.zhkt_three.Utils.eventbus.Subscriber;
import com.telit.zhkt_three.Utils.eventbus.ThreadMode;
import com.telit.zhkt_three.dialoge.LoadDialog;
import com.telit.zhkt_three.greendao.StudentInfoDao;
import com.tencent.mars.comm.NetStatusUtil;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.zbv.basemodel.LingChuangUtils;
import com.zbv.meeting.util.SharedPreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.XWalkActivity;
import org.xwalk.core.XWalkHttpAuthHandler;
import org.xwalk.core.XWalkJavascriptResult;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkWebResourceRequest;
import org.xwalk.core.XWalkWebResourceResponse;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;

import cn.jpush.android.api.JPushInterface;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * ????????????????????????????????????
 * http://open.ahjygl.gov.cn/openplatform/#/ssoDocument?id=client
 */
public class ProviceActivity extends XWalkActivity implements View.OnClickListener {

    private OauthCall oauthCall;
    private String deviceId;
    private static boolean isShow=false;

    /**
     * ????????????????????????????????????
     */
    private SharedPreferences sp_student;
    private XWalkView xWalkWebView;
    private XWalkSettings xWVSettings;
    private static final int Server_Error = 0;
    private static final int Error404 = 1;
    private static final int Operate_Success = 2;
    private static final int Other_Error = 3;
    private static final int Oauth_Result = 4;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Server_Error:
                    if (isShow){
                        QZXTools.popCommonToast(ProviceActivity.this, "??????????????????....???????????????", false);

                     /*   Intent intent=new Intent(ProviceActivity.this, LoginActivity.class);
                        startActivity(intent);*/

                        if (loadDialog!=null){
                            loadDialog.cancel();
                            loadDialog=null;
                        }

                    }
                    break;
                case Error404:
                    if (isShow){
                        QZXTools.popCommonToast(ProviceActivity.this, "??????????????????....", false);
                        if (loadDialog!=null){
                            loadDialog.cancel();
                            loadDialog=null;
                        }
                    }
                    break;
                case Operate_Success:
                    if (isShow){
                        /**
                         * ???????????????????????????????????????
                         * 6006 ?????????tag?????????????????????40??????
                         * */
                        JpushApply.getIntance().registJpush(MyApplication.getInstance());

                        QZXTools.logE("Alias:"+UserUtils.getUserId(),null);

                        //????????????????????????
                        MiPushClient.setAlias(ProviceActivity.this, UserUtils.getUserId(), null);

                       // QZXTools.popCommonToast(ProviceActivity.this, (String) msg.obj, false);
                        //??????home ???
                        LingChuangUtils.getInstance().stopHome(MyApplication.getInstance());
                        //????????????????????????
                        UserUtils.setBooleanTypeSpInfo(sp_student, "isLoginIn", true);

                     //   startActivity(new Intent(ProviceActivity.this, MainActivity.class));

                        //??????????????????
                        ProviceActivity.this.finish();

                        //??????????????????
                        BuriedPointUtils.buriedPoint("2001","","","","");

                        if (timer!=null){
                            timer.cancel();
                            timer=null;
                        }

                        if (loadDialog!=null){
                            loadDialog.cancel();
                            loadDialog=null;
                        }
                    }
                    break;
                case Other_Error:
                    if (isShow){
                      //  QZXTools.popCommonToast(ProviceActivity.this, (String) msg.obj, false);

                        if (loadDialog!=null){
                            loadDialog.cancel();
                            loadDialog=null;
                        }
                    }
                    break;
                case Oauth_Result:
                    if (isShow){
                        String resultJson = (String) msg.obj;
                        Gson gson = new Gson();
                        Map<String, Object> map = gson.fromJson(resultJson, new TypeToken<Map<String, Object>>() {
                        }.getType());

                        String Oauth_UserId = (String) map.get("data");

                        sp_student.edit().putString("oauth_id", Oauth_UserId).commit();

                        if (loadDialog!=null){
                            loadDialog.cancel();
                            loadDialog=null;
                        }
                    }

                    break;
            }
        }
    };
    private Timer timer;

    private TextView tv_refresh;
    private TextView tv_versionCode;
    private View view_no_net;
    private ProgressBar mBar;
    private LoadDialog loadDialog;

    private void loginIn() {
        if (!TextUtils.isEmpty(oauthCall.tgt)) {
            String url = "http://open.ahjygl.gov.cn/sso-oauth/client/refreshTgt";

            Map<String, String> paramMap = new LinkedHashMap<>();
            paramMap.put("tgt", oauthCall.tgt);
            paramMap.put("client", "pc");//?????????????????????
            paramMap.put("deviceId", deviceId);

            OkHttp3_0Utils.getInstance().asyncGetOkHttp(url, paramMap, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    QZXTools.logE("??????", null);
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String resultJson = response.body().string();//??????????????????response.body().string()
                        QZXTools.logE("response=" + resultJson, null);
                        Gson gson = new Gson();
                        Map<String, Object> map = gson.fromJson(resultJson, new TypeToken<Map<String, Object>>() {
                        }.getType());
                        if (map.get("code").equals("1")) {
                            getCallback(null);
                        } else {
                            //????????????????????????
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    enterLoginPage();
                                }
                            });
                        }

                    }
                }
            });

        } else {
            enterLoginPage();
        }
    }
    private CircleProgressDialogFragment circleProgressDialogFragment;

    @Override
    protected void onXWalkReady() {

            XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);
//        XWalkPreferences.setValue(XWalkPreferences.ANIMATABLE_XWALK_VIEW, true);

            XWalkPreferences.setValue(XWalkPreferences.JAVASCRIPT_CAN_OPEN_WINDOW, true);
            XWalkPreferences.setValue(XWalkPreferences.ALLOW_UNIVERSAL_ACCESS_FROM_FILE, true);
            XWalkPreferences.setValue(XWalkPreferences.SUPPORT_MULTIPLE_WINDOWS, true);
//        XWalkPreferences.setValue(XWalkPreferences.PROFILE_NAME, true);
            XWalkPreferences.setValue(XWalkPreferences.SPATIAL_NAVIGATION, true);
//        XWalkPreferences.setValue(XWalkPreferences.ENABLE_THEME_COLOR, true);
            XWalkPreferences.setValue(XWalkPreferences.ENABLE_JAVASCRIPT, true);
            XWalkPreferences.setValue(XWalkPreferences.ENABLE_EXTENSIONS, true);

            //??????setting
            xWVSettings = xWalkWebView.getSettings();
            xWVSettings.setSupportZoom(true);//????????????
            xWVSettings.setBuiltInZoomControls(true);//??????????????????
            xWVSettings.setLoadWithOverviewMode(true);
            xWVSettings.setUseWideViewPort(true);//????????????????????????webview?????????
            xWVSettings.setLoadsImagesAutomatically(true);
            //??????JS??????.??????????????????17,????????????@JavascriptInterface
            xWVSettings.setJavaScriptEnabled(true);//??????JS
            xWVSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            xWVSettings.setSupportMultipleWindows(false);

            xWVSettings.setAllowFileAccess(true);
            xWVSettings.setDomStorageEnabled(true);
            xWVSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

            xWVSettings.setAllowUniversalAccessFromFileURLs(true);
//        xWVSettings.setMediaPlaybackRequiresUserGesture(true);

            xWalkWebView.setUIClient(new XWalkUIClient(xWalkWebView) {
                @Override
                public void onPageLoadStarted(XWalkView view, String url) {
                    super.onPageLoadStarted(view, url);
                }

                @Override
                public boolean onJsAlert(XWalkView view, String url, String message, XWalkJavascriptResult result) {
                    //Toast.makeText(ProciapalActivity.this, message, Toast.LENGTH_SHORT).show();
                    return super.onJsAlert(view, url, message, result);
                }

                @Override
                public void onScaleChanged(XWalkView view, float oldScale, float newScale) {
                    if (view != null) {
                        view.invalidate();
                    }
                    super.onScaleChanged(view, oldScale, newScale);
                }

                @Override
                public void onPageLoadStopped(XWalkView view, String url, LoadStatus status) {
                    super.onPageLoadStopped(view, url, status);
                }

                @Override
                public boolean onCreateWindowRequested(XWalkView view, InitiateBy initiator, final ValueCallback<XWalkView> callback) {
                    return true;
                }
            });

            xWalkWebView.setResourceClient(new XWalkResourceClient(xWalkWebView) {
                @Override
                public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
//                Log.e(TAG, "shouldOverrideUrlLoading url : " + url);
                    view.loadUrl(url);
                    return true;
                }

                @Override
                public XWalkWebResourceResponse shouldInterceptLoadRequest(XWalkView view, XWalkWebResourceRequest request) {
//                Log.e(TAG, "shouldInterceptLoadRequest url : " + equest.getUrl().toString());
                    return super.shouldInterceptLoadRequest(view, request);
                }

                @Override
                public void onReceivedSslError(XWalkView view, ValueCallback<Boolean> callback, SslError error) {
                    //super.onReceivedSslError(view, callback, error);
                    //Log.e(TAG, "onReceivedSslError");
                    //Toast.makeText(ProciapalActivity.this, "???????????????", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLoadFinished(XWalkView view, String url) {
                    super.onLoadFinished(view, url);
                    //??????????????????
                    if (circleProgressDialogFragment != null) {
                        circleProgressDialogFragment.dismissAllowingStateLoss();
                        circleProgressDialogFragment = null;
                    }

                }

                @Override
                public void onLoadStarted(XWalkView view, String url) {
                    super.onLoadStarted(view, url);
                }

                @Override
                public void onProgressChanged(XWalkView view, int progressInPercent) {
                    super.onProgressChanged(view, progressInPercent);

                    if (progressInPercent == 100) {
                        mBar.setVisibility(View.GONE);
                    } else {
                        mBar.setVisibility(View.VISIBLE);
                        mBar.setProgress(progressInPercent);
                    }
                }


                @Override
                public void onDocumentLoadedInFrame(XWalkView view, long frameId) {
                    super.onDocumentLoadedInFrame(view, frameId);
                }

                @Override
                public void onReceivedHttpAuthRequest(XWalkView view, XWalkHttpAuthHandler handler, String host, String realm) {
                    super.onReceivedHttpAuthRequest(view, handler, host, realm);
                }

                @Override
                public void onReceivedLoadError(XWalkView view, int errorCode, String description, String failingUrl) {
                    super.onReceivedLoadError(view, errorCode, description, failingUrl);
                }

                @Override
                public void onReceivedResponseHeaders(XWalkView view, XWalkWebResourceRequest request, XWalkWebResourceResponse response) {
                    super.onReceivedResponseHeaders(view, request, response);
                }
            });
        //????????????
      /*  if (!TextUtils.isEmpty(sp_student.getString("oauth_id", ""))) {
            requestOauthLogin(sp_student.getString("oauth_id", ""));
        }else {

        }*/
        loginIn();
        if (JPushInterface.isPushStopped(MyApplication.getInstance())) {
            QZXTools.logE("LoginActivity JPush resume", null);
            JPushInterface.resumePush(MyApplication.getInstance());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_1);
        AndroidBug5497Workaround.assistActivity(this);
        mBar = findViewById(R.id.progress_Bar);
        xWalkWebView = findViewById(R.id.webView);
        TextView tv_click_login = findViewById(R.id.tv_click_login);
        TextView tv_click_login_offline = findViewById(R.id.tv_click_login_offline);
        ImageView iv_open_wifi = findViewById(R.id.iv_open_wifi);
        isShow=true;

        sp_student = getSharedPreferences("student_info", MODE_PRIVATE);

        oauthCall = new OauthCall();

        EventBus.getDefault().register(this);
        tv_click_login.setOnClickListener(this);
        iv_open_wifi.setOnClickListener(this);

        tv_refresh = findViewById(R.id.tv_refresh);
        tv_versionCode = findViewById(R.id.tv_versionCode);
        tv_versionCode.setText("????????????v" + VersionUtils.getVersionName(this));

        tv_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetStatusUtil.isNetworkConnected(ProviceActivity.this)) {
                    view_no_net.setVisibility(View.GONE);

                    loginIn();
                }else {
                    view_no_net.setVisibility(View.VISIBLE);

                    Toast.makeText(ProviceActivity.this,"??????????????????",Toast.LENGTH_LONG).show();
                }

            }
        });
        //????????????
        tv_click_login_offline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ProviceActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });

        view_no_net = findViewById(R.id.view_no_net);
        if (NetStatusUtil.isNetworkConnected(this)) {
            view_no_net.setVisibility(View.GONE);
        }

        loadDialog = new LoadDialog(this);
    }
    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();

        if (timer!=null){
            timer.cancel();
            timer=null;
        }
    }
  public static final int NET_OUT_NET=0X008;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Toast.makeText(ProviceActivity.this,"??????????????????",Toast.LENGTH_LONG).show();
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
      //  enterLoginPage();
    }

    //----------------------------????????????
    private void enterLoginPage() {

        // ??????addJavascriptInterface()???Java???????????????JS??????
        //??????1???Javascript?????????
        //??????2???Java?????????
        xWalkWebView.addJavascriptInterface(oauthCall, "oauthCall");//OauthCall??????????????????js???oauthCall??????

        WifiManager wm = (WifiManager) MyApplication.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String wlan_mac = wm.getConnectionInfo().getMacAddress();

        deviceId = md5(wlan_mac);
        String themeType = "2";

        Map<String, String> map = new HashMap<String, String>();
        map.put("User-Agent", "");
        xWalkWebView.loadUrl("http://open.ahjygl.gov.cn/sso-oauth/client/login?deviceId=" + deviceId + "&themeType=" + themeType, map);
        SharedPreferenceUtil.getInstance(MyApplication.getInstance()).setString("deviceId",deviceId);
    }

    @Subscriber(tag = "getTgt", mode = ThreadMode.MAIN)
    public void getCallback(String flag) {
        if (loadDialog!=null){
            loadDialog.show();
        }
        String url = "http://open.ahjygl.gov.cn/sso-oauth/client/validateTgt";
        Map<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put("appkey", Constant.EduAuthAppKey);
        paramMap.put("tgt", SharedPreferenceUtil.getInstance(MyApplication.getInstance()).getString("getTgt"));

        paramMap.put("client", "pc");//?????????????????????
        paramMap.put("deviceId", deviceId);

        OkHttp3_0Utils.getInstance().asyncGetOkHttp(url, paramMap, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                QZXTools.logE("??????", null);

                QZXTools.logD("tiantianqinLogin.............."+e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    /**
                     *
                     * response={"code":"1","message":"success","data":"9c5e8fc2cc6e5a197b4ea823497f3da719ce42bcab3b04bf532573912beb97da4826d86417934d5f5d4a9af096137783f2e175af23ffe065","success":true}
                     * */
                    String resultJson = response.body().string();//??????????????????response.body().string()
                    QZXTools.logE("response=" + resultJson, null);

                    QZXTools.logD("tiantianqinLogin..........."+resultJson.toString());
                    Gson gson = new Gson();
                    Map<String, Object> map = gson.fromJson(resultJson, new TypeToken<Map<String, Object>>() {
                    }.getType());
                    QZXTools.logE("data=" + map.get("data"), null);
                    if (map.get("code").equals("1")) {
                        //?????????????????????tgt
                        SharedPreferences sharedPreferences = getSharedPreferences("tgtLogin", MODE_PRIVATE);
                        UserUtils.setTgt(sharedPreferences, "tgt", oauthCall.tgt);

                     //   intent.putExtra("data_userid", (String) map.get("data"));

                        requestOauthLogin((String) map.get("data"));

                        sp_student.edit().putString("oauth_id", (String) map.get("data")).commit();

                    } else if (map.get("code").equals("-1")) {
                        //???????????????tgt
                        //loginOut();

                        Message message = mHandler.obtainMessage();
                        message.what = Other_Error;
                        message.obj = (String) map.get("data");
                        mHandler.sendMessage(message);
                    }
                   /* setResult(RESULT_OK, intent);
                    finish();*/


                }
            }
        });

    }


    /**
     * Can not perform this action after onSaveInstanceState
     */

    private void requestOauthLogin(String userXfId) {
        //????????????????????????
        UserUtils.setBooleanTypeSpInfo(sp_student, "IsOauthMode", true);

        String url = UrlUtils.BaseUrl + UrlUtils.OauthLogin;

//        String url = "http://172.16.4.40:8090/wisdomclass/toLoginByOauth";

        Map<String, String> paraMap = new LinkedHashMap<>();
        // 9c5e8fc2cc6e5a197b4ea823497f3da719ce42bcab3b04bf532573912beb97da4826d86417934d5f5d4a9af096137783f2e175af23ffe065
        //??????user ID ???????????????
        paraMap.put("userId", userXfId);
        paraMap.put("type", "student");
        //?????????????????????ID????????????id
        String registrationID = JPushInterface.getRegistrationID(this);
        QZXTools.logD("tiantianqinLogin...........registrationID="+registrationID);
        paraMap.put("deviceId", JPushInterface.getRegistrationID(this));

        OkHttp3_0Utils.getInstance().asyncPostOkHttp(url, paraMap, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                QZXTools.logE("onFailure e=" + e, null);
                //???????????????,???????????????????????????????????????
                mHandler.sendEmptyMessage(Server_Error);


                QZXTools.logD("tiantianqinLogin...........??????????????????????????????..."+e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {

//                            QZXTools.logE("resultJson=" + resultJson, null);
                    try {
                        String resultJson = response.body().string();

                        QZXTools.logD("tiantianqinLogin...........??????????????????????????????..."+resultJson);
                        JSONObject jsonObject = new JSONObject(resultJson);
                        boolean success = jsonObject.getBoolean("success");
                        String errorCode = jsonObject.getString("errorCode");
                        String msg = jsonObject.getString("msg");
                        int total = jsonObject.getInt("total");
                        int pageNo = jsonObject.getInt("pageNo");

                        String result = jsonObject.getString("result");
//                                QZXTools.logE("result=" + result, null);

                        if (errorCode.equals("1")) {
                            Gson gson = new Gson();
                            StudentInfo studentInfo = gson.fromJson(result, StudentInfo.class);
                            QZXTools.logE("studentInfo=" + studentInfo, null);

                            UserUtils.setStringTypeSpInfo(sp_student, "studentId", studentInfo.getStudentId());
                            UserUtils.setStringTypeSpInfo(sp_student, "schoolId", studentInfo.getSchoolId());
                            UserUtils.setStringTypeSpInfo(sp_student, "loginName", studentInfo.getLoginName());
                            UserUtils.setStringTypeSpInfo(sp_student, "classId", studentInfo.getClassId());
                            UserUtils.setStringTypeSpInfo(sp_student, "userId", studentInfo.getUserId());
                            UserUtils.setStringTypeSpInfo(sp_student, "shot_classId", studentInfo.getClassShortId());
                            UserUtils.setStringTypeSpInfo(sp_student, "token", studentInfo.getToken());


                            //??????className studentName photo
                            UserUtils.setStringTypeSpInfo(sp_student, "className", studentInfo.getClassName());
                            UserUtils.setStringTypeSpInfo(sp_student, "studentName", studentInfo.getStudentName());

                            if (studentInfo.getPhoto() != null) {
                                UserUtils.setStringTypeSpInfo(sp_student, "avatarUrl", studentInfo.getPhoto());
                            }

                            //????????????????????????
                            StudentInfoDao studentInfoDao = MyApplication.getInstance().getDaoSession().getStudentInfoDao();
                            studentInfoDao.insertOrReplace(studentInfo);

                            Message message = mHandler.obtainMessage();
                            message.what = Operate_Success;
                            message.obj = "????????????";
                            mHandler.sendMessage(message);
                            String s = MD5Utils.MD5(studentInfo.getSchoolId());
                            Log.i("schoolId", "onResponse: "+s);
                            //?????????????????????????????????????????????app????????????????????? tudo
                            Intent intent = new Intent("com.linspirer.edu.loginapkfinish");
                            intent.putExtra("userid", studentInfo.getUserId());
                            intent.putExtra("username", studentInfo.getLoginName());
                            intent.putExtra("schoolid", MD5Utils.MD5(studentInfo.getSchoolId()));
                            intent.putExtra("classname", studentInfo.getGradeName()
                                    + " " + studentInfo.getClassName());
                            intent.putExtra("txurl", studentInfo.getPhoto());
                           // intent.putExtra("useOfflineLogin", false);
                            intent.setPackage("com.android.launcher3");
                            sendBroadcast(intent);
                        } else {
                            //??????????????????????????????????????????
                            Message message = mHandler.obtainMessage();
                            message.what = Other_Error;
                            message.obj = msg;
                            mHandler.sendMessage(message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Message message = mHandler.obtainMessage();
                        message.what = Other_Error;
                        message.obj = "json????????????";
                        mHandler.sendMessage(message);
                    }
                } else {
                    mHandler.sendEmptyMessage(Error404);
                }
            }
        });

    }



    // MD5?????????32?????????
    public static String md5(String str) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        md5.update(str.getBytes());
        byte[] md5Bytes = md5.digest();
        StringBuilder hexValue = new StringBuilder();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case  R.id.tv_click_login:
                //???????????????????????????
                lingChang();
                break;
            case R.id.iv_open_wifi:
                QZXTools.enterWifiSetting(this);
                break;
        }
    }


    private void lingChang() {
        Intent intent = new Intent("com.android.launcher3.mdm.OPEN_ADMIN");
        intent.setPackage("com.android.launcher3");
        sendBroadcast(intent);
         //Toast.makeText(this,"?????????com.android.launcher3.mdm.OPEM_ADMIN??????",Toast.LENGTH_LONG).show();
    }

    public void loginOut() {
        String url = "http://open.ahjygl.gov.cn/sso-oauth/client/logout";

        Map<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put("tgt", oauthCall.tgt);

        OkHttp3_0Utils.getInstance().asyncGetOkHttp(url, paramMap, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                QZXTools.logE("??????", null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resultJson = response.body().string();//??????????????????response.body().string()
                    //?????????????????????
                    SharedPreferences sharedPreferences = getSharedPreferences("student_info", MODE_PRIVATE);
                    UserUtils.setBooleanTypeSpInfo(sharedPreferences, "isLoginIn", false);
                    UserUtils.setOauthId(sharedPreferences, "oauth_id", "");
                    UserUtils.removeTgt();

                    SharedPreferenceUtil.getInstance(MyApplication.getInstance()).setString("getTgt","");
                    QZXTools.logE("response=" + resultJson, null);
                }
            }
        });
    }
}

