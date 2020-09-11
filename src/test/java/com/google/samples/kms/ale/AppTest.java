/*
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

package com.google.samples.kms.ale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.samples.kms.CryptoHelper;
import com.google.samples.kms.CryptoHelperEnvelope;

import com.google.samples.kms.ale.CsvHelper;
import com.google.samples.kms.redis.RedisBulkloadHelper;
import com.google.samples.kms.redis.RedisHelper;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {

  private static final Preferences preferences = initializePreferences();
  private static final String redisHost4_0 = preferences.get("redisHost4_0", "127.0.0.1");
  private static final String redisHost3_2 = preferences.get("redisHost3_2", "127.0.0.2");
  private static final boolean redisIsOnline = preferences.getBoolean("redisIsOnline", false);
  private static final int redisBatchSize = preferences.getInt("redisBatchSize", 5000);

  private static Preferences initializePreferences() {
    final String name = AppTest.class.getCanonicalName();
    Preferences preferences = Preferences.userNodeForPackage(AppTest.class);
    if (!preferences.getBoolean(name, false)) {
      preferences.putBoolean(name, Boolean.TRUE);
      try {
        preferences.flush();
      } catch (BackingStoreException e) {
        e.printStackTrace();
      }
    }
    return preferences;
  }

  @Test
  public void testPreferences() throws BackingStoreException {
    String v = preferences.get("keysetFilename", "keyset.json");
    assertEquals("keyset.json", v);
  }

  @Test
  public void testRedis4_0() {
    if (redisIsOnline) {
      RedisHelper.testConnection(redisHost4_0);
    }
  }

  @Test
  public void testRedis3_2() {
    if (redisIsOnline) {
      RedisHelper.testConnection(redisHost3_2);
    }
  }

  @Test
  public void testRedisRoundtripRedis3_2()
      throws UnsupportedEncodingException, NullPointerException, GeneralSecurityException, IOException {
    testRedisRoundtripRedisSeries(redisHost3_2);
  }

  @Test
  public void testRedisRoundtripRedis4_0()
      throws UnsupportedEncodingException, NullPointerException, GeneralSecurityException, IOException {
    testRedisRoundtripRedisSeries(redisHost4_0);
  }

  public void testRedisRoundtripRedisSeries(String host)
      throws UnsupportedEncodingException, NullPointerException, GeneralSecurityException, IOException {
    if (redisIsOnline) {
      testRedisRoundtripClear(host);
      testRedisRoundtrip(host);
      testRedisRoundtripClear(host);
      testRedisRoundtrip(host);
      testRedisRoundtripClear(host);
      testRedisRoundtrip(host);
    }
  }

  public void testRedisRoundtrip(String host) throws java.security.GeneralSecurityException, java.io.IOException,
      UnsupportedEncodingException, NullPointerException {
    CryptoHelper ctx = new CryptoHelper().loadKeyset(false);
    List<Map.Entry<String, String>> data = new ArrayList<Map.Entry<String, String>>();
    for (int i = 0; i < redisBatchSize; i++) {
      data.add(new AbstractMap.SimpleEntry<String, String>(String.format("%d", i), UUID.randomUUID().toString()));
    }
    RedisHelper r = new RedisHelper(ctx, host, 6379);
    long time = System.nanoTime();
    r.set(data.iterator());
    System.out.println(String.format("Set of %d encrypted values took on average %dµs on host %s", redisBatchSize,
        (System.nanoTime() - time) / redisBatchSize / 1000, host));
    List<String> keys = new ArrayList<String>();
    for (Map.Entry<String, String> kv : data) {
      keys.add(kv.getKey());
    }
    String[] keyArray = keys.toArray(new String[0]);
    time = System.nanoTime();
    List<String> clearText = r.get(keyArray);
    System.out.println(String.format("Get of %d encrypted values took on average %dµs on host %s", redisBatchSize,
        (System.nanoTime() - time) / redisBatchSize / 1000, host));

    Iterator<String> actual = clearText.iterator();
    for (Map.Entry<String, String> expected : data) {
      assertEquals(expected.getValue(), actual.next());
    }
  }

  public void testRedisRoundtripClear(String host) throws java.security.GeneralSecurityException, java.io.IOException,
      UnsupportedEncodingException, NullPointerException {
    CryptoHelper ctx = new CryptoHelper().loadKeyset(false);
    List<Map.Entry<String, String>> data = new ArrayList<Map.Entry<String, String>>();
    for (int i = 0; i < redisBatchSize; i++) {
      data.add(new AbstractMap.SimpleEntry<String, String>(String.format("%d", i), UUID.randomUUID().toString()));
    }
    RedisHelper r = new RedisHelper(ctx, host, 6379);
    long time = System.nanoTime();
    r.setClear(data.iterator());
    System.out.println(String.format("Set of %d cleartext values took on average %dµs on host %s", redisBatchSize,
        (System.nanoTime() - time) / redisBatchSize / 1000, host));
    List<String> keys = new ArrayList<String>();
    for (Map.Entry<String, String> kv : data) {
      keys.add(kv.getKey());
    }
    String[] keyArray = keys.toArray(new String[0]);
    time = System.nanoTime();
    List<String> clearText = r.getClear(keyArray);
    System.out.println(String.format("Get of %d cleartext values took on average %dµs on host %s", redisBatchSize,
        (System.nanoTime() - time) / redisBatchSize / 1000, host));

    Iterator<String> actual = clearText.iterator();
    for (Map.Entry<String, String> expected : data) {
      assertEquals(expected.getValue(), actual.next());
    }
  }

  @Test
  public void testGetKmsClient() throws java.security.GeneralSecurityException {
    CryptoHelper ctx = new CryptoHelper().loadKeyset(false);
    Assert.assertNotEquals(null, ctx.getKmsClient());
  }

  @Test
  public void testEncryptCsv() throws java.security.GeneralSecurityException, IOException {
    CryptoHelper ctx = new CryptoHelper().loadKeyset(false);
    String outputFileName = String.format("data-encrypted-%s.csv", UUID.randomUUID().toString());
    CsvHelper.encrypt(ctx, "data.csv", outputFileName);
    File f = new File(outputFileName);
    assertTrue(f.exists());
    f.delete();
  }

  @Test
  public void testGetKeyset() throws java.security.GeneralSecurityException, IOException {
    CryptoHelper ctx = new CryptoHelper().loadKeyset(false);
    String keysetBase64String = ctx.getEncodedKeysetHandle();
    ctx.setEncodedKeysetHandle(keysetBase64String);
    Assert.assertNotEquals(null, keysetBase64String);
    String text = "David Kubelka";
    String ciphertext = ctx.encrypt(text);
    String clearText = ctx.decrypt(ciphertext);
    assertEquals(text, clearText);
  }

  @Test
  public void testEncryptBulk() throws GeneralSecurityException, IOException {
    String outputFileName = String.format("data-encrypted-%s.rb", UUID.randomUUID().toString());
    CryptoHelper ctx = new CryptoHelper().loadKeyset(false);
    RedisBulkloadHelper.encrypt(ctx, "data.csv", outputFileName);
    File f = new File(outputFileName);
    assertTrue(f.exists());
    f.delete();
  }

  @Test
  public void testEncryptEnvelope()
      throws java.security.GeneralSecurityException, java.io.IOException, UnsupportedEncodingException {
    CryptoHelper ctx = new CryptoHelperEnvelope().loadKeyset(false);
    String text = "David Kubelka";
    String ciphertext = ctx.encrypt(text);
    System.out.println(String.format("Envelope encryption cipherlength: %d", ciphertext.length()));
    String clearText = ctx.decrypt(ciphertext);
    assertEquals(text, clearText);
  }

  @Test
  public void testEncrypt()
      throws java.security.GeneralSecurityException, java.io.IOException, UnsupportedEncodingException {
    CryptoHelper ctx = new CryptoHelper().loadKeyset(false);
    String text = "David Kubelka";
    String ciphertext = ctx.encrypt(text);
    System.out.println(String.format("AES256_GCM encryption cipherlength: %d", ciphertext.length()));
    String clearText = ctx.decrypt(ciphertext);
    assertEquals(text, clearText);
  }

  /**
   * Rigorous Test :-).
   */
  @Test
  public void shouldAnswerWithTrue() {
    assertTrue(true);
  }

  @Test
  public void testGetAeadEnvelope() throws java.security.GeneralSecurityException, IOException {
    CryptoHelper ctx = new CryptoHelperEnvelope().loadKeyset(false);
    Assert.assertNotEquals(null, ctx.getAead());
  }

  @Test
  public void testGetAead() throws java.security.GeneralSecurityException, IOException {
    CryptoHelper ctx = new CryptoHelper().loadKeyset(false);
    Assert.assertNotEquals(null, ctx.getAead());
  }
}
