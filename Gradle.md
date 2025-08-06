Of course. I will provide each part of the guide in a separate Markdown block. You can click the "Copy" button in the top-right corner of each block and paste the content into a new text file on your computer, saving it with the suggested `.md` filename.

Here are the files.

### File 1: `01-Part1-Quick-Start.md`

```markdown
# The Boondi Gradle Plugin: A Developer's Guide

## Part 1: The "Why" and the "Quick Start"

Welcome! This guide is for Java developers who are new to this project's build system. If you have experience with Maven, you'll find that many concepts are similar, but the way they are implemented is different. This guide will help you become an expert in using this powerful plugin suite.

### 1.1 Introduction & Philosophy: Why Does This Plugin Exist?

The main goal of this plugin suite is to provide **Convention over Configuration**.

This means it's designed to standardize and automate the entire development lifecycle, from writing code locally to deploying it in a CI/CD pipeline. By providing smart, pre-configured defaults for common tasks, it allows developers to focus on writing business logic instead of wrestling with build scripts.

It automates and standardizes:
*   **Building:** Compiling your Java code.
*   **Testing:** Running unit, integration, and many other types of tests.
*   **Formatting:** Keeping all code in the repository consistently styled.
*   **Analysis:** Checking code quality with SonarQube and test coverage with JaCoCo.
*   **Publishing:** Packaging and publishing your application (e.g., as a JAR file) to a Maven repository.

### 1.2 Core Concepts for the Maven Developer

Let's translate your Maven experience to the world of Gradle and this specific plugin.

| Concept in Maven | How it works in "Boondi Gradle" | Key Takeaway |
| :--- | :--- | :--- |
| **Build File** | Maven uses `pom.xml`. | This project uses `build.gradle.kts`. It's not XML; it's written in Kotlin, giving it more power and flexibility. |
| **Unit of Work** | You run Maven **Goals** (e.g., `mvn compile`). | You run Gradle **Tasks** (e.g., `./gradlew build`). You'll almost always use the `./gradlew` wrapper script, which means you **don't need to install Gradle yourself**. |
| **Build Lifecycle** | Maven has a fixed lifecycle (`validate`, `compile`, `test`, `package`, `verify`, `install`, `deploy`). | This plugin defines its own, more explicit **CI Lifecycle** with tasks like `ciBuild`, `ciTest`, `ciPublishArtifact`. These "parent" tasks are the main entry points for both local and CI builds. |
| **Dependencies** | You define dependencies in a `<dependencies>` block. | You define them in a `dependencies { ... }` block in `build.gradle.kts`. A common `implementation` scope is similar to Maven's `<scope>compile</scope>`, and `testImplementation` is like `<scope>test</scope>`. |
| **CI/CD Pipeline** | You might be used to a `Jenkinsfile`. | This project uses a `.gitlab-ci.yml` file. This is just a YAML file in the repository that tells GitLab's automation servers which `./gradlew` commands to run for a given commit or merge request. It often configures the build by setting environment variables, which the plugin is designed to read automatically. |

### 1.3 Quick Start: Your First Local Build

Let's get you from a fresh `git clone` to a successful build on your local machine.

#### Prerequisites
*   A Java Development Kit (JDK), version 21, installed on your machine.
*   You **do not** need to install Gradle. The project includes a wrapper (`gradlew`) that will download and use the correct version automatically.

#### Step 1: Clone the Repository
Get the code onto your machine as you normally would.
```bash
git clone <your-repository-url>
cd <your-repository-name>
```

#### Step 2: Format Your Code (The "Fix" Task)
Before you even build, it's good practice to ensure your code is formatted correctly. The plugin uses a tool called "Spotless" for this.

Run the following command in your terminal at the root of the project:
```bash
./gradlew spotlessApply
```
This command finds all source files (Java, Kotlin, YAML, etc.) and automatically reformats them to match the project's style guide. Doing this first prevents annoying "linting" errors in the main build.

#### Step 3: Run the Main Build Lifecycle Task
Now, let's run the primary task that simulates what the CI pipeline does. This single command will compile your code, run the unit tests, and perform analysis.

```bash
./gradlew ciBuild
```
This powerful task will:
1.  Compile your main source code (`src/main/java`).
2.  Compile your test source code (`src/test/java`).
3.  Run all the unit tests.
4.  If the tests pass, it will generate a JaCoCo code coverage report.
5.  It will then check if your coverage meets the minimum threshold (e.g., 100%).

> **Pro Tip:** If you want to see what tasks a command would run *without* actually running them, you can use the `--dry-run` flag. For example: `./gradlew ciBuild --dry-run`.

#### Step 4: Understand the Output
After the command finishes, you should see a message:
```
BUILD SUCCESSFUL in Xs
```
This means everything worked! You can now view the reports that were generated:

*   **Test Results:** Open the file `build/reports/tests/test/index.html` in your web browser. This gives you a clickable report of all the tests that ran.
*   **Code Coverage:** Open `build/reports/jacoco/test/html/index.html` in your browser. This shows you exactly which lines of your code were (or were not) covered by the unit tests.
```

