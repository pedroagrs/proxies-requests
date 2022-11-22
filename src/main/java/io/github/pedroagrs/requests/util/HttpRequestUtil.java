package io.github.pedroagrs.requests.util;

import io.github.pedroagrs.requests.protocol.http.HttpRequestController;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;

@UtilityClass
public class HttpRequestUtil {

    public HttpURLConnection createUrlConnection(Proxy proxy, URI uri) {
        try {
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection(proxy);
            connection.setConnectTimeout(HttpRequestController.DEFAULT_TIMEOUT);
            connection.setReadTimeout(HttpRequestController.DEFAULT_TIMEOUT);
            connection.setUseCaches(false);

            return connection;
        } catch (Exception exception) {
            System.out.println("Error: " + uri);
        }

        return null;
    }

    public BufferedReader getBufferedReader(HttpURLConnection connection) throws IOException {
        return new BufferedReader(new InputStreamReader((connection.getInputStream())));
    }
}
