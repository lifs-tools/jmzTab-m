/*
 *
 */
package de.isas.lipidomics.jmztabm.io;

import de.isas.mztab1_1.model.ValidationMessage;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public interface ParsingResult {
    ValidationMessage getValidationMessage();
}
