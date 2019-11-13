/**
 * Copyright 2019 Google LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.google.samples.kms.redis;

import com.google.samples.kms.CryptoHelper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

public class RedisHelper {
  static final String redisKeysetKey = "--tink-keyset--";
  private CryptoHelper cryptoHelper;
  private JedisPool jedisPool;

  public RedisHelper(CryptoHelper ctx, String host, int port) throws GeneralSecurityException, IOException {
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setTestOnBorrow(true);
    poolConfig.setTestWhileIdle(true);
    jedisPool = new JedisPool(poolConfig, host, port, 30);
    this.cryptoHelper = ctx.loadKeyset(false);
    String k = getRaw(redisKeysetKey);
    if (k == null) {
      setRaw(redisKeysetKey, ctx.getEncodedKeysetHandle());
    } else {
      ctx.setEncodedKeysetHandle(k);
    }
  }

  private void setRaw(String key, String value) {
    try (Jedis j = jedisPool.getResource()) {
      j.set(key, value);
    }
  }

  private String getRaw(String key) {
    try (Jedis j = jedisPool.getResource()) {
      return j.get(key);
    }
  }

  public void set(Iterator<Map.Entry<String, String>> kvs) throws NullPointerException, GeneralSecurityException, IOException {
    try (Pipeline p = jedisPool.getResource().pipelined()) {
      while (kvs.hasNext()) {
        Map.Entry<String, String> r = kvs.next();
        p.set(r.getKey(), cryptoHelper.encrypt(r.getValue()));
      }
      p.sync();
    }
  }

  public void setClear(Iterator<Map.Entry<String, String>> kvs)
      throws NullPointerException, GeneralSecurityException, IOException {
    try (Pipeline p = jedisPool.getResource().pipelined()) {
      while (kvs.hasNext()) {
        Map.Entry<String, String> r = kvs.next();
        p.set(r.getKey(), r.getValue());
      }
      p.sync();
    }
  }

  public List<String> get(String... keys)
      throws UnsupportedEncodingException, NullPointerException, GeneralSecurityException, IOException {
    Jedis j = jedisPool.getResource();
    List<String> values = j.mget(keys);
    List<String> l = new ArrayList<String>();
    for (String v : values) {
      l.add(cryptoHelper.decrypt(v));
    }
    return l;
  }

  public List<String> getClear(String... keys)
      throws UnsupportedEncodingException, NullPointerException, GeneralSecurityException, IOException {
    Jedis j = jedisPool.getResource();
    List<String> values = j.mget(keys);
    return values;
  }

  public static void testConnection(String host) {
    try (Jedis jedis = new Jedis(host, 6379)) {
      try (Pipeline pipeline = jedis.pipelined()) {
        int count = 10000;
        for (int i = 0; i < count; i++) {
          String kv = UUID.randomUUID().toString();
          pipeline.set(kv, kv);
        }
        pipeline.sync();
        pipeline.close();
      }
      jedis.close();
    }
  }
}