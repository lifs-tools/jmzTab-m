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
import java.util.Optional;
import org.lifstools.mztab2.cvmapping.local.ResolutionException.Reason;
import uk.ac.ebi.pride.jmztab2.utils.errors.CrossCheckErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;

/**
 * A vocabulary that cannot be had in a strict run, or in a forced run that
 * found no substitute either (design §4.5). It carries the one Error-3017 the
 * validation reports for that vocabulary; the semantic validation stops
 * (Ruling P5).
 */
public final class VocabularyUnavailableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** The vocabulary's cv label. */
    private final String label;
    /** The version that could not be had. */
    private final String versionWanted;
    /** Why it could not be had. */
    private final String reason;
    /** The kind of failure, or null if none applies. */
    private final Reason kind;
    /** The Error-3017. */
    private final MZTabError error;

    /**
     * A failure of unknown kind, without a cause.
     *
     * @param label the vocabulary's cv label
     * @param versionWanted the version that was wanted, or {@code latest}
     * @param reason why it cannot be had, without a final period
     */
    public VocabularyUnavailableException(String label, String versionWanted, String reason) {
        this(label, versionWanted, null, reason, null);
    }

    /**
     * A failure with its kind and cause.
     *
     * @param label the vocabulary's cv label
     * @param versionWanted the version that was wanted, or {@code latest}
     * @param kind the kind of failure, or null
     * @param reason why it cannot be had, without a final period
     * @param cause the underlying failure, or null
     */
    public VocabularyUnavailableException(String label, String versionWanted, Reason kind, String reason,
        Throwable cause) {
        this(new MZTabError(CrossCheckErrorType.CvVersionUnavailable, -1, label, versionWanted, reason), label,
            versionWanted, kind, reason, cause);
    }

    private VocabularyUnavailableException(MZTabError error, String label, String versionWanted, Reason kind,
        String reason, Throwable cause) {
        super(error.getMessage(), cause);
        this.label = Objects.requireNonNull(label, "label");
        this.versionWanted = Objects.requireNonNull(versionWanted, "versionWanted");
        this.reason = Objects.requireNonNull(reason, "reason");
        this.kind = kind;
        this.error = error;
    }

    /**
     * The vocabulary.
     *
     * @return its cv label
     */
    public String label() {
        return label;
    }

    /**
     * The version that was wanted.
     *
     * @return the version, or {@code latest}
     */
    public String versionWanted() {
        return versionWanted;
    }

    /**
     * Why the vocabulary cannot be had.
     *
     * @return the reason, without a final period
     */
    public String reason() {
        return reason;
    }

    /**
     * The kind of failure.
     *
     * @return the kind, if known
     */
    public Optional<Reason> kind() {
        return Optional.ofNullable(kind);
    }

    /**
     * The error the validation reports.
     *
     * @return the Error-3017 for this vocabulary
     */
    public MZTabError error() {
        return error;
    }
}
