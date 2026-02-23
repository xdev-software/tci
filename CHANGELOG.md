# 3.2.0
* Deprecated `SeleniumRecordingExtension`
  * Use `SeleniumRecorder` and `FileSystemFriendlyName` instead

# 3.1.0
* `EntityManagerController`
  * Now always checks if the EntityManager is already closed
    * This was changed because the Jakarta EE does not specify how an already closed EntityManager should behave
      * for example Hibernate doesn't care when you call call close multiple times as it checks for that while EclipseLink does not and crashes
  * Removed `closeEntityManagerOnCleanupWithoutCheck`
* Updated dependencies

# 3.0.3
* `EntityManagerController`
  * Make it possible to configure `closeEntityManagerOnCleanupWithoutCheck`

# 3.0.2
* Fix doubled values for `ContainerMemory`

# 3.0.1
* Fixated OIDC Server Mock version to latest minor (`1.2`) to prevent possible future random test failures
* Updated demo / integration tests framework

# 3.0.0
* Updated to Spring Boot 4.x
* Updated to Spring 7.x
* Updated to Hibernate 7.x
* Updated to Jakarta Persistence 3.2.0
* `db-jdbc`
  * Deprecated `PersistenceConfigurationCompat` use `jakarta.persistence.PersistenceConfiguration` instead
* `db-jdbc-spring-orm`
  * Adopt and replace `MutablePersistenceUnitInfo` with `SpringPersistenceUnitInfo`
* Demo's are not fully working because the underlying framework is not available for Spring Boot 4.x yet

# 2.9.5
* Updated dependencies

# 2.9.4
* Selenium
  * Hide download popup by default in Firefox so that UI is not blocked
  * Use default framerate (15FPS)
* Updated dependencies

# 2.9.3
* Updated dependencies

# 2.9.2
* Updated dependencies

# 2.9.1
* Updated dependencies

# 2.9.0
* Update to Testcontainers v2

# 2.8.2
* Updated dependencies

# 2.8.1
* Updated dependencies

# 2.8.0
* ``selenium``
  * Moved warmUp code to correct factory
  * Warmup will try to download/pre-pull the recording container image if required (as long as `recordingMode != SKIP`)
  * Behavior can be customized with method `withPullVideoRecordingContainerOnWarmUp`

# 2.7.2
* `LazyNetwork`: Use explicitly defined `ExecutorService`
* Updated dependencies

# 2.7.1
* Readd missing whitespace in `TCITracer`

# 2.7.0
* ExecutorService creation is now controlled centrally (`ExecutorServiceCreator`)
  * All created ExecutorServices now use `VirtualThread`s on Java 21+
* Use explicitly defined `ExecutorService` - wherever possible
  * _Context_: `CompletableFuture#runAsync`, `CompletableFuture#supplyAsync` and `parallelStream` use Java's common pool.<br/>
  However calling these methods is usually done (in TCI) for I/O tasks.<br/>
  This might exhaust the common pool thus negatively impacting performance.<br/>
  It was therefore decided to use dedicated pools instead.
  * Stored in `TCIExecutorServiceHolder`
  * Utilizes a `CachedThreadPool` (for Java `<` 21)
    * On Java21+ it uses `VirtualThread`s for better scaling
* Other minor improvements
  * Add missing timeout when pulling `SeleniumRecordingContainer`
  * Removed uses of `String.replaceAll("<Regex>", "")` and compiled pattern instead only once
* Updated dependencies

# 2.6.0
* ``db-jdbc-spring-*``
  * Added ``DynamicPersistenceClassFinder``
    * Contains pre-defined methods for adding entities like ``withSearchForPersistenceClasses``
    * Supersedes ``DynamicClassFinder``
  * JAR file urls are no longer added automatically for scanning
    * It's recommended to use ``DynamicPersistenceClassFinder`` as this is roughly 5-10x faster compared to Hibernate's JAR url scanning
    * If you still require the default scanning use ``.withAddJarFileUrls(true)``
* Updated dependencies

# 2.5.2
* Fix ``AnnotatedClassFinder`` returning the annotation and not the annotated class #373
* Removed deprecated ``CachedEntityAnnotatedClassNameFinder``

# 2.5.1
* Use timeout in ``SafeNamedContainerStarter#tryCleanupContainerAfterStartFail`` to prevent app/thread hang #370

# 2.5.0
* Improved overall error handling and logging when unexpected errors occur during the start of infra
  * Improved retrying in ``BaseTCIFactory``. Now unexpected problems that require a retry are logged/reported.
    * ``BaseTCIFactory#setGetNewTryCount`` was renamed to ``setGetNewTryCount``
  * Added timeout for ``PreStartableTCIFactory#makeExposedPortsFix``
    * defaults to ``90s``
  * ``WaitableJDBCContainer`` increase default timeout (overall timeout was ``30s``, now at least ``60s``)
    * This also now scales with the ``cpuSlownessFactor``
  * Log when a commited image is being snapshoted and report lock changes
* Updated dependencies

# 2.4.1
* Fixed random "recursive update during ServiceLoading" exception #342

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
        * Other implementation like CRIU may be available in the future once Docker adds support for them

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
