package com.demo.jira;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class TaskRunner implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskRunner.class);
    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(16);

    @Value("${jql}")
    private String jql;

    @Value("${output.dir}")
    private String outputDir;

    @Value("${batch.size}")
    private Integer batchSize;

    @Autowired
    private JiraApiClient jiraApiClient;

    @Override
    public void run(final String... args) throws Exception {
        LOGGER.info("Jira issues import - Started");
        run();
        LOGGER.info("Jira issues import - Finished");
    }

    public void run() throws ParseException {
        final Integer totalIssues = getTotalIssues();
        final Integer apiCallsToBeMade = getApiCallsToBeMade(totalIssues);
        final List<Integer> startAtValues = getStartAtValues(apiCallsToBeMade);
        final List<Future<String>> futures = new ArrayList<>();

        submitTasks(startAtValues, futures);
        processTasksResponse(futures);

        LOGGER.info("Total issues imported: {}", totalIssues);
    }

    private int getApiCallsToBeMade(Integer totalIssues) {
        return needExtraBatch(totalIssues) //
                ? totalIssues / batchSize + 1 //
                : totalIssues / batchSize;
    }

    private boolean needExtraBatch(Integer totalIssues) {
        return Double.valueOf(totalIssues) % batchSize > 0;
    }

    private List<Integer> getStartAtValues(Integer apiCallsToBeMade) {
        return IntStream.range(0, apiCallsToBeMade)
                .map(number -> number * batchSize)
                .boxed()
                .collect(Collectors.toList());
    }

    private Integer getTotalIssues() throws ParseException {
        final String response = jiraApiClient.search(jql, 0, 0);
        final JSONParser parser = new JSONParser();
        final JSONObject json = (JSONObject) parser.parse(response);

        return Integer.parseInt(String.valueOf(json.get("total")));
    }

    private void processTasksResponse(List<Future<String>> futures) {
        final int fileNumber[] = new int[]{1};

        futures.forEach(future -> {
            try {
                final String output = future.get();

                Files.write(Paths.get(outputDir + fileNumber[0] + ".json"), //
                        Stream.of(output).collect(Collectors.toList()), //
                        Charset.forName("UTF-8"));
                fileNumber[0]++;
            } catch (InterruptedException | ExecutionException | IOException e) {
                LOGGER.error("Caught exception while writing issues. Exception: {}", e);
                e.printStackTrace();
            }
        });
    }

    private void submitTasks(List<Integer> startAtValues, List<Future<String>> futures) {
        int count = 0;

        for (final Integer startAt : startAtValues) {
            futures.add(EXECUTOR_SERVICE.submit(new Task(jiraApiClient, jql, startAt, batchSize)));
            count++;
            LOGGER.info("Submitted tasks:= {}", count);
        }

        LOGGER.info("Total tasks:= {}", futures.size());
    }

    public static class Task implements Callable<String> {
        final JiraApiClient jiraApiClient;
        final String jql;
        final Integer startAt;
        final Integer maxResults;

        public Task(final JiraApiClient jiraApiClient,
                    final String jql,
                    final Integer startAt,
                    final Integer maxResults) {
            this.jiraApiClient = jiraApiClient;
            this.jql = jql;
            this.startAt = startAt;
            this.maxResults = maxResults;
        }

        @Override
        public String call() {
            COUNTER.getAndIncrement();

            // To show the tasks progress..
            if (COUNTER.get() % 5 == 0) {
                LOGGER.info("Tasks Processed:= {}", COUNTER.get());
            }

            return jiraApiClient.search(jql, startAt, maxResults);
        }
    }
}
