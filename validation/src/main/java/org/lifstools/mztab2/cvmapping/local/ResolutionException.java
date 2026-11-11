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
 * A vocabulary release that cannot be resolved, with the reason.
 *
 * <p>
 * The message says what went wrong, without the vocabulary's name and version
 * (the 3017 and 3018 texts name both) and without a final period (those texts
 * append one).</p>
 */
public final class ResolutionException extends Exception {

    private static final long serialVersionUID = 1L;

    /** Why a release cannot be resolved. */
    public enum Reason {
        /** The release does not exist: its URL answers 404, or its name is not a release name. */
        NO_SUCH_RELEASE,
        /** A download or a release index request failed. */
        DOWNLOAD_FAILED,
        /**
         * A SHA-256 differs: a downloaded or cached file's from the one the
         * resources file knows, or a cached file's or import's from the one
         * its release.json records (a corrupt cache entry, found before its
         * index is rebuilt; it is neither downloaded again nor deleted).
         */
        CHECKSUM_MISMATCH,
        /** The file is not of the format the resources file gives. */
        PARSE_FAILED,
        /** Downloads are off and the cache holds no copy. */
        OFFLINE_NOT_CACHED,
        /** The resources file has no entry for the vocabulary. */
        UNKNOWN_VOCABULARY,
        /** Only OLS could answer, and OLS serves only its current release. */
        ONLY_OLS,
        /** The cache folder cannot be written. */
        WRITE_FAILED
    }

    /** Why the release cannot be resolved. */
    private final Reason reason;

    /**
     * Creates the exception.
     *
     * @param reason why the release cannot be resolved
     * @param message what went wrong; a final period is removed
     */
    public ResolutionException(Reason reason, String message) {
        this(reason, message, null);
    }

    /**
     * Creates the exception with its cause.
     *
     * @param reason why the release cannot be resolved
     * @param message what went wrong; a final period is removed
     * @param cause the underlying failure, or null
     */
    public ResolutionException(Reason reason, String message, Throwable cause) {
        super(withoutFinalPeriod(message), cause);
        this.reason = Objects.requireNonNull(reason, "reason");
    }

    /**
     * The reason.
     *
     * @return why the release cannot be resolved
     */
    public Reason reason() {
        return reason;
    }

    private static String withoutFinalPeriod(String message) {
        String m = String.valueOf(message).strip();
        while (m.endsWith(".")) {
            m = m.substring(0, m.length() - 1).strip();
        }
        return m;
    }
}
