# Code Review with Task Agent

Review the current changes in the repository using Claude Code's Task agent for in-depth analysis.

## Agent Type Selection

**Default**: `android-library-reviewer` - Optimized for Android library development with expertise in:
- API design and Java interoperability
- SQLite patterns and thread safety
- Kotlin idioms and best practices
- Public API surface management
- Testing strategies for Android libraries

**Optional**: Pass a different agent type as argument: `$ARGUMENTS`

Available agent types:
- `android-library-reviewer` (default) - Android library expert
- `general-purpose` - General codebase review
- `Explore` - Codebase exploration focus

## Steps to Execute

Follow the **Common Command Instructions** from @.claude/templates/review-base.md, with these Task agent-specific customizations:

### 1. Determine Agent Type
- If `$ARGUMENTS` is provided, use that as the subagent type
- Otherwise, default to `android-library-reviewer`

### 2. Follow Common Steps

Execute steps 1-3 from the Common Command Instructions section in the template:
- Verify changes exist
- Read project documentation (optional)
- Gather current context

### 3. Generate Task Agent-Specific Prompt

Read the guidelines from @.claude/templates/review-base.md and follow Step 4 (Generate Customized Prompt), using these Task agent-specific introduction templates:

**Agent-Specific Introduction Templates:**

**For android-library-reviewer:**
```
You are an Android library development expert reviewing code changes
for the Historian Android library.

Your expertise includes:
- API design and Java interoperability (@JvmStatic, @JvmOverloads, etc.)
- SQLite patterns (transactions, statement management, threading)
- Kotlin idioms and best practices
- Thread safety (ExecutorService, @Volatile, synchronization)
- Android context handling and lifecycle
- Testing strategies (Robolectric, unit testing)

You are running as a Claude Code Task agent with access to:
- Bash commands (git, grep, find, etc.)
- Read tool for examining files
- All standard file operations

Project context:
- Working directory: [actual pwd output]
- Current branch: [actual branch name]
- Main branch: master
- Architecture: Multi-module library (historian-core, historian-tree, sample)
- Key classes: Historian (main API), HistorianTree (Timber adapter), LogWriter (persistence)

Your task is to perform an Android library-focused code review, paying special attention to
API design, Java interop, thread safety, and SQLite patterns.
```

**For general-purpose:**
```
You are an expert code reviewer analyzing code changes for the Historian Android library.

You are running as a Claude Code Task agent with access to:
- Bash commands (git, grep, find, etc.)
- Read tool for examining files
- All standard file operations

Project context:
- Working directory: [actual pwd output]
- Current branch: [actual branch name]
- Main branch: master

Your task is to perform a comprehensive code review covering code quality,
architecture, testing, and potential issues.
```

**For Explore:**
```
You are a codebase exploration specialist reviewing code changes for the Historian
Android library.

You are running as a Claude Code Task agent with access to:
- Bash commands (git, grep, find, etc.)
- Read tool for examining files
- All standard file operations

Project context:
- Working directory: [actual pwd output]
- Current branch: [actual branch name]
- Main branch: master

Your task is to review code changes with emphasis on code organization,
module structure, and architectural patterns.
```

**Then complete the prompt by:**
- Replacing `[AVAILABLE_READ_TOOL]` with "Read tool" in git analysis instructions
- Including all template sections as per Common Command Instructions Step 4
- Emphasizing review criteria based on agent type (see template section 5)

### 4. Launch Task Agent

Use the `Task` tool with the fully customized prompt:

**Note:** The Task tool invocation below uses the actual Claude Code API. The `subagent_type` parameter specifies which specialized agent to use, and `prompt` contains the complete customized review instructions generated in step 3.

```
Tool: Task
Parameters:
{
  "subagent_type": "[determined agent type from step 1]",
  "description": "Review Android library code changes",
  "prompt": "<fully customized prompt from step 3>",
  "model": "sonnet"  // Optional: specify model; "sonnet" for thorough analysis, "haiku" for quick reviews
}
```

### 5. Present Findings

Present the results directly to the user with clear sections as specified in the template's Expected Output Format

## Usage

```bash
# Default: Use android-library-reviewer
/agent-review

# Use general-purpose agent
/agent-review general-purpose

# Use Explore agent for codebase-focused review
/agent-review Explore
```

## Comparison with /codex-review

- **`/agent-review`**: Uses Claude Code's Task agent (subagent execution within current session)
    - No external MCP dependencies
    - Integrated with current Claude Code session
    - Good for regular reviews
    - Limited by Claude Code's standard rate limits

- **`/codex-review`**: Uses Codex MCP server (separate execution environment)
    - Separate execution context
    - May have different resource allocation
    - Requires Codex MCP server to be available
    - Subject to Codex MCP rate limits

Choose based on availability and preference. Both use the same review criteria from `@.claude/templates/review-base.md`.
