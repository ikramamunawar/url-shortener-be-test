package com.github.vivyteam.service;

import com.github.vivyteam.exception.UrlNotFoundException;
import com.github.vivyteam.model.Url;
import com.github.vivyteam.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;
    private UrlService urlService;
    private static final String ORIGINAL_URL = "https://www.google.com";
    private static final String SHORT_URL = "https://myservicedomain.de/abc123";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        urlService = new UrlService(urlRepository);
    }

    @Test
    @DisplayName("Create and save short URL")
    void testCreateAndSaveShortUrl() {
        // Arrange
        String originalUrl = ORIGINAL_URL;
        String shortUrl = SHORT_URL;

        given(urlRepository.findByOriginalUrl(originalUrl)).willReturn(Mono.empty());
        given(urlRepository.save(any(Url.class))).willReturn(Mono.just(new Url(originalUrl, shortUrl)));

        // Act
        Mono<String> result = urlService.createAndSaveShortUrl(originalUrl);

        // Assert
        StepVerifier.create(result)
                .expectNext(shortUrl)
                .verifyComplete();

        verify(urlRepository).findByOriginalUrl(originalUrl);
        verify(urlRepository).save(any(Url.class));
    }

    @Test
    @DisplayName("Create and save short URL - Already exists")
    public void testCreateAndSaveShortUrlAlreadyExists() {
        // Arrange
        String originalUrl = ORIGINAL_URL;
        String shortUrl = SHORT_URL;

        given(urlRepository.findByOriginalUrl(originalUrl)).willReturn(Mono.just(new Url(originalUrl, shortUrl)));

        // Act
        Mono<String> result = urlService.createAndSaveShortUrl(originalUrl);

        // Assert
        StepVerifier.create(result)
                .expectNext(shortUrl)
                .verifyComplete();

        verify(urlRepository).findByOriginalUrl(originalUrl);
        verify(urlRepository, never()).save(any(Url.class));
    }

    @Test
    @DisplayName("Get original URL")
    void testGetOriginalUrl() {
        // Arrange
        String shortUrl = SHORT_URL;
        String originalUrl = ORIGINAL_URL;
        Url url = new Url(originalUrl, shortUrl);

        given(urlRepository.findByShortUrl(shortUrl)).willReturn(Mono.just(url));

        // Act
        Mono<String> result = urlService.getOriginalUrl(shortUrl);

        // Assert
        StepVerifier.create(result)
                .expectNext(originalUrl)
                .verifyComplete();

        verify(urlRepository).findByShortUrl(shortUrl);
    }

    @Test
    @DisplayName("Get original URL - Not found")
    void testGetOriginalUrlNotFound() {
        // Arrange
        String shortUrl = SHORT_URL;

        given(urlRepository.findByShortUrl(shortUrl)).willReturn(Mono.empty());

        // Act
        Mono<String> result = urlService.getOriginalUrl(shortUrl);

        // Assert
        StepVerifier.create(result)
                .expectError(UrlNotFoundException.class)
                .verify();

        verify(urlRepository).findByShortUrl(shortUrl);
    }

    @Test
    @DisplayName("Redirect")
    void testRedirect() {
        // Arrange
        String shortUrl = SHORT_URL;
        String originalUrl = ORIGINAL_URL;
        Url url = new Url(originalUrl, shortUrl);

        given(urlRepository.findByShortUrl(shortUrl)).willReturn(Mono.just(url));

        // Act
        Mono<String> result = urlService.redirect(shortUrl);

        // Assert
        StepVerifier.create(result)
                .expectNext(originalUrl)
                .verifyComplete();

        verify(urlRepository).findByShortUrl(shortUrl);
    }

}