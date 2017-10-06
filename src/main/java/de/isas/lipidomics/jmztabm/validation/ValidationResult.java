package de.isas.lipidomics.jmztabm.validation;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public interface ValidationResult {
    public static enum Type{ SYNTACTIC, SEMANTIC};
    
    public int getRowNumber();
}
