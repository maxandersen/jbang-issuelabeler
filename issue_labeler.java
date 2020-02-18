//usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.2.0
//DEPS org.kohsuke:github-api:1.101

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

@Command(name = "issue_labeler", mixinStandardHelpOptions = true, version = "issue_labeler 0.1",
        description = "issue_labeler made with jbang")
class issue_labeler implements Callable<Integer> {


    public static void main(String... args) {
        int exitCode = new CommandLine(new issue_labeler()).execute(args);
        exit(exitCode);
    }

    @Command
    public int init() {
        return CommandLine.ExitCode.OK;
    }


    @Override
    public Integer call() throws Exception { // your business logic goes here...
        // If you don't specify the GitHub user id then the sdk will retrieve it via /user endpoint

        GitHub github = new GitHubBuilder().withOAuthToken(getenv("GITHUB_TOKEN")).build();

        var repo = github.getRepository(getenv("GITHUB_REPOSITORY"));

        var event_path = getenv("GITHUB_EVENT_PATH");

        out.println(event_path);

        var eventfile = new File(event_path);

        out.println(Files.readAllLines(eventfile.toPath()));

        return 0;
    }
}
