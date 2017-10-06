/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.isas.lipidomics.jmztabm.io;

import de.isas.lipidomics.jmztabm.model.MzTabFile;
import java.util.List;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public interface MzTabParseResult {
    MzTabFile getFile();
    List<ParsingResult> getParsingResults();
}
