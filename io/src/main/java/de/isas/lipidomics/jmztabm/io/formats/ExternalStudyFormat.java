/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.formats;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.isas.lipidomics.jmztabm.io.serialization.ContactSerializer;
import de.isas.lipidomics.jmztabm.io.serialization.ExternalStudySerializer;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
@JsonSerialize(using = ExternalStudySerializer.class)
public abstract class ExternalStudyFormat {

}
