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

/**
 * A preferred-resources file that can't be read or doesn't fit its schema.
 * The message names the file, the entry and the JSON pointer of the first
 * problem.
 */
public final class InvalidResourcesException extends Exception {

    private static final long serialVersionUID = 1L;

    /** The key of the entry at fault, or null. */
    private final String entryKey;
    /** The JSON pointer of the first problem; empty for the whole file. */
    private final String pointer;

    InvalidResourcesException(String message, String entryKey, String pointer, Throwable cause) {
        super(message, cause);
        this.entryKey = entryKey;
        this.pointer = pointer;
    }

    /**
     * The entry at fault.
     *
     * @return the key of the entry at fault, empty if the problem is outside any entry
     */
    public Optional<String> entryKey() {
        return Optional.ofNullable(entryKey);
    }

    /**
     * Where in the file the problem is.
     *
     * @return the JSON pointer of the first problem; empty for the whole file
     */
    public String pointer() {
        return pointer;
    }
}
