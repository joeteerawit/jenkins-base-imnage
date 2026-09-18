IMAGE ?= joewalker/jenkins-master
GROOVY_JAR = .cache/groovy-all.jar

build:
	DOCKER_BUILDKIT=1 docker build -t $(IMAGE) .

push:
	docker push $(IMAGE)

run:
	docker run --rm --env-file .env -p 8080:8080 $(IMAGE)

# unit tests for the pure logic, using the same groovy the controller runs
$(GROOVY_JAR):
	@mkdir -p $(dir $@)
	docker run --rm $(IMAGE) sh -c 'unzip -p /usr/share/jenkins/jenkins.war "WEB-INF/lib/groovy-all-*.jar"' > $@

test: $(GROOVY_JAR)
	docker run --rm -v "$(PWD):/work" -w /work $(IMAGE) \
	  java -cp "$(GROOVY_JAR):src/main/groovy" groovy.ui.GroovyMain src/test/groovy/ConfigTest.groovy

# boots the image and checks it really configured itself
verify:
	./scripts/verify.sh

# what the image is carrying: mostly upstream debian and the git-lfs binary the
# jenkins image bundles, neither of which this repo builds
scan:
	docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v trivycache:/root/.cache \
	  aquasec/trivy:latest image --scanners vuln --severity CRITICAL,HIGH --quiet $(IMAGE)

lint:
	npx -y npm-groovy-lint "init.groovy.d/*.groovy" "src/**/*.groovy" "scripts/*.groovy"

lint_fix:
	npx -y npm-groovy-lint --fix "init.groovy.d/*.groovy" "src/**/*.groovy" "scripts/*.groovy"

.PHONY: build push run test verify scan lint lint_fix
