package io.github.pedroagrs.requests;

import io.github.pedroagrs.requests.decoder.RequestProxyURLDecoderBuilder;
import io.github.pedroagrs.requests.protocol.decoder.ProtocolDecoderType;
import io.github.pedroagrs.requests.protocol.http.HttpRequestController;
import io.github.pedroagrs.requests.protocol.socks.SocksRequestController;
import io.github.pedroagrs.requests.protocol.socks.proxy.SocksCustomProxySelector;

import java.net.Proxy;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.List;

public class Requests {
    private static final List<String> BLACKLIST_COUNTRIES = List.of(
            "US", "CA", "BR", "HK", "RU", "DE", "FR", "IN", "AR", "IT", "NL", "ID", "KR", "PL", "GB", "SG", "AE", "CN",
            "BD");

    public static final String REQUEST_URL = "";

    public static final URI REQUEST_URI = URI.create(REQUEST_URL);

    private static final int MAX_PROXIES = 100;

    public static void main(String[] args) {
        new SocksRequestController(100, () -> RequestProxyURLDecoderBuilder.builder()
                .urlText("https://github.com/proxylist-to/proxy-list/blob/main/socks5.txt")
                .elementsClass("blob-code blob-code-inner js-file-line")
                .type(ProtocolDecoderType.COMPLEX_HOST_AND_PORT_GITHUB)
                .proxyType(Proxy.Type.SOCKS)
                .build()
                .create()
                .stream()
                .map(customProxy -> new SocksCustomProxySelector(customProxy.getProxy(), customProxy.getId()))
                .limit(MAX_PROXIES)
                .collect(ArrayDeque::new, ArrayDeque::add, ArrayDeque::addAll));

        System.out.println("------------------------------------");

        new HttpRequestController(100, () -> RequestProxyURLDecoderBuilder.builder()
                .urlText("https://github.com/proxylist-to/proxy-list/blob/main/http.txt")
                .elementsClass("blob-code blob-code-inner js-file-line")
                .type(ProtocolDecoderType.COMPLEX_HOST_AND_PORT_GITHUB)
                .build()
                .create()
                .stream()
                .limit(MAX_PROXIES)
                .collect(ArrayDeque::new, ArrayDeque::add, ArrayDeque::addAll));
    }
}
