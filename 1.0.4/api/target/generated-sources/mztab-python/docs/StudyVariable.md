# StudyVariable

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | The study variable name. | 
**assay_refs** | [**list[Assay]**](Assay.md) | The assays referenced by this study variable. | [optional] 
**average_function** | [**Parameter**](Parameter.md) | The function used to calculate summarised small molecule quantities over the assays referenced by this study variable. | [optional] 
**variation_function** | [**Parameter**](Parameter.md) | The function used to calculate the variation of small molecule quantities over the assays referenced by this study variable. | [optional] 
**description** | **str** | A free-form description of this study variable. | [optional] 
**factors** | [**list[Parameter]**](Parameter.md) | Parameters indicating which factors were used for the assays referenced by this study variable, and at which levels. | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


