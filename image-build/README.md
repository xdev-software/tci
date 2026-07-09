# Image-Build

Provides common utility and configuration to build images.

## Config

<details><summary>The configuration is dynamically loaded from (sorted by highest priority)</summary>

* Environment variables 
    * prefixed with ``TCI_IMAGE-BUILD_`` 
    * all properties are in UPPERCASE and use `_` instead of `.` or `-`
* System properties
    * prefixed with ``tci.image-build.``

</details>

<details><summary>Full list of configuration options</summary>

_Please note that the preconfigured values usually work out of the box.<br/>_
_You should know exactly what you're doing when doing modifications._

| Property | Type | Default | Notes |
| --- | --- | --- | --- |
| `delete-on-exit` | `bool` | `false` | Should the image be deleted on exit? |
| `logger-for-build-prefix` | `string` | `container.build.` | Prefix used for the build logger |
| `cache-from` | `string` | - | Only applies to BuildKit (native) build.<br/> See [Docker docs](https://docs.docker.com/build/cache/backends/) for details. |
| `cache-to` | `string` | - | Only applies to BuildKit (native) build.<br/> See [Docker docs](https://docs.docker.com/build/cache/backends/) for details. |

</details>


