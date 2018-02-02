/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.formats;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.isas.lipidomics.jmztabm.io.serialization.SampleSerializer;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
@JsonSerialize(using = SampleSerializer.class)
public abstract class SampleFormat {

}
