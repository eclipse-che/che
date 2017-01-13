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
package org.eclipse.che.api.local;

import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;

import org.eclipse.che.api.local.storage.LocalStorage;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anton Korneta
 */
public class LocalStorageTest {

    private LocalStorage storage;
    private String       storageDir;

    @BeforeTest
    public void prepare() throws URISyntaxException {
        storageDir = targetDir().resolve("root_storage").toString();
    }

    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = "Impossible to create root folder for local storage")
    public void createStorageInDirectoryWithoutPermissionsTest() throws IOException {
        File f = new File(storageDir, "dir-root");
        f.mkdirs();
        f.setWritable(false, true);
        File dest = new File(f, "dir");
        storage = new LocalStorage(dest.getPath(), "file.json");
    }

    @Test
    public void storeWithoutExistingFileTest() throws URISyntaxException, IOException {
        File storedFile = new File(storageDir, "stored.json");
        if (storedFile.exists()) {
            storedFile.delete();
        }
        storage = new LocalStorage(storageDir, storedFile.getName());
        List<String> list = new ArrayList<>();
        list.add("first element");
        list.add("second element");
        storage.store(list);

        assertTrue(storedFile.exists());

        String content = Files.toString(storedFile, Charset.forName("UTF-8"));
        System.out.println(content);

        assertEquals(content, "[\n" +
                              "  \"first element\",\n" +
                              "  \"second element\"\n" +
                              "]");
    }

    @Test
    public void storeWhenFileAlreadyExistTest() throws URISyntaxException, IOException {
        File storedFile = new File(storageDir, "stored.json");
        if (!storedFile.exists()) {
            storedFile.createNewFile();
        }
        storage = new LocalStorage(storageDir, storedFile.getName());
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        storage.store(list);
    }

    @Test(expectedExceptions = IOException.class)
    public void storeToFileWithoutPermissionsTest() throws IOException {
        File f = new File(storageDir, "with-permissions-1");
        f.mkdirs();
        f.setWritable(false, true);
        storage = new LocalStorage(f.getPath(), "file.json");
        List<String> list = new ArrayList<>();
        list.add("any string");
        storage.store(list);
    }

    @Test
    public void storeWithInvalidJsonContentTest() throws URISyntaxException, IOException {
        File storedFile = new File(storageDir, "file-1.json");
        storage = new LocalStorage(storageDir, storedFile.getName());
        storage.store("\\/*=invalid.");
    }

    @Test
    public void loadContentOfFileAndParseItIntoObjectTest() throws URISyntaxException, IOException {
        storage = new LocalStorage(storageDir, "file.json");
        Map<String, String> map = new HashMap<>();
        map.put("first", "value1");
        storage.store(map);
        map = storage.load(new TypeToken<Map<String, String>>() {});
        assertEquals(map.get("first"), "value1");
    }

    @Test
    public void loadWithInvalidJsonContentTest() throws URISyntaxException, IOException {
        File storedFile = new File(storageDir, "invalid.json");
        if (!storedFile.exists()) {
            storedFile.createNewFile();
        }
        Files.write("\\/*=invalid.", storedFile, Charset.forName("UTF-8"));
        storage = new LocalStorage(storageDir, storedFile.getName());
        assertEquals(storage.load(new TypeToken<Map<String, String>>() {}), null);
    }

    @Test
    public void loadFromNonExistentFileTest() throws URISyntaxException, IOException {
        File storedFile = new File(storageDir, "nonexistent.json");
        storage = new LocalStorage(storageDir, storedFile.getName());
        assertEquals(storage.load(new TypeToken<String>() {}), null);
    }

    @Test
    public void loadValidContentsOfExistingFilesIntoContainersTest() throws URISyntaxException, IOException {
        File file1 = new File(storageDir, "list.json");
        File file2 = new File(storageDir, "map.json");
        LocalStorage listStorage = new LocalStorage(storageDir, file1.getName());
        LocalStorage mapStorage = new LocalStorage(storageDir, file2.getName());
        List<String> list = Collections.singletonList("e1");
        Map<String, String> map = new HashMap<>();
        map.put("k1", "v2");

        listStorage.store(list);
        mapStorage.store(map);
        List<String> list1 = listStorage.loadList(new TypeToken<List<String>>() {});
        Map<String, String> map1 = mapStorage.loadMap(new TypeToken<Map<String, String>>() {});

        assertEquals(list1.get(0), "e1");
        assertEquals(map1.get("k1"), "v2");
    }

    @Test
    public void loadInvalidContentsOfExistingFilesIntoContainersTest() throws URISyntaxException, IOException {
        File storedFile = new File(storageDir, "invalid.json");
        if (!storedFile.exists()) {
            storedFile.createNewFile();
        }
        Files.write("\\/*=invalid.", storedFile, Charset.forName("UTF-8"));
        storage = new LocalStorage(storageDir, storedFile.getName());

        List<String> list = storage.loadList(new TypeToken<List<String>>() {});
        Map<String, String> map = storage.loadMap(new TypeToken<Map<String, String>>() {});

        assertTrue(list.isEmpty());
        assertTrue(map.isEmpty());
    }


    private Path targetDir() throws URISyntaxException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(url);
        return Paths.get(url.toURI()).getParent();
    }
}
