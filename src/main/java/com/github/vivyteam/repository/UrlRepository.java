package com.github.vivyteam.repository;

import com.github.vivyteam.model.Url;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UrlRepository extends R2dbcRepository<Url, Long> {
    Mono<Url> findByShortUrl(String shortUrl);
    Mono<Url> findByOriginalUrl(String originalUrl);
}
