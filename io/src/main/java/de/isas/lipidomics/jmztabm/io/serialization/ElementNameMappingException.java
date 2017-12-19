/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.serialization;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public final class ElementNameMappingException extends RuntimeException {

    public ElementNameMappingException(Object element) {
        super("No mzTab element name mapping available for " + element.
            getClass().
            getName());
    }

}
