# check PR's Review comments and CI status, resolve any issue

## 1. Identify the PR

If you don't understand which PR you should look into, ask user first.

## 2. Get review comments - MANDATORY EXECUTION

**CRITICAL**: You MUST actually execute these commands and retrieve ALL comments. Do not skip this step or make assumptions about what comments exist.

**EXECUTION REQUIREMENT**: You MUST run ALL of these commands and display the raw output before proceeding to evaluation.

### To get active inline code review comments (excluding minimized):

```bash
gh api repos/{owner}/{repo}/pulls/{PR_NUMBER}/comments --jq '
  [.[] | select(.performed_via_github_app == null or (.performed_via_github_app.name == "GitHub Actions" | not)) | select(has("minimized_reason") | not)]
  | sort_by(.created_at) | reverse
  | .[]
  | "Created: \(.created_at)\nFile: \(.path)\nLine: \(.line // .original_line)\nAuthor: \(.user.login)\nComment: \(.body)\n---"
'
```

### To get active review summaries (excluding minimized):

```bash
gh api repos/{owner}/{repo}/pulls/{PR_NUMBER}/reviews --jq '
  [.[] | select(has("body") and .body and (.body == "" | not) and (.body == null | not))]
  | group_by(.user.login)
  | map(sort_by(.submitted_at) | reverse | .[0])
  | sort_by(.submitted_at) | reverse
  | .[]
  | "Submitted: \(.submitted_at)\nReviewer: \(.user.login)\nState: \(.state)\nBody: \(.body)\n---"
'
```

### To get general PR comments:

```bash
gh pr view [PR_NUMBER] --comments
```

### Important filtering rules:

1. **Minimized comments are automatically filtered out** - these are hidden/outdated
2. **Only the LATEST review from each reviewer is shown** - older reviews are ignored
3. **Focus on inline comments for specific actionable issues**
4. **Review summaries provide overall context**
5. **Comments sorted by newest first** - most recent feedback takes priority

### What gets filtered out:

- ✅ Comments with `minimized_reason` field (hidden by GitHub)
- ✅ Old reviews superseded by newer ones from same reviewer
- ✅ Bot comments from GitHub Actions (unless relevant)
- ✅ Empty review bodies

## 2.5. Verify completeness - MANDATORY CHECKLIST

**STOP**: Before proceeding to evaluation, you MUST complete this verification checklist and report the results:

### Verification Checklist:

Run these verification commands and report the counts:

```bash
# Count inline comments (non-minimized)
gh api repos/{owner}/{repo}/pulls/{PR_NUMBER}/comments --jq '[.[] | select(has("minimized_reason") | not)] | length'

# List all reviewers who left inline comments
gh api repos/{owner}/{repo}/pulls/{PR_NUMBER}/comments --jq '[.[] | select(has("minimized_reason") | not) | .user.login] | unique'

# Count review summaries
gh api repos/{owner}/{repo}/pulls/{PR_NUMBER}/reviews --jq '[.[] | select(has("body") and .body and (.body == "" | not))] | group_by(.user.login) | length'

# List all reviewers who left review summaries
gh api repos/{owner}/{repo}/pulls/{PR_NUMBER}/reviews --jq '[.[] | select(has("body") and .body and (.body == "" | not)) | .user.login] | unique'
```

### Report format (MANDATORY):

You MUST create a summary table like this BEFORE evaluation:

```
## Comment Retrieval Verification

### Inline Comments Retrieved:
- Total count: X comments
- Reviewers: [list all reviewer names including bots]
- Breakdown by reviewer:
  - Copilot: X comments
  - user1: X comments
  - etc.

### Review Summaries Retrieved:
- Total count: X reviews
- Reviewers: [list all]

### Cross-check with review summaries:
- Review summary claimed: "X comments" ✅/❌ Matches actual count
- All reviewers accounted for: ✅/❌

### Verification Status:
- [ ] Retrieved all inline comments
- [ ] Retrieved all review summaries
- [ ] Counted and verified comment counts
- [ ] Listed all reviewers (humans AND bots)
- [ ] Cross-checked claimed vs actual comment counts
- [ ] Ready to proceed to evaluation

**DO NOT PROCEED** until all checkboxes are marked.
```

## 3. Evaluate and prioritize issues

**CRITICAL**: You MUST independently evaluate EVERY review comment that was verified in step 2.5, regardless of the reviewer's suggested priority.

**Count verification**: The number of comments you evaluate MUST match the count from step 2.5. If you verified 11 inline comments, you MUST evaluate all 11.

### For EACH review comment, analyze and report:

1. **What the issue is**: Clearly describe the problem or suggestion
2. **Reviewer's priority**: What did they say (e.g., "optional", "nitpick", "critical")
3. **Your independent assessment**:
    - Code quality impact (readability, maintainability, correctness)
    - Alignment with project patterns and standards
    - Cost vs benefit (effort required vs value gained)
    - Risk of regression or breaking changes
    - Whether it should be fixed now, deferred, or skipped
4. **Your decision**: Fix now / Defer to future PR / Skip entirely
5. **Reasoning**: Explain WHY you made this decision

### Example analysis format:

```
## Review Comment #1: Unused import `hasScrollAction`
- **Reviewer**: Copilot
- **Reviewer's Priority**: Standard issue
- **Impact**: Low - keeps code clean, no functional impact
- **Cost**: Very low - just delete one line
- **Alignment**: Follows project cleanliness standards
- **Decision**: ✅ FIX NOW
- **Reasoning**: Quick fix, improves code cleanliness, zero risk

## Review Comment #2: Use callback instead of LocalContext for Toast
- **Reviewer**: Claude
- **Reviewer's Priority**: Optional improvement
- **Impact**: Medium - improves testability and follows Compose best practices
- **Cost**: Medium - requires changing Fragment and Route signatures
- **Alignment**: Matches ProcessEvents pattern used elsewhere
- **Decision**: ✅ FIX NOW
- **Reasoning**: Improves testability, aligns with existing patterns, worth the effort

## Review Comment #3: Add more Preview variations
- **Reviewer**: Claude
- **Reviewer's Priority**: Optional nice-to-have
- **Impact**: Low - development convenience only
- **Cost**: Low-Medium - need to create additional @Preview functions
- **Alignment**: Not critical for this PR
- **Decision**: ⏭️ DEFER
- **Reasoning**: Adds value but not essential for this migration task, can be added incrementally
```

### Present your analysis BEFORE making changes

After evaluating all comments, present your complete analysis to the user with:
- **Count summary**: "Evaluated X out of X comments" (MUST be 100%)
- Summary table of all issues
- Your decisions and reasoning
- Which fixes you'll implement
- Which you'll defer or skip and why

**Verification before proceeding**:
- Confirm: "I have evaluated all [count] inline comments and [count] review summaries verified in step 2.5"
- If counts don't match, STOP and go back to retrieve missing comments

**Then wait for user confirmation before proceeding with fixes.**

## 4. Create commits

You MUST create one commit for each problem you fix.

## 5. Verify changes

Check if the build, test and lint succeeds once you finished fixing:

```bash
./gradlew assembleDebug testDebugUnitTest lint
```

## 6. Push changes

When it's ok, commit the fix and push.