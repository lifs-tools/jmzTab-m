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
import de.isas.mztab2.model.Assay;
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
 * Specification of study_variable.
(empty) name: A name for each study variable (experimental condition or factor), to serve as a list of the study variables that MUST be reported in the following tables. For software that does not capture study variables, a single study variable MUST be reported, linking to all assays. This single study variable MUST have the identifier “undefined“.
assay_refs: Bar-separated references to the IDs of assays grouped in the study variable.
average_function: The function used to calculate the study variable quantification value and the operation used is not arithmetic mean (default) e.g. “geometric mean”, “median”. The 1-n refers to different study variables.
variation_function: The function used to calculate the study variable quantification variation value if it is reported and the operation used is not coefficient of variation (default) e.g. “standard error”.
description: A textual description of the study variable.
factors: Additional parameters or factors, separated by bars, that are known about study variables allowing the capture of more complex, such as nested designs.  

 * 
 *
 * <p>mzTab-M specification example(s):</p>
 * <pre><code>MTD	study_variable[1]	control
MTD	study_variable[1]-assay_refs	assay[1]| assay[2]| assay[3]
MTD	study_variable-average_function	[MS, MS:1002883, median, ]
MTD	study_variable-variation_function	[MS, MS:1002885, standard error, ]
MTD	study_variable[1]-description	Group B (spike-in 0.74 fmol/uL)
MTD	study_variable[1]-factors	[,,time point, 1 minute]|[,,rapamycin dose,0.5mg]
MTD	study_variable[2]	1 minute 0.5mg rapamycin
</code></pre>
 * 
 *
 */
@ApiModel(description = "Specification of study_variable. (empty) name: A name for each study variable (experimental condition or factor), to serve as a list of the study variables that MUST be reported in the following tables. For software that does not capture study variables, a single study variable MUST be reported, linking to all assays. This single study variable MUST have the identifier “undefined“. assay_refs: Bar-separated references to the IDs of assays grouped in the study variable. average_function: The function used to calculate the study variable quantification value and the operation used is not arithmetic mean (default) e.g. “geometric mean”, “median”. The 1-n refers to different study variables. variation_function: The function used to calculate the study variable quantification variation value if it is reported and the operation used is not coefficient of variation (default) e.g. “standard error”. description: A textual description of the study variable. factors: Additional parameters or factors, separated by bars, that are known about study variables allowing the capture of more complex, such as nested designs.   ")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-02-11T15:12:05.057+01:00")
@XmlRootElement(name = "StudyVariable")
@XmlAccessorType(XmlAccessType.FIELD)
@JacksonXmlRootElement(localName = "StudyVariable")
public class StudyVariable extends IndexedElement {

  /**
   * Property enumeration for StudyVariable.
   */
  public static enum Properties {
      name("name"), 
      assayRefs("assay_refs"), 
      averageFunction("average_function"), 
      variationFunction("variation_function"), 
      description("description"), 
      factors("factors");

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
  @JsonProperty("assay_refs")
  // Is a container wrapped=false
  // items.name=assayRefs items.baseName=assayRefs items.xmlName= items.xmlNamespace=
  // items.example= items.type=Assay
  @XmlElement(name = "assayRefs")
  private List<Assay> assayRefs = null;
  @JsonProperty("average_function")
  @JacksonXmlProperty(localName = "average_function")
  @XmlElement(name = "average_function")
  private Parameter averageFunction = null;
  @JsonProperty("variation_function")
  @JacksonXmlProperty(localName = "variation_function")
  @XmlElement(name = "variation_function")
  private Parameter variationFunction = null;
  @JsonProperty("description")
  @JacksonXmlProperty(localName = "description")
  @XmlElement(name = "description")
  private String description = null;
  @JsonProperty("factors")
  // Is a container wrapped=false
  // items.name=factors items.baseName=factors items.xmlName= items.xmlNamespace=
  // items.example= items.type=Parameter
  @XmlElement(name = "factors")
  private List<Parameter> factors = null;
  
