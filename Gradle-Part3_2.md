Got it. Let's move on to the `GitPlugin`. This one is fascinating because it acts as an information provider for many of the other plugins, making the build "smarter" and more aware of its context.

---

### 3.2 The `GitPlugin`

**Purpose:** To extract information from the Git repository and make it available to the build script and other plugins. It's the "eyes and ears" of the build system, allowing it to know what branch it's on, what the last commit message was, and whether it's running in a pull request.

**Key Features (it doesn't add many user-facing tasks, but provides critical info):**

*   **Reads Git Information Lazily:** It uses efficient `ValueSource`s to query Git for information only when needed.
*   **Parses Commit Messages:** It has a powerful parser that understands **Conventional Commits**. It can automatically extract:
    *   The **Task ID** (e.g., `JIRA-1234`)
    *   The commit type (`feat`, `fix`, `chore`, etc.)
    *   The scope and a detailed message.
*   **Provides Context to Other Plugins:** The `SonarPlugin` uses this to configure merge request analysis, and the `JavaPlugin` uses the Task ID to run specific acceptance tests.
*   **Manages Git Hooks:** It can install a `pre-commit` hook to help developers run checks locally before they even push their code.

**Default Behavior (Out-of-the-Box):**

*   The plugin automatically finds the root of your Git repository (by searching for the `.git` directory).
*   It assumes your main/release branch is named `main`.
*   It automatically reads the last commit message and parses it. If the commit message does not contain a valid Task ID (e.g., `TICKET-123`), the build will fail.
*   The Git pre-commit hook is **disabled** by default.

---

### Configuration & Overrides

Configuration for this plugin goes inside a `boondiGit { ... }` block in your `build.gradle.kts` file. You will rarely need to change these settings.

#### Example 1: Changing the Release Branch Name

If your project uses an older convention and your main branch is named `master` or `develop`, you must tell the plugin.

*   **How:** Set the `releaseBranch` property.
*   **Where:** `build.gradle.kts`

```kotlin
// In build.gradle.kts
boondiGit {
  releaseBranch.set("master")
}
```

#### Example 2: Enabling the Git Pre-Commit Hook

This is a very useful feature for local development. When enabled, the plugin will create a file at `.git/hooks/pre-commit`. Now, every time you run `git commit`, this script will run `ciBuild` *first*. If the build fails, the commit is aborted. This prevents you from ever committing broken code.

*   **How:** Set the `gitHookEnabled` property to `true`.
*   **Where:** `build.gradle.kts`

```kotlin
// In build.gradle.kts
boondiGit {
  gitHookEnabled.set(true)
}
```
**Important:** After setting this to true, you must run the bootstrap task once to install the hook:
```bash
./gradlew ciBootstrap
```

---

### Common Mistakes and "Gotchas"

1.  **Mistake:** The build fails with a message like `git.commit.taskId.not-found`.
    *   **Problem:** Your most recent commit message is missing a required task/ticket ID in the format `[A-Z]-[0-9]`.
    *   **Solution:** Amend your last commit to include the ID.
        ```bash
        # Bad commit message: "fixed a bug"
        # Good commit message: "JIRA-1234 fix: correct the null pointer exception on login"

        git commit --amend -m "JIRA-1234 fix: correct the null pointer exception on login"
        ```

2.  **Mistake:** Working in a directory that is not part of a Git repository.
    *   **Problem:** The build will fail immediately with a `boondiGitPluginException: git.extension.gitDir.not-found` error.
    *   **Solution:** Ensure you have run `git init` and have made at least one commit in your project directory or one of its parent directories.

3.  **Convention Alert:** The commit message format is a **strict convention**. The `CommitMessage.kt` file defines the exact regular expressions used for parsing. While it attempts to support both "Conventional Commits" and simpler formats, the presence of a Task ID is non-negotiable. This is a core design decision to ensure every commit is traceable to a piece of work.

---

The `GitPlugin` is a crucial, "behind-the-scenes" component that enables much of the automation in the build.

When you're ready, we can proceed to the next section: **3.3 The `FormatterPlugin`**, which keeps your codebase clean and consistent.