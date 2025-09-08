package com.nexsplit.config;

import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

@Configuration
public class TomcatConfig {

    @Bean
    public TomcatProtocolHandlerCustomizer<ProtocolHandler> protocolHandlerVirtualThreads() {
        return protocolHandler -> {
            if (protocolHandler instanceof Http11NioProtocol http) {
                http.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            }
        };
    }
}
