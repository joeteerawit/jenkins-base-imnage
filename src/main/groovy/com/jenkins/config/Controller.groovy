package com.jenkins.config

import hudson.slaves.EnvironmentVariablesNodeProperty
import jenkins.model.JenkinsLocationConfiguration

/** Executors, location and the global environment variables every build sees. */
class Controller {

    static final int NUM_EXECUTORS = 2
    static final String LABEL = 'master-node'
    static final String ADMIN_ADDRESS = 'Jenkins Admin <admin@jenkins.com>'

    final Config config

    Controller(Config config) {
        this.config = config
    }

    void apply(jenkins) {
        // https://javadoc.jenkins-ci.org/hudson/model/Node.html
        jenkins.numExecutors = NUM_EXECUTORS
        jenkins.labelString = LABEL

        def location = JenkinsLocationConfiguration.get()
        location.url = config.jenkinsUrl
        location.adminAddress = ADMIN_ADDRESS
        location.save()

        globalEnvVarsOf(jenkins).putAll(config.globalEnvVars)
    }

    private static Map globalEnvVarsOf(jenkins) {
        def properties = jenkins.globalNodeProperties
        def envProperty = properties.get(EnvironmentVariablesNodeProperty)
        if (envProperty == null) {
            envProperty = new EnvironmentVariablesNodeProperty()
            properties.add(envProperty)
        }
        envProperty.envVars
    }
}
