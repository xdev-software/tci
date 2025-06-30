[![Latest version](https://img.shields.io/maven-central/v/software.xdev/tci?logo=apache%20maven)](https://mvnrepository.com/artifact/software.xdev/tci)
[![Build](https://img.shields.io/github/actions/workflow/status/xdev-software/tci/check-build.yml?branch=develop)](https://github.com/xdev-software/tci/actions/workflows/check-build.yml?query=branch%3Adevelop)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=xdev-software_tci&metric=alert_status)](https://sonarcloud.io/dashboard?id=xdev-software_tci)
[![javadoc](https://javadoc.io/badge2/software.xdev/tci/javadoc.svg)](https://javadoc.io/doc/software.xdev/tci) 

# <img src="./assets/logo.svg" height=28 > Testcontainers Infrastructure (TCI) Framework Base

Basis Module for XDEV's Testcontainer Infrastructure Framework

## Features
| Feature | Why? | Demo |
| --- | --- | --- |
| Easily create infrastructure using TCI<sup>[JD](https://javadoc.io/doc/software.xdev/base/latest/software/xdev/tci/TCI.html)</sup> (TestContainer Infrastructure) templating + Factories for that | Makes writing and designing tests easier | [here](./base-demo/src/test/java/software/xdev/tci/dummyinfra/) |
| [PreStarting mechanism](./base/src/main/java/software/xdev/tci/factory/prestart/)<sup>[JD](https://javadoc.io/doc/software.xdev/base/latest/software/xdev/tci/factory/prestart/PreStartableTCIFactory.html)</sup> for [additional performance](./PERFORMANCE.md) | Tries to run tests as fast as possible - with a few trade-offs | [here](./base-demo/src/test/java/software/xdev/tci/factory/prestart/) |
| All started containers have a unique human-readable name | Easier identification when tracing or debugging | [here](./base-demo/src/test/java/software/xdev/tci/safestart/) |
| An optimized [implementation of Network](./base/src/main/java/software/xdev/tci/network/)<sup>[JD](https://javadoc.io/doc/software.xdev/base/latest/software/xdev/tci/network/LazyNetwork.html)</sup> | Addresses various problems of the original implementation to speed up tests | [here](./base-demo/src/test/java/software/xdev/tci/network/) |
| [Safe starting of named containers](./base/src/main/java/software/xdev/tci/safestart/)<sup>[JD](https://javadoc.io/doc/software.xdev/base/latest/software/xdev/tci/safestart/SafeNamedContainerStarter.html)</sup> | Ensures that a container doesn't enter a crash loop during retried startups | [here](./base-demo/src/test/java/software/xdev/tci/safestart/) |
| [Container Leak detection](./base/src/main/java/software/xdev/tci/leakdetection/)¹ | Prevents you from running out of resources | [here](./base-demo/src/test/java/software/xdev/tci/leak/) |
| [Tracing](./base/src/main/java/software/xdev/tci/tracing/)¹ | Makes finding bottlenecks and similar problems easier | |

¹ = Active by default due to service loading

## Usage
Take a look at the [minimalistic demo](./base-demo/) that showcases the components individually.

You may also checkout the [advanced demo](./advanced-demo/) - a reference implementation of all features in a realistic project - to get a better feeling how this can be done.

> [!TIP]
> More detailed documentation is usually available in the corresponding [JavaDocs](https://javadoc.io/doc/software.xdev.tci/base).

## Installation
[Installation guide for the latest release](https://github.com/xdev-software/tci/releases/latest#Installation)

## Support
If you need support as soon as possible and you can't wait for any pull request, feel free to use [our support](https://xdev.software/en/services/support).

## Contributing
See the [contributing guide](./CONTRIBUTING.md) for detailed instructions on how to get started with our project.

## Dependencies and Licenses
View the [license of the current project](LICENSE) or the [summary including all dependencies](https://xdev-software.github.io/tci/dependencies)
