FROM jenkins/jenkins:2.568.3-lts-jdk21

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

# optional internal CAs: drop *.crt files into certs/
USER root
COPY certs/ /usr/local/share/ca-certificates/
RUN update-ca-certificates && \
    for crt in /usr/local/share/ca-certificates/*.crt; do \
      [ -e "$crt" ] || continue; \
      keytool -importcert -cacerts -storepass changeit -noprompt \
        -alias "$(basename "$crt" .crt)" -file "$crt"; \
    done
USER jenkins

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt

# the bootstrap logic is read from the image, not from jenkins_home, so it stays
# current across upgrades even when init.groovy.d in jenkins_home is stale
COPY src/main/groovy/ /usr/share/jenkins/config/
COPY init.groovy.d/ /usr/share/jenkins/ref/init.groovy.d/
