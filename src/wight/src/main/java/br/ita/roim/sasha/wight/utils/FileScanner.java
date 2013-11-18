package br.ita.roim.sasha.wight.utils;

import javafx.util.Pair;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Predicate;

/**
 * Contains utilities to work with all files (recursively) in a folder.
 */
public class FileScanner {
    private final Path root;

    public FileScanner(Path root) {
        this.root = root;
    }

    public DirectoryStream<Path> getSubFolders() throws IOException {
        return getSubFolders(root);
    }

    private static DirectoryStream<Path> getSubFolders(Path folder) throws IOException {
        return Files.newDirectoryStream(folder, entry -> Files.isDirectory(entry));
    }

    public DirectoryStream<Path> getSubFiles() throws IOException {
        return getSubFiles(root);
    }

    private static DirectoryStream<Path> getSubFiles(Path folder) throws IOException {
        return Files.newDirectoryStream(folder, entry -> !Files.isDirectory(entry));
    }

    public void fileWalker(final Predicate<Pair<Path, BasicFileAttributes>> action) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!action.test(new Pair<>(file, attrs))) {
                    return FileVisitResult.TERMINATE;
                }

                return FileVisitResult.CONTINUE;
            }
        });
    }
}