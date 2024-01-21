# mada-style-gradle

A Gradle plugin that enables and configures plugins for java development as I prefer it (for dk.mada code).

If you are in disagreement about the style, you are most welcome to not use it.

This plugin can be removed without risking anything more than the style shifting away from my preferences.

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


For null-checker annotations, you should add the [JSpecify](https://jspecify.dev/) dependency:

    compileOnly    "org.jspecify:jspecify:0.3.0"

## Based on plugins

* [Checkstyle](https://docs.gradle.org/current/userguide/checkstyle_plugin.html): Style checking framework
* [ErrorProne](https://plugins.gradle.org/plugin/net.ltgt.errorprone): Error checking framework
* [NullAway](https://github.com/uber/NullAway): ErrorProne plugin
* [Sonar](https://docs.sonarsource.com/sonarqube/latest/analyzing-source-code/scanners/sonarscanner-for-gradle/): SonarScanner
* [Spotless](https://plugins.gradle.org/plugin/com.diffplug.spotless): Enforces the eclipse-base dk.mada code formatter


## Configuration

Properties in gradle.properties allows configuration of the plugin.
Using properties (instead of DSL) allows the plugin to be removed without breaking the build.

The options are (shows here with their default value).

**Checkstyle**

By default uses [this configuration](./src/main/resources/config/checkstyle/checkstyle-mada.xml)

* `dk.mada.style.checkstyle.enabled = true`  
 Boolean flag allowing the checkstyle checker to be disabled
* `dk.mada.style.checkstyle.ignore-generated-source = false`  
 Boolean flag to control scanning of test source files
* `dk.mada.style.checkstyle.ignore-test-source = false`  
 Boolean flag to control scanning of generated source files
* `dk.mada.style.checkstyle.config-path = null`  
 Optional path to an alternative checkstyle configuration file


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
* `dk.mada.style.formatter.include = src/main/java/**/*.java`  
 Ant-style include pattern for files to format
* `dk.mada.style.formatter.exclude = `  
 Ant-style exclude pattern for files to not format
* `dk.mada.style.formatter.eclipse-config-path = null`  
 Optional path to an alternative eclipse formatter configuration file

**Null-checker**

Note that this is a plugin to ErrorProne, so is also affected by errorprone configuration keys.

* `dk.mada.style.null-checker.enabled = true`  
 Boolean flag allowing the null-checker to be disabled
* `dk.mada.style.null-checker.include-packages = dk`  
 Comma-separated list of packages to be scanned (will include sub-packages)
* `dk.mada.style.null-checker.exclude-packages = `  
 Comma-separated list of packages to be excluded from scanning

**Sonar**

All properties (except `enabled`) are simply passed on to the [Sonar plugin](https://docs.sonarsource.com/sonarqube/latest/analyzing-source-code/analysis-parameters/), thus allowing configuration without DSL.

** `dk.mada.style.sonar.enabled = true`  
 Boolean flag allowing sonar to be disabled
** `dk.mada.style.sonar.host.url = https://sonarcloud.io`  
 The sonar cloud host address
** `dk.mada.style.sonar.sourceEncoding = UTF-8`  
 The source encoding

## Development

For testing snapshot builds in other projects:

```console
$ ./gradlew -t publishToMavenLocal
```

Got building a version used for self-check:

```console
$ ./gradlew -Pversion=0.0.1 publishToMavenLocal
```
