import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class FileNamingAutomation {

    private static final String pathName = "C:\\Users\\Lenovo\\Desktop\\feyyaz"; // Target directory path
    private static final String renameSuffix = "renamed"; // Suffix for renaming files

    // Map to store counters for each file type
    private static final Map<String, Integer> typeCounters = new HashMap<>();

    public static void main(String[] args) {
        File directory = new File(pathName);

        // Check if the directory exists and is a valid directory
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Invalid directory path. Please check and try again.");
            return;
        }

        // First, find the existing counters from renamed files
        findExistingCounters(directory);

        // Then, rename the new files
        renameNewFiles(directory);
    }

    /**
     * Finds the existing counters for renamed files in the directory.
     * It checks files that contain the 'rename' suffix and extracts the number part from the name.
     * It updates the typeCounters map to store the maximum number for each file type.
     */
    private static void findExistingCounters(File directory) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {

            String fileName = file.getName();
            if (file.isFile() && fileName.contains(renameSuffix)) {
                String extension = getFileExtension(fileName);


                // Try extracting the number from the file name
                try {
                    String numberPart = fileName.substring(getFileTypePrefix(extension).length(), fileName.indexOf(renameSuffix)
                    );
                    int number = Integer.parseInt(numberPart);

                    // Update the maximum number found for the file type
                    //typeCounters.merge(extension, number, Integer::max);
                    if (typeCounters.containsKey(extension)) {
                        int existingValue = typeCounters.get(extension);
                        typeCounters.put(extension, Math.max(existingValue, number));
                    } else {
                        typeCounters.put(extension, number);
                    }

                } catch (Exception e) {
                    // If we can't extract a number, just skip this file
                }
            }
        }
    }

    /**
     * Renames the new files in the directory that don't already have the 'rename' suffix.
     * It sorts the files alphabetically and renames them sequentially.
     */
    private static void renameNewFiles(File directory) {
        File[] files = directory.listFiles();
        if (files == null) return;

        // Sort the files by name to ensure sequential renaming
        Arrays.sort(files, Comparator.comparing(File::getName));

        boolean renamedAnyFile = false;

        // Process each file that doesn't have the 'rename' suffix
        for (File file : files) {
            if (file.isFile() && !file.getName().contains(renameSuffix)) {
                renameFile(file);
                renamedAnyFile=true;
            }
        }

        if (!renamedAnyFile){
            System.out.println("No new files were renamed. All files are already renamed.");
        }
    }

    /**
     * Renames a file by adding a sequential number based on its type.
     * The new file name consists of the file type prefix, a sequential number, the 'rename' suffix, and the file extension.
     *
     * @param file The file to be renamed.
     */
    private static void renameFile(File file) {
        String extension = getFileExtension(file.getName());
        String fileType = getFileTypePrefix(extension);

        // Get the current counter for the file type and increment it
        int typeCount = typeCounters.getOrDefault(extension, 0) + 1;
        typeCounters.put(extension, typeCount);

        // Create the new file name
        String newFileName = String.format("%s%d%s%s",
                fileType,
                typeCount,
                renameSuffix,
                extension
        );

        File renamedFile = new File(file.getParent(), newFileName);

        // Attempt to rename the file and output the result
        if (file.renameTo(renamedFile)) {
            System.out.println("Renamed: " + file.getName() + " -> " + newFileName);
        } else {
            System.out.println("Renaming failed: " + file.getName());
        }
    }

    /**
     * Gets the file type prefix based on the file extension.
     * For example, ".docx" and ".doc" return "word", ".pdf" returns "pdf", etc.
     *
     * @param extension The file extension (e.g., ".docx", ".pdf").
     * @return The file type prefix (e.g., "word", "pdf").
     */
    private static String getFileTypePrefix(String extension) {
        switch (extension.toLowerCase()) {
            case ".docx":
                return "word";
            case ".xlsx":
                return "excel";
            case ".txt":
                return "txt";
            case ".png":
                return "image";
            default:
                return "file";
        }
    }

    /**
     * Extracts the file extension from a file name.
     *
     * @param fileName The file name (e.g., "document.docx").
     * @return The file extension (e.g., ".docx").
     */
    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex != -1) ? fileName.substring(dotIndex) : "";
    }
}




