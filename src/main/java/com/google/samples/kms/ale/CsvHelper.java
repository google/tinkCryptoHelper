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

package com.google.samples.kms.ale;

import com.google.samples.kms.CryptoHelper;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class CsvHelper {

  public enum Headers {
    Key, Member
  }

  public static Iterable<CSVRecord> read(String filename) throws IOException {
    Reader in = new FileReader(filename);
    return CSVFormat.RFC4180.withHeader(Headers.class).withSkipHeaderRecord().parse(in);
  }

  public static void encrypt(CryptoHelper ctx, String inputFileName, String outputFileName)
      throws IOException, GeneralSecurityException {
    Reader in = new FileReader(inputFileName);
    Appendable writer = Files.newBufferedWriter(Paths.get(outputFileName));
    final CSVPrinter printer = CSVFormat.RFC4180.withHeader(Headers.class).print(writer);
    printer.printRecord("--tink-keyset--", ctx.getEncodedKeysetHandle());
    Iterable<CSVRecord> records = CSVFormat.RFC4180.withHeader(Headers.class).parse(in);
    for (CSVRecord record : records) {
      String key = record.get(Headers.Key);
      String member = record.get(Headers.Member);
      printer.printRecord(key, ctx.encryptToBytes(member));
    }
    printer.close(true);
  }

}