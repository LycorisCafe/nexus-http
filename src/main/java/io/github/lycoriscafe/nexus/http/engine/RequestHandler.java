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

package io.github.lycoriscafe.nexus.http.engine;

import io.github.lycoriscafe.nexus.http.core.requestMethods.HttpRequestMethod;
import io.github.lycoriscafe.nexus.http.core.statusCodes.HttpStatusCode;
import io.github.lycoriscafe.nexus.http.engine.ReqResManager.httpRes.HttpResponse;
import io.github.lycoriscafe.nexus.http.helper.HTTPServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class RequestHandler implements Runnable {
    Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    final List<HttpResponse<?>> RESPONSES = new ArrayList<>();

    private long requestId = 0L;
    private long responseId = 0L;

    private final Socket SOCKET;
    private final BufferedInputStream INPUT_STREAM;
    private final BufferedOutputStream OUTPUT_STREAM;
    private final RequestProcessor PROCESSOR;

    public RequestHandler(final HTTPServerConfiguration CONFIGURATION,
                          final Socket SOCKET,
                          final Connection DATABASE) throws IOException {
        this.SOCKET = SOCKET;
        this.INPUT_STREAM = new BufferedInputStream(SOCKET.getInputStream());
        this.OUTPUT_STREAM = new BufferedOutputStream(SOCKET.getOutputStream());
        PROCESSOR = new RequestProcessor(this, INPUT_STREAM, CONFIGURATION, DATABASE);
    }

    private void incrementRequestId() {
        requestId = (requestId == Long.MAX_VALUE ? 0 : requestId + 1);
    }

    private void incrementResponseId() {
        responseId = (responseId == Long.MAX_VALUE ? 0 : responseId + 1);
    }

    public void addToSendQue(final HttpResponse<?> httpResponse) {
        RESPONSES.add(httpResponse);
        send();
    }

    private ArrayList<Object> validateRequestLine(final ArrayList<Object> requestLine,
                                                  final String request) {
        try {
            HTTPVersion httpVersion;
            String[] parts = request.split(" ");
            if (parts.length != 3 ||
                    !HttpRequestMethod.validate(parts[0]) ||
                    (httpVersion = HTTPVersion.validate(parts[2])) == null) {
                return null;
            }

            requestLine.add(HttpRequestMethod.valueOf(parts[0]));
            requestLine.add(parts[1]);
            requestLine.add(httpVersion);
            return requestLine;
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, List<String>> processHeader(final Map<String, List<String>> headers,
                                                    final String headerLine) {
        List<String> values = new ArrayList<>();
        try {
            String[] parts = headerLine.splitWithDelimiters(":", 2);
            for (String value : parts[2].split(",")) {
                if (value.charAt(0) == ' ') {
                    value = value.replaceFirst(" ", "");
                }
                values.add(value);
            }
            headers.put(parts[0].toLowerCase(Locale.ROOT), values);
            return headers;
        } catch (Exception e) {
            return null;
        }
    }

    public void processBadRequest(final long REQUEST_ID,
                                  final HttpStatusCode STATUS) {
        HttpResponse<String> response = new HttpResponse<>(REQUEST_ID);
        response.setVersion(HTTPVersion.HTTP_1_1);
        response.setStatusCode(STATUS);
        addDefaultHeaders(response);
        response.setHeaders(Map.of("Content-length", List.of("0")));
        response.formatProtocol();
        addToSendQue(response);
    }

    public static void addDefaultHeaders(final HttpResponse<?> RESPONSE) {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Server", List.of("LycorisCafe/NexusHTTP(v1.0.0)"));
        headers.put("Date", List.of(getServerTime()));
        RESPONSE.setHeaders(headers);
    }

    private static String getServerTime() {
        return DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
                .withZone(ZoneId.of("GMT")).format(ZonedDateTime.now());
    }

    @Override
    public void run() {
        ArrayList<Object> requestLine = new ArrayList<>();
        Map<String, List<String>> headers = new HashMap<>();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int terminator = 0;
        int character;

        headersLoop:
        while (true) {
            try {
                character = INPUT_STREAM.read();
                switch (character) {
                    case -1 -> {
                        break headersLoop;
                    }
                    case '\r' -> {
                        INPUT_STREAM.skipNBytes(1);

                        terminator++;
                        if (terminator == 2) {
                            PROCESSOR.processRequest(requestId, requestLine, headers);
                            incrementRequestId();
                            requestLine.clear();
                            headers.clear();
                            terminator = 0;
                            continue;
                        }

                        if (requestLine.isEmpty()) {
                            requestLine = validateRequestLine(requestLine, buffer.toString(StandardCharsets.UTF_8));
                            if (requestLine == null) {
                                processBadRequest(requestId, HttpStatusCode.BAD_REQUEST);
                                break headersLoop;
                            }
                            buffer.reset();
                            continue;
                        }

                        headers = processHeader(headers, buffer.toString(StandardCharsets.UTF_8));
                        if (headers == null) {
                            processBadRequest(requestId, HttpStatusCode.BAD_REQUEST);
                            break headersLoop;
                        }
                        buffer.reset();
                    }
                    default -> {
                        buffer.write(character);
                        terminator = 0;
                    }
                }
            } catch (IOException e) {
                break;
                // TODO handle socket io exception
            }
        }
    }

    private synchronized void send() {
        for (HttpResponse<?> httpResponse : RESPONSES) {
            if (httpResponse.getRESPONSE_ID() == responseId) {
                try {
                    OUTPUT_STREAM.write(httpResponse.getFormattedProtocol().getBytes(StandardCharsets.UTF_8));
                    if (httpResponse.getContent() instanceof byte[] b) {
                        OUTPUT_STREAM.write(b);
                    }
                    if (httpResponse.getContent() instanceof String s) {
                        OUTPUT_STREAM.write(s.getBytes(StandardCharsets.UTF_8));
                    }
                    if (httpResponse.getContent() instanceof File f) {
                        byte[] buffer = new byte[1024];
                        BufferedInputStream bIn = new BufferedInputStream(new FileInputStream(f));
                        while ((bIn.read(buffer)) != -1) {
                            OUTPUT_STREAM.write(buffer);
                        }
                    }
                    OUTPUT_STREAM.flush();
                } catch (IOException e) {
                    // TODO handle exception
                }
                incrementResponseId();
            }
        }
    }
}
