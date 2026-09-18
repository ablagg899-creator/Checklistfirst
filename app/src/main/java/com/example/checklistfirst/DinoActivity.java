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
 @Override public void onCreate(Bundle state){
  super.onCreate(state);
  requestWindowFeature(Window.FEATURE_NO_TITLE);
  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
  web=new WebView(this);
  web.setBackgroundColor(0x00000000);
  web.setFitsSystemWindows(false);
  WebSettings s=web.getSettings();
  s.setJavaScriptEnabled(true);
  s.setDomStorageEnabled(true);
  s.setDatabaseEnabled(true);
  s.setAllowFileAccess(true);
  s.setAllowContentAccess(true);
  s.setBuiltInZoomControls(false);
  s.setDisplayZoomControls(false);
  s.setSupportZoom(false);
  // Let the HTML meta viewport control the CSS viewport on phones.
  // Wide-view scaling was causing the phone to render the desktop canvas
  // (and crop the right side) instead of activating the mobile CSS.
  s.setUseWideViewPort(false);
  s.setLoadWithOverviewMode(false);
  s.setTextZoom(100);
  web.setWebViewClient(new WebViewClient());
  web.setVerticalScrollBarEnabled(false);
  web.setHorizontalScrollBarEnabled(false);
  web.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
  web.loadUrl("file:///android_asset/index.html");
  setContentView(web);
 }
 @Override protected void onDestroy(){if(web!=null)web.destroy();super.onDestroy();}
}
