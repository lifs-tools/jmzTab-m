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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.lifstools.mztab2.cvmapping.local.VersionChoice.Reason;
import uk.ac.ebi.pride.jmztab2.utils.errors.CrossCheckErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;

/**
 * Which version of a vocabulary is used: design §4.4's table, after the
 * declared version has been normalised ({@link DeclaredVersion}).
 *
 * <table>
 * <caption>The file's normalised version against the mapping's</caption>
 * <tr><th>File</th><th>Version used</th><th>Message</th></tr>
 * <tr><td>the same or newer</td><td>the file's (FILE, or FILE_DATE for a
 * date)</td><td>none</td></tr>
 * <tr><td>older</td><td>the mapping's (MAPPING)</td><td>Warn-3019</td></tr>
 * <tr><td>none, or not recognised</td><td>the mapping's (MAPPING)</td><td>none;
 * Warn-3021 for a non-empty value that is not recognised</td></tr>
 * <tr><td>a version, the mapping none</td><td>the file's (FILE)</td>
 * <td>none</td></tr>
 * <tr><td>none, the mapping none</td><td>the latest (LATEST)</td>
 * <td>Info-3022, which the resolver adds once the latest release has
 * been resolved</td></tr>
 * <tr><td>not comparable with the mapping's</td><td>the file's (FILE)</td>
 * <td>Warn-3020</td></tr>
 * </table>
 */
public final class VersionRule {

    private VersionRule() {
    }

    /**
     * Chooses the version of one vocabulary.
     *
     * @param label the vocabulary's cv label, for the messages
     * @param declared the file's version, normalised
     * @param mappingVersion the version the mapping's rules were written for
     * @param scheme how the vocabulary's versions compare
     * @return the version to use, why, and the messages
     */
    public static VersionChoice choose(String label, DeclaredVersion declared, Optional<String> mappingVersion,
        VersionScheme scheme) {
        List<MZTabError> messages = new ArrayList<>();
        if (declared.kind() == DeclaredVersion.Kind.NOT_RECOGNISED && !declared.original().isBlank()) {
            messages.add(new MZTabError(CrossCheckErrorType.CvVersionNotRecognised, -1, label, declared.original()));
        }
        Optional<String> file = declared.normalised();
        if (file.isEmpty()) {
            return mappingVersion
                .map(m -> new VersionChoice(m, Reason.MAPPING, messages))
                .orElseGet(() -> new VersionChoice(null, Reason.LATEST, messages));
        }
        Reason fileReason = declared.kind() == DeclaredVersion.Kind.FROM_DATE ? Reason.FILE_DATE : Reason.FILE;
        if (mappingVersion.isEmpty()) {
            return new VersionChoice(file.get(), fileReason, messages);
        }
        String mapping = mappingVersion.get();
        Optional<Integer> order = scheme.compare(file.get(), mapping);
        if (order.isEmpty()) {
            messages.add(new MZTabError(CrossCheckErrorType.CvVersionsNotComparable, -1, label, file.get(), mapping));
            return new VersionChoice(file.get(), fileReason, messages);
        }
        if (order.get() < 0) {
            messages.add(new MZTabError(CrossCheckErrorType.CvVersionOlderThanMapping, -1, label, file.get(),
                mapping));
            return new VersionChoice(mapping, Reason.MAPPING, messages);
        }
        return new VersionChoice(file.get(), fileReason, messages);
    }
}
