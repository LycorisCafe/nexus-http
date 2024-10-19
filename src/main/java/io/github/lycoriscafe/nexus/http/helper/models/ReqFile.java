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

package io.github.lycoriscafe.nexus.http.helper.models;

import io.github.lycoriscafe.nexus.http.core.requestMethods.HttpRequestMethod;

public final class ReqFile extends ReqMaster {
    private final String location;
    private final String lastModified;
    private final String eTag;

    public ReqFile(final String requestEndpoint,
                   final String location,
                   final String lastModified,
                   final String eTag) {
        super("/", requestEndpoint, HttpRequestMethod.GET);
        this.location = location;
        this.lastModified = lastModified;
        this.eTag = eTag;
    }

    public String getLocation() {
        return location;
    }

    public String getLastModified() {
        return lastModified;
    }

    public String getETag() {
        return eTag;
    }
}