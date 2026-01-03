package com.shelfpulse.activation_automation.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url:}")
    private String dbUrl;

    @Value("${spring.datasource.username:}")
    private String dbUsername;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();

        String jdbcUrl = dbUrl;
        String username = dbUsername;
        String password = dbPassword;

        if (dbUrl != null && !dbUrl.isEmpty()) {
            if (dbUrl.startsWith("postgresql://") || dbUrl.startsWith("postgres://")) {
                try {
                    URI uri = new URI(dbUrl);
                    String host = uri.getHost();
                    int port = uri.getPort() != -1 ? uri.getPort() : 5432;
                    String database = uri.getPath().substring(1);

                    jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;

                    String userInfo = uri.getUserInfo();
                    if (userInfo != null && !userInfo.isEmpty()) {
                        String[] parts = userInfo.split(":", 2);
                        if (parts.length >= 1 && (username == null || username.isEmpty())) {
                            username = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                        }
                        if (parts.length >= 2 && (password == null || password.isEmpty())) {
                            password = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                        }
                    }

                    String query = uri.getQuery();
                    if (query != null && !query.isEmpty()) {
                        jdbcUrl += "?" + query;
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse database URL: " + dbUrl, e);
                }
            }
        }

        dataSource.setJdbcUrl(jdbcUrl);

        if (username != null && !username.isEmpty()) {
            dataSource.setUsername(username);
        }
        if (password != null && !password.isEmpty()) {
            dataSource.setPassword(password);
        }

        dataSource.setDriverClassName("org.postgresql.Driver");

        return dataSource;
    }
}
