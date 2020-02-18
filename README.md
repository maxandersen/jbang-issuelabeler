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


### autolabeler.yml

To define what should be labelled create a file in `.github/autolabeler.yml`

```
- labels: [area/hibernate, need-review]
  
  expression: |
              title_description.matches("(?is).*hibernate.*") 
              and
              !title_description.matches("(?is).*validator.*") 
  addcomment: "/cc @maxandersen"
- labels: [bug]
  title: "^[bug] .*"
```

Each item is a "rule" and the meaning of each attribute is as follows:

| Attribute | Description | Exmample |
|-----------|-------------|----------|
|labels     | list of labels to add if match found | `[area/hibernate, triage]`
|title      | Java regular expression to match against issue title. | `(?i).*qute.*` |
|description | Java regular expression to match against issue description. Useful to use `(?si)` to activate matching across newlines and ignore casing. | `(?si).*qute.*`
|expression | javax.el expression that should evaluate to a boolean. Can refer to `title`, `description`, `title_description` attributes. | `title.matches(".*hibernate.*") and !description.contains(".*validator.*") |
|addcomment | free-form text which will be added as a comment | `/cc @maxandersen please review`