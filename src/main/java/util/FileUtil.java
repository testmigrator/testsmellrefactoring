package util;

import com.github.javaparser.utils.Log;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FileUtil {

    public static String getFileName(String filePath) {
        // Split the path by the file separator
        String[] parts = filePath.split("[/\\\\]");
        // The last part of the array should be the file name
        return parts[parts.length - 1];
    }

    public static String getNameByFilepath(String filepath) {
        if (StringUtils.isBlank(filepath)) {
            return StringUtils.EMPTY;
        }
        String[] split = filepath.split(File.separator);
        String fileName = split[split.length - 1];
        return fileName.split("\\.")[0];
    }

    public static List<File> listFiles(File directory, String fileSuffix) {
        List<File> collectFiles = Lists.newArrayList();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    collectFiles.addAll(listFiles(file, fileSuffix));
                } else if (file.getName().endsWith(fileSuffix)) {
                    collectFiles.add(file);
                }
            }
        }
        return collectFiles;
    }

    public static void writeDataToFile(String filepath, String data) {
        Path path = Paths.get(filepath);

        Path dirPath = path.getParent();

        if (dirPath != null && !Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            Files.write(path, data.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Log.info("write done, filePath: " + filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToNewFile(String content, String filename) {
        Path path = Paths.get(filename);
        try {
            Path parentDir = path.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            Files.write(path, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
