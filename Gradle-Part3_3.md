Understood. I will generate the rest of the guide in a single response. This will be a comprehensive "data dump" that you can review at your leisure.

Here are the remaining sections of the guide.

---

### 3.3 The `FormatterPlugin`

**Purpose:** To automatically format all source code in the project, ensuring a consistent and clean coding style across the entire team. This eliminates arguments about style and makes code reviews easier.

**Key Features:**

*   **Underlying Tool:** It uses the excellent **Spotless** Gradle plugin under the hood.
*   **Multi-Language Support:** It's pre-configured to format:
    *   **Java** (using `google-java-format`)
    *   **Kotlin** (using `ktfmt`)
    *   **Gradle Scripts** (`*.gradle.kts`)
    *   **YAML** files (`*.yml`, `*.yaml`)
    *   **JavaScript/TypeScript** (using Prettier)
*   **Efficient Ratcheting:** This is its most powerful feature. It only attempts to format files that have changed compared to your main branch (e.g., `origin/main`). This means you don't waste time formatting the entire repository, only the files you are currently working on.

**Key Tasks it Adds:**

*   `spotlessApply`: **The main task you will use.** It finds all applicable files and reformats them in place. Run this before you commit.
*   `spotlessCheck`: Checks if all files are formatted correctly. It doesn't change anything, but it will **fail the build** if it finds a file that needs formatting. The `ciSource` lifecycle task runs this, so unformatted code will be blocked by the CI pipeline.
*   `spotlessClean`: A utility task to clean up Spotless's cache, hooked into `ciBootstrapClean`.

**Default Behavior (Out-of-the-Box):**

*   It uses `origin/main` as the branch to compare against for "ratcheting".
*   It automatically excludes common build output and dependency directories like `build/`, `node_modules/`, and `.git/`.
*   If it detects a JavaScript/TypeScript project, it will automatically create a `.prettierrc` configuration file in your project root if one doesn't already exist.

---

### Configuration & Overrides

Configuration goes into a `boondiFormatter { ... }` block in your `build.gradle.kts`.

#### Example 1: Changing the Ratchet Branch

If your main development branch is not `origin/main`, you need to tell Spotless which branch to compare against.

*   **How:** Set the `ratchetBranch` property.
*   **Where:** `build.gradle.kts`

```kotlin
// In build.gradle.kts
boondiFormatter {
  ratchetBranch.set("origin/develop")
}
```

#### Example 2: Excluding Additional Files or Directories

If you have a directory of auto-generated code that you don't want Spotless to touch, you can add it to the exclusion list.

*   **How:** Add a path string to the `exclude` list property.
*   **Where:** `build.gradle.kts`

```kotlin
// In build.gradle.kts
boondiFormatter {
  // Exclude a specific generated protobufs directory
  exclude.add("**/generated-sources/protobuf/**")
}
```

---

### Common Mistakes and "Gotchas"

1.  **Mistake:** The CI build fails on the `spotlessCheck` task.
    *   **Problem:** You have committed files that are not formatted correctly.
    *   **Solution:** Run `./gradlew spotlessApply` locally. This will fix the formatting. Then, commit the changes and push again. This is the most common reason for a CI build to fail early.

2.  **Mistake:** Applying the Formatter plugin in a subproject's `build.gradle.kts`.
    *   **Problem:** The plugin is designed to work at the repository root level to have a single view of all files. Applying it in a subproject can lead to confusing behavior and incomplete formatting.
    *   **Solution:** **Convention Alert:** Always apply the `boondi-formatter` plugin in the **root `build.gradle.kts` file** only.

---

### 3.4 The `SonarPlugin`

**Purpose:** To perform deep static analysis on your code and report the results to a central SonarQube server. This helps find bugs, security vulnerabilities, and "code smells" before they reach production.

**Key Features:**

