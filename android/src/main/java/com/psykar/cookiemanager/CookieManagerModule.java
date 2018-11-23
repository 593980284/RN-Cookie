package com.psykar.cookiemanager;

import android.content.Context;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.facebook.react.modules.network.ForwardingCookieHandler;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Promise;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpCookie;
import java.net.URISyntaxException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CookieManagerModule extends ReactContextBaseJavaModule {

    private ForwardingCookieHandler cookieHandler;
    private WeakReference<ReactApplicationContext> contextWeakReference;

    public CookieManagerModule(ReactApplicationContext context) {
        super(context);
        this.cookieHandler = new ForwardingCookieHandler(context);
        contextWeakReference = new WeakReference<>(context);
    }
    @Override
    public String getName() {
        return "RNCookieManagerAndroid";
    }

    @ReactMethod
    public void set(ReadableMap cookie, final Promise promise) throws Exception {
//        throw new Exception("Cannot call on android, try setFromResponse");
        Context context = contextWeakReference.get();
        if(context == null){
            promise.reject("context == null");
            return;
        }
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT <Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(context);
        }
        cookieManager.setAcceptCookie(true);
        String path = cookie.getString("domain");
        cookieManager.setCookie(path,cookie.getString("name") + "=" + cookie.getString("value"));

        cookieManager.setCookie(path,"domain" + "=" + cookie.getString("domain"));
        cookieManager.setCookie(path,"origin" + "=" + cookie.getString("origin"));
        cookieManager.setCookie(path,"path" + "=" + cookie.getString("path"));
        cookieManager.setCookie(path,"version" + "=" + cookie.getString("version"));
        cookieManager.setCookie(path,"expiration" + "=" + cookie.getString("expiration"));
        if (Build.VERSION.SDK_INT <Build.VERSION_CODES.LOLLIPOP) {
             CookieSyncManager.getInstance().sync();
        }
        promise.resolve(null);
    }




    @ReactMethod
    public void setFromResponse(String url, String value, final Promise promise) throws URISyntaxException, IOException {
        Map headers = new HashMap<String, List<String>>();
        // Pretend this is a header
        headers.put("Set-cookie", Collections.singletonList(value));
        URI uri = new URI(url);
        this.cookieHandler.put(uri, headers);
        promise.resolve(null);
    }

    @ReactMethod
    public void getFromResponse(String url, Promise promise) throws URISyntaxException, IOException {
        promise.resolve(url);
    }

    @ReactMethod
    public void getAll(Promise promise) throws Exception {
        throw new Exception("Cannot get all cookies on android, try getCookieHeader(url)");
    }

    @ReactMethod
    public void get(String url, Promise promise) throws URISyntaxException, IOException {
        URI uri = new URI(url);

        Map<String, List<String>> cookieMap = this.cookieHandler.get(uri, new HashMap());
        // If only the variables were public
        List<String> cookieList = cookieMap.get("Cookie");
        WritableMap map = Arguments.createMap();
        if (cookieList != null) {
            String[] cookies = cookieList.get(0).split(";");
            for (int i = 0; i < cookies.length; i++) {
                String[] cookie = cookies[i].split("=", 2);
                if(cookie.length > 1) {
                  map.putString(cookie[0].trim(), cookie[1]);
                }
            }
        }
        promise.resolve(map);
    }

    @ReactMethod
    public void clearAll(final Promise promise) {
        this.cookieHandler.clearCookies(new Callback() {
            @Override
            public void invoke(Object... args) {
                promise.resolve(null);
            }
        });
    }
}
