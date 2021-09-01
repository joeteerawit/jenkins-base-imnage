# Jenkins Docker image

Jenkins Docker image with [Docker](https://docs.docker.com) based on
[jenkinsci/blueocean](https://hub.docker.com/r/_/jenkinsci/)

let take a look of `init.groovy.d` folder if you want to add more stop to initail pipeline, the think you need to do is create groovy script inside this folder because when jenkins boot up it will execute script inside in this folder first.

```
.
├── init.groovy.d
    ├── ConfigurePipelineGlobalShared.groovy
    ├── CreateAdminUser.groovy
    ├── CreateCredentials.groovy
    ├── CreateGloblaProperties.groovy
    ├── DisableCLI.groovy
    ├── DisableScriptsSecurityForJobDSLScripts.groovy
    ├── EnableCSRFProtection.groovy
    ├── EnableSlaveMasterAccessControl.groovy
    ├── Executors.groovy
    ├── InitialJobs.groovy
    └── SetURLAndEmail.groovy
```

some groovy scripts in order to:

- create Jenkins shared libraries.
- create default admin user.
- create credential such as github credentail, credentail for ssh to Jenkins slave etc.
- create global constant variable.
- disable the jenkins cli over remoting
- disable scripts security for the job dsl scripts
- enable CSRF protection.
- enable slave master access control.
- set number of executors.
- create initial jobs.
- set up Jenkins url and email.

## Getting started

- rename file `.env.example` to `.env`.
- assign value in `.env` file.
- run command `Make run`.

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
| GIT_PIPELINE_REPO_NAME |                   |                                    |
| JENKINS_URL            |                   |                                    |

## Deploy Jenkins

- Remote to Jenkins server.
- Login private docker registry using this command `eval $(aws ecr get-login --region ap-southeast-1 --no-include-email)`.
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

## Get Jenkins plugin list

- Go to Jenkins script url `http://localhost:8080/script` then run below script for get plugins list.

```groovy
  Jenkins.instance.pluginManager.plugins.each{
    plugin -> 
      println ("${plugin.getShortName()}:${plugin.getVersion()}")
  }
```

#### Development

- [Setup IDE](https://www.bonusbits.com/wiki/HowTo:Setup_Project_in_IntellJ_IDEA_for_Working_with_Jenkins_Plugins_Groovy_Init_Scripts)
