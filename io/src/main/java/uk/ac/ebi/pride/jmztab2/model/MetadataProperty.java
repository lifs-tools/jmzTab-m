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
package uk.ac.ebi.pride.jmztab2.model;

import java.util.Optional;

/**
 * Define a property in metadata, which depend on the {@link uk.ac.ebi.pride.jmztab2.model.MetadataElement}.
 *
 * @author qingwei
 * @author nilshoffmann
 * @since 23/05/13
 * 
 */
public enum MetadataProperty {
    MZTAB_VERSION                         (MetadataElement.MZTAB,                               "version"),
    MZTAB_ID                              (MetadataElement.MZTAB,                               "ID"),

    INSTRUMENT_NAME                       (MetadataElement.INSTRUMENT,                          "name"),
    INSTRUMENT_SOURCE                     (MetadataElement.INSTRUMENT,                          "source"),
    INSTRUMENT_ANALYZER                   (MetadataElement.INSTRUMENT,                          "analyzer"),
    INSTRUMENT_DETECTOR                   (MetadataElement.INSTRUMENT,                          "detector"),

    SOFTWARE_SETTING                      (MetadataElement.SOFTWARE,                            "setting"),

    CONTACT_NAME                          (MetadataElement.CONTACT,                             "name"),
    CONTACT_AFFILIATION                   (MetadataElement.CONTACT,                             "affiliation"),
    CONTACT_EMAIL                         (MetadataElement.CONTACT,                             "email"),
    CONTACT_ORCID                         (MetadataElement.CONTACT,                               "orcid"),

    SMALL_MOLECULE_QUANTIFICATION_UNIT    (MetadataElement.SMALL_MOLECULE,                      "quantification_unit"),
    SMALL_MOLECULE_IDENTIFICATION_RELIABILITY(MetadataElement.SMALL_MOLECULE,                   "identification_reliability"),
    SMALL_MOLECULE_FEATURE_QUANTIFICATION_UNIT(MetadataElement.SMALL_MOLECULE_FEATURE,          "quantification_unit"),

    MS_RUN_FORMAT                         (MetadataElement.MS_RUN,                              "format"),
    MS_RUN_LOCATION                       (MetadataElement.MS_RUN,                              "location"),
    MS_RUN_INSTRUMENT_REF                 (MetadataElement.MS_RUN,                              "instrument_ref"),
    MS_RUN_ID_FORMAT                      (MetadataElement.MS_RUN,                              "id_format"),
    MS_RUN_FRAGMENTATION_METHOD           (MetadataElement.MS_RUN,                              "fragmentation_method"),
    MS_RUN_HASH                           (MetadataElement.MS_RUN,                              "hash"),
    MS_RUN_HASH_METHOD                    (MetadataElement.MS_RUN,                              "hash_method"),
    MS_RUN_SCAN_POLARITY                  (MetadataElement.MS_RUN,                              "scan_polarity"),

    SAMPLE_SPECIES                        (MetadataElement.SAMPLE,                              "species"),
    SAMPLE_TISSUE                         (MetadataElement.SAMPLE,                              "tissue"),
    SAMPLE_CELL_TYPE                      (MetadataElement.SAMPLE,                              "cell_type"),
    SAMPLE_DISEASE                        (MetadataElement.SAMPLE,                              "disease"),
    SAMPLE_DESCRIPTION                    (MetadataElement.SAMPLE,                              "description"),
    SAMPLE_CUSTOM                         (MetadataElement.SAMPLE,                              "custom"),

    ASSAY_SAMPLE_REF                      (MetadataElement.ASSAY,                               "sample_ref"),
    ASSAY_MS_RUN_REF                      (MetadataElement.ASSAY,                               "ms_run_ref"),
    ASSAY_CUSTOM                          (MetadataElement.ASSAY,                               "custom"),
    ASSAY_EXTERNAL_URI                    (MetadataElement.ASSAY,                               "external_uri"),

    STUDY_VARIABLE_ASSAY_REFS             (MetadataElement.STUDY_VARIABLE,                      "assay_refs"),
    STUDY_VARIABLE_DESCRIPTION            (MetadataElement.STUDY_VARIABLE,                      "description"),
    STUDY_VARIABLE_AVERAGE_FUNCTION       (MetadataElement.STUDY_VARIABLE,                      "average_function"),
    STUDY_VARIABLE_VARIATION_FUNCTION     (MetadataElement.STUDY_VARIABLE,                      "variation_function"),
    STUDY_VARIABLE_FACTORS                (MetadataElement.STUDY_VARIABLE,                      "factors"),

    CV_LABEL                              (MetadataElement.CV,                                  "label"),
    CV_FULL_NAME                          (MetadataElement.CV,                                  "full_name"),
    CV_VERSION                            (MetadataElement.CV,                                  "version"),
    CV_URI                                (MetadataElement.CV,                                  "uri"),
    
    DATABASE_PREFIX                       (MetadataElement.DATABASE,                             "prefix"),
    DATABASE_VERSION                      (MetadataElement.DATABASE,                             "version"),
    DATABASE_URI                          (MetadataElement.DATABASE,                             "uri");
    
    private String name;
    private MetadataElement element;

    /**
     * Define a property depend on {@link MetadataElement}
     * For example: assay[1-n]-sample_ref, assay[1-n] is {@link MetadataElement#ASSAY},
     * sample_ref is {@link MetadataProperty#ASSAY_SAMPLE_REF}
     */
    MetadataProperty(MetadataElement element, String name) {
        this.element = element;
        this.name = name;
    }

    /**
     * <p>Getter for the field <code>element</code>.</p>
     *
     * @return dependent {@link uk.ac.ebi.pride.jmztab2.model.MetadataElement}
     */
    public MetadataElement getElement() {
        return element;
    }

    /**
     * <p>Getter for the field <code>name</code>.</p>
     *
     * @return property name
     */
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Find property by {@link uk.ac.ebi.pride.jmztab2.model.MetadataElement} and property name with case-insensitive. If not find, return null.
     *
     * @param element a {@link uk.ac.ebi.pride.jmztab2.model.MetadataElement} object.
     * @param propertyName a {@link java.lang.String} object.
     * @return an optional {@link uk.ac.ebi.pride.jmztab2.model.MetadataProperty} object.
     */
    public static Optional<MetadataProperty> findProperty(MetadataElement element, String propertyName) {
        if (element == null || propertyName == null) {
            return Optional.empty();
        }

        MetadataProperty property;
        try {
            property = MetadataProperty.valueOf((element.getName() + "_" + propertyName).toUpperCase());
        } catch (IllegalArgumentException e) {
            property = null;
        }

        return Optional.ofNullable(property);
    }
}
