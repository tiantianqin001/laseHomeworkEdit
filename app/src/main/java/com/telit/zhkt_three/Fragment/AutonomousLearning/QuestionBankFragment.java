package com.telit.zhkt_three.Fragment.AutonomousLearning;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.telit.zhkt_three.Activity.AutonomousLearning.ItemBankKnowledgeActivity;
import com.telit.zhkt_three.Adapter.AutoLearning.RVAutoLearningAdapter;
import com.telit.zhkt_three.Constant.UrlUtils;
import com.telit.zhkt_three.CustomView.ToUsePullView;
import com.telit.zhkt_three.Fragment.BaseFragment;
import com.telit.zhkt_three.Fragment.CircleProgressDialogFragment;
import com.telit.zhkt_three.JavaBean.AutonomousLearning.QuestionGrade;
import com.telit.zhkt_three.JavaBean.AutonomousLearning.QuestionKnowledge;
import com.telit.zhkt_three.JavaBean.AutonomousLearning.QuestionParam;
import com.telit.zhkt_three.JavaBean.AutonomousLearning.QuestionSection;
import com.telit.zhkt_three.JavaBean.AutonomousLearning.QuestionSubject;
import com.telit.zhkt_three.JavaBean.Gson.KnowledgeParamBean;
import com.telit.zhkt_three.JavaBean.Gson.KnowledgeSectionBean;
import com.telit.zhkt_three.JavaBean.Gson.ResourceInfoBean;
import com.telit.zhkt_three.JavaBean.Resource.FillResource;
import com.telit.zhkt_three.JavaBean.Resource.ResourceBean;
import com.telit.zhkt_three.MyApplication;
import com.telit.zhkt_three.R;
import com.telit.zhkt_three.Utils.OkHttp3_0Utils;
import com.telit.zhkt_three.Utils.QZXTools;
import com.telit.zhkt_three.Utils.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * *****************************************************************
 * author: Administrator
 * time: 2021/4/6 14:12
 * name;
 * overview:
 * usage: ??????
 * ******************************************************************
 */
