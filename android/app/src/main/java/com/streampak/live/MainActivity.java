package com.streampak.live;

import android.app.PictureInPictureParams;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    private WebView webView;
    private boolean isVideoPlaying = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Screen ON rakhna
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        new android.os.Handler().postDelayed(() -> {
            webView = getBridge().getWebView();
            if (webView == null) return;

            WebSettings settings = webView.getSettings();

            // ✅ FIX 1: JavaScript ON
            settings.setJavaScriptEnabled(true);

            // ✅ FIX 2: Media autoplay WITHOUT user gesture
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                settings.setMediaPlaybackRequiresUserGesture(false);
            }

            // ✅ FIX 3: Mixed Content
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }

            // ✅ FIX 4: Hardware Acceleration
            webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);

            // ✅ FIX 5: DOM Storage, Cache
            settings.setDomStorageEnabled(true);
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
            settings.setDatabaseEnabled(true);

            // ✅ FIX 6: File access
            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                settings.setAllowFileAccessFromFileURLs(true);
                settings.setAllowUniversalAccessFromFileURLs(true);
            }

            // ✅ FIX 7: WebChromeClient — media + PiP permissions
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onPermissionRequest(PermissionRequest request) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        request.grant(request.getResources());
                    }
                }

                // ✅ PiP FIX: Jab video play ho — Android ko batao
                // Yeh JavaScript se "isVideoPlaying" flag set karta hai
                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    String msg = consoleMessage.message();
                    if (msg != null) {
                        if (msg.contains("PIP_START")) {
                            isVideoPlaying = true;
                        } else if (msg.contains("PIP_STOP")) {
                            isVideoPlaying = false;
                        }
                    }
                    return super.onConsoleMessage(consoleMessage);
                }
            });

            // ✅ FIX 8: WebViewClient
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return false;
                }
            });

            android.util.Log.d("StreamPak", "✅ WebView + PiP fixes applied");

        }, 500);
    }

    // ✅ PiP FIX: Home button dabane par automatically PiP mein jaao
    @Override
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (isVideoPlaying && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPipMode();
        }
    }

    // ✅ PiP mode enter karne ka function
    private void enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                PictureInPictureParams.Builder pipBuilder = new PictureInPictureParams.Builder();
                // 16:9 ratio — TV channels ke liye perfect
                pipBuilder.setAspectRatio(new Rational(16, 9));
                enterPictureInPictureMode(pipBuilder.build());
            } catch (Exception e) {
                android.util.Log.e("StreamPak", "PiP enter failed: " + e.getMessage());
            }
        }
    }

    // ✅ PiP mode change hone par handle karo
    @Override
    public void onPictureInPictureModeChanged(boolean isInPiPMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPiPMode, newConfig);
        if (webView != null) {
            if (isInPiPMode) {
                // PiP mein gaye — UI hide karo, sirf video dikhao
                webView.evaluateJavascript(
                    "document.getElementById('bottomNav') && (document.getElementById('bottomNav').style.display='none');" +
                    "document.getElementById('header') && (document.getElementById('header').style.display='none');" +
                    "document.getElementById('scrollArea') && (document.getElementById('scrollArea').style.display='none');",
                    null
                );
            } else {
                // PiP se wapas aaye — UI dikhao
                webView.evaluateJavascript(
                    "document.getElementById('bottomNav') && (document.getElementById('bottomNav').style.display='');" +
                    "document.getElementById('header') && (document.getElementById('header').style.display='');" +
                    "document.getElementById('scrollArea') && (document.getElementById('scrollArea').style.display='');",
                    null
                );
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
