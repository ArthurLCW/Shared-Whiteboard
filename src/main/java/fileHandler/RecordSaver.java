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

    // TODO: WXH may need to modify this function.
    // Currently, this function return 0,-1,1.
    // 1 indicates there is no such filename and the path is valid. Writes successfully.
    // 0 indicates the path is incorrect. write abort.
    // -1 indicates there is already a file whose name is filename. Writing fails.
    // You need to modify -1 accordingly with your UI to solve the overwrite problem.
    // Notice: the strings are stored in separate lines in the file. Please store file as xx.whiteboard

    // TODO: WARNING: Overwrite problem MUST be solved!!! Otherwise we will lose mark again like the assignment 1.
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
