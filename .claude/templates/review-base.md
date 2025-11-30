# Code Review Prompt Template

This template provides guidelines for generating customized code review prompts for the Historian Android library. Commands should use this as a reference to build agent-specific prompts, NOT copy it verbatim.

## How to Use This Template

When generating a review prompt:
1. **Read this template** to understand the structure and requirements
2. **Customize for the agent type**:
   - Add agent-specific context (tools available, execution environment)
   - Emphasize relevant expertise (Android library development, API design)
   - Adjust technical depth based on agent capabilities
3. **Include session context**: Working directory, current branch, project specifics
4. **Adapt review focus**: Prioritize checks based on what the agent does best

## Common Command Instructions

All review commands (agent-review, codex-review) should follow these common steps:

### Review Scope

The review can examine:
- Current workspace changes (staged and unstaged)
- Entire branch context (all commits since branching from master)
- Committed files for full context when needed

### Step 1: Verify Changes Exist

Run `git status` to confirm there are changes to review

### Step 2: Read Project Documentation (Optional)

If relevant to the changes, read project-specific documentation:
- `CLAUDE.md` - Project overview and build commands
- Serena memories (if available) - Architecture and conventions

### Step 3: Gather Current Context

Before generating the prompt, gather:
- Current working directory: `pwd`
- Current branch name: `git rev-parse --abbrev-ref HEAD`
- Main branch name: `master`

### Step 4: Generate Customized Prompt

**IMPORTANT**: Do NOT simply copy this template. Generate a NEW prompt that:

1. **Includes an agent/backend-specific introduction**
   - State the agent type and expertise
   - Describe the execution environment
   - List available tools and capabilities

2. **Includes actual context values**
   - Replace `[PROJECT_PATH]` with actual working directory (from pwd)
   - Replace `[BRANCH_NAME]` with current branch name (from git)
   - Replace `[MAIN_BRANCH]` with `master`
   - Add project-specific architecture details (see Review Prompt Structure section below)

3. **Incorporates template sections** with placeholders replaced:
   - Git analysis instructions (section 2) - replace `[MAIN_BRANCH]` with `master`
   - Project documentation references (section 3)
   - Review criteria (section 4) - emphasize based on agent type
   - Agent-specific focus areas (section 5)
   - Output format requirements

4. **Emphasizes the agent/backend's specific strengths**
   - For Task agents: mention available tools (Bash, Read, etc.)
   - For Codex: mention separate execution environment, extensive git access
   - For android-library-reviewer: emphasize Android library development, API design

### Prompt Requirements

The generated prompt must be:
- **Complete and standalone** (no references to external templates)
- **Contextual** (includes actual working directory, branch names)
- **Environment-specific** (mentions available tools and execution context)
- **Tailored to agent expertise** (emphasizes relevant knowledge areas)

## Review Prompt Structure

### 1. Introduction & Context
```
You are [AGENT_TYPE] reviewing code changes for the Historian Android library.

You are running in [EXECUTION_ENVIRONMENT] with access to:
- [LIST_OF_TOOLS_AVAILABLE]
- [SPECIFIC_CAPABILITIES]

Project context:
- Working directory: [PROJECT_PATH]
- Current branch: [BRANCH_NAME]
- Main branch: master

Project overview:
- Historian is an Android library that saves Timber logs to SQLite for debugging
- Multi-module structure: historian-core, historian-tree, sample
- Key classes: Historian (main API), HistorianTree (Timber adapter), LogWriter (persistence)
```

### 2. Git Analysis Instructions
```
IMPORTANT: First, run git commands to understand what changed:

**Step 1: Check current workspace changes**
- `git status` - to see which files have changes (staged and unstaged)
- `git diff` - to review unstaged changes
- `git diff --cached` - to review staged changes
- `git diff --stat` and `git diff --cached --stat` - for change summaries

**Step 2: Check branch context (if necessary)**
- `git log master..HEAD --oneline` - to see all commits in the current branch
- `git diff master...HEAD --stat` - to see all changes in the branch (summary)
- `git diff master...HEAD` - to see full diff of all branch changes

**Step 3: Read files for context (when needed)**
- Use [AVAILABLE_READ_TOOL] to read the committed files for full context
- This is especially important when:
  - Current changes depend on earlier commits in the branch
  - You need to verify architectural consistency across multiple commits
  - The change touches complex areas that need full file context
  - Testing strategy needs to be evaluated across the whole feature
```

