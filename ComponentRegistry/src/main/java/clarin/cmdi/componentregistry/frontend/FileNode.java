package clarin.cmdi.componentregistry.frontend;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.io.FileUtils;

public class FileNode implements Serializable{

    private static final long serialVersionUID = 1L;
    private final File file;
    private final boolean deleted;

    public FileNode(File file, boolean deleted) {
        this.file = file;
        this.deleted = deleted;
    }
    
    public String getFileContent() {
        try {
            return FileUtils.readFileToString(getFile(), "UTF-8");
        } catch (IOException e) {
            return "Error could not read file: " + e;
        }
    }

    @Override
    public String toString() {
        return getFile().getName();
    }

    public File getFile() {
        return file;
    }

    public boolean isDeleted() {
        return deleted;
    }
}