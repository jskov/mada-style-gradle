# mada-style-gradle

A Gradle plugin that enables and configures plugins for java development as I prefer it (for dk.mada code).

If you are in disagreement about the style, you are most welcome to not use it.

This plugin can be removed without risking anything more than the style shifting away from my preferences.

## Activated plugins

**Formatter**
* [Spotless](https://plugins.gradle.org/plugin/com.diffplug.spotless): Enforces the eclipse-base dk.mada code formatter

**Null-checker**
* [ErrorProne](https://plugins.gradle.org/plugin/net.ltgt.errorprone): Error checking framework using [NullAway](https://github.com/uber/NullAway)

## Configuration

Properties in gradle.properties allows some configuration of the plugin.
This allows the plugin to be removed without breaking the build.

The options are (shows here with their default value):

* dk.mada.style.formatter.enabled = true  
Boolean flag allowing the formatter to be disabled



## Development

For testing snapshot builds in other projects:

```console
$ ./gradlew -t publishToMavenLocal
```

Got building a version used for self-check:

```console
$ ./gradlew -Pversion=0.0.1 publishToMavenLocal
```