### 3. Project Documentation References
```
Read any relevant project-specific documentation for additional context:
- CLAUDE.md - Project overview, build commands, git conventions
```

### 4. Review Criteria

Customize this section based on agent expertise. Include all relevant criteria, but emphasize areas where the agent excels.

```
Review ALL changes (both staged and unstaged) and analyze them for:

1. **Code Quality**:
   - Adherence to Kotlin coding standards and idioms
   - Proper use of Kotlin features (data classes, extension functions, null safety)
   - Code readability and maintainability

2. **API Design** (for library code):
   - Java interoperability (@JvmStatic, @JvmField, @JvmOverloads)
   - Builder pattern consistency
   - Public API surface (avoid exposing internals)
   - Backwards compatibility

3. **Android Best Practices**:
   - SQLite patterns (transactions, statement management)
   - Thread safety (ExecutorService, @Volatile)
   - Context handling (avoid Activity leaks)
   - Resource management (use{} blocks, proper closing)

4. **Architecture**:
   - Module boundaries (historian-core, historian-tree, sample)
   - Separation of concerns
   - Internal vs public visibility

5. **Testing**:
   - Test coverage for new/changed code
   - Robolectric configuration
   - Thread safety testing

6. **Potential Issues**:
   - Performance concerns
   - Memory leaks
   - Threading issues
   - Resource leaks (database connections, statements)

7. **Build Configuration**:
   - Gradle setup (Kotlin DSL, version catalog)
   - Dependencies properly configured

Please provide:
- ✅ What's done well
- ⚠️ Issues that should be fixed
- 💡 Suggestions for improvement
- 🔴 Critical problems that must be addressed

Be specific with file paths and line references where possible.
```

### 5. Agent-Specific Focus Areas

**For android-library-reviewer:**
- Emphasize: API design, Java interop, thread safety, SQLite patterns
- Deep dive: Public API surface, backwards compatibility, resource management
- Catch: Context leaks, exposed internals, thread safety issues

**For general-purpose:**
- Balanced coverage of all criteria
- Focus on general code quality and architecture
- Standard software engineering best practices

**For Explore agent:**
- Emphasize: Code organization, module structure, architectural patterns
- Deep dive: Codebase navigation, pattern discovery, relationship mapping

## Expected Output Format

The review should include:

1. **Changes Summary**: List of files changed
   - Current workspace changes (staged and unstaged)
   - Branch commits (if reviewed for broader context)
   - Files affected across the entire branch (if applicable)

2. **Review Results**: Analysis with clear sections:
   - ✅ What's done well
   - ⚠️ Issues that should be fixed
   - 💡 Suggestions for improvement
   - 🔴 Critical problems that must be addressed

3. **Actionable Feedback**: Specific file paths and line references for each finding

4. **Critical Issues**: Highlighted problems that must be addressed before merging

**Example Output Format**:
```
## Changes Summary

### Current Workspace
Staged:
- historian-core/src/main/java/net/yslibrary/historian/Historian.kt (+50, -20)
- historian-core/src/main/java/net/yslibrary/historian/internal/LogWriter.kt (+10, -5)

Unstaged:
- historian-core/src/test/java/net/yslibrary/historian/HistorianTest.kt (+25, -0)

### Branch Context (if reviewed)
Commits in branch (2 commits):
- abc1234 feat: Add new logging feature
- def5678 test: Add tests for new feature

Files changed in entire branch:
- 5 files changed, 85 insertions(+), 25 deletions(-)

## Review Results

✅ What's done well
- Proper use of @JvmOverloads for Java callers
- Thread-safe implementation with proper synchronization
- SQLite statements properly closed with use{}

⚠️ Issues that should be fixed
- Historian.kt:45: Missing @JvmStatic annotation for Java callers
- LogWriter.kt:23: Consider adding transaction for batch operations

💡 Suggestions for improvement
- Consider adding KDoc for new public API methods
- Could extract complex logic into separate internal function

🔴 Critical problems
- Historian.kt:67: Potential context leak - storing Activity reference
  (Note: Use applicationContext instead)
```
