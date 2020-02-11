
# ValidationMessage

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**code** | **String** |  | 
**category** | [**CategoryEnum**](#CategoryEnum) |  | 
**messageType** | [**MessageTypeEnum**](#MessageTypeEnum) |  |  [optional]
**message** | **String** |  | 
**lineNumber** | **Long** |  |  [optional]


<a name="CategoryEnum"></a>
## Enum: CategoryEnum
Name | Value
---- | -----
FORMAT | &quot;format&quot;
LOGICAL | &quot;logical&quot;
CROSS_CHECK | &quot;cross_check&quot;


<a name="MessageTypeEnum"></a>
## Enum: MessageTypeEnum
Name | Value
---- | -----
ERROR | &quot;error&quot;
WARN | &quot;warn&quot;
INFO | &quot;info&quot;



