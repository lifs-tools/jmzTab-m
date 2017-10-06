package de.isas.lipidomics.jmztabm.model;

import java.util.Map;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public interface MzTabRecord {
    String getKey();
    Map<String, ColumnSpec> getColumnSpecs();
    <T> T getValue(ColumnSpec columnSpec, Class<? extends T> typeClass);
    String toString();
}
