# Issue labeler built with jbang

This container is a github action used to automatically label issues based on 
title and description content.

[Source](https://github.com/maxandersen/jbang-issuelabeler)

## Github Action

### Inputs

### Outputs

### Example usage


```
on:
    issues:
        type: [opened]

jobs:
    jbang:
      runs-on: ubuntu-latest
      name: A job to run jbang
      steps:
      - name: checkout
        uses: actions/checkout@v1
      - name: issuelabeler
        uses: maxandersen/jbang-issuelabeler@master
        env:
          GITHUB_TOKEN: ${{ secrets.ISSUE_GITHUB_TOKEN }}
```
