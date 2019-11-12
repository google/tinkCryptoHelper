# cryptoHelper

This repository contains the Java soures for cryptoHelper, a sample on how to implement application level encryption. The sample contains code to use the cryptoHelper for Redis and to encrypt CSV files. The Google Cloud platforms provides the key material with the KMS service.

## How to use this sample
* Clone the repository
* Install Maven
* Install OpenJDK, this code was tested with OpenJDK 13.0.1
* Build sample
    $ mv package
* Running test test with a real Redis server, needs a few prefernces to be set. Please lookup how to set preferences for your operating system. This is the list of used preferences in class AppTest:
  * redisIsOnline
    * Default: false
    * Values: true/false
    * if online, Redis will be tested
  * redisBatchSize:
    * Default: 5000
    * Values: number of records to batch
    This many records will be writtn between syncs
  * redisHost4_0
    * Default: 127.0.0.1
    * Values: Ip address of Redis 4.0 instance
  * redisHost3_2
    * Default: 127.0.0.1
    * Values: Ip address of Redi 3.2 instance
  * keysetFilename
    * Default:

This template uses the Apache license, as is Google's default.  See the
documentation for instructions on using alternate license.

## How to use this template

1. Check it out from GitHub.
    * There is no reason to fork it.
1. Create a new local repository and copy the files from this repo into it.
1. Modify README.md and CONTRIBUTING.md to represent your project, not the
   template project.
1. Develop your new project!

``` shell
git clone https://github.com/google/new-project
mkdir my-new-thing
cd my-new-thing
git init
cp -r ../new-project/* .
git add *
git commit -a -m 'Boilerplate for new Google open source project'
```

## Source Code Headers

Every file containing source code must include copyright and license
information. This includes any JS/CSS files that you might be serving out to
browsers. (This is to help well-intentioned people avoid accidental copying that
doesn't comply with the license.)

Apache header:

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