  @Override
  public StudyVariable id(Integer id) {
   super.setId(id);
   return this;
  }
  
  @Override
  public StudyVariable elementType(String elementType) {
   super.setElementType(elementType);
   return this;
  }

 /**
   * Builder method for name.
   *
   * @see StudyVariable#setName StudyVariable#setName for specification examples
   * @see StudyVariable#getName StudyVariable#getName for validation constraints
   * @param name a {@code String} parameter.
   * @return StudyVariable
  **/
  public StudyVariable name(String name) {
   this.name = name;
   return this;
  }

   /**
   * The study variable name.
   *
   * @return name
  **/
  @NotNull
  @ApiModelProperty(required = true, value = "The study variable name.")
  public String getName() {
    return name;
  }

 /**
   * Set name.
   *
   * 
   * 
   * @see #getName StudyVariable#getName for validation constraints
   * @param name a {@code String} parameter.
  **/
  public void setName(String name) {
    this.name = name;
  }


 /**
   * Builder method for assayRefs.
   *
   * @see StudyVariable#setAssayRefs StudyVariable#setAssayRefs for specification examples
   * @see StudyVariable#getAssayRefs StudyVariable#getAssayRefs for validation constraints
   * @param assayRefs a {@code List<Assay>} parameter.
   * @return StudyVariable
  **/
  public StudyVariable assayRefs(List<Assay> assayRefs) {
   this.assayRefs = assayRefs;
   return this;
  }

  /**
   * Add a single assayRefsItem to the assayRefs collection.
   *
   * @see StudyVariable#getAssayRefs StudyVariable#getAssayRefs for validation constraints
   * @param assayRefsItem a {@code Assay} parameter.
   * @return StudyVariable
   */
  public StudyVariable addAssayRefsItem(Assay assayRefsItem) {
    if (this.assayRefs == null) {
      this.assayRefs = new ArrayList<>();
    }
    this.assayRefs.add(assayRefsItem);
    return this;
  }

   /**
   * The assays referenced by this study variable.
   *
   * @return assayRefs
  **/
  @Valid
  @ApiModelProperty(value = "The assays referenced by this study variable.")
  public List<Assay> getAssayRefs() {
    return assayRefs;
  }

 /**
   * Set assayRefs.
   *
   * 
   * 
   * @see #getAssayRefs StudyVariable#getAssayRefs for validation constraints
   * @param assayRefs a {@code List<Assay>} parameter.
  **/
  public void setAssayRefs(List<Assay> assayRefs) {
    this.assayRefs = assayRefs;
  }


 /**
   * Builder method for averageFunction.
   *
   * @see StudyVariable#setAverageFunction StudyVariable#setAverageFunction for specification examples
   * @see StudyVariable#getAverageFunction StudyVariable#getAverageFunction for validation constraints
   * @param averageFunction a {@code Parameter} parameter.
   * @return StudyVariable
  **/
  public StudyVariable averageFunction(Parameter averageFunction) {
   this.averageFunction = averageFunction;
   return this;
  }

   /**
   * The function used to calculate summarised small molecule quantities over the assays referenced by this study variable.
   *
   * @return averageFunction
  **/
  @Valid
  @ApiModelProperty(value = "The function used to calculate summarised small molecule quantities over the assays referenced by this study variable.")
  public Parameter getAverageFunction() {
    return averageFunction;
  }

 /**
   * Set averageFunction.
   *
   * 
   * 
   * @see #getAverageFunction StudyVariable#getAverageFunction for validation constraints
   * @param averageFunction a {@code Parameter} parameter.
  **/
  public void setAverageFunction(Parameter averageFunction) {
    this.averageFunction = averageFunction;
  }


 /**
   * Builder method for variationFunction.
   *
   * @see StudyVariable#setVariationFunction StudyVariable#setVariationFunction for specification examples
   * @see StudyVariable#getVariationFunction StudyVariable#getVariationFunction for validation constraints
   * @param variationFunction a {@code Parameter} parameter.
   * @return StudyVariable
  **/
  public StudyVariable variationFunction(Parameter variationFunction) {
   this.variationFunction = variationFunction;
   return this;
  }

