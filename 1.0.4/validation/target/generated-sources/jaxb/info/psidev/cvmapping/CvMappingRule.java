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
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


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
 *         &lt;element ref="{}CvTerm" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="scopePath" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="cvElementPath" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="cvTermsCombinationLogic" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *             &lt;enumeration value="OR"/&gt;
 *             &lt;enumeration value="AND"/&gt;
 *             &lt;enumeration value="XOR"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="requirementLevel" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *             &lt;enumeration value="MUST"/&gt;
 *             &lt;enumeration value="SHOULD"/&gt;
 *             &lt;enumeration value="MAY"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "cvTerm"
})
@XmlRootElement(name = "CvMappingRule")
public class CvMappingRule {

    @XmlElement(name = "CvTerm", required = true)
    protected List<CvTerm> cvTerm;
    @XmlAttribute(name = "scopePath", required = true)
    protected String scopePath;
    @XmlAttribute(name = "cvElementPath", required = true)
    protected String cvElementPath;
    @XmlAttribute(name = "cvTermsCombinationLogic", required = true)
    protected CvMappingRule.CvTermsCombinationLogic cvTermsCombinationLogic;
    @XmlAttribute(name = "requirementLevel", required = true)
    protected CvMappingRule.RequirementLevel requirementLevel;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    /**
     * One or more terms from a CVsource that are allowed to be associated with the given element of the model. Gets the value of the cvTerm property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cvTerm property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCvTerm().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CvTerm }
     * 
     * 
     */
    public List<CvTerm> getCvTerm() {
        if (cvTerm == null) {
            cvTerm = new ArrayList<CvTerm>();
        }
        return this.cvTerm;
    }

    /**
     * Ruft den Wert der scopePath-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScopePath() {
        return scopePath;
    }

    /**
     * Legt den Wert der scopePath-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScopePath(String value) {
        this.scopePath = value;
    }

    /**
     * Ruft den Wert der cvElementPath-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCvElementPath() {
        return cvElementPath;
    }

    /**
     * Legt den Wert der cvElementPath-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCvElementPath(String value) {
        this.cvElementPath = value;
    }

    /**
     * Ruft den Wert der cvTermsCombinationLogic-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CvMappingRule.CvTermsCombinationLogic }
     *     
     */
    public CvMappingRule.CvTermsCombinationLogic getCvTermsCombinationLogic() {
        return cvTermsCombinationLogic;
    }

    /**
     * Legt den Wert der cvTermsCombinationLogic-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CvMappingRule.CvTermsCombinationLogic }
     *     
     */
    public void setCvTermsCombinationLogic(CvMappingRule.CvTermsCombinationLogic value) {
        this.cvTermsCombinationLogic = value;
    }

    /**
     * Ruft den Wert der requirementLevel-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CvMappingRule.RequirementLevel }
     *     
     */
    public CvMappingRule.RequirementLevel getRequirementLevel() {
        return requirementLevel;
    }

    /**
     * Legt den Wert der requirementLevel-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CvMappingRule.RequirementLevel }
     *     
     */
    public void setRequirementLevel(CvMappingRule.RequirementLevel value) {
        this.requirementLevel = value;
    }

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }


    /**
     * <p>Java-Klasse für null.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * <p>
     * <pre>
     * &lt;simpleType&gt;
     *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
     *     &lt;enumeration value="OR"/&gt;
     *     &lt;enumeration value="AND"/&gt;
     *     &lt;enumeration value="XOR"/&gt;
     *   &lt;/restriction&gt;
     * &lt;/simpleType&gt;
     * </pre>
     * 
     */
    @XmlType(name = "")
    @XmlEnum
    public enum CvTermsCombinationLogic {

        OR,
        AND,
        XOR;

        public String value() {
            return name();
        }

        public static CvMappingRule.CvTermsCombinationLogic fromValue(String v) {
            return valueOf(v);
        }

    }


    /**
     * <p>Java-Klasse für null.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * <p>
     * <pre>
     * &lt;simpleType&gt;
     *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
     *     &lt;enumeration value="MUST"/&gt;
     *     &lt;enumeration value="SHOULD"/&gt;
     *     &lt;enumeration value="MAY"/&gt;
     *   &lt;/restriction&gt;
     * &lt;/simpleType&gt;
     * </pre>
     * 
     */
    @XmlType(name = "")
    @XmlEnum
    public enum RequirementLevel {

        MUST,
        SHOULD,
        MAY;

        public String value() {
            return name();
        }

        public static CvMappingRule.RequirementLevel fromValue(String v) {
            return valueOf(v);
        }

    }

}
