package zadanie;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;

public class Directory {
    private final String HOME = "HOME";
    private final String DEV = "DEV";
    private final String TEST = "TEST";
    private final String COUNT = "HOME\\count.txt";

    private int countMovedFilesToDEV = 0;
    private int countMovedFilesToTEST = 0;

    public void createDirectoryStructure() {
        try {
            Files.createDirectories(Paths.get(HOME));
            Files.createDirectories(Paths.get(DEV));
            Files.createDirectories(Paths.get(TEST));

        } catch (IOException e) {
            System.err.println("Failed to create directory!" + e.getMessage());
        }
    }

    public void startDirectoryWatching() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path pathToWatch = Paths.get(HOME);
            pathToWatch.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE);

            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    String filename = String.valueOf(event.context());
                    handleTheFile(filename);
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleTheFile(String filename) {
        if (FilenameUtils.getExtension(filename).equals("jar")) {
            Path filePath = Paths.get(HOME + "\\" + filename);
            int creationHour = getFileCreationHour(filePath);
            Path newPath;
            if (creationHour % 2 == 0) {
                newPath = Paths.get(DEV + "\\" + filename);
                countMovedFilesToDEV++;
            } else {
                newPath = Paths.get(TEST + "\\" + filename);
                countMovedFilesToTEST++;
            }
            moveFile(filePath, newPath);
        } else if (FilenameUtils.getExtension(filename).equals("xml")) {
            Path filePath = Paths.get(HOME + "\\" + filename);
            Path newPath = Paths.get(DEV + "\\" + filename);
            countMovedFilesToDEV++;
            moveFile(filePath, newPath);
        }
    }

    private int getFileCreationHour(Path filePath) {
        try {
            BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
            FileTime fileTime = attr.creationTime();
            Date date = new Date(fileTime.toMillis());

            return (int) (date.getTime() % 86400000) / 3600000;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void moveFile(Path from, Path to) {
        try {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
            saveCountersToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveCountersToFile() {
        createCountFile();

        try {
            FileWriter myWriter = new FileWriter(COUNT);
            int numberOfMovedFiles = countMovedFilesToDEV + countMovedFilesToTEST;
            myWriter.write("Liczba przeniesionych plików: " + numberOfMovedFiles
                    + "\nLiczba przeniesionych plikow do TEST: " + countMovedFilesToTEST
                    + "\nLiczba przeniesionych plikow do DEV: " + countMovedFilesToDEV);
            myWriter.close();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private void createCountFile() {
        try {
            File myObj = new File(COUNT);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
