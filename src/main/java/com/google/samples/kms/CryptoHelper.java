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

package com.google.samples.kms;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.KmsClient;
import com.google.crypto.tink.KmsClients;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.config.TinkConfig;
import com.google.crypto.tink.integration.gcpkms.GcpKmsClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class CryptoHelper {
  private static final Preferences preferences = initializePreferences();
  static final String keyResourceIdUri = preferences.get("keyResourceIdUri",
      "gcp-kms://projects/tink-test-infrastructure/locations/global/keyRings/unit-and-integration-testing/cryptoKeys/aead-key");
  static final String kmsCredentialsFilename = preferences.get("kmsCredentialsFilename",
      "kmsServiceAccountCredentials.json");
  static final String keysetFilename = preferences.get("keysetFilename", "keyset.json");
  static final String keysetFilenameClear = preferences.get("keysetFilenameClear", "keyset-clear.json");
  static final String charsetName = "UTF-8";
  static final String authenticationText = "CryptoHelper";

  private boolean writeKeyset = false;
  private boolean loadKeyset = false;

  private boolean writeClearKeyset = false;
  private byte[] authentication = getAuthenticaton();

  private Encoder encoder = Base64.getEncoder();
  private Decoder decoder = Base64.getDecoder();

  protected KeysetHandle keysetHandle;
  protected Aead aead;

  public CryptoHelper() throws GeneralSecurityException {
    TinkConfig.register();
  }

  private static Preferences initializePreferences() {
    final String name = CryptoHelper.class.getCanonicalName();
    Preferences preferences = Preferences.userNodeForPackage(CryptoHelper.class);
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

  public CryptoHelper loadKeyset(boolean flag) {
    loadKeyset = flag;
    return this;
  }

  public CryptoHelper writeClearKeyset(boolean flag) {
    writeClearKeyset = flag;
    return this;
  }

  public CryptoHelper writeKeyset(boolean flag) {
    writeKeyset = flag;
    return this;
  }

  public KmsClient getKmsClient() throws GeneralSecurityException {
    File credentials = new File(kmsCredentialsFilename);
    if (credentials.exists()) {
      return new GcpKmsClient().withCredentials(kmsCredentialsFilename);
    }
    return new GcpKmsClient();
  }

  protected KeysetHandle useExistingKeyset() throws IOException, GeneralSecurityException {
    if (loadKeyset) {
      File keyset = new File(keysetFilename);
      if (keyset.exists()) {
        return KeysetHandle.read(JsonKeysetReader.withFile(keyset), //
            getKmsClient().getAead(keyResourceIdUri));
      }
    }
    return null;
  }

  protected void write(KeysetHandle k) throws IOException, GeneralSecurityException {
    if (writeKeyset) {
      File keyset = new File(keysetFilename);
      k.write(JsonKeysetWriter.withFile(keyset), getKmsClient().getAead(keyResourceIdUri));
    }
    if (writeClearKeyset) {
      File keysetClear = new File(keysetFilenameClear);
      CleartextKeysetHandle.write(k, JsonKeysetWriter.withFile(keysetClear));
    }
  }

  public void write(OutputStream output) throws GeneralSecurityException, IOException {
    getAead();
    keysetHandle.write(JsonKeysetWriter.withOutputStream(output), //
        getKmsClient().getAead(keyResourceIdUri));
  }

  public void setEncodedKeysetHandle(String keysetBase64String) //
      throws GeneralSecurityException, IOException {
    byte[] k = decoder.decode(keysetBase64String);
    keysetHandle = KeysetHandle.read(JsonKeysetReader.withBytes(k), //
        getKmsClient().getAead(keyResourceIdUri));
  }

  public String getEncodedKeysetHandle() throws GeneralSecurityException, IOException {
    getAead();
    PipedOutputStream pos = new PipedOutputStream();
    BufferedOutputStream bos = new BufferedOutputStream(pos);
    PipedInputStream pis = new PipedInputStream(pos);
    keysetHandle.write(JsonKeysetWriter.withOutputStream(bos), //
        getKmsClient().getAead(keyResourceIdUri));
    bos.close();
    byte[] b = pis.readAllBytes();
    pis.close();
    return encoder.encodeToString(b);
  }

  private byte[] getAuthenticaton() {
    if (authentication == null) {
      try {
        authentication = authenticationText.getBytes(charsetName);
      } catch (Exception e) {
        return null;
      }
    }
    return authentication;
  }

  public String encrypt(String text) //
      throws GeneralSecurityException, IOException, NullPointerException {
    Aead a = getAead();
    byte[] data = text.getBytes(charsetName);
    byte[] ciphertext = a.encrypt(data, authentication);
    return encoder.encodeToString(ciphertext);
  }

  public byte[] encryptToBytes(String plaintext) //
      throws GeneralSecurityException, IOException, NullPointerException {
    return getAead().encrypt(plaintext.getBytes(charsetName), authentication);
  }

  public String decrypt(byte[] ciphertext) //
      throws NullPointerException, GeneralSecurityException, IOException {
    byte[] clearText = getAead().decrypt(ciphertext, authentication);
    return new String(clearText, charsetName);
  }

  public String decrypt(String ciphertext)
      throws UnsupportedEncodingException, GeneralSecurityException, NullPointerException, IOException {
    byte[] c = decoder.decode(ciphertext);
    byte[] clearText = getAead().decrypt(c, authentication);
    return new String(clearText, charsetName);
  }

  protected KeysetHandle getKeysetHandle() throws GeneralSecurityException {
    if (keysetHandle == null) {
      keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.createAesGcmKeyTemplate(256 / 8));
    }
    return keysetHandle;
  }

  public Aead getAead() throws java.security.GeneralSecurityException, IOException, NullPointerException {
    if (aead == null) {
      KeysetHandle k = useExistingKeyset();
      if (k == null) {
        k = getKeysetHandle();
        write(k);
      }
      KmsClients.add(getKmsClient());
      aead = k.getPrimitive(Aead.class);
      keysetHandle = k;
    }
    return aead;
  }
}
