/*
 * Copyright 2024 Lycoris Cafe
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

package main.test;

import io.github.lycoriscafe.nexus.http.HttpServer;
import io.github.lycoriscafe.nexus.http.helper.configuration.HttpServerConfiguration;
import io.github.lycoriscafe.nexus.http.helper.configuration.ThreadType;

import java.io.IOException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            HttpServerConfiguration httpServerConfiguration = new HttpServerConfiguration(Main.class)
                    .setPort(2004)
                    .setThreadType(ThreadType.VIRTUAL)
                    .setBacklog(5)
                    .setDatabaseLocation("");
            HttpServer httpServer1 = new HttpServer(httpServerConfiguration);
            httpServer1.start();
//            System.out.println(httpServerConfiguration.getBasePackage());
            //            HttpServer httpServer2 = new HttpServer(2004, ThreadType.VIRTUAL, MemoryType.PRIMARY, 5);
        } catch (
                IOException | SQLException |
                ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
