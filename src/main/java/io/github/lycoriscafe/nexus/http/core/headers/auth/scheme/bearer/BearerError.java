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

package io.github.lycoriscafe.nexus.http.core.headers.auth.scheme.bearer;

/**
 * Possible errors for 'Brear' authentication request.
 *
 * @see BearerAuthentication
 * @see io.github.lycoriscafe.nexus.http.core.headers.auth.Authentication Authentication
 * @see <a href="https://datatracker.ietf.org/doc/rfc6750">The OAuth 2.0 Authorization Framework: Bearer Token Usage (rfc6750)</a>
 * @since v1.0.0
 */
public enum BearerError {
    /**
     * The request is missing a required parameter, includes an unsupported parameter or parameter value, repeats the same parameter, uses more than
     * one method for including an access token, or is otherwise malformed. The resource server SHOULD respond with the HTTP 400 (Bad Request) status
     * code.
     */
    INVALID_REQUEST("invalid_request"),
    /**
     * The access token provided is expired, revoked, malformed, or invalid for other reasons. The resource SHOULD respond with the HTTP 401
     * (Unauthorized) status code. The client MAY request a new access token and retry the protected resource request.
     */
    INVALID_TOKEN("invalid_token"),
    /**
     * The request requires higher privileges than provided by the access token. The resource server SHOULD respond with the HTTP 403 (Forbidden)
     * status code and MAY include the "scope" attribute with the scope necessary to access the protected resource.
     */
    INSUFFICIENT_SCOPE("insufficient_scope");

    private final String value;

    BearerError(String value) {
        this.value = value;
    }

    /**
     * Get the target 'BearerError' value to set in header.
     *
     * @return 'BearerError' value to set in header
     * @see BearerError
     * @since v1.0.0
     */
    public String getValue() {
        return value;
    }
}