/*import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class FileRenamer {

    private static final String TARGET_FOLDER = "C:\\Users\\Lenovo\\Desktop\\task"; // Replace with your target folder path
    private static final Logger logger = Logger.getLogger(FileRenamer.class.getName());

    static {
        try {
            FileHandler fileHandler = new FileHandler("renamer.log", true);
            logger.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
        } catch (IOException e) {
            logger.severe("Error initializing logger: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            renameFiles(new File(TARGET_FOLDER), executor);
        } finally {
            executor.shutdown(); // Ensure shutdown is always called
            try {
                // Added awaitTermination that ensures all tasks are completed before shutting down
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    logger.severe("Executor did not terminate in the specified time.");
                }
            } catch (InterruptedException e) {
                logger.severe("Interrupted while waiting for termination: " + e.getMessage());
            }
        }
    }

    private static void renameFiles(File folder, ExecutorService executor) {
        File[] files = folder.listFiles();

        if (files == null) {
            logger.warning("Folder is inaccessible: " + folder.getAbsolutePath());
            return;
        }

        // Check if the folder is empty
        if (files.length == 0) {
            logger.warning("Folder is empty: " + folder.getAbsolutePath());
            return;
        }

        // Added ConcurrentHashMap to ensure thread-safe. Thread-safe map to store counters for file types.
        ConcurrentHashMap<String, Integer> counters = new ConcurrentHashMap<>();

        // Process subfolders concurrently
        for (File file : files) {
            if (file.isDirectory()) {
                // Added a warning log if the folder is empty or inaccessible. Log if a subfolder is empty
                if (isEmptyDirectory(file)) {
                    logger.warning("Subfolder is empty: " + file.getAbsolutePath());
                } else {
                    executor.submit(() -> renameFiles(file, executor));
                }
            }
        }

        // Process files in the current folder
        for (File file : files) {
            if (file.isFile() && !file.getName().startsWith("Renamed_")) {
                renameFile(file, counters);
            }
        }

        logger.info("Processed folder: " + folder.getAbsolutePath());
    }


    // Added a warning log if the folder is empty or inaccessible.
    private static boolean isEmptyDirectory(File folder) {
        File[] files = folder.listFiles();
        return files == null || files.length == 0;
    }

    private static void renameFile(File file, ConcurrentHashMap<String, Integer> counters) {
        String originalName = file.getName();
        int extensionIndex = originalName.lastIndexOf('.');
        if (extensionIndex == -1) {
            logger.severe("Error renaming file: " + originalName + ". No extension found.");
            return;
        }

        String extension = originalName.substring(extensionIndex);
        String originalNameWithoutExtension = originalName.substring(0, extensionIndex);
        String fileType = getFileType(extension);

        // Find the correct sequence number considering existing files
        int counter = counters.compute(fileType, (key, value) -> {
            if (value == null) value = getCurrentMaxSequence(file.getParentFile(), fileType);
            return value + 1;
        });

        String newName = "Renamed_" + fileType + counter + "_" + originalNameWithoutExtension + extension;
        File newFile = new File(file.getParent(), newName);

        try {
            if (file.renameTo(newFile)) {
                logger.info("Renamed file: " + originalName + " to " + newName);
            } else {
                logger.severe("Error renaming file: " + originalName + " to " + newName);
            }
        } catch (Exception e) {
            logger.severe("Error renaming file: " + originalName + ". " + e.getMessage());
        }
    }


    //Added to determine the highest sequence number for a specific file type in the current folder
    private static int getCurrentMaxSequence(File folder, String fileType) {
        // Scan existing files in the folder to determine the current maximum sequence number
        int max = 0;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith("Renamed_" + fileType)) {
                    String name = file.getName();
                    String[] parts = name.split("_");
                    if (parts.length > 2) {
                        try {
                            int num = Integer.parseInt(parts[1].substring(fileType.length()));
                            max = Math.max(max, num);
                        } catch (NumberFormatException ignored) {
                            // Skip invalid naming patterns
                        }
                    }
                }
            }
        }
        return max;
    }

    private static String getFileType(String extension) {
        switch (extension) {
            case ".docx":
                return "word";
            case ".xlsx":
                return "excel";
            case ".txt":
                return "text";
            case ".png":
                return "image";
            default:
                return "file";
        }
    }
}*/
