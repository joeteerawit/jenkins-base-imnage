/*
 * Asserts the bootstrap produced the Jenkins state we expect. Runs on the
 * controller via /scriptText, driven by scripts/verify.sh.
 */
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy
import hudson.security.HudsonPrivateSecurityRealm
import hudson.slaves.EnvironmentVariablesNodeProperty
import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries

def jenkins = Jenkins.get()

// plugins
assert jenkins.pluginManager.failedPlugins.empty : "failed plugins: ${jenkins.pluginManager.failedPlugins*.name}"
def inactive = jenkins.pluginManager.plugins.findAll { plugin -> !plugin.active }
assert inactive.empty : "inactive plugins: ${inactive*.shortName}"

// security
assert jenkins.securityRealm instanceof HudsonPrivateSecurityRealm
assert jenkins.authorizationStrategy instanceof FullControlOnceLoggedInAuthorizationStrategy
assert !jenkins.authorizationStrategy.allowAnonymousRead : 'anonymous read must be off'
assert jenkins.crumbIssuer != null : 'CSRF protection must be on'
assert jenkins.securityRealm.getUser(System.getenv('ADMIN_USERNAME')) != null : 'admin account missing'

// controller
assert jenkins.numExecutors == 2
assert jenkins.labelString == 'master-node'
assert jenkins.rootUrl == System.getenv('JENKINS_URL') + '/'

// global environment variables, the contract with the jenkins-configuration repo
def envVars = jenkins.globalNodeProperties.get(EnvironmentVariablesNodeProperty).envVars
def repo = System.getenv('GIT_PIPELINE_REPO')
assert envVars['GIT_HOST_NAME'] == 'https://github.com'
assert envVars['JENKINS_CONFIGURATION_REPO'] == repo
assert envVars['GIT_PIPELINE_REPO'] == "https://github.com/${repo}"

// credential and shared library
assert SystemCredentialsProvider.instance.credentials*.id == [System.getenv('GIT_CREDENTIAL_ID')]
assert GlobalLibraries.get().libraries*.name == ['pipeline-library']

// the one job a human runs
def seed = jenkins.getItem('master_seed')
assert seed != null : 'master_seed job missing'
assert seed.builders*.class*.simpleName == ['ExecuteDslScripts']
assert seed.scm.userRemoteConfigs*.url == ["https://github.com/${repo}.git"]

println 'VERIFY_STATE_OK'
