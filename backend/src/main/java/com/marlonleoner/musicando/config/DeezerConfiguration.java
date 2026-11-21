package com.marlonleoner.musicando.config;

import com.marlonleoner.musicando.client.deezer.DeezerClient;
import com.marlonleoner.musicando.client.deezer.DeezerLinkClient;
import com.marlonleoner.musicando.config.properties.DeezerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.http.HttpClient;

@Configuration
public class DeezerConfiguration {

    @Bean
    public DeezerClient deezerClient(final DeezerProperties properties) {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.timeout());
        requestFactory.setReadTimeout(properties.timeout());

        final RestClient restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();

        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(DeezerClient.class);
    }

    /**
     * Cliente para abrir links curtos da Deezer. Não segue redirecionamentos: quem decide se o próximo endereço é confiável é o
     * {@link com.marlonleoner.musicando.integration.DeezerIntegrator}.
     */
    @Bean
    public DeezerLinkClient deezerLinkClient(final DeezerProperties properties) {
        final HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(properties.timeout())
                .build();
        final JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.timeout());

        final RestClient restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();

        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(DeezerLinkClient.class);
    }
}
