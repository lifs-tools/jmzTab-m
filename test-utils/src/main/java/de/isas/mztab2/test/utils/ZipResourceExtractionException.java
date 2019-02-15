/*
 * Copyright 2018 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
package de.isas.mztab2.test.utils;

/**
 * This runtime exception is thrown when IO exceptions are encountered within
 * the {@link ZipResourceExtractor} test rule.
 *
 * @author nilshoffmann
 */
public class ZipResourceExtractionException extends RuntimeException {

    public ZipResourceExtractionException() {
    }

    public ZipResourceExtractionException(String message) {
        super(message);
    }

    public ZipResourceExtractionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZipResourceExtractionException(Throwable cause) {
        super(cause);
    }

    public ZipResourceExtractionException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
