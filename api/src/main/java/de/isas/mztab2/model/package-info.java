/*
 * Copyright 2019 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
/**
 * Base package for the jmztab-m data model, generated from the <a href="https://github.com/lifs-tools/jmzTab-m/blob/master/api/src/main/resources/mzTab_m_swagger.yml" target="_blank">mzTab_m_swagger.yml</a> specification.
 * 
 * <ul>
 * <li><a href="https://github.com/lifs-tools/jmzTab-m#creating-an-mztab-20-object-model">How to use the jmzTab-m object model.</a></li>
 * <li><a href="https://github.com/lifs-tools/jmzTab-m">The jmzTab-m reference implementation.</a></li>
 * <li><a href="https://hupo-psi.github.io/mzTab/">The mzTab-M specification homepage.</a></li>
 * </ul>
 * 
 * The <a href="MzTab.html">MzTab</a> object is the root of the mzTab-M model hierarchy. It contains the following child sections:
 * <ul>
 * <li><a href="Metadata.html">Metadata</a> - The metadata section provides additional information about the dataset(s) reported in the mzTab file</li>
 * <li><a href="SmallMoleculeSummary.html">SmallMoleculeSummary</a> - Each row of the small molecule summary section is intended to report one final result to be communicated in terms of a molecule that has been quantified.</li>
 * <li><a href="SmallMoleculeFeature.html">SmallMoleculeFeature</a> - Each row of the small molecule feature section represents individual MS regions (generally considered to be the elution profile for all isotopomers formed from a single charge state of a molecule), that have been measured/quantified. Different adducts or derivatives and different charge states of individual molecules should be reported as separate SMF rows.</li>
 * <li><a href="SmallMoleculeEvidence.html">SmallMoleculeEvidence</a> - Each row of the small molecule evidence section represents evidence for identifications of small molecules/features, from database search or any other process used to give putative identifications to molecules. In a typical case, each row represents one result from a single search or intepretation of a piece of evidence e.g. a database search with a fragmentation spectrum. Multiple results from a given input data item (e.g. one fragment spectrum) SHOULD share the same value under evidence_input_id.</li> 
 * </ul>
 * 
 * @author nilshoffmann
 * @since 1.0.0
 * @version 1.0.2
 */
package de.isas.mztab2.model;
