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
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 *
 * @author csanchez
 */
class ValidatingYamlParameterValueTest {

    @Test
    void equals() {
        ValidatingYamlParameterValue v = new ValidatingYamlParameterValue("DUMMY", "---\ntest1: value1\n", "error validating yaml");
        assertEquals(v, v);
    }

    @Test
    void testGetFailedValidationMessage() {
        String errorMsg = "test error message";
        ValidatingYamlParameterValue v = new ValidatingYamlParameterValue("DUMMY", "value", errorMsg);
        assertEquals(errorMsg, v.getFailedValidationMessage());
    }

    @Test
    void testSetFailedValidationMessage() {
        ValidatingYamlParameterValue v = new ValidatingYamlParameterValue("DUMMY", "value");
        String errorMsg = "new error message";
        v.setFailedValidationMessage(errorMsg);
        assertEquals(errorMsg, v.getFailedValidationMessage());
    }

    @Test
    void testCreateBuildWrapperWithValidYaml() {
        ValidatingYamlParameterValue v = new ValidatingYamlParameterValue("DUMMY", "key: value");
        assertNull(v.createBuildWrapper(null));
    }

    @Test
    void testCreateBuildWrapperWithInvalidYaml() throws Exception {
        ValidatingYamlParameterValue v = new ValidatingYamlParameterValue("DUMMY", "key: : value");
        BuildWrapper wrapper = v.createBuildWrapper(null);
        assertNotNull(wrapper);

        AbstractBuild<?, ?> build = mock(AbstractBuild.class);
        Launcher launcher = mock(Launcher.class);
        BuildListener listener = mock(BuildListener.class);

        assertThrows(AbortException.class, () ->
            wrapper.setUp(build, launcher, listener)
        );
    }

    @Test
    void testHashCode() {
        ValidatingYamlParameterValue v1 = new ValidatingYamlParameterValue("DUMMY", "key: value");
        ValidatingYamlParameterValue v2 = new ValidatingYamlParameterValue("DUMMY", "key: value");
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    void testEqualsWithDifferentValues() {
        ValidatingYamlParameterValue v1 = new ValidatingYamlParameterValue("DUMMY", "key1: value1");
        ValidatingYamlParameterValue v2 = new ValidatingYamlParameterValue("DUMMY", "key2: value2");
        assertNotEquals(v1, v2);
    }

    @Test
    void testToString() {
        ValidatingYamlParameterValue v = new ValidatingYamlParameterValue("DUMMY", "key: value");
        assertEquals("(ValidatingYamlParameterValue) DUMMY='key: value'", v.toString());
    }

    @Test
    void testEqualsAndHashCodeWithNull() {
        ValidatingYamlParameterValue v1 = new ValidatingYamlParameterValue("DUMMY", null);
        ValidatingYamlParameterValue v2 = new ValidatingYamlParameterValue("DUMMY", null);
        assertEquals(v1, v2);
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    void testEqualsAndHashCodeWithEmptyValue() {
        ValidatingYamlParameterValue v1 = new ValidatingYamlParameterValue("DUMMY", "");
        ValidatingYamlParameterValue v2 = new ValidatingYamlParameterValue("DUMMY", "");
        assertEquals(v1, v2);
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    void testCreateBuildWrapperWithVariousYamlFormats() {
        // Test valid complex YAML
        String complexYaml = "---\n" +
                           "key1: value1\n" +
                           "key2:\n" +
                           "  nested: value2\n" +
                           "  array:\n" +
                           "    - item1\n" +
                           "    - item2\n";
        ValidatingYamlParameterValue v = new ValidatingYamlParameterValue("DUMMY", complexYaml);
        assertNull(v.createBuildWrapper(null));

        // Test invalid complex YAML
        String invalidComplexYaml = complexYaml + "  invalid: : value\n";
        v = new ValidatingYamlParameterValue("DUMMY", invalidComplexYaml);
        BuildWrapper wrapper = v.createBuildWrapper(null);
        assertNotNull(wrapper);

        AbstractBuild<?, ?> build = mock(AbstractBuild.class);
        Launcher launcher = mock(Launcher.class);
        BuildListener listener = mock(BuildListener.class);

        assertThrows(AbortException.class, () ->
            wrapper.setUp(build, launcher, listener)
        );
    }

    @Test
    void testCreateBuildWrapperWithSpecialCharacters() {
        // Test special characters
        String yamlWithSpecialChars = "key: 'value with special chars: !@#$%^&*()'";
        ValidatingYamlParameterValue v = new ValidatingYamlParameterValue("DUMMY", yamlWithSpecialChars);
        assertNull(v.createBuildWrapper(null));

        // Test Unicode characters
        String yamlWithUnicode = "key: 'value with unicode: 你好世界'";
        v = new ValidatingYamlParameterValue("DUMMY", yamlWithUnicode);
        assertNull(v.createBuildWrapper(null));

        // Test empty lines
        String yamlWithEmptyLines = "key1: value1\n\n\nkey2: value2";
        v = new ValidatingYamlParameterValue("DUMMY", yamlWithEmptyLines);
        assertNull(v.createBuildWrapper(null));

        // Test comments
        String yamlWithComments = "# This is a comment\nkey: value # Inline comment";
        v = new ValidatingYamlParameterValue("DUMMY", yamlWithComments);
        assertNull(v.createBuildWrapper(null));
    }

    @Test
    void testCreateBuildWrapperWithEmptyLines() {
        String yamlWithEmptyLines = "key1: value1\n\n\nkey2: value2";
        ValidatingYamlParameterValue v = new ValidatingYamlParameterValue("DUMMY", yamlWithEmptyLines);
        assertNull(v.createBuildWrapper(null));
    }

    @Test
    void testCreateBuildWrapperWithComments() {
        String yamlWithComments = "# This is a comment\nkey: value # Inline comment";
        ValidatingYamlParameterValue v = new ValidatingYamlParameterValue("DUMMY", yamlWithComments);
        assertNull(v.createBuildWrapper(null));
    }

    @Test
    void testEqualsWithDifferentNames() {
        ValidatingYamlParameterValue v1 = new ValidatingYamlParameterValue("DUMMY1", "key: value");
        ValidatingYamlParameterValue v2 = new ValidatingYamlParameterValue("DUMMY2", "key: value");
        assertNotEquals(v1, v2);
    }

    @Test
    void testEqualsWithDifferentDescriptions() {
        ValidatingYamlParameterValue v1 = new ValidatingYamlParameterValue("DUMMY", "key: value", "error", "desc1");
        ValidatingYamlParameterValue v2 = new ValidatingYamlParameterValue("DUMMY", "key: value", "error", "desc2");
        assertEquals(v1, v2); // Description shouldn't affect equality
    }
}
