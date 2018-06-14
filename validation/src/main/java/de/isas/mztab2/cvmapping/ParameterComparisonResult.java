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
package de.isas.mztab2.cvmapping;

/**
 * <p>
 * Result of a comparison of a CvTerm T defined in a CvRule against the parameter P
 * found via JXPath.</p>
 * <p>
 * The comparison can yield three different results: 
 *  P == T : P is identical to T: IDENTICAL,
 *  P -- T : P is a child of T: CHILD_OF,
 *  P != T : P is neither a child nor identical to T, they are: NOT_RELATED;
 *</p>
 * @author nilshoffmann
 */
public enum ParameterComparisonResult {
    IDENTICAL, CHILD_OF, NOT_RELATED;
}
