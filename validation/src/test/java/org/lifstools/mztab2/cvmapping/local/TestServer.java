/*
 * Copyright 2026 LIFS Tools Consortium.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lifstools.mztab2.cvmapping.local;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A local HTTP server for the tests of this package: routes by path, counts
 * the requests and keeps their headers. Unrouted paths answer 404.
 */
final class TestServer implements AutoCloseable {

    interface Route {
        void handle(HttpExchange exchange) throws IOException;
    }

    interface Page {
        String json(int page);
    }

    private final HttpServer http;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<String, Route> routes = new ConcurrentHashMap<>();
    private final Map<String, List<HttpExchange>> requests = new ConcurrentHashMap<>();
    private final Map<String, List<String>> authorizations = new ConcurrentHashMap<>();
    private final Map<String, List<String>> userAgents = new ConcurrentHashMap<>();
    private final AtomicInteger total = new AtomicInteger();

    TestServer() throws IOException {
        http = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        http.createContext("/", x -> {
            String path = x.getRequestURI().getPath();
            total.incrementAndGet();
            requests.computeIfAbsent(path, p -> new CopyOnWriteArrayList<>()).add(x);
            authorizations.computeIfAbsent(path, p -> new CopyOnWriteArrayList<>());
            String auth = x.getRequestHeaders().getFirst("Authorization");
            if (auth != null) {
                authorizations.get(path).add(auth);
            }
            userAgents.computeIfAbsent(path, p -> new CopyOnWriteArrayList<>())
                .add(String.valueOf(x.getRequestHeaders().getFirst("User-Agent")));
            Route route = routes.get(path);
            try {
                if (route == null) {
                    send(x, 404, "not found".getBytes(StandardCharsets.UTF_8));
                } else {
                    route.handle(x);
                }
            } finally {
                x.close();
            }
        });
        http.setExecutor(executor);
        http.start();
    }

    String base() {
        return "http://127.0.0.1:" + http.getAddress().getPort();
    }

    URI api() {
        return URI.create(base() + "/api");
    }

    void route(String path, Route route) {
        routes.put(path, route);
    }

    void redirect(String path, String location, int status) {
        route(path, x -> {
            x.getResponseHeaders().add("Location", location);
            x.sendResponseHeaders(status, -1);
        });
    }

    void file(String path, byte[] body) {
        route(path, x -> send(x, 200, body));
    }

    void json(String path, Page pages) {
        route(path, x -> {
            String query = String.valueOf(x.getRequestURI().getQuery());
            int page = 1;
            for (String part : query.split("&")) {
                if (part.startsWith("page=")) {
                    page = Integer.parseInt(part.substring(5));
                }
            }
            x.getResponseHeaders().add("Content-Type", "application/json");
            send(x, 200, pages.json(page).getBytes(StandardCharsets.UTF_8));
        });
    }

    int hits(String path) {
        return requests.getOrDefault(path, List.of()).size();
    }

    int totalHits() {
        return total.get();
    }

    List<String> authorizations(String path) {
        return authorizations.getOrDefault(path, List.of());
    }

    List<String> userAgents(String path) {
        return userAgents.getOrDefault(path, List.of());
    }

    @Override
    public void close() {
        http.stop(0);
        executor.shutdownNow();
    }

    /** Answers with a status and a body. */
    static void send(HttpExchange x, int status, byte[] body) throws IOException {
        x.sendResponseHeaders(status, body.length);
        try (OutputStream out = x.getResponseBody()) {
            out.write(body);
        }
    }
}
