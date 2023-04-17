package com.github.vivyteam.controller;

import com.github.vivyteam.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
public class UrlController {
    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    public Mono<String> shortenUrl(@RequestBody String originalUrl) {
        return urlService.createAndSaveShortUrl(originalUrl);
    }

    @GetMapping("/original")
    public Mono<String> getOriginalUrl(@RequestParam String shortUrl) {
        return urlService.getOriginalUrl(shortUrl);
    }

    @GetMapping("/redirect")
    public Mono<Void> redirect(@RequestParam String shortUrl, ServerHttpResponse response) {
        return urlService.redirect(shortUrl)
                .doOnNext(url -> {
                    response.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
                    response.getHeaders().setLocation(URI.create(url));
                })
                .then();
    }
 }
