# Issue labeler built with jbang

This container is a github action used to automatically label issues based on 
title and description content.

[Source](https://github.com/maxandersen/jbang-issuelabeler)

## Github Action

### Inputs

### Outputs

### Example usage

CONFIG defaults to be `.github/autolabeler.yml` which requires you to use checkout
which can take unnecessary time thus recommendation is to pass a URL to CONFIG instead.

```
on:
    issues:
        type: [opened]

jobs:
    jbang:
      runs-on: ubuntu-latest
      name: A job to run jbang
      steps:
      - name: issuelabeler
        uses: maxandersen/jbang-issuelabeler@master
        env:
          GITHUB_TOKEN: ${{ secrets.ISSUE_GITHUB_TOKEN }}
          CONFIG: https://raw.githubusercontent.com/maxandersen/githubaction-testinggrounds/master/.github/autoissuelabeler.yml
```
