
package com.telit.zhkt_three.Activity.HomeScreen;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.telit.zhkt_three.Activity.BaseActivity;
import com.telit.zhkt_three.Activity.OauthMy.ProviceActivity;
import com.telit.zhkt_three.Adapter.HomeViewPagerAdapter;
import com.telit.zhkt_three.Adapter.vp_transformer.CustomPageTransformer;
import com.telit.zhkt_three.Constant.Constant;
import com.telit.zhkt_three.Constant.UrlUtils;
import com.telit.zhkt_three.CustomView.CircleImageView;
import com.telit.zhkt_three.Fragment.CircleProgressDialogFragment;
import com.telit.zhkt_three.Fragment.Dialog.FileReceiveDialog;
import com.telit.zhkt_three.Fragment.Dialog.TipsDialog;
import com.telit.zhkt_three.Fragment.Dialog.UrlUpdateDialog;
import com.telit.zhkt_three.Fragment.HomeStopOneFragment;
import com.telit.zhkt_three.Fragment.HomeStopTwoFragment;
import com.telit.zhkt_three.Fragment.SysyemFragment;
import com.telit.zhkt_three.Fragment.SysyemFragment1;
import com.telit.zhkt_three.Fragment.SysyemFragment2;
import com.telit.zhkt_three.Fragment.SysyemFragment3;
import com.telit.zhkt_three.JavaBean.AppInfo;
import com.telit.zhkt_three.JavaBean.AppListBean;
import com.telit.zhkt_three.JavaBean.AppUpdate.UpdateBean;
import com.telit.zhkt_three.JavaBean.StudentInfo;
import com.telit.zhkt_three.MyApplication;
import com.telit.zhkt_three.R;
import com.telit.zhkt_three.Service.AppInfoService;
import com.telit.zhkt_three.Utils.ApkListInfoUtils;
import com.telit.zhkt_three.Utils.CheckVersionUtil;
import com.telit.zhkt_three.Utils.Jpush.TagAliasOperatorHelper;
import com.telit.zhkt_three.Utils.OkHttp3_0Utils;
import com.telit.zhkt_three.Utils.QZXTools;
import com.telit.zhkt_three.Utils.UserUtils;
import com.telit.zhkt_three.Utils.eventbus.EventBus;
import com.telit.zhkt_three.Utils.eventbus.Subscriber;
import com.telit.zhkt_three.Utils.eventbus.ThreadMode;
import com.telit.zhkt_three.floatingview.PopWindows;
import com.telit.zhkt_three.greendao.AppInfoDao;
import com.telit.zhkt_three.greendao.StudentInfoDao;
import com.telit.zhkt_three.receiver.AppChangeReceiver;
import com.telit.zhkt_three.receiver.NetworkChangeBroadcastReceiver;
import com.telit.zhkt_three.websocket.JWebSocketClient;
import com.telit.zhkt_three.websocket.JWebSocketClientService;
import com.zbv.basemodel.LingChuangUtils;
import com.zbv.meeting.util.SharedPreferenceUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.jpush.android.api.JPushInterface;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * ????????????????????????SingleInstance,???RVHomeAdapter??????mContext.startActivity(new Intent(mContext, Test2Activity.class));
 * ????????????onPause??????????????????????????????????????????????????????MainActivity
 * <p>
 * UserManager,sInstance????????????????????????
 * <p>
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private Unbinder unbinder;
    @BindView(R.id.home_avatar)
    CircleImageView home_avatar;
    @BindView(R.id.home_nickname)
    TextView home_nickname;
    @BindView(R.id.home_clazz)
    TextView home_clazz;
    @BindView(R.id.home_time)
    TextView home_time;
    @BindView(R.id.home_date)
    TextView home_date;
    @BindView(R.id.home_weekend)
    TextView home_weekend;
    @BindView(R.id.home_wifi)
    ImageView home_wifi;
    @BindView(R.id.home_timetable)
    ImageView home_timetable;


    @BindView(R.id.home_viewpager)
    ViewPager home_viewpager;
    @BindView(R.id.home_dots_linear)
    LinearLayout home_dot_linear;
    @BindView(R.id.home_IndicatorDotView)
    ImageView home_dotview;

    protected static final String[] weekends = {"?????????", "?????????", "?????????", "?????????", "?????????", "?????????", "?????????"};
    private ScheduledExecutorService scheduledExecutorService;
    private HomeViewPagerAdapter homeViewPagerAdapter;
    private List<AppInfo> datas;
    private int vp_page_count;
    private int distance_dots;
    private ScheduledExecutorService timeExecutor;
    private static final int FIREST_UPDATE_VP = 0x7;
    private static final int UPDATE_TIME = 0x8;
    /**
     * ??????????????????Vp
     */
    private boolean isInitVp = true;
    //??????????????????
    private NetworkChangeBroadcastReceiver netConnectChangedReceiver;
    //????????????????????????????????????????????????Activity???????????????????????????????????????????????????????????????????????????
    private static final String HAT_APP_MARKET_CLASS = "com.example.edcationcloud.edcationcloud.activity.DownloadCenterActivity";

    //App???????????????
    private AppChangeReceiver appChangeReceiver;

    private CircleProgressDialogFragment circleProgressDialogFragment;

    private static final int Update_App_Dialog = 0;
    private static final int Close_Ling_Chuang_brocast = 0x98;

    private JWebSocketClient client;
    private JWebSocketClientService.JWebSocketClientBinder binder;
    private JWebSocketClientService jWebSClientService;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Close_Ling_Chuang_brocast:
                    if (executorLingChuangService != null) {
                        executorLingChuangService.shutdownNow();
                    }
                    break;
                case FIREST_UPDATE_VP:
                    if (circleProgressDialogFragment != null) {
                        circleProgressDialogFragment.dismissAllowingStateLoss();
                        circleProgressDialogFragment = null;
                    }
                    initVpAdapter();
                    ApkListInfoUtils.getInstance().onStop();
                    break;

                case TIMES_SEND:
                    //???????????????????????????????????????????????????????????????????????????
                    if (times%2==0){
                        iv_san_suo.setBackgroundColor(getResources().getColor(R.color.transGray));
                    }else {
                        iv_san_suo.setBackgroundColor(getResources().getColor(R.color.colorDarkBlue));
                    }
                    break;
                case UPDATE_TIME:
                    Bundle bundle = (Bundle) msg.obj;
                    home_weekend.setText(bundle.getString("weekend"));
                    home_time.setText(bundle.getString("time"));
                    int month = bundle.getInt("month");
                    int day = bundle.getInt("day");
                    String date = month + "???" + day + "???";
                    home_date.setText(date);
                    break;
                case Update_App_Dialog:
                    //????????????
                    UpdateBean updateBean = (UpdateBean) msg.obj;
                    int currentCode = msg.arg1;
                    TipsDialog tipsDialog = new TipsDialog();
                    tipsDialog.setTipsStyle("?????????????????????\n???????????????" + updateBean.getVersionName() +
                                    "\n???????????????" + QZXTools.getVerionName(MainActivity.this) + "\n???????????????" + updateBean.getDescription(),
                            "????????????", "????????????", -1);
                    tipsDialog.setClickInterface(new TipsDialog.ClickInterface() {
                        @Override
                        public void cancle() {
                            tipsDialog.dismissAllowingStateLoss();
                        }

                        @Override
                        public void confirm() {
                            tipsDialog.dismissAllowingStateLoss();
                            downloadNewApp(updateBean.getUpdateUrl());
                        }
                    });
                    tipsDialog.show(getSupportFragmentManager(), TipsDialog.class.getSimpleName());
                    break;
            }
        }
    };
    private ExecutorService executorLingChuangService;
    private LingChuangAppsListReceiver lingChuangAppsListReceiver;
    private static final int REQUEST_OVERLAY = 4444;
    private ImageView iv_san_suo;
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    private PopWindows popWindows;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        QZXTools.logDeviceInfo(this);
        QZXTools.logE("MainActivity onCreate " + getTaskId(), null);
        //??????Url??????
       // updateUrl();
        //??????????????????????????????
        initLingChuangInfo();
        //????????????????????????
        if (!UserUtils.isLoginIn()) {
            // startActivity(new Intent(this, LoginActivity.class));


            startActivity(new Intent(this, ProviceActivity.class));
            finish();
            return;
        } else {
            //???????????????tat  ????????????
            refreshTgtLogin();

            //????????????
            startJWebSClientService();
            //????????????
            bindService();
        }

        unbinder = ButterKnife.bind(this);

        //-----------------------------------------------------??????????????????????????????
        IntentFilter filter = new IntentFilter();
        //??????wifi?????????????????????????????????????????????
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        //????????????????????????????????????????????????????????????????????????????????????wifi???????????????
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        //??????????????????????????????????????????wifi??????????????????
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netConnectChangedReceiver = new NetworkChangeBroadcastReceiver();
        registerReceiver(netConnectChangedReceiver, filter);
        //-----------------------------------------------------??????????????????????????????
        //-----------------------------------------------------App????????????????????????
        //Andoird8.0+????????????????????????
        IntentFilter appFilter = new IntentFilter();
        appFilter.addAction("android.intent.action.PACKAGE_ADDED");
        appFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        appFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        appFilter.addDataScheme("package");
        appChangeReceiver = new AppChangeReceiver();
        registerReceiver(appChangeReceiver, appFilter);
        //-----------------------------------------------------App???????????????
        //??????AppService
        //????????????o,api26???8.0?????????????????? AppInfoService.enqueueWork
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AppInfoService.enqueueWork(this, new Intent(this, AppInfoService.class));
        } else {
            startService(new Intent(this, AppInfoService.class));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //?????????????????????
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY);
            } else {

            }
        }
        //??????EventBus
        EventBus.getDefault().register(this);
        //??????????????????????????????????????? recent ???
        // LingChuangUtils.getInstance().stopRecent(MyApplication.getInstance());

        StudentInfoDao studentInfoDao = MyApplication.getInstance().getDaoSession().getStudentInfoDao();
        StudentInfo studentInfo = studentInfoDao.queryBuilder().where(StudentInfoDao.Properties.UserId.eq(UserUtils.getUserId())).unique();
        if (studentInfo != null) {
            home_nickname.setText(studentInfo.getStudentName());
            if (studentInfo.getClassName() != null) {
                if (studentInfo.getGradeName() != null) {
                    home_clazz.setText(studentInfo.getGradeName().concat(studentInfo.getClassName()));
                } else {
                    home_clazz.setText(studentInfo.getClassName());
                }
            }
            if (studentInfo.getPhoto() == null) {
                home_avatar.setImageResource(R.mipmap.icon_user);
            } else {
                Glide.with(this).load(studentInfo.getPhoto()).
                        placeholder(R.mipmap.icon_user).error(R.mipmap.icon_user).into(home_avatar);
            }
        }
     /*   StudentInfoDao studentInfoDao = MyApplication.getInstance().getDaoSession().getStudentInfoDao();
        StudentInfo studentInfo = studentInfoDao.queryBuilder().where(StudentInfoDao.Properties.UserId.eq(UserUtils.getUserId())).unique();
        if (studentInfo != null) {

        }*/

        home_nickname.setText(UserUtils.getStudentName());
        home_clazz.setText(UserUtils.getClassName());
        Glide.with(this).load(UserUtils.getAvatarUrl()).
                placeholder(R.mipmap.icon_user).error(R.mipmap.icon_user).into(home_avatar);
        //??????GreenDao?????????????????????????????????????????????
   /*     AppInfoDao appInfoDao = MyApplication.getInstance().getDaoSession().getAppInfoDao();
        //????????????
        datas = appInfoDao.queryBuilder().orderAsc(AppInfoDao.Properties.OrderNum).list();
        if (datas == null || datas.size() <= 0) {
             datas= new ArrayList<>();
            if (circleProgressDialogFragment != null && circleProgressDialogFragment.isVisible()) {
                circleProgressDialogFragment.dismissAllowingStateLoss();
                circleProgressDialogFragment = null;
            }
            circleProgressDialogFragment = new CircleProgressDialogFragment();
            circleProgressDialogFragment.show(getSupportFragmentManager(), CircleProgressDialogFragment.class.getSimpleName());

            //????????????
            ApkListInfoUtils.getInstance().onStart();
             ApkListInfoUtils.getInstance().getAppSystem("lingchang", appsLists);
            //datas = ApkListInfoUtils.getInstance().getAppSystem("lingchang", appsLists);



        } else {
            initVpAdapter();
        }*/


        datas = new ArrayList<>();
        if (circleProgressDialogFragment != null && circleProgressDialogFragment.isVisible()) {
            circleProgressDialogFragment.dismissAllowingStateLoss();
            circleProgressDialogFragment = null;
        }
        circleProgressDialogFragment = new CircleProgressDialogFragment();
        circleProgressDialogFragment.show(getSupportFragmentManager(), CircleProgressDialogFragment.class.getSimpleName());


        //datas = ApkListInfoUtils.getInstance().getAppSystem("lingchang", appsLists);
        home_viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) home_dotview.getLayoutParams();
                layoutParams.leftMargin = (int) (distance_dots * (positionOffset + position)
                        + QZXTools.dp2px(MainActivity.this, 5));
                home_dotview.setLayoutParams(layoutParams);
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        timeExecutor = Executors.newSingleThreadScheduledExecutor();
        timeExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
