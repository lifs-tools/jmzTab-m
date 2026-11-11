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

import java.util.Optional;
import org.lifstools.mztab2.cvmapping.AncestorSource;

/**
 * The last fallback of a forced run, and the source of a run that asks OLS
 * (Ruling P7): a service that knows only the current release of each
 * vocabulary. Its answers cannot be reproduced later, so a vocabulary it
 * answers for is recorded as not re-runnable.
 */
public interface CurrentReleaseSource extends AncestorSource {

    /**
     * The name the messages give this source, e.g. {@code OLS}.
     *
     * @return the name
     */
    String name();

    /**
     * The release this source currently serves for a vocabulary.
     *
     * @param cvLabel the cv label of a term, as the file gives it
     * @return the version, or empty if the source names none or cannot be
     * asked
     */
    Optional<String> currentVersion(String cvLabel);
}
