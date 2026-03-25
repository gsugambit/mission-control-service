## Application

This application will serve as Mission Control Dashboard backend for openclaw task prioritization/completion

## Docker Build
DOCKER_BUILDKIT=1 docker build --target build --build-arg GIT_BRANCH=main --build-arg IMAGE_TAG=v0.1.0 -t gambit-labs/mission-control-service:v0.1.0 .