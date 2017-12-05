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
public class ValidationResult {


    private final int lineNumber;
    private final Level level;
    private final String message;
    private final String ruleId;
    private final String styleClass;

    public ValidationResult(int lineNumber, Level level, String message, String ruleId) {
        this.lineNumber = lineNumber;
        this.level = level;
        this.message = message;
        this.ruleId = ruleId;
        switch (level) {
            case ERROR:
                this.styleClass = "table-danger";
                break;
            case WARN:
                this.styleClass = "table-warning";
                break;
            default:
                this.styleClass = "";
        }
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public Level getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getStyleClass() {
        return styleClass;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + this.lineNumber;
        hash = 59 * hash + Objects.hashCode(this.level);
        hash = 59 * hash + Objects.hashCode(this.message);
        hash = 59 * hash + Objects.hashCode(this.ruleId);
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
        final ValidationResult other = (ValidationResult) obj;
        if (this.lineNumber != other.lineNumber) {
            return false;
        }
        if (!Objects.equals(this.message, other.message)) {
            return false;
        }
        if (!Objects.equals(this.ruleId, other.ruleId)) {
            return false;
        }
        if (this.level != other.level) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ValidationResult{" + "lineNumber=" + lineNumber + ", level=" + level + ", message=" + message + ", ruleId=" + ruleId + '}';
    }

}
