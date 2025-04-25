package com.zsh.petsystem.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

  @Resource
  private RedisTemplate<String, Object> redisTemplate;

  /**
   * 设置缓存
   */
  public void set(String key, Object value, Long timeout) {
    redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
  }

  /**
   * 获取缓存
   */
  public Object get(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  /**
   * 删除缓存
   */
  public Boolean delete(String key) {
    return redisTemplate.delete(key);
  }

  /**
   * 判断key是否存在
   */
  public Boolean hasKey(String key) {
    return redisTemplate.hasKey(key);
  }
}