/*
 * mzTab-M reference implementation and validation API.
 * This is the mzTab-M reference implementation and validation API service.
 *
 * OpenAPI spec version: 2.0.0
 * Contact: nils.hoffmann@isas.de
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package de.isas.mztab2.model;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import de.isas.mztab2.model.IndexedElement;
import de.isas.mztab2.model.Parameter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.dataformat.xml.annotation.*;
import javax.xml.bind.annotation.*;
import javax.validation.constraints.*;
import javax.validation.Valid;
import de.isas.lipidomics.mztab2.validation.constraints.*;
/**
 * 
 * Software used to analyze the data and obtain the reported results. The parameter’s value SHOULD contain the software’s version. The order (numbering) should reflect the order in which the tools were used. A software setting used. This field MAY occur multiple times for a single software. The value of this field is deliberately set as a String, since there currently do not exist CV terms for every possible setting.

 * 
 *
 * <p>mzTab-M specification example(s):</p>
 * <pre><code>MTD	software[1]	[MS, MS:1002879, Progenesis QI, 3.0]
MTD	software[1]-setting	Fragment tolerance = 0.1 Da
…
MTD	software[2]-setting	Parent tolerance = 0.5 Da
</code></pre>
 * 
 *
 */
@ApiModel(description = "Software used to analyze the data and obtain the reported results. The parameter’s value SHOULD contain the software’s version. The order (numbering) should reflect the order in which the tools were used. A software setting used. This field MAY occur multiple times for a single software. The value of this field is deliberately set as a String, since there currently do not exist CV terms for every possible setting. ")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-02-11T15:12:05.057+01:00")
@XmlRootElement(name = "Software")
@XmlAccessorType(XmlAccessType.FIELD)
@JacksonXmlRootElement(localName = "Software")
public class Software extends IndexedElement {

  /**
   * Property enumeration for Software.
   */
  public static enum Properties {
      parameter("parameter"), 
      setting("setting");

    private final String propertyName;

    private Properties(String propertyName) {
      this.propertyName = propertyName;
    }

    public String getPropertyName() {
      return propertyName;
    }

    public String toString() {
      return propertyName;
    }

    public String toUpper() {
      return propertyName.toUpperCase();
    }

    public static Properties of(String value) {
      if(value==null) {
        throw new NullPointerException("Argument value must not be null!");
      }
      return Arrays.asList(Properties.values()).stream().filter(m -> m.propertyName.equals(value.toLowerCase())).findAny().orElseThrow(IllegalArgumentException::new);
    }
  };
  
  @JsonProperty("parameter")
  @JacksonXmlProperty(localName = "parameter")
  @XmlElement(name = "parameter")
  private Parameter parameter = null;
  @JsonProperty("setting")
  // Is a container wrapped=false
  // items.name=setting items.baseName=setting items.xmlName= items.xmlNamespace=
  // items.example= items.type=String
  @XmlElement(name = "setting")
  private List<String> setting = null;
  
  @Override
  public Software id(Integer id) {
   super.setId(id);
   return this;
  }
  
  @Override
  public Software elementType(String elementType) {
   super.setElementType(elementType);
   return this;
  }

 /**
   * Builder method for parameter.
   *
   * @see Software#setParameter Software#setParameter for specification examples
   * @see Software#getParameter Software#getParameter for validation constraints
   * @param parameter a {@code Parameter} parameter.
   * @return Software
  **/
  public Software parameter(Parameter parameter) {
   this.parameter = parameter;
   return this;
  }

   /**
   * Parameter defining the software being used.
   *
   * @return parameter
  **/
  @Valid
  @ApiModelProperty(value = "Parameter defining the software being used.")
  public Parameter getParameter() {
    return parameter;
  }

 /**
   * Set parameter.
   *
   * 
   * 
   * @see #getParameter Software#getParameter for validation constraints
   * @param parameter a {@code Parameter} parameter.
  **/
  public void setParameter(Parameter parameter) {
    this.parameter = parameter;
  }


 /**
   * Builder method for setting.
   *
   * @see Software#setSetting Software#setSetting for specification examples
   * @see Software#getSetting Software#getSetting for validation constraints
   * @param setting a {@code List<String>} parameter.
   * @return Software
  **/
  public Software setting(List<String> setting) {
   this.setting = setting;
   return this;
  }

  /**
   * Add a single settingItem to the setting collection.
   *
   * @see Software#getSetting Software#getSetting for validation constraints
   * @param settingItem a {@code String} parameter.
   * @return Software
   */
  public Software addSettingItem(String settingItem) {
    if (this.setting == null) {
      this.setting = new ArrayList<>();
    }
    this.setting.add(settingItem);
    return this;
  }

   /**
   * A software setting used. This field MAY occur multiple times for a
single software. The value of this field is deliberately set as a
String, since there currently do not exist cvParams for every
possible setting.

   *
   * @return setting
  **/
  @ApiModelProperty(value = "A software setting used. This field MAY occur multiple times for a single software. The value of this field is deliberately set as a String, since there currently do not exist cvParams for every possible setting. ")
  public List<String> getSetting() {
    return setting;
  }

 /**
   * Set setting.
   *
   * 
   * 
   * @see #getSetting Software#getSetting for validation constraints
   * @param setting a {@code List<String>} parameter.
  **/
  public void setSetting(List<String> setting) {
    this.setting = setting;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Software software = (Software) o;
    return Objects.equals(this.parameter, software.parameter) &&
        Objects.equals(this.setting, software.setting) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parameter, setting, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Software {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    parameter: ").append(toIndentedString(parameter)).append("\n");
    sb.append("    setting: ").append(toIndentedString(setting)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

