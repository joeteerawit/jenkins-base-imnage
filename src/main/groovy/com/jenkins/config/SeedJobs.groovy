package com.jenkins.config

import hudson.model.Cause
import hudson.model.FreeStyleProject
import hudson.plugins.git.BranchSpec
import hudson.plugins.git.GitSCM
import hudson.plugins.git.UserRemoteConfig
import javaposse.jobdsl.plugin.ExecuteDslScripts
import javaposse.jobdsl.plugin.LookupStrategy
import javaposse.jobdsl.plugin.RemovedJobAction
import javaposse.jobdsl.plugin.RemovedViewAction

/**
 * The seed job. It reads the job dsl out of the jenkins-configuration repo and
 * generates every other job from there. Created and queued on every boot, so a
 * fresh container comes up with all jobs already generated.
 */
class SeedJobs {

    static final String JOB_NAME = 'master_seed'
    static final String SCRIPT = 'jobs/master_seed/job.groovy'
    static final String CLASSPATH = 'src/main/groovy'
    static final String BRANCH = '*/master'
    static final int QUIET_PERIOD_SECONDS = 15

    final Config config

    SeedJobs(Config config) {
        this.config = config
    }

    void apply(jenkins) {
        // recreate on every boot so the job always matches this class
        jenkins.getItem(JOB_NAME)?.delete()

        def builder = new ExecuteDslScripts()
        builder.with {
            targets = SCRIPT
            additionalClasspath = CLASSPATH
            useScriptText = false
            ignoreExisting = false
            ignoreMissingFiles = false
            removedJobAction = RemovedJobAction.DISABLE
            removedViewAction = RemovedViewAction.IGNORE
            lookupStrategy = LookupStrategy.SEED_JOB
        }

        def project = new FreeStyleProject(jenkins, JOB_NAME)
        project.scm = new GitSCM(
            [new UserRemoteConfig(config.pipelineRepoGitUrl, null, null, config.gitCredentialId)],
            [new BranchSpec(BRANCH)],
            null,
            null,
            []
        )
        project.buildersList.add(builder)

        jenkins.add(project, JOB_NAME)
        project.save()

        // queued now, run once Jenkins finishes starting up
        project.scheduleBuild2(QUIET_PERIOD_SECONDS, new Cause.RemoteCause('bootstrap', 'first boot'))
    }
}
