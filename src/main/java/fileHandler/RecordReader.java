package fileHandler;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class RecordReader {
    private String pathname;
    private List<String> records;

    public RecordReader(String pathname){
        this.pathname = pathname;
    }

    public int readFile() throws IOException {
        Path file  = Paths.get(pathname);
        File file0 = file.toFile();
        if (file0.exists()){
            System.out.println("FileReader: File exists! start reading");
            records = Files.readAllLines(file);
            for (int i=0;i<records.size(); i++) {
                // System.out.println("records: "+records.get(i));
            }
            return 1;
        }
        else{
            System.out.println("FileReader: Path invalid!");
            return 0;
        }
    }

    public List<String> getRecords(){
        return this.records;
    }
}
