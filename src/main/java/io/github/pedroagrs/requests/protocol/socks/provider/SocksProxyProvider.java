package io.github.pedroagrs.requests.protocol.socks.provider;

import io.github.pedroagrs.requests.protocol.socks.SocksRequestController;
import org.apache.http.HttpHost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.*;

public final class SocksProxyProvider {

    private static final Registry<ConnectionSocketFactory> FACTORY_REGISTRY =
            RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", ProxySelectorPlainConnectionSocketFactory.INSTANCE)
                    .register("https", new ProxySelectorSSLConnectionSocketFactory(SSLContexts.createSystemDefault()))
                    .build();

    public static CloseableHttpClient createClient() {
        final PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager(FACTORY_REGISTRY);

        return HttpClients.custom()
                .disableContentCompression()
                .disableRedirectHandling()
                .disableAutomaticRetries()
                .disableAuthCaching()
                .disableCookieManagement()
                .setConnectionManager(connectionManager)
                .build();
    }

    private enum ProxySelectorPlainConnectionSocketFactory implements ConnectionSocketFactory {
        INSTANCE;

        @Override
        public Socket createSocket(HttpContext context) {
            return SocksProxyProvider.createSocket(context);
        }

        @Override
        public Socket connectSocket(
                int connectTimeout,
                Socket sock,
                HttpHost host,
                InetSocketAddress remoteAddress,
                InetSocketAddress localAddress,
                HttpContext context)
                throws IOException {
            return PlainConnectionSocketFactory.INSTANCE.connectSocket(
                    SocksRequestController.DEFAULT_TIMEOUT, sock, host, remoteAddress, localAddress, context);
        }
    }

    private static final class ProxySelectorSSLConnectionSocketFactory extends SSLConnectionSocketFactory {
        ProxySelectorSSLConnectionSocketFactory(SSLContext sslContext) {
            super(sslContext);
        }

        @Override
        public Socket createSocket(HttpContext context) {
            return SocksProxyProvider.createSocket(context);
        }
    }

    private static Socket createSocket(HttpContext context) {
        HttpHost httpTargetHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
        URI uri = URI.create(httpTargetHost.toURI());
        Proxy proxy = ProxySelector.getDefault().select(uri).iterator().next();

        final Socket socket = new Socket(proxy);

        try {
            socket.setSoTimeout(SocksRequestController.DEFAULT_TIMEOUT);
        } catch (SocketException exception) {
            exception.printStackTrace();
        }

        return socket;
    }
}