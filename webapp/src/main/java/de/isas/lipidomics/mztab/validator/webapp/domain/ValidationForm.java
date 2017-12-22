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
package de.isas.lipidomics.mztab.validator.webapp.domain;

import de.isas.lipidomics.jmztabm.validation.MzTabValidator;
import de.isas.lipidomics.mztab.validator.webapp.service.ValidationService;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class ValidationForm {
    private MultipartFile file = null;
    private ValidationService.MzTabVersion mzTabVersion = ValidationService.MzTabVersion.MZTAB_1_1;
    private Integer maxErrors = 100;
    private final List<ValidationService.MzTabVersion> allVersions = Arrays.asList(ValidationService.MzTabVersion.values());

    public ValidationForm() {
        this(null, ValidationService.MzTabVersion.MZTAB_1_1, 100);
    }
    public ValidationForm(MultipartFile file,
        ValidationService.MzTabVersion mzTabVersion, Integer maxErrors) {
        this.file = file;
        this.mzTabVersion = mzTabVersion;
        this.maxErrors = maxErrors;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
    
    public List<ValidationService.MzTabVersion> getAllVersions() {
        return allVersions;
    }

    public ValidationService.MzTabVersion getMzTabVersion() {
        return mzTabVersion;
    }

    public void setMzTabVersion(ValidationService.MzTabVersion mzTabVersion) {
        this.mzTabVersion = mzTabVersion;
    }

    public Integer getMaxErrors() {
        return maxErrors;
    }

    public void setMaxErrors(Integer maxErrors) {
        this.maxErrors = maxErrors;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.file);
        hash = 97 * hash + Objects.hashCode(this.mzTabVersion);
        hash = 97 * hash + Objects.hashCode(this.maxErrors);
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
        final ValidationForm other = (ValidationForm) obj;
        if (!Objects.equals(this.file, other.file)) {
            return false;
        }
        if (this.mzTabVersion != other.mzTabVersion) {
            return false;
        }
        if (!Objects.equals(this.maxErrors, other.maxErrors)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ValidationForm{" + "file=" + file + ", mzTabVersion=" + mzTabVersion + ", maxErrors=" + maxErrors + '}';
    }

}
