import java.io.Serializable;

public record FileInfo(String fileName, Long fileSizeInBytes) implements Serializable {

    public String getFileName() {
        return fileName;
    }

    public Long getFileSizeInBytes() {
        return fileSizeInBytes;
    }
}