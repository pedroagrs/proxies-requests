package io.github.pedroagrs.requests.protocol.http;

import io.github.pedroagrs.requests.Requests;
import io.github.pedroagrs.requests.blacklist.BlackListController;
import io.github.pedroagrs.requests.blacklist.SimpleBlackListFactory;
import io.github.pedroagrs.requests.model.CustomProxy;
import io.github.pedroagrs.requests.scheduler.RequestsTimerScheduler;
import io.github.pedroagrs.requests.util.HttpRequestUtil;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.ProxySelector;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class HttpRequestController {

    public static final int DEFAULT_TIMEOUT = 10000;

    private static final Logger LOGGER = Logger.getLogger("[HttpLogger]");

    private static final String SKIPPING_FORMAT = "[HttpLogger] Invalid proxy: %s (%sth | %s S)";

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(2);

    private Queue<CustomProxy> proxies;

    private final Supplier<Queue<CustomProxy>> updater;

    private final int maxProxiesRefresh;

    private int proxyCounter, updaterCounter, successfullyProxies;

    private final SimpleBlackListFactory blackListFactory =
            BlackListController.getInstance().getFactory();

    private boolean ignoreBlacklist = false;

    public HttpRequestController(int maxProxiesRefresh, Supplier<Queue<CustomProxy>> updater) {
        this.updater = updater;
        this.proxies = updater.get();
        this.maxProxiesRefresh = Math.max(maxProxiesRefresh, proxies.size());

        new RequestsTimerScheduler(Math.min(2, DEFAULT_TIMEOUT / 1000), this::nextProxy);
    }

    public HttpRequestController(int maxProxiesRefresh, Supplier<Queue<CustomProxy>> updater, boolean ignoreBlacklist) {
        this.updater = updater;
        this.proxies = updater.get();
        this.maxProxiesRefresh = maxProxiesRefresh;
        this.ignoreBlacklist = ignoreBlacklist;

        new RequestsTimerScheduler(Math.min(2, DEFAULT_TIMEOUT / 1000), this::nextProxy);
    }

    public void nextProxy() {
        if (proxies.isEmpty()) {
            refreshProxies();
            return;
        }

        CustomProxy currentProxy = proxies.peek();

        if (currentProxy == null) return;

        if (ignoreBlacklist) {
            proxies.removeIf(proxy -> proxy.getId().equals(currentProxy.getId()));
        } else if (blackListFactory.contains(currentProxy.getId())) {
            try {
                proxies.removeIf(proxy -> proxy.getId().equals(currentProxy.getId()));
            } catch (Exception ignored) {}
            return;
        }

        if (refreshProxies()) return;

        if (!ignoreBlacklist) blackListFactory.add(currentProxy.getId());

        EXECUTOR_SERVICE.submit(() -> {
            ProxySelector.setDefault(null);

            final HttpURLConnection connection =
                    HttpRequestUtil.createUrlConnection(currentProxy.getProxy(), Requests.REQUEST_URI);

            try {
                if (connection != null && connection.getResponseCode() == 200) {
                    try (final BufferedReader reader = HttpRequestUtil.getBufferedReader(connection)) {
                        final String responseEntity = reader.readLine();

                        if (!responseEntity.contains("cloudflare")) {
                            LOGGER.info(
                                    String.format("Success proxy: [%s] | %s", currentProxy.getId(), responseEntity));

                            successfullyProxies++;
                            return;
                        }

                        printInvalidAndRemove(currentProxy);
                    }
                }
            } catch (Exception ignored) {
                printInvalidAndRemove(currentProxy);
            }

            if (connection != null) connection.disconnect();
        });
    }

    public boolean refreshProxies() {
        if (updater != null && this.updaterCounter >= maxProxiesRefresh) {
            this.updaterCounter = 0;

            Queue<CustomProxy> newProxies;

            try {
                newProxies = updater.get();

                this.proxies = newProxies == null || newProxies.isEmpty() ? this.proxies : newProxies;

                return true;
            } catch (Exception ignored) {
                return false;
            }
        }

        return false;
    }

    private void printInvalidAndRemove(CustomProxy proxy) {
        remove(proxy.getId());

        final String message = String.format(SKIPPING_FORMAT, proxy.getId(), proxyCounter++, successfullyProxies);

        System.out.println(message);
    }

    private void remove(String id) {
        this.updaterCounter++;

        proxies.removeIf(proxy -> proxy.getId().equals(id));
    }
}