package com.jenkins.config

import hudson.security.FullControlOnceLoggedInAuthorizationStrategy
import hudson.security.HudsonPrivateSecurityRealm
import javaposse.jobdsl.plugin.GlobalJobDslSecurityConfiguration
import jenkins.model.GlobalConfiguration

/** Admin account, authorization strategy and job dsl script security. */
class Security {

    final Config config

    Security(Config config) {
        this.config = config
    }

    void apply(jenkins) {
        def realm = new HudsonPrivateSecurityRealm(false)
        realm.createAccount(config.adminUsername, config.adminPassword)
        jenkins.securityRealm = realm

        // without an authorization strategy Jenkins stays "anyone can do anything"
        def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
        strategy.allowAnonymousRead = false
        jenkins.authorizationStrategy = strategy

        // the job dsl scripts come from our own repo, so the sandbox only gets in the way
        def dslSecurity = GlobalConfiguration.all().get(GlobalJobDslSecurityConfiguration)
        dslSecurity.useScriptSecurity = false
        dslSecurity.save()
    }
}
