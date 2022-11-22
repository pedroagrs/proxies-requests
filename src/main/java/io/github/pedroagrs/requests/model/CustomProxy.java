package io.github.pedroagrs.requests.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.Proxy;

@RequiredArgsConstructor
@Getter
public class CustomProxy {

    private final String id;
    private final Proxy proxy;

}
