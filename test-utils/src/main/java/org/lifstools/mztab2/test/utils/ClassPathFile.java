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
package org.lifstools.mztab2.test.utils;

/**
 * Test files enum to configure test files.
 *
 * @author nilshoffmann
 */
public enum ClassPathFile {

//    "/metabolomics/MTBLS263.mztab",
//        "/metabolomics/MouseLiver_negative.mzTab",
//        "/metabolomics/MouseLiver_negative_mztab_null-colunit.txt",
//        "/metabolomics/StandardMix_negative_exportPositionLevel.mzTab",
//        "/metabolomics/StandardMix_negative_exportSpeciesLevel.mzTab",
//        "/metabolomics/StandardMix_positive_exportPositionLevel.mzTab",
//        "/metabolomics/StandardMix_positive_exportSpeciesLevel.mzTab",
//        "/metabolomics/gcxgc-ms-example.mztab",
//        "/metabolomics/lipidomics-example.mzTab",
//        "/metabolomics/minimal-m-2.0.mztab"
///testset/mtdFile.txt
    MTDFILE("/testset/", "mtdFile.txt"),
    MTBLS263("/metabolomics/", "MTBLS263.mztab"),
    MOUSELIVER_NEGATIVE("/metabolomics/", "MouseLiver_negative.mzTab"),
    MOUSELIVER_NEGATIVE_MZTAB_NULL_COLUNIT("/metabolomics/",
        "MouseLiver_negative_mztab_null-colunit.txt"),
    STANDARDMIX_NEGATIVE_EXPORTPOSITIONLEVEL("/metabolomics/",
        "StandardMix_negative_exportPositionLevel.mzTab"),
    STANDARDMIX_NEGATIVE_EXPORTSPECIESLEVEL("/metabolomics/",
        "StandardMix_negative_exportSpeciesLevel.mzTab"),
    STANDARDMIX_POSITIVE_EXPORTPOSITIONLEVEL("/metabolomics/",
        "StandardMix_positive_exportPositionLevel.mzTab"),
    STANDARDMIX_POSITIVE_EXPORTSPECIESLEVEL("/metabolomics/",
        "StandardMix_positive_exportSpeciesLevel.mzTab"),
    GCXGC_MS_EXAMPLE("/metabolomics/", "gcxgc-ms-example.mztab"),
    LIPIDOMICS_EXAMPLE("/metabolomics/", "lipidomics-example.mzTab"),
    LIPIDOMICS_EXAMPLE_WRONG_MSSCAN_REF("/metabolomics/", "lipidomics-example-wrong-msscan-ref.mzTab"),
    MINIMAL_EXAMPLE("/metabolomics/", "minimal-m-2.0.mztab");

    private final String resourcePathPrefix;
    private final String fileName;

    ClassPathFile(String resourcePathPrefix, String fileName) {
        this.resourcePathPrefix = resourcePathPrefix;
        this.fileName = fileName;
    }

    public String fileName() {
        return fileName;
    }

    public String resourcePath() {
        return resourcePathPrefix + fileName();
    }

}
