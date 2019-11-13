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

package com.google.samples.kms.ale;

import com.google.samples.kms.CryptoHelper;
import com.google.samples.kms.redis.RedisBulkloadHelper;

import java.io.IOException;
import java.security.GeneralSecurityException;

public final class ApplicationLayerEncryption {

  public static void main(String[] args) throws IOException, GeneralSecurityException {
    if (args.length == 0) {
      System.err.println("please pass input csv filename as parameters");
      return;
    }
    CryptoHelper c = new CryptoHelper();
    RedisBulkloadHelper.encrypt(c, args[0], args[1]);
  }
}