//                QZXTools.logE("fixedReate", null);
                //????????????????????? ??????????????????????????????????????????????????????????????????????????????????????????UI
                fillWeekAndTime();
            }
        }, 0, 30000, TimeUnit.MILLISECONDS);

        home_avatar.setOnClickListener(this);
        home_wifi.setOnClickListener(this);
        home_timetable.setOnClickListener(this);

        //??????????????????????????????Url??????
        home_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                long curTime = System.currentTimeMillis();
                if (count == 1) {
                    touchFirstTime = curTime;
                }
                if (count == 3 && curTime - touchFirstTime <= 1000) {
                    count = 0;
                    //???????????????????????????IP??????
                    UrlUpdateDialog urlUpdateDialog = new UrlUpdateDialog();
                    urlUpdateDialog.show(getSupportFragmentManager(), UrlUpdateDialog.class.getSimpleName());
                } else if (curTime - touchFirstTime > 1000) {
                    //??????
                    count = 0;
                } else if (count > 3) {
                    //??????
                    count = 0;
                }
            }
        });
    }

    int times=0;
    private Timer timer;
    private static final int TIMES_SEND = 0X100;
    private void showReadTime() {
        if (timer == null) {

            timer = new Timer();
        }
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                // System.out.println("????????????????????????");
                times++;
                //??????????????????
                mHandler.sendEmptyMessage(TIMES_SEND);

            }
        }, 100, 100);
        /*????????????????????????5s??????????????????2s???????????????????????????*/
    }
    private long touchFirstTime;
    private int count;

    @Override
    protected void onResume() {
        super.onResume();

        //????????????????????????
        /*if (!UserUtils.isLoginIn()) {
            startActivity(new Intent(this, ProviceActivity.class));
            finish();
            return;
        }*/
        if (JPushInterface.isPushStopped(MyApplication.getInstance())) {
            QZXTools.logE("JPush resume", null);
            JPushInterface.resumePush(MyApplication.getInstance());
        }
        if (popWindows==null){
            popWindows = new PopWindows(getApplication());
            popWindows.setView(R.layout.popwindoeview)
                    .setGravity(Gravity.LEFT | Gravity.TOP)
                    .setYOffset(600)
                    .show();
            iv_san_suo = (ImageView) popWindows.findViewById(R.id.iv_san_suo);
        }
        showReadTime();

        //????????????????????????
        if (UserUtils.isLoginIn()) {
            //??????????????????
            CheckVersionUtil.getInstance().requestCheckVersion(this);
        }

        //????????????
        ApkListInfoUtils.getInstance().onStart();
        ApkListInfoUtils.getInstance().getAppSystem("lingchang", appsLists);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (unbinder != null) {
            unbinder.unbind();
        }


        if (timeExecutor != null) {
            timeExecutor.shutdown();
            timeExecutor = null;
        }

        //??????????????????
        if (netConnectChangedReceiver != null) {
            unregisterReceiver(netConnectChangedReceiver);
        }


        //??????App?????????????????????
        if (appChangeReceiver != null) {
            unregisterReceiver(appChangeReceiver);
        }
        TagAliasOperatorHelper.getInstance().releaseHandler();
        if (circleProgressDialogFragment != null) {
            circleProgressDialogFragment.dismissAllowingStateLoss();
            circleProgressDialogFragment = null;
        }

        mHandler.removeCallbacksAndMessages(null);

        QZXTools.logE("???????????????",null);

        if (UserUtils.isLoginIn()){
            unBindService();
            stopJWebSClientService();

            SharedPreferences sharedPreferences = getSharedPreferences("student_info", MODE_PRIVATE);
            UserUtils.setBooleanTypeSpInfo(sharedPreferences, "isLoginIn", false);
        }

        super.onDestroy();
    }

    /**
     * ??????Url
     */
    private void updateUrl() {
        String path = QZXTools.getExternalStorageForFiles(this, null) + "/config.txt";
        Properties properties = QZXTools.getConfigProperties(path);
        QZXTools.logE("rootIp=" + properties.getProperty("rootIp"), null);
        QZXTools.logE("path=" + path, null);
        String rootIp = properties.getProperty("rootIp");
   /*     String socketIp = properties.getProperty("socketIp");
        String socketPort = properties.getProperty("socketPort");
        String imgIp = properties.getProperty("imgIp");*/
        String uPAddressIp = properties.getProperty("uPAddressIp");
        if (TextUtils.isEmpty(uPAddressIp)) {
            properties.setProperty("uPAddressIp", UrlUtils.AppUpdate);

            try {
                FileOutputStream fos = new FileOutputStream(path);
                properties.store(new OutputStreamWriter(fos, "UTF-8"),
                        "Config");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(rootIp)) {
            UrlUtils.BaseUrl = rootIp;
        } else {
            //?????????????????????????????????
            properties.setProperty("rootIp", UrlUtils.BaseUrl);
        /*    properties.setProperty("socketIp", UrlUtils.SocketIp);
            properties.setProperty("socketPort", UrlUtils.SocketPort + "");
            properties.setProperty("imgIp", UrlUtils.ImgBaseUrl);*/
            //???????????????url
            properties.setProperty("uPAddressIp", UrlUtils.AppUpdate);

            try {
                FileOutputStream fos = new FileOutputStream(path);
                properties.store(new OutputStreamWriter(fos, "UTF-8"),
                        "Config");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

 /*       if (!TextUtils.isEmpty(socketIp)) {
            UrlUtils.SocketIp = socketIp;
        }

        if (!TextUtils.isEmpty(socketPort)) {
            UrlUtils.SocketPort = Integer.parseInt(socketPort);
        }

        if (!TextUtils.isEmpty(imgIp)) {
            UrlUtils.ImgBaseUrl = imgIp;
        }*/


    }

    /**
     * ???????????????  ???????????????????????????????????????????????????????????????,??????????????????onAppInt(int type)??????
     */
    @Subscriber(tag = Constant.EVENT_TAG_APP, mode = ThreadMode.MAIN)
    public void onAppInt(int type) {
        switch (type) {
            case Constant.APP_NEW_ADD:
            case Constant.APP_UPDATE:
               /* homeViewPagerAdapter.setmList(appInfoDao_update.queryBuilder().orderAsc(AppInfoDao.Properties.OrderNum).list());
                homeViewPagerAdapter.setNeedChange(true);
                homeViewPagerAdapter.notifyDataSetChanged();
                homeViewPagerAdapter.setNeedChange(false);*/

            case Constant.APP_DELETE:
                AppInfoDao appInfoDao_add = MyApplication.getInstance().getDaoSession().getAppInfoDao();
               // homeViewPagerAdapter.setmList(appInfoDao_add.queryBuilder().orderAsc(AppInfoDao.Properties.OrderNum).list());
                /*homeViewPagerAdapter.setNeedChange(true);
                homeViewPagerAdapter.notifyDataSetChanged();
                homeViewPagerAdapter.setNeedChange(false);*/

                datas.clear();
                datas.addAll(appInfoDao_add.queryBuilder().orderAsc(AppInfoDao.Properties.OrderNum).list());
                mHandler.sendEmptyMessage(FIREST_UPDATE_VP);

                break;
              /*  homeViewPagerAdapter.setmList(appInfoDao_del.queryBuilder().orderAsc(AppInfoDao.Properties.OrderNum).list());
                homeViewPagerAdapter.setNeedChange(true);
                homeViewPagerAdapter.notifyDataSetChanged();
                homeViewPagerAdapter.setNeedChange(false);*/

        }
    }

    @Subscriber(tag = "delete_app", mode = ThreadMode.MAIN)
    public void deleteApp(String packageName) {
        /**
         * ???????????????????????????????????????????????????????????????????????????
         * */
        QZXTools.uninstallApk(this, packageName);
    }

    private List<Fragment> fragments = new ArrayList<>();

    private void initVpAdapter() {
        QZXTools.logE("initVpAdapter:2222 " + distance_dots,null);
        int systemCom = 0;
        fragments.clear();
        //??????????????????
        home_viewpager.setPageTransformer(false, new CustomPageTransformer());


        HomeStopOneFragment homeStopOneFragment = new HomeStopOneFragment();
        HomeStopTwoFragment homeStopTwoFragment = new HomeStopTwoFragment();
        fragments.add(homeStopOneFragment);
        fragments.add(homeStopTwoFragment);
        //???????????????fragment
        if (datas.size() > 0 && datas.size() >= 12) {
            if (datas.size() % 12 == 0) {
                systemCom = datas.size() / 12;
            } else {
                systemCom = datas.size() / 12;
                systemCom++;
            }
        } else {
            systemCom = 1;
        }

        AppListBean appListBean = new AppListBean();
        appListBean.setDatas(datas);
        if (systemCom == 1) {
            SysyemFragment sysyemFragment = new SysyemFragment();
            fragments.add(sysyemFragment);

            Bundle bundle = new Bundle();
            bundle.putSerializable("applist", appListBean);
            sysyemFragment.setArguments(bundle);

        } else if (systemCom == 2) {
            SysyemFragment sysyemFragment = new SysyemFragment();
            SysyemFragment1 sysyemFragment1 = new SysyemFragment1();
            fragments.add(sysyemFragment);
            fragments.add(sysyemFragment1);

            Bundle bundle = new Bundle();
            bundle.putSerializable("applist", appListBean);
            sysyemFragment.setArguments(bundle);
            sysyemFragment1.setArguments(bundle);

        } else if (systemCom == 3) {
            SysyemFragment sysyemFragment = new SysyemFragment();
            SysyemFragment1 sysyemFragment1 = new SysyemFragment1();
            SysyemFragment2 sysyemFragment2 = new SysyemFragment2();
            fragments.add(sysyemFragment);
            fragments.add(sysyemFragment1);
            fragments.add(sysyemFragment2);

            Bundle bundle = new Bundle();
            bundle.putSerializable("applist", appListBean);
            sysyemFragment.setArguments(bundle);
            sysyemFragment1.setArguments(bundle);
            sysyemFragment2.setArguments(bundle);
        }else if (systemCom == 4) {
            SysyemFragment sysyemFragment = new SysyemFragment();
            SysyemFragment1 sysyemFragment1 = new SysyemFragment1();
            SysyemFragment2 sysyemFragment2 = new SysyemFragment2();
            SysyemFragment3 sysyemFragment3 = new SysyemFragment3();
            fragments.add(sysyemFragment);
            fragments.add(sysyemFragment1);
            fragments.add(sysyemFragment2);
            fragments.add(sysyemFragment3);

            Bundle bundle = new Bundle();
            bundle.putSerializable("applist", appListBean);
            sysyemFragment.setArguments(bundle);
            sysyemFragment1.setArguments(bundle);
            sysyemFragment2.setArguments(bundle);
            sysyemFragment3.setArguments(bundle);
        }
        homeViewPagerAdapter = new HomeViewPagerAdapter(getSupportFragmentManager(), fragments);
        vp_page_count = homeViewPagerAdapter.totalPage();

        Log.i("qin", "initVpAdapter: " + vp_page_count);

        home_viewpager.setAdapter(homeViewPagerAdapter);
        //????????????????????????????????????????????????
        if (vp_page_count > 0) {
            home_dotview.setVisibility(View.VISIBLE);
            home_dot_linear.removeAllViews();
            for (int i = 0; i < vp_page_count; i++) {
                addDot(false);
            }
            //???onMeasure onLayout???????????????????????????onDraw
            home_dotview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    //????????????????????????
                    home_dotview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    distance_dots = home_dot_linear.getChildAt(1).getLeft() - home_dot_linear.getChildAt(0).getLeft();

                }
            });
        } else {
            home_dotview.setVisibility(View.GONE);
        }
    }

    /**
     * ?????????????????????
     */
    private void fillWeekAndTime() {
        Date date = new Date();
        int week = QZXTools.judgeWeekFromDate(date);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        String time = simpleDateFormat.format(date);

        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Message message = mHandler.obtainMessage();
        message.what = UPDATE_TIME;
        Bundle bundle = new Bundle();
        bundle.putString("weekend", weekends[week - 1]);
        bundle.putInt("month", month);
        bundle.putInt("day", day);
        bundle.putString("time", time);
        message.obj = bundle;
        mHandler.sendMessage(message);
    }

    /**
     * ????????????dot
     */
    private void addDot(boolean isEnable) {
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.shape_enable_dot);
        imageView.setPadding(QZXTools.dp2px(this, 5),
                QZXTools.dp2px(this, 5),
                QZXTools.dp2px(this, 5),
                QZXTools.dp2px(this, 5));
        home_dot_linear.addView(imageView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_avatar:
                startActivity(new Intent(this, PersonInfoActivity.class));
                overridePendingTransition(R.anim.activity_enter_from_left_to_right, R.anim.out_fade);
                break;
            case R.id.home_wifi:
                QZXTools.enterWifiSetting(this);
                break;
            case R.id.home_timetable:
                startActivity(new Intent(this, TimeTableActivity.class));
                break;
        }
    }

    //----------------------------------------------AppUpdate-------------------------------------

    private String installFilePath;

    @Subscriber(tag = Constant.CAN_INSTALL, mode = ThreadMode.MAIN)
    public void getInstallPath(String path) {
        installFilePath = path;
    }

    /**
     * ??????????????????
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.INSTALL_PACKAGES_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    QZXTools.logE("onRequestPermissionsResult ?????????????????????", null);
                    QZXTools.installApk(this, installFilePath);

                } else {
                    QZXTools.logE("onRequestPermissionsResult ????????????????????????????????????", null);
                    //  ????????????????????????????????????
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    startActivityForResult(intent, Constant.GET_UNKNOWN_APP_SOURCES);
                }
                break;
            default:
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.GET_UNKNOWN_APP_SOURCES) {
            QZXTools.logE("onActivityResult GET_UNKNOWN_APP_SOURCES", null);
            if (Build.VERSION.SDK_INT >= 26) {
                boolean b = getPackageManager().canRequestPackageInstalls();
                if (b) {
                    QZXTools.installApk(this, installFilePath);
                } else {
                    QZXTools.popToast(this, "??????????????????????????????", false);
                }
            }
        }
    }

    /**
     * ??????????????????App
     */
    private void downloadNewApp(String downloadUrl) {
        FileReceiveDialog fileReceiveDialog = new FileReceiveDialog();
        fileReceiveDialog.setFileBodyString(true, downloadUrl, null);
        fileReceiveDialog.show(getSupportFragmentManager(), FileReceiveDialog.class.getSimpleName());
    }

    //------------------------------------------------????????????

    /**
     * todo ??????????????????apk????????????????????????????????????apk
     */
    @Override
    public void onBackPressed() {
    }

    /**
     * ??????????????????????????????????????????????????????
     */
    @Subscriber(tag = Constant.Update_Avatar, mode = ThreadMode.MAIN)
    public void updateAvatar(String urlImg) {
        Glide.with(this).load(urlImg).
                placeholder(R.mipmap.icon_user).error(R.mipmap.icon_user).into(home_avatar);
    }

    private void initLingChuangInfo() {
        //??????home ???

       // LingChuangUtils.getInstance().startHome(MyApplication.getInstance());
        // ?????? recent ???(
       // LingChuangUtils.getInstance().startRecent(MyApplication.getInstance());
        //??????back ???
       // LingChuangUtils.getInstance().startBack(MyApplication.getInstance());
        // LingChuangUtils.getInstance().stopDisablenavigationbar(MyApplication.getInstance());
        //???????????????app ?????????
      /*  lingChuangAppsListReceiver = new LingChuangAppsListReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.launcher3.mdm.hide_show_apps");
        registerReceiver(lingChuangAppsListReceiver, filter);*/
        //????????????app ??????
        /*LingChuangSystemReceiver lingChuangSystemReceiver=new LingChuangSystemReceiver();
        IntentFilter intentFilterSystem=new IntentFilter();
        intentFilterSystem.addAction("com.android.launcher3.mdm.control_default_apps");
        registerReceiver(lingChuangSystemReceiver,intentFilterSystem);*/

        //?????????home ?????????10???
        executorLingChuangService = ApkListInfoUtils.getInstance().onStart();
        QZXTools.logE("initLingChuangInfo: " + executorLingChuangService,null);
        executorLingChuangService.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i <= 15; i++) {
                    try {
                        Thread.sleep(2000);
                        //????????????home?????????
                        lingChangHomeAction();

                        if (i == 15) {
                            Message message = Message.obtain();
                            message.what = Close_Ling_Chuang_brocast;
                            mHandler.sendMessage(message);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }


            }
        });


    }

    private List<String> appsLists = new ArrayList<>();

    private class LingChuangAppsListReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            appsLists.clear();
            //??????????????????10???   ????????????10???home?????????????????????  ????????????????????????app ?????????
            List<String> hide_show_apps = intent.getStringArrayListExtra("hide_show_apps");

            QZXTools.logE("onReceive: " + hide_show_apps,null);
            if (hide_show_apps != null) {
                for (String app : hide_show_apps) {
                    appsLists.add(app);
                }
            }
            initlingchaungList(appsLists);
        }


    }

    private List<String> appsSystems = new ArrayList<>();

    private class LingChuangSystemReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            appsSystems.clear();
            List<Map<String, Integer>> systemData = (List<Map<String, Integer>>) intent.getSerializableExtra("apps_status_list");
            QZXTools.logE( "onReceive: " + systemData,null);
        }
    }
    private void initlingchaungList(List<String> appsListss) {
        ApkListInfoUtils.getInstance().onStart();
        if (!datas.isEmpty() && datas.size() > 0) {
            datas.clear();
        }
        appsListss.add("com.ndwill.swd.appstore");
        ApkListInfoUtils.getInstance().getAppSystem("lingchang", appsListss);
    }

    @Subscriber(tag = Constant.LINGCHUANG_APP_LIST, mode = ThreadMode.MAIN)
    public void lingChuangList(List<AppInfo> appInfoList) {
        QZXTools.logE("lingChuangList: " + appInfoList,null);
       /* if (homeViewPagerAdapter != null) {
            homeViewPagerAdapter.notifyDataSetChanged();
        }else {

           // initVpAdapter();
        }*/

        datas.clear();
        datas.addAll(appInfoList);
        mHandler.sendEmptyMessage(FIREST_UPDATE_VP);

    }

    private void lingChangHomeAction() {
        Intent intent = new Intent("com.linspirer.edu.homeaction");
        intent.setPackage("com.android.launcher3");
        sendBroadcast(intent);
    }

    //????????????
    private void refreshTgtLogin() {
        String url = "http://open.ahjygl.gov.cn/sso-oauth/client/refreshTgt";
        String getTgt = SharedPreferenceUtil.getInstance(MyApplication.getInstance()).getString("getTgt");
        String deviceId = SharedPreferenceUtil.getInstance(MyApplication.getInstance()).getString("deviceId");
        Map<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put("tgt", getTgt);
        paramMap.put("client", "pc");//?????????????????????
        paramMap.put("deviceId", deviceId);

        QZXTools.logE("paramMap:"+new Gson().toJson(paramMap),null);

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
                    if (TextUtils.isEmpty(resultJson)){
                        Gson gson = new Gson();
                        Map<String, Object> map = gson.fromJson(resultJson, new TypeToken<Map<String, Object>>() {
                        }.getType());
                        if (map.get("code").equals("1")) {
                            getCallback();
                        } else {
                            //????????????????????????
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(MainActivity.this, ProviceActivity.class));
                                    finish();
                                }
                            });
                        }
                    }
                }
            }
        });


    }

    public void getCallback() {
        String getTgt = SharedPreferenceUtil.getInstance(MyApplication.getInstance()).getString("getTgt");
        String deviceId = SharedPreferenceUtil.getInstance(MyApplication.getInstance()).getString("deviceId");
        String url = "http://open.ahjygl.gov.cn/sso-oauth/client/validateTgt";
        Map<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put("appkey", Constant.EduAuthAppKey);
        paramMap.put("tgt", getTgt);
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
                    /**
                     *
                     * response={"code":"1","message":"success","data":"9c5e8fc2cc6e5a197b4ea823497f3da719ce42bcab3b04bf532573912beb97da4826d86417934d5f5d4a9af096137783f2e175af23ffe065","success":true}
                     * */
                    String resultJson = response.body().string();//??????????????????response.body().string()
                    QZXTools.logE("response=" + resultJson, null);
                    Gson gson = new Gson();
                    Map<String, Object> map = gson.fromJson(resultJson, new TypeToken<Map<String, Object>>() {
                    }.getType());
                    QZXTools.logE("data=" + map.get("data"), null);
                    if (map.get("code").equals("1")) {
                        //?????????????????????tgt
                        SharedPreferences sharedPreferences = getSharedPreferences("tgtLogin", MODE_PRIVATE);
                        //   intent.putExtra("data_userid", (String) map.get("data"));
/*
                        requestOauthLogin((String) map.get("data"));

                        sp_student.edit().putString("oauth_id", (String) map.get("data")).commit();*/
                    } else if (map.get("code").equals("-1")) {
                        //???????????????tgt
                       // loginOut();
                    }
                   /* setResult(RESULT_OK, intent);
                    finish();*/


                }
            }
        });

    }

    public void loginOut() {
        String url = "http://open.ahjygl.gov.cn/sso-oauth/client/logout";
        String getTgt = SharedPreferenceUtil.getInstance(MyApplication.getInstance()).getString("getTgt");
        Map<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put("tgt", getTgt);

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

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            QZXTools.logE("???????????????????????????",null);
            binder = (JWebSocketClientService.JWebSocketClientBinder) iBinder;
            jWebSClientService = binder.getService();
            client = jWebSClientService.client;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            QZXTools.logE( "???????????????????????????",null);
        }
    };

    /**
     * ????????????
     */
    private void bindService() {
        Intent bindIntent = new Intent(this, JWebSocketClientService.class);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    /**
     * ????????????
     */
    private void unBindService(){
        if (serviceConnection!=null){
            unbindService(serviceConnection);
        }
    }

    /**
     * ???????????????websocket??????????????????
     */
    private void startJWebSClientService() {
        Intent intent = new Intent(this, JWebSocketClientService.class);
        startService(intent);
    }

    /**
     * ???????????????websocket??????????????????
     */
    private void stopJWebSClientService() {
        Intent intent = new Intent(this, JWebSocketClientService.class);
        stopService(intent);
    }
}
