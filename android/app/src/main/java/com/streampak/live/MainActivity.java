package com.streampak.live;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.webkit.PermissionRequest;
import android.webkit.ConsoleMessage;
import android.view.WindowManager;
import android.os.Build;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Screen ON rakhna — video dekhte waqt screen off na ho
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // WebView fix — thodi der baad apply karo (Bridge init ke baad)
        new android.os.Handler().postDelayed(() -> {
            WebView webView = getBridge().getWebView();
            if (webView == null) return;

            WebSettings settings = webView.getSettings();

            // ✅ FIX 1: JavaScript ON
            settings.setJavaScriptEnabled(true);

            // ✅ FIX 2: Media autoplay WITHOUT user gesture (BLACK SCREEN FIX!)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                settings.setMediaPlaybackRequiresUserGesture(false);
            }

            // ✅ FIX 3: Mixed Content (HTTP + HTTPS dono allow)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }

            // ✅ FIX 4: Hardware Acceleration (GPU video decode)
            webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);

            // ✅ FIX 5: DOM Storage, Cache
            settings.setDomStorageEnabled(true);
            settings.setAppCacheEnabled(true);
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
            settings.setDatabaseEnabled(true);

            // ✅ FIX 6: File access
            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                settings.setAllowFileAccessFromFileURLs(true);
                settings.setAllowUniversalAccessFromFileURLs(true);
            }

            // ✅ FIX 7: WebChromeClient — media permissions grant karo
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onPermissionRequest(PermissionRequest request) {
                    // Camera, audio, video sab grant karo
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        request.grant(request.getResources());
                    }
                }

                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    // Debug logs (optional)
                    return super.onConsoleMessage(consoleMessage);
                }
            });

            // ✅ FIX 8: WebViewClient — redirects handle karo
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // Sab URLs WebView mein hi open karo
                    return false;
                }
            });

            android.util.Log.d("StreamPak", "✅ WebView fixes applied successfully");

        }, 500);
    }

    @Override
    public void onResume() {
        super.onResume();
        // App wapas foreground — screen flag refresh
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
