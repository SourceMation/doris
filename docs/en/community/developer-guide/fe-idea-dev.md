---
{
    "title": "Setting Up dev env for FE - IntelliJ IDEA",
    "language": "en"
}
---

<!-- 
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

# Setting Up Development Environment for FE using IntelliJ IDEA

## 1. Environmental Preparation

* Git
* JDK1.8+
* IntelliJ IDEA
* Maven (Optional, IDEA shipped embedded Maven3)

1. Git clone codebase from https://github.com/apache/doris.git


2. Use IntelliJ IDEA to open the code root directory

3. If you only develop fe without compiling thirdparty, you need to install thrift, and copy or connect thrift to the `thirdparty/installed/bin` directory

    Install `thrift 0.16.0` (Note: `Doris` 0.15 - 1.2 builds on `thrift 0.13.0`, the latest code is built using `thrift 0.16.0`)

    **The following examples use 0.16.0 as an example. If you need 0.13.0, please change 0.16.0 in the example below to 0.13.0. **
    
    - Windows: 

        1. Download: `http://archive.apache.org/dist/thrift/0.16.0/thrift-0.16.0.exe`
        2. Copy: Copy the file to `./thirdparty/installed/bin`
    
    - MacOS: 

        1. `brew tap-new $USER/local-tap`
        2. `brew extract --version='0.16.0' thrift $USER/local-tap`
        3. `brew install thrift@0.16.0`

        If there is an error related to downloading, you can modify the following files:

        `/usr/local/Homebrew/Library/Taps/$USER/homebrew-local-tap/Formula/thrift\@0.16.0.rb`

        Modify:

        `url "https://www.apache.org/dyn/closer.lua?path=thrift/0.16.0/thrift-0.16.0.tar.gz"`

        To:

        `url "https://archive.apache.org/dist/thrift/0.16.0/thrift-0.16.0.tar.gz"`

        Reference: `https://gist.github.com/tonydeng/02e571f273d6cce4230dc8d5f394493c`
    
    - Linux:

        1. Download source: `wget https://archive.apache.org/dist/thrift/0.16.0/thrift-0.16.0.tar.gz`
        2. Install dependencies: `yum install -y autoconf automake libtool cmake ncurses-devel openssl-devel lzo-devel zlib-devel gcc gcc-c++`
        3. `tar zxvf thrift-0.16.0.tar.gz`
        4. `cd thrift-0.16.0`
        5. `./configure --without-tests`
        6. `make`
        7. `make install`

        Validate thrift version: `thrift --version`  

        > Note: If you have compiled Doris, you do not need to install thrift, you can directly use $DORIS_HOME/thirdparty/installed/bin/thrift

4. If it is a Mac or Linux environment, the code can be automatically generated by the following command:

    ```
    sh generated-source.sh
    ```

    If version before 1.2, using:
    
    ```
    cd fe
    mvn generate-sources
    ```

    Or:

    ```
    cd fe && mvn clean install -DskipTests
    ```

Or run the maven command through the graphical interface to generate:

![](/images/gen_code.png)

If you are developing on the OS which lack of support to run `shell script` and `make` such as Windows, a workround here 
is generate codes in Linux and copy them back. Using Docker should also be an option.

5. If a help document has not been generated, go to the docs directory and run`sh build_help_zip.sh`，

   Then copy help-resource.zip from build to fe/fe-core/target/classes

## 2. Debug

1. Import `./fe` into IDEA

2. Follow the picture to create the folders (The directory may exist in the new version. If it exists, skip it, otherwise create it.)

![](/images/DEBUG4.png)

3. Build `ui` project , and copy files from directory `ui/dist` into directory `webroot` ( you can skip this step , if you don't need `Doris` UI )

## 3. Custom FE configuration

Copy below content into `conf/fe.conf` and tune it to fit your environment(Note: If developed using`Mac`, since`docker for Mac`does not support`Host`mode,`be`needs to be exposed using`-p` and `fe.conf` `priority_networks` configured to be accessible within the container, such as WIFI Ip).

```
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

#####################################################################
## The uppercase properties are read and exported by bin/start_fe.sh.
## To see all Frontend configurations,
## see fe/src/org/apache/doris/common/Config.java
#####################################################################

# the output dir of stderr and stdout 
LOG_DIR = ${DORIS_HOME}/log

DATE = `date +%Y%m%d-%H%M%S`
JAVA_OPTS="-Xmx2048m -XX:+UseMembar -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=7 -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+CMSClassUnloadingEnabled -XX:-CMSParallelRemarkEnabled -XX:CMSInitiatingOccupancyFraction=80 -XX:SoftRefLRUPolicyMSPerMB=0 -Xloggc:$DORIS_HOME/log/fe.gc.log.$DATE"

# For jdk 9+, this JAVA_OPTS will be used as default JVM options
JAVA_OPTS_FOR_JDK_9="-Xmx4096m -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=7 -XX:+CMSClassUnloadingEnabled -XX:-CMSParallelRemarkEnabled -XX:CMSInitiatingOccupancyFraction=80 -XX:SoftRefLRUPolicyMSPerMB=0 -Xlog:gc*:$DORIS_HOME/log/fe.gc.log.$DATE:time"

##
## the lowercase properties are read by main program.
##

# INFO, WARN, ERROR, FATAL
sys_log_level = INFO

# store metadata, create it if it is not exist.
# Default value is ${DORIS_HOME}/doris-meta
# meta_dir = ${DORIS_HOME}/doris-meta

http_port = 8030
rpc_port = 9020
query_port = 9030
edit_log_port = 9010
mysql_service_nio_enabled = true

# Choose one if there are more than one ip except loopback address. 
# Note that there should at most one ip match this list.
# If no ip match this rule, will choose one randomly.
# use CIDR format, e.g. 10.10.10.0/24
# Default value is empty.
# priority_networks = 10.10.10.0/24;192.168.0.0/16

# Advanced configurations 
# log_roll_size_mb = 1024
# sys_log_dir = ${DORIS_HOME}/log
# sys_log_roll_num = 10
# sys_log_verbose_modules = 
# audit_log_dir = ${DORIS_HOME}/log
# audit_log_modules = slow_query, query
# audit_log_roll_num = 10
# meta_delay_toleration_second = 10
# qe_max_connection = 1024
# max_conn_per_user = 100
# qe_query_timeout_second = 300
# qe_slow_log_ms = 5000

```

## 4. Setting Environment Variables

Follow the picture to set runtime Environment Variables in IDEA

![](/images/DEBUG5.png)

## 5. Config options

Because part of the dependency is `provided`, idea needs to do a special config. Click on the right `Modify Options` in the `Run/Debug Configurations` setting. Check the `Add Dependencies with "Provided" scope to classpath` option.

![](/images/idea_options.png)

## 6. Start FE

Having fun with Doris FE!