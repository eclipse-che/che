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
package org.eclipse.che.api.vfs;

import com.google.common.io.ByteStreams;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.util.NotClosableInputStream;
import org.eclipse.che.api.vfs.util.ZipContent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipArchiver extends Archiver {
    public ZipArchiver(VirtualFile folder) {
        super(folder);
    }

    @Override
    public void compress(OutputStream zipOutput) throws IOException, ServerException {
        compress(zipOutput, VirtualFileFilter.ACCEPT_ALL);
    }

    @Override
    public void compress(OutputStream zipOutput, VirtualFileFilter filter) throws IOException, ServerException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(zipOutput)) {
            folder.accept(new VirtualFileVisitor() {
                @Override
                public void visit(VirtualFile visitedVirtualFile) throws ServerException {
                    if (filter.accept(visitedVirtualFile)) {
                        if (!visitedVirtualFile.equals(folder)) {
                            addZipEntry(visitedVirtualFile, zipOutputStream);
                        }
                        if (visitedVirtualFile.isFolder()) {
                            for (VirtualFile child : visitedVirtualFile.getChildren()) {
                                child.accept(this);
                            }
                        }
                    }
                }
            });
        }
    }

    private String getZipEntryName(VirtualFile virtualFile) {
        Path zipPath = virtualFile.getPath().subPath(folder.getPath());
        if (virtualFile.isFolder()) {
            return zipPath.toString() + '/';
        }
        return zipPath.toString();
    }

    private void addZipEntry(VirtualFile virtualFile, ZipOutputStream zipOutputStream) throws ServerException {
        try {
            ZipEntry zipEntry = new ZipEntry(getZipEntryName(virtualFile));
            zipOutputStream.putNextEntry(zipEntry);
            if (virtualFile.isFolder()) {
                zipEntry.setTime(0);
            } else {
                try (InputStream content = virtualFile.getContent()) {
                    ByteStreams.copy(content, zipOutputStream);
                }
                zipEntry.setTime(virtualFile.getLastModificationDate());
            }
            zipOutputStream.closeEntry();
        } catch (ForbiddenException e) {
            throw new ServerException(e.getServiceError());
        } catch (IOException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    public void extract(InputStream zipInput, boolean overwrite, int stripNumber)
            throws IOException, ForbiddenException, ConflictException, ServerException {
        try (ZipInputStream zip = new ZipInputStream(ZipContent.of(zipInput).getContent())) {
            InputStream notClosableInputStream = new NotClosableInputStream(zip);
            ZipEntry zipEntry;
            while ((zipEntry = zip.getNextEntry()) != null) {
                VirtualFile extractFolder = folder;

                Path relativePath = Path.of(zipEntry.getName());

                if (stripNumber > 0) {
                    if (relativePath.length() <= stripNumber) {
                        continue;
                    }
                    relativePath = relativePath.subPath(stripNumber);
                }

                if (zipEntry.isDirectory()) {
                    if (!extractFolder.hasChild(relativePath)) {
                        extractFolder.createFolder(relativePath.toString());
                    }
                    continue;
                }

                if (relativePath.length() > 1) {
                    Path neededParentPath = relativePath.getParent();
                    VirtualFile neededParent = extractFolder.getChild(neededParentPath);
                    if (neededParent == null) {
                        neededParent = extractFolder.createFolder(neededParentPath.toString());
                    }
                    extractFolder = neededParent;
                }

                String fileName = relativePath.getName();
                VirtualFile file = extractFolder.getChild(Path.of(fileName));
                if (file == null) {
                    extractFolder.createFile(fileName, notClosableInputStream);
                } else {
                    if (overwrite) {
                        file.updateContent(notClosableInputStream);
                    } else {
                        throw new ConflictException(String.format("File '%s' already exists", file.getPath()));
                    }
                }
                zip.closeEntry();
            }
        }
    }
}
