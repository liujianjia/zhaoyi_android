package com.zhaoyi.walker.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.activity.AboutActivity;
import com.zhaoyi.walker.activity.MyAppointmentActivity;
import com.zhaoyi.walker.activity.MyCollectionActivity;
import com.zhaoyi.walker.activity.MyCommentActivity;
import com.zhaoyi.walker.activity.MyWalletActivity;
import com.zhaoyi.walker.activity.PatientRecordActivity;
import com.zhaoyi.walker.activity.PatientsManager;
import com.zhaoyi.walker.activity.SettingActivity;
import com.zhaoyi.walker.activity.UserPersonalPageActivity;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.view.ImageViewPlus;

import static com.zhaoyi.walker.utils.OkHttpManager.context;

public class MeFragment extends Fragment implements View.OnClickListener{
    ImageViewPlus ivAvatar;

    private final int TYPE_REQUEST_LOGOUT = 108;
    private final int TYPE_REQUEST_UPDATE_INFO_STATUS = 116;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.me_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        //((TextView)getView().findViewById(R.id.tvTop)).setText("我");
        this.getActivity().findViewById(R.id.re_personal_info).setOnClickListener(this);
        this.getActivity().findViewById(R.id.re_my_order).setOnClickListener(this);
        this.getActivity().findViewById(R.id.re_setting).setOnClickListener(this);
        ivAvatar = (ImageViewPlus) this.getActivity().findViewById(R.id.iv_avatar);
        this.getActivity().findViewById(R.id.re_patient_record).setOnClickListener(this);
        this.getActivity().findViewById(R.id.re_my_comment).setOnClickListener(this);
        this.getActivity().findViewById(R.id.re_patient_manage).setOnClickListener(this);
        this.getActivity().findViewById(R.id.re_my_collection).setOnClickListener(this);
        this.getActivity().findViewById(R.id.re_my_wallet).setOnClickListener(this);

        String cacheAvatar = ZyApplication.getInstance().getUserJson().getString(MyContants.JSON_KEY_AVATAR);
        String avatarUrl = "";
        if(!TextUtils.isEmpty(cacheAvatar)) {
            avatarUrl = MyContants.BASE_URL + cacheAvatar;
        }
        /*Glide.with(MeFragment.this)
                .load(avatarUrl)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.zy_default_useravatar)
                .centerCrop()
                .into(ivAvatar);*/
        Glide.with(MeFragment.this)
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
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.re_personal_info:
                getActivity().startActivityForResult(new Intent(MeFragment.this.getActivity(), UserPersonalPageActivity.class),
                        TYPE_REQUEST_UPDATE_INFO_STATUS);
                break;
            case R.id.re_about:
                startActivity(new Intent(MeFragment.this.getActivity(), AboutActivity.class));
                break;
            case R.id.re_my_order:
                startActivity(new Intent(MeFragment.this.getActivity(), MyAppointmentActivity.class));
                break;
            case R.id.re_setting:
                getActivity().startActivityForResult(new Intent(MeFragment.this.getActivity(), SettingActivity.class),
                        TYPE_REQUEST_LOGOUT);
                break;
            case R.id.re_patient_record:
                startActivity(new Intent(MeFragment.this.getActivity(), PatientRecordActivity.class));
                break;
            case R.id.re_my_comment:
                startActivity(new Intent(MeFragment.this.getActivity(), MyCommentActivity.class));
                break;
            case R.id.re_patient_manage:
                startActivity(new Intent(MeFragment.this.getActivity(), PatientsManager.class));
                break;
            case R.id.re_my_collection:
                startActivity(new Intent(MeFragment.this.getActivity(), MyCollectionActivity.class));
                break;
            case R.id.re_my_wallet:
                startActivity(new Intent(MeFragment.this.getActivity(), MyWalletActivity.class));
                break;
            default:
                break;
        }
    }

}
