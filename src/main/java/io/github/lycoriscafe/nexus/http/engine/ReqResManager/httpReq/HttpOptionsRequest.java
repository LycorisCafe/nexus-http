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

package io.github.lycoriscafe.nexus.http.engine.ReqResManager.httpReq;

import io.github.lycoriscafe.nexus.http.core.requestMethods.HttpRequestMethod;
import io.github.lycoriscafe.nexus.http.engine.RequestConsumer;

import java.util.HashSet;

public final class HttpOptionsRequest extends HttpGetRequest {
    private HttpRequestMethod accessControlRequestMethod;
    private HashSet<String> accessControlRequestHeaders;

    public HttpOptionsRequest(final RequestConsumer requestConsumer,
                              final long requestId,
                              final HttpRequestMethod requestMethod) {
        super(requestConsumer, requestId, requestMethod);
    }

    public void setAccessControlRequestMethod(final HttpRequestMethod accessControlRequestMethod) {
        this.accessControlRequestMethod = accessControlRequestMethod;
    }

    public void setAccessControlRequestHeader(final String accessControlRequestHeader) {
        if (accessControlRequestHeaders == null) {
            accessControlRequestHeaders = new HashSet<>();
        }
        accessControlRequestHeaders.add(accessControlRequestHeader);
    }

    public HttpRequestMethod getAccessControlRequestMethod() {
        return accessControlRequestMethod;
    }

    public HashSet<String> getAccessControlRequestHeaders() {
        return accessControlRequestHeaders;
    }
}
