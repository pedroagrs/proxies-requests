package io.github.pedroagrs.requests.decoder;

import io.github.pedroagrs.requests.model.CustomProxy;
import io.github.pedroagrs.requests.protocol.decoder.ProtocolDecoderType;
import io.github.pedroagrs.requests.protocol.decoder.RequestDecoder;
import io.github.pedroagrs.requests.util.RequestsUtil;
import lombok.Builder;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

@Builder
public class RequestProxyURLDecoderBuilder implements RequestDecoder {

    @Builder.Default
    private final Proxy.Type proxyType = Proxy.Type.HTTP;

    private boolean ignoreBlacklist;

    private static final Logger LOGGER = Logger.getLogger(RequestProxyURLDecoderBuilder.class.getName());

    private final String urlText;

    // Used to capture elements/div using JSoup (complex pages)
    private final String elementsClass, select, category;

    private final ProtocolDecoderType type;

    private final List<String> countriesBlacklist;

    @Override
    public List<CustomProxy> create() {
        if (type == ProtocolDecoderType.COMPLEX_HOST_AND_PORT_GITHUB && elementsClass == null) {
            throw new IllegalArgumentException("elementsClass cannot be null when using " + type.name());
        }

        final List<CustomProxy> proxies = new LinkedList<>();

        try {
            if (type == ProtocolDecoderType.COMPLEX_HOST_AND_PORT_GITHUB) {
                ProxySelector.setDefault(null); // reset proxy selector

                final Document document = Jsoup.connect(urlText)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .proxy(Proxy.NO_PROXY)
                        .url(urlText)
                        .userAgent("Mozilla/5.0")
                        .get();

                final List<InetSocketAddress> elements =
                        fetchProxiesFromGithub(document.getElementsByClass(elementsClass));

                elements.forEach(proxyAddress -> {
                    final Proxy proxy = new Proxy(proxyType, proxyAddress);

                    proxies.add(new CustomProxy(proxyAddress.getHostString() + ":" + proxyAddress.getPort(), proxy));
                });
            } else if (type == ProtocolDecoderType.HOST_AND_PORT) {
                final URL url = new URL(urlText);

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                    RequestsUtil.addReaderProxies(proxies, reader, type, countriesBlacklist, proxyType);
                }
            } else if (type == ProtocolDecoderType.COMPLEX_HOST_AND_PORT_DOCUMENT) {
                proxies.addAll(
                        RequestsUtil.fetchProxiesFromElement(urlText, elementsClass, select, category, proxyType));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        if (!ignoreBlacklist)
            applyBlacklist(proxies);


        LOGGER.info("Loaded " + proxies.size() + " proxies from " + urlText + " (" + proxyType.name() + ")");

        return proxies;
    }

    /**
     * Use this method to get the elements in github
     * @param elements the elements from Jsoup
     * @see <a href="https://stackoverflow.com/questions/62785962/get-raw-file-from-github-without-waiting-for-5-minute-cache-update">Github raw delay</a>
     * @return new list of InetSocketAddress
     */
    private static List<InetSocketAddress> fetchProxiesFromGithub(Elements elements) {
        final List<InetSocketAddress> addresses = new LinkedList<>();

        for (Element element : elements) {
            final String[] split =
                    element.toString().split(">")[1].split("<")[0].split(":");

            if (split.length != 2) continue;

            final String host = split[0];
            final int port = NumberUtils.toInt(split[1], -1);

            if (port == -1) continue;

            addresses.add(new InetSocketAddress(host, port));
        }

        return addresses;
    }
}
