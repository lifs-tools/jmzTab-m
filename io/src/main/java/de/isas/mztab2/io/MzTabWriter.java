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
package de.isas.mztab2.io;

import de.isas.mztab2.model.MzTab;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Interface for mztab writer implementors.
 *
 * @author nilshoffmann
 */
public interface MzTabWriter<T> {

    /**
     * <p>
     * Write the mzTab object to the provided output stream writer.</p>
     *
     * This method does not close the output stream but will issue a
     * <code>flush</code> on the provided output stream writer!
     *
     * @param writer a {@link java.io.OutputStreamWriter} object.
     * @param mzTab a {@link de.isas.mztab2.model.MzTab} object.
     * @return the optional payload.
     * @throws java.io.IOException if any.
     */
    Optional<T> write(OutputStreamWriter writer, MzTab mzTab) throws IOException;

    /**
     * <p>
     * Write the mzTab object to the provided path file.</p>
     *
     *
     * @param path a {@link java.nio.file.Path} object.
     * @param mzTab a {@link de.isas.mztab2.model.MzTab} object.
     * @return the optional payload.
     * @throws java.io.IOException if any.
     */
    Optional<T> write(Path path, MzTab mzTab) throws IOException;

}
