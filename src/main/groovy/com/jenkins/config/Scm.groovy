package com.jenkins.config

import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import jenkins.plugins.git.GitSCMSource
import jenkins.plugins.git.traits.BranchDiscoveryTrait
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever

/** Git credential and the implicitly loaded pipeline shared library. */
class Scm {

    static final String LIBRARY_NAME = 'pipeline-library'
    static final String DEFAULT_VERSION = 'master'

    final Config config

    Scm(Config config) {
        this.config = config
    }

    /* groovylint-disable-next-line UnusedMethodParameter */
    void apply(jenkins) {
        // the credential store and the library list are global singletons, so
        // nothing here needs the controller; the parameter keeps every step
        // callable the same way from Bootstrap
        addGitCredential()
        addSharedLibrary()
    }

    /** Re-adding an existing id is a no-op, so this is safe on every boot. */
    private void addGitCredential() {
        def credential = new UsernamePasswordCredentialsImpl(
            CredentialsScope.GLOBAL,
            config.gitCredentialId,
            '',
            config.gitUser,
            config.gitToken
        )
        SystemCredentialsProvider.instance.store.addCredentials(Domain.global(), credential)
    }

    private void addSharedLibrary() {
        if (!config.sharedLibGitUrl) {
            println '    no GIT_SHARE_LIB_REPO, skipping shared library'
            return
        }

        def source = new GitSCMSource(config.sharedLibGitUrl)
        source.credentialsId = config.gitCredentialId
        source.traits = [new BranchDiscoveryTrait()]

        def library = new LibraryConfiguration(LIBRARY_NAME, new SCMSourceRetriever(source))
        library.defaultVersion = DEFAULT_VERSION
        library.implicit = true
        library.allowVersionOverride = true
        library.includeInChangesets = true

        GlobalLibraries.get().libraries = [library]
    }
}
