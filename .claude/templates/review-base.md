# Code Review Prompt Template

This template provides guidelines for generating customized code review prompts. Commands should use this as a reference to build agent-specific prompts, NOT copy it verbatim.

## How to Use This Template

When generating a review prompt:
1. **Read this template** to understand the structure and requirements
2. **Customize for the agent type**:
   - Add agent-specific context (tools available, execution environment)
   - Emphasize relevant expertise (Android/Compose for android-compose-architect)
   - Adjust technical depth based on agent capabilities
3. **Include session context**: Working directory, current branch, project specifics
4. **Adapt review focus**: Prioritize checks based on what the agent does best

## Common Command Instructions

All review commands (agent-review, codex-review) should follow these common steps:

### Review Scope

The review can examine:
- Current workspace changes (staged and unstaged)
- Entire branch context (all commits since branching from main)
- Committed files for full context when needed

### Step 1: Verify Changes Exist

Run `git status` to confirm there are changes to review

### Step 2: Read Project Documentation (Optional)

If relevant to the changes, read project-specific documentation:
- `docs/mvi-architecture.md` - MVI architecture details (if exists)
- `.spec-workflow/` - Current specification documents (if any exist)
- `build-logic/README.md` - Build system conventions (if changes involve build config)
- Any task-specific documentation from `.spec-workflow/specs/` related to the changes

### Step 3: Gather Current Context

Before generating the prompt, gather:
- Current working directory: `pwd`
- Current branch name: `git rev-parse --abbrev-ref HEAD`
- Main branch name: typically `master` or `main`

### Step 4: Generate Customized Prompt

**IMPORTANT**: Do NOT simply copy this template. Generate a NEW prompt that:

1. **Includes an agent/backend-specific introduction**
   - State the agent type and expertise
   - Describe the execution environment
   - List available tools and capabilities

2. **Includes actual context values**
   - Replace `[PROJECT_PATH]` with actual working directory (from pwd)
   - Replace `[BRANCH_NAME]` with current branch name (from git)
   - Replace `[MAIN_BRANCH]` with main branch name (typically "master" or "main")
   - Add project-specific architecture details (see Review Prompt Structure section below)

3. **Incorporates template sections** with placeholders replaced:
   - Git analysis instructions (section 2) - replace `[MAIN_BRANCH]` with actual branch
   - Project documentation references (section 3)
   - Review criteria (section 4) - emphasize based on agent type
   - Agent-specific focus areas (section 5)
   - Output format requirements

4. **Emphasizes the agent/backend's specific strengths**
   - For Task agents: mention available tools (Bash, Read, etc.)
   - For Codex: mention separate execution environment, extensive git access
   - For android-compose-architect: emphasize Android/Compose expertise

### Prompt Requirements

The generated prompt must be:
- **Complete and standalone** (no references to external templates)
- **Contextual** (includes actual working directory, branch names)
- **Environment-specific** (mentions available tools and execution context)
- **Tailored to agent expertise** (emphasizes relevant knowledge areas)

## Review Prompt Structure

### 1. Introduction & Context
```
You are [AGENT_TYPE] reviewing code changes for this project.

You are running in [EXECUTION_ENVIRONMENT] with access to:
- [LIST_OF_TOOLS_AVAILABLE]
- [SPECIFIC_CAPABILITIES]

Project context:
- Working directory: [PROJECT_PATH]
- Current branch: [BRANCH_NAME]
- Main branch: [MAIN_BRANCH]

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
- `git log [MAIN_BRANCH]..HEAD --oneline` - to see all commits in the current branch
- `git diff [MAIN_BRANCH]...HEAD --stat` - to see all changes in the branch (summary)
- `git diff [MAIN_BRANCH]...HEAD` - to see full diff of all branch changes

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
- docs/mvi-architecture.md - MVI architecture patterns (if changes involve MVI)
- .spec-workflow/ - Check for active specifications related to these changes
- build-logic/README.md - Build system conventions (if changes involve build config)
```

### 4. Review Criteria

Customize this section based on agent expertise. Include all relevant criteria, but emphasize areas where the agent excels.

```
Review ALL changes (both staged and unstaged) and analyze them for:

1. **Code Quality**:
   - Adherence to Kotlin coding standards
   - Named parameters usage (mandatory for multi-param calls)
   - No fully qualified names (proper imports required)
   - Code readability and maintainability

2. **Architecture**:
   - MVI pattern compliance (Store/Reducer/Processor)
   - Clean architecture principles
   - Module boundaries and dependencies
   - Proper separation of concerns

3. **Android Best Practices**:
   - Jetpack Compose usage (if applicable)
   - Room database patterns
   - Coroutines and Flow usage
   - View binding patterns

4. **Testing**:
   - Test coverage for new/changed code (MANDATORY: all new behavior must have tests)
   - Test quality and assertions
   - Use of test utilities from :core:testing
   - MockK and Turbine usage
   - Error states and edge cases covered

5. **Potential Issues**:
   - Performance concerns
   - Memory leaks
   - Threading issues
   - Resource management

6. **Security**:
   - No hardcoded secrets
   - Proper data handling
   - Input validation

7. **Documentation**:
   - Code comments where necessary
   - KDoc for public APIs
   - README updates if needed

Please provide:
- ✅ What's done well
- ⚠️ Issues that should be fixed
- 💡 Suggestions for improvement
- 🔴 Critical problems that must be addressed

Be specific with file paths and line references where possible.
```

### 5. Agent-Specific Focus Areas

**For android-compose-architect:**
- Emphasize: Jetpack Compose best practices, MVI patterns, Android lifecycle, threading
- Deep dive: Compose recomposition, state management, navigation patterns
- Catch: Memory leaks in ViewModels, improper Compose usage, lifecycle violations

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
- app/src/main/java/com/example/ui/TimerDetailScreen.kt (+156, -89)
- domain/src/main/java/com/example/domain/GetTimerUseCase.kt (+12, -5)
- data/repository/src/main/java/com/example/data/TimerRepository.kt (+8, -3)

Unstaged:
- app/src/test/java/com/example/ui/TimerDetailScreenTest.kt (+45, -0)

### Branch Context (if reviewed)
Commits in branch (3 commits):
- abc1234 feat: Add timer detail screen
- def5678 refactor: Update use case for new requirements
- ghi9012 test: Add tests for timer detail

Files changed in entire branch:
- 8 files changed, 245 insertions(+), 112 deletions(-)

Note: Changes span multiple modules (UI, domain, data) - verify clean architecture boundaries.

## Review Results

✅ What's done well
- Clean separation of concerns across modules (app, domain, data)
- Named parameters used consistently at TimerDetailScreen.kt:42, 67, 91
- Proper MVI state management in TimerDetailScreen.kt:25-35

⚠️ Issues that should be fixed
- TimerRepository.kt:45: Missing error handling for database operation
- GetTimerUseCase.kt:23: Consider adding input validation

💡 Suggestions for improvement
- TimerDetailScreen.kt:120: Extract complex composable into separate function
- Consider adding integration test for cross-module flow

🔴 Critical problems
- TimerDetailScreenTest.kt: Missing test coverage for error states
  (Note: All new/changed behavior must have corresponding tests)
```
