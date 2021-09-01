import jenkins.model.Jenkins
import jenkins.model.JenkinsLocationConfiguration

def jlc         = JenkinsLocationConfiguration.get()
def jenkinsUrl  = System.getenv("JENKINS_URL") == null ? "http://localhost:8080" : System.getenv("JENKINS_URL")
jlc.setUrl(jenkinsUrl)
jlc.setAdminAddress('Jenkins Admin <admin@jenkins.com>')
jlc.save()