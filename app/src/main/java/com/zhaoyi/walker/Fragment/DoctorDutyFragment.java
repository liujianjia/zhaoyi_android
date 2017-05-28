package com.zhaoyi.walker.Fragment;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.activity.ConfirmAppointmentActivity;
import com.zhaoyi.walker.activity.LoginActivity;
import com.zhaoyi.walker.adapter.FrameGridAdapter;
import com.zhaoyi.walker.model.ResultFromServer;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by jianjia Liu on 2017/3/30.
 */

public class DoctorDutyFragment extends Fragment {
    private GridView gvDoctorDuty;
    private Map<String, Object> date;
    private List<Integer> totalTicket;
    private List<Integer> remainTicket;
    private List<Integer> cancelTicket;
    private List<String> myItems;
    private TreeSet list;
    private final int TYPE_REQUEST_APPOINTMENT = 109;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.doctor_duty_gridview_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        gvDoctorDuty = (GridView)this.getActivity().findViewById(R.id.gv_doctor_duty);

        totalTicket = new ArrayList<>();
        remainTicket = new ArrayList<>();
        cancelTicket = new ArrayList<>();
        myItems= new ArrayList<>();

        parseAppointmentData();

        Resources res = getResources();
        ColorStateList csl = res.getColorStateList(R.color.theme_default_color);
        FrameGridAdapter adapter = new FrameGridAdapter(this.getActivity(), myItems, csl);

        gvDoctorDuty.setAdapter(adapter);
        gvDoctorDuty.setSelector(new ColorDrawable(Color.TRANSPARENT));  // 取消Item点击的默认效果，效果为无

        gvDoctorDuty.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                getExactlyAppointmentTime(i);
            }
        });
    }

    private void getExactlyAppointmentTime(int i) {
        if(i > 8 && i < 24 &&  i != 16) {  //是否为可预约位置
            String isLogin = ZyApplication.getInstance()
                    .getUserJson()
                    .getString(MyContants.JSON_KEY_ISLOGIN);

            if(TextUtils.isEmpty(isLogin) || !isLogin.equals("1")) {
                startActivity(new Intent(DoctorDutyFragment.this.getActivity(), LoginActivity.class));
                return;
            }
            int subFactor = i < 16 ? 9 : 17;  // 获取表头偏移
            Iterator it = list.iterator();
            int factor = i - subFactor;  // 获取无表头偏移起始点
            while(it.hasNext() && factor != 0) {  // 同步factor和it，使it到达factor的表头位置
                it.next();
                factor--;
            }

            String appointmentDate = it.next().toString();  // 获取日期
            final int idx = i - subFactor + (i < 16 ? 0 : 7);
            final String afternoon = subFactor < 16 ? "上午" : "下午";

            String userId = ZyApplication.getInstance()
                    .getUserJson()
                    .getString(MyContants.JSON_KEY_ZYID);

            if(TextUtils.isEmpty(userId)) {
                Log.e("Error: ", "userId can't be empty.");
                return;
            }

            List<Param> params = new ArrayList<Param>();
            params.add(new Param("userId", userId));
            params.add(new Param("date", appointmentDate));
            params.add(new Param("time", afternoon));
            //params.add(new Param("number", Long.toString(totalTicket.get(number) - remainTicket.get(number) + 1)));

            OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.GETEXACTLYAPPOINTMENTTIME,
                    new OkHttpManager.HttpCallBack() {
                        @Override
                        public void onResponse(JSONObject json) {
                            try {
                                int code = json.getInteger("code");

                                if (code == 1000) {
                                    try {
                                        Map<String, Object> map = json.getObject("result", Map.class);

                                        String detailDate = (String)map.get("date");
                                        String detailTime = (String)map.get("time");
                                        JSONArray pArray = (JSONArray)map.get("PatientInfo");

                                        int size = pArray.size();
                                        ArrayList<String> patientId = new ArrayList<>();
                                        ArrayList<String> patientName = new ArrayList<>();
                                        ArrayList<String> isDefault = new ArrayList<>();
                                        for(int i = 0; i < size; i++) {
                                            Map<String, Object> info = (Map<String, Object>) pArray.get(i);
                                            patientId.add((String) info.get("id"));
                                            patientName.add((String) info.get("realName"));
                                            isDefault.add((String) info.get("isDefault"));
                                        }
                                        Intent intent = new Intent(DoctorDutyFragment.this.getActivity(), ConfirmAppointmentActivity.class);
                                        intent.putExtra("patientId", patientId);
                                        intent.putExtra("patientName", patientName);
                                        intent.putExtra("isDefault", isDefault);
                                        intent.putExtra("date", detailDate);
                                        intent.putExtra("time", detailTime);
                                        intent.putExtra("afternoon", afternoon);
                                        int n = totalTicket.get(idx) - remainTicket.get(idx) + cancelTicket.get(idx) + 1;
                                        intent.putExtra("number", Long.toString(n));
                                        DoctorDutyFragment.this.getActivity().startActivityForResult(intent, TYPE_REQUEST_APPOINTMENT);
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                else if(code == 2001){
                                    Toast.makeText(DoctorDutyFragment.this.getActivity(),
                                            "获取就诊人失败", Toast.LENGTH_SHORT)
                                            .show();
                                }
                                else if(code == 2002) {
                                    Toast.makeText(DoctorDutyFragment.this.getActivity(),
                                            "您已被暂停使用挂号功能", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(String errorMsg) {

                        }
                    });
        }
    }
    public void parseAppointmentData() {
        JSONObject json = (JSONObject)ResultFromServer.getDoctor().get(0);
        date = json.getObject("dateList", Map.class);
        totalTicket.clear();
        remainTicket.clear();
        cancelTicket.clear();
        myItems.clear();

        list = new TreeSet(date.keySet());  // 得到排序的日期
        String[] field = {"textItem", "上午", "下午", "预约" };

        Iterator it = list.iterator();
        Object sortedDate;
        while (it.hasNext()) {
            sortedDate = it.next();
            totalTicket.add(((JSONObject)date.get(sortedDate)).getObject("morcount", Integer.class));
            remainTicket.add(((JSONObject)date.get(sortedDate)).getObject("上午", Integer.class));
            cancelTicket.add(((JSONObject)date.get(sortedDate)).getObject("mccount", Integer.class));
        }
        it = list.iterator();
        while (it.hasNext()) {
            sortedDate = it.next();
            totalTicket.add(((JSONObject)date.get(sortedDate)).getObject("aftcount", Integer.class));
            remainTicket.add(((JSONObject)date.get(sortedDate)).getObject("下午", Integer.class));
            cancelTicket.add(((JSONObject)date.get(sortedDate)).getObject("account", Integer.class));
        }

        it = list.iterator();
        myItems.add("");

        while(it.hasNext()) {
            myItems.add((String)it.next());
        }
        myItems.add(field[1]);

        boolean flag = true;
        for(int i = 0; i < remainTicket.size(); i++) {
            if(i == 7 && flag) {
                myItems.add(field[2]);
                flag = false;
                --i;
                continue;
            }
            if(remainTicket.get(i) > 0) {
                myItems.add(field[3]);  // 可预约
            }
            else {
                myItems.add("");
            }
        }
    }
}
