// Copyright 2019 Google LLC

// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

//     https://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.samples.kms;

import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadKeyTemplates;

import java.security.GeneralSecurityException;

public class CryptoHelperEnvelope extends CryptoHelper {
  public CryptoHelperEnvelope() throws GeneralSecurityException {
    super();
  }

  @Override
  KeysetHandle getKeysetHandle() throws GeneralSecurityException {
    if (super.keysetHandle == null) {
      super.keysetHandle = KeysetHandle.generateNew(
          AeadKeyTemplates.createKmsEnvelopeAeadKeyTemplate(keyResourceIdUri, AeadKeyTemplates.AES256_GCM));
    }
    return keysetHandle;
  }
}
