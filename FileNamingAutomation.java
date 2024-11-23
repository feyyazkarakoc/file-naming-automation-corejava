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
