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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author csanchez
 */
class ValidationResultTest {

    @Test
    void testDefaultConstructor() {
        ValidationResult result = new ValidationResult();
        assertFalse(result.getResult());
        assertNull(result.getError());
    }

    @Test
    void testSetAndGetResult() {
        ValidationResult result = new ValidationResult();
        result.setResult(true);
        assertTrue(result.getResult());

        result.setResult(false);
        assertFalse(result.getResult());
    }

    @Test
    void testSetAndGetError() {
        ValidationResult result = new ValidationResult();
        String errorMessage = "Test error message";
        result.setError(errorMessage);
        assertEquals(errorMessage, result.getError());

        result.setError(null);
        assertNull(result.getError());
    }

    @Test
    void testTypicalUsageScenario() {
        ValidationResult result = new ValidationResult();
        // Initial state
        assertFalse(result.getResult());
        assertNull(result.getError());

        // Successful validation
        result.setResult(true);
        assertTrue(result.getResult());

        // Failed validation
        result.setResult(false);
        result.setError("Validation failed");
        assertFalse(result.getResult());
        assertEquals("Validation failed", result.getError());
    }

    @Test
    void testErrorHandlingWithNull() {
        ValidationResult result = new ValidationResult();
        result.setError(null);
        assertNull(result.getError());
    }

    @Test
    void testResultStateTransitions() {
        ValidationResult result = new ValidationResult();

        // Test multiple state transitions
        result.setResult(true);
        assertTrue(result.getResult());

        result.setResult(false);
        assertFalse(result.getResult());

        result.setResult(true);
        assertTrue(result.getResult());
    }

    @Test
    void testErrorMessageUpdate() {
        ValidationResult result = new ValidationResult();

        result.setError("First error");
        assertEquals("First error", result.getError());

        result.setError("Updated error");
        assertEquals("Updated error", result.getError());
    }
}
