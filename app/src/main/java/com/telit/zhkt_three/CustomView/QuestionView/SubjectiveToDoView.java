package com.telit.zhkt_three.CustomView.QuestionView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.telit.zhkt_three.Activity.HomeWork.ExtraInfoBean;
import com.telit.zhkt_three.Activity.HomeWork.WhiteBoardActivity;
import com.telit.zhkt_three.Constant.Constant;
import com.telit.zhkt_three.JavaBean.HomeWork.QuestionInfo;
import com.telit.zhkt_three.JavaBean.HomeWorkAnswerSave.LocalTextAnswersBean;
import com.telit.zhkt_three.MediaTools.image.ImageLookActivity;
import com.telit.zhkt_three.MyApplication;
import com.telit.zhkt_three.R;
import com.telit.zhkt_three.Utils.QZXTools;
import com.telit.zhkt_three.Utils.UserUtils;
import com.telit.zhkt_three.Utils.ViewUtils;
import com.telit.zhkt_three.Utils.ZBVPermission;
import com.telit.zhkt_three.Utils.eventbus.EventBus;
import com.telit.zhkt_three.Utils.eventbus.Subscriber;
import com.telit.zhkt_three.Utils.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * author: qzx
 * Date: 2019/5/25 9:38
 * <p>
 * ?????????
 * <p>
 * ????????????????????????/storage/emulated/0/Android/data/com.telit.smartclass.desktop/files/Pictures/IMG_XXXX.jpg
 * <p>
 * ????????????????????????????????????????????????????????????
 * ????????????????????????????????????????????????????????????
 */
public class SubjectiveToDoView extends RelativeLayout implements View.OnClickListener, ZBVPermission.PermPassResult {

    private RelativeLayout subjective_imgs_layout;

    private FrameLayout subjective_answer_frame_one;
    private FrameLayout subjective_answer_frame_two;
    private FrameLayout subjective_answer_frame_three;

    private ImageView subjective_img_one;
    private ImageView subjective_img_two;
    private ImageView subjective_img_three;


    private RelativeLayout subjective_del_layout_one;
    private RelativeLayout subjective_del_layout_two;
    private RelativeLayout subjective_del_layout_three;
    private ImageView subjective_del_one;
    private ImageView subjective_del_two;
    private ImageView subjective_del_three;


    private RelativeLayout subjective_answer_tool_layout;
    private TextView subjective_camera;
    private TextView subjective_board;

    private EditText subjective_input;

    //????????????
    private ArrayList<String> imgFilePathList;

    private Context mContext;

    private QuestionInfo questionInfo;
    private TextView tv_teacher_question;
    private TextView tv_teacher_answer_content;

    /**
     * ????????????????????????????????????
     */
    public void setQuestionInfo(QuestionInfo questionInfo) {
        this.questionInfo = questionInfo;
    }

