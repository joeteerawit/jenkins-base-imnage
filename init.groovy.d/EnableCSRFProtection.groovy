/*
 * Configures CSRF protection in global security settings.
 */
import hudson.security.csrf.DefaultCrumbIssuer
import jenkins.model.Jenkins

if(!Jenkins.instance.isQuietingDown()) {
  def instance = Jenkins.instance
  if(instance.getCrumbIssuer() == null) {
    instance.setCrumbIssuer(new DefaultCrumbIssuer(true))
    instance.save()
    println 'CSRF Protection configuration has changed.  Enabled CSRF Protection.'
  }
  else {
    println 'Nothing changed.  CSRF Protection already configured.'
  }
}
else {
  println "Shutdown mode enabled.  Configure CSRF protection SKIPPED."
}