   /**
   * The function used to calculate the variation of small molecule quantities over the assays referenced by this study variable.
   *
   * @return variationFunction
  **/
  @Valid
  @ApiModelProperty(value = "The function used to calculate the variation of small molecule quantities over the assays referenced by this study variable.")
  public Parameter getVariationFunction() {
    return variationFunction;
  }

 /**
   * Set variationFunction.
   *
   * 
   * 
   * @see #getVariationFunction StudyVariable#getVariationFunction for validation constraints
   * @param variationFunction a {@code Parameter} parameter.
  **/
  public void setVariationFunction(Parameter variationFunction) {
    this.variationFunction = variationFunction;
  }


 /**
   * Builder method for description.
   *
   * @see StudyVariable#setDescription StudyVariable#setDescription for specification examples
   * @see StudyVariable#getDescription StudyVariable#getDescription for validation constraints
   * @param description a {@code String} parameter.
   * @return StudyVariable
  **/
  public StudyVariable description(String description) {
   this.description = description;
   return this;
  }

   /**
   * A free-form description of this study variable.
   *
   * @return description
  **/
  @ApiModelProperty(value = "A free-form description of this study variable.")
  public String getDescription() {
    return description;
  }

 /**
   * Set description.
   *
   * 
   * 
   * @see #getDescription StudyVariable#getDescription for validation constraints
   * @param description a {@code String} parameter.
  **/
  public void setDescription(String description) {
    this.description = description;
  }


 /**
   * Builder method for factors.
   *
   * @see StudyVariable#setFactors StudyVariable#setFactors for specification examples
   * @see StudyVariable#getFactors StudyVariable#getFactors for validation constraints
   * @param factors a {@code List<Parameter>} parameter.
   * @return StudyVariable
  **/
  public StudyVariable factors(List<Parameter> factors) {
   this.factors = factors;
   return this;
  }

  /**
   * Add a single factorsItem to the factors collection.
   *
   * @see StudyVariable#getFactors StudyVariable#getFactors for validation constraints
   * @param factorsItem a {@code Parameter} parameter.
   * @return StudyVariable
   */
  public StudyVariable addFactorsItem(Parameter factorsItem) {
    if (this.factors == null) {
      this.factors = new ArrayList<>();
    }
    this.factors.add(factorsItem);
    return this;
  }

   /**
   * Parameters indicating which factors were used for the assays referenced by this study variable, and at which levels.
   *
   * @return factors
  **/
  @Valid
  @ApiModelProperty(value = "Parameters indicating which factors were used for the assays referenced by this study variable, and at which levels.")
  public List<Parameter> getFactors() {
    return factors;
  }

 /**
   * Set factors.
   *
   * 
   * 
   * @see #getFactors StudyVariable#getFactors for validation constraints
   * @param factors a {@code List<Parameter>} parameter.
  **/
  public void setFactors(List<Parameter> factors) {
    this.factors = factors;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StudyVariable studyVariable = (StudyVariable) o;
    return Objects.equals(this.name, studyVariable.name) &&
        Objects.equals(this.assayRefs, studyVariable.assayRefs) &&
        Objects.equals(this.averageFunction, studyVariable.averageFunction) &&
        Objects.equals(this.variationFunction, studyVariable.variationFunction) &&
        Objects.equals(this.description, studyVariable.description) &&
        Objects.equals(this.factors, studyVariable.factors) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, assayRefs, averageFunction, variationFunction, description, factors, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StudyVariable {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    assayRefs: ").append(toIndentedString(assayRefs)).append("\n");
    sb.append("    averageFunction: ").append(toIndentedString(averageFunction)).append("\n");
    sb.append("    variationFunction: ").append(toIndentedString(variationFunction)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    factors: ").append(toIndentedString(factors)).append("\n");
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

