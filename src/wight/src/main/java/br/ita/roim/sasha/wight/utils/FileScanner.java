package br.ita.roim.sasha.wight.utils;

import javafx.util.Pair;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Contains utilities to work with all files (recursively) in a folder.
 */
public class FileScanner {
    private final Path root;

    public FileScanner(final Path root) {
        this.root = root;
    }

    /**
     * Gets all the folders inside the one used by the scanner, with an exact depth.
     * @param depth the exact depth the returned subfolders should have. 0 represents only the root path.
     * @return An iterable with paths to the subfolders
     * @throws IOException
     */
    public Iterable<Path> getSubFolders(final int depth) throws IOException {
        final Stream<Path> results = Files.find(root, depth, (path, attr) -> path.getNameCount() == depth && attr.isDirectory());
        return results::iterator;
        // http://stackoverflow.com/questions/20129762/why-does-streamt-not-implement-iterablet
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