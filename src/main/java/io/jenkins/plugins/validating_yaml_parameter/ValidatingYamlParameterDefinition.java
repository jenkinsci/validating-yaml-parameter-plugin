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
import hudson.Extension;
import hudson.cli.CLICommand;
import hudson.model.Failure;
import hudson.model.Item;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.security.Permission;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;
import org.kohsuke.stapler.StaplerRequest;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.LoaderOptions;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author csanchez
 */

public class ValidatingYamlParameterDefinition extends ParameterDefinition{

    private static final long serialVersionUID = 900032072543915L;

    private static final Logger LOGGER = Logger.getLogger(ValidatingYamlParameterDefinition.class.getName());
    private String defaultValue;
    private String failedValidationMessage;
    private static boolean result;

    private String value;

    @DataBoundConstructor
    public ValidatingYamlParameterDefinition(String name, String defaultValue, String failedValidationMessage, String description) {
        super(name, description);
        this.defaultValue = defaultValue;
        this.failedValidationMessage = failedValidationMessage;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public String getValue() {
        return this.defaultValue;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public String getDescription() {
        return super.getDescription();
    }

    public String getFailedValidationMessage() {
        return failedValidationMessage;
    }

    @Override
    public ValidatingYamlParameterValue getDefaultParameterValue() {
        ValidatingYamlParameterValue v = new ValidatingYamlParameterValue(getName(), defaultValue);
        return v;
    }

    private static ValidationResult doCheckYaml(String value) {
        ValidationResult vres = new ValidationResult();
        LoaderOptions options = new LoaderOptions();
        SafeConstructor constructor = new SafeConstructor(options);
        Yaml yaml = new Yaml(constructor);
        try {
            yaml.load(value);
            vres.setResult(true);
        } catch (Exception e) {
            vres.setResult(false);
            vres.setError(e.toString());
        }
        return vres;
    }

    @Extension @Symbol("validatingYamlParameter")
    public static class DescriptorImpl extends ParameterDescriptor {

        @Override
        public String getDisplayName() {
            return "Validating Yaml Parameter";
        }

        /**
         *  Check yaml syntax
         */
        @POST
        public FormValidation doValidate(
                @QueryParameter("value") final String value,
                @QueryParameter("failedValidationMessage") final String failedValidationMessage,
                @AncestorInPath Item item
                ) {

            if (item == null) {
                return FormValidation.ok();
            }
            item.checkPermission(Permission.CONFIGURE);

            ValidationResult vres = doCheckYaml(value);
            if(vres.result) {
                return FormValidation.ok();
            }
            else {
                return failedValidationMessage == null || "".equals(failedValidationMessage)
                        ? FormValidation.error("Invalid yaml string: " + vres.error)
                        : FormValidation.error(failedValidationMessage);
            }
        }
    }

    @Override
    public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
        ValidatingYamlParameterValue value = req.bindJSON(ValidatingYamlParameterValue.class, jo);
        String req_value = value.getValue();
        ValidationResult vres = doCheckYaml(req_value);

        if (!vres.result) {
            throw new Failure("Req: Invalid YAML syntax for parameter [" + getName() + "] specified: " + req_value);
        }

        return value;
    }

    @Override
    public ParameterValue createValue(StaplerRequest req) {
        String[] value = req.getParameterValues(getName());

        if (value == null || value.length < 1) {
            return getDefaultParameterValue();
        } else {
            ValidationResult vres = doCheckYaml(value[0]);
            if (!vres.result) {
                throw new Failure("Req: Invalid value for parameter [" + getName() + "] specified: " + value[0]);
            }
            return new ValidatingYamlParameterValue(getName(), value[0]);
        }
    }

    @Override
    public ParameterValue createValue(CLICommand command, String value) throws IOException, InterruptedException {
        if (value == null || value.length() == 0) {
            return getDefaultParameterValue();
        } else {
            ValidationResult vres = doCheckYaml(value);
            if (!vres.result) {
                throw new AbortException("Invalid value for parameter [" + getName() + "] specified: " + value);
            }
            return new ValidatingYamlParameterValue(getName(), value, failedValidationMessage);
        }
    }

    @Override
    public ParameterDefinition copyWithDefaultValue(ParameterValue defaultValue) {
        if (defaultValue instanceof ValidatingYamlParameterValue) {
            ValidatingYamlParameterValue value = (ValidatingYamlParameterValue) defaultValue;
            return new ValidatingYamlParameterDefinition(getName(), value.value, getFailedValidationMessage(), getDescription());
        } else {
            return this;
        }
    }
}
