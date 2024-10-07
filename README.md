# mada-style-gradle
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jskov_mada-style-gradle&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=jskov_mada-style-gradle)
[![Reproducible Builds](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/jvm-repo-rebuild/reproducible-central/master/content/dk/mada/style/mada-style-gradle/badge.json)](https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/dk/mada/style/mada-style-gradle/README.md)

A Gradle plugin that enables and configures a number of code style and quality plugins for java development.

This plugin is a low-friction way of improving you code quality:

* enables a range of plugins with no clutter added to the build file (assuming the convention configurations are agreeable)
* configured entirely via entries in `gradle.properties`, so disabling/removing the plugin does not break the build file

## Using the Plugin

Add plugin activation to the build file header (substitute the relevant version):

    plugins {
        ...
        id 'dk.mada.style' version '1.n.n'
    }

And make sure the plugin can be fetched from MavenCentral:

    pluginManagement {
        repositories {
            gradlePluginPortal()
            mavenCentral()
        }
    }

### Null-checking

For null-checker annotations, you should add the [JSpecify](https://jspecify.dev/) dependency:

    compileOnly    "org.jspecify:jspecify:1.0.0"

Note that the annotations are not as easy to remove as the plugin (since they will be spread out over various source files).

### Removing the Plugin

Just remove the apply-line in `build.gradle` to disable or remove all plugin activity.

Remove all the `dk.mada.style.`-prefixed entries in `gradle.properties` if you want to the remove the plugin for good.


## Sub-plugins

* [Checkstyle](https://docs.gradle.org/current/userguide/checkstyle_plugin.html): Style checking framework
* [ErrorProne](https://plugins.gradle.org/plugin/net.ltgt.errorprone): Error checking framework
* [NullAway](https://github.com/uber/NullAway): ErrorProne plugin
* [Sonar](https://docs.sonarsource.com/sonarqube/latest/analyzing-source-code/scanners/sonarscanner-for-gradle/): SonarScanner
* [Spotless](https://plugins.gradle.org/plugin/com.diffplug.spotless): Enforces the eclipse-base dk.mada code formatter


## Configuration

Properties in `gradle.properties` allow configuration of the sub-plugins.

Using properties (instead of DSL) allows the plugin (and sub-plugins) to be removed without breaking the build.
(Assuming that you do not add sub-plugin DSL configuration elements to the build file.)

The options are (shows here with their default value).

**Checkstyle**

By default uses [this configuration](./src/main/resources/config/checkstyle/checkstyle-mada.xml)

* `dk.mada.style.checkstyle.enabled = true`  
 Boolean flag allowing the checkstyle checker to be disabled
* `dk.mada.style.checkstyle.includes = `  
 Comma-separated Ant-style include patterns for files to check
* `dk.mada.style.checkstyle.excludes = `  
 Comma-separated Ant-style exclude patterns for files to not check
* `dk.mada.style.checkstyle.ignore-generated-source = false`  
 Boolean flag to control scanning of test source files
* `dk.mada.style.checkstyle.ignore-test-source = false`  
 Boolean flag to control scanning of generated source files
* `dk.mada.style.checkstyle.config-path = null`  
 Optional path to an alternative checkstyle configuration file  
 This can be a URL; the content will be downloaded and cached (so if you want to update the content, you must change the URL)


**ErrorProne**

* `dk.mada.style.errorprone.enabled = true`  
 Boolean flag allowing the error prone checker to be disabled
* `dk.mada.style.errorprone.ignore-generated-source = false`  
 Boolean flag to control scanning of generated source files  
 Note: works poorly with Immutable generated sources (as they cannot be referenced from the main sources when enabled)
* `dk.mada.style.errorprone.ignore-test-source = false`  
 Boolean flag to control scanning of test source files
* `dk.mada.style.errorprone.excluded-paths-regexp = `  
 Optional regular expression used to exclude files from scanning

**Formatter (Spotless)**

By default uses [this configuration](./src/main/resources/config/spotless/eclipse-formatter-mada.xml)

* `dk.mada.style.formatter.enabled = true`  
 Boolean flag allowing the formatter to be disabled
* `dk.mada.style.formatter.include = src/main/java/**/*.java, src/test/java/**/*.java`  
 Comma-separated Ant-style include patterns for files to format
* `dk.mada.style.formatter.exclude = `  
 Comma-separated Ant-style exclude patterns for files to not format
* `dk.mada.style.formatter.eclipse-config-path = null`  
 Optional path to an alternative eclipse formatter configuration file
 This can be a URL; the content will be downloaded and cached (so if you want to update the content, you must change the URL)
* `dk.mada.style.formatter.eclipse-429-p2-url = null`
 Optional URL to a P2 update repository containing Eclipse 4.29 (aka 2023.09) plugins.

**Null-checker**

Note that this is a plugin to ErrorProne, so is also affected by errorprone configuration keys.

* `dk.mada.style.null-checker.enabled = true`  
 Boolean flag allowing the null-checker to be disabled
* `dk.mada.style.null-checker.include-packages = dk`  
 Comma-separated list of packages to be scanned (will include sub-packages)
* `dk.mada.style.null-checker.exclude-packages = `  
 Comma-separated list of packages to be excluded from scanning
* `dk.mada.style.null-checker.exclude-field-annotations = javafx.fxml.FXML, org.junit.jupiter.api.io.TempDir`  
 Comma-separated list of classes that are assumed to inject (non-null) value into fields


**Sonar**

All properties (except `enabled`) are simply passed on to the [Sonar plugin](https://docs.sonarsource.com/sonarqube/latest/analyzing-source-code/analysis-parameters/), thus allowing configuration without DSL.

* `dk.mada.style.sonar.enabled = true`  
 Boolean flag allowing sonar to be disabled
* `dk.mada.style.sonar.host.url = https://sonarcloud.io`  
 The sonar cloud host address
* `dk.mada.style.sonar.sourceEncoding = UTF-8`  
 The source encoding

## Development

For testing snapshot builds in other projects:

```console
$ ./gradlew -t publishToMavenLocal -Pversion=0.0.1
```
