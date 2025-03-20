/*
 * The MIT License
 *
 * Copyright 2021 csanchez.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.plugins.validating_yaml_parameter;

import hudson.cli.CLICommand;
import hudson.model.Failure;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.stapler.StaplerRequest2;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author csanchez
 */
@ExtendWith(MockitoExtension.class)
class ValidatingYamlParameterDefinitionTest {

    @Mock
    private StaplerRequest2 req;

    @Mock
    private CLICommand cliCommand;

    @Test
    void simpleConfiguration() {
        ValidatingYamlParameterDefinition d = new ValidatingYamlParameterDefinition("DUMMY", "---\nkey1: value1\nkey2: value2\n", "msg", "yaml validation");
        assertEquals("DUMMY", d.getName());
        assertEquals("---\nkey1: value1\nkey2: value2\n", d.getDefaultValue());
        assertEquals("msg", d.getFailedValidationMessage());
        assertEquals("yaml validation", d.getDescription());
        Mockito.when(req.getParameterValues("DUMMY")).thenReturn(new String[]{"---\nkey1: value1\nkey2: value2\n"});
        assertEquals(new ValidatingYamlParameterValue("DUMMY", "---\nkey1: value1\nkey2: value2\n"), d.createValue(req));
        JSONObject jo = new JSONObject();
        ValidatingYamlParameterValue v = new ValidatingYamlParameterValue("DUMMY", "---\nkey1: value1\nkey2: value2\n");
        Mockito.when(req.bindJSON(ValidatingYamlParameterValue.class, jo)).thenReturn(v);
        assertEquals(v, d.createValue(req, jo));
    }

    @Test
    void failedCreateValueStapler() {
        ValidatingYamlParameterDefinition d = new ValidatingYamlParameterDefinition("DUMMY", "---\nkey1: value1: value2", "yaml syntax error", "description");
        Mockito.when(req.getParameterValues("DUMMY")).thenReturn(new String[]{"---\nkey1: value1: value2\n"});
        assertThrows(Failure.class, () -> d.createValue(req));
    }

    @Test
    void failedCreateValueJSONObject() {
        ValidatingYamlParameterDefinition d = new ValidatingYamlParameterDefinition("DUMMY", "---\nkey1: value1: value2\n", "yaml syntax error", "Description");
        ValidatingYamlParameterValue v = new ValidatingYamlParameterValue("DUMMY", "---\nkey1: value1: value2");
        JSONObject jo = new JSONObject();
        Mockito.when(req.bindJSON(ValidatingYamlParameterValue.class, jo)).thenReturn(v);
        assertThrows(Failure.class, () -> d.createValue(req, jo));
    }

}
