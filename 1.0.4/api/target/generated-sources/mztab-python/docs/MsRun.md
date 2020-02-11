# MsRun

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | The msRun&#39;s name. | 
**location** | **str** | The msRun&#39;s location URI. | 
**instrument_ref** | [**Instrument**](Instrument.md) | The instrument on which this msRun was measured. | [optional] 
**format** | [**Parameter**](Parameter.md) | The msRun&#39;s file format. | [optional] 
**id_format** | [**Parameter**](Parameter.md) | The msRun&#39;s mass spectra id format. | [optional] 
**fragmentation_method** | [**list[Parameter]**](Parameter.md) | The fragmentation methods applied during this msRun. | [optional] 
**scan_polarity** | [**list[Parameter]**](Parameter.md) | The scan polarity/polarities used during this msRun. | [optional] 
**hash** | **str** | The file hash value of this msRun&#39;s data file. | [optional] 
**hash_method** | [**Parameter**](Parameter.md) | The hash method used to calculate the file hash. | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


