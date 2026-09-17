package com.jenkins.config

import jenkins.model.Jenkins

/**
 * Composes the bootstrap steps. Adding a step means adding a class to the list
 * below, nothing in init.groovy.d has to change.
 */
class Bootstrap {

    void run() {
        def config = Config.fromEnv()
        config.validate()

        def jenkins = Jenkins.get()

        [
            new Security(config),
            new Scm(config),
            new Controller(config),
            new SeedJobs(config),
        ].each { step ->
            println "--> ${step.class.simpleName}"
            step.apply(jenkins)
        }

        jenkins.save()
    }
}
