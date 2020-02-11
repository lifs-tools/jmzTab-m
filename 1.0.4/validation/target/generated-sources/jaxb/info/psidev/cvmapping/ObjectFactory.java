//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.02.11 um 10:13:18 AM CET 
//


package info.psidev.cvmapping;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the info.psidev.cvmapping package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: info.psidev.cvmapping
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CvMappingRule }
     * 
     */
    public CvMappingRule createCvMappingRule() {
        return new CvMappingRule();
    }

    /**
     * Create an instance of {@link CvMapping }
     * 
     */
    public CvMapping createCvMapping() {
        return new CvMapping();
    }

    /**
     * Create an instance of {@link CvMapping.CvReferenceList }
     * 
     */
    public CvMapping.CvReferenceList createCvMappingCvReferenceList() {
        return new CvMapping.CvReferenceList();
    }

    /**
     * Create an instance of {@link CvMapping.CvMappingRuleList }
     * 
     */
    public CvMapping.CvMappingRuleList createCvMappingCvMappingRuleList() {
        return new CvMapping.CvMappingRuleList();
    }

    /**
     * Create an instance of {@link CvTerm }
     * 
     */
    public CvTerm createCvTerm() {
        return new CvTerm();
    }

    /**
     * Create an instance of {@link CvReference }
     * 
     */
    public CvReference createCvReference() {
        return new CvReference();
    }

}
