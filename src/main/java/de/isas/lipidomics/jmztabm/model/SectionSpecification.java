package de.isas.lipidomics.jmztabm.model;

import java.util.Map;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public interface SectionSpecification {
    public String getPrefix();
    public String getDescription();
    public Map<String, ColumnSpec> getColumns();
}
