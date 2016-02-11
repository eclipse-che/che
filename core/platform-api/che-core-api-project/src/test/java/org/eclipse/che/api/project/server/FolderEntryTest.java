/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.vfs.server.SystemPathsFilter;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.VirtualFileSystemUser;
import org.eclipse.che.api.vfs.server.VirtualFileSystemUserContext;
import org.eclipse.che.api.vfs.server.impl.memory.MemoryMountPoint;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author andrew00x
 */
public class FolderEntryTest {
    private static final String      workspace     = "my_ws";
    private static final String      vfsUserName   = "dev";
    private static final Set<String> vfsUserGroups = new LinkedHashSet<>(Arrays.asList("workspace/developer"));

    private MemoryMountPoint mmp;
    private VirtualFile      myVfProject;
    private VirtualFile      myVfFolder;
    private FolderEntry      myFolder;

    @BeforeMethod
    public void setUp() throws Exception {
        mmp = new MemoryMountPoint(workspace, new EventService(), null, new VirtualFileSystemUserContext() {
            @Override
            public VirtualFileSystemUser getVirtualFileSystemUser() {
                return new VirtualFileSystemUser(vfsUserName, vfsUserGroups);
            }
        }, new SystemPathsFilter(Collections.singleton(new ProjectMiscPathFilter())));
        VirtualFile myVfRoot = mmp.getRoot();
        myVfProject = myVfRoot.createFolder("my_project");
        myVfFolder = myVfProject.createFolder("test_folder");
        myVfFolder.createFile("child_file", new ByteArrayInputStream("to be or not to be".getBytes()));
        myVfFolder.createFolder("child_folder");
        myFolder = new FolderEntry(workspace, myVfFolder);
        Assert.assertTrue(myFolder.isFolder());
    }

    @Test
    public void testGetName() throws Exception {
        Assert.assertEquals(myFolder.getName(), myVfFolder.getName());
    }

    @Test
    public void testGetPath() throws Exception {
        Assert.assertEquals(myFolder.getPath(), myVfFolder.getPath());
    }

    @Test
    public void testGetParent() throws Exception {
        Assert.assertEquals(myFolder.getParent().getPath(), myVfProject.getPath());
    }

    @Test
    public void testRename() throws Exception {
        String name = myFolder.getName();
        String newName = name + "_renamed";
        String newPath = myVfProject.getVirtualFilePath().newPath(newName).toString();

        myFolder.rename(newName);
        Assert.assertNull(myVfProject.getChild(name));
        Assert.assertNotNull(myVfProject.getChild(newName));
        Assert.assertEquals(myFolder.getName(), newName);
        Assert.assertEquals(myFolder.getPath(), newPath);
        Assert.assertNotNull(myFolder.getChild("child_file"));
        Assert.assertNotNull(myFolder.getChild("child_folder"));
    }

    @Test
    public void testMove() throws Exception {
        VirtualFile vfProject = mmp.getRoot().createFolder("my_project_2");
        vfProject.createFolder(Constants.CODENVY_DIR).createFile("project", null);
        String name = myFolder.getName();
        String newPath = vfProject.getVirtualFilePath().newPath(name).toString();

        myFolder.moveTo(vfProject.getPath());
        Assert.assertNull(myVfProject.getChild(name));
        Assert.assertNotNull(vfProject.getChild(name));
        Assert.assertEquals(myFolder.getName(), name);
        Assert.assertEquals(myFolder.getPath(), newPath);
        Assert.assertNotNull(myFolder.getChild("child_file"));
        Assert.assertNotNull(myFolder.getChild("child_folder"));
    }

    @Test
    public void testCopy() throws Exception {
        VirtualFile vfProject = mmp.getRoot().createFolder("my_project_2");
        vfProject.createFolder(Constants.CODENVY_DIR).createFile("project", null);
        String name = myFolder.getName();
        String newPath = vfProject.getVirtualFilePath().newPath(name).toString();

        FolderEntry copy = myFolder.copyTo(vfProject.getPath());
        Assert.assertNotNull(myVfProject.getChild(name));
        Assert.assertNotNull(vfProject.getChild(name));
        Assert.assertEquals(copy.getName(), name);
        Assert.assertEquals(copy.getPath(), newPath);
        Assert.assertNotNull(myFolder.getChild("child_file"));
        Assert.assertNotNull(myFolder.getChild("child_folder"));
    }

    @Test
    public void testRemove() throws Exception {
        String name = myFolder.getName();
        myFolder.remove();
        Assert.assertFalse(myVfFolder.exists());
        Assert.assertNull(myVfProject.getChild(name));
    }
}
