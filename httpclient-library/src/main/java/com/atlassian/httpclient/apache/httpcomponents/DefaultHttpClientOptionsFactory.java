package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.factory.HttpClientOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class DefaultHttpClientOptionsFactory {

    @Bean
    public static HttpClientOptions getOptions() {
        HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setIgnoreCookies(true);
        return httpClientOptions;
    }
}
