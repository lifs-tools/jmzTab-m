//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.02.11 um 10:13:18 AM CET 
//


package info.psidev.cvmapping;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="CvReferenceList"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{}CvReference" maxOccurs="unbounded"/&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name="cvSourceVersion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="CvMappingRuleList"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{}CvMappingRule" maxOccurs="unbounded"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="modelName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="modelURI" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="modelVersion" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "cvReferenceList",
    "cvMappingRuleList"
})
@XmlRootElement(name = "CvMapping")
public class CvMapping {

    @XmlElement(name = "CvReferenceList", required = true)
    protected CvMapping.CvReferenceList cvReferenceList;
    @XmlElement(name = "CvMappingRuleList", required = true)
    protected CvMapping.CvMappingRuleList cvMappingRuleList;
    @XmlAttribute(name = "modelName", required = true)
    protected String modelName;
    @XmlAttribute(name = "modelURI", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String modelURI;
    @XmlAttribute(name = "modelVersion", required = true)
    protected String modelVersion;

    /**
     * Ruft den Wert der cvReferenceList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CvMapping.CvReferenceList }
     *     
     */
    public CvMapping.CvReferenceList getCvReferenceList() {
        return cvReferenceList;
    }

    /**
     * Legt den Wert der cvReferenceList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CvMapping.CvReferenceList }
     *     
     */
    public void setCvReferenceList(CvMapping.CvReferenceList value) {
        this.cvReferenceList = value;
    }

    /**
     * Ruft den Wert der cvMappingRuleList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CvMapping.CvMappingRuleList }
     *     
     */
    public CvMapping.CvMappingRuleList getCvMappingRuleList() {
        return cvMappingRuleList;
    }

    /**
     * Legt den Wert der cvMappingRuleList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CvMapping.CvMappingRuleList }
     *     
     */
    public void setCvMappingRuleList(CvMapping.CvMappingRuleList value) {
        this.cvMappingRuleList = value;
    }

    /**
     * Ruft den Wert der modelName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Legt den Wert der modelName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModelName(String value) {
        this.modelName = value;
    }

    /**
     * Ruft den Wert der modelURI-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModelURI() {
        return modelURI;
    }

    /**
     * Legt den Wert der modelURI-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModelURI(String value) {
        this.modelURI = value;
    }

    /**
     * Ruft den Wert der modelVersion-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * Legt den Wert der modelVersion-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModelVersion(String value) {
        this.modelVersion = value;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element ref="{}CvMappingRule" maxOccurs="unbounded"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "cvMappingRule"
    })
    public static class CvMappingRuleList {

        @XmlElement(name = "CvMappingRule", required = true)
        protected List<CvMappingRule> cvMappingRule;

        /**
         * Single mapping rule between a specific elements of the model and to controlled vocabularies listed on the CvTerm element.Gets the value of the cvMappingRule property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the cvMappingRule property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCvMappingRule().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link CvMappingRule }
         * 
         * 
         */
        public List<CvMappingRule> getCvMappingRule() {
            if (cvMappingRule == null) {
                cvMappingRule = new ArrayList<CvMappingRule>();
            }
            return this.cvMappingRule;
        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element ref="{}CvReference" maxOccurs="unbounded"/&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="cvSourceVersion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "cvReference"
    })
    public static class CvReferenceList {

        @XmlElement(name = "CvReference", required = true)
        protected List<CvReference> cvReference;
        @XmlAttribute(name = "cvSourceVersion")
        @XmlSchemaType(name = "anySimpleType")
        protected String cvSourceVersion;

        /**
         * Description of a CV or ontology resource.Gets the value of the cvReference property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the cvReference property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCvReference().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link CvReference }
         * 
         * 
         */
        public List<CvReference> getCvReference() {
            if (cvReference == null) {
                cvReference = new ArrayList<CvReference>();
            }
            return this.cvReference;
        }

        /**
         * Ruft den Wert der cvSourceVersion-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCvSourceVersion() {
            return cvSourceVersion;
        }

        /**
         * Legt den Wert der cvSourceVersion-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCvSourceVersion(String value) {
            this.cvSourceVersion = value;
        }

    }

}
