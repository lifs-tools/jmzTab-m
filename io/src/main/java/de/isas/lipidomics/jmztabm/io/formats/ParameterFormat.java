/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.formats;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.isas.lipidomics.jmztabm.io.serialization.ParameterSerializer;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
@JsonSerialize(using = ParameterSerializer.class)
public abstract class ParameterFormat {

}
