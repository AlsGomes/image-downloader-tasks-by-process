package br.com.agdev;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class SummaryGenerator {

    private static final Logger LOG = LogManager.getLogger(SummaryGenerator.class);

    public static void generateSummary(List<Summary> summaryList) {
        LOG.info("Generating summary with [{}] executions...", summaryList.size());

        Path path = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "summary.csv");

        FileWriter fileWriter = null;
        try {
            Files.deleteIfExists(path);
            fileWriter = new FileWriter(path.toFile(), StandardCharsets.UTF_8, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PrintWriter printWriter = new PrintWriter(fileWriter);

        for (Summary summary : summaryList) {
            printWriter.printf("%s,%d,%d%n", summary.method(), summary.time(), summary.threads());
        }

        printWriter.close();

        LOG.info("Summary generated successfully!");
    }
}
