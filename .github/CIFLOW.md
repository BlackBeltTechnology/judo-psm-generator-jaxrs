# CI/CD Flow — Development Versions and Branch Handling

This document describes the branching strategy, versioning policy, and GitHub Actions workflows used by this project.

## Branches

The versioning policy follows [GitFlow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) with the following branch types:

| Branch Pattern | Purpose | Based On |
|----------------|---------|----------|
| `develop` | Latest development sources of the active version | — |
| `feature/JNG-NNN_short_summary` | New features for the active version | `develop` |
| `(release/)X.Y.Z` | Release stabilization branches (`release/` prefix reserved for CI) | `develop` |
| `bugfix/JNG-NNN_short_summary` | Bug fixes applied to release branches, then merged forward | release branch |
| `support/JNG-NNN_short_summary` | Minor changes for a previous release, merged back to release branch | release branch |
| `master` | Latest released sources of the active version | release branch |
| `hotfix/JNG-NNN_short_summary` | Emergency fixes applied to both release and master branches | `master` |

```mermaid
gitGraph
    commit id: "initial"
    branch develop
    commit id: "dev-1"
    branch feature/JNG-1
    commit id: "feat-1"
    commit id: "feat-2"
    checkout develop
    merge feature/JNG-1 id: "merge-feat-1"
    branch feature/JNG-3
    commit id: "feat-3"
    checkout develop
    branch release/1.0-beta1
    commit id: "rc-1"
    branch bugfix/JNG-4
    commit id: "fix-1"
    checkout release/1.0-beta1
    merge bugfix/JNG-4 id: "merge-fix"
    checkout develop
    merge feature/JNG-3 id: "merge-feat-3"
    merge release/1.0-beta1 id: "merge-release"
    checkout main
    merge release/1.0-beta1 id: "v1.0-beta1" tag: "v1.0-beta1"
```

## Version Numbers

Version numbers follow semantic versioning, with these rules:

| Event | Version Change |
|-------|---------------|
| Starting a feature branch | No change |
| Starting a release branch | 2nd number on `develop` is incremented |
| Bugfix on release branch | No change (fixes go into the release before it ships) |
| Starting a support branch | 3rd number is incremented |
| Starting a hotfix branch | 4th number is incremented |

## GitHub Actions Workflows

### build.yml — Main Build Pipeline

Triggered on pushes to `develop` and pull requests targeting `develop`, `master`, `increment/*`, or `release/*`.

```mermaid
flowchart TD
    Trigger["Push to develop<br/>or PR to develop/master/<br/>increment/release"]
    Trigger --> BranchCheck{Branch type?}

    BranchCheck -->|"master, release/*"| StableVersion["Version from pom.xml<br/>(without -SNAPSHOT)"]
    BranchCheck -->|"develop, increment/*"| SnapshotVersion["Version: major.minor.qualifier<br/>.date_commitId_branchName"]

    StableVersion --> Build["Build & deploy to Nexus"]
    SnapshotVersion --> Build

    Build --> Tag["Create git tag<br/>v&lt;version&gt;"]

    Tag --> PRCheck{Branch type?}
    PRCheck -->|"increment/*, release/*"| MergeTag["Create tag<br/>merge-pr/&lt;version&gt;"]
    MergeTag --> TriggerMerge["Triggers<br/>merge-pr-tagged.yml"]

    PRCheck -->|"develop"| Changelog["Build changelog"]
    Changelog --> Release["Create GitHub prerelease<br/>with changelog"]
```

### merge-pr-tagged.yml — Auto-Merge After Build

Triggered when a `merge-pr/*` tag is pushed.

```mermaid
flowchart TD
    Trigger["Push on merge-pr/* tag"]
    Trigger --> Extract["Extract version from tag"]
    Extract --> VersionCheck{Version format?}

    VersionCheck -->|"major.minor.qualifier<br/>(stable)"| MergeMaster["Merge PR to master"]
    MergeMaster --> TriggerMasterRelease["Triggers<br/>create-release-on-master.yml"]

    VersionCheck -->|"other<br/>(snapshot)"| SquashDevelop["Squash PR to develop"]
    SquashDevelop --> TriggerBuild["Triggers build.yml"]

    MergeMaster --> Cleanup["Delete merge-pr/* tag"]
    SquashDevelop --> Cleanup
```

### create-release-on-master.yml — Master Branch Release

Triggered on pushes to `master`. Gets the version from the tag, builds a changelog, and creates a GitHub release marked as the latest.

### release.yml — Manual Release Trigger

Manually triggered with a version parameter (either `'auto'` to use the pom.xml version, or a specific `major.minor.qualifier`).

```mermaid
flowchart TD
    Trigger["Manual trigger<br/>with version param"]
    Trigger --> VersionCheck{Version = 'auto'?}

    VersionCheck -->|Yes| FromPom["Release version<br/>from pom.xml"]
    VersionCheck -->|No| FromInput["Release version<br/>from input"]

    FromPom --> NextVersion["Next version =<br/>qualifier + 1"]
    FromInput --> NextVersion

    NextVersion --> ReleasePR["Create PR to master<br/>with release version"]
    NextVersion --> DevelopPR["Create PR to develop<br/>with next version"]

    ReleasePR --> BuildRelease["Triggers build.yml"]
    DevelopPR --> BuildDevelop["Triggers build.yml"]
```

## Development Rules

> **Important:** There is no commit without a ticket number. Every pull request and commit must include a JIRA reference like `JNG-xxx`.

Issue tracking: [JIRA dashboard](https://blackbelt.atlassian.net/jira/dashboards)