### File 2: `02-Part2-Core-System.md`

```markdown
## Part 2: The Core System - How It All Works

This part explains the foundational "magic" that makes the plugin suite so powerful and consistent. We'll cover the standardized task graph, the flexible configuration system, and the expected project structure.

### 2.1 The Standardized CI Lifecycle

The single most important concept in this plugin suite is the **Standardized CI Lifecycle**, defined in `LifecycleBasePlugin.kt`.

Think of it as a pre-defined skeleton of tasks. These tasks don't do anything by themselves; they are empty "parent" tasks that act as hooks. The other feature plugins (like `JavaPlugin`, `SonarPlugin`, etc.) then attach their own tasks as dependencies to this skeleton.

This creates a predictable and ordered task graph, ensuring that no matter which project you're on, the command `ciBuild` always performs the same logical sequence of operations.

Here are the primary lifecycle tasks you will use:

| Parent Task Name | What It Does (High-Level) | Common Child Tasks Attached by Other Plugins |
| :--- | :--- | :--- |
| **`ciBootstrap`** | Sets up the initial development environment. | `dotEnvWrite`, `gitPreCommitHookUpdate`, `spotlessClean` |
| **`ciSource`** | Lints, fixes, and analyzes the source code itself. | `spotlessCheck`, `spotlessApply` |
| **`ciBuild`** | **The main task you will run locally.** It compiles, tests, and performs static analysis. | `compileJava`, `test` (unit tests), `jacocoTestCoverageVerification`, `sonar` |
| **`ciTest`** | Runs tests that require a deployed environment (e.g., component, integration). | `componentTest`, `integrationTest` |
| **`ciPublishArtifact`** | Publishes the final build artifact (e.g., a JAR file) to a repository. | `publishToMavenRepository` |

**Example: Visualizing the `ciBuild` Task**

When you run `./gradlew ciBuild`, you aren't just running one task. You are running a chain of dependencies. A simplified view looks like this:

```
                  ┌──────────────────┐
                  │     ciBuild      │  (You run this)
                  └────────┬─────────┘
                           │
             ┌─────────────┴─────────────┐
             │                           │
  ┌──────────────────┐        ┌─────────────────────────┐
  │     ciBuildSAST    │        │ ciBuildStaticAnalysis │ (SAST = Static Application Security Testing)
  └────────┬─────────┘        └──────────┬────────────┘
           │                             │
┌──────────────────┐           ┌─────────┴─────────┐
│   ciBuildAssemble  │           │      sonar        │ (From SonarPlugin)
└────────┬─────────┘           └─────────┬─────────┘
         │                               │
┌──────────────────┐        ┌────────────┴───────────────┐
│     compileJava    │        │ jacocoTestCoverageVerification │ (From JavaPlugin)
└──────────────────┘        └───────────┬────────────────┘
                                        │
                               ┌────────┴────────┐
                               │       test      │ (From JavaPlugin)
                               └─────────────────┘
