package com.letv.statistic.http;

import com.letv.mobile.http.builder.LetvHttpBaseUrlBuilder;
import com.letv.mobile.http.parameter.LetvBaseParameter;
import com.letv.statistic.Constants;

import android.net.Uri;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * @author lilong
 *         上报请求使用的url拼接器
 */
public class HttpUrlBuilder extends LetvHttpBaseUrlBuilder {

    public final LetvBaseParameter params;

    public HttpUrlBuilder(String domain, String baseUrl, LetvBaseParameter params, int type) {
        super(domain, baseUrl, type);
        this.params = params;
    }

    public HttpUrlBuilder(String domain, String baseUrl, LetvBaseParameter params) {
        super(domain, baseUrl, Type.GET);
        this.params = params;
    }

    public HttpUrlBuilder(String baseUrl, LetvBaseParameter params) {
        super(Constants.REPORT_DOMAIN, baseUrl, Type.GET);
        this.params = params;
    }

    /**
     * 根据hashmap拼接请求参数
     **/
    @Override
    public StringBuilder buildParameter() {
        StringBuilder sb = new StringBuilder();
        if (this.params == null || this.params.isEmpty()) {
            return sb;
        }
        Iterator<Entry<String, Object>> iter = this.params.entrySet().iterator();
        while (iter.hasNext()) {
            try {
                Entry<String, Object> entry = iter.next();
                Object key = entry.getKey();
                Object val = entry.getValue();
                if (val == null) {
                    val = "";
                }
                sb.append(key.toString());
                sb.append("=");
                if (val != null) {
                    sb.append(URLEncoder.encode(val.toString(), "UTF-8"));
                }
                if (iter.hasNext()) {
                    sb.append("&");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb;
    }

    protected Uri generateUri(String domain, String path) {
        // NOTE(qingxia): If we use Uri.Builder to create a Uri like this :
        // builder.setbuilder.scheme("http");
        // builder.authority(this.getAuthority());
        // builder.build().toString() will return a encode authority like this:
        // http://xxx%20port but we expected http://xxx:port.
        // So we used Uri.parse("http://xxx:port").buildUpon to generate the
        // builder.
        Uri.Builder builder = Uri.parse(domain).buildUpon();
        // NOTE(qingxia): We should use path not appendPath, or Uri.Builder will
        // encode the
        // path. ex: use appendPath, http://xxx/suggest/ will to encode to
        // http://xxx/suggest%2F
        builder.path(path);
        return builder.build();
    }

    @Override
    public String buildUrl() {
        return this
                .buildUrl(this.domain, this.baseUrl, this.buildParameter().toString(), this.type);
    }

    private String buildUrl(String domain, String path, String parameter, int httpMethod) {
        if (TextUtils.isEmpty(domain)) {
            return null;
        }
        Uri.Builder builder = Uri.parse(domain).buildUpon();
        if (TextUtils.isEmpty(path)) {
            return builder.build().toString();
        }
        if (path.endsWith("?")) {
            path = path.substring(0, path.length() - 1);
        }
        builder.path(path);
        if (httpMethod == LetvHttpBaseUrlBuilder.Type.GET && !TextUtils.isEmpty(parameter)) {
            return builder.build().toString() + "?" + parameter;
        }
        String str = builder.build().toString();
        return builder.build().toString();
    }
}
