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
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.project.shared.dto.event.VfsWatchEvent;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.eclipse.che.api.vfs.impl.file.FileWatcherNotificationListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *  @author gazarenkov
 */
public class VfsWatcherTest extends WsAgentTestBase {

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();

        pm.addWatchListener(new MyPTListener());
    }

    @Test
    public void testWatcher() throws Exception {

        eventService.subscribe(new TestSubscriber());

        pm.getProjectsRoot().createFolder("test");

        pm.getProjectsRoot().getChild("test").getVirtualFile().createFolder("test1");

        Thread.sleep(5000);

        pm.getProjectsRoot().getChild("test").getVirtualFile().createFile("file.txt", "lorem ipsum");

        Thread.sleep(5000);

        pm.getProjectsRoot().getChild("test/file.txt").getVirtualFile().updateContent("to be or not to be");

        Thread.sleep(5000);

        pm.getProjectsRoot().getChild("test").getVirtualFile().delete();

        Thread.sleep(5000);

        //System.out.println(" >>>>> "+pm.getProjects().size());



    }

    private static class TestSubscriber implements EventSubscriber<VfsWatchEvent> {

        @Override
        public void onEvent(VfsWatchEvent event) {

            System.out.println(" >>>>> " + event.getPath() +" " + event.getType() + " " + event.isFile());
        }
    }

    private static class MyPTListener extends FileWatcherNotificationListener {

        public MyPTListener() {
            super(file -> {
                if(file.getPath().getName().equals("file.txt"))
                    return true;
                return false;
            });
        }

        @Override
        public void onFileWatcherEvent(VirtualFile virtualFile, FileWatcherEventType eventType) {
            System.out.println(" file.txt EVENT>>>>> " + virtualFile.getPath() + " " + eventType.name());
        }
    }




}
