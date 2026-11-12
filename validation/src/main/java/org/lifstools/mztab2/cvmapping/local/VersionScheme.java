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
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * How the releases of a vocabulary are named, and therefore how two release
 * names are ordered.
 */
public enum VersionScheme {
    /** Two or more dot-separated non-negative integers, e.g. 4.1.200. */
    DOTTED,
    /** A calendar date as yyyy-MM-dd. */
    ISO_DATE,
    /** A plain non-negative integer, e.g. 230. */
    RELEASE_NUMBER;

    private static final Pattern DOTTED_FORM = Pattern.compile("\\d+(\\.\\d+)+");
    private static final Pattern NUMBER_FORM = Pattern.compile("\\d+");

    /**
     * Whether a version is a release name of this scheme.
     *
     * @param v a version string, may be null
     * @return true if v is a release name of this scheme
     */
    public boolean accepts(String v) {
        if (v == null) {
            return false;
        }
        switch (this) {
            case DOTTED:
                return DOTTED_FORM.matcher(v).matches();
            case ISO_DATE:
                return isoDate(v) != null;
            case RELEASE_NUMBER:
                return NUMBER_FORM.matcher(v).matches();
            default:
                return false;
        }
    }

    /**
     * Compares two release names. Missing trailing dotted components count as 0.
     *
     * @param a a release name
     * @param b another release name
     * @return negative, zero or positive; empty if either value does not fit
     * this scheme
     */
    public Optional<Integer> compare(String a, String b) {
        if (!accepts(a) || !accepts(b)) {
            return Optional.empty();
        }
        switch (this) {
            case DOTTED:
                return Optional.of(compareDotted(a, b));
            case ISO_DATE:
                return Optional.of(isoDate(a).compareTo(isoDate(b)));
            default:
                return Optional.of(compareNumbers(a, b));
        }
    }

    private static LocalDate isoDate(String v) {
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static int compareDotted(String a, String b) {
        String[] x = a.split("\\.");
        String[] y = b.split("\\.");
        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            int c = compareNumbers(i < x.length ? x[i] : "0", i < y.length ? y[i] : "0");
            if (c != 0) {
                return c;
            }
        }
        return 0;
    }

    /** Numeric comparison of digit strings of any length. */
    private static int compareNumbers(String a, String b) {
        String x = a.replaceFirst("^0+(?=\\d)", "");
        String y = b.replaceFirst("^0+(?=\\d)", "");
        if (x.length() != y.length()) {
            return Integer.signum(x.length() - y.length());
        }
        return Integer.signum(x.compareTo(y));
    }
}
