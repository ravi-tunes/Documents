Excellent. Now that you have a feel for the basic workflow, let's look under the hood. Understanding these core concepts is the key to mastering the plugin.

---

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

As we covered briefly in the Quick Start, the plugin is **opinionated** and expects a specific folder structure to work correctly. Following this convention means the plugin can automatically discover your code and tests without any extra configuration.

#### Key Directory Conventions:

*   **`src/main/java`**: Your main application source code.
*   **`src/test/java`**: **Only for Unit Tests.** These are tests that have no external dependencies (like databases or networks) and run quickly. They are executed as part of the `ciBuild` task.
*   **`src/integrationTest/java`**: For integration tests. These tests might require a database or other services to be running. They are executed by the separate `ciTest` lifecycle task.
*   **`src/regressionTest/java`**: For regression and acceptance tests.

> **Convention Alert:** If you place your integration tests inside `src/test/java`, they will be run as part of the fast, local `ciBuild` task, which is likely not what you want. Always place your tests in the correct source folder (`source set`) based on their type. Refer back to the list in Part 1 for all possible test directories.

---

You now understand the three pillars of this plugin system: the **Lifecycle** that defines *what* happens, the **Configuration Resolver** that defines *how* it's configured, and the **Folder Structure** that defines *where* things are located.

With this foundation, we are ready to explore what each specific feature plugin does. When you're ready, ask me to proceed to **Part 3: The Feature Plugins - A Deep Dive**, and we'll start with the `JavaPlugin`.

You've asked a very precise and important question.

The list I provided in Part 2 covers the **most common and significant** directory conventions you'll interact with daily. However, it is not 100% exhaustive. The `TestType.kt` file is the ultimate source of truth, and it defines a few more specialized test directories.

Let me give you the **complete, exhaustive list** based on the `TestType.kt` file you provided. This is the full set of `sourceSets` the `JavaPlugin` knows how to create and manage.

### The Exhaustive List of Test `sourceSet` Directories

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

### Why So Many?

This structure reflects a mature testing strategy where different types of tests are executed at different stages of the CI/CD pipeline.

*   **Fast Feedback Loop (`ciBuild`):** `test` (unit tests) are run on every single commit because they are fast and reliable.
*   **Slower Feedback Loop (`ciTest`):** `integrationTest`, `componentTest`, etc., are often slower because they might need to spin up a database or a Docker container. They are typically run after a `ciBuild` succeeds, often after a merge request has been created.
*   **Specialized Pipelines:** `performanceTest` or `postReleaseTest` might only be run on a nightly schedule or after a successful production deployment, triggered by a `nightly` or `release` workflow.

By separating tests into these conventional directories, the build system can automatically select the right tests to run at the right time, without you needing to manually configure anything. Just place your file in the right folder, and the plugin handles the rest.

Thank you for pressing for this level of detail. It's a critical point for using the system correctly. We can now add this exhaustive list to the final guide.

Ready to proceed to **Part 3**?