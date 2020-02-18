//usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.2.0
//DEPS org.kohsuke:github-api:1.101
//DEPS com.fasterxml.jackson.core:jackson-databind:2.2.3
//DEPS com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.9

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.codec.language.bm.Rule;
import org.apache.commons.lang3.StringEscapeUtils;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static java.lang.System.*;
import static picocli.CommandLine.*;

@Command(name = "issue_labeler", mixinStandardHelpOptions = true, version = "issue_labeler 0.1",
        description = "issue_labeler made with jbang")
class issue_labeler implements Callable<Integer> {

    @Option(names={"--token"}, defaultValue = "${env:GITHUB_TOKEN}",
                        description = "Token to use for github (env: $GITHUB_TOKEN)", required = true)
    String githubToken;

    /*
    @Option(names={"--repository"}, defaultValue = "${env:GITHUB_REPOSITORY}",
                        description = "Repository used for the labels", required = true)
    String githubRepository;
    */

    @Option(names={"--eventpath"}, defaultValue = "${env:GITHUB_EVENT_PATH}",
            description = "Path to read webhook event data", required = true)
    File githubEventpath;


    @Option(names={"--config"}, defaultValue = ".github/autoissuelabeler.yml")
    File config;

    public static void main(String... args) {
        int exitCode = new CommandLine(new issue_labeler()).execute(args);
        exit(exitCode);
    }

    @Command
    public int init() {
        return ExitCode.OK;
    }

    @Override
    public Integer call() throws Exception {

        GitHub github = new GitHubBuilder().withOAuthToken(githubToken).build();

        var data = loadEventData();

        List<Rule> rules = getRules();

        Optional<String> title = Optional.ofNullable(data.get("issue").get("title")).map(JsonNode::asText);
        Optional<String> description = Optional.ofNullable(data.get("issue").get("description")).map(JsonNode::asText);
        Optional<String> repository_id = Optional.ofNullable(data.get("repository").get("id")).map(JsonNode::asText);
        Optional<String> issue_number = Optional.ofNullable(data.get("issue").get("number")).map(JsonNode::asText);
        Optional<String> issue_url = Optional.ofNullable(data.get("issue").get("html_url")).map(JsonNode::asText);


        Set<String> labels = new HashSet<>();
        for(Rule rule:rules) {
            if(rule.matches(title.orElse(""), description.orElse(""))) {
                labels.addAll(rule.getLabels());
            }
        }

        if(labels.isEmpty()) {
            System.out.println("No labels to apply.");
        } else {
            System.out.printf("#%s %s:%s\n", issue_number.orElse("N/A"), title.orElse("N/A"), labels);
            GHIssue issue = github.getRepositoryById(repository_id.orElseThrow())
                                    .getIssue(Integer.parseInt(issue_number.orElseThrow()));
            issue.addLabels(labels.toArray(new String[0]));
            System.out.println(issue_url.get());
        }
        return 0;
    }

    private List<Rule> getRules() throws IOException {
        return new ObjectMapper(new YAMLFactory()).readValue(config, new TypeReference<List<Rule>>(){});
    }

    private JsonNode loadEventData() throws java.io.IOException {
        return new ObjectMapper().readValue(githubEventpath, JsonNode.class);
    }

    public static class Rule {

        List<String> labels;
        Pattern title;
        Pattern description;

        public List<String> getLabels() {
            return labels;
        }

        public void setLabels(List<String> labels) {
            this.labels = labels;
        }

        public String getTitle() {
            return title.toString();
        }

        public void setTitle(String title) {
            this.title = Pattern.compile(title);
        }

        public String getDescription() {
            return description.toString();
        }

        public void setDescription(String description) {
            this.description = Pattern.compile(description);
        }

        boolean matches(String issue_title, String issue_description) {

            if(title != null && title.matcher(issue_title).matches()) {
                return true;
            }

            if(description != null && description.matcher(issue_description).matches()) {
                return true;
            }

            return false;

        }
    }


}

