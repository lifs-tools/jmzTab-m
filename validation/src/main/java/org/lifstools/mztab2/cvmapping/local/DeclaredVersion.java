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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Optional;

/**
 * The version a file declares for a vocabulary, turned into a release.
 * Rules, in order:
 * <ol>
 * <li>null or blank: NONE.</li>
 * <li>a value that fits the scheme and is one of the known releases, or any
 * value of an ISO_DATE scheme that fits it: RELEASE, as is.</li>
 * <li>a value in a recognised date form: FROM_DATE, the newest release dated
 * on or before it, or NOT_RECOGNISED if there is none.</li>
 * <li>a value that fits the scheme but is no known release and no date
 * (e.g. 4.1.999): RELEASE, as is, so that a strict check can report it.</li>
 * <li>anything else: NOT_RECOGNISED.</li>
 * </ol>
 *
 * @param original the declared text, as given (may be null)
 * @param normalised the release it stands for, if any
 * @param kind how the declaration was understood
 */
public record DeclaredVersion(String original, Optional<String> normalised, Kind kind) {

    public enum Kind {
        /** Nothing declared (null or blank). */
        NONE,
        /** A release name of the vocabulary's scheme, used as is. */
        RELEASE,
        /** A date, turned into the release current on that date. */
        FROM_DATE,
        /** Neither a release name nor a usable date; treated as undeclared. */
        NOT_RECOGNISED
    }

    private static final List<DateTimeFormatter> DATE_TIMES = List.of(
            formatter("dd:MM:uuuu HH:mm"));
    private static final List<DateTimeFormatter> DATES = List.of(
            formatter("dd-MM-uuuu"),
            formatter("dd:MM:uuuu"),
            formatter("uuuu-MM-dd"),
            formatter("dd.MM.uuuu"));

    private static DateTimeFormatter formatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);
    }

    /**
     * @param declared the declared version text, may be null
     * @param scheme the vocabulary's version scheme
     * @param releases the known releases of the vocabulary
     * @return the classification of the declaration
     */
    public static DeclaredVersion of(String declared, VersionScheme scheme, List<Release> releases) {
        if (declared == null || declared.isBlank()) {
            return new DeclaredVersion(declared, Optional.empty(), Kind.NONE);
        }
        String value = declared.trim();
        boolean fits = scheme.accepts(value);
        if (fits && (scheme == VersionScheme.ISO_DATE || isListed(value, releases))) {
            return new DeclaredVersion(declared, Optional.of(value), Kind.RELEASE);
        }
        Optional<LocalDate> date = parseDate(value);
        if (date.isPresent()) {
            Optional<String> release = currentOn(date.get(), scheme, releases);
            return release.isPresent()
                    ? new DeclaredVersion(declared, release, Kind.FROM_DATE)
                    : new DeclaredVersion(declared, Optional.empty(), Kind.NOT_RECOGNISED);
        }
        if (fits) {
            return new DeclaredVersion(declared, Optional.of(value), Kind.RELEASE);
        }
        return new DeclaredVersion(declared, Optional.empty(), Kind.NOT_RECOGNISED);
    }

    private static boolean isListed(String value, List<Release> releases) {
        return releases.stream().anyMatch(r -> value.equals(r.version()));
    }

    private static Optional<LocalDate> parseDate(String value) {
        for (DateTimeFormatter f : DATE_TIMES) {
            try {
                return Optional.of(LocalDateTime.parse(value, f).toLocalDate());
            } catch (DateTimeParseException e) {
                // try the next form
            }
        }
        for (DateTimeFormatter f : DATES) {
            try {
                return Optional.of(LocalDate.parse(value, f));
            } catch (DateTimeParseException e) {
                // try the next form
            }
        }
        return Optional.empty();
    }

    /** The newest release dated on or before the given date. */
    private static Optional<String> currentOn(LocalDate date, VersionScheme scheme, List<Release> releases) {
        Release best = null;
        for (Release r : releases) {
            if (r.date().isEmpty() || r.date().get().isAfter(date)) {
                continue;
            }
            if (best == null || newer(r, best, scheme)) {
                best = r;
            }
        }
        return Optional.ofNullable(best).map(Release::version);
    }

    private static boolean newer(Release a, Release b, VersionScheme scheme) {
        int byDate = a.date().get().compareTo(b.date().get());
        if (byDate != 0) {
            return byDate > 0;
        }
        return scheme.compare(a.version(), b.version()).orElse(0) > 0;
    }
}
