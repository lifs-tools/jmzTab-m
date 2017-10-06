package de.isas.lipidomics.jmztabm.model;

import lombok.Value;


/**
 *
 * @author nils.hoffmann
 */
@Value
public class MetadataSection {
    private final String prefix = "MTD";
    private final String description = "The MTD section holds the metadata of the mzTab file.";
//    private final HashMap<String, MzTabRecord> = ;
//    prefix: MTD
//Description: |
// The MTD section holds the metadata of the mzTab file.
//# Allowed children in MTD
//mzTab-version:
// Description: |
//  The version of the mzTab file.
// Type: String
// Mandatory: True
// Cardinality: 1..1
// Example: |
//  MTD mzTab-version 1.1.0
//# mzTab-ID
//mzTab-ID:
//  Description: |
//   The unique ID of the mzTab file.
//  Type: String
//  Mandatory: True
//  Cardinality: 1
//  Example: |
//   MTD  mzTab-ID PRIDE_1234
//# multiple children purposefully omitted
//sample_processing:
//  Description: |
//   A list of parameters describing a sample processing step. The order of the data_processing items should reflect the order these processing steps were performed in. If multiple parameters are given for a step these MUST be separated by a “|”.
//  Type: Parameter List
//  Mandatory: False
//  Cardinality: 0..*
//  Example: |
//   MTD sample_processing[1] [SEP, SEP:00173, SDS PAGE,]
//   MTD sample_processing[2] [SEP, SEP:00142, enzyme digestion,]|[MS, … MS:1001251, Trypsin, ]
//# multiple children purposefully omitted
//software:
// Description: |
//  Software used to analyze the data and obtain the reported results. The parameter’s value SHOULD contain the software’s version. The order (numbering) should reflect the order in which the tools were used.
// Type: Parameter
// Mandatory: True
// Cardinality: 1..*
// Example: |
//   MTD software[1] [MS, MS:1001207, Mascot, 2.3]
//   MTD software[2] [MS, MS:1001561, Scaffold, 1.0]
}
