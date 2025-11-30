# Code Review with Codex

Review the current changes in the repository using Codex for in-depth analysis.

## Steps to Execute

Follow the **Common Command Instructions** from @.claude/templates/review-base.md, with these Codex-specific customizations:

### 1-3. Follow Common Steps

Execute steps 1-3 from the Common Command Instructions section in the template:
- Verify changes exist
- Read project documentation (optional)
- Gather current context

### 4. Generate Codex-Specific Prompt

Read the guidelines from @.claude/templates/review-base.md and follow Step 4 (Generate Customized Prompt), using this Codex-specific introduction template:

**Codex-Specific Introduction Template:**

```
You are an expert code reviewer analyzing code changes for the Historian Android library.

You are running in Codex MCP with workspace-write sandbox mode, which gives you access to:
- Bash commands (git, file operations, etc.)
- File reading capabilities
- Full repository access

Project context:
- Working directory: [actual pwd output]
- Current branch: [actual branch name]
- Main branch: master

Project overview:
- Historian is an Android library that saves Timber logs to SQLite for debugging
- Multi-module structure: historian-core, historian-tree, sample
- Key classes: Historian (main API), HistorianTree (Timber adapter), LogWriter (persistence)

Your task is to perform a comprehensive code review covering workspace changes
and branch context as needed. Your separate execution environment allows for
thorough analysis and extensive git operations.
```

**Then complete the prompt by:**
- Replacing `[AVAILABLE_READ_TOOL]` with "bash cat command or file reading" in git analysis instructions
- Including all template sections as per Common Command Instructions Step 4
- Emphasizing Codex's strengths: separate environment, extensive git access, methodical review

### 5. Execute Codex Review

Call the `mcp__codex__codex` tool with the fully customized prompt:

```
Tool: mcp__codex__codex
Parameters:
{
  "cwd": "<absolute-path-to-project-root>",
  "sandbox": "workspace-write",
  "prompt": "<fully customized prompt from step 4>"
}
```

### 6. Present Findings

Present the results directly to the user with clear sections as specified in the template's Expected Output Format

## Usage

Simply type `/codex-review` in Claude Code to trigger this review workflow.
