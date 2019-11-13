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

import com.google.samples.kms.ale.CsvHelper;

import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.commons.csv.CSVRecord;

public class RedisBulkloadHelper {

  public static String formatToRedisBulkString(String key, String member) {
    return String.format(//
        "*3\r\n$3\r\nSET\r\n$%d\r\n%s\r\n$%d\r\n%s\r\n", //
        key.length(), key, member.length(), member);
  }

  public static void encrypt(CryptoHelper cryptoHelper, String csvFileName, String redisBulkloadFileName) //
      throws IOException, GeneralSecurityException {
    FileWriter out = new FileWriter(redisBulkloadFileName);
    out.write(formatToRedisBulkString("--tink-keyset--", cryptoHelper.getEncodedKeysetHandle()));
    for (CSVRecord record : CsvHelper.read(csvFileName)) {
      String key = record.get(CsvHelper.Headers.Key);
      String member = record.get(CsvHelper.Headers.Member);
      String ciphertext = cryptoHelper.encrypt(member);
      out.write(formatToRedisBulkString(key, ciphertext));
    }
    out.flush();
    out.close();
  }
}