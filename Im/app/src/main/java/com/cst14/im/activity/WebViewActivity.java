package com.cst14.im.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.cst14.im.R;
import com.cst14.im.utils.ImApplication;


public class WebViewActivity extends Activity {
    WebView Game_webView;
    ProgressBar Game_progressBar;
    private String gameURL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        initControl();
        Intent intent = getIntent();getIntent();
        String game=intent.getStringExtra("game");
        switch (game){
            case "fruit_ninja":
                gameURL= ImApplication.GAME_SERVER_HOST+"/fruits";
                break;
            case "flappy_bird":
                gameURL=ImApplication.GAME_SERVER_HOST+"/flappy";
                break;
        }
        initView();
    }

    private void initView() {
        WebSettings webSettings = Game_webView .getSettings();
        //支持获取手势焦点，输入用户名、密码或其他
        Game_webView.requestFocusFromTouch();
        webSettings.setDomStorageEnabled(true);
        //设置webview属性能够执行javascript脚本
        webSettings.setJavaScriptEnabled(true);  //支持js
        //  设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true);  //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        webSettings.setSupportZoom(true);  //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); //支持内容重新布局
        webSettings. supportMultipleWindows();  //多窗口
        webSettings.setAllowFileAccess(true);  //设置可以访问文件
        webSettings.setNeedInitialFocus(true); //当webview调用requestFocus时为webview设置节点
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true);  //支持自动加载图片
        webSettings. setDefaultTextEncodingName("utf-8");//设置编码格式
        //加载需要显示的网页
        Game_webView.loadUrl(gameURL);
        Game_webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Game_progressBar.setVisibility(View.VISIBLE);
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });
        //显示进度
        Game_webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                Log.e("newProgress", newProgress+"");
                Game_progressBar.setProgress(newProgress);
                if(newProgress >= 100){
                    Game_progressBar.setVisibility(View.GONE);
                }
                super.onProgressChanged(view, newProgress);
            }

        });
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && Game_webView.canGoBack()) {
            Game_webView.goBack();//退回到上一个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void initControl() {
        Game_webView=(WebView)findViewById(R.id.Game_webView);
        Game_progressBar=(ProgressBar)findViewById(R.id.Game_progressBar);
    }
}
