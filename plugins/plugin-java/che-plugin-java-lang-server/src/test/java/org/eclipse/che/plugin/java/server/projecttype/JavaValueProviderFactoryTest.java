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
package org.eclipse.che.plugin.java.server.projecttype;

import org.eclipse.che.api.project.server.FileEntry;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.type.ValueProvider;
import org.eclipse.che.api.project.server.type.ValueStorageException;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.eclipse.che.ide.ext.java.shared.Constants.CONTAINS_JAVA_FILES;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test for the Project Type provider
 *
 * @author Florent Benoit
 */
@Listeners(value = {MockitoTestNGListener.class})
public class JavaValueProviderFactoryTest {

    @Mock
    private FolderEntry rootProjectFolder;

    /**
     * In this case we have a folder with a java file, so it should find a java file
     */
    @Test
    public void checkFoundJavaFilesInCurrentFolder() throws Throwable {

        // we return a file entry that is a java file
        FileEntry fileEntry = mock(FileEntry.class);
        when(fileEntry.getName()).thenReturn("helloworld.java");
        when(rootProjectFolder.getChildFiles()).thenReturn(Collections.singletonList(fileEntry));
        ValueProvider javaPropertiesValueProvider = new JavaValueProviderFactory().newInstance(rootProjectFolder);
        List<String> hasJavaFiles = javaPropertiesValueProvider.getValues(CONTAINS_JAVA_FILES);
        assertNotNull(hasJavaFiles);
        assertEquals(hasJavaFiles, Collections.singletonList("true"));
    }

    /**
     * In this case we have a folder with a javascript file, so it shouldn't find any java files
     */
    @Test
    public void checkNotFoundJavaFilesInCurrentFolder() throws Throwable {

        // we return a file entry that is a javascript file
        FileEntry fileEntry = mock(FileEntry.class);
        when(fileEntry.getName()).thenReturn("helloworld.js");
        when(rootProjectFolder.getChildFiles()).thenReturn(Collections.singletonList(fileEntry));
        ValueProvider javaPropertiesValueProvider = new JavaValueProviderFactory().newInstance(rootProjectFolder);
        try {
            javaPropertiesValueProvider.getValues(CONTAINS_JAVA_FILES);
        } catch (ValueStorageException e) {
            assertEquals(e.getMessage(), "There are no Java files inside the project");
        }

    }

    /**
     * In this case we have a folder with a javascript file, but some sub folders contains java files
     */
    @Test
    public void checkFoundJavaButNotInRootFolder() throws Throwable {

        // we return a file entry that is a javascript file
        FileEntry fileEntry = mock(FileEntry.class);
        when(fileEntry.getName()).thenReturn("helloworld.js");
        when(rootProjectFolder.getChildFiles()).thenReturn(Collections.singletonList(fileEntry));

        FileEntry javaFileEntry = mock(FileEntry.class);
        when(javaFileEntry.getName()).thenReturn("helloworld.java");

        FolderEntry subFolder = mock(FolderEntry.class);
        when(subFolder.getChildFiles()).thenReturn(Collections.singletonList(javaFileEntry));
        when(rootProjectFolder.getChildFolders()).thenReturn(Collections.singletonList(subFolder));

        ValueProvider javaPropertiesValueProvider = new JavaValueProviderFactory().newInstance(rootProjectFolder);
        List<String> hasJavaFiles = javaPropertiesValueProvider.getValues(CONTAINS_JAVA_FILES);
        assertNotNull(hasJavaFiles);
        assertEquals(hasJavaFiles, Collections.singletonList("true"));
    }


    /**
     * In this case we have java file in a very deep folder
     */
    @Test
    public void checkFoundJavaDeepFolder() throws Throwable {

        // we return a file entry that is a javascript file
        FileEntry fileEntry = mock(FileEntry.class);
        when(fileEntry.getName()).thenReturn("helloworld.js");
        when(rootProjectFolder.getChildFiles()).thenReturn(Collections.singletonList(fileEntry));

        FolderEntry subFolder = mock(FolderEntry.class);
        when(subFolder.getChildFiles()).thenReturn(Collections.emptyList());
        when(rootProjectFolder.getChildFolders()).thenReturn(Collections.singletonList(subFolder));


        FileEntry javaFileEntry = mock(FileEntry.class);
        when(javaFileEntry.getName()).thenReturn("helloworld.java");
        FolderEntry subSubFolder = mock(FolderEntry.class);
        when(subSubFolder.getChildFiles()).thenReturn(Collections.singletonList(javaFileEntry));
        when(subFolder.getChildFolders()).thenReturn(Collections.singletonList(subSubFolder));

        ValueProvider javaPropertiesValueProvider = new JavaValueProviderFactory().newInstance(rootProjectFolder);
        List<String> hasJavaFiles = javaPropertiesValueProvider.getValues(CONTAINS_JAVA_FILES);
        assertNotNull(hasJavaFiles);
        assertEquals(hasJavaFiles, Collections.singletonList("true"));
    }


    /**
     * In this case we have an exception while trying to search in sub folders
     */
    @Test(expectedExceptions = ValueStorageException.class)
    public void checkWithErrorInSubfolder() throws Throwable {

        // we return a file entry that is a javascript file
        FileEntry fileEntry = mock(FileEntry.class);
        when(fileEntry.getName()).thenReturn("helloworld.js");
        when(rootProjectFolder.getChildFiles()).thenReturn(Collections.singletonList(fileEntry));

        FileEntry javaFileEntry = mock(FileEntry.class);
        when(javaFileEntry.getName()).thenThrow(new IllegalStateException("unable to get name of this file"));

        FolderEntry subFolder = mock(FolderEntry.class);
        when(subFolder.getChildFiles()).thenReturn(Collections.singletonList(javaFileEntry));
        when(rootProjectFolder.getChildFolders()).thenReturn(Collections.singletonList(subFolder));

        ValueProvider javaPropertiesValueProvider = new JavaValueProviderFactory().newInstance(rootProjectFolder);
        javaPropertiesValueProvider.getValues(CONTAINS_JAVA_FILES);
        org.testng.Assert.fail("We should have exception reported");
    }

}
