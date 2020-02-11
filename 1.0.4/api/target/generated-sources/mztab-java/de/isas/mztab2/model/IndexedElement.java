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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.*;
import javax.xml.bind.annotation.*;
import javax.validation.constraints.*;
import javax.validation.Valid;
import de.isas.lipidomics.mztab2.validation.constraints.*;
/**
 * 
 * Indexed elements (IDs) define a unique ID for a collection of multiple metadata elements of the same type within the mzTab-M document, e.g. for sample, assay, study variable etc.
 * 
 *
 * <p>mzTab-M specification example(s):</p>
 * <pre><code>MTD	sample[1]-species[1]	[NCBITaxon, NCBITaxon:9606, Homo sapiens, ]
MTD	assay[1]	first assay description
MTD	study_variable[1]	Group B (spike-in 0.74 fmol/uL)
</code></pre>
 * 
 *
 */
@ApiModel(description = "Indexed elements (IDs) define a unique ID for a collection of multiple metadata elements of the same type within the mzTab-M document, e.g. for sample, assay, study variable etc.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-02-11T15:12:05.057+01:00")@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "elementType", visible = true )
@JsonSubTypes({
  @JsonSubTypes.Type(value = Uri.class, name = "Uri"),
  @JsonSubTypes.Type(value = Assay.class, name = "Assay"),
  @JsonSubTypes.Type(value = MsRun.class, name = "MsRun"),
  @JsonSubTypes.Type(value = Database.class, name = "Database"),
  @JsonSubTypes.Type(value = StudyVariable.class, name = "StudyVariable"),
  @JsonSubTypes.Type(value = SampleProcessing.class, name = "SampleProcessing"),
  @JsonSubTypes.Type(value = Sample.class, name = "Sample"),
  @JsonSubTypes.Type(value = Publication.class, name = "Publication"),
  @JsonSubTypes.Type(value = Contact.class, name = "Contact"),
  @JsonSubTypes.Type(value = CV.class, name = "CV"),
  @JsonSubTypes.Type(value = Instrument.class, name = "Instrument"),
  @JsonSubTypes.Type(value = Parameter.class, name = "Parameter"),
  @JsonSubTypes.Type(value = Software.class, name = "Software"),
})

@XmlRootElement(name = "IndexedElement")
@XmlAccessorType(XmlAccessType.FIELD)
@JacksonXmlRootElement(localName = "IndexedElement")
public class IndexedElement {

  /**
   * Property enumeration for IndexedElement.
   */
  public static enum Properties {
      id("id"), 
      elementType("elementType");

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
  
  @JsonProperty("id")
  @JacksonXmlProperty(localName = "id")
  @XmlElement(name = "id")
  private Integer id = null;
  @JsonProperty("elementType")
  @JacksonXmlProperty(localName = "elementType")
  @XmlElement(name = "elementType")
  private String elementType = "element_type";

 /**
   * Builder method for id.
   *
   * @see IndexedElement#setId IndexedElement#setId for specification examples
   * @see IndexedElement#getId IndexedElement#getId for validation constraints
   * @param id a {@code Integer} parameter.
   * @return IndexedElement
  **/
  public IndexedElement id(Integer id) {
   this.id = id;
   return this;
  }

   /**
   * <p>Get id.</p>
   *
   * <p>Minimum: 1</p>
   * @return id
  **/
  @NotNull
 @Min(1)  @ApiModelProperty(required = true, value = "")
  public Integer getId() {
    return id;
  }

 /**
   * Set id.
   *
   * 
   * 
   * @see #getId IndexedElement#getId for validation constraints
   * @param id a {@code Integer} parameter.
  **/
  public void setId(Integer id) {
    this.id = id;
  }


 /**
   * Builder method for elementType.
   *
   * @see IndexedElement#setElementType IndexedElement#setElementType for specification examples
   * @see IndexedElement#getElementType IndexedElement#getElementType for validation constraints
   * @param elementType a {@code String} parameter.
   * @return IndexedElement
  **/
  public IndexedElement elementType(String elementType) {
   this.elementType = elementType;
   return this;
  }

   /**
   * <p>Get elementType.</p>
   *
   * @return elementType
  **/
  @NotNull
  @ApiModelProperty(required = true, value = "")
  public String getElementType() {
    return elementType;
  }

 /**
   * Set elementType.
   *
   * 
   * 
   * @see #getElementType IndexedElement#getElementType for validation constraints
   * @param elementType a {@code String} parameter.
  **/
  public void setElementType(String elementType) {
    this.elementType = elementType;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IndexedElement indexedElement = (IndexedElement) o;
    return Objects.equals(this.id, indexedElement.id) &&
        Objects.equals(this.elementType, indexedElement.elementType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, elementType);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IndexedElement {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    elementType: ").append(toIndentedString(elementType)).append("\n");
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

