package com.zhaoyi.walker.Fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhaoyi.walker.R;
import com.zhaoyi.walker.model.ResultFromServer;

/**
 * Created by jianjia Liu on 2017/3/29.
 */

public class HosServiceFragment extends Fragment {
    private String pubInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.hosp_service_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        pubInfo = (String) ResultFromServer.getHosInfo().get(0).get("notice");

        this.getActivity().findViewById(R.id.ll_hosp_public_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HosServiceFragment.this.getActivity());
                builder.setTitle("医院公告")
                        .setMessage(pubInfo)
                        .setPositiveButton("确定", null)
                        .show();
            }
        });
    }
}
