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
package de.isas.mztab2.io.serialization;

import com.fasterxml.jackson.databind.util.StdConverter;
import de.isas.mztab2.model.Uri;
import java.util.Optional;

/**
 * Converter from Uri to String.
 *
 * @author nilshoffmann
 */
public class UriConverter extends StdConverter<Uri, String> {

    /**
     * {@inheritDoc}
     */
    @Override
    public String convert(Uri uri) {
        return Optional.of(uri.getValue()).orElse("null");
    }

}
