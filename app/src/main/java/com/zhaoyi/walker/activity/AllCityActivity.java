package com.zhaoyi.walker.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.model.CurrentHosInfo;
import com.zhaoyi.walker.model.ResultFromServer;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllCityActivity extends FragmentActivity {

    private String[] mStrs = {"aaa", "bbb", "ccc", "airsaid"};
    private SearchView mSearchView;
    private GridView allCityGridView;
    private GridView hotCityGridView;
    private ImageView ivGoack;
    private static final String[] cityStr = {"北京", "上海", "广州", "深圳", "成都",
            "武汉", "南京", "天津", "重庆", "西安", "长沙", "昆明"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);
        allCityGridView = (GridView) findViewById(R.id.gv_allcity);
        hotCityGridView = (GridView) findViewById(R.id.gv_hotcity);
        ivGoack = (ImageView) findViewById(R.id.iv_choose_city_back);

        List<Map<String, String>> items = new ArrayList<Map<String, String>>();
        for (int i = 0; i < cityStr.length; i++) {
            Map<String, String> item = new HashMap<String, String>();
            item.put("textItem", cityStr[i]);
            items.add(item);
        }

        //实例化一个适配器
        SimpleAdapter adapter = new SimpleAdapter(this,
                items,
                R.layout.gridview_item,
                new String[]{"textItem"},
                new int[]{R.id.text_item});

        //为GridView设置适配器
        hotCityGridView.setAdapter(adapter);
        allCityGridView.setAdapter(adapter);
        hotCityGridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                submitChooseCity(cityStr[+ position]);
            }
        });
        allCityGridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                submitChooseCity(cityStr[+ position]);
            }
        });
        ivGoack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private void submitChooseCity(final String city) {
        List<Param> params = new ArrayList<Param>();

        params.add(new Param("address", city));

        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.RECOMMENDDOCTOR,
                new OkHttpManager.HttpCallBack() {
            @Override
            public void onResponse(JSONObject json) {
                int code = json.getInteger("code");
                if (code == 1000) {

                    Map<String, Object> map = json.getObject("result", Map.class);

                    JSONArray hi = (JSONArray) map.get("DocInfo");
                    JSONArray homeInfo = (JSONArray)map.get("HomeInfo");
                    ResultFromServer.initAllDoctor();
                    ResultFromServer.initHomeInfo();
                    if(hi != null) {
                        int docSize = hi.size();

                        for (int i = 0; i < docSize; i++) {
                            Map<String, Object> n = hi.getObject(i, Map.class);
                            ResultFromServer.getAllDoctor().add(n);
                        }
                        if(docSize == 0) {
                            Toast.makeText(AllCityActivity.this, "暂时没有推荐医生", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(homeInfo != null) {
                        for(int i = 0; i < homeInfo.size(); i++) {
                            ResultFromServer.addHomeInfo(homeInfo.getObject(i, Map.class));
                        }
                    }
                    //getData(city);
                } else if(code == 2001) {
                    ResultFromServer.getAllDoctor().clear();
                    Toast.makeText(AllCityActivity.this, "暂时没有推荐医生", Toast.LENGTH_SHORT).show();
                }
                CurrentHosInfo.setCurCity(city);
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(String errorMsg) {

            }
        });
    }

    /* 获取推荐医生内容 */
    private void getData(String city) {
        List<Param> params = new ArrayList<Param>();
        if (TextUtils.isEmpty(city)) {
            city = "北京";
        }
        params.add(new Param("address", city));

        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.RECOMMENDDOCTOR,
                new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject json) {
                        int code = json.getInteger("code");
                        if (code == 1000) {
                            Map<String, Object> map = json.getObject("result", Map.class);

                            JSONArray hi = (JSONArray) map.get("DocInfo");
                            ResultFromServer.initAllDoctor();
                            for (int i = 0; i < hi.size(); i++) {
                                Map<String, Object> n = hi.getObject(i, Map.class);
                                ResultFromServer.getAllDoctor().add(n);
                            }
                        } else if (code == 2001) {
                            ResultFromServer.getAllDoctor().clear();
                            Toast.makeText(AllCityActivity.this, "暂时没有推荐医生", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }
                }
        );
    }

}