```
This dependency graph guarantees that your code is always compiled before it's tested, and tests are always run before coverage is checked and sent to SonarQube.

### 2.2 The Magic of Configuration (`SystemOverrideResolver`)

How does the GitLab CI pipeline configure your build without changing the `build.gradle.kts` file? The answer is the **`SystemOverrideResolver`**, a shared Gradle service that all the Boondi plugins use.

It provides a single, consistent way to configure *any* property by looking for a value in a specific order of priority.

**The Priority Order:**

The resolver checks these places in order. The **first one it finds, it uses.**

1.  **Environment Variable (uppercase):** `EP_PROPERTY-PREFIX_YOUR_PROPERTY_KEY`
2.  **Environment Variable (lowercase):** `ep_property-prefix_your_property_key`
    *(Note: The plugin checks multiple capitalizations for flexibility/backwards-compatibility).*
3.  **Java System Property:** `-Dproperty.prefix.your.property.key=value`
4.  **Gradle Property:** `-Pproperty.prefix.your.property.key=value` (or in `gradle.properties`)

**Example: Overriding the SonarQube Project Key**

Let's say you want to set the SonarQube project key. The property key for this, found in `SonarExtension.kt`, is `sonar.project.key`.

Here's how you could set it, from highest to lowest priority:

1.  **In GitLab CI (or your shell):** This is the most common way for CI.
    ```yaml
    # In .gitlab-ci.yml
    variables:
      EP_SONAR_PROJECT_KEY: "my-awesome-project"
    ```

2.  **As a Java System Property:**
    ```bash
    ./gradlew ciBuild -Dep.sonar.project.key="my-awesome-project"
    ```

3.  **As a Gradle Property:**
    ```bash
    ./gradlew ciBuild -Pep.sonar.project.key="my-awesome-project"
    ```
    *or, by adding this line to your `gradle.properties` file:*
    ```properties
    ep.sonar.project.key=my-awesome-project
    ```
This system gives you ultimate flexibility. You can set temporary values on the command line for a single run, or have the CI/CD pipeline provide the official configuration for every build.

### 2.3 Project Structure: The Golden Path

The plugin is **opinionated** and expects a specific folder structure to work correctly. Following this convention means the plugin can automatically discover your code and tests without any extra configuration.

#### The Exhaustive List of Test `sourceSet` Directories

This is the "Golden Path" in its entirety. If you create a test, it should live in one of these directories based on its purpose.

```
your-project/
└── src/
    ├── main/
    │   └── java/                     // Not a test, but the main source for context
    │
    ├── test/
    │   └── java/                     // For UnitTestType
    │
    ├── regressionTest/
    │   └── java/                     // For RegressionTestType AND AcceptanceTestType
    │
    ├── contractTest/
    │   └── java/                     // For ContractTestType
    │
    ├── contractVerificationTest/
    │   └── java/                     // For ContractVerificationTestType
    │
    ├── componentTest/
    │   └── java/                     // For ComponentTestType
    │
    ├── integrationTest/
    │   └── java/                     // For IntegrationTestType
    │
    ├── systemTest/
    │   └── java/                     // For SystemTestType
    │
    ├── unstableTest/
    │   └── java/                     // For UnstableTestType
    │
    ├── performanceTest/
    │   └── java/                     // For PerformanceTestType
    │
    ├── largeTest/
    │   └── java/                     // For LargeTestType
    │
    └── postReleaseTest/
        └── java/                     // For PostReleaseTestType
```

#### The Testing Strategy Behind the Structure

This structure reflects a mature testing strategy where different types of tests are executed at different stages of the CI/CD pipeline.

*   **Fast Feedback Loop (`ciBuild`):** `test` (unit tests) are run on every single commit because they are fast and reliable.
*   **Slower Feedback Loop (`ciTest`):** `integrationTest`, `componentTest`, etc., are often slower because they might need to spin up a database or a Docker container. They are typically run after a `ciBuild` succeeds, often after a merge request has been created.
*   **Specialized Pipelines:** `performanceTest` or `postReleaseTest` might only be run on a nightly schedule or after a successful production deployment, triggered by a `nightly` or `release` workflow.

> **Convention Alert:** If you place your integration tests inside `src/test/java`, they will be run as part of the fast, local `ciBuild` task, which is likely not what you want. Always place your tests in the correct source folder (`source set`) based on their type.
```

### File 3: `03-Part3-Feature-Plugins.md`

