/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.formats;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.isas.lipidomics.jmztabm.io.serialization.MetadataSerializer;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
@JsonSerialize(using = MetadataSerializer.class)
public abstract class MetadataFormat {

}
