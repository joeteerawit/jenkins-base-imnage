# Jenkins Docker image

Jenkins Docker image based on [jenkins/jenkins](https://hub.docker.com/r/jenkins/jenkins) LTS (JDK 21).
Plugins are pinned in `plugins.txt` and installed at build time, the setup wizard is skipped.

Jenkins runs every script in `init.groovy.d` at boot. There is only one, and all it
does is load the `com.jenkins.config` package and run it:

```
.
├── init.groovy.d
│   └── bootstrap.groovy          # 3 lines, loads the package below
├── src
│   ├── main/groovy/com/jenkins/config
│   │   ├── Bootstrap.groovy      # the list of steps to run
│   │   ├── Config.groovy         # env vars and the urls derived from them
│   │   ├── Security.groovy       # admin user, authorization, job dsl security
│   │   ├── Scm.groovy            # git credential, pipeline shared library
│   │   ├── Controller.groovy     # executors, url, email, global env vars
│   │   └── SeedJobs.groovy       # the master_seed job
│   └── test/groovy
│       └── ConfigTest.groovy
└── scripts
    ├── verify.sh                 # boots the image and checks it configured itself
    └── verify.groovy
```

To add a step, write a class with an `apply(jenkins)` method and add it to the list
in `Bootstrap.groovy`. Nothing in `init.groovy.d` changes.

The package lives at `/usr/share/jenkins/config` in the image rather than in
`jenkins_home`, so upgrading the image updates the logic even when an old copy of
`bootstrap.groovy` is still sitting in a persistent `jenkins_home`.

## Tests

| Command       | What it does                                                                      |
| :------------ | :-------------------------------------------------------------------------------- |
| `make test`   | Unit tests for `Config`, run with the same Groovy the controller uses. No booting. |
| `make verify` | Builds nothing; boots the image, asserts the configured state, waits for `master_seed`. |
| `make lint`   | `npm-groovy-lint` over every groovy file.                                          |

`make verify` needs network access to `GIT_PIPELINE_REPO`. The other classes are thin
wrappers over the Jenkins API, so `verify` covers them against a real controller
instead of mocks.

## Getting started

- rename file `.env.example` to `.env`.
- assign value in `.env` file.
- run command `make build run`.
- `master_seed` is created and queued at boot, so it generates every other job on its
  own. Log in only to watch it.

**Note** upgrading from a build that had one script per concern: those scripts are still
in a persistent `jenkins_home` and would run alongside `bootstrap.groovy`. Delete
`jenkins_home/init.groovy.d` before starting the new image.

Internal CA certificates: drop `*.crt` files into `certs/` before building.

## Parameter Description

| Name                   | Value             | Description                        |
| :--------------------- | :---------------- | :--------------------------------- |
| ADMIN_USERNAME         |                   | Get it from keyweb `jenkins` entry |
| ADMIN_PASSWORD         |                   | Get it from keyweb `jenkins` entry |
| GIT_USER               |                   |                                    |
| GIT_TOKEN              |                   |                                    |
| GIT_BASE_URL           |                   |                                    |
| GIT_SHARE_LIB_REPO     |                   |                                    |
| GIT_CREDENTIAL_ID      | github_credential |                                    |
| GIT_PIPELINE_REPO      |                   | `owner/repo` of jenkins-configuration |
| JENKINS_URL            |                   |                                    |

## Deploy Jenkins

- Remote to Jenkins server.
- Login private docker registry using this command `aws ecr get-login-password --region ap-southeast-1 | docker login --username AWS --password-stdin 393437166688.dkr.ecr.ap-southeast-1.amazonaws.com`.
- Run Jenkins container using below command.

**Note** For restore Jenkins before run the container you shoud download backup file form s3 (bucket: `com.tescolotus.seacust-jenkins`) first.

```
docker run -d \
--env-file .env \
-v /var/jenkins_home:/var/jenkins_home \
-v /var/run/docker.sock:/var/run/docker.sock \
-p 80:8080 \
-p 5000:5000 \
393437166688.dkr.ecr.ap-southeast-1.amazonaws.com/jenkins
```

## Update Jenkins and plugins

- bump the image tag in `Dockerfile` (latest LTS: https://www.jenkins.io/changelog-stable/).
- get the latest plugin versions compatible with that image, then replace `plugins.txt` with the output:

```
docker run --rm -v $PWD/plugins.txt:/plugins.txt jenkins/jenkins:<tag> \
  jenkins-plugin-cli -f /plugins.txt --available-updates --output txt
```

Only top-level plugins are listed, dependencies are resolved by `jenkins-plugin-cli`.

#### Development

- [Setup IDE](https://www.bonusbits.com/wiki/HowTo:Setup_Project_in_IntellJ_IDEA_for_Working_with_Jenkins_Plugins_Groovy_Init_Scripts)
