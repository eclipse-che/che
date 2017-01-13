/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs.impl.file;

import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardOpenOption.CREATE;

public class FileWatcherTestTree {
    private final java.nio.file.Path root;

    public FileWatcherTestTree(File root) {
        this.root = root.toPath();
    }

    public List<String> createTree(String parent, int numberOfItemsInEachLevel, int depth) throws IOException {
        List<String> paths = newArrayList();
        if (depth > 0) {
            for (int i = 0; i < numberOfItemsInEachLevel; i++) {
                if (i % 2 == 0) {
                    String directory = createDirectory(parent);
                    paths.add(directory);
                    paths.addAll(createTree(directory, numberOfItemsInEachLevel, depth - 1));
                } else {
                    paths.add(createFile(parent));
                }
            }
        }
        return paths;
    }

    public String createFile(String parent) throws IOException {
        return createFile(parent, NameGenerator.generate("file-", ".txt", 7));
    }

    public String createFile(String parent, String name) throws IOException {
        String content = Long.toString(System.currentTimeMillis());
        java.nio.file.Path file = Files.write(root.resolve(parent).resolve(name), newArrayList(content), CREATE);
        return root.relativize(file).toString();
    }

    public String createDirectory(String parent) throws IOException {
        return createDirectory(parent, NameGenerator.generate("dir-", 7));
    }

    public String createDirectory(String parent, String name) throws IOException {
        java.nio.file.Path dir = Files.createDirectory(root.resolve(parent).resolve(name));
        return root.relativize(dir).toString();
    }

    public List<String> listDirectories(String path) throws IOException {
        final Path dir = root.resolve(path);
        return Files.list(dir).map(child -> root.relativize(child).toString()).collect(Collectors.toList());
    }

    public List<String> findAllFilesInTree(String path) throws IOException {
        List<String> files = newArrayList();
        Files.walkFileTree(root.resolve(path), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                files.add(root.relativize(file).toString());
                return CONTINUE;
            }
        });
        return files;
    }

    public List<String> findAllDirectoriesInTree(String path) throws IOException {
        List<String> directories = newArrayList();
        Files.walkFileTree(root.resolve(path), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                directories.add(root.relativize(dir).toString());
                return CONTINUE;
            }
        });
        return directories;
    }

    public boolean delete(String path) {
        return IoUtil.deleteRecursive(new File(root.toFile(), path));
    }

    public void updateFile(String file) throws IOException {
        String content = Long.toString(System.currentTimeMillis());
        Files.write(root.resolve(file), newArrayList(content), StandardOpenOption.APPEND);
    }
}