public class QuestionBankFragment extends BaseFragment implements View.OnClickListener, ToUsePullView.SpinnerClickInterface,
        XRecyclerView.LoadingListener{
    private XRecyclerView xRecyclerView;

    private RelativeLayout press_layout;
    private RelativeLayout select_layout;

    //????????????
    private LinearLayout layout_pull_all;
    private LinearLayout layout_pull;
    private ToUsePullView subject_view;
    private ToUsePullView section_view;
    private ToUsePullView grade_view;
    private ToUsePullView select_view;
    private ToUsePullView press_view;

    //???????????????????????????
    private ImageView leak_resource;
    private LinearLayout leak_net_layout;
    private TextView link_network;

    //??????????????????
    private CircleProgressDialogFragment circleProgressDialogFragment;


    private RVAutoLearningAdapter rvAutoLearningAdapter;
    private List<FillResource> fillResourceList;

    //?????????key???????????????
    //??????
    private Map<String, String> sectionMap;
    //??????
    private Map<String, String> subjectMap;
    //??????
    private Map<String, String> gradeMap;
    //?????????
    private Map<String, String> pressMap;

    private static final int Server_Error = 0;
    private static final int Error404 = 1;
    private static final int Operate_Section_Success = 2;
    private static final int Operate_Subject_Grade_Success = 3;
    private static final int Operate_Resource_Condition_Success = 4;
    private static final int Operate_Resource_Success = 5;

    private static boolean isShow=false;

    /**
     * ????????????????????????
     */
    private int countRequest;
    private int countAdd;

    private Handler mHandler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Server_Error:
                    if (isShow){
                        QZXTools.popToast(getActivity(), "??????????????????....", false);
                        if (circleProgressDialogFragment != null) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }

                        if (xRecyclerView != null) {
                            xRecyclerView.refreshComplete();
                            xRecyclerView.loadMoreComplete();
                        }
                    }

                    break;
                case Error404:
                    if (isShow){
                        QZXTools.popToast(getActivity(), "?????????????????????", false);
                        if (circleProgressDialogFragment != null) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }
                    }

                    break;
                case Operate_Section_Success:
                    if (isShow){
                        List<String> sectionList = new ArrayList<String>(sectionMap.keySet());
                        section_view.setDataList(sectionList);
                    }

                    break;
                case Operate_Subject_Grade_Success:
                    if (isShow){
                        countAdd++;

                        List<String> subjectList = new ArrayList<String>(subjectMap.keySet());
                        subject_view.setDataList(subjectList);

                        List<String> gradeList = new ArrayList<String>(gradeMap.keySet());
                        grade_view.setDataList(gradeList);

                        List<String> pressList = new ArrayList<String>(pressMap.keySet());
                        press_view.setDataList(pressList);
                        if (pressList != null && pressList.size() > 0) {
                            press_view.setPullContent(pressList.get(0));
                        }

                        if (countAdd == countRequest) {
                            if (circleProgressDialogFragment != null) {
                                circleProgressDialogFragment.dismissAllowingStateLoss();
                                circleProgressDialogFragment = null;
                            }
                        }

                        //???????????????????????????
                        if (xRecyclerView != null) {
                            xRecyclerView.refreshComplete();
                            xRecyclerView.loadMoreComplete();
                        }

                        if (fillResourceList.size() > 0) {
                            leak_resource.setVisibility(View.GONE);
                        } else {
                            leak_resource.setVisibility(View.VISIBLE);
                        }
                        rvAutoLearningAdapter.notifyDataSetChanged();

                        //????????????
                        xRecyclerView.setNoMore(true);
                    }

                    break;
                case Operate_Resource_Success:
                    if (isShow){
                        if (circleProgressDialogFragment != null) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }

                        if (xRecyclerView != null) {
                            xRecyclerView.refreshComplete();
                            xRecyclerView.loadMoreComplete();
                        }

                        if (fillResourceList.size() > 0) {
                            leak_resource.setVisibility(View.GONE);
                        } else {
                            leak_resource.setVisibility(View.VISIBLE);
                        }
                        rvAutoLearningAdapter.notifyDataSetChanged();
                    }

                    break;
            }
        }
    };

    /**
     * ???????????????
     *
     * @return
     */
    public static QuestionBankFragment newInstance() {
        QuestionBankFragment fragment = new QuestionBankFragment();
        return fragment;
    }

    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question_bank, container, false);

        initView(view);
        initData();
        initListener();

        return view;
    }

    private void initView(View view){
        xRecyclerView = view.findViewById(R.id.learning_xRecycler);

        layout_pull_all = view.findViewById(R.id.learning_pull_all);
        layout_pull = view.findViewById(R.id.learning_pull_layout);
        subject_view = view.findViewById(R.id.learning_pull_subject);
        section_view = view.findViewById(R.id.learning_pull_section);
        grade_view = view.findViewById(R.id.learning_pull_grade);
        select_view = view.findViewById(R.id.learning_pull_select);
        press_view = view.findViewById(R.id.learning_pull_press);
        press_layout = view.findViewById(R.id.learning_pull_press_layout);
        select_layout = view.findViewById(R.id.learning_pull_select_layout);

        leak_resource = view.findViewById(R.id.leak_resource);
        leak_net_layout = view.findViewById(R.id.leak_net_layout);
        link_network = view.findViewById(R.id.link_network);
    }

    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initData(){
        isShow=true;

        fillResourceList = new ArrayList<>();
        xRecyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);
        xRecyclerView.getDefaultFootView().setNoMoreHint("");
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 4);
        xRecyclerView.setLayoutManager(gridLayoutManager);
        xRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        rvAutoLearningAdapter = new RVAutoLearningAdapter(getActivity(), fillResourceList,"1");
        xRecyclerView.setAdapter(rvAutoLearningAdapter);

        xRecyclerView.setItemViewCacheSize(20);

        subjectMap = new LinkedHashMap<>();
        sectionMap = new LinkedHashMap<>();
        gradeMap = new LinkedHashMap<>();
        pressMap = new LinkedHashMap<>();

        List<String> papers = new ArrayList<>();
        papers.add("?????????");
        papers.add("??????");
        select_view.setDataList(papers);

        resetViewInterface(true);

        //???????????????????????????????????????????????????
        fetchNetworkForSection();
    }

    private void initListener(){
        xRecyclerView.setLoadingListener(this);

        link_network.setOnClickListener(this);

        subject_view.setSpinnerClick(this);
        section_view.setSpinnerClick(this);
        select_view.setSpinnerClick(this);
        grade_view.setSpinnerClick(this);
        press_view.setSpinnerClick(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.link_network:
                QZXTools.enterWifiSetting(getActivity());
                break;
            case R.id.learning_pull_all:
                //????????????????????????????????????????????????
                if (subject_view.pullViewPopShown() || section_view.pullViewPopShown()
                        || grade_view.pullViewPopShown()
                        || press_view.pullViewPopShown()) {
                    return;
                }
                layout_pull_all.setVisibility(View.GONE);
                break;
        }
    }

    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void spinnerClick(View parent, String text) {
        switch (parent.getId()) {
            case R.id.learning_pull_subject:
                subject_view.setPullContent(text);
                break;
            case R.id.learning_pull_section:
                section_view.setPullContent(text);
                break;
            case R.id.learning_pull_grade:
                grade_view.setPullContent(text);
                break;
            case R.id.learning_pull_select:
                //????????????????????????????????????????????????
                if (text.equals(getResources().getString(R.string.knowledge_point))) {
                    if (subject_view.getPullContent().equals("") || section_view.getPullContent().equals("")) {
                        QZXTools.popToast(getActivity(), "???????????????????????????", false);
                        return;
                    }
                    select_view.setPullContent(text);
                    press_layout.setVisibility(View.GONE);
                    //??????????????????????????????????????????,?????????????????????
                    Intent intent = new Intent(getActivity(), ItemBankKnowledgeActivity.class);
                    //?????????????????????
                    String learning_section = sectionMap.get(section_view.getPullContent());
                    String subject = subjectMap.get(subject_view.getPullContent());
                    QZXTools.logE("learning_section=" + learning_section + ";subject=" + subject, null);
                    intent.putExtra("learning_section", learning_section);
                    intent.putExtra("subject", subject);
                    startActivity(intent);
                    return;
                } else if (text.equals(getResources().getString(R.string.teaching_material))) {
                    if (section_view.getPullContent().equals("")) {
                        QZXTools.popToast(getActivity(), "??????????????????", false);
                        return;
                    }
                    select_view.setPullContent(text);
                    press_layout.setVisibility(View.VISIBLE);

                    type = "1010";

                    fetchNetworkForSubjectGrade(sectionMap.get(section_view.getPullContent()), "1",
                            subjectMap.get(subject_view.getPullContent()), pressMap.get(press_view.getPullContent()));
                    return;
                }
                break;
            case R.id.learning_pull_press:
                press_view.setPullContent(text);
                break;
        }
        //????????????---??????????????????
        if (isItemBank) {
            if (subject_view.getPullContent().equals("") || section_view.getPullContent().equals("")
                    || press_view.getPullContent().equals("")) {
                return;
            }
            fetchNetworkForSubjectGrade(sectionMap.get(section_view.getPullContent()), "1",
                    subjectMap.get(subject_view.getPullContent()), pressMap.get(press_view.getPullContent()));
        } else {
            fetchNetworkForResourceContent(false, subjectMap.get(subject_view.getPullContent()),
                    gradeMap.get(grade_view.getPullContent()), sectionMap.get(section_view.getPullContent()),
                    pressMap.get(press_view.getPullContent()));
        }
    }

    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRefresh() {
        try {
            curPageNo = 1;
            if (isItemBank) {
                fetchNetworkForSubjectGrade(sectionMap.get(section_view.getPullContent()), "1",
                        subjectMap.get(subject_view.getPullContent()), pressMap.get(press_view.getPullContent()));
            } else {
                fetchNetworkForResourceContent(false, subjectMap.get(subject_view.getPullContent()),
                        gradeMap.get(grade_view.getPullContent()), sectionMap.get(section_view.getPullContent()),
                        pressMap.get(press_view.getPullContent()));
            }
        }catch (Exception e){
            e.fillInStackTrace();
            QZXTools.popToast(MyApplication.getInstance(),"???????????????",true);
        }
    }

    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onLoadMore() {
        try {
            curPageNo++;
            if (isItemBank) {
            } else {
                fetchNetworkForResourceContent(true, subjectMap.get(subject_view.getPullContent()),
                        gradeMap.get(grade_view.getPullContent()), sectionMap.get(section_view.getPullContent()),
                        pressMap.get(press_view.getPullContent()));
            }
        }catch (Exception e){
            e.fillInStackTrace();
            QZXTools.popToast(MyApplication.getInstance(),"???????????????",true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isShow=false;

        EventBus.getDefault().unregister(this);

        if (circleProgressDialogFragment != null) {
            circleProgressDialogFragment.dismissAllowingStateLoss();
            circleProgressDialogFragment = null;
        }

        //??????????????????
        mHandler.removeCallbacksAndMessages(null);
        OkHttp3_0Utils.getInstance().cleanHandler();
        QZXTools.setmToastNull();
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void fetchNetworkForSection() {
        subjectMap.clear();
        sectionMap.clear();
        gradeMap.clear();
        pressMap.clear();

        subject_view.setHintText("????????????");
        section_view.setHintText("????????????");
        select_view.setHintText("????????????");
        grade_view.setHintText("????????????");
        press_view.setHintText("?????????");

        select_layout.setVisibility(View.VISIBLE);
        press_layout.setVisibility(View.GONE);

        countAdd = 0;

        if (circleProgressDialogFragment != null && circleProgressDialogFragment.isVisible()) {
            circleProgressDialogFragment.dismissAllowingStateLoss();
            circleProgressDialogFragment = null;
        }
        circleProgressDialogFragment = new CircleProgressDialogFragment();
        circleProgressDialogFragment.show(getChildFragmentManager(), CircleProgressDialogFragment.class.getSimpleName());
        String url = UrlUtils.BaseUrl + UrlUtils.QueryKnowledgeSection;

        OkHttp3_0Utils.getInstance().asyncGetOkHttp(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //???????????????
                mHandler.sendEmptyMessage(Server_Error);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String resultJson = response.body().string();
//                            QZXTools.logE("resultJson=" + resultJson, null);
                        Gson gson = new Gson();
                        KnowledgeSectionBean knowledgeSectionBean = gson.fromJson(resultJson, KnowledgeSectionBean.class);
//                    QZXTools.logE("knowledgeSectionBean=" + knowledgeSectionBean, null);

                        countRequest = knowledgeSectionBean.getResult().size();

                        for (int i = 0; i < knowledgeSectionBean.getResult().size(); i++) {
                            QuestionSection questionSection = knowledgeSectionBean.getResult().get(i);
                            sectionMap.put(questionSection.getXdName(), questionSection.getXd() + "");
                            //??????????????????????????????
                            fetchNetworkForSubjectGrade(questionSection.getXd() + "", "0", null, null);
                        }
                        mHandler.sendEmptyMessage(Operate_Section_Success);
                    }catch (Exception e){
                        if (circleProgressDialogFragment != null && circleProgressDialogFragment.isVisible()) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }
                        mHandler.sendEmptyMessage(Error404);
                    }

                } else {
                    mHandler.sendEmptyMessage(Error404);
                }
            }
        });
    }

    /**
     * ???????????????
     */
    private boolean isItemBank = false;

    private void fetchNetworkForSubjectGrade(String xd, String isSearchChapter, String subjectId, String pressId) {
        String url = UrlUtils.BaseUrl + UrlUtils.QueryKnowledgeSubjectGrade;
        Map<String, String> mapParams = new LinkedHashMap<>();
        mapParams.put("xd", xd);
        // 0:???????????????  1????????????
        mapParams.put("isSearchChapter", isSearchChapter);
        if (!TextUtils.isEmpty(subjectId)) {
            mapParams.put("chid", subjectId);
        }

        if (!TextUtils.isEmpty(pressId)) {
            mapParams.put("press", pressId);
        }

        /**
         * post????????????????????????int????????????????????????????????????????????????????????????
         * */
        //??????????????????
        OkHttp3_0Utils.getInstance().asyncPostOkHttp(url, mapParams, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //???????????????
                mHandler.sendEmptyMessage(Server_Error);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {

                    try {
                        String resultJson = response.body().string();
                        QZXTools.logE("resultJson=" + resultJson, null);
                        Gson gson = new Gson();
                        KnowledgeParamBean knowledgeSubjectGradeBean = gson.fromJson(resultJson, KnowledgeParamBean.class);
//                    QZXTools.logE("knowledgeSubjectGradeBean=" + knowledgeSubjectGradeBean, null);

                        QuestionParam questionParam = knowledgeSubjectGradeBean.getResult();

                        //??????
                        if (questionParam.getQuestionSubjects() != null && questionParam.getQuestionSubjects().size() > 0) {
                            for (int i = 0; i < questionParam.getQuestionSubjects().size(); i++) {
                                QuestionSubject questionSubject = knowledgeSubjectGradeBean.getResult().getQuestionSubjects().get(i);
                                subjectMap.put(questionSubject.getChname(), questionSubject.getChid() + "");
                            }
                        }

                        //??????
                        if (questionParam.getQuestionGrades() != null && questionParam.getQuestionGrades().size() > 0) {
                            for (int i = 0; i < questionParam.getQuestionGrades().size(); i++) {
                                QuestionGrade questionGrade = knowledgeSubjectGradeBean.getResult().getQuestionGrades().get(i);
                                gradeMap.put(questionGrade.getGradeName(), questionGrade.getGradeId() + "");
                            }
                        }

                        //?????????
                        if (questionParam.getQuestionEdition() != null && questionParam.getQuestionEdition().size() > 0) {
                            for (int i = 0; i < questionParam.getQuestionEdition().size(); i++) {
                                QuestionKnowledge questionKnowledge = questionParam.getQuestionEdition().get(i);
                                //?????????????????????id
                                pressMap.put(questionKnowledge.getName(), questionKnowledge.getKnowledgeId() + "");
                            }
                        }

                        //????????????QuestionGradeVolum
                        if (questionParam.getQuestionGradeVolum() != null && questionParam.getQuestionGradeVolum().size() > 0) {

                            //?????????????????????
                            fillResourceList.clear();

                            for (int i = 0; i < questionParam.getQuestionGradeVolum().size(); i++) {
                                //???QuestionKnowledge?????????FillResource
                                QuestionKnowledge questionKnowledge = questionParam.getQuestionGradeVolum().get(i);

                                FillResource fillResource = new FillResource();
                                fillResource.setCover("");
                                fillResource.setGradename(questionKnowledge.getName());
                                fillResource.setTermname("");

                                displayItemBankText(questionKnowledge.getParentId(), questionKnowledge.getXd(), questionKnowledge.getChid());

                                fillResource.setPressname(pressName);
                                fillResource.setSubjectName(subjectName);

                                if (type.equals("1010")) {
                                    fillResource.setTeachingMaterial(true);
                                } else {
                                    fillResource.setTeachingMaterial(false);
                                }
                                fillResource.setTitle(TitleText);
                                fillResource.setType(type);
                                fillResource.setItemBank(true);


                                fillResource.setChid(questionKnowledge.getChid());
                                fillResource.setKnowledgeId(questionKnowledge.getKnowledgeId() + "");
                                fillResource.setXd(questionKnowledge.getXd());
                                fillResource.setId(questionKnowledge.getId()+"");

                                fillResourceList.add(fillResource);
                            }
                        }

                        mHandler.sendEmptyMessage(Operate_Subject_Grade_Success);
                    }catch (Exception e){
                        e.fillInStackTrace();
                        if (circleProgressDialogFragment != null && circleProgressDialogFragment.isVisible()) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }
                        mHandler.sendEmptyMessage(Error404);
                    }

                } else {
                    mHandler.sendEmptyMessage(Error404);
                }
            }
        });
    }

    private String pressName;
    private String subjectName;
    private String sectionName;
    private String TitleText;

    /**
     * ????????????????????????
     */
    private void displayItemBankText(String parentId, String xd, String chid) {
        //??????parentId?????????????????????
        Iterator<Map.Entry<String, String>> iterator = pressMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            if (entry.getValue().equals(parentId)) {
                pressName = entry.getKey();
                break;
            }
        }
        //??????xd??????????????????
        Iterator<Map.Entry<String, String>> iterator_xd = sectionMap.entrySet().iterator();
        while (iterator_xd.hasNext()) {
            Map.Entry<String, String> entry = iterator_xd.next();
            if (entry.getValue().equals(xd)) {
                sectionName = entry.getKey();
                break;
            }
        }
        //??????chid????????????
        Iterator<Map.Entry<String, String>> iterator_xk = subjectMap.entrySet().iterator();
        while (iterator_xk.hasNext()) {
            Map.Entry<String, String> entry = iterator_xk.next();
            if (entry.getValue().equals(chid)) {
                subjectName = entry.getKey();
                break;
            }
        }

        //???????????????????????????
        if (!TextUtils.isEmpty(sectionName) && !TextUtils.isEmpty(subjectName)) {
            TitleText = sectionName.concat(subjectName);
        }
    }


    private int curPageNo = 1;
    private String type = "3";

    /**
     * ???????????????????????????
     *
     * @param isLoadingMore ??????????????????????????????????????????????????????list??????
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void fetchNetworkForResourceContent(boolean isLoadingMore, String subjectId, String gradeId, String termId, String pressId) {

        //??????????????????
        if (!QZXTools.isNetworkAvailable()) {
            leak_net_layout.setVisibility(View.VISIBLE);
            return;
        } else {
            leak_net_layout.setVisibility(View.GONE);
        }

        QZXTools.logE("isLoadingMore=" + isLoadingMore + ";subjectId=" + subjectId
                + ";gradeId=" + gradeId + ";termId=" + termId + ";pressId=" + pressId + ";curPageNo="
                + curPageNo + ";type=" + type+";url="+UrlUtils.BaseUrl + UrlUtils.OldResource, null);

        if (!isLoadingMore) {
            fillResourceList.clear();
            //???????????????????????????????????????????????????Scrapped or attached views may not be recycled
            rvAutoLearningAdapter.notifyDataSetChanged();
        }

        if (circleProgressDialogFragment != null && circleProgressDialogFragment.isVisible()) {
            circleProgressDialogFragment.dismissAllowingStateLoss();
            circleProgressDialogFragment = null;
        }
        circleProgressDialogFragment = new CircleProgressDialogFragment();
        circleProgressDialogFragment.show(getChildFragmentManager(), CircleProgressDialogFragment.class.getSimpleName());
        String url = UrlUtils.BaseUrl + UrlUtils.OldResource;

        Map<String, String> paraMap = new LinkedHashMap<>();
        paraMap.put("pageNo", curPageNo + "");
        paraMap.put("pageSize", "30");
        paraMap.put("suffix",type);
        if (!TextUtils.isEmpty(subjectId)) {
            paraMap.put("subjectid", subjectId);
        }

        if (!TextUtils.isEmpty(gradeId)) {
            paraMap.put("gradeid", gradeId);
        }

        if (!TextUtils.isEmpty(termId)) {
            paraMap.put("term", termId);
        }

        if (!TextUtils.isEmpty(pressId)) {
            paraMap.put("press", pressId);
        }

        QZXTools.logE("paraMap:"+new Gson().toJson(paraMap),null);

        OkHttp3_0Utils.getInstance().asyncPostOkHttp(url, paraMap, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //???????????????
                mHandler.sendEmptyMessage(Server_Error);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String resultJson = response.body().string();
                        QZXTools.logE("resultJson=" + resultJson, null);
                        Gson gson = new Gson();
                        ResourceInfoBean resourceInfoBean = gson.fromJson(resultJson, ResourceInfoBean.class);
                        List<ResourceBean> resourceBeanList = resourceInfoBean.getResult();
                        for (int i = 0; i < resourceBeanList.size(); i++) {
                            FillResource fillResource = new FillResource();
                            fillResource.setId(resourceBeanList.get(i).getId());
                            fillResource.setCover(resourceBeanList.get(i).getCover());
                            fillResource.setGradename(resourceBeanList.get(i).getGradename());
                            fillResource.setPressname(resourceBeanList.get(i).getPressname());

                            if (type.equals("1010")) {
                                fillResource.setTeachingMaterial(true);
                            } else {
                                fillResource.setTeachingMaterial(false);
                            }

                            fillResource.setTermname(resourceBeanList.get(i).getTermname());
                            fillResource.setTitle(resourceBeanList.get(i).getTitle());
                            fillResource.setType(type);
                            fillResource.setItemBank(false);
                            fillResource.setSubjectName(subject_view.getPullContent());
                            fillResource.setSubjectId(subjectMap.get(subject_view.getPullContent()));

                            fillResourceList.add(fillResource);
                        }
                        mHandler.sendEmptyMessage(Operate_Resource_Success);
                    }catch (Exception e){
                        e.fillInStackTrace();
                        if (circleProgressDialogFragment != null) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }
                        mHandler.sendEmptyMessage(Server_Error);
                    }

                } else {
                    mHandler.sendEmptyMessage(Error404);
                }
            }
        });
    }

    /**
     * ????????????
     */
    private void resetViewInterface(Boolean isItemBank) {
        //????????????
        curPageNo = 1;
        //?????????????????????
        this.isItemBank = isItemBank;
        //????????????????????????
        fillResourceList.clear();
        rvAutoLearningAdapter.notifyDataSetChanged();
        //??????????????????
        if (isItemBank) {
            subjectMap.clear();
            sectionMap.clear();
            gradeMap.clear();
            pressMap.clear();

            subject_view.setHintText("????????????");
            section_view.setHintText("????????????");
            select_view.setHintText("????????????");
            grade_view.setHintText("????????????");
            press_view.setHintText("?????????");

            subject_view.setPullContent("");
            section_view.setPullContent("");
            select_view.setPullContent("");
            grade_view.setPullContent("");
            press_view.setPullContent("");

            select_layout.setVisibility(View.VISIBLE);
            press_layout.setVisibility(View.GONE);
        } else {
            subjectMap.clear();
            sectionMap.clear();
            gradeMap.clear();
            pressMap.clear();

            subject_view.setHintText("????????????");
            section_view.setHintText("????????????");
            grade_view.setHintText("????????????");
            press_view.setHintText("?????????");

            subject_view.setPullContent("");
            section_view.setPullContent("");
            grade_view.setPullContent("");
            press_view.setPullContent("");

            select_layout.setVisibility(View.GONE);
            press_layout.setVisibility(View.VISIBLE);
        }

        //?????????????????????????????????????????????
        leak_resource.setVisibility(View.VISIBLE);
    }
}
