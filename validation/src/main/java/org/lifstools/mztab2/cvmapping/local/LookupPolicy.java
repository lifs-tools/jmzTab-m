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

import java.util.Objects;

/**
 * How a validation run looks up its vocabularies (design §4.5).
 *
 * @param source where terms are answered from: the local cache, or OLS only
 * @param forced true to go ahead with a substitute when a version cannot be
 * had (warnings), false to fail (errors)
 * @param cache the vocabulary cache; not used, and may be null, when the
 * source is OLS
 * @param resources the preferred resources, which say which vocabularies are
 * known and where their releases are
 * @param current the source of current releases: OLS's answers, and the last
 * fallback of a forced run
 */
public record LookupPolicy(Source source, boolean forced, VocabularyCache cache, PreferredResources resources,
    CurrentReleaseSource current) {

    /** Where terms are answered from. */
    public enum Source {
        /** Versioned releases from the {@link VocabularyCache}. */
        LOCAL,
        /** The current release source only (OLS): no version can be had. */
        OLS
    }

    /** Requires every field but the cache, which a LOCAL source requires too. */
    public LookupPolicy {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(resources, "resources");
        Objects.requireNonNull(current, "current");
        if (source == Source.LOCAL) {
            Objects.requireNonNull(cache, "cache");
        }
    }
}
