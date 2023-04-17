package com.github.vivyteam.controller;

import com.github.vivyteam.service.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class UrlControllerTest {

    @Mock
    private UrlService urlService;
    private WebTestClient client;
    private static final String ORIGINAL_URL = "https://www.google.com";
    private static final String SHORT_URL = "https://myservicedomain.de/abc123";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        UrlController urlController = new UrlController(urlService);
        client = WebTestClient.bindToController(urlController).build();
    }

    @Test
    @DisplayName("POST /shorten - Success")
    void testShortenUrlSuccess() {
        // Arrange
        String originalUrl = ORIGINAL_URL;
        String shortUrl = SHORT_URL;
        when(urlService.createAndSaveShortUrl(originalUrl)).thenReturn(Mono.just(shortUrl));

        // Act & Assert
        client.post()
                .uri("/shorten")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue(originalUrl)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(shortUrl);

        verify(urlService).createAndSaveShortUrl(originalUrl);
    }

    @Test
    @DisplayName("GET /original - Success")
    void testGetOriginalUrlSuccess() {
        // Arrange
        String originalUrl = ORIGINAL_URL;
        String shortUrl = SHORT_URL;
        when(urlService.getOriginalUrl(shortUrl)).thenReturn(Mono.just(originalUrl));

        // Act & Assert
        client.get()
                .uri(uriBuilder -> uriBuilder.path("/original").queryParam("shortUrl", shortUrl).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(originalUrl);

        verify(urlService).getOriginalUrl(shortUrl);
    }

    @Test
    @DisplayName("POST /shorten - Missing Body")
    void testShortenUrlMissingBody() {
        // Act & Assert
        client.post()
                .uri("/shorten")
                .contentType(MediaType.TEXT_PLAIN)
                .exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(urlService);
    }

    @Test
    @DisplayName("GET /redirect - Success")
    public void testRedirectSuccess() {
        // Arrange
        String originalUrl = ORIGINAL_URL;
        String shortUrl = SHORT_URL;
        given(urlService.redirect(shortUrl)).willReturn(Mono.just(originalUrl));

        // Act & Assert
        client.get()
                .uri(uriBuilder -> uriBuilder.path("/redirect").queryParam("shortUrl", shortUrl).build())
                .exchange()
                .expectStatus().isPermanentRedirect()
                .expectHeader().valueEquals(HttpHeaders.LOCATION, originalUrl);

        verify(urlService).redirect(shortUrl);
    }
}
