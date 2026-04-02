# spawn

build.spawn: A framework to programmatically launch and control processes, applications, services and containers

When using this framework to launch Docker containers, the following JVM command-line argument must be added
(with Java 25+):

```bash
    --enable-native-access=ALL-UNNAMED
```

On MacOS (and linux), local machine access to the Docker Engine may be restricted to prevent read and write access to
the docker.socket. To ensure access to the Docker Engine, the permissions for the following files may need to be
increase.

```
sudo chmod 660 ~/Library/Containers/com.docker.docker/Data/docker.raw.sock
```

and

```
sudo chmod 660 /var/run/docker.sock
```

It's also possible that the symbolic link to `/var/run/docker.sock` may need to be recreated.

```
sudo ln -s ~/Library/Containers/com.docker.docker/Data/docker.raw.sock /var/run/docker.sock
```

## Running Tests

The Docker integration tests (`SessionTests`) require several images to be present in the local Docker cache before running. 

Pre-pull them once using:

```bash
docker pull alpine:latest && docker pull rabbitmq:latest && docker pull nginx:latest
```

Without this step, the test setup (`@BeforeAll`) will attempt to pull these images on demand, which blocks the tests 
for several minutes and may fail due to transient registry (timeout) errors.
