package fileHandler;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingDeque;

public class RecordSaver {
    private String pathname;
    private LinkedBlockingDeque<String> drawingRecord;

    public RecordSaver(String pathname, LinkedBlockingDeque<String> drawingRecord){
        this.pathname = pathname;
        this.drawingRecord = drawingRecord;
    }

    public int saveFile() throws IOException {
        Path file  = Paths.get(pathname);
        File file0 = file.toFile();
        if (file0.exists()){
            System.out.println("FileSaver: File already exists! Writing aborted");
            return -1;
        }
        else{
            Path path = Files.write(file, drawingRecord, StandardCharsets.UTF_8);
            File file1 = path.toFile();
            if (!file1.exists()) {
                System.out.println("FileSaver: Pathname incorrect! Write failed. ");
                return 0;
            }
            else{
                System.out.println("FileSaver: Pathname correct! Create the new file. ");
                return 1;
            }
        }
    }
}
