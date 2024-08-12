package br.com.agdev;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Semaphore;

public class ImageProcessTask implements Runnable {

    private final Logger log = LogManager.getLogger(getClass());
    private final ImageInfo imageInfo;
    private final Semaphore semaphore;

    public ImageProcessTask(ImageInfo imageInfo, Semaphore semaphore) {
        this.imageInfo = imageInfo;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        Path path = imageInfo.path();
        String pathString = path.toString();

        log.info("Extracting image [{}]...", imageInfo);
        extractImage(imageInfo.url(), pathString);
        log.info("Image extracted successfully: [{}]", imageInfo);

        if (imageInfo.convert()) {
            log.info("Converting image [{}]...", imageInfo);
            convert(pathString, pathString.substring(0, pathString.lastIndexOf('.') + 1) + "png");
            log.info("Converted image [{}] successfully", imageInfo);

            try {
                log.info("Deleting image [{}]...", imageInfo);
                Files.deleteIfExists(path);
                log.info("Deleted image [{}] successfully", imageInfo);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        semaphore.release();
        log.info("Finished process for [{}]", imageInfo);
    }

    private void extractImage(String url, String path) {
        try {
            URL imageUrl = URL.of(URI.create(url), null);
            HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
            connection.setRequestMethod("GET");

            InputStream inputStream = connection.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(path);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
            connection.disconnect();
        } catch (Exception e) {
            log.error("Error while extracting image [{}]", imageInfo, e);
        }
    }

    private void convert(String webpPath, String pngPath) {
        String executable = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "libwebp-0.4.1-linux-x86-64", "bin", "dwebp").toString();
        String[] args = new String[]{executable, webpPath, "-o", pngPath};

        try {
            Process exec = Runtime.getRuntime().exec(args);
            exec.waitFor();
        } catch (Exception e) {
            log.error("Error while converting [{}]", imageInfo, e);
        }
    }
}
