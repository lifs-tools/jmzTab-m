/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.formats;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.isas.lipidomics.jmztabm.io.serialization.ContactSerializer;
import de.isas.lipidomics.jmztabm.io.serialization.SmallMoleculeSummarySerializer;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
@JsonSerialize(using = SmallMoleculeSummarySerializer.class)
public abstract class SmallMoleculeSummaryFormat {

}
