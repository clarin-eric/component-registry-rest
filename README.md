# CLARIN Component Registry - REST service

This project implements the back end for the
[CLARIN Component Registry](https://www.clarin.eu/componentregistry). 
The Component Registry stores [CMDI](https://www.clarin.eu/cmdi) metadata components and
profiles, and offers a service to list and retrieve them. There is a 
[separate project](https://github.com/clarin-eric/component-registry-front-end) for the
Component Registry front end.

The build output of this project is used to build a Docker image. The project for this
Docker image can be found in a GitLab repository:
[docker-component-registry-rest](https://gitlab.com/CLARIN-ERIC/docker-component-registry-rest).

Detailed information about the architecture, usage and deployment can be found on the
[page on this service](https://trac.clarin.eu/wiki/ComponentRegistryAndEditor) in the CLARIN
Trac Wiki ([CLARIN developer account](https://www.clarin.eu/content/development-information)
required).

## Building

Build the Maven project using `mvn clean install`. This requires Java 11 and Maven 3.6 or
higher. You can also use the `build.sh` script without having Java or Maven installed but
this does require Docker.

Notice that there are several build profiles. The default settings or the `development`
profiles can be used for local testing. Use the `docker` profile to build for the
Docker image (see below).

Pushing to this repository will trigger a build in 
[GitHub actions](https://github.com/clarin-eric/component-registry-rest/actions).

The 
[docker-component-registry-rest](https://gitlab.com/CLARIN-ERIC/docker-component-registry-rest)
can be used to build a Docker image based on a build of this project. For this, the
`docker` profile has to be used, which creates a
`component-registry-rest-<version>-docker.tar.gz` package in the target directory. The
Docker build process unpacks this and embeds it into a Tomcat servlet container.

The Docker image can be deployed separately, in which case it has to be configured to
connect to a PostgreSQL database. An easy way to deploy the complete Component Registry
(back end, front end, database) is to use the
[compose_compreg](https://gitlab.com/CLARIN-ERIC/compose_compreg) Docker Compose project.

## Changes and upgrading

See [CHANGES.md](CHANGES.md) for release history and [UPGRADE.md](UPGRADE.md) for 
version specific upgrade instructions.

## Links

- Front end project:
[component-registry-front-end](https://github.com/clarin-eric/component-registry-front-end)
- Docker image project for this back end application:
[docker-component-registry-rest](https://gitlab.com/CLARIN-ERIC/docker-component-registry-rest)
- Docker Compose project for the Componen Registry (back end and front end):
[compose_compreg](https://gitlab.com/CLARIN-ERIC/compose_compreg)

## Licence

Copyright (C) 2026  CLARIN ERIC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
