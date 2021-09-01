import com.michelin.cio.hudson.plugins.rolestrategy.*
import jenkins.model.*
import hudson.security.*

println "--> creating admin user"
def instance = Jenkins.getInstance()

def adminUsername = System.getenv("ADMIN_USERNAME")
def adminPassword = System.getenv("ADMIN_PASSWORD")

assert adminPassword != null : "No ADMIN_USERNAME env var provided, but required"
assert adminPassword != null : "No ADMIN_PASSWORD env var provided, but required"

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount(adminUsername, adminPassword)
instance.setSecurityRealm(hudsonRealm)

// https://gist.github.com/mllrjb/ccfd3315b7546ae8e8382ff693b34d7f
// def roleBasedAuthenticationStrategy = new RoleBasedAuthorizationStrategy()
// instance.setAuthorizationStrategy(roleBasedAuthenticationStrategy)

instance.save()
