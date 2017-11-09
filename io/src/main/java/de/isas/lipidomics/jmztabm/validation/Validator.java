package de.isas.lipidomics.jmztabm.validation;

import de.isas.mztab1_1.model.MzTab;
import de.isas.mztab1_1.model.ValidationMessage;
import java.util.List;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public interface Validator {
    public List<ValidationMessage> validate(MzTab mzTab);
}
