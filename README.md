# GitHub Tag Java

A lightweight, lightning-fast GitHub Action to automatically calculate and bump Semantic Versioning tags based on commit messages. Rewritten in pure Java 25 as a zero-dependency composite action for maximum performance.

## Usage

Add the following step to your GitHub Actions workflow. **Note:** You must set `fetch-depth: 0` in the checkout step so the action can analyze your commit history.

```yaml
name: Release
on:
  push:
    branches:
      - main
      - beta

jobs:
  tag:
    runs-on: ubuntu-latest
    permissions:
      contents: write # Required to push tags and create releases
    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4
        with:
          fetch-depth: 0 # Important: Fetch all history for commit analysis

      - name: Bump Version and Tag
        id: tag_version
        uses: caffeinesoft/github-tag-java@v1
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          default_bump: minor
          tag_prefix: v

      - name: Create GitHub Release
        if: steps.tag_version.outputs.new_tag != ''
        uses: softprops/action-gh-release@v2
        with:
          tag_name: ${{ steps.tag_version.outputs.new_tag }}
          name: Release ${{ steps.tag_version.outputs.new_tag }}
          body: ${{ steps.tag_version.outputs.changelog }}
```

## Inputs

| Input | Description | Default |
|-------|-------------|---------|
| `github_token` | Required for permission to tag the repo. | `${{ github.token }}` |
| `default_bump` | Fallback bump type (`major`, `minor`, `patch`, `false`) if no commits specify one. | `patch` |
| `default_prerelease_bump` | Fallback bump type for prereleases. | `prerelease` |
| `release_branches` | Comma-separated list of branches (regex) that generate release tags. | `main,master` |
| `pre_release_branches`| Comma-separated list of branches (regex) that generate pre-release tags. | `next,next-major,beta,alpha` |
| `tag_prefix` | Prefix added to the generated tag. | `v` |
| `append_to_pre_release_tag`| String appended to prerelease tags (e.g., `alpha`). | `<empty>` |
| `dry_run` | If `true`, calculates the new version but does not push the tag. | `false` |
| `fetch_all_tags` | Fetch all tags from the repository (useful for deep history). | `false` |
| `create_annotated_tag` | If `true`, creates a Git annotated tag object instead of a lightweight ref. | `false` |
| `commit_sha` | Overrides the target commit SHA to tag. | Triggering SHA |

## Outputs

| Output | Description |
|--------|-------------|
| `new_version` | The calculated semantic version (e.g., `1.2.0`). |
| `new_tag` | The calculated semantic version with prefix (e.g., `v1.2.0`). |
| `previous_version`| The previous semantic version detected. |
| `previous_tag` | The previous tag detected with prefix. |
| `changelog` | A formatted Markdown list of commit messages since the last tag. |

## License
AGPL-3.0 License
```
