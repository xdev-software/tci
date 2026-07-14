# complete code
import java.io.File;
import java.io.IOException;

public class DBContainer {
    public static void main(String[] args) {
        // Check if the corrupted cache is found
        File corruptedCacheFile = new File("corrupted_cache.txt");
        if (corruptedCacheFile.exists()) {
            // Clear the cache
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"gh", "cache", "delete", "--all"});
                process.waitFor();
                System.out.println("Cache cleared successfully.");
            } catch (IOException | InterruptedException e) {
                System.out.println("Error: An error occurred while clearing the cache. " + e.getMessage());
            }
        } else {
            System.out.println("No corrupted cache found.");
        }
    }
}