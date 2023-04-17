package com.github.vivyteam.service;

import com.github.vivyteam.exception.UrlNotFoundException;
import com.github.vivyteam.model.Url;
import com.github.vivyteam.repository.UrlRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class UrlService {

    private static final String BASE_URL = "https://myservicedomain.de/";
    private static final int SHORT_URL_LENGTH = 6;
    private final UrlRepository urlRepository;

    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public Mono<String> createAndSaveShortUrl(String originalUrl) {
        return urlRepository.findByOriginalUrl(originalUrl)
                .switchIfEmpty(Mono.defer(() -> {
                    String hash = generateHash(originalUrl);
                    String shortUrl = BASE_URL + hash.substring(0, SHORT_URL_LENGTH);
                    return urlRepository.save(new Url(originalUrl, shortUrl));
                }))
                .map(Url::getShortUrl);
    }

    public Mono<String> getOriginalUrl(String shortUrl) throws UrlNotFoundException {
        return urlRepository.findByShortUrl(shortUrl)
                .map(Url::getOriginalUrl)
                .switchIfEmpty(Mono.error(new UrlNotFoundException("Short URL not found.")));
    }
    public Mono<String> redirect(String shortUrl) {
        return getOriginalUrl(shortUrl);
    }

    private String generateHash(String originalUrl) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(originalUrl.getBytes(StandardCharsets.UTF_8));
            String base64Encoded = Base64.getUrlEncoder().encodeToString(hashBytes);
            return base64Encoded.replace("=", "");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
