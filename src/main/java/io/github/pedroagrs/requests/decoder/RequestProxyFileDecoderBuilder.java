package io.github.pedroagrs.requests.decoder;

import io.github.pedroagrs.requests.model.CustomProxy;
import io.github.pedroagrs.requests.protocol.decoder.ProtocolDecoderType;
import io.github.pedroagrs.requests.protocol.decoder.RequestDecoder;
import io.github.pedroagrs.requests.util.RequestsUtil;
import lombok.Builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Proxy;
import java.util.LinkedList;
import java.util.List;

@Builder
public class RequestProxyFileDecoderBuilder implements RequestDecoder {

    @Builder.Default
    private final Proxy.Type proxyType = Proxy.Type.HTTP;

    private final File file;

    private final ProtocolDecoderType decoderType;

    private final List<String> countriesBlacklist;

    private boolean ignoreBlacklist;

    @Override
    public List<CustomProxy> create() {
        if (file == null || !file.exists()) throw new IllegalStateException("File proxies not found");

        final List<CustomProxy> proxies = new LinkedList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            RequestsUtil.addReaderProxies(proxies, reader, decoderType, countriesBlacklist, proxyType);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        if (!ignoreBlacklist) applyBlacklist(proxies);

        LOGGER.info("Loaded " + proxies.size() + " proxies from " + file.getName() + " (" + proxyType.name() + ")");

        return proxies;
    }
}
