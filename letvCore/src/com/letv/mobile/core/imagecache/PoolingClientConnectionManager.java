package com.letv.mobile.core.imagecache;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

/**
 * HTTP CLIENT CONNECTION POOL
 *
 * @author Fengwx
 */
public class PoolingClientConnectionManager implements BasicHttpClient {

    public static HttpClient get() {
        final HttpParams httpParams = new BasicHttpParams();
        ConnManagerParams.setTimeout(httpParams, TIMEOUT);//获取连接的最大等待时间
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams,
                new ConnPerRouteBean(CONN_PER_ROUTE_MAX));
        ConnManagerParams.setMaxTotalConnections(httpParams, MAX_CONNECTIONS);
        HttpClientParams.setRedirecting(httpParams, true);
        HttpProtocolParams.setUseExpectContinue(httpParams, true);
        HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);
        HttpConnectionParams.setSoTimeout(httpParams, SOCKET_TIMEOUT);//读取超时时间
        HttpConnectionParams
                .setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);//连接超时时间
        HttpConnectionParams.setTcpNoDelay(httpParams, true);
        HttpConnectionParams
                .setSocketBufferSize(httpParams, SOCKET_BUFFER_SIZE);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(
                new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        try {
            KeyStore trustStore = KeyStore
                    .getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(
                    SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            schemeRegistry.register(new Scheme("https", sf, 443));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ClientConnectionManager manager = new ThreadSafeClientConnManager(
                httpParams, schemeRegistry);
        DefaultHttpClient mHttpClient = new DefaultHttpClient(manager,
                httpParams);

        return mHttpClient;
    }

    private static class MySSLSocketFactory extends SSLSocketFactory {
        final SSLContext sslContext = SSLContext.getInstance("TLS");

        public MySSLSocketFactory(KeyStore truststore)
                throws NoSuchAlgorithmException, KeyManagementException,
                KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            sslContext.init(null, new TrustManager[] { tm }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port,
                boolean autoClose) throws IOException,
                UnknownHostException {
            return sslContext.getSocketFactory()
                    .createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }
}
