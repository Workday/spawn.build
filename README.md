# spawn
A Java framework for programmatically launching and controlling processes, JVMs, and Docker containers.

[![CI](https://github.com/Workday/spawn.build/actions/workflows/main-pull-request.yml/badge.svg)](https://github.com/Workday/spawn.build/actions/workflows/main-pull-request.yml)
[![Maven Central](https://img.shields.io/maven-central/v/build.spawn/spawn-application)](https://central.sonatype.com/artifact/build.spawn/spawn-application)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

## Overview

`spawn` provides a unified abstraction over local processes, JVMs, and Docker containers. Define a
`Specification`, call `platform.launch(spec)`, and get back an `Application` with
`CompletableFuture`-based lifecycle hooks — regardless of the underlying execution environment.

## Modules

| Module | Purpose |
|--------|---------|
| `spawn-option` | Shared option types |
| `spawn-application` | Core abstractions: Platform, Application, Process, Specification, Customizer |
| `spawn-application-composition` | Multi-application topology management |
| `spawn-jdk` | JDK launch abstractions and SpawnAgent for two-way JVM communication |
| `spawn-local-platform` | Local OS process launcher (`LocalMachine`) |
| `spawn-local-jdk` | JDK detection and `LocalJDKLauncher` |
| `spawn-docker` | Docker Engine API interfaces |
| `spawn-docker-jdk` | JDK HTTP Client-based Docker implementation (unix socket via junixsocket) |

## Requirements

- Java 25+
- Maven (wrapper included — no separate install needed)
- Docker (only required for `spawn-docker` / `spawn-docker-jdk` modules)

## Using this Library

Add individual modules as dependencies. All modules share the same version:

```xml
<dependency>
    <groupId>build.spawn</groupId>
    <artifactId>spawn-application</artifactId>
    <version>VERSION</version>
</dependency>
```

Replace `VERSION` with the latest version shown in the Maven Central badge above.

When using the Docker modules, add the following JVM flag (Java 25+):

```
--enable-native-access=ALL-UNNAMED
```

On macOS and Linux, access to the Docker socket may require a permissions adjustment:

```bash
sudo chmod 660 ~/Library/Containers/com.docker.docker/Data/docker.raw.sock
sudo chmod 660 /var/run/docker.sock
```

If `/var/run/docker.sock` is missing, recreate the symlink:

```bash
sudo ln -s ~/Library/Containers/com.docker.docker/Data/docker.raw.sock /var/run/docker.sock
```

## Building from Source

```bash
./mvnw clean install
```

To build a custom version:

```bash
./mvnw -Drevision=x.y.z-SNAPSHOT-my-name clean install
```

### Running Tests

Docker integration tests require several images to be present in the local cache. Pre-pull them once before running:

```bash
docker pull alpine:latest && docker pull rabbitmq:latest && docker pull nginx:latest
```

## Contributing

Code style is enforced by Checkstyle: no tabs, no star imports, final locals and parameters, braces
required on all blocks, no `assert` statements. Import order: third-party, standard Java, then
static. IntelliJ configuration is at `config/intellij/CodeStyle.xml`.

Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/).

## License

Apache 2.0 — see [LICENSE](LICENSE)