```markdown
## Part 3: The Feature Plugins - A Deep Dive

This part breaks down each feature plugin, explaining its purpose, the tasks it provides, and how you can configure its behavior.

### 3.1 The `JavaPlugin`

**Purpose:** To build, test, and analyze standard Java projects. This is the workhorse plugin you will interact with the most.

**Key Tasks it Configures:**
*   `compileJava`: Compiles your `src/main/java` source code.
*   `test`: Compiles and runs your unit tests from `src/test/java`.
*   **A task for every other test type:** `integrationTest`, `componentTest`, etc. These tasks are created to run tests from their respective directories.
*   `jacocoTestReport`: Generates an HTML code coverage report after the `test` task runs.
*   `jacocoTestCoverageVerification`: Checks if your code coverage meets the required minimum percentage and fails the build if it doesn't.
*   `jacocoPrintCoverage`: A convenient task that prints the final coverage percentage to your console.

**Default Behavior (Out-of-the-Box):**
*   It expects your project to use **Java 21**.
*   It automatically adds all the special testing directories (source sets).
*   It requires **100% line coverage** for your code as measured by unit tests. If you add a new line of code and don't write a unit test for it, the `ciBuild` will fail.
*   It automatically applies **Lombok** to your project to reduce boilerplate code.
*   It automatically adds a standard set of useful testing libraries to your project, including **JUnit 5**, **Mockito**, **AssertJ**, and **Datafaker**. You don't need to declare these in your `build.gradle.kts`.

---

#### Configuration & Overrides

All overrides for this plugin go inside a `boondiJava { ... }` block in your `build.gradle.kts` file.

**Example 1: Changing the Java Version**
```kotlin
// In build.gradle.kts
boondiJava {
  version.set("17")
}
```

**Example 2: Temporarily Lowering Code Coverage**
```kotlin
// In build.gradle.kts
import com.boondi.ep.gradle.core.configuration.StandardOverride
import java.math.BigDecimal

boondiJava {
  lineCoverage.set(
    StandardOverride(
      value = BigDecimal("0.85"), // New minimum is 85%
      reason = "CREDOPS-456: Temporarily lowered for initial refactoring of the new service."
    )
  )
}
```

**Example 3: Excluding a Class from Code Coverage**
```kotlin
// In build.gradle.kts
import com.boondi.ep.gradle.core.configuration.StandardOverride

boondiJava {
  coverageExcludedClasses.add(
    StandardOverride(
      value = "com/boondi/ep/gradle/MyGeneratedDto.class",
      reason = "This is a generated data class with no logic to test."
    )
  )
  // You can also exclude entire packages
  coverageExcludedClasses.add(
    StandardOverride(
      value = "com/boondi/ep/gradle/legacy/**", // Excludes everything in the 'legacy' package
      reason = "Legacy code to be refactored in Q4."
    )
  )
}
```

---

### 3.2 The `GitPlugin`

**Purpose:** To extract information from the Git repository and make it available to the build script and other plugins. It's the "eyes and ears" of the build system.

**Key Features:**
*   **Reads Git Information Lazily:** It uses efficient `ValueSource`s to query Git for information only when needed.
*   **Parses Commit Messages:** It has a powerful parser that understands **Conventional Commits** and automatically extracts the **Task ID** (e.g., `JIRA-1234`).
*   **Provides Context to Other Plugins:** The `SonarPlugin` uses this to configure merge request analysis, and the `JavaPlugin` uses the Task ID to run specific acceptance tests.
*   **Manages Git Hooks:** It can install a `pre-commit` hook to help developers run checks locally before they even push their code.

**Default Behavior (Out-of-the-Box):**
*   Assumes your main/release branch is named `main`.
*   Requires the last commit message to contain a valid Task ID (e.g., `TICKET-123`), otherwise the build will fail.
*   The Git pre-commit hook is **disabled** by default.

---

#### Configuration & Overrides

Configuration goes inside a `boondiGit { ... }` block.

**Example 1: Changing the Release Branch Name**
```kotlin
// In build.gradle.kts
boondiGit {
  releaseBranch.set("master")
}
```

**Example 2: Enabling the Git Pre-Commit Hook**
```kotlin
// In build.gradle.kts
boondiGit {
  gitHookEnabled.set(true)
}
```
**Important:** After setting this to true, you must run the bootstrap task once to install the hook: `./gradlew ciBootstrap`

---

### 3.3 The `FormatterPlugin`

**Purpose:** To automatically format all source code in the project, ensuring a consistent and clean coding style.

**Key Features:**
*   **Underlying Tool:** It uses the **Spotless** Gradle plugin.
*   **Multi-Language Support:** Formats Java, Kotlin, Gradle Scripts, YAML, and JS/TS.
*   **Efficient Ratcheting:** It only attempts to format files that have changed compared to your main branch.

**Key Tasks it Adds:**
*   `spotlessApply`: **The main task you will use.** It finds and reformats all applicable files.
*   `spotlessCheck`: Checks if all files are formatted correctly and fails the build if not.

---

#### Configuration & Overrides

Configuration goes into a `boondiFormatter { ... }` block.

**Example 1: Changing the Ratchet Branch**
```kotlin
// In build.gradle.kts
boondiFormatter {
  ratchetBranch.set("origin/develop")
}
```

**Example 2: Excluding Additional Files or Directories**
```kotlin
// In build.gradle.kts
boondiFormatter {
  // Exclude a specific generated protobufs directory
  exclude.add("**/generated-sources/protobuf/**")
}
```

---

### 3.4 The `SonarPlugin`

**Purpose:** To perform deep static analysis on your code and report the results to a central SonarQube server.

**Key Features:**
*   **Seamless Integration:** It wraps the official SonarQube Gradle plugin.
*   **Context-Aware:** It uses information from the `GitPlugin` to configure pull request analysis automatically.
*   **Coverage Reporting:** It ensures the JaCoCo report is generated before the Sonar analysis runs.

**Key Tasks it Configures:**
*   `sonar`: The main task that sends all information to the SonarQube server. It is hooked into the `ciBuildStaticAnalysis` lifecycle task.

---

#### Configuration & Overrides

Configuration goes into a `boondiSonar { ... }` block.

**Example 1: Waiting for the Quality Gate**
```kotlin
// In build.gradle.kts
boondiSonar {
  qualityGateWait.set(true)
  qualityGateTimeoutInSeconds.set(600) // Wait up to 10 minutes
}
```

---

### 3.5 The `MavenPublishPlugin`

**Purpose:** To publish your project's output (typically a JAR file) to a Maven repository.

**Key Features:**
*   **Simple Wrapper:** It wraps the standard `maven-publish` Gradle plugin.
*   **Centralized Configuration:** It configures the repository URL and credentials from the environment.

**Key Tasks it Configures:**
*   `publishToMavenRepository`: Uploads the artifact. This is hooked into the `ciPublishArtifact` lifecycle task.

---

#### Configuration & Overrides

Configuration goes into a `boondiMavenPublish { ... }` block.

**Example: Overriding the Repository URL**
```kotlin
// In build.gradle.kts
import java.net.URI

