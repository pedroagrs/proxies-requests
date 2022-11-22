package io.github.pedroagrs.requests.protocol.socks.proxy;

import lombok.Getter;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public class SocksCustomProxySelector extends ProxySelector {

    private static final ProxySelector DEFAULT_PROXY_SELECTOR = ProxySelector.getDefault();

    private final List<Proxy> proxy;

    @Getter
    private final String id;

    @Getter
    private int retries = 0;

    public SocksCustomProxySelector(Proxy proxy, String id) {
        this.proxy = Collections.singletonList(proxy);
        this.id = id;
    }

    @Override
    public List<Proxy> select(URI uri) {
        return proxy;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        DEFAULT_PROXY_SELECTOR.connectFailed(uri, sa, ioe);
    }

    public void addRetry() {
        retries++;
    }
}
