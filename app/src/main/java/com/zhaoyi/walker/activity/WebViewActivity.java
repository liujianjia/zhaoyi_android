package com.zhaoyi.walker.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zhaoyi.walker.R;

import java.io.File;

import static android.view.KeyEvent.KEYCODE_BACK;

/**
 * Created by jianjia Liu on 2017/4/5.
 */

public class WebViewActivity extends AppCompatActivity {
    private WebView mWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        Bundle bundle = getIntent().getExtras();
        String url = bundle.getString("url");

        mWebView = (WebView)findViewById(R.id.wv_web_view);
        setUpWebView();
        mWebView.loadUrl(url);
    }

    private void setUpWebView() {
        /*
         * 如果页面中链接,如果希望点击链接继续在当前browser中响应,
         * 而不是新开Android的系统browser中响应该链接,必须覆盖
         * WebView的WebViewClient对象
         */
        mWebView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                view.loadUrl(url);
                return true;
            }
        });
        /*
         * 设置缓存模式
         *  LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
         *  LOAD_DEFAULT: 根据cache-control决定是否从网络上取数据。
         *  LOAD_CACHE_NORMAL: API level 17中已经废弃, 从API level 11开始作用同LOAD_DEFAULT模式
         *  LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
         *  LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据
         */
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

    }
    /*
     * 如果不做任何处理 ,浏览网页,点击系统“Back”键,整个 Browser 会
     * 调用 finish()而结束自身,如果希望浏览的网页回退而不是推出浏览器,
     * 需要在当前Activity中处理并gh处理掉该 Back 事件
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    /*
     * 删除WebView缓存，WebView会在/data/data/包名目录下生成 database 与 cache 两个文件夹
     */
    private int clearCacheFolder(File dir, long numDays) {
        int deletedFiles = 0;
        if (dir!= null && dir.isDirectory()){
            try {
                for (File child:dir.listFiles()){
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, numDays);
                    }
                    if (child.lastModified() < numDays) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return deletedFiles;
    }
    /*
     *  删除缓存的方法
     *  clearCache(true);
     *  deleteDatabase(“WebView.db”);和deleteDatabase(“WebViewCache.db”);
     *  webView.clearHistory();
     *  webView.clearFormData();
     *  getCacheDir().delete();
     *
     */
    /*
     * 删除缓存
     */
    /*private void clearCache() {
        File file = CacheManager.getCacheFileBaseDir();
        if (file != null && file.exists() && file.isDirectory()) {
            for (File item : file.listFiles()) {
                item.delete();
            }
            file.delete();
        }
        context.deleteDatabase("WebView.db");
        context.deleteDatabase("WebViewCache.db");
    }*/
}
