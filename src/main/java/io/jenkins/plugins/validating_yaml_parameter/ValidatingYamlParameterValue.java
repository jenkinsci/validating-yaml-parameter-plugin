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
import hudson.model.StringParameterValue;
import hudson.tasks.BuildWrapper;
import java.io.IOException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.LoaderOptions;

/**
 *
 * @author csanchez
 */
public class ValidatingYamlParameterValue extends StringParameterValue {

    private boolean syntaxHighlighting;
    private String failedValidationMessage;

    @DataBoundConstructor
    public ValidatingYamlParameterValue(String name, String value) {
        super(name, value);
    }

    public ValidatingYamlParameterValue(String name, String value, String failedValidationMessage) {
        super(name, value);
        this.failedValidationMessage = failedValidationMessage;
    }
    public ValidatingYamlParameterValue(String name, String value, String failedValidationMessage, String description) {
        super(name, value, description);
        this.failedValidationMessage = failedValidationMessage;
    }

    public String getFailedValidationMessage() {
        return failedValidationMessage;
    }

    @DataBoundSetter
    public void setFailedValidationMessage(String failedValidationMessage) {
        this.failedValidationMessage = failedValidationMessage;
    }

    private boolean doCheckYaml(String value) {
        LoaderOptions options = new LoaderOptions();
        SafeConstructor constructor = new SafeConstructor(options);
        Yaml yaml = new Yaml(constructor);
        try {
            yaml.load(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public BuildWrapper createBuildWrapper(AbstractBuild<?, ?> build) {
        if (!doCheckYaml(value)) {
            return new BuildWrapper() {
                @Override
                public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
                    throw new AbortException("Invalue value for parameter [" + getName() + "] specified: " + value);
                }
            };
        } else {
            return null;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 71;
        int result = super.hashCode();
        result = prime * result;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        ValidatingYamlParameterValue other = (ValidatingYamlParameterValue) obj;
        if (value == null) {
            return other.value == null;
        } else {
            return value.equals(other.value);
        }
    }

    @Override
    public String toString() {
        return "(ValidatingYamlParameterValue) " + getName() + "='" + value + "'";
    }
}
