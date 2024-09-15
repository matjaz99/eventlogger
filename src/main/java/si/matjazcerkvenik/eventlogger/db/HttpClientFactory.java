/*
   Copyright 2021 Matjaž Cerkvenik

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package si.matjazcerkvenik.eventlogger.db;

import okhttp3.*;
import si.matjazcerkvenik.eventlogger.util.LogFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

public class HttpClientFactory {

    /**
     * Create new instance of http client. ClientConfig provides schema (http or https),
     * timeouts, username and password for authentication.
     * @param config
     * @return http client
     */
    public static OkHttpClient instantiateHttpClient(ClientConfig config) {

        if (config.getSchema().equalsIgnoreCase("http")) {
            LogFactory.getLogger().info("HttpClientFactory: instantiating HTTP client");

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(config.getConnectionTimeout(), TimeUnit.SECONDS)
                    .readTimeout(config.getReadTimeout(), TimeUnit.SECONDS)
                    .build();
            return client;
        }

        // continue if https

        LogFactory.getLogger().debug("HttpClientFactory: instantiating HTTPS client");

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        try {

            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            if (config.getUsername() != null && config.getPassword() != null) {
                builder.authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        if (response.request().header("Authorization") != null)
                            return null;  //if you've tried to authorize and failed, give up

                        String credential = Credentials.basic(config.getUsername(), config.getPassword());
                        return response.request().newBuilder().header("Authorization", credential).build();
                    }
                });
            }

            builder.connectTimeout(config.getConnectionTimeout(), TimeUnit.SECONDS);
            builder.readTimeout(config.getReadTimeout(), TimeUnit.SECONDS);

            return builder.build();

        } catch (Exception e) {
            return null;
        }

    }

}
