RELEASING
===

This project uses [release-please](https://github.com/googleapis/release-please) to automate releases.

## How It Works

1. Make changes using [Conventional Commits](https://www.conventionalcommits.org/) format
2. When changes are merged to `master`, release-please automatically creates/updates a Release PR
3. The Release PR updates `CHANGELOG.md` and `gradle.properties` (VERSION_NAME)
4. When the Release PR is merged, a GitHub release is created and the publish workflow runs

## Conventional Commit Types

- `feat:` - New features (triggers minor version bump)
- `fix:` - Bug fixes (triggers patch version bump)
- `feat!:` or `BREAKING CHANGE:` - Breaking changes (triggers major version bump)
- `docs:` - Documentation changes
- `chore:` - Maintenance tasks
- `deps:` - Dependency updates

## Manual Publishing (if needed)

Requires these environment variables:

```
export ORG_GRADLE_PROJECT_mavenCentralRepositoryUsername=$SONATYPE_USERNAME
export ORG_GRADLE_PROJECT_mavenCentralRepositoryPassword=$SONATYPE_PASSWORD
export ORG_GRADLE_PROJECT_signingKey=$YOUR_GPG_KEY
export ORG_GRADLE_PROJECT_signingKeyPassword=$YOUR_GPG_KEY_PASSWORD
```

```bash
./gradlew clean publish --no-daemon
```

Then visit [Sonatype](https://oss.sonatype.org/#stagingRepositories) to promote the artifact.
