package com.shelfpulse.activation_automation.config;

import io.lettuce.core.RedisCredentials;
import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.url:}")
    private String redisUrl;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        if (StringUtils.hasText(redisUrl)) {
            RedisURI redisURI = RedisURI.create(redisUrl);
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(redisURI.getHost());
            config.setPort(redisURI.getPort());
            config.setDatabase(redisURI.getDatabase());
            RedisCredentials credentials = redisURI.getCredentialsProvider().resolveCredentials().block();
            if (credentials != null && credentials.getPassword() != null && credentials.getPassword().length > 0) {
                config.setPassword(RedisPassword.of(credentials.getPassword()));
            }
            return new LettuceConnectionFactory(config);
        }

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (StringUtils.hasText(redisPassword)) {
            config.setPassword(RedisPassword.of(redisPassword));
        }
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        RedisSerializer<Object> jsonSerializer = RedisSerializer.json();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
