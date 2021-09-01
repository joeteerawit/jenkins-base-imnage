build:
	DOCKER_BUILDKIT=1 docker build -t joewalker/jenkins-master .

push:
	docker push joewalker/jenkins-master

run:
	docker run --rm --env-file .env -p 8080:8080 joewalker/jenkins-master