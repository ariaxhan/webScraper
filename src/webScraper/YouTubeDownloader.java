package webScraper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class YouTubeDownloader {

    // Update this with the absolute path to your yt-dlp executable
    private static final String YT_DLP_PATH = "/Users/ariahan/eclipse-workspace/webScraper/src/webScraper/yt-dlp-2024.05.27/yt-dlp";

    /**
     * Downloads a YouTube video using yt-dlp.
     *
     * @param videoUrl The URL of the YouTube video.
     * @param outputDir The directory to save the downloaded video.
     * @return The path of the downloaded video.
     * @throws Exception If an error occurs during downloading.
     */
    public static String downloadVideo(String videoUrl, String outputDir) throws Exception {
        System.out.println("Starting download for video URL: " + videoUrl);

        // Print the current working directory
        System.out.println("Current working directory: " + new File(".").getAbsolutePath());

        // Verify the yt-dlp path
        File ytDlpFile = new File(YT_DLP_PATH);
        if (!ytDlpFile.exists() || !ytDlpFile.isFile()) {
            throw new IOException("yt-dlp not found at: " + YT_DLP_PATH);
        }

        List<String> command = new ArrayList<>();
        command.add(YT_DLP_PATH);
        command.add(videoUrl);
        command.add("-o");
        command.add(outputDir + File.separator + "%(title)s.%(ext)s");

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("yt-dlp failed with exit code " + exitCode);
        }

        System.out.println("Download completed for video URL: " + videoUrl);

        // You can further refine this to return the exact path of the downloaded video.
        return outputDir;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java YouTubeDownloader <YouTube URL> <Output Directory>");
            System.exit(1);
        }

        String videoUrl = args[0];
        String outputDir = args[1];

        try {
            String downloadedPath = downloadVideo(videoUrl, outputDir);
            System.out.println("Video downloaded to: " + downloadedPath);
        } catch (Exception e) {
            System.err.println("Failed to download video: " + e.getMessage());
            e.printStackTrace();
        }
    }
}