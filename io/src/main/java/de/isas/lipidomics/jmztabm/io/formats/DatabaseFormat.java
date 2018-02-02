/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.formats;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.isas.lipidomics.jmztabm.io.serialization.DatabaseSerializer;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
@JsonSerialize(using = DatabaseSerializer.class)
public abstract class DatabaseFormat {

}
