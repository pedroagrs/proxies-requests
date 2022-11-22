package io.github.pedroagrs.requests.util;

import io.github.pedroagrs.requests.model.CustomProxy;
import io.github.pedroagrs.requests.protocol.decoder.ProtocolDecoderType;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@UtilityClass
public class RequestsUtil {

    private static final Logger LOGGER = Logger.getLogger("RequestUtil");

    private final Pattern HOST_PATTERN = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");

    private final Pattern PORT_PATTERN = Pattern.compile("\\d{1,5}");

    public List<CustomProxy> fetchProxiesFromElement(
            @NonNull String urlText, @NonNull String element, String select, String category, Proxy.Type proxyType) {
        LOGGER.info("Refreshing proxies... (" + proxyType.name() + ")");

        final List<CustomProxy> proxies = new LinkedList<>();

        Document document;
        Connection connection;

        try {
            ProxySelector.setDefault(null);

            connection = Jsoup.connect(urlText).proxy(Proxy.NO_PROXY).timeout(3000);
            document = connection.get();
        } catch (Exception exception) {
            LOGGER.warning("Proxies site is down or request denied. (Error refreshing proxies)");
            return proxies;
        }

        final Elements elements = document.getElementsByClass(element);

        elements.select(select).forEach(tr -> {
            Elements tds = tr.select(category);
            String host = tds.get(0).text();
            String port = tds.get(1).text();

            if (HOST_PATTERN.matcher(host).matches()
                    && PORT_PATTERN.matcher(port).matches()) {
                InetSocketAddress proxyAddress = new InetSocketAddress(host, Integer.parseInt(port));

                CustomProxy customProxy = new CustomProxy(proxyAddress.toString(), new Proxy(proxyType, proxyAddress));

                proxies.add(customProxy);
            }
        });

        return proxies;
    }

    public InetSocketAddress getSocketAddress(
            String parser, ProtocolDecoderType decoderType, List<String> countriesBlacklist) {
        if (decoderType == null || decoderType == ProtocolDecoderType.HOST_AND_PORT) {
            String[] split = parser.split(":");

            if (split.length < 2) return null;

            String host = split[0];
            int port = NumberUtils.toInt(split[1], -1);

            if (port == -1) return null;

            return new InetSocketAddress(host, port);
        }

        for (String countries : countriesBlacklist) {
            if (parser.contains(countries)) return null;
        }

        String[] split = parser.replace("\"", "").split(",");
        String host = split[0].split(":")[0];
        int port = NumberUtils.toInt(split[0].split(":")[1], -1);

        return new InetSocketAddress(host, port);
    }

    public void addReaderProxies(
            List<CustomProxy> proxies,
            BufferedReader reader,
            ProtocolDecoderType type,
            List<String> countriesBlacklist,
            Proxy.Type proxyType)
            throws IOException {
        String line;

        while ((line = reader.readLine()) != null) {
            final InetSocketAddress proxyAddress = RequestsUtil.getSocketAddress(line, type, countriesBlacklist);

            if (proxyAddress == null) continue;

            final Proxy proxy = new Proxy(proxyType, proxyAddress);

            proxies.add(new CustomProxy(line, proxy));
        }
    }
}
