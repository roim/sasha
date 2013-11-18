package br.ita.roim.sasha.common;

import org.apache.lucene.document.*;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

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

        // At this point our path should be treated as a URL. This will adjust smb:/foo to smb://foo
        //     notice that this won't break the filename standards, since '//' will be resolved back to '/'
        //     if we use this as a path.
        // We kept using java's Path and not URL up to this point, however, since SMB urls are not valid in Java.
        //
        String singleSlashPath = filePath.getParent().toString() + "/";
        Path = singleSlashPath.replaceFirst(":/", "://");

        Size = size;
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
