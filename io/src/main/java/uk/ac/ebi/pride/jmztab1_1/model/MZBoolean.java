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
package uk.ac.ebi.pride.jmztab1_1.model;

/**
 * In mzTab, using 0-false, 1-true to express the boolean value.
 *
 * @author qingwei
 * @since 06/02/13
 * 
 */
public enum MZBoolean {
    True("1"), False("0");
    private String value;

    /**
     * "0" for false, "1" for true.
     * @param value
     */
    MZBoolean(String value) {
        this.value = value;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return value;
    }
    
    /**
     * <p>Convert to a native Boolean.</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean toBoolean() {
        if(this == MZBoolean.True) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * <p>Find the MZBoolean for the given string representation.</p>
     *
     * @param booleanLabel "0" or "1" which used to define a boolean used in mzTab.
     * @return null if can not recognize the boolean label.
     */
    public static MZBoolean findBoolean(String booleanLabel) {
        booleanLabel = booleanLabel.trim();
        try {
            Integer id = new Integer(booleanLabel);
            MZBoolean mzBoolean = null;
            switch (id) {
                case 0:
                    mzBoolean = MZBoolean.False;
                    break;
                case 1:
                    mzBoolean = MZBoolean.True;
                    break;
            }
            return mzBoolean;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
