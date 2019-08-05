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
package de.isas.lipidomics.mztab2.validation;

import de.isas.mztab2.model.MzTab;
import de.isas.mztab2.model.MzTabTest;
import de.isas.mztab2.model.ValidationMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author nilshoffmann
 */
public class MzTabValidatorTest {

    /**
     * Test of validate method, of class MzTabValidator.
     */
    @Test
    public void testValidate_MzTab() {
        MzTab mzTab = MzTabTest.createTestMzTab();
        MzTabValidator instance = new MzTabValidator(new Validator<MzTab>() {
        });
        List<ValidationMessage> result = instance.validate(mzTab);
        assertEquals(0, result.size());
    }

    /**
     * Test of validate method, of class MzTabValidator.
     */
    @Test
    public void testValidate_3args() {
        MzTab mzTab = MzTabTest.createTestMzTab();
        ValidationMessage.MessageTypeEnum validationLevel = ValidationMessage.MessageTypeEnum.INFO;
        List<Validator<MzTab>> validators = Arrays.asList(new Validator<MzTab>() {
            @Override
            public List<ValidationMessage> validate(MzTab t) {
                List<ValidationMessage> messages = new ArrayList<>();
                
                ValidationMessage message = new ValidationMessage();
                message.code(1 + "").message("just testing validator 1").messageType(ValidationMessage.MessageTypeEnum.INFO);
                messages.add(message);
                return messages;
            }
         ;
        
        }, new Validator<MzTab>() {
            @Override
            public List<ValidationMessage> validate(MzTab t) {
                List<ValidationMessage> messages = new ArrayList<>();
                ValidationMessage message = new ValidationMessage();
                message.code(2 + "").message("just testing validator 2").messageType(ValidationMessage.MessageTypeEnum.ERROR);
                messages.add(message);
                return messages;
            }
        ;

        });
        List<ValidationMessage> result = MzTabValidator.validate(mzTab, validationLevel, validators.toArray(new Validator[validators.size()]));
        assertEquals(2, result.size());
        assertEquals(1 + "", result.get(0).getCode());
        assertEquals(2 + "", result.get(1).getCode());
    }

}
