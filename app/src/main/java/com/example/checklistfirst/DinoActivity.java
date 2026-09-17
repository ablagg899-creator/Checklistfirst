package com.example.checklistfirst;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
public class DinoActivity extends Activity {
 private WebView web;
 @Override public void onCreate(Bundle state){super.onCreate(state);requestWindowFeature(Window.FEATURE_NO_TITLE);getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);web=new WebView(this);WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setDatabaseEnabled(true);s.setAllowFileAccess(true);s.setAllowContentAccess(true);s.setBuiltInZoomControls(false);s.setDisplayZoomControls(false);web.setWebViewClient(new WebViewClient());web.setVerticalScrollBarEnabled(false);web.setHorizontalScrollBarEnabled(false);web.loadUrl("file:///android_asset/index.html");setContentView(web);}
 @Override protected void onDestroy(){if(web!=null)web.destroy();super.onDestroy();}
}
