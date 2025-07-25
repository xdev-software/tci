# 2.4.0
* ``oidc-server-mock``
  * Improved extensibility by creating abstract base classes

# 2.3.0
* Modularized ``db-jdbc-orm`` into ``db-jdbc``, ``db-jdbc-spring-orm`` and ``db-jdbc-spring-orm-hibernate`` #330
  * Packages might be slightly different
  * In case of doubt migrate to ``db-jdbc-spring-orm-hibernate``
* Deprecated ``CachedEntityAnnotatedClassNameFinder`` use ``DynamicClassFinder`` instead

# 2.2.4
* ``oidc-server-mock``
  * Split ``addUser`` into better customizable methods

# 2.2.3
* ``oidc-server-mock``
  * Make it easier to define extend from ``OIDCTCIFactory``
* Updated ``org.springframework`` to latest version

# 2.2.2
* Use ``ConcurrentHashMap`` instead of ``Collections.synchronizedMap(new HashMap<>())`` to prevent ``ConcurrentModification`` in recursive ``computeIfAbsent`` calls

# 2.2.1
* Improve default leak detection stop timeout

# 2.2.0
* Leak-Detection: Automatically wait until infra is stopped #308
  * This should no longer require you to manually implement a ``LeakDetectionAsyncReaper`` and ``REAP_CFS``
* Made it possible to configure default leak-detection with environment variables and properties
* Updated dependencies

# 2.1.1
* Selenium (Docker) 4.34+: Correctly detect and replace cdp/bidiUrl (was ``127.0.0.1``, now ``localhost``)
* Updated dependencies

# 2.1.0
* ``CommitedImageSnapshotManager``
  * ``waitForFirstSnapshot``
    * Waits for the first snapshot to be created
    * Significantly reduces resource usage and prevents bottlenecks in most use cases
    * Enabled by default
  * ``commitedImagePrefix``
    * Let's you control the prefix of the commited/snapshotted image
    * Default value: ``commited-cache``
* Minor performance improvements

# 2.0.4
* Made some constants public

# 2.0.3
* Should fix that the bom is not being deployed (likely central-publishing-maven-plugin bug)

# 2.0.2
* Fix bom using incorrect groupId

# 2.0.1
* Various fixes regarding the release workflow

# 2.0.0
> [!WARNING]
> This release contains breaking changes

* ⚠ Renamed ``tci-base`` to ``tci``
* ⚠ Moved maven coordinates ``software.xdev:tci-base`` to ``software.xdev.tci:base``
* Added common modules, like selenium, db-jdbc-orm, mockserver, ... #208
  * Updated demo accordingly
* Added ``EnvironmentPerformance`` which currently tracks ``cpuSlownessFactor``
* Refactoring and code cleanup

# 1.2.0
* [PreStart] Make it possible to "snapshot" containers and use these snapshots to speed up subsequent containers
    * Recommended for containers that highly depend on storage (e.g. databases)
        * Example: MariaDB startup time with database migration
            * Without snapshot: ~10s
            * With snapshot: ~5s (~50% faster)
    * Implementations:
        * [``docker container commit``](https://docs.docker.com/reference/cli/docker/container/commit/)
            * Only snapshots storage - no in-memory data, processes, etc.
            * Volumes are not snapshoted due to [limitations](https://github.com/moby/moby/issues/43190) in Docker
        * Other implementation like [CRIU](https://criu.org) may be available in the future once Docker adds support for them

# 1.1.3
* Migrated deployment to _Sonatype Maven Central Portal_ [#155](https://github.com/xdev-software/standard-maven-template/issues/155)

# 1.1.2
* Update docs
* Fix ``ContainerMemory#M8G`` being the same as ``ContainerMemory#M4G``

# 1.1.1
* Added ``ContainerMemory`` utility class as it's needed in most projects
* Updated dependencies

# 1.1.0
* Port fixation 
    * can now be disabled
    * now also respects non-TCP ports (e.g. UDP)
    * acquires free ports in batches (previously each port was acquired individually)
* [Demo] Explicit database dialect is no longer required for connection-less start

# 1.0.5
* Updated dependencies
* [Demo] Use [SSE](https://github.com/xdev-software/spring-security-extras) to minimize code

# 1.0.4
* Updated dependencies

# 1.0.3
* Fix ``ConcurrentModificationException`` due to missing synchronized blocks
* Don't warmUp already warmed up factories again on coordinator level
* Correctly register factories to ``GlobalPreStartCoordinator``
* Document ``warmUp``
* Updated dependencies

# 1.0.2
* Fix unlikely modification error "Accept exceeded fixed size of ..." during warm-up
* Updated dependencies

# 1.0.1 
* Make it possible to disable agents
* Improved D&D (docs and demo)

# 1.0.0
_Initial release_
