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

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.util.NotClosableInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TarArchiver extends Archiver {
    public TarArchiver(VirtualFile folder) {
        super(folder);
    }

    @Override
    public void compress(OutputStream tarOutput) throws IOException, ServerException {
        compress(tarOutput, VirtualFileFilter.ACCEPT_ALL);
    }

    @Override
    public void compress(OutputStream tarOutput, VirtualFileFilter filter) throws IOException, ServerException {
        try (TarArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(tarOutput)) {
            tarOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            folder.accept(new VirtualFileVisitor() {
                @Override
                public void visit(VirtualFile visitedVirtualFile) throws ServerException {
                    if (filter.accept(visitedVirtualFile)) {
                        if (!visitedVirtualFile.equals(folder)) {
                            addTarEntry(visitedVirtualFile, tarOutputStream);
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

    private String getTarEntryName(VirtualFile virtualFile) {
        Path tarPath = virtualFile.getPath().subPath(folder.getPath());
        if (virtualFile.isFolder()) {
            return tarPath.toString() + '/';
        }
        return tarPath.toString();
    }

    private void addTarEntry(VirtualFile virtualFile, TarArchiveOutputStream tarOutputStream) throws ServerException {
        try {
            TarArchiveEntry tarEntry = new TarArchiveEntry(getTarEntryName(virtualFile));
            if (virtualFile.isFolder()) {
                tarEntry.setModTime(0);
                tarOutputStream.putArchiveEntry(tarEntry);
            } else {
                tarEntry.setSize(virtualFile.getLength());
                tarEntry.setModTime(virtualFile.getLastModificationDate());
                tarOutputStream.putArchiveEntry(tarEntry);
                try (InputStream content = virtualFile.getContent()) {
                    ByteStreams.copy(content, tarOutputStream);
                }
            }
            tarOutputStream.closeArchiveEntry();
        } catch (ForbiddenException e) {
            throw new ServerException(e.getServiceError());
        } catch (IOException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    public void extract(InputStream tarInput, boolean overwrite, int stripNumber)
            throws IOException, ForbiddenException, ConflictException, ServerException {
        try (TarArchiveInputStream tarInputStream = new TarArchiveInputStream(tarInput)) {
            InputStream notClosableInputStream = new NotClosableInputStream(tarInputStream);
            TarArchiveEntry tarEntry;
            while ((tarEntry = tarInputStream.getNextTarEntry()) != null) {
                VirtualFile extractFolder = folder;

                Path relativePath = Path.of(tarEntry.getName());

                if (stripNumber > 0) {
                    if (relativePath.length() <= stripNumber) {
                        continue;
                    }
                    relativePath = relativePath.subPath(stripNumber);
                }

                if (tarEntry.isDirectory()) {
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
            }
        }
    }
}
