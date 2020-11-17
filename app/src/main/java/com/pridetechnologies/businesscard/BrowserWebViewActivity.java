package com.pridetechnologies.businesscard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textview.MaterialTextView;

import im.delight.android.webview.AdvancedWebView;

//
public class BrowserWebViewActivity extends AppCompatActivity implements AdvancedWebView.Listener{

    private AdvancedWebView mWebView;
    private String url, webTitle;
    private MaterialTextView titleView, bodyView;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private ProgressBar progressBar;
    private ImageButton reloadBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        ImageButton closeBtn = (ImageButton) findViewById(R.id.imageButton300);
        closeBtn.setOnClickListener(view -> finish());
        ImageButton homeBtn = (ImageButton) findViewById(R.id.imageButton301);
        homeBtn.setOnClickListener(view -> {
            titleView.setText("Loading...");
            mWebView.stopLoading();
            mWebView.loadUrl("https://google.com/");
        });
        reloadBtn = (ImageButton) findViewById(R.id.imageButton302);
        reloadBtn.setOnClickListener(view -> {
            reloadBtn.setVisibility(View.GONE);
            titleView.setText("Loading...");
            mWebView.stopLoading();
            mWebView.reload();
        });

        titleView = (MaterialTextView)findViewById(R.id.textView130);
        titleView.setText("Loading...");
        bodyView = (MaterialTextView)findViewById(R.id.textView129);
        progressBar = (ProgressBar) findViewById(R.id.mProgressBar);


        url = getIntent().getExtras().get("urlLink").toString();

        mWebView = (AdvancedWebView) findViewById(R.id.webView);
        mWebView.setListener(this, this);
        mWebView.setGeolocationEnabled(true);
        mWebView.setMixedContentAllowed(true);
        mWebView.setCookiesEnabled(true);
        mWebView.setThirdPartyCookiesEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                //Toast.makeText(WebViewActivity.this, "Finished loading", Toast.LENGTH_SHORT).show();
            }

        });
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                webTitle=title;
                //titleView.setText(title);
               // Toast.makeText(WebViewActivity.this, title, Toast.LENGTH_SHORT).show();
            }

        });
        mWebView.addHttpHeader("X-Requested-With", url);
        mWebView.loadUrl(url);

        mySwipeRefreshLayout = (SwipeRefreshLayout)this.findViewById(R.id.swipeContainer);
        mySwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.stopLoading();
                mWebView.reload();
                mySwipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
        // ...
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        mWebView.onPause();
        // ...
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mWebView.onDestroy();
        // ...
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mWebView.onActivityResult(requestCode, resultCode, intent);
        // ...
    }

    @Override
    public void onBackPressed() {
        if (!mWebView.onBackPressed()) { return; }

        super.onBackPressed();
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        titleView.setText("Loading...");
        bodyView.setText(url);
        progressBar.setVisibility(View.VISIBLE);
        //Toast.makeText(WebViewActivity.this, url, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPageFinished(String url) {
        titleView.setText(webTitle);
        bodyView.setText(url);
        progressBar.setVisibility(View.GONE);
        reloadBtn.setVisibility(View.VISIBLE);
        //Toast.makeText(WebViewActivity.this, url, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
        titleView.setText(errorCode);
        bodyView.setText(failingUrl);
        reloadBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {
        // some file is available for download
        // either handle the download yourself or use the code below

        if (AdvancedWebView.handleDownload(this, url, suggestedFilename)) {
            Toast.makeText(this, "Download Successful", Toast.LENGTH_SHORT).show();
            // download successfully handled
        }
        else {
            Toast.makeText(this, "Download Failed", Toast.LENGTH_SHORT).show();
            // download couldn't be handled because user has disabled download manager app on the device
            // TODO show some notice to the user
        }
    }

    @Override
    public void onExternalPageRequest(String url) { }

}
