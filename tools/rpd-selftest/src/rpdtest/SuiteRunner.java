package rpdtest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public final class SuiteRunner {

    private SuiteRunner() {}

    public static final class Outcome {
        public final String scenario;
        public final String verdict;
        public final String reason;
        public final boolean passed;
        public final Duration duration;
        public final Path directory;

        private Outcome(String scenario, String verdict, String reason, boolean passed,
                        Duration duration, Path directory) {
            this.scenario = scenario;
            this.verdict = verdict;
            this.reason = reason;
            this.passed = passed;
            this.duration = duration;
            this.directory = directory;
        }
    }

    public static void main(String[] args) throws Exception {
        String selection = args.length == 0 ? "all" : args[0];
        if (args.length > 1) throw new IllegalArgumentException("Expected one scenario selection");
        if ("list".equals(selection)) {
            for (String name : scenarios()) System.out.println(name);
            return;
        }
        Path output = Paths.get(System.getProperty("rpd.output", "build/rpd-selftest"));
        List<Outcome> results = run(selection, output);
        if (results.stream().anyMatch(result -> !result.passed)) System.exit(1);
    }

    public static List<String> scenarios() throws IOException {
        return Collections.unmodifiableList(Arrays.asList(manifest().getProperty("scenarios").split(",")));
    }

    public static List<Outcome> run(String selection, Path output) throws IOException, InterruptedException {
        Properties metadata = manifest();
        List<String> selected = select(selection, scenarios());
        Files.createDirectories(output.toAbsolutePath());
        Path run = Files.createTempDirectory(output.toAbsolutePath(), "run-");
        List<Outcome> results = new ArrayList<>();
        try {
            for (String name : selected) {
                Outcome result = execute(name, metadata, run.resolve(name));
                results.add(result);
                System.out.println((result.passed ? "[OK] " : "[FAILED] ") + name + " "
                        + result.verdict + " " + result.reason);
            }
        } finally {
            writeReport(results, run.resolve("TEST-e2e.xml"));
            System.out.println("Report: " + run.resolve("TEST-e2e.xml"));
        }
        return Collections.unmodifiableList(results);
    }

    private static Properties manifest() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = SuiteRunner.class.getResourceAsStream("/scenarios.properties")) {
            if (input == null) throw new IOException("Missing scenarios.properties; build :e2e-tests:classes first");
            properties.load(input);
        }
        return properties;
    }

    private static List<String> select(String selection, List<String> available) {
        List<String> selected = new ArrayList<>();
        for (String expression : selection.split(",")) {
            String filter = expression.trim();
            List<String> matches = new ArrayList<>();
            for (String name : available) {
                boolean match;
                switch (filter) {
                    case "all": match = !name.startsWith("SelfTest_"); break;
                    case "extra": match = name.startsWith("Extra_"); break;
                    case "selftest": match = name.startsWith("SelfTest_"); break;
                    default:
                        String regex = Pattern.quote(filter).replace("*", "\\E.*\\Q").replace("?", "\\E.\\Q");
                        match = name.matches(regex);
                }
                if (match) matches.add(name);
            }
            if (matches.isEmpty()) throw new IllegalArgumentException("No scenario matches: " + filter);
            for (String name : matches) if (!selected.contains(name)) selected.add(name);
        }
        return selected;
    }

    private static Outcome execute(String name, Properties metadata, Path directory)
            throws IOException, InterruptedException {
        Files.createDirectories(directory);
        Path home = Files.createDirectories(directory.resolve("sandbox/home"));
        Path temporary = Files.createDirectories(directory.resolve("sandbox/tmp"));
        long timeout = Long.parseLong(metadata.getProperty(name + ".timeout", "240"));
        String expected = metadata.getProperty(name + ".expected", "PASS");
        String expectedReason = metadata.getProperty(name + ".reason", "");
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        List<String> command = new ArrayList<>();
        command.add(Paths.get(System.getProperty("java.home"), "bin", os.contains("win") ? "java.exe" : "java").toString());
        command.add("-Xmx1024m");
        if (os.contains("mac")) command.add("-XstartOnFirstThread");
        command.add("-Djava.awt.headless=true");
        command.add("-Dfile.encoding=UTF-8");
        command.add("-Duser.home=" + home);
        command.add("-Djava.io.tmpdir=" + temporary);
        command.add("-Drpd.scenario=" + name);
        command.add("-Drpd.rundir=" + directory);
        command.add("-Drpd.timeout=" + timeout);
        command.add("-Drpd.parentPid=" + ProcessHandle.current().pid());
        for (String key : Arrays.asList("versionName", "versionCode")) {
            String value = System.getProperty("rpd." + key);
            if (value != null) command.add("-Drpd." + key + "=" + value);
        }
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add("rpdtest.Launcher");
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectOutput(directory.resolve("stdout.log").toFile());
        builder.redirectError(directory.resolve("stderr.log").toFile());
        builder.environment().put("APPDATA", home.toString());
        builder.environment().put("LOCALAPPDATA", home.toString());
        builder.environment().put("XDG_CONFIG_HOME", home.toString());
        long started = System.nanoTime();
        Process process = builder.start();
        Thread cleanup = new Thread(() -> process.destroyForcibly(), "e2e-process-cleanup");
        Runtime.getRuntime().addShutdownHook(cleanup);
        String verdict;
        String reason;
        boolean timedOut;
        int exit;
        try {
            timedOut = !process.waitFor(timeout + 30, TimeUnit.SECONDS);
            if (timedOut) process.destroyForcibly();
            exit = process.waitFor();
        } finally {
            process.destroyForcibly();
            Runtime.getRuntime().removeShutdownHook(cleanup);
        }
        if (timedOut) {
            verdict = "KILLED";
            reason = "process exceeded external timeout";
        } else if (Files.isRegularFile(directory.resolve("result.txt"))) {
            Properties result = new Properties();
            for (String line : Files.readAllLines(directory.resolve("result.txt"), StandardCharsets.UTF_8)) {
                int separator = line.indexOf('=');
                if (separator > 0) result.setProperty(line.substring(0, separator), line.substring(separator + 1));
            }
            verdict = result.getProperty("verdict", "ERROR");
            reason = result.getProperty("reason", "missing reason");
            try {
                if (exit != Result.Verdict.valueOf(verdict).exitCode) {
                    verdict = "ERROR";
                    reason = "exit code disagrees with result.txt: " + exit;
                }
            } catch (IllegalArgumentException invalid) {
                verdict = "ERROR";
                reason = "invalid result verdict";
            }
        } else {
            verdict = "DIED";
            reason = "process exited " + exit + " without result.txt; see stderr.log";
        }
        return new Outcome(name, verdict, reason, expected.equals(verdict) && reason.contains(expectedReason),
                Duration.ofNanos(System.nanoTime() - started), directory);
    }

    private static void writeReport(List<Outcome> results, Path file) throws IOException {
        long failures = results.stream().filter(result -> !result.passed).count();
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<testsuite name=\"rpd-e2e\" tests=\"").append(results.size())
                .append("\" failures=\"").append(failures).append("\">\n");
        for (Outcome result : results) {
            xml.append("  <testcase classname=\"rpdtest.scenarios\" name=\"").append(escape(result.scenario))
                    .append("\" time=\"").append(result.duration.toMillis() / 1000.0).append("\">\n");
            if (!result.passed) xml.append("    <failure message=\"").append(escape(result.verdict + ": " + result.reason))
                    .append("\">").append(escape(result.directory.toString())).append("</failure>\n");
            xml.append("    <system-out>").append(escape(result.verdict + ": " + result.reason))
                    .append("</system-out>\n  </testcase>\n");
        }
        xml.append("</testsuite>\n");
        Files.write(file, xml.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
