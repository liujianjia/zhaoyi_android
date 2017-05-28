package com.zhaoyi.walker.Fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaoyi.walker.R;
import com.zhaoyi.walker.activity.WebViewActivity;
import com.zhaoyi.walker.model.ResultFromServer;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by jianjia Liu on 2017/3/29.
 */

public class HosSimpleInfoFragment extends Fragment {
    private TextView tvHosPhone;
    private TextView tvHosUrl;
    private TextView tvHosSimpleInfo;
    private TextView tvHosAddr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.hosp_simple_intro_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        tvHosPhone = (TextView)this.getActivity().findViewById(R.id.tv_hos_phone);
        tvHosUrl = (TextView)this.getActivity().findViewById(R.id.tv_hos_url);
        tvHosAddr = (TextView)this.getActivity().findViewById(R.id.tv_hos_addr);
        tvHosSimpleInfo = (TextView)this.getActivity().findViewById(R.id.tv_hos_simple_info);
        setUpUi();

       this.getActivity().findViewById(R.id.ll_hos_phone).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tvHosPhone.getText().toString()));
               PackageManager pm = HosSimpleInfoFragment.this.getActivity().getPackageManager();
               ComponentName cn = intent.resolveActivity(pm);
               if(cn != null) {
                   startActivity(intent);
               } else {
                   Log.d(TAG, "No such activity to resolve this Action: " +
                   Intent.ACTION_DIAL);
               }

           }
       });
       this.getActivity().findViewById(R.id.ll_url).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               String url = tvHosUrl.getText().toString();
               Intent intent = new Intent(HosSimpleInfoFragment.this.getActivity(),
                       WebViewActivity.class);
               intent.putExtra("url", url);
               startActivity(intent);
           }
       });
    }

    private void setUpUi() {
        try {
            tvHosPhone.setText((String) (ResultFromServer.getHosInfo().get(0).get("phone")));
            tvHosAddr.setText((String)ResultFromServer.getHosInfo().get(0).get("hosAddress"));
            String url = "<a href>" + (ResultFromServer.getHosInfo().get(0).get("url")) + "</a>";
            tvHosUrl.setText(Html.fromHtml(url));
            tvHosSimpleInfo.setText((String) (ResultFromServer.getHosInfo().get(0).get("introduction")));
        }
        catch (Exception e) {
            Toast.makeText(HosSimpleInfoFragment.this.getActivity(), "从服务器返回数据错误", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
