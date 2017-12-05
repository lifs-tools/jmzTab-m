/*
 * Copyright 2017 Leibniz Institut für Analytische Wissenschaften - ISAS e.V..
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
package de.isas.lipidomics.mztab.validator.webapp.service.validation;

import java.util.Objects;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class ValidationFile {

    private String file;
    private String mzTabVersion;

    public ValidationFile() {

    }

    public ValidationFile(String file, String mzTabVersion) {
        this();
        this.file = file;
        this.mzTabVersion = mzTabVersion;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getMzTabVersion() {
        return mzTabVersion;
    }

    public void setMzTabVersion(String mzTabVersion) {
        this.mzTabVersion = mzTabVersion;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.file);
        hash = 97 * hash + Objects.hashCode(this.mzTabVersion);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ValidationFile other = (ValidationFile) obj;
        if (!Objects.equals(this.file, other.file)) {
            return false;
        }
        if (!Objects.equals(this.mzTabVersion, other.mzTabVersion)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ValidationFile{" + "file=" + file + ", mzTabVersion=" + mzTabVersion + '}';
    }

}
