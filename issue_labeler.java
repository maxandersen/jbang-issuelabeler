//usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.2.0
//DEPS org.kohsuke:github-api:1.101
//DEPS com.fasterxml.jackson.core:jackson-databind:2.2.3

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.Callable;

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
    String githubEventpath;


    public static void main(String... args) {
        int exitCode = new CommandLine(new issue_labeler()).execute(args);
        exit(exitCode);
    }

    @Command
    public int init() {
        return ExitCode.OK;
    }

    @Override
    public Integer call() throws Exception { // your business logic goes here...
        // If you don't specify the GitHub user id then the sdk will retrieve it via /user endpoint

        GitHub github = new GitHubBuilder().withOAuthToken(githubToken).build();

        //var repo = github.getRepository(githubRepository);

        out.println(githubEventpath);

        var eventfile = new File(githubEventpath);

        out.println(Files.readString(eventfile.toPath()));

        return 0;
    }
}
