package com.sherlocky.springboot2.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.UnknownHostException;

/**
 * 装配 RedisTemplate Bean （也可直接定义在SpringBoot的 Application 启动类中）
 * @author: zhangcx
 * @date: 2019/1/16 16:35
 */
@Configuration
public class RedisTemplateConfiguration {
    /**
     * 覆盖 SpringBoot reids 默认使用的序列化器（默认的为二进制，对可视化不友好）
     */
    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory)
            throws UnknownHostException {
        RedisTemplate<Object, Object> template = new RedisTemplate<Object, Object>();
        template.setConnectionFactory(redisConnectionFactory);

        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        // 设置值（value）的序列化采用 Jackson2JsonRedisSerializer
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // 设置值（key）的序列化采用 StringRedisSerializer
        template.setKeySerializer(new StringRedisSerializer());

        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}