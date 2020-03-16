# KFile
Kotlin multi-platform file library.

[![Coverage Status](https://coveralls.io/repos/boennemann/badges/badge.svg)](https://coveralls.io/r/boennemann/badges)

[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)

Dependency Info (Maven Central): `com.pajato.io:KFile-<target>:0.3.2` where targets are `jvm` and `native`

Provides a file abstraction that mimics a Java File object for the JVM and native platforms (Windows, Linux, Mac)

Version 0.3.2 upgrades to support Kotlin 1.3.70 and adds a builder for a file URL input: `createKFileWithUrl(url: String)`

Complete testing is provided.  JVM code coverage is 100%.  Native code coverage is a work in progress by JetBrains.

PR's are welcome.

Note: the next version after 0.0.2 is 0.3.0.  This was a mistake, one that will not be rectified nor rationalized. The
version following 0.3.0 is 0.3.2 as 0.3.1 was never released.
