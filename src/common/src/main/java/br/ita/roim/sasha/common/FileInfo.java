package br.ita.roim.sasha.common;

import org.apache.lucene.document.*;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Immutable file information. Contains fields/getters for the information to be stored and other convenience methods.
 */
public class FileInfo {

    public final String rowCompletePath = "CompletePath";
    public final String rowExtension = "Extension";
    public final String rowFileName = "FileName";
    public final String rowLastSeen = "LastSeen";
    public final String rowPath = "Path";
    public final String rowSize = "Size";

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

        Path = filePath.getParent().toString() + "/";
        Size = size;
    }

    /**
     * Creates a new lucene document containing this file's information.
     * @return the document containing this file's info, to be added/updated to the index.
     */
    public Document createDocument() {
        Document doc = new Document();
        doc.add(new StringField(rowCompletePath, getCompletePath(), Field.Store.YES));
        doc.add(new TextField(rowFileName, Name, Field.Store.NO));
        doc.add(new TextField(rowPath, Path, Field.Store.NO));
        doc.add(new TextField(rowExtension, Extension, Field.Store.NO));
        doc.add(new LongField(rowSize, Size, Field.Store.YES));
        doc.add(new LongField(rowLastSeen, System.currentTimeMillis(), Field.Store.YES));
        return doc;
    }

    public String getCompletePath() {
        return Path + Name + Extension;
    }

}