*   **Seamless Integration:** It wraps the official `org.sonarqube.gradle.plugin` and configures it automatically.
*   **Context-Aware:** It uses information from the `GitPlugin` to tell SonarQube if it's analyzing a regular branch or a pull request. This is critical for getting analysis results posted directly to your merge request in GitLab.
*   **Coverage Reporting:** It automatically ensures that the JaCoCo test coverage report is generated *before* the Sonar analysis runs, so your coverage metrics are always included in the SonarQube report.

**Key Tasks it Configures:**

*   `sonar`: This is the main task that collects all information (source files, test reports, coverage reports) and sends it to the SonarQube server for analysis. It is automatically hooked into the `ciBuildStaticAnalysis` lifecycle task.

**Default Behavior (Out-of-the-Box):**

*   Waits for the quality gate result from SonarQube? **No (`qualityGateWait = false`)**. By default, the build will not wait for the SonarQube analysis to complete. This allows for a faster CI pipeline. The result can be checked later in the SonarQube UI.
*   The SonarQube server URL is pre-configured to a default internal address.
*   It expects the SonarQube project key and authentication token to be provided via the standard configuration mechanism (usually environment variables in GitLab CI).

---

### Configuration & Overrides

Configuration goes into a `boondiSonar { ... }` block in your `build.gradle.kts`.

#### Example 1: Waiting for the Quality Gate

For a critical release build, you might want the CI pipeline to fail if the SonarQube Quality Gate does not pass.

*   **How:** Set `qualityGateWait` to `true` and configure an appropriate timeout.
*   **Where:** `build.gradle.kts`

```kotlin
// In build.gradle.kts
boondiSonar {
  qualityGateWait.set(true)
  qualityGateTimeoutInSeconds.set(600) // Wait up to 10 minutes
}
```

#### Example 2: Manually Setting the Project Key (for local analysis)

While the CI pipeline provides the project key, you might want to run a local analysis against a specific Sonar project.

*   **How:** You can set the project key and token using Gradle properties for a single run.
*   **Where:** Command Line

```bash
./gradlew sonar \
  -Pep.sonar.project.key="my-local-test-project" \
  -Pep.sonar.token="sqp_xxxxxxxxxxxxxxxxxxxx"
```

---

### Common Mistakes and "Gotchas"

1.  **Mistake:** The build fails with `sonar.project.key.undefined` or `sonar.token.undefined`.
    *   **Problem:** The plugin was not able to find the SonarQube project key or token. This almost always happens in a CI environment.
    *   **Solution:** Check the `.gitlab-ci.yml` file and the GitLab CI/CD variable settings. The required environment variables (e.g., `EP_SONAR_PROJECT_KEY` and `EP_SONAR_TOKEN`) are likely missing or named incorrectly.

---

### 3.5 The `MavenPublishPlugin`

**Purpose:** To publish your project's output (typically a JAR file) to a Maven repository, like Nexus or Artifactory.

**Key Features:**

*   **Simple Wrapper:** It wraps the standard `maven-publish` Gradle plugin.
*   **Centralized Configuration:** It configures the repository URL and credentials using the standard `SystemOverrideResolver`, so you don't have to put secrets in your build script.

**Key Tasks it Configures:**

*   `publishToMavenRepository`: This task, which comes from the underlying `maven-publish` plugin, is responsible for the actual upload.
*   The plugin ensures that this task is a dependency of the main **`ciPublishArtifact`** lifecycle task.

**Default Behavior (Out-of-the-Box):**

*   It configures a single Maven repository named "boondiRepository".
*   It expects the repository URL, username, and password to be provided via the configuration system (e.g., GitLab CI environment variables).

---

### Configuration & Overrides

Configuration goes into a `boondiMavenPublish { ... }` block. You rarely need to do this, as the settings are almost always provided by the CI environment.

#### Example: Overriding the Repository URL

*   **How:** Set the `publishRepository` property.
*   **Where:** `build.gradle.kts`

