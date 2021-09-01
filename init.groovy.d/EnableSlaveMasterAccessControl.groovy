import jenkins.model.*
import jenkins.security.s2m.AdminWhitelistRule

println "--> enabling slave master access control"
def instance = Jenkins.getInstance()

instance
  .injector
  .getInstance(AdminWhitelistRule.class)
  .setMasterKillSwitch(false)

instance.save()