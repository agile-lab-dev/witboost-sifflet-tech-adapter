<p align="center">
    <a href="https://www.agilelab.it/witboost">
        <img src="docs/img/witboost_logo.svg" alt="witboost" width=600 >
    </a>
</p>

Designed by [Agile Lab](https://www.agilelab.it/), Witboost is a versatile platform that addresses a wide range of sophisticated data engineering challenges. It enables businesses to discover, enhance, and productize their data, fostering the creation of automated data platforms that adhere to the highest standards of data governance. Want to know more about Witboost? Check it out [here](https://www.witboost.com) or [contact us!](https://www.witboost.com/contact-us).

This repository is part of our [Starter Kit](https://github.com/agile-lab-dev/witboost-starter-kit) meant to showcase Witboost integration capabilities and provide a "batteries-included" product.

# Sifflet Tech Adapter

- [Overview](#overview)
- [Building](#building)
- [Configuring](#configuring)
- [Running](#running)
- [OpenTelemetry Setup](docs/opentelemetry.md)
- [Deploying](#deploying)
- [HLD](docs/HLD/HLD.md)


## Overview

This project implements a simple Tech Adapter for Sifflet.

### What's a Tech Adapter?

A Tech Adapter (formerly a Specific Provisioner) is a microservice which is in charge of deploying components that use a specific technology. When the deployment of a Data Product is triggered, the platform generates it descriptor and orchestrates the deployment of every component contained in the Data Product. For every such component the platform knows which Tech Adapter is responsible for its deployment, and can thus send a provisioning request with the descriptor to it so that the Tech Adapter can perform whatever operation is required to fulfill this request and report back the outcome to the platform.

You can learn more about how the Tech Adapters fit in the broader picture [here](https://docs.witboost.com/docs/p2_arch/p1_intro/#deploy-flow).

### Sifflet

Sifflet is a data observability platform that helps monitor the quality, availability, and reliability of data. It automatically detects anomalies, schema issues, and threshold breaches. It enables data engineering and analytics teams to proactively manage data incidents.

Learn more on [Sifflet official documentation](https://docs.siffletdata.com/).


### Software stack

This microservice is written in Java 17, using SpringBoot for the HTTP layer supported by the Java Tech Adapter Framework. Project is built with Apache Maven and supports packaging and Docker image, ideal for Kubernetes deployments (which is the preferred option).

The API layer is handled by the framework and follows this [OpenAPI Specification](https://github.com/agile-lab-dev/witboost-java-tech-adapter-framework/tree/master/core/src/main/resources/interface-specification.yml).

### Git hooks

Hooks are programs you can place in a hooks directory to trigger actions at certain points in git’s execution. Hooks that don’t have the executable bit set are ignored.

The hooks are all stored in the hooks subdirectory of the Git directory. In most projects, that’s `.git/hooks`.

Out of the many available hooks supported by Git, we use `pre-commit` hook in order to check the code changes before each commit. If the hook returns a non-zero exit status, the commit is aborted.


#### Setup Pre-commit hooks

In order to use `pre-commit` hook, you can use [**pre-commit**](https://pre-commit.com/) framework to set up and manage multi-language pre-commit hooks.

To set up pre-commit hooks, follow the below steps:

- Install pre-commit framework either using pip (or) using homebrew (if your Operating System is macOS):

    - Using pip:
      ```bash
      pip install pre-commit
      ```
    - Using homebrew:
      ```bash
      brew install pre-commit
      ```

- Once pre-commit is installed, you can execute the following:

```bash
pre-commit --version
```

If you see something like `pre-commit 4.0.1`, your installation is ready to use!


- To use pre-commit, create a file named `.pre-commit-config.yaml` inside the project directory. This file tells pre-commit which hooks needed to be installed based on your inputs. Below is an example configuration:

```bash
repos:
  - repo: https://github.com/pre-commit/pre-commit-hooks
    rev: v4.4.0
    hooks:
      - id: trailing-whitespace
```

The above configuration says to download the `pre-commit-hooks` project and run its trailing-whitespace hook on the project.


- Run the below command to install pre-commit into your git hooks. pre-commit will then run on every commit.

```bash
pre-commit install
```

## Building

**Requirements:**

- Java 17
- Apache Maven 3.9+
- For Helm deployment: Helm 3+, Helm-docs 1.11.0

**Version:** the version is set dynamically via an environment variable, `PROVISIONER_VERSION`. Make sure you have it exported, even for local development. Example:

```bash
export PROVISIONER_VERSION=0.0.0-SNAPHSOT
```

**Build:**

```bash
mvn compile
```

**Type check:** is handled by Spotless:

```bash
mvn spotless:check
```

**Bug checks:** are handled by SpotBugs:

```bash
mvn spotbugs:check
```

**Tests:** are handled by JUnit:

```bash
mvn test
```

**Artifacts & Docker image:** the project leverages Maven for packaging. Build artifacts (normal and fat jar) with:

```bash
mvn package spring-boot:repackage
```

The Docker image can be built with:

```bash
docker build .
```

More details can be found [here](docs/docker.md).

*Note:* when running in the CI/CD pipeline the version for the project is automatically computed using information gathered from Git, using branch name and tags. Unless you are on a release branch `1.2.x` or a tag `v1.2.3` it will end up being `0.0.0`. You can follow this branch/tag convention or update the version computation to match your preferred strategy. When running locally if you do not care about the version (ie, nothing gets published or similar) you can manually set the environment variable `PROVISIONER_VERSION` to avoid warnings and oddly-named artifacts; as an example you can set it to the build time like this:
```bash
export PROVISIONER_VERSION=$(date +%Y%m%d-%H%M%S);
```

**CI/CD:** the pipeline is based on GitLab CI as that's what we use internally. It's configured by the `.gitlab-ci.yaml` file in the root of the repository. You can use that as a starting point for your customizations.


## Configuring

Application configuration is handled using the features provided by Spring Boot. You can find the default settings in the [application.yml](common/src/main/resources/application.yml). Customize it and use the options provided by the framework according to your needs.


The following table lists the configuration properties required for integrating the application with Sifflet. These settings can be passed as environment variables.

| Environment Variable              | Description                                                  | Configuration                      |
|:----------------------------------|:-------------------------------------------------------------|:-----------------------------------|
| **SIFFLET_TOKEN**                 | Authentication token for accessing the Sifflet API           | sifflet.token                      |
| **SIFFLET_BASE_PATH**             | Base URL for sending requests to the Sifflet API             | sifflet.basePath                   |
| **SOURCE_UPDATE_TIMEOUT_SECONDS** | Timeout (in seconds) for updating source metadata in Sifflet | sifflet.sourceUpdateTimeoutSeconds |
| **SIFFLET_SOURCE_IAM_ROLE**       | AWS IAM Role ARN used to connect Sifflet to Athena databases | sifflet.athena.iamRole             |

### IAM permissions

To ensure the microservice functions correctly, the IAM role specified in `sifflet.athena.iamRole` must have the permissions outlined [here](docs/permissionList.md).

## Running

To run the server locally, use:

```bash
mvn -pl common spring-boot:run
```

By default, the server binds to port `8888` on localhost. After it's up and running you can make provisioning requests to this address. You can access the running application [here](http://127.0.0.1:8888).

SwaggerUI is configured and hosted on the path `/docs`. You can access it [here](http://127.0.0.1:8888/docs)

## Deploying

This microservice is meant to be deployed to a Kubernetes cluster with the included Helm chart and the scripts that can be found in the `helm` subdirectory. You can find more details [here](helm/README.md).

## License

This project is available under the [Apache License, Version 2.0](https://opensource.org/licenses/Apache-2.0); see [LICENSE](LICENSE) for full details.

## About Witboost

[Witboost](https://witboost.com/) is a cutting-edge Data Experience platform, that streamlines complex data projects across various platforms, enabling seamless data production and consumption. This unified approach empowers you to fully utilize your data without platform-specific hurdles, fostering smoother collaboration across teams.

It seamlessly blends business-relevant information, data governance processes, and IT delivery, ensuring technically sound data projects aligned with strategic objectives. Witboost facilitates data-driven decision-making while maintaining data security, ethics, and regulatory compliance.

Moreover, Witboost maximizes data potential through automation, freeing resources for strategic initiatives. Apply your data for growth, innovation and competitive advantage.

[Contact us](https://witboost.com/contact-us) or follow us on:

- [LinkedIn](https://www.linkedin.com/showcase/witboost/)
- [YouTube](https://www.youtube.com/@witboost-platform)
