Excellent. I have mentally updated the guide to include the exhaustive test directory structure and the "Testing Strategy" rationale within Part 2. This will make the final guide more complete and accurate.

Let's now move on to the details of what each plugin does for you. We'll start with the most important one for a Java developer.

---

## Part 3: The Feature Plugins - A Deep Dive (Section 1: The Java Plugin)

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
*   It automatically adds all the special testing directories (source sets) we discussed in Part 2.
*   It requires **100% line coverage** for your code as measured by unit tests. If you add a new line of code and don't write a unit test for it, the `ciBuild` will fail.
*   It automatically applies **Lombok** to your project to reduce boilerplate code.
*   It automatically adds a standard set of useful testing libraries to your project, including **JUnit 5**, **Mockito**, **AssertJ**, and **Datafaker**. You don't need to declare these in your `build.gradle.kts`.

---

### Configuration & Overrides

While the defaults are powerful, you will occasionally need to change them. All overrides for this plugin go inside a `boondiJava { ... }` block in your `build.gradle.kts` file.

#### Example 1: Changing the Java Version

If your project is legacy and still needs to use Java 17, you can override the default.

*   **How:** Set the `version` property.
*   **Where:** `build.gradle.kts`

```kotlin
// In build.gradle.kts
boondiJava {
  version.set("17")
}
```

#### Example 2: Temporarily Lowering Code Coverage

Imagine you are working on a complex new feature and cannot immediately achieve 100% coverage. You can temporarily lower the requirement, but you **must provide a reason**. This is a great feature for enforcing accountability.

*   **How:** Set the `lineCoverage` property with a `StandardOverride`.
*   **Where:** `build.gradle.kts`

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
When you run the build, the plugin will log a warning with your reason, making it visible to everyone.

#### Example 3: Excluding a Class from Code Coverage

Sometimes you have code that is difficult or impossible to unit test (e.g., a generated DTO class, or code that interacts with a complex static framework). You can exclude specific files from the coverage calculation.

*   **How:** Add a `StandardOverride` to the `coverageExcludedClasses` list property. **The path must use forward slashes `/`**.
*   **Where:** `build.gradle.kts`

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

### Common Mistakes and "Gotchas"

1.  **Mistake:** Placing integration tests in `src/test/java`.
    *   **Problem:** This will cause them to run during the fast `ciBuild` task, potentially slowing it down or causing it to fail if external services aren't available.
    *   **Solution:** Always move tests that require external systems (databases, APIs) to the appropriate directory, like `src/integrationTest/java`.

2.  **Mistake:** The build fails with a message like `Line coverage of 0.98 missed goal of 1.0`.
    *   **Problem:** You've added new code but haven't written a unit test that executes it.
    *   **Solution:** Open the coverage report at `build/reports/jacoco/test/html/index.html`. It will show you exactly which lines are highlighted in red (uncovered). Write a test to cover that logic. If you absolutely cannot test it, use the `coverageExcludedClasses` override with a good reason.

3.  **Mistake:** Forgetting to provide a reason for an override.
    *   **Problem:** The `StandardOverride` class requires a non-empty `reason` string. The build will fail if you leave it out.
    *   **Solution:** Always explain *why* you are deviating from the standard. This helps your team understand the technical debt being introduced.

---
This covers the `JavaPlugin` in detail. Once you're ready, we can proceed to the next section: **3.2 The `GitPlugin`**, which is responsible for making the build "aware" of its context within the Git repository.