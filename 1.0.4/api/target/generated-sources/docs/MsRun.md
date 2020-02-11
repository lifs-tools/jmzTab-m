
# MsRun

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **String** | The msRun&#39;s name. | 
**location** | **String** | The msRun&#39;s location URI. | 
**instrumentRef** | [**Instrument**](Instrument.md) | The instrument on which this msRun was measured. |  [optional]
**format** | [**Parameter**](Parameter.md) | The msRun&#39;s file format. |  [optional]
**idFormat** | [**Parameter**](Parameter.md) | The msRun&#39;s mass spectra id format. |  [optional]
**fragmentationMethod** | [**List&lt;Parameter&gt;**](Parameter.md) | The fragmentation methods applied during this msRun. |  [optional]
**scanPolarity** | [**List&lt;Parameter&gt;**](Parameter.md) | The scan polarity/polarities used during this msRun. |  [optional]
**hash** | **String** | The file hash value of this msRun&#39;s data file. |  [optional]
**hashMethod** | [**Parameter**](Parameter.md) | The hash method used to calculate the file hash. |  [optional]



