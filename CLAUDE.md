# Spawn Project — Claude Code Context

## Codebase Overview

Spawn is a Java 25 framework for programmatically launching and controlling processes, JVMs, and Docker containers. It provides a unified abstraction (`Platform` / `Application` / `Process`) over different execution environments. The core pattern: define a `Specification`, call `platform.launch(spec)`, get back an `Application` with `CompletableFuture`-based lifecycle hooks.

**Stack**: Java 25, Maven, Jackson, junixsocket, `build.base.*` and `build.codemodel.injection`

**Structure**: 8 Maven modules in a monorepo, each mapping to a JPMS module:
- `spawn-option` → shared option types
- `spawn-application` → core abstractions (Platform, Application, Process, Specification, Customizer)
- `spawn-application-composition` → multi-app topology management
- `spawn-jdk` → JDK launch abstractions + SpawnAgent (two-way JVM communication)
- `spawn-local-platform` → local OS process launcher (`LocalMachine`)
- `spawn-local-jdk` → JDK detection + `LocalJDKLauncher`
- `spawn-docker` → Docker Engine API interfaces
- `spawn-docker-jdk` → JDK HTTP Client-based Docker implementation (uses `java.net.http` + junixsocket)

For detailed architecture, see [docs/CODEBASE_MAP.md](docs/CODEBASE_MAP.md).

## Build

```bash
./mvnw clean install                    # build all modules + run tests
./mvnw clean install -pl spawn-docker-jdk  # build specific module
```

Tests requiring Docker are gated by `@EnabledIf("isDockerAvailable")`. The `spawn-docker-jdk` module requires `--enable-native-access=ALL-UNNAMED` (configured in surefire).

## Key Conventions

- All option types are immutable with static `of(...)` factories and `@Default` annotated defaults
- `Customizer` inner classes on `Application` interfaces are auto-discovered and applied at launch
- Launcher registry: `META-INF/<PlatformClassName>` properties files map `Application=Launcher`
- Checkstyle enforced: no tabs, no star imports, final locals, no asserts, braces required
