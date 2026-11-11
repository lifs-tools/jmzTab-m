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

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * A vocabulary a validation run looked up, and the version it was checked
 * against: the per-vocabulary part of design §4.7's attestation.
 *
 * @param label the cv label: the file's {@code cv[n]-label}, or the
 * parameter's label if the file declares none
 * @param declaredOriginal the file's {@code cv[n]-version} as written; null
 * if the file has no cv entry for the label
 * @param declaredNormalised the release the declared version stands for
 * (design §4.4), if any; empty when the source is OLS, which lists no
 * releases
 * @param mappingVersion the version the mapping's rules were written for
 * @param versionUsed the concrete version the terms were checked against;
 * never null nor "latest"
 * @param reason why that version: FILE, FILE_DATE, MAPPING, LATEST,
 * SUBSTITUTE_NEWER, SUBSTITUTE_LATEST (forced substitutes from the cache) or
 * OLS (the current-release source)
 * @param release the cached release, with its source URL, SHA-256 and OWL
 * imports; empty for OLS
 * @param olsVersion the version the current-release source reported; empty
 * for a cached release
 * @param olsDate when the current-release source reported it; empty for a
 * cached release
 * @param reRunnable true for a cached release, which a later run can use
 * again; false for OLS
 */
public record ResolvedVocabulary(String label, String declaredOriginal, Optional<String> declaredNormalised,
    Optional<String> mappingVersion, String versionUsed, String reason, Optional<CachedRelease> release,
    Optional<String> olsVersion, Optional<Instant> olsDate, boolean reRunnable) {

    /** Requires the label, the version used, the reason and every Optional. */
    public ResolvedVocabulary {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(declaredNormalised, "declaredNormalised");
        Objects.requireNonNull(mappingVersion, "mappingVersion");
        Objects.requireNonNull(versionUsed, "versionUsed");
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(release, "release");
        Objects.requireNonNull(olsVersion, "olsVersion");
        Objects.requireNonNull(olsDate, "olsDate");
    }
}
