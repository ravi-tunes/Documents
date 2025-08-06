Excellent. Let's begin.

Here is the first part of your guide. It's designed to set the stage, translate your existing Maven knowledge, and get you running your first build successfully.

---

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

---

You have now successfully built the project and know how to view the most important outputs. This covers the most common local workflow.

Once you've had a chance to read this and are comfortable with the concepts, let me know, and we can proceed to **Part 2: The Core System - How It All Works**, where we'll dive into the architecture of the CI Lifecycle and the configuration system.