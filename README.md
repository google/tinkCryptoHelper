
    Copyright 2019 Google LLC

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

# CryptoHelper

## Index

1. [Introduction](#introduction)
2. [Getting started](#getting-started)
3. [Learn more](#learn-more)

## Introduction

This repository contains the Java source code for cryptoHelper, a sample on how to implement application level encryption using Tink. The sample contains code to use the cryptoHelper for Redis and to encrypt CSV files. The Google Cloud platforms provides the key material with the Google Cloud KMS service. This example can be easily extended for other storage systems.

## Getting Started

* Clone the repository
* Install Maven, this code was tested with Maven 3.6.2
* Install OpenJDK, this code was tested with OpenJDK 13.0.1
* Build sample  ```$ mvn package```
* Running tests with a real Redis server needs a few preferences to be set. Please lookup up how to set Java preferences on your operating system.

### Preferences in class AppTest:

  * com.google.samples.kms.ale.AppTest
    * Default: true
    Will be written on class initialization. This will help to locate the preferences for this class
  * redisIsOnline
    * Default: false
    * Values: true/false
    * if online, Redis will be tested
  * redisBatchSize:
    * Default: 5000
    * Values: number of records to batch
    This many records will be written between syncs
  * redisHost4_0
    * Default: 127.0.0.1
    * Values: Ip address of Redis 4.0 instance
  * redisHost3_2
    * Default: 127.0.0.1
    * Values: Ip address of Redis 3.2 instance
  * keysetFilename
    * Default: keyset.json
    * Value: Path to where keyset file will be written (only for debugging purposes)

### Preferences used by the CryptoHelper Class:

  * com.google.samples.kms.CryptoHelper
    * Default: true
    Will be written on class initialization. This will help to locate the preferences for this class
  * keyResourceIdUri
    * Default: key for unit and integration testing
    * Values: the resource name of the key to be used for the cryptoHelper in the following format:
    "gcp-kms://projects/<ProjectName>/locations/<Location>/keyRings/<KeyRing>/cryptoKeys/<KeyName>"
  * kmsCredentialsFilename
    * Default: kmsServiceAccountCredentials.json
    * Value: Path to the downloaded json file containing the service account key credentials
  * keysetFilename
    * Default: keyset.json
    * Value: Path to where keyset file will be written (only for debugging purposes)
  * keysetFilenameClear
    * Default: keysetFilenameClear
    * Value: ath to where the clear-text keyset file will be written (only for debugging purposes)

This project uses the Apache license, as is Google's default.

## Learn more

Also please look at [Tink @ github](https://github.com/google/tink) that has been used in this sample. Tink is a multi-language,
cross-platform library that provides cryptographic APIs that are secure, easy to use correctly, and hard(er) to misuse.

This is not an offically supported Google product!