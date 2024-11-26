import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FileNamingAutomation {



    // Target directory path
    private static final String folderPath = "C:\\Users\\Lenovo\\Desktop\\feyyaz";

    // Türlere göre maksimum numaraları saklamak için bir Map
    private static final Map<String, Integer> typeCounters = new HashMap<>();

    // Dosya türleri için uzantılar ve tür isimleri
    private static final Map<String, String> FILE_TYPES = Map.of(
            ".docx", "word",
            ".xlsx", "excel",
            ".txt", "text",
            ".png", "image"
    );

    public static void main(String[] args) {

        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Invalid folder path. Please check and try again.");
            return;
        }

        System.out.println("\nProcessing folder path: " + folder.getAbsolutePath());

        // Rename the new files in the main folder
        renameNewFiles(folder);

        // Process subdirectories as well
        processSubFolders(folder);

    }


    private static void renameNewFiles(File folder) {

        boolean renamedAnyFile = false;


        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isFile() && !file.getName().startsWith("renamed_")) {

                String extension = getFileExtension(file.getName());
                String type = FILE_TYPES.getOrDefault(extension, "file");


                int currentNumber = typeCounters.getOrDefault(type, findMaxCounter(folder, type)) + 1;
                typeCounters.put(type, currentNumber);

                // Yeni dosya ismini olustur
                String newFileName = String.format(
                        "renamed_%s%d_%s%s",
                        type,
                        currentNumber,
                        getOriginalFileNameWithoutExtension(file.getName()),
                        extension
                );

                File renamedFile = new File(folder, newFileName);


                // Dosyayı yeniden adlandır
                if (file.renameTo(renamedFile)) {
                    System.out.println(file.getName() + " yeniden adlandırıldı -> " + renamedFile.getName());

                    renamedAnyFile = true;

                } else {
                    System.out.println("Hata: " + file.getName() + " yeniden adlandırılamadı!");
                }
            }
        }

        if (!renamedAnyFile) {
            System.out.println("No files to rename.");
        }
    }


    // Dosya uzantısını alır
    private static String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        return (index == -1) ? "" : fileName.substring(index);
    }

    // Dosya adını uzantısı olmadan alır
    private static String getOriginalFileNameWithoutExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        return (index == -1) ? fileName : fileName.substring(0, index);
    }

    // Bir tür için klasördeki maksimum numarayı bulur
    private static int findMaxCounter(File folder, String type) {
        int maxCounter = 0;

        File[] files = folder.listFiles();
        if (files == null) {
            System.out.println("Folder is null.");
            return maxCounter;
        }

        for (File file : folder.listFiles()) {
            if (file.getName().startsWith("renamed_" + type)) {
                String numberPart = file.getName().substring(("renamed_" + type).length());
                numberPart = numberPart.split("_")[0]; // İlk "_" karakterine kadar olan kısmı al
                try {
                    int number = Integer.parseInt(numberPart);
                    maxCounter = Math.max(maxCounter, number);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format: " + e.getMessage()+" "+file.getName());
                }
            }
        }

        return maxCounter;
    }


    private static void processSubFolders(File folder) {

        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                typeCounters.clear();

                System.out.println("\nProcessing subfolder path : " + file.getAbsolutePath());
                renameNewFiles(file);

                processSubFolders(file);
            }
        }

    }

}