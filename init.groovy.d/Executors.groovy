import jenkins.model.*

println "--> setting num executors to 5"

def instance = Jenkins.getInstance()

// https://javadoc.jenkins-ci.org/hudson/model/Node.html
instance.setNumExecutors(2)
instance.setLabelString("master-node")