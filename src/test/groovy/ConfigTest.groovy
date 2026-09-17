import com.jenkins.config.Config

/**
 * Unit tests for the env parsing and url building. Plain asserts, no framework:
 * run with `make test`.
 *
 * The other classes in the package are thin wrappers over the Jenkins API and
 * need a real controller to mean anything, so scripts/verify.sh covers those.
 */

def base = [
    ADMIN_USERNAME: 'admin',
    ADMIN_PASSWORD: 'secret',
    GIT_BASE_URL: 'https://github.com/',
    GIT_CREDENTIAL_ID: 'github_credential',
    GIT_PIPELINE_REPO: 'acme/jenkins-configuration',
]

def config = { Map overrides = [:] -> new Config(base + overrides) }

def fails = { Closure body ->
    try {
        body()
        return null
    } catch (AssertionError expected) {
        return expected.message
    }
}

// --- validation -------------------------------------------------------------

config().validate()

// pinned literally: asserting against Config.REQUIRED would silently shrink
// the test whenever someone shrinks the list
assert Config.REQUIRED.toSet() == [
    'ADMIN_USERNAME',
    'ADMIN_PASSWORD',
    'GIT_BASE_URL',
    'GIT_CREDENTIAL_ID',
    'GIT_PIPELINE_REPO',
].toSet()

Config.REQUIRED.each { key ->
    def message = fails { new Config(base.findAll { k, v -> k != key }).validate() }
    assert message?.contains(key) : "dropping ${key} should fail validation"
}

// an empty string is as useless as a missing var
assert fails { config(GIT_PIPELINE_REPO: '').validate() } != null

// --- git urls ---------------------------------------------------------------

assert config().gitHostName == 'https://github.com'
assert config(GIT_BASE_URL: 'https://github.com').gitHostName == 'https://github.com'
assert config(GIT_BASE_URL: 'https://git.internal//').gitHostName == 'https://git.internal'

assert config().pipelineRepoUrl == 'https://github.com/acme/jenkins-configuration'
assert config().pipelineRepoGitUrl == 'https://github.com/acme/jenkins-configuration.git'

// a base url without the trailing slash used to produce 'https://github.comacme/...'
assert config(GIT_BASE_URL: 'https://github.com').pipelineRepoUrl == config().pipelineRepoUrl

// --- shared library ---------------------------------------------------------

assert config().sharedLibGitUrl == null : 'no shared library unless GIT_SHARE_LIB_REPO is set'
assert config(GIT_SHARE_LIB_REPO: '').sharedLibGitUrl == null
assert config(GIT_SHARE_LIB_REPO: 'acme/libs').sharedLibGitUrl == 'https://github.com/acme/libs.git'

// --- jenkins url ------------------------------------------------------------

assert config().jenkinsUrl == Config.DEFAULT_JENKINS_URL
assert config(JENKINS_URL: '').jenkinsUrl == Config.DEFAULT_JENKINS_URL
assert config(JENKINS_URL: 'https://ci.acme.io').jenkinsUrl == 'https://ci.acme.io'

// --- global env vars --------------------------------------------------------

// the jenkins-configuration repo reads these four names, so they are a contract
assert config().globalEnvVars == [
    GIT_BASE_URL: 'https://github.com/',
    GIT_PIPELINE_REPO: 'https://github.com/acme/jenkins-configuration',
    GIT_HOST_NAME: 'https://github.com',
    JENKINS_CONFIGURATION_REPO: 'acme/jenkins-configuration',
]

// values are pushed into Jenkins' EnvVars map, which rejects GStrings
config().globalEnvVars.each { key, value ->
    assert value instanceof String : "${key} must be a String, got ${value.getClass()}"
}

println 'ConfigTest: all assertions passed'
