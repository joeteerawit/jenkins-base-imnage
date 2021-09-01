FROM jenkins/jenkins:lts

USER root

RUN apk update --no-cache && \
    apk add --no-cache \
    ca-certificates 

COPY internal-ca /usr/local/share/ca-certificates/internal-ca.crt
RUN update-ca-certificates && \
    keytool \
    -import \
    -storepass changeit \
    -file /usr/local/share/ca-certificates/internal-ca.crt \
    -keystore /etc/ssl/certs/java/cacerts \
    -noprompt \
    -alias jenkins_master