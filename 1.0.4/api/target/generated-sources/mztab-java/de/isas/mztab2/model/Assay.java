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
import de.isas.mztab2.model.MsRun;
import de.isas.mztab2.model.Parameter;
import de.isas.mztab2.model.Sample;
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
 * Specification of assay.
(empty) name: A name for each assay, to serve as a list of the assays that MUST be reported in the following tables. 
custom: Additional custom parameters or values for a given assay. 
external_uri: An external reference uri to further information about the assay, for example via a reference to an object within an ISA-TAB file. 
sample_ref: An association from a given assay to the sample analysed. 
ms_run_ref: An association from a given assay to the source MS run. All assays MUST reference exactly one ms_run unless a workflow with pre-fractionation is being encoded, in which case each assay MUST reference n ms_runs where n fractions have been collected. Multiple assays SHOULD reference the same ms_run to capture multiplexed experimental designs.      

 * 
 *
 * 
 *
 */
@ApiModel(description = "Specification of assay. (empty) name: A name for each assay, to serve as a list of the assays that MUST be reported in the following tables.  custom: Additional custom parameters or values for a given assay.  external_uri: An external reference uri to further information about the assay, for example via a reference to an object within an ISA-TAB file.  sample_ref: An association from a given assay to the sample analysed.  ms_run_ref: An association from a given assay to the source MS run. All assays MUST reference exactly one ms_run unless a workflow with pre-fractionation is being encoded, in which case each assay MUST reference n ms_runs where n fractions have been collected. Multiple assays SHOULD reference the same ms_run to capture multiplexed experimental designs.       ")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-02-11T15:12:05.057+01:00")
@XmlRootElement(name = "Assay")
@XmlAccessorType(XmlAccessType.FIELD)
@JacksonXmlRootElement(localName = "Assay")
public class Assay extends IndexedElement {

  /**
   * Property enumeration for Assay.
   */
  public static enum Properties {
      name("name"), 
      custom("custom"), 
      externalUri("external_uri"), 
      sampleRef("sample_ref"), 
      msRunRef("ms_run_ref");

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
  
  @JsonProperty("name")
  @JacksonXmlProperty(localName = "name")
  @XmlElement(name = "name")
  private String name = null;
  @JsonProperty("custom")
  // Is a container wrapped=false
  // items.name=custom items.baseName=custom items.xmlName= items.xmlNamespace=
  // items.example= items.type=Parameter
  @XmlElement(name = "custom")
  private List<Parameter> custom = null;
  @JsonProperty("external_uri")
  @JacksonXmlProperty(localName = "external_uri")
  @XmlElement(name = "external_uri")
  private String externalUri = null;
  @JsonProperty("sample_ref")
  @JacksonXmlProperty(localName = "sample_ref")
  @XmlElement(name = "sample_ref")
  private Sample sampleRef = null;
  @JsonProperty("ms_run_ref")
  // Is a container wrapped=false
  // items.name=msRunRef items.baseName=msRunRef items.xmlName= items.xmlNamespace=
  // items.example= items.type=MsRun
  @XmlElement(name = "msRunRef")
  private List<MsRun> msRunRef = new ArrayList<>();
  
  @Override
  public Assay id(Integer id) {
   super.setId(id);
   return this;
  }
  
  @Override
  public Assay elementType(String elementType) {
   super.setElementType(elementType);
   return this;
  }

 /**
   * Builder method for name.
   *
   * @see Assay#setName Assay#setName for specification examples
   * @see Assay#getName Assay#getName for validation constraints
   * @param name a {@code String} parameter.
   * @return Assay
  **/
  public Assay name(String name) {
   this.name = name;
   return this;
  }

   /**
   * The assay name.
   *
   * @return name
  **/
  @NotNull
  @ApiModelProperty(required = true, value = "The assay name.")
  public String getName() {
    return name;
  }

 /**
   * Set name.
   *
   * 
   * 
   * @see #getName Assay#getName for validation constraints
   * @param name a {@code String} parameter.
  **/
  public void setName(String name) {
    this.name = name;
  }


 /**
   * Builder method for custom.
   *
   * @see Assay#setCustom Assay#setCustom for specification examples
   * @see Assay#getCustom Assay#getCustom for validation constraints
   * @param custom a {@code List<Parameter>} parameter.
   * @return Assay
  **/
  public Assay custom(List<Parameter> custom) {
   this.custom = custom;
   return this;
  }

  /**
   * Add a single customItem to the custom collection.
   *
   * @see Assay#getCustom Assay#getCustom for validation constraints
   * @param customItem a {@code Parameter} parameter.
   * @return Assay
   */
  public Assay addCustomItem(Parameter customItem) {
    if (this.custom == null) {
      this.custom = new ArrayList<>();
    }
    this.custom.add(customItem);
    return this;
  }

