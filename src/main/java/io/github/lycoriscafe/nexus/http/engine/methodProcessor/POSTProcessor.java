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

package io.github.lycoriscafe.nexus.http.engine.methodProcessor;

import io.github.lycoriscafe.nexus.http.core.requestMethods.HttpRequestMethod;
import io.github.lycoriscafe.nexus.http.core.statusCodes.HttpStatusCode;
import io.github.lycoriscafe.nexus.http.engine.ReqResManager.httpReq.HttpRequest;
import io.github.lycoriscafe.nexus.http.engine.ReqResManager.httpRes.HttpResponse;
import io.github.lycoriscafe.nexus.http.engine.RequestHandler;
import io.github.lycoriscafe.nexus.http.helper.Database;
import io.github.lycoriscafe.nexus.http.helper.configuration.HttpServerConfiguration;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class POSTProcessor implements MethodProcessor {
    private final RequestHandler REQ_HANDLER;
    private final BufferedInputStream INPUT_STREAM;
    private final Connection DATABASE;
    private final HttpServerConfiguration CONFIG;

    public POSTProcessor(final RequestHandler REQ_HANDLER,
                         final BufferedInputStream INPUT_STREAM,
                         final Connection DATABASE,
                         final HttpServerConfiguration CONFIG) {
        this.REQ_HANDLER = REQ_HANDLER;
        this.INPUT_STREAM = INPUT_STREAM;
        this.DATABASE = DATABASE;
        this.CONFIG = CONFIG;
    }

    @Override
    public HttpResponse<?> process(HttpRequest<?> request) {
        HttpResponse<?> httpResponse = null;
        if (request.getHeaders().containsKey("content-length")) {
            int contentLen = Integer.parseInt(request.getHeaders().get("content-length").getFirst());
            if (contentLen > CONFIG.getMaxContentLength()) {
                REQ_HANDLER.processBadRequest(request.getREQUEST_ID(), HttpStatusCode.BAD_REQUEST);
                return null;
            }

            byte[] bytes = new byte[contentLen];
            try {
                INPUT_STREAM.read(bytes, 0, bytes.length);
            } catch (IOException e) {
                REQ_HANDLER.processBadRequest(request.getREQUEST_ID(), HttpStatusCode.INTERNAL_SERVER_ERROR);
                return null;
            }
            HttpRequest<byte[]> httpReq = request;
            httpReq.setContent(bytes);
        }
        try {
            List<String> details = Database.getEndpointDetails(DATABASE, HttpRequestMethod.POST, request.getRequestURL());
            if (details.get(0) == null) {
                REQ_HANDLER.processBadRequest(request.getREQUEST_ID(), HttpStatusCode.NOT_FOUND);
                return httpResponse;
            }
            Class<?> clazz = Class.forName(details.get(1));
            Method method = clazz.getMethod(details.get(2), HttpRequest.class);
            httpResponse = (HttpResponse<?>) method.invoke(null, request);
        } catch (SQLException | ClassNotFoundException | InvocationTargetException |
                 NoSuchMethodException | IllegalAccessException e) {
            REQ_HANDLER.processBadRequest(request.getREQUEST_ID(), HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
        return httpResponse;
    }
}
