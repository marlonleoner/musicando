package com.marlonleoner.musicando.client.deezer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.net.URI;

/**
 * Abre links da Deezer sem seguir redirecionamentos, para descobrir para onde um link curto de compartilhamento aponta.
 */
@HttpExchange
public interface DeezerLinkClient {

    @GetExchange
    ResponseEntity<Void> open(URI link);
}
