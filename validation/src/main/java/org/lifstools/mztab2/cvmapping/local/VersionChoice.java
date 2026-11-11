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

import java.util.List;
import java.util.Objects;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;

/**
 * The version {@link VersionRule} chooses for a vocabulary, why, and the
 * messages the choice gives.
 *
 * @param version the release to use; null for the latest, which the resolver
 * turns into a concrete version
 * @param reason why this version
 * @param messages Warn-3019, Warn-3020 or Warn-3021, in the order they arose
 */
public record VersionChoice(String version, Reason reason, List<MZTabError> messages) {

    /** Why a version is chosen. */
    public enum Reason {
        /** The file's declared release. */
        FILE,
        /** The release current on the date the file declares. */
        FILE_DATE,
        /** The mapping's version. */
        MAPPING,
        /** Neither the file nor the mapping names one: the latest. */
        LATEST
    }

    /** Requires the reason, and a version for every reason but LATEST; copies the messages. */
    public VersionChoice {
        Objects.requireNonNull(reason, "reason");
        if ((version == null) != (reason == Reason.LATEST)) {
            throw new IllegalArgumentException("a version is null exactly for LATEST: " + version + " " + reason);
        }
        messages = List.copyOf(messages);
    }
}
