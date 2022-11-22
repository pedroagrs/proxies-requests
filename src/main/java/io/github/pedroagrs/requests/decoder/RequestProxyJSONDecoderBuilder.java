package io.github.pedroagrs.requests.decoder;

import io.github.pedroagrs.requests.model.CustomProxy;
import io.github.pedroagrs.requests.protocol.decoder.RequestDecoder;
import lombok.Builder;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

@Builder
public class RequestProxyJSONDecoderBuilder implements RequestDecoder {

    private final String url, addressPath, portPath, countryPath;

    private final boolean ignoreBlacklist;

    @Builder.Default
    private final Proxy.Type proxyType = Proxy.Type.HTTP;

    private final List<String> countriesBlacklist;


    @Override
    public List<CustomProxy> create() {
        final List<CustomProxy> proxies = new LinkedList<>();

        try {
            JSONObject jsonObject = JsonReader.readJsonFromUrl(url);
            JSONArray data = (JSONArray) jsonObject.get("data");

            for (int i = 0; i < data.length(); i++) {
                JSONObject proxy = (JSONObject) data.get(i);
                String country = (String) proxy.get(countryPath);

                if (countriesBlacklist.contains(country)) {
                    continue;
                }

                int port = NumberUtils.toInt(proxy.get(portPath).toString(), -1);

                String address = (String) proxy.get(addressPath);

                if (port == -1) {
                    continue;
                }

                InetSocketAddress proxyAddress = new InetSocketAddress(address, port);

                proxies.add(new CustomProxy(
                        proxyAddress.getHostString() + ":" + proxyAddress.getPort(),
                        new Proxy(proxyType, proxyAddress)));
            }

            if (!ignoreBlacklist) applyBlacklist(proxies);

            LOGGER.info("Loaded " + proxies.size() + " proxies from " + url + " (" + proxyType.name() + ")");
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return proxies;
    }

    private static class JsonReader {
        private static JSONObject readJsonFromUrl(String url) throws IOException {
            try (InputStream inputStream = new URL(url).openStream();
                    Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                return new JSONObject(read(reader));
            }
        }

        private static String read(Reader reader) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();

            int line;

            while ((line = reader.read()) != -1) {
                stringBuilder.append((char) line);
            }

            return stringBuilder.toString();
        }
    }
}
