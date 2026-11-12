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

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * A vocabulary release in the {@link VocabularyCache}, or an OWL import read
 * with one.
 *
 * <p>
 * A release's {@code imports} are every {@code owl:imports} read with it,
 * transitively and each once, in the order they were read. An import is a
 * {@code CachedRelease} too: its {@code index} is null, because its terms are
 * in the index of the release that imports it, and its own {@code imports} is
 * empty, because they are listed with that release.</p>
 *
 * @param key the resources entry's key (MS); for an import, the OBO ontology
 * id its IRI names (obi), else a slug of the IRI's host and path
 * @param version the release name (4.1.200); for an import, the version its
 * IRI or its {@code owl:versionIRI} names, else {@code unversioned}
 * @param source the URL the file was requested from: the location of the
 * release, or the import's IRI
 * @param sha256 the SHA-256 of the file as downloaded, lower-case hex
 * @param original the file as downloaded (compressed if it came so)
 * @param index the term index of the release and its imports; null for an
 * import
 * @param imports the imports read with the release
 */
public record CachedRelease(String key, String version, URI source, String sha256, Path original, Path index,
    List<CachedRelease> imports) {

    /** Requires every field but the index, and copies the imports. */
    public CachedRelease {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(sha256, "sha256");
        Objects.requireNonNull(original, "original");
        imports = List.copyOf(imports);
    }
}