    private static final String[] needPermissions = {Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public SubjectiveToDoView(Context context) {
        this(context, null);
    }

    public SubjectiveToDoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SubjectiveToDoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        QZXTools.logE("SubjectiveToDoView", null);

        mContext = context;

        View view = LayoutInflater.from(context).inflate(R.layout.subjective_option_complete_layout, this, true);

        subjective_imgs_layout = view.findViewById(R.id.subjective_imgs_layout);

        subjective_answer_frame_one = view.findViewById(R.id.subjective_answer_frame_one);
        subjective_answer_frame_two = view.findViewById(R.id.subjective_answer_frame_two);
        subjective_answer_frame_three = view.findViewById(R.id.subjective_answer_frame_three);

        subjective_img_one = view.findViewById(R.id.subjective_img_one);
        subjective_img_two = view.findViewById(R.id.subjective_img_two);
        subjective_img_three = view.findViewById(R.id.subjective_img_three);

        subjective_del_layout_one = view.findViewById(R.id.subjective_del_layout_one);
        subjective_del_layout_two = view.findViewById(R.id.subjective_del_layout_two);
        subjective_del_layout_three = view.findViewById(R.id.subjective_del_layout_three);

        subjective_del_one = view.findViewById(R.id.subjective_del_one);
        subjective_del_two = view.findViewById(R.id.subjective_del_two);
        subjective_del_three = view.findViewById(R.id.subjective_del_three);


        subjective_answer_tool_layout = view.findViewById(R.id.subjective_answer_tool_layout);
        subjective_camera = view.findViewById(R.id.subjective_camera);
        subjective_board = view.findViewById(R.id.subjective_board);

        subjective_input = view.findViewById(R.id.subjective_input);
        //???????????????
        tv_teacher_question = view.findViewById(R.id.tv_teacher_question);
        tv_teacher_answer_content = view.findViewById(R.id.tv_teacher_answer_content);

        subjective_answer_frame_one.setVisibility(GONE);
        subjective_answer_frame_two.setVisibility(GONE);
        subjective_answer_frame_three.setVisibility(GONE);

        subjective_camera.setOnClickListener(this);
        subjective_board.setOnClickListener(this);

        subjective_img_one.setOnClickListener(this);
        subjective_img_two.setOnClickListener(this);
        subjective_img_three.setOnClickListener(this);

        subjective_del_one.setOnClickListener(this);
        subjective_del_two.setOnClickListener(this);
        subjective_del_three.setOnClickListener(this);

        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "PingFang-SimpleBold.ttf");
        subjective_camera.setTypeface(typeface);
        subjective_board.setTypeface(typeface);
        subjective_input.setTypeface(typeface);

    }

    private boolean isClickCamera = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.subjective_camera:
                if (imgFilePathList.size() >= 3) {
                    QZXTools.popCommonToast(getContext(), "??????????????????????????????", false);
                    return;
                }

                isClickCamera = true;

                //???????????????ID
                TotalQuestionView.subjQuestionId = questionInfo.getId() + "";

                ZBVPermission.getInstance().setPermPassResult(this);
                if (!ZBVPermission.getInstance().hadPermissions((Activity) mContext, needPermissions)) {
                    ZBVPermission.getInstance().requestPermissions((Activity) mContext, needPermissions);
                } else {
                    //??????????????????
                    QZXTools.logD("?????????????????????????????????");
                    openCamera();
                }

                break;
            case R.id.subjective_board:
                Log.i("qin0509", "onClick: subjective_board");
                if (imgFilePathList.size() >= 3) {
                    QZXTools.popCommonToast(getContext(), "??????????????????????????????", false);
                    return;
                }

                isClickCamera = false;

                ZBVPermission.getInstance().setPermPassResult(this);
                if (!ZBVPermission.getInstance().hadPermissions((Activity) mContext, needPermissions)) {
                    ZBVPermission.getInstance().requestPermissions((Activity) mContext, needPermissions);
                } else {
                    Intent intent = new Intent(getContext(), WhiteBoardActivity.class);
                    intent.putExtra("extra_info", questionInfo.getId());
                    getContext().startActivity(intent);
                }
                break;
            case R.id.subjective_img_one:
                showPhotoView(0);
                break;
            case R.id.subjective_img_two:
                showPhotoView(1);
                break;
            case R.id.subjective_img_three:
                showPhotoView(2);
                break;
            case R.id.subjective_del_one:
                imgFilePathList.remove(0);
                //?????????????????????????????????
                showImgsSaveAnswer();
                break;
            case R.id.subjective_del_two:
                imgFilePathList.remove(1);
                showImgsSaveAnswer();
                break;
            case R.id.subjective_del_three:
                imgFilePathList.remove(2);
                showImgsSaveAnswer();
                break;
        }
    }



    /**
     * ?????????????????????
     */
    private void showPhotoView(int curIndex) {
        Intent intent = new Intent(mContext, ImageLookActivity.class);
        intent.putStringArrayListExtra("imgResources", imgFilePathList);
        intent.putExtra("NeedComment", false);
        intent.putExtra("curImgIndex", curIndex);
        mContext.startActivity(intent);
        //??????????????????????????????????????????????????????
//        ActivityOptionsCompat options = ActivityOptionsCompat.
//                makeSceneTransitionAnimation((Activity) mContext, this, "");
//        mContext.startActivity(intent, options.toBundle());
    }

    public static final int CODE_SYS_CAMERA = 1;//????????????RequestCode

    private File cameraFile;

    //???????????????
    private boolean isOpenCamera;

    /**
     * ????????????
     */
    private void openCamera() {
        isOpenCamera = true;

        String fileDir = QZXTools.getExternalStorageForFiles(mContext, Environment.DIRECTORY_PICTURES);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("IMG_");
        stringBuilder.append(simpleDateFormat.format(new Date()));

        stringBuilder.append(".jpg");
        cameraFile = new File(fileDir, stringBuilder.toString());

        Uri cameraUri = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cameraUri = FileProvider.getUriForFile(mContext, mContext.getPackageName()
                    + ".fileprovider", cameraFile);
        } else {
            cameraUri = Uri.fromFile(cameraFile);
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //???????????????????????????????????????????????????Uri??????????????????
        }
        //?????????????????????????????????????????????????????????onActivityResult????????????Intent??????
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
//        cameraIntent.putExtra("extra_info", questionInfo.getId());
        ((Activity) mContext).startActivityForResult(cameraIntent, CODE_SYS_CAMERA);
    }

    /**
     * ???????????????   ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????(KB)
     */
    @Subscriber(tag = Constant.Subjective_Board_Callback, mode = ThreadMode.MAIN)
    public void fromBoardCallback(ExtraInfoBean extraInfoBean) {
        QZXTools.logE("fromBoardCallback ExtraInfoBean=" + extraInfoBean + ";id=" + questionInfo.getId(), null);
        //???????????????????????????????????????SubjectiveToDoView??????????????????????????????
        if (extraInfoBean.getQuestionId().equals(questionInfo.getId())) {
            imgFilePathList.add(extraInfoBean.getFilePath());
            showImgsSaveAnswer();
        }
    }

    /**
     * ???????????????   ???????????????????????????????????????????????????????????????????????????????????????????????????(MB)
     * <p>
     * ??????????????????????????????????????????????????????????????????????????????
     */
    @Subscriber(tag = Constant.Subjective_Camera_Callback, mode = ThreadMode.MAIN)
    public void fromCameraCallback(String flag) {
        if (flag.equals("CAMERA_CALLBACK")) {
            QZXTools.logE("fromCameraCallback filePath=" + cameraFile.getAbsolutePath(), null);
            //???????????????????????????????????????SubjectiveToDoView??????????????????????????????
            if (TotalQuestionView.subjQuestionId.equals(questionInfo.getId() + "")) {
                //??????????????????
//                File compressFile = compressImage(BitmapFactory.decodeFile(cameraFile.getAbsolutePath()));

                //?????????????????? notes:???????????????????????????????????????????????????
                File compressFile = compressBitmapToFile(cameraFile.getAbsolutePath(),
                        getResources().getDimensionPixelSize(R.dimen.x800));

                imgFilePathList.add(compressFile.getAbsolutePath());
                showImgsSaveAnswer();
            }
        }
    }

    /**
     * ???????????????????????????
     */
    public File compressBitmapToFile(String srcPath, float desWidth) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;//?????????,????????????
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float desHeight = desWidth * h / w;
        int be = 1;
        if (w > h && w > desWidth) {
            be = (int) (newOpts.outWidth / desWidth);
        } else if (w < h && h > desHeight) {
            be = (int) (newOpts.outHeight / desHeight);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//???????????????

//        newOpts.inPreferredConfig = Config.ARGB_8888;//?????????????????????,?????????
        newOpts.inPurgeable = true;// ????????????????????????
        newOpts.inInputShareable = true;//???????????????????????????????????????????????????

        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//???????????????????????????100????????????????????????????????????????????????baos???
        String fileDir = QZXTools.getExternalStorageForFiles(mContext, Environment.DIRECTORY_PICTURES);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("IMG_");
        stringBuilder.append(simpleDateFormat.format(new Date()));
        stringBuilder.append(".jpg");
        File file = new File(fileDir, stringBuilder.toString());

        try {
            FileOutputStream fos = new FileOutputStream(file);
            try {
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return file;
    }


    /**
     * ??????????????????????????????
     */
    public void showImgsAndContent(LocalTextAnswersBean localTextAnswersBean, int types,String status) {
        //?????????????????????????????????
        if (localTextAnswersBean == null) {
            imgFilePathList = new ArrayList<>();
        } else {
            imgFilePathList = (ArrayList<String>) localTextAnswersBean.getImageList();

            //??????????????????  ???????????????
            if (types == 1){
                //????????????
                if (status.equals(Constant.Retry_Status)){

                    subjective_del_one.setVisibility(GONE);
                    subjective_del_two.setVisibility(GONE);
                    subjective_del_three.setVisibility(GONE);
                    if (imgFilePathList!=null){
                        subjective_answer_tool_layout.setVisibility(GONE);
                    }
                    if (questionInfo.getOwnList().size()>0){

                        subjective_input.setText("????????????: "+questionInfo.getOwnList().get(0).getAnswerContent());
                    }
                    subjective_input.setFocusableInTouchMode(false);
                }
                //??????????????????      tv_teacher_question
                //        tv_teacher_answer_content
                if (status.equals(Constant.Commit_Status)){
                    if (questionInfo!=null){
                        tv_teacher_question.setText("????????????: "+questionInfo.getAnswer());
                        subjective_answer_tool_layout.setVisibility(INVISIBLE);
                        subjective_del_one.setVisibility(GONE);
                        subjective_del_two.setVisibility(GONE);
                        subjective_del_three.setVisibility(GONE);

                        String textAnswer = localTextAnswersBean.getAnswerContent();
                        subjective_input.setText("????????????:"+textAnswer);
                        subjective_input.setSelection(textAnswer.length());
                    }

                }

                //?????????
                if (status.equals(Constant.Review_Status)){
                    //????????????
                    //???????????????
                    tv_teacher_question.setText("????????????: "+questionInfo.getAnswer());
                    tv_teacher_answer_content.setText("????????????: "+questionInfo.getComment());
                    subjective_answer_tool_layout.setVisibility(INVISIBLE);
                    subjective_del_one.setVisibility(GONE);
                    subjective_del_two.setVisibility(GONE);
                    subjective_del_three.setVisibility(GONE);

                    String textAnswer = localTextAnswersBean.getAnswerContent();
                    subjective_input.setText("????????????:"+textAnswer);
                    subjective_input.setSelection(textAnswer.length());
                }
                //??????
                if (status.equals(Constant.Save_Status)){
                    subjective_input.setText(localTextAnswersBean.getAnswerContent());
                }
                if (status.equals(Constant.Todo_Status)){
                    subjective_input.setText(localTextAnswersBean.getAnswerContent());
                }

            }else {
                //?????????0  ????????????????????????



                if ("0".equals(status)){
                    subjective_input.setText(localTextAnswersBean.getAnswerContent());
                }else {
                    //?????????????????????????????????
                    subjective_del_one.setVisibility(GONE);
                    subjective_del_two.setVisibility(GONE);
                    subjective_del_three.setVisibility(GONE);
                    subjective_answer_tool_layout.setVisibility(INVISIBLE);
                    subjective_input.setFocusableInTouchMode(false);
                    subjective_input.setText("????????????:"+localTextAnswersBean.getAnswerContent()+"\n"+"????????????:"+questionInfo.getAnswer());

                }
            }

        }

        if (imgFilePathList != null && imgFilePathList.size() > 0) {
            for (int i = 0; i < imgFilePathList.size(); i++) {
                switch (i) {
                    case 0:
                        subjective_answer_frame_one.setVisibility(VISIBLE);
                        subjective_answer_frame_two.setVisibility(GONE);
                        subjective_answer_frame_three.setVisibility(GONE);
                        Glide.with(getContext()).load(imgFilePathList.get(i)).into(subjective_img_one);
//                        subjective_img_one.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                        break;
                    case 1:
                        subjective_answer_frame_two.setVisibility(VISIBLE);
                        subjective_answer_frame_three.setVisibility(GONE);
                        Glide.with(getContext()).load(imgFilePathList.get(i)).into(subjective_img_two);
//                        subjective_img_two.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                        break;
                    case 2:
                        subjective_answer_frame_three.setVisibility(VISIBLE);
                        Glide.with(getContext()).load(imgFilePathList.get(i)).into(subjective_img_three);
//                        subjective_img_three.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                        break;
                    default:
                        QZXTools.popCommonToast(getContext(), "imgFileList????????????3??????", false);
                        break;
                }
            }
        }
    }

    /**
     * ????????????
     * ????????????
     */
    private void showImgsSaveAnswer() {

        if (imgFilePathList != null && imgFilePathList.size() <= 0) {
            subjective_answer_frame_one.setVisibility(GONE);
            subjective_answer_frame_two.setVisibility(GONE);
            subjective_answer_frame_three.setVisibility(GONE);
        }

        for (int i = 0; i < imgFilePathList.size(); i++) {
            switch (i) {
                case 0:
                    subjective_answer_frame_one.setVisibility(VISIBLE);
                    subjective_answer_frame_two.setVisibility(GONE);
                    subjective_answer_frame_three.setVisibility(GONE);
                    subjective_img_one.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                    break;
                case 1:
                    subjective_answer_frame_two.setVisibility(VISIBLE);
                    subjective_answer_frame_three.setVisibility(GONE);
                    subjective_img_two.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                    break;
                case 2:
                    subjective_answer_frame_three.setVisibility(VISIBLE);
                    subjective_img_three.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                    break;
                default:
                    QZXTools.popCommonToast(getContext(), "imgFileList????????????3??????", false);
                    break;
            }
        }

        //-------------------------?????????????????????????????????id
        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
        localTextAnswersBean.setHomeworkId(questionInfo.getHomeworkId());
        localTextAnswersBean.setQuestionId(questionInfo.getId());
        localTextAnswersBean.setUserId(UserUtils.getUserId());
        localTextAnswersBean.setQuestionType(questionInfo.getQuestionType());
        localTextAnswersBean.setAnswerContent(subjective_input.getText().toString().trim());
        localTextAnswersBean.setImageList(imgFilePathList);
        QZXTools.logE("subjective Save localTextAnswersBean=" + localTextAnswersBean, null);
        //???????????????????????????
        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
    }

    //??????????????????????????????
    private int cursorPos;
    //???????????????EditText????????????
    private String inputAfterText;
    //???????????????EditText?????????
    private boolean resetText;

    /**
     * ?????????????????????????????????????????????????????????EditText????????????
     */
    private int num = 80;
    public void hideAnswerTools(boolean needHideTool) {
        if (needHideTool) {
            //??????????????????
            subjective_answer_tool_layout.setVisibility(GONE);
            //EditText????????????
            subjective_input.setFocusable(false);
            //??????????????????
            subjective_del_layout_one.setVisibility(GONE);
            subjective_del_layout_two.setVisibility(GONE);
            subjective_del_layout_three.setVisibility(GONE);
        } else {
            //??????????????????????????????

            subjective_input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if (!resetText) {
                        cursorPos = subjective_input.getSelectionEnd();
                        // ?????????s.toString()???????????????s??????????????????s???
                        // ?????????inputAfterText???s??????????????????????????????????????????s????????????
                        // inputAfterText????????????????????????????????????????????????
                        inputAfterText = s.toString();
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!resetText) {
                        if (count >= 2) {//????????????????????????????????????2
                            if ((cursorPos + count) <= s.toString().trim().length()) {
                                CharSequence input = s.subSequence(cursorPos, cursorPos + count);
                                if (ViewUtils.containsEmoji(input.toString())) {
                                    resetText = true;
                                    Toast.makeText(mContext, "???????????????Emoji????????????", Toast.LENGTH_SHORT).show();
                                    //?????????????????????????????????????????????????????????????????????
                                    subjective_input.setText(inputAfterText);
                                    QZXTools.logE("inputAfterText:"+inputAfterText,null);
                                    CharSequence text = subjective_input.getText();
                                    if (text.length() > 0) {
                                        if (text instanceof Spannable) {
                                            Spannable spanText = (Spannable) text;
                                            Selection.setSelection(spanText, text.length());
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        resetText = false;
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    //-------------------------?????????????????????????????????id
                    LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                    localTextAnswersBean.setHomeworkId(questionInfo.getHomeworkId());
                    localTextAnswersBean.setQuestionId(questionInfo.getId());
                    localTextAnswersBean.setQuestionType(questionInfo.getQuestionType());
                    localTextAnswersBean.setAnswerContent(subjective_input.getText().toString());
                    localTextAnswersBean.setUserId(UserUtils.getUserId());
                    localTextAnswersBean.setAnswer(questionInfo.getAnswer());
                    localTextAnswersBean.setImageList(imgFilePathList);
//                                QZXTools.logE("Save localTextAnswersBean=" + localTextAnswersBean, null);
                    //???????????????????????????
                    MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                    //-------------------------?????????????????????????????????id

                    QZXTools.logE("?????????????????????:"+new Gson().toJson(localTextAnswersBean),null);
                }
            });
        }
    }

    @Override
    protected void onAttachedToWindow() {
        QZXTools.logE("onAttachedToWindow", null);
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        QZXTools.logE("onDetachedFromWindow", null);
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    /**
     * ???????????????????????????????????????????????????EventBus????????????????????????????????????????????????????????????????????????
     */
    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        QZXTools.logE("onFocusChanged", null);
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    //------------------------------------------------------------------------------------------------
    @Override
    public void grantPermission() {
        QZXTools.logD("???????????????????????????");
        if (isClickCamera) {
            openCamera();
        } else {
           // getContext().startActivity(new Intent(getContext(), WhiteBoardActivity.class));

            Intent intent = new Intent(getContext(), WhiteBoardActivity.class);
            intent.putExtra("extra_info", questionInfo.getId());
            getContext().startActivity(intent);
        }
    }

    @Override
    public void denyPermission() {
        QZXTools.logD("???????????????");
        Toast.makeText(mContext, "??????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
    }
}