   /**
   * Additional user or cv parameters.
   *
   * @return custom
  **/
  @Valid
  @ApiModelProperty(value = "Additional user or cv parameters.")
  public List<Parameter> getCustom() {
    return custom;
  }

 /**
   * Set custom.
   *
   * 
   * 
   * @see #getCustom Assay#getCustom for validation constraints
   * @param custom a {@code List<Parameter>} parameter.
  **/
  public void setCustom(List<Parameter> custom) {
    this.custom = custom;
  }


 /**
   * Builder method for externalUri.
   *
   * @see Assay#setExternalUri Assay#setExternalUri for specification examples
   * @see Assay#getExternalUri Assay#getExternalUri for validation constraints
   * @param externalUri a {@code String} parameter.
   * @return Assay
  **/
  public Assay externalUri(String externalUri) {
   this.externalUri = externalUri;
   return this;
  }

   /**
   * An external URI to further information about this assay.
   *
   * @return externalUri
  **/
  @ApiModelProperty(value = "An external URI to further information about this assay.")
  public String getExternalUri() {
    return externalUri;
  }

 /**
   * Set externalUri.
   *
   * 
   * 
   * @see #getExternalUri Assay#getExternalUri for validation constraints
   * @param externalUri a {@code String} parameter.
  **/
  public void setExternalUri(String externalUri) {
    this.externalUri = externalUri;
  }


 /**
   * Builder method for sampleRef.
   *
   * @see Assay#setSampleRef Assay#setSampleRef for specification examples
   * @see Assay#getSampleRef Assay#getSampleRef for validation constraints
   * @param sampleRef a {@code Sample} parameter.
   * @return Assay
  **/
  public Assay sampleRef(Sample sampleRef) {
   this.sampleRef = sampleRef;
   return this;
  }

   /**
   * The sample referenced by this assay.
   *
   * @return sampleRef
  **/
  @Valid
  @ApiModelProperty(value = "The sample referenced by this assay.")
  public Sample getSampleRef() {
    return sampleRef;
  }

 /**
   * Set sampleRef.
   *
   * 
   * 
   * @see #getSampleRef Assay#getSampleRef for validation constraints
   * @param sampleRef a {@code Sample} parameter.
  **/
  public void setSampleRef(Sample sampleRef) {
    this.sampleRef = sampleRef;
  }


 /**
   * Builder method for msRunRef.
   *
   * @see Assay#setMsRunRef Assay#setMsRunRef for specification examples
   * @see Assay#getMsRunRef Assay#getMsRunRef for validation constraints
   * @param msRunRef a {@code List<MsRun>} parameter.
   * @return Assay
  **/
  public Assay msRunRef(List<MsRun> msRunRef) {
   this.msRunRef = msRunRef;
   return this;
  }

  /**
   * Add a single msRunRefItem to the msRunRef collection.
   *
   * @see Assay#getMsRunRef Assay#getMsRunRef for validation constraints
   * @param msRunRefItem a {@code MsRun} parameter.
   * @return Assay
   */
  public Assay addMsRunRefItem(MsRun msRunRefItem) {
    this.msRunRef.add(msRunRefItem);
    return this;
  }

   /**
   * The ms run(s) referenced by this assay.
   *
   * @return msRunRef
  **/
  @NotNull
  @Valid
 @Size(min=1)  @ApiModelProperty(required = true, value = "The ms run(s) referenced by this assay.")
  public List<MsRun> getMsRunRef() {
    return msRunRef;
  }

 /**
   * Set msRunRef.
   *
   * 
   * 
   * @see #getMsRunRef Assay#getMsRunRef for validation constraints
   * @param msRunRef a {@code List<MsRun>} parameter.
  **/
  public void setMsRunRef(List<MsRun> msRunRef) {
    this.msRunRef = msRunRef;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Assay assay = (Assay) o;
    return Objects.equals(this.name, assay.name) &&
        Objects.equals(this.custom, assay.custom) &&
        Objects.equals(this.externalUri, assay.externalUri) &&
        Objects.equals(this.sampleRef, assay.sampleRef) &&
        Objects.equals(this.msRunRef, assay.msRunRef) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, custom, externalUri, sampleRef, msRunRef, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Assay {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    custom: ").append(toIndentedString(custom)).append("\n");
    sb.append("    externalUri: ").append(toIndentedString(externalUri)).append("\n");
    sb.append("    sampleRef: ").append(toIndentedString(sampleRef)).append("\n");
    sb.append("    msRunRef: ").append(toIndentedString(msRunRef)).append("\n");
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

