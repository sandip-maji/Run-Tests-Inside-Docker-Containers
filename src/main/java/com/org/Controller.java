package com.org;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
public class Controller {

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service is Up....... ");
    }

    @PostMapping("/run-bdd")
    public ResponseEntity<String> runTest(@RequestBody TestRequest request) {
        try {
            String outputPath = "/tmp/test-reports/" + UUID.randomUUID();
            String reportPath = runCucumberTestsInDocker(
                    request.getRepoUrl(),
                    request.getMavenCommand(),
                    outputPath
            );
            return ResponseEntity.ok("Test completed. Report at: " + reportPath);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }


    public String runCucumberTestsInDocker(String gitRepoUrl, String mavenCommand, String reportOutputDir)
            throws IOException, InterruptedException {

        String imageName = "bdd-runner:" + UUID.randomUUID();
        String dockerfilePath = "/tmp/Dockerfile";

        // 1. Copy base Dockerfile template to /tmp (you can keep it in resources)
        Files.copy(Paths.get("C:/MyComputer/Professional/Workspace/ThetaOne/BeeRelevant/Git/GIT-REPO/othres/Run-Tests-Inside-Docker-Containers/src/main/resources/Dockerfile"), Paths.get(dockerfilePath), StandardCopyOption.REPLACE_EXISTING);

        // 2. Build Docker image with GIT_REPO_URL and MAVEN_COMMAND
        ProcessBuilder build = new ProcessBuilder(
                "docker", "build",
                "--build-arg", "GIT_REPO_URL=" + gitRepoUrl,
                "--build-arg", "MAVEN_COMMAND=" + mavenCommand,
                "-t", imageName,
                "-f", dockerfilePath,
                "."
        );
        build.inheritIO().start().waitFor();

        // 3. Run container
        String containerName = "runner-" + UUID.randomUUID();
        new ProcessBuilder("docker", "run", "--name", containerName, imageName)
                .inheritIO().start().waitFor();

        // 4. Copy report from container
        new ProcessBuilder("docker", "cp", containerName + ":/output", reportOutputDir)
                .inheritIO().start().waitFor();

        // 5. Cleanup
        new ProcessBuilder("docker", "rm", containerName).start().waitFor();
        new ProcessBuilder("docker", "rmi", imageName).start().waitFor();

        return reportOutputDir + "/cucumber-reports";
    }



}
