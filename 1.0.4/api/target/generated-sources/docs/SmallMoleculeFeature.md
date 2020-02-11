
# SmallMoleculeFeature

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**prefix** | [**PrefixEnum**](#PrefixEnum) | The small molecule feature table row prefix. SMF MUST be used for rows of the small molecule feature table. |  [optional]
**headerPrefix** | [**HeaderPrefixEnum**](#HeaderPrefixEnum) | The small molecule feature table header prefix. SFH MUST be used for the small molecule feature table header line (the column labels). |  [optional]
**smfId** | **Integer** | A within file unique identifier for the small molecule feature. | 
**smeIdRefs** | **List&lt;Integer&gt;** | References to the identification evidence (SME elements) via referencing SME_ID values. Multiple values MAY be provided as a “|” separated list to indicate ambiguity in the identification or to indicate that different types of data supported the identifiction (see SME_ID_REF_ambiguity_code). For the case of a consensus approach where multiple adduct forms are used to infer the SML ID, different features should just reference the same SME_ID value(s). |  [optional]
**smeIdRefAmbiguityCode** | **Integer** | If multiple values are given under SME_ID_REFS, one of the following codes MUST be provided. 1&#x3D;Ambiguous identification; 2&#x3D;Only different evidence streams for the same molecule with no ambiguity; 3&#x3D;Both ambiguous identification and multiple evidence streams. If there are no or one value under SME_ID_REFs, this MUST be reported as null. |  [optional]
**adductIon** | **String** | The assumed classification of this molecule’s adduct ion after detection, following the general style in the 2013 IUPAC recommendations on terms relating to MS e.g. [M+H]1+, [M+Na]1+, [M+NH4]1+, [M-H]1-, [M+Cl]1-, [M+H]1+. |  [optional]
**isotopomer** | [**Parameter**](Parameter.md) | If de-isotoping has not been performed, then the isotopomer quantified MUST be reported here e.g. “+1”, “+2”, “13C peak” using CV terms, otherwise (i.e. for approaches where SMF rows are de-isotoped features) this MUST be null. |  [optional]
**expMassToCharge** | **Double** | The experimental mass/charge value for the feature, by default assumed to be the mean across assays or a representative value. For approaches that report isotopomers as SMF rows, then the m/z of the isotopomer MUST be reported here. | 
**charge** | **Integer** | The feature’s charge value using positive integers both for positive and negative polarity modes. | 
**retentionTimeInSeconds** | **Double** | The apex of the feature on the retention time axis, in a Master or aggregate MS run. Retention time MUST be reported in seconds. Retention time values for individual MS runs (i.e. before alignment) MAY be reported as optional columns. Retention time SHOULD only be null in the case of direct infusion MS or other techniques where a retention time value is absent or unknown. Relative retention time or retention time index values MAY be reported as optional columns, and could be considered for inclusion in future versions of mzTab as appropriate. |  [optional]
**retentionTimeInSecondsStart** | **Double** | The start time of the feature on the retention time axis, in a Master or aggregate MS run. Retention time MUST be reported in seconds. Retention time start and end SHOULD only be null in the case of direct infusion MS or other techniques where a retention time value is absent or unknown and MAY be reported in optional columns. |  [optional]
**retentionTimeInSecondsEnd** | **Double** | The end time of the feature on the retention time axis, in a Master or aggregate MS run. Retention time MUST be reported in seconds. Retention time start and end SHOULD only be null in the case of direct infusion MS or other techniques where a retention time value is absent or unknown and MAY be reported in optional columns.. |  [optional]
**abundanceAssay** | **List&lt;Double&gt;** | The feature’s abundance in every assay described in the metadata section MUST be reported. Null or zero values may be reported as appropriate. |  [optional]
**opt** | [**List&lt;OptColumnMapping&gt;**](OptColumnMapping.md) | Additional columns can be added to the end of the small molecule feature table. These column headers MUST start with the prefix “opt_” followed by the {identifier} of the object they reference: assay, study variable, MS run or “global” (if the value relates to all replicates). Column names MUST only contain the following characters: ‘A’-‘Z’, ‘a’-‘z’, ‘0’-‘9’, ‘’, ‘-’, ‘[’, ‘]’, and ‘:’. CV parameter accessions MAY be used for optional columns following the format: opt{identifier}_cv_{accession}_\\{parameter name}. Spaces within the parameter’s name MUST be replaced by ‘_’.  |  [optional]
**comment** | [**List&lt;Comment&gt;**](Comment.md) |  |  [optional]


<a name="PrefixEnum"></a>
## Enum: PrefixEnum
Name | Value
---- | -----
SMF | &quot;SMF&quot;


<a name="HeaderPrefixEnum"></a>
## Enum: HeaderPrefixEnum
Name | Value
---- | -----
SFH | &quot;SFH&quot;



