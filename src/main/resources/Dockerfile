FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Accept Git repo and Maven command as build args
ARG GIT_REPO_URL
ARG MAVEN_COMMAND

# Clone the repo
RUN git clone ${GIT_REPO_URL} repo

WORKDIR /app/repo

# Run the Maven command
RUN bash -c "${MAVEN_COMMAND}"

# Copy reports to a known location
RUN mkdir /output && cp -r target/cucumber-reports /output/

FROM busybox:1.35.0-uclibc
COPY --from=builder /output /output
