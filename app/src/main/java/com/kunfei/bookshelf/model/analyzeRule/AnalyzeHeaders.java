package com.kunfei.bookshelf.model.analyzeRule;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.CookieBean;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import static android.text.TextUtils.isEmpty;
import static com.kunfei.bookshelf.constant.AppConstant.DEFAULT_USER_AGENT;
import static com.kunfei.bookshelf.constant.AppConstant.MAP_STRING;
import static com.kunfei.bookshelf.utils.NetworkUtils.headerPattern;

/**
 * Created by GKF on 2018/3/2.
 * 解析Headers
 */

public class AnalyzeHeaders {
    private static SharedPreferences preferences = MApplication.getConfigPreferences();

    public static Map<String, String> getMap(BookSourceBean bookSourceBean) {
        Map<String, String> headerMap = new HashMap<>();
        if (bookSourceBean != null && !isEmpty(bookSourceBean.getHttpUserAgent())) {
            headerMap.put("User-Agent", bookSourceBean.getHttpUserAgent());
        } else {
            headerMap.put("User-Agent", getDefaultUserAgent());
        }
        if (bookSourceBean != null) {
            CookieBean cookie = DbHelper.getDaoSession().getCookieBeanDao().load(bookSourceBean.getBookSourceUrl());
            if (cookie != null) {
                headerMap.put("Cookie", cookie.getCookie());
            }
        }
        if (bookSourceBean != null && !isEmpty(bookSourceBean.getHttpHeader())) {
            try {
                Map<String, String> map = new Gson().fromJson(bookSourceBean.getHttpHeader(), MAP_STRING);
                headerMap.putAll(map);
            } catch (Exception ignored) {
            }
        }
        return headerMap;
    }

    private static String getDefaultUserAgent() {
        return preferences.getString(MApplication.getInstance().getString(R.string.pk_user_agent), DEFAULT_USER_AGENT);
    }
}
