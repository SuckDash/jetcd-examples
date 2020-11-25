/*
 * Copyright 2016-2020 The jetcd authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etcd.jetcd.examples.ctl;

import com.beust.jcommander.Parameter;
import com.google.protobuf.ByteString;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.grpc.netty.GrpcSslContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException, SSLException {

        String endpoints = "http://127.0.0.1:2379";
        File caFile = new File("src/main/resources/ca.crt");
        File certFile = new File("src/main/resources/server.crt");
        File keyFile = new File("src/main/resources/server.key");

        //创建带有证书的etcd client
        System.out.println("client will creat");

        //创建获取key value所需要的参数
        ByteSequence key = ByteSequence.from(ByteString.copyFromUtf8("\0"));
        GetOption option = GetOption.newBuilder()
                .withSortField(GetOption.SortTarget.KEY)
                .withSortOrder(GetOption.SortOrder.DESCEND)
                .withRange(key)
                .build();

        //创建连接etcd客户端
        Client client = Client.builder()
                .endpoints(endpoints)
                .sslContext(GrpcSslContexts.forClient()
                        .trustManager(caFile)
                        .keyManager(certFile, keyFile)
                        .build())
                .build();

        //获取key并循环输出
        CompletableFuture<GetResponse> getResponseCompletableFuture = client.getKVClient().get(key, option);
        GetResponse getResponse = getResponseCompletableFuture.get();
        List<KeyValue> kvs = getResponse.getKvs();
        for (KeyValue kv : kvs) {
            System.out.println("key=" + kv.getKey().toString());
            System.out.println("value=" + kv.getValue().toString());
        }
        LOGGER.info("client created");
        System.out.println("client created");
    }


    public static class Args {
        @Parameter(names = { "--endpoints" }, description = "gRPC endpoints ")
        private String endpoints = "http://127.0.0.1:2379";

        @Parameter(names = { "-h", "--help" }, help = true)
        private boolean help = false;
    }
}
