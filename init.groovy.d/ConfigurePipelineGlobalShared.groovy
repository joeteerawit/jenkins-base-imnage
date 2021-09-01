import jenkins.model.Jenkins
import jenkins.plugins.git.GitSCMSource
import jenkins.plugins.git.traits.BranchDiscoveryTrait
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever

List libraries = [] as ArrayList

def remote          = System.getenv("GIT_BASE_URL") + System.getenv("GIT_SHARE_LIB_REPO") + ".git"
def credentialsId   = System.getenv("GIT_CREDENTIAL_ID")
def name            = 'pipeline-library'
def defaultVersion  = 'master'

if (remote != null) {

  def scm = new GitSCMSource(remote)
  if (credentialsId != null) {
      scm.credentialsId = credentialsId
  }

  scm.traits = [new BranchDiscoveryTrait()]
  def retriever = new SCMSourceRetriever(scm)

  def library = new LibraryConfiguration(name, retriever)
  library.defaultVersion        = defaultVersion
  library.implicit              = true
  library.allowVersionOverride  = true
  library.includeInChangesets   = true

  libraries << library

  def global_settings = Jenkins.instance.getExtensionList(GlobalLibraries.class)[0]
  global_settings.libraries = libraries
  global_settings.save()
}
