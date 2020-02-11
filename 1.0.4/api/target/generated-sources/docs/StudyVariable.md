
# StudyVariable

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **String** | The study variable name. | 
**assayRefs** | [**List&lt;Assay&gt;**](Assay.md) | The assays referenced by this study variable. |  [optional]
**averageFunction** | [**Parameter**](Parameter.md) | The function used to calculate summarised small molecule quantities over the assays referenced by this study variable. |  [optional]
**variationFunction** | [**Parameter**](Parameter.md) | The function used to calculate the variation of small molecule quantities over the assays referenced by this study variable. |  [optional]
**description** | **String** | A free-form description of this study variable. |  [optional]
**factors** | [**List&lt;Parameter&gt;**](Parameter.md) | Parameters indicating which factors were used for the assays referenced by this study variable, and at which levels. |  [optional]



