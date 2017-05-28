package com.zhaoyi.walker.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.zhaoyi.walker.Fragment.InputDialogFragment;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.zhaoyi.walker.utils.OkHttpManager.context;

/**
 * Created by jianjia Liu on 2017/4/5.
 */

public class UserPersonalPageActivity extends FragmentActivity implements
        InputDialogFragment.InputListener, View.OnClickListener {
    private TextView tvName;
    private TextView tvRealName;
    private TextView tvIdCard;
    private TextView tvPhone;
    private ImageView ivAvatar;
    private ImageView ivBack;
    private TextView tvBloodType;
    private TextView tvBirth;
    private TextView tvRegion;
    private TextView tvSex;

    private String imageName;
    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果
    private static final int TYPE_USER_PHONE_UPDATE = 102;// 验证手机

    private boolean hasChange = false;
    private JSONObject userJson;
    private File path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_detail_info);

        userJson = ZyApplication.getInstance().getUserJson();

        tvName = (TextView) findViewById(R.id.tv_name);
        tvRealName = (TextView) findViewById(R.id.tv_real_name);
        tvIdCard = (TextView) findViewById(R.id.tv_id_card);
        tvPhone = (TextView) findViewById(R.id.tv_phone);
        ivAvatar = (ImageView) findViewById(R.id.iv_avatar);
        tvBirth = (TextView) findViewById(R.id.tv_birth);
        tvSex = (TextView) findViewById(R.id.tv_sex);
        tvBloodType = (TextView) findViewById(R.id.tv_blood_type);
        tvRegion = (TextView) findViewById(R.id.tv_region);
        ivBack = (ImageView) findViewById(R.id.iv_back);

        /*tvRegion.setText(UserDetailInfo.getUserAge());
        tvBirth.setText(UserDetailInfo.getUserAge());
        tvPhone.setText(UserDetailInfo.getUserPhone());
        tvSex.setText(UserDetailInfo.getUserSex());*/
        tvName.setText(userJson.getString(MyContants.JSON_KEY_NAME));
        tvRealName.setText(userJson.getString(MyContants.JSON_KEY_REAL_NAME));
        tvIdCard.setText(userJson.getString(MyContants.JSON_KEY_IDCARD));
        tvPhone.setText(userJson.getString(MyContants.JSON_KEY_PHONE));
        tvBirth.setText(userJson.getString(MyContants.JSON_KEY_BIRTH));
        tvRegion.setText(userJson.getString(MyContants.JSON_KEY_ADDRESS));
        tvSex.setText(userJson.getString(MyContants.JSON_KEY_SEX));
        tvBloodType.setText(userJson.getString(MyContants.JSON_KEY_BLOODTYPE));
        String avatarUrl = MyContants.BASE_URL + userJson.getString(MyContants.JSON_KEY_AVATAR);

        Glide.with(UserPersonalPageActivity.this)
                .load(avatarUrl)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.zy_default_useravatar)
                .into(new BitmapImageViewTarget(ivAvatar) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        ivAvatar.setImageDrawable(circularBitmapDrawable);
                    }
                });

        findViewById(R.id.re_avatar).setOnClickListener(this);
        findViewById(R.id.re_phone).setOnClickListener(this);
        findViewById(R.id.re_birth).setOnClickListener(this);
        findViewById(R.id.re_region).setOnClickListener(this);
        findViewById(R.id.re_blood_type).setOnClickListener(this);
        findViewById(R.id.re_sex).setOnClickListener(this);

        ivBack.setOnClickListener(this);

        path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if(!path.exists()) {
            path.mkdirs();
        }
    }

    @Override
    public void onInputComplete(Object obj, int index) {
        if(obj instanceof String) {
            String value = (String) obj;
            String key = "";
            if(value != null)
                hasChange = true;
            switch (index) {
                case 0:
                    //tvRegion.setText(ans);
                    key=MyContants.JSON_KEY_ADDRESS;
                    break;
                case 1:
                    //tvBloodType.setText(ans);
                    key=MyContants.JSON_KEY_BLOODTYPE;
                    break;
                case 2:
                    //tvSex.setText(ans);
                    key=MyContants.JSON_KEY_SEX;
                    break;
                default:
                    break;
            }
            updateInServer(key, value);
        }
    }

    @Override
    public void onClick(View v) {

        InputDialogFragment inputDialogFragment;
        switch(v.getId()) {
            case R.id.re_avatar:
                showPhotoDialog();
                break;
            case R.id.re_birth:
                DatePickerDialog datePickerDialog = new DatePickerDialog(UserPersonalPageActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                //tvBirth.setText(year + "-" + month + "-" + dayOfMonth);
                                int monthPlus = month + 1;
                                String value = year + "-" + monthPlus + "-" + dayOfMonth;
                                updateInServer(MyContants.JSON_KEY_BIRTH, value);
                            }
                        }, 1990, 1, 1);
                datePickerDialog.show();
                break;
            case R.id.re_region:
                inputDialogFragment = InputDialogFragment.newInstance(0);
                inputDialogFragment.show(getFragmentManager(), "Dialog");
                break;
            case R.id.re_phone:
                Intent intent = new Intent(UserPersonalPageActivity.this, VerifyOldPhoneActivity.class);
                intent.putExtra("phone", tvPhone.getText().toString().trim());
                startActivityForResult(intent, TYPE_USER_PHONE_UPDATE);
                break;
            case R.id.re_sex:
                inputDialogFragment = InputDialogFragment.newInstance(2);
                inputDialogFragment.show(getFragmentManager(), "Dialog");
                break;
            case R.id.re_blood_type:
                inputDialogFragment = InputDialogFragment.newInstance(1);
                inputDialogFragment.show(getFragmentManager(), "Dialog");
                break;
            case R.id.iv_back:
                checkHasChange();
                break;
            default:
                break;
        }
    }
    private void showPhotoDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserPersonalPageActivity.this);
        String[] items = { "拍照", "相册" };
        alertDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        imageName = getNowTime() + ".png";
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        // 指定调用相机拍照后照片的储存路径
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(new File(path, imageName)));
                        startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
                        break;
                    case 1:
                        imageName = getNowTime() + ".png";
                        Intent intent2 = new Intent(Intent.ACTION_PICK, null);
                        intent2.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                        startActivityForResult(intent2, PHOTO_REQUEST_GALLERY);
                        break;
                    default:
                        break;
                }
            }
        });
        alertDialog.show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTO_REQUEST_TAKEPHOTO:
                    startPhotoZoom(
                            Uri.fromFile(new File(path, imageName)),
                            480);
                    break;

                case PHOTO_REQUEST_GALLERY:
                    if (data != null)
                        startPhotoZoom(data.getData(), 480);
                    break;

                case PHOTO_REQUEST_CUT:
                    updateInServer(MyContants.JSON_KEY_AVATAR, imageName);
                    break;

                case TYPE_USER_PHONE_UPDATE:
                    Bundle args = getIntent().getExtras();
                    String phone = args.getString("newPhone");
                    updateInServer(MyContants.JSON_KEY_PHONE, phone);
                    break;
                default:
                    break;
            }
            super.onActivityResult(requestCode, resultCode, data);

        }
    }
    private void startPhotoZoom(Uri uri1, int size) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri1, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", size);
        intent.putExtra("outputY", size);
        intent.putExtra("return-data", false);

        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(new File(path, imageName)));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }
    private String getNowTime() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmssSS");
        return dateFormat.format(date);
    }

    private void updateInServer(final String key, final String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在更新...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        List<Param> params = new ArrayList<Param>();
        params.add(new Param("key", key));
        params.add(new Param("value", value));
        params.add(new Param("userId", userJson.getString(MyContants.JSON_KEY_ZYID))); ///*userJson.getString(MyContants.JSON_KEY_ZYID))*/);
        List<File> files = new ArrayList<File>();
        if (key == MyContants.JSON_KEY_AVATAR) {
            File file = new File(path, value);
            if (file.exists()) {
                files.add(file);
            }
        }
        OkHttpManager.getInstance().post(params, files, MyContants.BASE_URL + MyContants.UPDATEPROFILE,
                new OkHttpManager.HttpCallBack() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                progressDialog.dismiss();
                int code = jsonObject.getIntValue("code");
                if (code == 1000) {
                    //update ui
                    hasChange=true;
                    String newValue = value;

                    if (key.equals(MyContants.JSON_KEY_AVATAR)) {

                        Bitmap bitmap = BitmapFactory.decodeFile(path + "/" + value);
                        ivAvatar.setImageBitmap(bitmap);
                        Map<String, String> url = jsonObject.getObject("result", Map.class);
                        String avatarUrl = url.get("image");
                        newValue = avatarUrl;
                        //userJson.put(key, MyContants.BASE_URL + avatarUrl);
                        //ZyApplication.getInstance().setUserJson(userJson);
                        //return;
                    } else if (key.equals(MyContants.JSON_KEY_SEX)) {
                        tvSex.setText(newValue);
                    } else if(key.equals(MyContants.JSON_KEY_PHONE)) {
                        tvPhone.setText(newValue);
                    } else if (key.equals(MyContants.JSON_KEY_ADDRESS)) {
                        tvRegion.setText(newValue);
                    } else if(key.equals(MyContants.JSON_KEY_BLOODTYPE)) {
                        tvBloodType.setText(newValue);
                    } else if(key.equals(MyContants.JSON_KEY_BIRTH)) {
                        tvBirth.setText(newValue);
                    }

                    userJson.put(key, newValue);
                    //ZyApplication.getInstance().setUserJson(userJson);
                } else {
                    Toast.makeText(getApplicationContext(), "更新失败,code:" + code, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(String errorMsg) {
                progressDialog.dismiss();
            }
        });


    }

    @Override
    public  void onBackPressed(){
        checkHasChange();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            checkHasChange();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void checkHasChange(){

        if( hasChange) {
            setResult(RESULT_OK);
            ZyApplication.getInstance().setUserJson(userJson);
        }
        finish();
    }
}
