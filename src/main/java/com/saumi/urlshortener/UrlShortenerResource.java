package com.saumi.urlshortener;

import com.google.common.hash.Hashing;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@RequestMapping("/")
@RestController
public class UrlShortenerResource {

    @Autowired
    StringRedisTemplate redisTemplate;
    @GetMapping("/{id}")
    public String getUrl(@PathVariable String id) {
        var url = redisTemplate.opsForValue().get(id);
        System.out.println("Retrieved URL: " + url);
        return url;
    }

    @PostMapping
    public String createShortUrl(@RequestBody String url) {
        var validator = new UrlValidator(
                new String[] { "http", "https" }
        );

        if (validator.isValid(url)) {
            var hash = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
            if (!redisTemplate.hasKey(hash)) {
                redisTemplate.opsForValue().set(hash, url);
                System.out.println("URL hash generated: " + hash + " for: " + url);
            } else {
                System.out.println("URL hash already exists.");
            }
            return hash;
        }

        throw new RuntimeException("Invalid URL. Please only use `http` or `https` URLs.");
    }
}
