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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The term indexes of a process: one {@link TermIndex} per index file, shared
 * by every validation run and thread (Ruling P2).
 *
 * <p>
 * The store owns the indexes it hands out (Ruling R-AI). A cached release
 * never changes, so its index stays open for the life of the process, and no
 * caller can close it: {@link TermIndex} has no {@code close()}, since
 * closing a shared index would end it for every run and thread.</p>
 */
public final class TermIndexStore {

    private static final Map<Path, TermIndex> OPEN = new ConcurrentHashMap<>();

    private TermIndexStore() {
    }

    /**
     * The index of a release, opened on first use.
     *
     * @param release a release of the {@link VocabularyCache}
     * @return the shared index, owned by the store
     * @throws IOException if the index cannot be read
     * @throws IllegalArgumentException for an import, which has no index of
     * its own
     */
    public static TermIndex open(CachedRelease release) throws IOException {
        if (release.index() == null) {
            throw new IllegalArgumentException(release.key() + " " + release.version()
                + " has no index of its own: its terms are in the index of the release that imports it");
        }
        Path file = release.index().toAbsolutePath().normalize();
        TermIndex index = OPEN.get(file);
        if (index != null) {
            return index;
        }
        try {
            return OPEN.computeIfAbsent(file, f -> {
                try {
                    return TermIndex.open(f);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }
}
