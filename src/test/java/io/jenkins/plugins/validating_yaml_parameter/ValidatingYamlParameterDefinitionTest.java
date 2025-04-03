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

import hudson.AbortException;
import hudson.cli.CLICommand;
import hudson.model.Failure;
import hudson.model.Item;
import hudson.model.StringParameterValue;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.stapler.StaplerRequest2;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

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

    @Test
    void testGetDefaultParameterValue() {
        ValidatingYamlParameterDefinition d = new ValidatingYamlParameterDefinition(
            "DUMMY", "default: value", "error", "description");
        ValidatingYamlParameterValue v = d.getDefaultParameterValue();
        assertEquals("DUMMY", v.getName());
        assertEquals("default: value", v.getValue());
    }

    @Test
    void testCreateValueWithNullCLICommand() throws IOException, InterruptedException {
        ValidatingYamlParameterDefinition d = new ValidatingYamlParameterDefinition(
            "DUMMY", "default: value", "error", "description");
        ValidatingYamlParameterValue v = (ValidatingYamlParameterValue) d.createValue(cliCommand, null);
        assertEquals(d.getDefaultParameterValue().getValue(), v.getValue());
    }

    @Test
    void testCreateValueWithEmptyCLICommand() throws IOException, InterruptedException {
        ValidatingYamlParameterDefinition d = new ValidatingYamlParameterDefinition(
            "DUMMY", "default: value", "error", "description");
        ValidatingYamlParameterValue v = (ValidatingYamlParameterValue) d.createValue(cliCommand, "");
        assertEquals(d.getDefaultParameterValue().getValue(), v.getValue());
    }

    @Test
    void testCreateValueWithValidCLICommand() throws IOException, InterruptedException {
        ValidatingYamlParameterDefinition d = new ValidatingYamlParameterDefinition(
            "DUMMY", "default: value", "error", "description");
        String yaml = "key: value";
        ValidatingYamlParameterValue v = (ValidatingYamlParameterValue) d.createValue(cliCommand, yaml);
        assertEquals(yaml, v.getValue());
    }

    @Test
    void testCreateValueWithInvalidCLICommand() {
        ValidatingYamlParameterDefinition d = new ValidatingYamlParameterDefinition(
            "DUMMY", "default: value", "error", "description");
        String invalidYaml = "key: : value";
        assertThrows(AbortException.class, () -> 
            d.createValue(cliCommand, invalidYaml)
        );
    }

    @Test
    void testCopyWithDefaultValue() {
        ValidatingYamlParameterDefinition d = new ValidatingYamlParameterDefinition(
            "DUMMY", "default: value", "error", "description");
        ValidatingYamlParameterValue v = new ValidatingYamlParameterValue("DUMMY", "new: value");
        ValidatingYamlParameterDefinition copied = (ValidatingYamlParameterDefinition) d.copyWithDefaultValue(v);

        assertEquals(d.getName(), copied.getName());
        assertEquals("new: value", copied.getDefaultValue());
        assertEquals(d.getFailedValidationMessage(), copied.getFailedValidationMessage());
        assertEquals(d.getDescription(), copied.getDescription());
    }

    @Test
    void testCopyWithNonMatchingDefaultValue() {
        ValidatingYamlParameterDefinition d = new ValidatingYamlParameterDefinition(
            "DUMMY", "default: value", "error", "description");
        StringParameterValue v = new StringParameterValue("DUMMY", "new value");
        ValidatingYamlParameterDefinition copied = (ValidatingYamlParameterDefinition) d.copyWithDefaultValue(v);

        assertSame(d, copied);
    }

    @Test
    void testDescriptorDisplayName() {
        ValidatingYamlParameterDefinition.DescriptorImpl descriptor = new ValidatingYamlParameterDefinition.DescriptorImpl();
        assertEquals("Validating Yaml Parameter", descriptor.getDisplayName());
    }

    @Test
    void testDescriptorValidateWithNullItem() {
        ValidatingYamlParameterDefinition.DescriptorImpl descriptor = new ValidatingYamlParameterDefinition.DescriptorImpl();
        FormValidation validation = descriptor.doValidate("key: value", "error", null);
        assertEquals(FormValidation.Kind.OK, validation.kind);
    }

    @Test
    void testDescriptorValidateWithValidYaml() {
        ValidatingYamlParameterDefinition.DescriptorImpl descriptor = new ValidatingYamlParameterDefinition.DescriptorImpl();
        Item item = mock(Item.class);
        FormValidation validation = descriptor.doValidate("key: value", "error", item);
        assertEquals(FormValidation.Kind.OK, validation.kind);
    }

    @Test
    void testDescriptorValidateWithInvalidYaml() {
        ValidatingYamlParameterDefinition.DescriptorImpl descriptor = new ValidatingYamlParameterDefinition.DescriptorImpl();
        Item item = mock(Item.class);
        FormValidation validation = descriptor.doValidate("key: : value", "Custom error", item);
        assertEquals(FormValidation.Kind.ERROR, validation.kind);
        assertEquals("Custom error", validation.getMessage());
    }

    @Test
    void testDescriptorValidateWithInvalidYamlNoCustomMessage() {
        ValidatingYamlParameterDefinition.DescriptorImpl descriptor = new ValidatingYamlParameterDefinition.DescriptorImpl();
        Item item = mock(Item.class);
        FormValidation validation = descriptor.doValidate("key: : value", "", item);
        assertEquals(FormValidation.Kind.ERROR, validation.kind);
        assertTrue(validation.getMessage().startsWith("Invalid yaml string:"));
    }

    @Test
    void testCreateValueWithNullParameterValues() {
        ValidatingYamlParameterDefinition d = new ValidatingYamlParameterDefinition(
            "DUMMY", "default: value", "error", "description");
        Mockito.when(req.getParameterValues("DUMMY")).thenReturn(null);
        ValidatingYamlParameterValue v = (ValidatingYamlParameterValue) d.createValue(req);
        assertEquals(d.getDefaultParameterValue().getValue(), v.getValue());
    }

    @Test
    void testCreateValueWithEmptyParameterValues() {
        ValidatingYamlParameterDefinition d = new ValidatingYamlParameterDefinition(
            "DUMMY", "default: value", "error", "description");
        Mockito.when(req.getParameterValues("DUMMY")).thenReturn(new String[0]);
        ValidatingYamlParameterValue v = (ValidatingYamlParameterValue) d.createValue(req);
        assertEquals(d.getDefaultParameterValue().getValue(), v.getValue());
    }

    @Test
    void testDescriptorValidateWithNullValue() {
        ValidatingYamlParameterDefinition.DescriptorImpl descriptor = new ValidatingYamlParameterDefinition.DescriptorImpl();
        Item item = mock(Item.class);
        FormValidation validation = descriptor.doValidate(null, "error", item);
        assertEquals(FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    void testGetValue() {
        ValidatingYamlParameterDefinition d = new ValidatingYamlParameterDefinition(
            "DUMMY", "default: value", "error", "description");
        assertEquals("default: value", d.getValue());
    }

    @Test
    void testDescriptorValidateWithComplexYaml() {
        ValidatingYamlParameterDefinition.DescriptorImpl descriptor = new ValidatingYamlParameterDefinition.DescriptorImpl();
        Item item = mock(Item.class);
        String complexYaml = "---\n" +
                           "key1: value1\n" +
                           "key2:\n" +
                           "  nested: value2\n" +
                           "  array:\n" +
                           "    - item1\n" +
                           "    - item2\n";
        FormValidation validation = descriptor.doValidate(complexYaml, "error", item);
        assertEquals(FormValidation.Kind.OK, validation.kind);
    }

    @Test
    void testDescriptorValidateWithInvalidComplexYaml() {
        ValidatingYamlParameterDefinition.DescriptorImpl descriptor = new ValidatingYamlParameterDefinition.DescriptorImpl();
        Item item = mock(Item.class);
        String invalidComplexYaml = "---\n" +
                                  "key1: value1\n" +
                                  "key2:\n" +
                                  "  nested: value2\n" +
                                  "  array:\n" +
                                  "    - item1\n" +
                                  "    - item2\n" +
                                  "  invalid: : value\n";
        FormValidation validation = descriptor.doValidate(invalidComplexYaml, "Custom error", item);
        assertEquals(FormValidation.Kind.ERROR, validation.kind);
        assertEquals("Custom error", validation.getMessage());
    }

    @Test
    void testCreateValueWithSpecialCharacters() throws IOException, InterruptedException {
        ValidatingYamlParameterDefinition d = new ValidatingYamlParameterDefinition(
            "DUMMY", "default: value", "error", "description");
        String yamlWithSpecialChars = "key: 'value with special chars: !@#$%^&*()'";
        ValidatingYamlParameterValue v = (ValidatingYamlParameterValue) d.createValue(cliCommand, yamlWithSpecialChars);
        assertEquals(yamlWithSpecialChars, v.getValue());
    }

    @Test
    void testCreateValueWithUnicodeCharacters() throws IOException, InterruptedException {
        ValidatingYamlParameterDefinition d = new ValidatingYamlParameterDefinition(
            "DUMMY", "default: value", "error", "description");
        String yamlWithUnicode = "key: 'value with unicode: 你好世界'";
        ValidatingYamlParameterValue v = (ValidatingYamlParameterValue) d.createValue(cliCommand, yamlWithUnicode);
        assertEquals(yamlWithUnicode, v.getValue());
    }

    @Test
    void testCreateValueWithComments() throws IOException, InterruptedException {
        ValidatingYamlParameterDefinition d = new ValidatingYamlParameterDefinition(
            "DUMMY", "default: value", "error", "description");
        String yamlWithComments = "# This is a comment\nkey: value # Inline comment";
        ValidatingYamlParameterValue v = (ValidatingYamlParameterValue) d.createValue(cliCommand, yamlWithComments);
        assertEquals(yamlWithComments, v.getValue());
    }

    @Test
    void testCreateValueWithEmptyOrNullInput() throws IOException, InterruptedException {
        ValidatingYamlParameterDefinition d = new ValidatingYamlParameterDefinition(
            "DUMMY", "default: value", "error", "description");

        // Test null CLI command
        ValidatingYamlParameterValue v = (ValidatingYamlParameterValue) d.createValue(cliCommand, null);
        assertEquals(d.getDefaultParameterValue().getValue(), v.getValue());

        // Test empty CLI command
        v = (ValidatingYamlParameterValue) d.createValue(cliCommand, "");
        assertEquals(d.getDefaultParameterValue().getValue(), v.getValue());

        // Test null parameter values
        Mockito.when(req.getParameterValues("DUMMY")).thenReturn(null);
        v = (ValidatingYamlParameterValue) d.createValue(req);
        assertEquals(d.getDefaultParameterValue().getValue(), v.getValue());

        // Test empty parameter values
        Mockito.when(req.getParameterValues("DUMMY")).thenReturn(new String[0]);
        v = (ValidatingYamlParameterValue) d.createValue(req);
        assertEquals(d.getDefaultParameterValue().getValue(), v.getValue());
    }
}
