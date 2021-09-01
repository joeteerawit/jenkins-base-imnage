import jenkins.model.Jenkins
import javaposse.jobdsl.plugin.*
import hudson.model.FreeStyleProject
import hudson.plugins.git.GitSCM
import hudson.plugins.git.UserRemoteConfig
import hudson.plugins.git.BranchSpec
import hudson.triggers.*

/**
* Note
* https://github.com/camiloribeiro/cdeasy/blob/master/docker/jenkins/seed.groovy
*
*/ 
println "--> initial jobs"
println "--> clean jobs"
def jenkins     = Jenkins.instance
def githubUrl   = System.getenv("GIT_BASE_URL") + System.getenv("GIT_PIPELINE_REPO") + ".git"
def credential  = System.getenv("GIT_CREDENTIAL_ID")

jenkins
  .items
  .findAll { job -> job.name == jobName }
  .each { job -> job.delete() }

// --------------------------------------------------------------------------------------------------------------------------------------------------
def jobs = [
  [
    gitUrl: githubUrl,
    credentialsId: credential,
    jobName: "master_seed",
    script: "jobs/master_seed/job.groovy"
  ],
  [
    gitUrl: githubUrl,
    credentialsId: credential,
    jobName: "initial_jobs",
    script: "jobs/initial_jobs/job.groovy"
  ]
].each { item -> 
  def gitUrl        = item["gitUrl"]
  def credentialsId = item["credentialsId"]
  def branch        = item.containsKey("branch") ? item["branch"] : "*/master"
  def jobName       = item["jobName"]
  def script        = item["script"]

  def dslBuilder  = new ExecuteDslScripts()

  dslBuilder.setTargets(script)
  dslBuilder.setUseScriptText(false)
  dslBuilder.setIgnoreExisting(false)
  dslBuilder.setIgnoreMissingFiles(false)
  dslBuilder.setRemovedJobAction(RemovedJobAction.DISABLE)
  dslBuilder.setRemovedViewAction(RemovedViewAction.IGNORE)
  dslBuilder.setLookupStrategy(LookupStrategy.SEED_JOB)

  def dslProject  = new hudson.model.FreeStyleProject(jenkins, jobName)
  def config      = new UserRemoteConfig(
                      gitUrl,
                      null,
                      null,
                      credentialsId
                    )
  // Set git repository
  dslProject.scm                    = new GitSCM(gitUrl)
  dslProject.scm.branches           = [new BranchSpec(branch)]
  dslProject.scm.userRemoteConfigs  = [config]

  dslProject.createTransientActions()
  dslProject.getPublishersList().add(dslBuilder)
  jenkins.add(dslProject, jobName)
}
