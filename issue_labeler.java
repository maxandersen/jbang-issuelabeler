//usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.2.0
//DEPS org.kohsuke:github-api:1.101
//DEPS com.fasterxml.jackson.core:jackson-databind:2.2.3
//DEPS com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.9
//DEPS org.glassfish:jakarta.el:3.0.3

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

import javax.el.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.System.*;
import static picocli.CommandLine.*;

@Command(name = "issue_labeler", mixinStandardHelpOptions = true, version = "issue_labeler 0.1",
        description = "issue_labeler made with jbang")
class issue_labeler implements Callable<Integer> {

    @Option(names={"--token"}, defaultValue = "${GITHUB_TOKEN}",
                        description = "Token to use for github (env: $GITHUB_TOKEN)", required = true)
    String githubToken;

    /*
    @Option(names={"--repository"}, defaultValue = "${env:GITHUB_REPOSITORY}",
                        description = "Repository used for the labels", required = true)
    String githubRepository;
    */

    @Option(names={"--eventpath"}, defaultValue = "${GITHUB_EVENT_PATH}",
            description = "Path to read webhook event data", required = true)
    File githubEventpath;


    @Option(names={"--config"}, defaultValue = "${CONFIG:-.github/autoissuelabeler.yml}")
    String config;

    @Option(names={"--noop"}, defaultValue = "false")
    boolean noop;

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

        List<Rule> rules = getRules();

        GitHub github = new GitHubBuilder().withOAuthToken(githubToken).build();

        var data = loadEventData();


        if(data.has("pull_request")) {
            System.out.println("Ignoring pull request");
            return ExitCode.OK;
        }

        if(data.get("action")==null || !"opened".equals(data.get("action").asText())) {
            System.out.println("Ignoring this action - only allow opened action");
        }

        Optional<String> title = Optional.ofNullable(data.get("issue").get("title")).map(JsonNode::asText);
        Optional<String> user = Optional.ofNullable(data.get("issue").get("user").get("login")).map(JsonNode::asText);
        Optional<String> description = Optional.ofNullable(data.get("issue").get("description")).map(JsonNode::asText);
        Optional<String> repository_id = Optional.ofNullable(data.get("repository").get("id")).map(JsonNode::asText);
        Optional<String> issue_number = Optional.ofNullable(data.get("issue").get("number")).map(JsonNode::asText);
        Optional<String> issue_url = Optional.ofNullable(data.get("issue").get("html_url")).map(JsonNode::asText);


        Set<String> labels = new HashSet<>();
        Set<String> comments = new HashSet<>();

        for(Rule rule:rules) {
            if(rule.matches(title.orElse(""), description.orElse(""))) {
                labels.addAll(rule.getLabels());

                if(rule.getNotify()!=null && !rule.getNotify().isEmpty()) {
                    var res = rule.getNotify().stream()
                            .filter(usr -> { return !usr.equals(user.get());  })
                            .map(usr -> { return "@" + usr; })
                            .collect(Collectors.joining(", "));


                    comments.add("/cc " + res);
                }
                if(rule.getAddcomment()!=null) {
                    comments.add(rule.getAddcomment());
                }
            }
        }

        if(labels.isEmpty()) {
            System.out.println("No labels to apply.");
        } else {
            System.out.printf("#%s %s:%s {%s}\n", issue_number.orElse("N/A"), title.orElse("N/A"), labels, comments);
            if(noop) {
                System.out.println("noop - not actually adding labels nor comments");
            } else {
                GHIssue issue = github.getRepositoryById(repository_id.orElseThrow())
                                    .getIssue(Integer.parseInt(issue_number.orElseThrow()));
                issue.addLabels(labels.toArray(new String[0]));

                if(!comments.isEmpty())
                    issue.comment(comments.stream().collect(Collectors.joining("\n")));
                }

            System.out.println(issue_url.get());
        }
        return 0;
    }

    private List<Rule> getRules() throws IOException {
        List<Rule> allRules = new ArrayList<>();
        var mapper = new ObjectMapper(new YAMLFactory());

        if(config!=null) {
            if(config.startsWith("file://") || config.startsWith("https://")) {
                URL url = new URL(config);
                allRules.addAll(mapper.readValue(url, new TypeReference<List<Rule>>(){}));
            } else {
                allRules.addAll(mapper.readValue(new File(config), new TypeReference<List<Rule>>(){}));
            }
        }

        return allRules;
    }

    private JsonNode loadEventData() throws java.io.IOException {
        return new ObjectMapper().readValue(githubEventpath, JsonNode.class);
    }

    public static class Rule {

        List<String> labels;
        String expression;
        Pattern title;
        Pattern description;
        String addcomment;
        List<String> notify;

        public String getAddcomment() {
            return addcomment;
        }

        public void setAddcomment(String addcomment) {
            this.addcomment = addcomment;
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
        public String getExpression() {
            return expression;
        }

        public void setExpression(String expression) {
            this.expression = expression;
        }

        public void setNotify(List<String> notify) {
            this.notify = notify;
        }

        public List<String> getNotify() {
            return notify;
        }

        public List<String> getLabels() {
            return labels;
        }

        public void setLabels(List<String> labels) {
            this.labels = labels;
        }

        boolean matches(String issue_title, String issue_description) {

            if(title != null && title.matcher(issue_title).matches()) {
                return true;
            }

            if(description != null && description.matcher(issue_description).matches()) {
                return true;
            }

            if(expression!=null && !expression.trim().isEmpty()) {
                expression = "${" + expression + "}";

                //https://www.programcreek.com/java-api-examples/?api=javax.el.ExpressionFactory
                var elfactory = ELManager.getExpressionFactory();

                ELContext context = new StandardELContext(elfactory);
                context.getVariableMapper().setVariable("title", elfactory.createValueExpression(issue_title, String.class));
                context.getVariableMapper().setVariable("description", elfactory.createValueExpression(issue_title, String.class));
                context.getVariableMapper().setVariable("title_description", elfactory.createValueExpression(issue_title + "\n" + issue_description, String.class));

                //System.out.println(expression);
                var ve = elfactory.createValueExpression(context, expression, Boolean.class);

                var value = (Boolean) ve.getValue(context);

                return value != null ? value : false;
            }

            return false;
        }
    }


}

