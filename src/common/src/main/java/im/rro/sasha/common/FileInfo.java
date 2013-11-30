package im.rro.sasha.common;

import org.apache.lucene.document.*;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

/**
 * Immutable file information. Contains a basic description of the file, and conveniences to help integrating
 *  a file with a lucene index.
 */
public class FileInfo {

    public static final String ROW_COMPLETE_PATH = "CompletePath";
    public static final String ROW_EXTENSION = "Extension";
    public static final String ROW_FILE_NAME = "FileName";
    public static final String ROW_LAST_SEEN = "LastSeen";
    public static final String ROW_PATH = "Path";
    public static final String ROW_SIZE = "Size";

    public final String Extension;
    public final String Name;
    public final String Path;
    public final long Size;

    private static Set<String> ACC_PROTOCOLS;
    static {
        ACC_PROTOCOLS = new HashSet<>();
        ACC_PROTOCOLS.add("smb:");
        ACC_PROTOCOLS.add("ftp:");
    }

    public FileInfo(Path filePath, BasicFileAttributes attr) {
        this(filePath, attr.size());
    }

    public FileInfo(Path filePath, long size) {
        String fileNameExtension = filePath.getFileName().toString();
        int lastDotIndex = fileNameExtension.lastIndexOf('.');

        if (lastDotIndex >= 0) {
            Name = fileNameExtension.substring(0, lastDotIndex);
            Extension = fileNameExtension.substring(lastDotIndex);
        } else {
            Name = fileNameExtension;
            Extension = "";
        }

        Size = size;

        // At this point our path should be treated as a URL. If the first name in the path is a protocol, we adjust it.
        //     e.g., this will adjust smb:/foo to smb://foo
        //     notice that this won't break the filename standards, since '//' will be resolved back to '/'
        //     if we use this as a path.
        // We kept using java's Path and not URL up to this point, however, since SMB urls are not valid in Java.
        //
        String singleSlashPath = filePath.getParent().toString() + "/";
        if (!ACC_PROTOCOLS.contains(singleSlashPath.substring(0, singleSlashPath.indexOf("/")))) {
            // Maybe we should just keep the path as it is instead of throwing an exception
            throw new IllegalArgumentException("The first folder in the specified path does not represent" +
                    " a valid protocol: " + filePath.toString() +
                    " Did you forget the ':'?");
        }

        Path = singleSlashPath.replaceFirst("/", "//");
    }

    /**
     * Creates a new lucene document containing this file's information.
     * @return the document containing this file's info, to be added/updated to the index.
     */
    public Document createDocument() {
        Document doc = new Document();
        doc.add(new StringField(ROW_COMPLETE_PATH, getCompletePath(), Field.Store.YES));
        doc.add(new TextField(ROW_FILE_NAME, Name, Field.Store.NO));
        doc.add(new TextField(ROW_PATH, Path, Field.Store.NO));
        doc.add(new TextField(ROW_EXTENSION, Extension, Field.Store.NO));
        doc.add(new LongField(ROW_SIZE, Size, Field.Store.YES));
        doc.add(new LongField(ROW_LAST_SEEN, System.currentTimeMillis(), Field.Store.YES));
        return doc;
    }

    public String getCompletePath() {
        return Path + Name + Extension;
    }

}
