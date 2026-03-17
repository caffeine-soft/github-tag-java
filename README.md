# GitHub Tag Java

A lightweight, zero-dependency (aside from Jackson) GitHub Action to automatically calculate and bump Semantic Versioning tags based on commit messages. Rewritten in pure Java 25 for maximum performance and stability.

## Usage

Add the following step to your GitHub Actions workflow:

```yaml
name: Release
on:
  push:
    branches:
      - main

jobs:
  tag:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Repository
        uses: actions/checkout@v6

      - name: Bump Version and Tag
        id: tag_version
        uses: caffeinesoft/github-tag-java@v1
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          default_bump: minor
          tag_prefix: v
          create_annotated_tag: true

      - name: Print New Version
        run: echo "The new version is ${{ steps.tag_version.outputs.new_version }}"