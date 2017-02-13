package com.sample.foo.simplerssreader;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

public class ArticleViewActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_view);
        Bundle b = getIntent().getExtras();
        String link = b.getString("link");
        System.out.println("got the link" + link);
        WebView webview=(WebView)findViewById(R.id.webView);
        webview.loadUrl(link);
    }
}