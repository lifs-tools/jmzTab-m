//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.02.11 um 10:13:18 AM CET 
//


package info.psidev.cvmapping;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
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
 *       &lt;attribute name="cvIdentifierRef" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" /&gt;
 *       &lt;attribute name="termAccession" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="termName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="useTermName" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="useTerm" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="allowChildren" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="isRepeatable" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "CvTerm")
public class CvTerm {

    @XmlAttribute(name = "cvIdentifierRef", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected CvReference cvIdentifierRef;
    @XmlAttribute(name = "termAccession", required = true)
    protected String termAccession;
    @XmlAttribute(name = "termName", required = true)
    protected String termName;
    @XmlAttribute(name = "useTermName")
    protected Boolean useTermName;
    @XmlAttribute(name = "useTerm", required = true)
    protected boolean useTerm;
    @XmlAttribute(name = "allowChildren", required = true)
    protected boolean allowChildren;
    @XmlAttribute(name = "isRepeatable")
    protected Boolean isRepeatable;

    /**
     * Ruft den Wert der cvIdentifierRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public CvReference getCvIdentifierRef() {
        return cvIdentifierRef;
    }

    /**
     * Legt den Wert der cvIdentifierRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setCvIdentifierRef(CvReference value) {
        this.cvIdentifierRef = value;
    }

    /**
     * Ruft den Wert der termAccession-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTermAccession() {
        return termAccession;
    }

    /**
     * Legt den Wert der termAccession-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTermAccession(String value) {
        this.termAccession = value;
    }

    /**
     * Ruft den Wert der termName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTermName() {
        return termName;
    }

    /**
     * Legt den Wert der termName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTermName(String value) {
        this.termName = value;
    }

    /**
     * Ruft den Wert der useTermName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUseTermName() {
        if (useTermName == null) {
            return false;
        } else {
            return useTermName;
        }
    }

    /**
     * Legt den Wert der useTermName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUseTermName(Boolean value) {
        this.useTermName = value;
    }

    /**
     * Ruft den Wert der useTerm-Eigenschaft ab.
     * 
     */
    public boolean isUseTerm() {
        return useTerm;
    }

    /**
     * Legt den Wert der useTerm-Eigenschaft fest.
     * 
     */
    public void setUseTerm(boolean value) {
        this.useTerm = value;
    }

    /**
     * Ruft den Wert der allowChildren-Eigenschaft ab.
     * 
     */
    public boolean isAllowChildren() {
        return allowChildren;
    }

    /**
     * Legt den Wert der allowChildren-Eigenschaft fest.
     * 
     */
    public void setAllowChildren(boolean value) {
        this.allowChildren = value;
    }

    /**
     * Ruft den Wert der isRepeatable-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIsRepeatable() {
        if (isRepeatable == null) {
            return true;
        } else {
            return isRepeatable;
        }
    }

    /**
     * Legt den Wert der isRepeatable-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsRepeatable(Boolean value) {
        this.isRepeatable = value;
    }

}
