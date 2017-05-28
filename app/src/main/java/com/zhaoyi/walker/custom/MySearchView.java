package com.zhaoyi.walker.custom;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.activity.ShowHosListActivity;
import com.zhaoyi.walker.model.ResultFromServer;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jianjia Liu on 2017/5/13.
 */
public class MySearchView extends LinearLayout {

    private Context context;

    /*UI组件*/
    private TextView tv_clear;
    private AutoClearEditText et_search;
    private TextView tv_tip;
    private ImageView iv_search;

    /*列表及其适配器*/
    private ListViewForScrollView listView;
    private SimpleCursorAdapter adapter;

    /*数据库变量*/
    private MySQLiteOpenHelper myHelper ;
    private SQLiteDatabase db;

    private OkHttpClient okHttpClient;

    /*三个构造函数*/
    //在构造函数里直接对搜索框进行初始化 - init()
    public MySearchView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public MySearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public MySearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }


    /*初始化搜索框*/
    private void init(){

        //初始化UI组件
        initView();

        //实例化数据库SQLiteOpenHelper子类对象
        myHelper = new MySQLiteOpenHelper(context);

        // 第一次进入时查询所有的历史记录
        queryData("history", "");

        //"清空搜索历史"按钮
        tv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //清空数据库
                deleteData("history");
                queryData("history", "");
            }
        });

        //搜索框的文本变化实时监听
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            //输入后调用该方法
            @Override
            public void afterTextChanged(Editable s) {
                //每次输入后都查询数据库并显示
                //根据输入的值去模糊查询数据库中有没有数据
                String tempName = et_search.getText().toString();
                if (s.toString().trim().length() == 0) {
                    //若搜索框为空,则模糊搜索空字符,即显示所有的搜索历史
                    tv_tip.setText("搜索历史");
                    tv_tip.setVisibility(TextView.VISIBLE);
                    tv_clear.setVisibility(TextView.VISIBLE);
                    queryData("history", tempName);
                } else {
                    tv_tip.setVisibility(TextView.GONE);
                    tv_clear.setVisibility(TextView.GONE);
                    queryData("hospital", tempName);
                }
            }
        });


        // 搜索框的键盘搜索键
        // 点击回调
        et_search.setOnKeyListener(new View.OnKeyListener() {  // 输入完后按键盘上的搜索键
            // 修改回车键功能
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    // 隐藏键盘，这里getCurrentFocus()需要传入Activity对象，如果实际不需要的话就不用隐藏键盘了，免得传入Activity对象，这里就先不实现了
//                    ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
//                            getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    // 按完搜索键后将当前查询的关键字保存起来,如果该关键字已经存在就不执行保存
                    String text = et_search.getText().toString().trim();
                    if(TextUtils.isEmpty(text)) {
                        return true;
                    }
                    boolean hasData = hasData("history", text);
                    if (!hasData) {
                        insertData("history", text);
                        queryData("history", "");
                    }
                    //根据输入的内容模糊查询商品，并跳转到另一个界面，这个需要根据需求实现
                    //Toast.makeText(context, "点击搜索", Toast.LENGTH_SHORT).show();
                    searchHosp(text);
                }
                return false;
            }
        });

        //列表监听
        //即当用户点击搜索历史里的字段后,会直接将结果当作搜索字段进行搜索
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //获取到用户点击列表里的文字,并自动填充到搜索框内
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                String name = textView.getText().toString();
                et_search.setText(name);
                //Toast.makeText(context, name, Toast.LENGTH_SHORT).show();
                searchHosp(name);
            }
        });

        //点击搜索按钮后的事件
        iv_search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = et_search.getText().toString().trim();
                if(TextUtils.isEmpty(text)) {
                    return;
                }
                boolean hasData = hasData("history", text);
                if (!hasData) {
                    insertData("history", text);
                    //搜索后显示数据库里所有搜索历史是为了测试
                    //queryData("history", "");
                }
                //根据输入的内容模糊查询商品，并跳转到另一个界面，这个根据需求实现
                //Toast.makeText(context, "clicked!", Toast.LENGTH_SHORT).show();
                searchHosp(text);
            }
        });
    }
    /**
     * 封装的函数
     */

    /*初始化组件*/
    private void initView(){
        LayoutInflater.from(context).inflate(R.layout.my_search_view,this);
        et_search = (AutoClearEditText) findViewById(R.id.et_search);
        tv_clear = (TextView) findViewById(R.id.tv_clear);
        tv_tip = (TextView) findViewById(R.id.tv_tip);
        listView = (ListViewForScrollView) findViewById(R.id.listView);
        iv_search = (ImageView) findViewById(R.id.iv_search);
    }

    /*插入数据*/
    private void insertData(String table, String tempName) {
        db = myHelper.getWritableDatabase();
        db.execSQL("insert into " + table + "(name) values('" + tempName + "')");
        db.close();
    }

    /**
     *
     * @param table 表名
     * @param tempName 查询文本
     *
     * 模糊查询数据 并显示在ListView列表上
     */
    private void queryData(String table, String tempName) {

        //模糊搜索
        Cursor cursor = myHelper.getReadableDatabase().rawQuery(
                "select id as _id,name from " + table + " where name like '%" + tempName + "%' order by id desc ", null);
        if(table.equals("hospital") && cursor.getCount() == 0) {  // 数据库结果为空，在线查询
            postAsynHttp(tempName);
        } else {
            // 创建adapter适配器对象,装入模糊搜索的结果
            adapter = new SimpleCursorAdapter(context, android.R.layout.simple_list_item_1, cursor, new String[]{"name"},
                    new int[]{android.R.id.text1}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    /*检查数据库中是否已经有该条记录*/
    private boolean hasData(String table, String tempName) {
        //从Record这个表里找到name=tempName的id
        Cursor cursor = myHelper.getReadableDatabase().rawQuery(
                "select id as _id,name from " + table + " where name =?", new String[]{tempName});
        //判断是否有下一个
        return cursor.moveToNext();
    }

    /**
     *
     * @param text 搜索文本
     */
    private void postAsynHttp(final String text) {
        //okHttpClient=new OkHttpClient();
        if(okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                    .readTimeout(10000L, TimeUnit.MILLISECONDS)
                    //其他配置
                    .build();
        }

        RequestBody formBody = new FormBody.Builder()
                .add("hosname", text)
                .build();
        Request request = new Request.Builder()
                .url(MyContants.BASE_URL + MyContants.SEARCHHOSP)
                .post(formBody)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                try {
                    JSONObject json = JSONObject.parseObject(result);
                    parseResult(json, text);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }

    /**
     *  @param hosp 医院名称
     *
     */
    private void searchHosp(final String hosp) {
        if(TextUtils.isEmpty(hosp)) {
            Log.d("current city is ----->>", "null");
            return;
        }
        List<Param> params = new ArrayList<Param>();

        params.add(new Param("hosname", hosp));

        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.SEARCHHOSP,
                new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject json) {
                        try {
                            int code = json.getInteger("code");
                            if (code == 1000) {
                                Map<String, Object> map = json.getObject("result", Map.class);

                                JSONArray hi = (JSONArray) map.get("HosInfo");
                                JSONArray region = (JSONArray) map.get("districts");
                                JSONArray types = (JSONArray) map.get("types");
                                ResultFromServer.initFixedCityHos();
                                ResultFromServer.getFixedCityHos().clear();
                                ArrayList<String> regionList = new ArrayList<>();
                                regionList.add("全部");
                                int size = region.size();
                                for(int i = 0; i < size; i++) {
                                    String re = region.getString(i);
                                    regionList.add(re);
                                }

                                size = hi.size();
                                for (int i = 0; i < size; i++) {
                                    Map<String, Object> n = hi.getObject(i, Map.class);
                                    ResultFromServer.addFixedCityHos(n);
                                }
                                Intent intent = new Intent(context, ShowHosListActivity.class);
                                intent.putExtra("regionList", regionList);
                                intent.putExtra("typeList", types);
                                context.startActivity(intent);
                            }
                            else if (code == 2001) {
                                Toast.makeText(context, "暂时没有结果", Toast.LENGTH_SHORT).show();
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

    private void parseResult(JSONObject json, String text) {
        int code = json.getInteger("code");
        if (code == 1000) {
            Map<String, Object> map = json.getObject("result", Map.class);

            JSONArray hi = (JSONArray) map.get("HosInfo");

            int size = hi.size();

            for (int i = 0; i < size; i++) {
                Map<String, Object> n = hi.getObject(i, Map.class);
                Map<String, Object> m = new HashMap<>();
                String name = (String) n.get("hosName");
                m.put("name", name);
                if(!hasData("hospital", name)) {
                    insertData("hospital", name);
                }
            }

            queryData("hospital", text);
        }
    }

    /*清空数据*/
    private void deleteData(String table) {
        db = myHelper.getWritableDatabase();
        db.execSQL("delete from " + table);
        db.close();
    }
}
