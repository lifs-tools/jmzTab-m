/*
 * 
 */
package de.isas.lipidomics.jmztabm.validation;

import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.MzTab;
import de.isas.mztab1_1.model.SmallMoleculeEvidence;
import de.isas.mztab1_1.model.SmallMoleculeFeature;
import de.isas.mztab1_1.model.SmallMoleculeSummary;
import de.isas.mztab1_1.model.ValidationMessage;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class MzTabValidator implements Validator {

    @Override
    public List<ValidationMessage> validate(MzTab mzTab) {
        List<ValidationMessage> list = new LinkedList<>();
        
        List<SmallMoleculeSummary> sms = mzTab.getSmallMoleculeSummary();
        List<SmallMoleculeFeature> smf = mzTab.getSmallMoleculeFeature();
        List<SmallMoleculeEvidence> sme = mzTab.getSmallMoleculeEvidence();
        return list;
    }
    
    protected List<ValidationMessage> validateMetadata(MzTab mzTab) {
        List<ValidationMessage> validationMessages = new LinkedList<>();
        Metadata mtd = mzTab.getMetadata();
        return validationMessages;
    }
    
    protected List<ValidationMessage> validateSmallMoleculeSummary(MzTab mzTab) {
        List<ValidationMessage> validationMessages = new LinkedList<>();
        Metadata mtd = mzTab.getMetadata();
        return validationMessages;
    }
    
    protected List<ValidationMessage> validateSmallMoleculeFeature(MzTab mzTab) {
        List<ValidationMessage> validationMessages = new LinkedList<>();
        Metadata mtd = mzTab.getMetadata();
        return validationMessages;
    }
    
    protected List<ValidationMessage> validateSmallMoleculeEvidence(MzTab mzTab) {
        List<ValidationMessage> validationMessages = new LinkedList<>();
        Metadata mtd = mzTab.getMetadata();
        return validationMessages;
    }
}
