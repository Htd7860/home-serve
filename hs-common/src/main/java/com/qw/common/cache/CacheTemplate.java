package com.qw.common.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.qw.common.utils.RandomTTL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @Author：qw
 * @Package：common.cache
 * @Project：home-serve
 * @name：MultipleLevelChache
 * @Date：2026/5/18 15:11
 * @Filename：MultipleLevelChache
 */
@Slf4j
@Component
public class CacheTemplate {
    public static final Object NULL_SIGN=new Object();

    ObjectMapper objectMapper;
    StringRedisTemplate stringRedisTemplate;
    Cache<String,Object> cache;

    public CacheTemplate(ObjectMapper objectMapper,StringRedisTemplate stringRedisTemplate,Cache<String,Object> cache){
        this.cache=cache;
        this.objectMapper=objectMapper;
        this.stringRedisTemplate=stringRedisTemplate;
    }

    @SuppressWarnings("unchecked")
    public <T,O> T get(String cacheKey,O dbKey,Class<T> clazz, Function<O,T> function){
      T res= (T)cache.getIfPresent(cacheKey);

      if(res==null){
          String json = stringRedisTemplate.opsForValue().get(cacheKey);
        if(json!=null){
            if("".equals(json)){cache.put(cacheKey,NULL_SIGN);return null;}
            try {
                res=objectMapper.readValue(json,clazz);
            } catch (JsonProcessingException e) {
               log.error("{}",e);
               stringRedisTemplate.delete(cacheKey);
               return null;
            }
            cache.put(cacheKey,res);
            return res;
        }
      }else if(res==NULL_SIGN){return null;}
      else{
          return res;
      }

        if(res==null){res = function.apply(dbKey);}
      if(res==null){
          stringRedisTemplate.opsForValue().set(cacheKey,"",RandomTTL.randomTTL(1));
          cache.put(cacheKey,NULL_SIGN);
          return null;
      }else{
          try {
              stringRedisTemplate.opsForValue().set(cacheKey,objectMapper.writeValueAsString(res),RandomTTL.randomTTL(30));
          } catch (JsonProcessingException e) {
              log.error("{}",e);
          }
          cache.put(cacheKey,res);
      }
      return res;
    }

    public void clear(String cacheKey){
        cache.invalidate(cacheKey);
        stringRedisTemplate.delete(cacheKey);
    }

    @SuppressWarnings("unchecked")
    public <T,O> List<T> getList(String cacheKey, O dbKey, Class<T> clazz, Function<O,List<T>> function){
        List<T> res= (List<T>) cache.getIfPresent(cacheKey);

        if(res==null){
            String json = stringRedisTemplate.opsForValue().get(cacheKey);
            if(json!=null){
                if("".equals(json)){cache.put(cacheKey, Arrays.asList(NULL_SIGN));return null;}
                try {
                    res=objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class,clazz));
                    cache.put(cacheKey,res);
                    return res;
                } catch (JsonProcessingException e) {
                    log.error("{}",e);
                    stringRedisTemplate.delete(cacheKey);
                    return null;
                }
            }
        }else if(res.size()==1&&res.get(0)==NULL_SIGN){return null;}
        else{
            return res;
        }

        if(res==null){res = function.apply(dbKey);}
        if(res==null){
            stringRedisTemplate.opsForValue().set(cacheKey,"",RandomTTL.randomTTL(1));
            cache.put(cacheKey,Arrays.asList(NULL_SIGN));
            return null;
        }else{
            try {
                stringRedisTemplate.opsForValue().set(cacheKey,objectMapper.writeValueAsString(res),RandomTTL.randomTTL(30));
            } catch (JsonProcessingException e) {
                log.error("{}",e);
            }
            cache.put(cacheKey,res);
        }
        return res;
    }

}
