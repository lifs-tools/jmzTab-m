/*
 * Copyright 2019 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.pride.jmztab2.utils.errors;

import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author nils.hoffmann
 */
public class MZTabErrorTypeMapTest {

    /**
     * Test of getType method, of class MZTabErrorTypeMap.
     */
    @Test
    public void testGetType() {
        MZTabErrorTypeMap instance = new MZTabErrorTypeMap();
        
        for(MZTabErrorType formatErrorType: FormatErrorType.VALUES) {
            assertEquals(formatErrorType, instance.getType(formatErrorType.getCode()));
        }
        for(MZTabErrorType logicalErrorType:LogicalErrorType.VALUES) {
            assertEquals(logicalErrorType, instance.getType(logicalErrorType.getCode()));
        }
        for(MZTabErrorType crossCheckErrorType:CrossCheckErrorType.VALUES) {
            assertEquals(crossCheckErrorType, instance.getType(crossCheckErrorType.getCode()));
        }
    }

    /**
     * Test of getTypeMap method, of class MZTabErrorTypeMap.
     */
    @Test
    public void testGetTypeMap() {
        MZTabErrorTypeMap instance = new MZTabErrorTypeMap();
        Map<Integer, MZTabErrorType> result = instance.getTypeMap();
        assertEquals(95, result.size());
    }
    
}
