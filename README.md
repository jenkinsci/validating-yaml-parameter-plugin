# validating-yaml-parameter-plugin

The validating yaml parameter plugin contributes a new parameter type to Jenkins that allows Yaml
validation of the entered contents.

Usage
=====

This parameter plugin is used in the job configuration page by enabling parameterized builds.

You just need to configure the parameter by entering a name, default value (yaml), failed validation
message and the parameter description:

![](docs/images/validating-yaml-parameter-img1.jpg)

When a build is requested, the user is prompted with the parameters to enter. If the user types bad
yaml contents, then there's a validation error return after the form is submitted.

![](docs/images/validating-yaml-parameter-img2.jpg)
