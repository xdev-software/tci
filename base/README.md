# Base Module

Base for other modules and core components.

## Features

| Feature | Why? | Demo |
| --- | --- | --- |
| Easily create infrastructure using TCI<sup>[JD](https://javadoc.io/doc/software.xdev/base/latest/software/xdev/tci/TCI.html)</sup> (TestContainer Infrastructure) templating + Factories for that | Makes writing and designing tests easier | [here](../base-demo/src/test/java/software/xdev/tci/dummyinfra/) |
| [PreStarting mechanism](./src/main/java/software/xdev/tci/factory/prestart/)<sup>[JD](https://javadoc.io/doc/software.xdev/base/latest/software/xdev/tci/factory/prestart/PreStartableTCIFactory.html)</sup> for [additional performance](../PERFORMANCE.md) | Tries to run tests as fast as possible - with a few trade-offs | [here](../base-demo/src/test/java/software/xdev/tci/factory/prestart/) |
| All started containers have a unique human-readable name | Easier identification when tracing or debugging | [here](../base-demo/src/test/java/software/xdev/tci/safestart/) |
| An optimized [implementation of Network](./src/main/java/software/xdev/tci/network/)<sup>[JD](https://javadoc.io/doc/software.xdev/base/latest/software/xdev/tci/network/LazyNetwork.html)</sup> | Addresses various problems of the original implementation to speed up tests | [here](../base-demo/src/test/java/software/xdev/tci/network/) |
| [Safe starting of named containers](./src/main/java/software/xdev/tci/safestart/)<sup>[JD](https://javadoc.io/doc/software.xdev/base/latest/software/xdev/tci/safestart/SafeNamedContainerStarter.html)</sup> | Ensures that a container doesn't enter a crash loop during retried startups | [here](../base-demo/src/test/java/software/xdev/tci/safestart/) |
| [Container Leak detection](./src/main/java/software/xdev/tci/leakdetection/)¹ | Prevents you from running out of resources | [here](../base-demo/src/test/java/software/xdev/tci/leak/) |
| [Tracing](./src/main/java/software/xdev/tci/tracing/)¹ | Makes finding bottlenecks and similar problems easier | |

¹ = Active by default due to service loading

## Usage
Take a look at the [minimalistic demo](../base-demo/) that showcases the components individually.
