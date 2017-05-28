package com.zhaoyi.walker.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONArray;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.model.ResultFromServer;
import com.zhaoyi.walker.view.ExpandTabView;
import com.zhaoyi.walker.view.ViewLeft;
import com.zhaoyi.walker.view.ViewMiddle;
import com.zhaoyi.walker.view.ViewRight;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jianjia Liu on 2017/3/29.
 */

public class HosDepartmentFragment extends Fragment {
    RelativeLayout rLayout;
    private ExpandTabView expandTabView;
    private ArrayList<View> mViewArray = new ArrayList<View>();
    private ViewLeft viewLeft;
    private ViewMiddle viewMiddle;
    private ViewRight viewRight;
    //private GridView gvDepartmentInfo;
    private static List<Map<String, String>> items;
    private List<List<String>> mList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initVaule();
        initView();
        return viewMiddle; //inflater.inflate(R.layout.hosp_department_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //initView();
        //initVaule();
        //initListener();
        //gvDepartmentInfo = (GridView)this.getActivity().findViewById(R.id.gv_hos_department);

        /*List<Map<String, String>> map = (List<Map<String, String>>)(ResultFromServer.getHosInfo().get(0).get("offinfo"));

        items = new ArrayList<Map<String, String>>();
        for (int i = 0; i < map.size(); i++) {
            Map<String, String> item = new HashMap<String, String>();
            item.put("departmentItem", map.get(i).get("offName"));
            items.add(item);
        }
        SimpleAdapter adapter = new SimpleAdapter(this.getActivity(),
                items,
                R.layout.gridview_item,
                new String[]{"departmentItem"},
                new int[]{R.id.text_item});*/


        //gvDepartmentInfo.setAdapter(adapter);
        /*gvDepartmentInfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(HosDepartmentFragment.this.getActivity(), HosDepartmentFragment.getGridViewItems(i),
                        //Toast.LENGTH_SHORT).show();
                List<Param> params = new ArrayList<Param>();
                params.add(new Param("hosname", (String)ResultFromServer.getHosInfo().get(0).get("hosName")));
                params.add(new Param("offname", HosDepartmentFragment.getGridViewItems(i)));
                //department = HosDepartmentFragment.getGridViewItems(i);
                CurrentHosInfo.setCurDepartment(HosDepartmentFragment.getGridViewItems(i));
                OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.CHOOSEDEPARTMENT, new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject json) {
                        try {
                            int code = json.getInteger("code");
                            Map<String, Object> map = json.getObject("result", Map.class);

                            JSONArray hi = (JSONArray) map.get("DocInfo");
                            ResultFromServer.initAllDoctor();
                            for(int i = 0; i < hi.size(); i++) {
                                Map<String, Object> n = hi.getObject(i, Map.class);
                                ResultFromServer.getAllDoctor().add(n);
                            }

                            if (code == 1000) {
                                try {
                                    startActivity(new Intent(HosDepartmentFragment.this.getActivity(), ShowDoctorsFromFixedHosActivity.class));
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                Toast.makeText(HosDepartmentFragment.this.getActivity(),
                                        "服务器繁忙请重试...", Toast.LENGTH_SHORT)
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
        });*/
    }
    //public static String getDepartment() {
        //return department;
    //}
    public void setValue(View view) {
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        rl.leftMargin = 10;
        rl.rightMargin = 10;
        rLayout.addView(view, rl);
        rLayout.setTag(0);
        rLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.popup_main_background));
        //ToggleButton tButton = (ToggleButton) inflater.inflate(R.layout.toggle_button, this, false);
        //addView(tButton);
    }
    private void initView() {

        //expandTabView = (ExpandTabView) findViewById(R.id.expandtab_view);
        expandTabView = new ExpandTabView(this.getActivity());
        viewLeft = new ViewLeft(this.getActivity());
        viewMiddle = new ViewMiddle(this.getActivity(), mList);
        viewRight = new ViewRight(this.getActivity());

    }

    private void initVaule() {

//		mViewArray.add(viewLeft);
        //mViewArray.add(viewMiddle);
//		mViewArray.add(viewRight);
        //ArrayList<String> mTextArray = new ArrayList<String>();
//		mTextArray.add("距离");
        //mTextArray.add("区域");
//		mTextArray.add("距离");
        //expandTabView.setValue(mTextArray, mViewArray);
//		expandTabView.setTitle(viewLeft.getShowText(), 0);
//		expandTabView.setTitle(viewMiddle.getShowText(), 1);
//		expandTabView.setTitle(viewRight.getShowText(), 2);
        mList = new ArrayList<>();
        for(Object i : ResultFromServer.getDepartment()) {
            for(Object key : ((Map<String, Object>)i).keySet()) {
                List<String> name = new ArrayList<>();
                name.add((String)key);
                JSONArray array = ((JSONArray)(((Map<String, Object>)i).get(key)));
                int size = array.size();
                for(int j = 0; j < size; j++) {
                    name.add(((Map<String, String>)array.get(j)).get("offName"));
                }
                mList.add(name);
            }
        }
    }

    private void initListener() {

        viewLeft.setOnSelectListener(new ViewLeft.OnSelectListener() {

            @Override
            public void getValue(String distance, String showText) {
                onRefresh(viewLeft, showText);
            }
        });

        viewMiddle.setOnSelectListener(new ViewMiddle.OnSelectListener() {

            @Override
            public void getValue(String showText) {

                onRefresh(viewMiddle,showText);

            }
        });

        viewRight.setOnSelectListener(new ViewRight.OnSelectListener() {

            @Override
            public void getValue(String distance, String showText) {
                onRefresh(viewRight, showText);
            }
        });

    }

    private void onRefresh(View view, String showText) {

        expandTabView.onPressBack();
        int position = getPositon(view);
        //if (position >= 0 && !expandTabView.getTitle(position).equals(showText)) {
        //expandTabView.setTitle(showText, position);
        //}
        //Toast.makeText(HosDepartmentFragment.this.getActivity(), showText, Toast.LENGTH_SHORT).show();

    }

    private int getPositon(View tView) {
        for (int i = 0; i < mViewArray.size(); i++) {
            if (mViewArray.get(i) == tView) {
                return i;
            }
        }
        return -1;
    }
    public static String getGridViewItems(int pos) {
        return items.get(pos).get("departmentItem");
    }


}
