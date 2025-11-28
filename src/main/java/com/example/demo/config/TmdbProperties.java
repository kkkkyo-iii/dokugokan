package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tmdb.api") // "tmdb.api"から始まるプロパティをマッピング
public class TmdbProperties {

    /**
     * TMDB APIのキー
     */
    private String key;

    // keyに対するゲッターとセッター
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}