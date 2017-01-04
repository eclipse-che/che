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

import com.google.common.io.Files;

import org.eclipse.che.api.vfs.Path;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocalVirtualFileAssertionHelper {
    private final File testDirectory;

    public LocalVirtualFileAssertionHelper(File testDirectory) {
        this.testDirectory = testDirectory;
    }

    public void assertThatIoFileExists(Path virtualFilePath) {
        File ioFile = getIoFile(virtualFilePath);
        assertTrue(String.format("Expected %s but not found", ioFile), ioFile.exists());
    }

    public void assertThatIoFileDoesNotExist(Path vfsPath) {
        File ioFile = getIoFile(vfsPath);
        assertFalse(String.format("Not expected %s but found", ioFile), ioFile.exists());
    }

    public void assertThatIoFilesHaveSameContent(Path fileOne, Path fileTwo) throws IOException {
        assertArrayEquals(String.format("Same content expected for files %s and %s", fileOne, fileTwo),
                          Files.toByteArray(getIoFile(fileOne)), Files.toByteArray(getIoFile(fileTwo)));
    }

    public void assertThatIoFileHasContent(Path virtualFilePath, byte[] bytes) throws IOException {
        assertArrayEquals(bytes, Files.toByteArray(getIoFile(virtualFilePath)));
    }


    public void assertThatMetadataIoFileDoesNotExist(Path virtualFilePath) {
        File metadataIoFile = getMetadataIoFile(virtualFilePath);
        assertFalse(metadataIoFile.exists());
    }

    public void assertThatMetadataIoFilesHaveSameContent(Path fileOne, Path fileTwo) throws IOException {
        assertArrayEquals(String.format("Same content expected for files %s and %s", fileOne, fileTwo),
                          Files.toByteArray(getMetadataIoFile(fileOne)), Files.toByteArray(getMetadataIoFile(fileTwo)));
    }

    public void assertThatMetadataIoFileHasContent(Path virtualFilePath, byte[] bytes) throws IOException {
        assertArrayEquals(bytes, Files.toByteArray(getMetadataIoFile(virtualFilePath)));
    }


    public void assertThatLockIoFileExists(Path virtualFilePath) {
        File ioFile = getLockIoFile(virtualFilePath);
        assertTrue(ioFile.exists());
    }

    public void assertThatLockIoFileDoesNotExist(Path virtualFilePath) {
        File lockIoFile = getLockIoFile(virtualFilePath);
        assertFalse(lockIoFile.exists());
    }


    private File getIoFile(Path virtualFilePath) {
        return new File(testDirectory, toIoPath(virtualFilePath));
    }

    private File getMetadataIoFile(Path virtualFilePath) {
        Path metadataFilePath;
        if (virtualFilePath.isRoot()) {
            metadataFilePath = virtualFilePath.newPath(".vfs", "props", virtualFilePath.getName() + "_props");
        } else {
            metadataFilePath = virtualFilePath.getParent().newPath(".vfs", "props", virtualFilePath.getName() + "_props");
        }
        return new File(testDirectory, toIoPath(metadataFilePath));
    }

    private File getLockIoFile(Path virtualFilePath) {
        Path lockFilePath;
        if (virtualFilePath.isRoot()) {
            lockFilePath = virtualFilePath.newPath(".vfs", "locks", virtualFilePath.getName() + "_lock");
        } else {
            lockFilePath = virtualFilePath.getParent().newPath(".vfs", "locks", virtualFilePath.getName() + "_lock");
        }
        return new File(testDirectory, toIoPath(lockFilePath));
    }

    private String toIoPath(Path vfsPath) {
        if (vfsPath.isRoot()) {
            return "";
        }
        if ('/' == File.separatorChar) {
            return vfsPath.toString();
        }
        return vfsPath.join(File.separatorChar);
    }
}
