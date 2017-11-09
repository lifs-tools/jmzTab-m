/*
 *
 */
package de.isas.lipidomics.jmztabm.io;

import de.isas.mztab1_1.model.MzTab;
import java.util.List;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public interface MzTabParseResult {
    MzTab getFile();
    List<ParsingResult> getParsingResults();
}
