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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;

/**
 * Ruling R-AJ: a redirect from https to http is upgraded to https on the same
 * host, path and query, and refused if that does not answer. The test server
 * has no TLS, so the downloader under test sends every https request as http
 * to the server (its {@code wire} hook) while it decides on the https names.
 */
public class DownloaderRedirectTest {

    @Test
    public void anUpgradeKeepsHostPathAndQuery() {
        assertEquals(URI.create("https://ftp.ebi.ac.uk/pub/a%20b/c.gz?x=1&y=%2F"),
            HttpDownloader.upgrade(URI.create("http://ftp.ebi.ac.uk/pub/a%20b/c.gz?x=1&y=%2F")));
        assertEquals(URI.create("https://ftp.ebi.ac.uk/pub/c.gz"),
            HttpDownloader.upgrade(URI.create("http://ftp.ebi.ac.uk:80/pub/c.gz")));
        assertEquals(URI.create("https://host:8080/p"), HttpDownloader.upgrade(URI.create("http://host:8080/p")));
        assertEquals(URI.create("https://host"), HttpDownloader.upgrade(URI.create("http://host")));
    }

    @Test
    public void onlyAnHttpsToHttpRedirectIsADowngrade() {
        assertTrue(HttpDownloader.isDowngrade(URI.create("https://a/x"), URI.create("http://a/y")));
        assertFalse(HttpDownloader.isDowngrade(URI.create("http://a/x"), URI.create("http://a/y")));
        assertFalse(HttpDownloader.isDowngrade(URI.create("https://a/x"), URI.create("https://a/y")));
        assertFalse(HttpDownloader.isDowngrade(URI.create("http://a/x"), URI.create("https://a/y")));
    }

    @Test
    public void aFileOrARedirectAnswersAnErrorDoesNot() {
        assertTrue(HttpDownloader.answered(200));
        assertTrue(HttpDownloader.answered(206));
        assertTrue(HttpDownloader.answered(302));
        assertFalse(HttpDownloader.answered(404));
        assertFalse(HttpDownloader.answered(500));
        assertFalse(HttpDownloader.answered(403));
    }

    @Test
    public void aDowngradeIsUpgradedAndFollowedIfTheHttpsUrlAnswers() throws Exception {
        try (TestServer server = new TestServer()) {
            List<URI> sent = new CopyOnWriteArrayList<>();
            HttpDownloader downloader = downloader(server, sent);
            server.redirect("/start", server.base() + "/real?q=1", 302);
            server.file("/real", "ok".getBytes(StandardCharsets.UTF_8));

            try (InputStream in = downloader.get(URI.create(secure(server) + "/start"))) {
                assertEquals("ok", new String(in.readAllBytes(), StandardCharsets.UTF_8));
                assertEquals(URI.create(secure(server) + "/real?q=1"), ((Downloader.Download) in).location());
            }
            assertEquals(1, server.hits("/real"));
            assertEquals(List.of(URI.create(secure(server) + "/start"), URI.create(secure(server) + "/real?q=1")),
                sent, "nothing is ever requested over plain http");
        }
    }

    @Test
    public void aDowngradeIsRefusedIfTheHttpsUrlDoesNotAnswer() throws Exception {
        try (TestServer server = new TestServer()) {
            List<URI> sent = new CopyOnWriteArrayList<>();
            HttpDownloader downloader = downloader(server, sent);
            server.redirect("/start", server.base() + "/gone", 302);
            // /gone is not routed: the https URL answers 404

            IOException e = assertThrows(IOException.class,
                () -> downloader.get(URI.create(secure(server) + "/start")));

            assertEquals("redirect from " + secure(server) + "/start to " + server.base()
                + "/gone refused: https to http", e.getMessage());
            assertFalse(e instanceof Downloader.NotFoundException);
            assertTrue(sent.stream().allMatch(u -> "https".equals(u.getScheme())), sent.toString());
        }
    }

    @Test
    public void aDowngradeIsRefusedIfTheHttpsUrlCannotBeReached() throws Exception {
        try (TestServer server = new TestServer()) {
            List<URI> sent = new CopyOnWriteArrayList<>();
            HttpDownloader downloader = downloader(server, sent);
            // port 1 refuses the connection
            server.redirect("/start", "http://127.0.0.1:1/real", 302);

            IOException e = assertThrows(IOException.class,
                () -> downloader.get(URI.create(secure(server) + "/start")));

            assertTrue(e.getMessage().endsWith("refused: https to http"), e.getMessage());
        }
    }

    private static String secure(TestServer server) {
        return "https" + server.base().substring("http".length());
    }

    private static HttpDownloader downloader(TestServer server, List<URI> sent) {
        return new HttpDownloader(Duration.ofSeconds(5), uri -> {
            sent.add(uri);
            return URI.create(uri.toString().replaceFirst("^https", "http"));
        });
    }
}