boondiMavenPublish {
  publishRepository.set(URI.create("https://my-other-nexus.com/repository/releases"))
}
```
```

### File 4: `04-Part4-Practical-Recipes.md`

```markdown
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
```

### File 5: `05-Part5-Troubleshooting-Reference.md`

```markdown
## Part 5: Troubleshooting and Reference

### 5.1 Common Mistakes and "Gotchas" (Summary)
*   **Failing `spotlessCheck`:** You forgot to run `./gradlew spotlessApply` before committing.
*   **Failing `jacocoTestCoverageVerification`:** You have code that is not covered by a unit test.
*   **Failing with `taskId.not-found`:** Your commit message is missing a JIRA/ticket ID.
*   **Failing with `401 Unauthorized` during publish:** Your credentials (username/password) are wrong in the CI environment variables.
*   **Placing the wrong type of test in `src/test/java`:** Leads to slow local builds and potential failures if external systems are needed.
*   **Applying the Formatter plugin in a subproject:** The `boondi-formatter` should only be applied in the root project's `build.gradle.kts`.
*   **Forgetting to provide a reason for an override:** The `StandardOverride` class requires a non-empty `reason` string. The build will fail if you leave it out.

### 5.2 Glossary of Terms
*   **Task:** A single unit of work in Gradle (like a Maven Goal).
*   **Wrapper (`gradlew`):** A script that automatically downloads and runs the correct Gradle version for a project. Always use this.
*   **Source Set:** A collection of source files in Gradle (e.g., `main`, `test`, `integrationTest`). It's how Gradle groups code for different purposes.
*   **Extension:** A configuration object in Gradle that allows you to set properties for a plugin (e.g., `boondiJava { ... }`).
*   **Ratchet:** A technique used by Spotless to only check files that have been changed relative to a target branch.
*   **Convention over Configuration:** A software design paradigm that seeks to decrease the number of decisions a developer needs to make, gaining simplicity, but not necessarily losing flexibility. This plugin suite is a prime example.
```
