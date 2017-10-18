/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.isas.lipidomics.jmztabm.io;

import de.isas.mztab1_1.model.Parameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class MzTabWriterTest {
    
    @Test
    void testParameterToString() {
        Parameter p = new Parameter().cvLabel("MS").cvAccession("MS:100179").name("made up for testing").value(null);
        String s = MzTabWriter.parameterToString(p);
        System.out.println(s);
        String expected = "[MS, MS:100179, made up for testing, ]";
        Assertions.assertEquals(expected, s);
        p.value("some value");
        s = MzTabWriter.parameterToString(p);
        System.out.println(s);
        expected = "[MS, MS:100179, made up for testing, some value]";
        Assertions.assertEquals(expected, s);
    }
    
    @Test
    void testMtdParameterToMzTabLine() {
        Parameter p = new Parameter().cvLabel("MS").cvAccession("MS:100179").name("made up for testing").value(null);
        String s = MzTabWriter.parameterToString(p);
        String actual = MzTabWriter.mtdParameterToMzTabLine(1, "sample_processing", p);
        String expected = "MTD\tsample_processing[1]\t"+s+"\n\r";
        System.out.println(actual);
        Assertions.assertEquals(expected, actual);
    }
    
    @Test
    void testMtdParameterToMzTabLineException() {
        Parameter p = new Parameter().cvLabel("MS").cvAccession("MS:100179").name("made up for testing").value(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {String actual = MzTabWriter.mtdParameterToMzTabLine(0, "sample_processing", p);});
    }
}