```kotlin
// In build.gradle.kts
import java.net.URI

boondiMavenPublish {
  publishRepository.set(URI.create("https://my-other-nexus.com/repository/releases"))
}
```

---

### Common Mistakes and "Gotchas"

1.  **Mistake:** The `ciPublishArtifact` task fails with a `401 Unauthorized` error.
    *   **Problem:** The username and password provided to the plugin are incorrect or missing.
    *   **Solution:** Verify the environment variables in your CI/CD pipeline (e.g., `EP_MAVEN_PUBLISH_USERNAME_PROPERTY_KEY`, `EP_MAVEN_PUBLISH_PASSWORD_PROPERTY_KEY`) are set correctly in the GitLab project settings.

---

## Part 4: Practical Recipes & Common Workflows

This section provides quick, copy-pasteable answers to common "How do I...?" questions.

| How do I... | Command | Explanation |
| :--- | :--- | :--- |
| **...run only the unit tests?** | `./gradlew test` | This runs only the `test` task, which is mapped to the `src/test/java` directory. It will be much faster than `ciBuild`. |
| **...run only the integration tests?** | `./gradlew integrationTest` | This runs the `integrationTest` task, which is mapped to the `src/integrationTest/java` directory. |
| **...format all my code before committing?** | `./gradlew spotlessApply` | This finds and fixes all formatting issues in your changed files. Run this before `git commit`. |
| **...find out my current test coverage?** | `./gradlew test jacocoTestReport` | After this runs, open `build/reports/jacoco/test/html/index.html` in a browser to see the detailed report. |
| **...add a new third-party library?** | `dependencies { implementation("group:name:version") }` | Add the dependency to the `dependencies` block in your `build.gradle.kts`, similar to Maven. Use `implementation` for main code and `testImplementation` for test-only libraries. |
| **...clean the entire project?** | `./gradlew clean` | This is a standard Gradle task that deletes the `build` directory, giving you a fresh start. |
| **...skip all tests for a quick build?** | `./gradlew assemble -x test` | The `assemble` task builds the JAR without running tests. The `-x test` flag explicitly excludes the `test` task from the run. Use this with caution! |
| **...understand why my GitLab pipeline failed?** | *(In GitLab UI)* | Go to the failed pipeline job. The log will show the exact `./gradlew` command that was run and its full output. Look for the `BUILD FAILED` message and read the lines just above it for the error. The most common failures are from `spotlessCheck` or `jacocoTestCoverageVerification`. |

---

## Part 5: Troubleshooting and Reference

### 5.1 Common Mistakes (Summary)
*   **Failing `spotlessCheck`:** You forgot to run `./gradlew spotlessApply` before committing.
*   **Failing `jacocoTestCoverageVerification`:** You have code that is not covered by a unit test.
*   **Failing with `taskId.not-found`:** Your commit message is missing a JIRA/ticket ID.
*   **Failing with `401 Unauthorized` during publish:** Your credentials (username/password) are wrong in the CI environment variables.
*   **Placing the wrong type of test in `src/test/java`:** Leads to slow local builds and potential failures if external systems are needed.

### 5.2 Glossary of Terms
*   **Task:** A single unit of work in Gradle (like a Maven Goal).
*   **Wrapper (`gradlew`):** A script that automatically downloads and runs the correct Gradle version for a project. Always use this.
*   **Source Set:** A collection of source files in Gradle (e.g., `main`, `test`, `integrationTest`). It's how Gradle groups code for different purposes.
*   **Extension:** A configuration object in Gradle that allows you to set properties for a plugin (e.g., `boondiJava { ... }`).
*   **Ratchet:** A technique used by Spotless to only check files that have been changed relative to a target branch.

---

This completes the comprehensive guide based on the code provided. I recommend you read through it carefully. If you find any areas that are unclear or wish to make additions or corrections, just let me know, and we can refine it further.