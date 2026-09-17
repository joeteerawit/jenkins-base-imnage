package com.jenkins.config

/**
 * Everything the bootstrap needs, derived from the container environment.
 *
 * Deliberately free of any Jenkins or plugin import so it can be unit tested
 * with a plain Groovy runtime. See src/test/groovy/ConfigTest.groovy.
 */
class Config {

    static final String DEFAULT_JENKINS_URL = 'http://localhost:8080'
    static final List<String> REQUIRED = [
        'ADMIN_USERNAME',
        'ADMIN_PASSWORD',
        'GIT_BASE_URL',
        'GIT_CREDENTIAL_ID',
        'GIT_PIPELINE_REPO',
    ]

    final Map<String, String> env

    Config(Map<String, String> env) {
        this.env = env
    }

    static Config fromEnv() {
        new Config(System.getenv())
    }

    void validate() {
        def missing = REQUIRED.findAll { key -> !env[key] }
        assert missing.empty : "missing required env vars: ${missing.join(', ')}"
    }

    String getAdminUsername() { env['ADMIN_USERNAME'] }

    String getAdminPassword() { env['ADMIN_PASSWORD'] }

    String getGitUser() { env['GIT_USER'] }

    String getGitToken() { env['GIT_TOKEN'] }

    String getGitCredentialId() { env['GIT_CREDENTIAL_ID'] }

    String getGitBaseUrl() { env['GIT_BASE_URL'] }

    /** GIT_BASE_URL without trailing slashes, so it can be joined with a repo path. */
    String getGitHostName() { gitBaseUrl?.replaceAll('/+$', '') }

    /** owner/repo of the jenkins-configuration repository. */
    String getPipelineRepo() { env['GIT_PIPELINE_REPO'] }

    String getPipelineRepoUrl() { "${gitHostName}/${pipelineRepo}" }

    String getPipelineRepoGitUrl() { "${pipelineRepoUrl}.git" }

    String getSharedLibRepo() { env['GIT_SHARE_LIB_REPO'] }

    /** null when GIT_SHARE_LIB_REPO is unset, meaning no shared library is configured. */
    String getSharedLibGitUrl() {
        sharedLibRepo ? "${gitHostName}/${sharedLibRepo}.git" : null
    }

    String getJenkinsUrl() { env['JENKINS_URL'] ?: DEFAULT_JENKINS_URL }

    /** Exported to every build; the jenkins-configuration repo reads these by name. */
    Map<String, String> getGlobalEnvVars() {
        [
            GIT_BASE_URL: gitBaseUrl,
            GIT_PIPELINE_REPO: pipelineRepoUrl,
            GIT_HOST_NAME: gitHostName,
            JENKINS_CONFIGURATION_REPO: pipelineRepo,
        ]
    }
}
