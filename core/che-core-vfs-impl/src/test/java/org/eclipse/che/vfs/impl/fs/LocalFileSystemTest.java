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
package org.eclipse.che.vfs.impl.fs;

import junit.framework.TestCase;

import org.eclipse.che.vfs.impl.fs.AccessControlList;
import org.eclipse.che.vfs.impl.fs.FSMountPoint;
import org.eclipse.che.vfs.impl.fs.FileLock;
import org.eclipse.che.vfs.impl.fs.FileLockSerializer;
import org.eclipse.che.vfs.impl.fs.FileMetadataSerializer;
import org.eclipse.che.vfs.impl.fs.LocalFileSystemProvider;
import org.eclipse.che.vfs.impl.fs.WorkspaceHashLocalFSMountStrategy;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.vfs.server.SystemPathsFilter;
import org.eclipse.che.api.vfs.server.URLHandlerFactorySetup;
import org.eclipse.che.api.vfs.server.VirtualFileSystemApplication;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
import org.eclipse.che.api.vfs.shared.ItemType;
import org.eclipse.che.api.vfs.shared.dto.File;
import org.eclipse.che.api.vfs.shared.dto.Item;
import org.eclipse.che.api.vfs.shared.dto.ItemList;
import org.eclipse.che.api.vfs.shared.dto.Link;
import org.eclipse.che.api.vfs.shared.dto.Principal;
import org.eclipse.che.api.vfs.shared.dto.Property;
import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.lang.ws.rs.ExtMediaType;
import org.eclipse.che.commons.user.UserImpl;
import org.apache.commons.codec.binary.Base64;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.ApplicationPublisher;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.everrest.core.tools.DependencySupplierImpl;
import org.everrest.core.tools.ResourceLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.EventListenerList;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.commons.lang.IoUtil.deleteRecursive;
import static org.eclipse.che.commons.lang.NameGenerator.generate;

public abstract class LocalFileSystemTest extends TestCase {
    protected static final String                    MY_WORKSPACE_ID           = "my-ws";
    protected static       VirtualFileSystemRegistry virtualFileSystemRegistry = new VirtualFileSystemRegistry();

    private static void enableAssertion(Class<?> clazz) {
        clazz.getClassLoader().setPackageAssertionStatus(clazz.getPackage().getName(), true);
    }

    protected String ROOT_ID;

    static {
        // enable assertion to test state of some components.
        enableAssertion(FSMountPoint.class);
        URLHandlerFactorySetup.setup(virtualFileSystemRegistry);
    }


    protected static final FileFilter SERVICE_DIR_FILTER = new FileFilter() {
        @Override
        public boolean accept(java.io.File pathname) {
            String name = pathname.getName();
            return !(FSMountPoint.SERVICE_DIR.equals(name));
        }
    };

    protected final String BASE_URI              = "http://localhost/service";
    protected final String SERVICE_URI           = BASE_URI + "/vfs/my-ws/v2/";
    protected final String DEFAULT_CONTENT       = "__TEST__";
    protected final byte[] DEFAULT_CONTENT_BYTES = DEFAULT_CONTENT.getBytes();

    protected Logger log = LoggerFactory.getLogger(getClass());

    protected String                  testRootPath;
    protected ResourceLauncher        launcher;
    protected java.io.File            root;
    protected FSMountPoint            mountPoint;
    protected LocalFileSystemProvider provider;
    protected java.io.File            testFsIoRoot;

    /** @see junit.framework.TestCase#setUp() */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // root directory for ALL virtual file systems
        root = createRootDirectory();

        final String testName = getName();
        // path to test directory
        testRootPath = '/' + testName;
        // backend for test virtual filesystem
        testFsIoRoot = WorkspaceHashLocalFSMountStrategy.calculateDirPath(root, MY_WORKSPACE_ID);
        assertTrue(new java.io.File(testFsIoRoot, testName).mkdirs());

        provider = new LocalFileSystemProvider(MY_WORKSPACE_ID, new WorkspaceHashLocalFSMountStrategy(root, root), new EventService(), null,
                                               SystemPathsFilter.ANY, virtualFileSystemRegistry);
        provider.mount(testFsIoRoot);
        mountPoint = provider.getMountPoint(true);
        ROOT_ID = mountPoint.getRoot().getId();
        virtualFileSystemRegistry.registerProvider(MY_WORKSPACE_ID, provider);

        DependencySupplierImpl dependencies = new DependencySupplierImpl();
        dependencies.addComponent(VirtualFileSystemRegistry.class, virtualFileSystemRegistry);
        dependencies.addComponent(EventListenerList.class, mountPoint.getEventService());
        ResourceBinder resources = new ResourceBinderImpl();
        ProviderBinder providers = new ApplicationProviderBinder();
        EverrestProcessor processor = new EverrestProcessor(resources, providers, dependencies, new EverrestConfiguration(), null);
        launcher = new ResourceLauncher(processor);
        ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, ProviderBinder.getInstance()));

        ApplicationPublisher deployer = new ApplicationPublisher(resources, providers);
        deployer.publish(new VirtualFileSystemApplication());

        // RUNTIME VARIABLES
        EnvironmentContext.getCurrent().setUser(new UserImpl("admin", "admin", null, Arrays.asList("workspace/admin", "workspace/developer")
                , false));
    }

    // Directory "fs-root" in "target" folder of builder project.
    // It is root where all (but we have only one at the in test) virtual filesystems are bound.
    private java.io.File createRootDirectory() throws Exception {
        java.io.File root = new java.io.File(
                new java.io.File(Thread.currentThread().getContextClassLoader().getResource(".").toURI()).getParentFile(),
                "fs-root");
        if (!(root.exists() || root.mkdirs())) {
            fail("Unable create directory for test content.");
        }
        return root;
    }

    /** @see junit.framework.TestCase#tearDown() */
    protected void tearDown() throws Exception {
        mountPoint.getPathLockFactory().checkClean();
        //assertTrue("Unable unmount local filesystem. ", provider.umount(testFsIoRoot));
        virtualFileSystemRegistry.unregisterProvider(MY_WORKSPACE_ID);
        assertFalse("Unable unmount local filesystem. ", provider.isMounted());
        if (!deleteRecursive(root)) {
            fail("Unable clean test content. ");
        }
        super.tearDown();
    }

    // Copied from LocalFileSystem#virtualFileToId and adopted for tests.
    protected String pathToId(String path) {
        if ("/".equals(path)) {
            return ROOT_ID;
        }
        try {
            return Base64.encodeBase64URLSafeString((MY_WORKSPACE_ID + ':' + path).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // Should never happen.
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    protected java.io.File getIoFile(String vfsPath) {
        return new java.io.File(testFsIoRoot, vfsPath);
    }

    protected byte[] readFile(String vfsPath) throws IOException {
        return Files.readAllBytes(getIoFile(vfsPath).toPath());
    }

    protected void writeFile(String vfsPath, byte[] content) throws IOException {
        Files.write(getIoFile(vfsPath).toPath(), content);
    }

    protected String createFile(String parent, String name, byte[] content) throws IOException {
        String newPath = parent + '/' + name;
        java.io.File file = getIoFile(newPath);
        assertTrue(String.format("File %s already exists. ", newPath), file.createNewFile());
        if (content != null) {
            writeFile(newPath, content);
        }
        return newPath;
    }

    protected String createDirectory(String parent, String name) {
        String newPath = parent + '/' + name;
        assertTrue(String.format("File %s already exists. ", newPath), getIoFile(newPath).mkdirs());
        return newPath;
    }

    protected int createTree(String parent, int numberItemsEachLevel, int depth, Map<String, String[]> properties, String suffix)
            throws Exception {
        if (depth == 0) {
            return 0;
        }
        int num = 0;
        for (int i = 0; i < numberItemsEachLevel; i++, num++) {
            String newName = generate(null, 8);
            String newPath = parent + '/' + newName;
            java.io.File f = getIoFile(newPath);

            if (i % 2 == 0) {
                assertTrue(String.format("Failed create %s", newPath), f.mkdirs());
            } else {
                if (suffix != null) {
                    newPath += suffix;
                }
                writeFile(newPath, DEFAULT_CONTENT_BYTES);
            }

            if (!(properties == null || properties.isEmpty())) {
                writeProperties(newPath, properties);
            }

            if (f.isDirectory()) {
                num += createTree(newPath, numberItemsEachLevel, depth - 1, properties, suffix);
            }
        }
        return num;
    }

    protected int createTree(String parent, int numberItemsEachLevel, int depth, Map<String, String[]> properties) throws Exception {
        return createTree(parent, numberItemsEachLevel, depth, properties, null);
    }

    protected void compareDirectories(String a, String b, boolean checkServiceDirs) throws IOException {
        compareDirectories(getIoFile(a), getIoFile(b), checkServiceDirs);
    }

    protected void compareDirectories(java.io.File a, java.io.File b, boolean checkServiceDirs) throws IOException {
        if (!a.isDirectory() || !b.isDirectory()) {
            fail();
        }
        LinkedList<Pair<java.io.File, java.io.File>> q = new LinkedList<>();
        q.add(new Pair<>(a, b));
        while (!q.isEmpty()) {
            Pair<java.io.File, java.io.File> current = q.pop();
            java.io.File[] files1 = current.first.listFiles(checkServiceDirs ? null : SERVICE_DIR_FILTER);
            java.io.File[] files2 = current.second.listFiles(checkServiceDirs ? null : SERVICE_DIR_FILTER);
            if (files1 == null || files2 == null || files1.length != files2.length) {
                fail();
            }
            Arrays.sort(files1);
            Arrays.sort(files2);
            for (int i = 0; i < files1.length; i++) {
                java.io.File file1 = files1[i];
                java.io.File file2 = files2[i];
                if (!file1.getName().equals(file2.getName())) {
                    fail();
                }
                if (file1.isFile()) {
                    try (FileInputStream in1 = new FileInputStream(file1); FileInputStream in2 = new FileInputStream(file2)) {
                        compareStreams(in1, in2);
                    }
                } else {
                    q.push(new Pair<>(file1, file2));
                }
            }
        }
    }

    protected void compareStreams(InputStream in1, InputStream in2) throws IOException {
        int r;
        byte[] buf = new byte[1024];
        while ((r = in1.read(buf)) != -1) {
            byte[] bytes1 = Arrays.copyOf(buf, r);
            r = in2.read(buf);
            if (r == -1) {
                fail();
            }
            byte[] bytes2 = Arrays.copyOf(buf, r);
            compareBytes(bytes1, bytes2);
        }
    }

    protected void compareBytes(byte[] bytes1, byte[] bytes2) {
        assertEquals(bytes1.length, bytes2.length);
        for (int i = 0; i < bytes1.length; i++) {
            byte b1 = bytes1[i];
            byte b2 = bytes2[i];
            assertEquals(b1, b2);
        }
    }

    // Plain list of directory content.
    protected List<String> flattenDirectory(String vfsPath) {
        java.io.File directory = getIoFile(vfsPath);
        assertTrue("Not a directory ", directory.isDirectory());
        final int splitIndex = directory.getAbsolutePath().length() + 1;
        List<String> files = new ArrayList<>();
        LinkedList<java.io.File> q = new LinkedList<>();
        q.add(directory);
        while (!q.isEmpty()) {
            java.io.File current = q.pop();
            java.io.File[] list = current.listFiles(SERVICE_DIR_FILTER);
            if (list != null) {
                for (java.io.File f : list) {
                    files.add(f.getAbsolutePath().substring(splitIndex));
                    if (f.isDirectory()) {
                        q.push(f);
                    }
                }
            }
        }
        if (!files.isEmpty()) {
            java.util.Collections.sort(files);
        }
        return files;
    }

    private FileMetadataSerializer propertiesSerializer = new FileMetadataSerializer();

    protected java.io.File writeProperties(String vfsPath, Map<String, String[]> properties)
            throws IOException {
        java.io.File file = getIoFile(vfsPath);
        java.io.File propsDir = new java.io.File(file.getParentFile(), FSMountPoint.PROPS_DIR);
        if (!(propsDir.exists() || propsDir.mkdirs())) {
            fail();
        }

        java.io.File propsFile = new java.io.File(propsDir, file.getName() + FSMountPoint.PROPERTIES_FILE_SUFFIX);
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(propsFile)))) {
            propertiesSerializer.write(dos, properties);
        }

        return propsFile;
    }

    protected Map<String, String[]> readProperties(String vfsPath) throws Exception {
        java.io.File file = getIoFile(vfsPath);
        java.io.File propsDir = new java.io.File(file.getParentFile(), FSMountPoint.PROPS_DIR);
        java.io.File propsFile = new java.io.File(propsDir, file.getName() + FSMountPoint.PROPERTIES_FILE_SUFFIX);
        FileInputStream fIn;
        try {
            fIn = new FileInputStream(propsFile);
        } catch (FileNotFoundException e) {
            return null;
        }
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(fIn))) {
            return propertiesSerializer.read(dis);
        }
    }

    protected void validateProperties(String vfsPath, Map<String, String[]> expectedProperties, boolean recursively)
            throws Exception {
        Map<String, String[]> props = readProperties(vfsPath);
        assertNotNull(String.format("Missed properties for '%s'. ", vfsPath), props);

        for (Map.Entry<String, String[]> e : expectedProperties.entrySet()) {
            String name = e.getKey();
            assertNotNull(String.format("Missed property '%s' for '%s'. ", name, vfsPath), props.get(name));
            assertEquals(String.format("Invalid property '%s' for '%s'. ", name, vfsPath),
                         e.getValue().length, props.get(name).length);
            Set<String> expected = new HashSet<>(Arrays.asList(e.getValue()));
            Set<String> actual = new HashSet<>(Arrays.asList(props.get(name)));
            assertEquals(String.format("Invalid property '%s' for '%s'. ", name, vfsPath), expected, actual);
        }

        java.io.File ioFile = getIoFile(vfsPath);
        if (ioFile.isDirectory() && recursively) {
            java.io.File[] children = ioFile.listFiles(SERVICE_DIR_FILTER);
            assertNotNull(children);
            for (java.io.File child : children) {
                validateProperties(vfsPath + '/' + child.getName(), expectedProperties, recursively);
            }
        }
    }

    protected void validateProperties(String vfsPath, Map<String, String[]> expectedProperties)
            throws Exception {
        validateProperties(vfsPath, expectedProperties, false);
    }

    protected java.io.File writePermissions(String vfsPath, Map<Principal, Set<String>> permissions) throws IOException {
        java.io.File file = getIoFile(vfsPath);
        java.io.File aclDir = new java.io.File(file.getParentFile(), FSMountPoint.ACL_DIR);
        if (!(aclDir.exists() || aclDir.mkdirs())) {
            fail();
        }

        java.io.File aclFile = new java.io.File(aclDir, file.getName() + FSMountPoint.ACL_FILE_SUFFIX);
        AccessControlList accessControlList = new AccessControlList(permissions);
        if (accessControlList.isEmpty()) {
            if (!aclFile.delete()) {
                if (aclFile.exists()) {
                    fail("Cannot clear ACL. ");
                }
            }
        } else {
            try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(aclFile)))) {
                accessControlList.write(dos);
            }
        }
        return aclFile;
    }

    protected Map<Principal, Set<String>> readPermissions(String vfsPath) throws Exception {
        java.io.File file = getIoFile(vfsPath);
        java.io.File aclDir = new java.io.File(file.getParentFile(), FSMountPoint.ACL_DIR);
        java.io.File aclFile = new java.io.File(aclDir, file.getName() + FSMountPoint.ACL_FILE_SUFFIX);
        FileInputStream fIn;
        try {
            fIn = new FileInputStream(aclFile);
        } catch (FileNotFoundException e) {
            return null;
        }
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(fIn))) {
            return AccessControlList.read(dis).getPermissionMap();
        }
    }

    private FileLockSerializer lockSerializer = new FileLockSerializer();

    protected boolean createLock(String vfsPath, String lockToken, long timeout) throws IOException {
        java.io.File file = getIoFile(vfsPath);
        if (file.isFile()) {
            java.io.File locksDir = new java.io.File(file.getParentFile(), FSMountPoint.LOCKS_DIR);
            if (!(locksDir.exists() || locksDir.mkdirs())) {
                fail();
            }

            java.io.File lockFile = new java.io.File(locksDir, file.getName() + FSMountPoint.LOCK_FILE_SUFFIX);
            try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(lockFile)))) {
                lockSerializer.write(dos, new FileLock(lockToken, timeout));
            }
            return true;
        }
        // If not a file.
        return false;
    }

    protected FileLock readLock(String vfsPath) throws Exception {
        java.io.File file = getIoFile(vfsPath);
        java.io.File locksDir = new java.io.File(file.getParentFile(), FSMountPoint.LOCKS_DIR);
        java.io.File lockFile = new java.io.File(locksDir, file.getName() + FSMountPoint.LOCK_FILE_SUFFIX);
        FileInputStream fIn;
        try {
            fIn = new FileInputStream(lockFile);
        } catch (FileNotFoundException e) {
            return null;
        }
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(fIn))) {
            return lockSerializer.read(dis);
        }
    }

    protected boolean exists(String vfsPath) {
        return getIoFile(vfsPath).exists();
    }

    // --------------------------------------------

    protected Item getItem(String id) throws Exception {
        String requestPath = SERVICE_URI + "item/" + id;
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, null, null);
        if (response.getStatus() == 200) {
            return (Item)response.getEntity();
        }
        if (response.getStatus() == 404) {
            return null;
        }
        fail(String.format("Unable get %s.\nStatus: %d\nMessage: %s", id, response.getStatus(), response.getEntity()));
        //
        return null;
    }

    protected String getPropertyValue(Item item, String propertyName) {
        for (Property property : item.getProperties()) {
            if (propertyName.equals(property.getName()) && !property.getValue().isEmpty()) {
                return property.getValue().get(0);
            }
        }
        return null;
    }

    protected List<String> getPropertyValues(Item item, String propertyName) {
        for (Property property : item.getProperties()) {
            if (propertyName.equals(property.getName())) {
                return property.getValue();
            }
        }
        return null;
    }

    protected void checkPage(String url, String httpMethod, Method m, List<Object> expected) throws Exception {
        checkPage(url, httpMethod, null, null, m, expected);
    }

    protected void checkPage(String url, String httpMethod, Map<String, List<String>> headers, byte[] body, Method m,
                             List<Object> expected) throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service(httpMethod, url, BASE_URI, headers, body, writer, null);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        @SuppressWarnings("unchecked")
        List<Item> items = ((ItemList)response.getEntity()).getItems();
        List<Object> all = new ArrayList<>(expected.size());
        for (Item i : items) {
            validateLinks(i);
            all.add(m.invoke(i));
        }
        assertEquals(all, expected);
    }

    protected void validateLinks(Item item) throws Exception {
        Map<String, Link> links = item.getLinks();

        if (links.size() == 0) {
            fail("Links not found. ");
        }

        Link link = links.get(Link.REL_SELF);
        assertNotNull(String.format("'%s' link not found. ", Link.REL_SELF), link);
        assertEquals(MediaType.APPLICATION_JSON, link.getType());
        assertEquals(Link.REL_SELF, link.getRel());
        assertEquals(UriBuilder.fromPath(SERVICE_URI).path("item").path(item.getId()).build().toString(), link.getHref());

        link = links.get(Link.REL_PARENT);
        if (item.getParentId() == null) {
            assertNull(String.format("'%s' link not allowed for root folder. ", Link.REL_PARENT), link);
        } else {
            assertNotNull(String.format("'%s' link not found. ", Link.REL_PARENT), link);
            assertEquals(MediaType.APPLICATION_JSON, link.getType());
            assertEquals(Link.REL_PARENT, link.getRel());
            assertEquals(UriBuilder.fromPath(SERVICE_URI).path("item").path(item.getParentId()).build().toString(),
                         link.getHref());
        }

        link = links.get(Link.REL_ACL);
        assertNotNull(String.format("'%s' link not found. ", Link.REL_ACL), link);
        assertEquals(MediaType.APPLICATION_JSON, link.getType());
        assertEquals(Link.REL_ACL, link.getRel());
        assertEquals(UriBuilder.fromPath(SERVICE_URI).path("acl").path(item.getId()).build().toString(), link.getHref());

        link = links.get(Link.REL_DELETE);
        if (item.getParentId() == null) {
            assertNull(String.format("'%s' link not allowed for root folder. ", Link.REL_DELETE), link);
        } else {
            assertNotNull(String.format("'%s' link not found. ", Link.REL_DELETE), link);
            assertEquals(null, link.getType());
            assertEquals(Link.REL_DELETE, link.getRel());
            if (item.getItemType() == ItemType.FILE && ((File)item).isLocked()) {
                assertEquals(
                        UriBuilder.fromPath(SERVICE_URI).path("delete").path(item.getId())
                                  .queryParam("lockToken", "[lockToken]").build().toString(),
                        link.getHref());
            } else {
                assertEquals(UriBuilder.fromPath(SERVICE_URI).path("delete").path(item.getId()).build().toString(),
                             link.getHref());
            }
        }

        link = links.get(Link.REL_COPY);
        if (item.getParentId() == null) {
            assertNull(String.format("'%s' link not allowed for root folder. ", Link.REL_COPY), link);
        } else {
            assertNotNull(String.format("'%s' link not found. ", Link.REL_COPY), link);
            assertEquals(MediaType.APPLICATION_JSON, link.getType());
            assertEquals(Link.REL_COPY, link.getRel());
            assertEquals(
                    UriBuilder.fromPath(SERVICE_URI).path("copy").path(item.getId()).queryParam("parentId", "[parentId]")
                              .build().toString(),
                    link.getHref());
        }

        link = links.get(Link.REL_MOVE);
        if (item.getParentId() == null) {
            assertNull(String.format("'%s' link not allowed for root folder. ", Link.REL_MOVE), link);
        } else {
            assertNotNull(String.format("'%s' link not found. ", Link.REL_MOVE), link);
            assertEquals(MediaType.APPLICATION_JSON, link.getType());
            assertEquals(Link.REL_MOVE, link.getRel());
            if (item.getItemType() == ItemType.FILE && ((File)item).isLocked()) {
                assertEquals(
                        UriBuilder.fromPath(SERVICE_URI).path("move").path(item.getId()).queryParam("parentId", "[parentId]")
                                  .queryParam("lockToken", "[lockToken]").build().toString(),
                        link.getHref());
            } else {
                assertEquals(
                        UriBuilder.fromPath(SERVICE_URI).path("move").path(item.getId()).queryParam("parentId", "[parentId]")
                                  .build().toString(),
                        link.getHref());
            }
        }

        link = links.get(Link.REL_RENAME);
        if (item.getParentId() == null) {
            assertNull(String.format("'%s' link not allowed for root folder. ", Link.REL_RENAME), link);
        } else {
            assertNotNull(String.format("'%s' link not found. ", Link.REL_RENAME), link);
            assertEquals(MediaType.APPLICATION_JSON, link.getType());
            assertEquals(Link.REL_RENAME, link.getRel());
            if (item.getItemType() == ItemType.FILE && ((File)item).isLocked()) {
                assertEquals(
                        UriBuilder.fromPath(SERVICE_URI).path("rename").path(item.getId()).queryParam("newname", "[newname]")
                                  .queryParam("mediaType", "[mediaType]").queryParam("lockToken", "[lockToken]").build().toString(),
                        link.getHref());
            } else {
                assertEquals(
                        UriBuilder.fromPath(SERVICE_URI).path("rename").path(item.getId()).queryParam("newname", "[newname]")
                                  .queryParam("mediaType", "[mediaType]").build().toString(),
                        link.getHref());
            }
        }

        ItemType type = item.getItemType();
        if (type == ItemType.FILE) {
            File file = (File)item;

            link = links.get(Link.REL_CONTENT);
            assertNotNull(String.format("'%s' link not found. ", Link.REL_CONTENT), link);
            assertEquals(file.getMimeType(), link.getType());
            assertEquals(Link.REL_CONTENT, link.getRel());
            assertEquals(UriBuilder.fromPath(SERVICE_URI).path("content").path(file.getId()).build().toString(),
                         link.getHref());

            link = links.get(Link.REL_DOWNLOAD_FILE);
            assertNotNull(String.format("'%s' link not found. ", Link.REL_DOWNLOAD_FILE), link);
            assertEquals(file.getMimeType(), link.getType());
            assertEquals(Link.REL_DOWNLOAD_FILE, link.getRel());
            assertEquals(UriBuilder.fromPath(SERVICE_URI).path("downloadfile").path(file.getId()).build().toString(),
                         link.getHref());

            link = links.get(Link.REL_CONTENT_BY_PATH);
            assertNotNull(String.format("'%s' link not found. ", Link.REL_CONTENT_BY_PATH), link);
            assertEquals(file.getMimeType(), link.getType());
            assertEquals(Link.REL_CONTENT_BY_PATH, link.getRel());
            assertEquals(UriBuilder.fromPath(SERVICE_URI).path("contentbypath").path(file.getPath().substring(1)).build()
                                   .toString(), link.getHref());

            link = links.get(Link.REL_CURRENT_VERSION);
            assertNotNull(String.format("'%s' link not found. ", Link.REL_CURRENT_VERSION), link);
            assertEquals(MediaType.APPLICATION_JSON, link.getType());
            assertEquals(Link.REL_CURRENT_VERSION, link.getRel());
            String expectedCurrentVersionId = file.getId();
            assertEquals(UriBuilder.fromPath(SERVICE_URI).path("item").path(expectedCurrentVersionId).build().toString(),
                         link.getHref());

            link = links.get(Link.REL_VERSION_HISTORY);
            assertNotNull(String.format("'%s' link not found. ", Link.REL_VERSION_HISTORY), link);
            assertEquals(MediaType.APPLICATION_JSON, link.getType());
            assertEquals(Link.REL_VERSION_HISTORY, link.getRel());
            assertEquals(UriBuilder.fromPath(SERVICE_URI).path("version-history").path(file.getId()).build().toString(),
                         link.getHref());

            link = links.get(Link.REL_LOCK);
            if (file.isLocked()) {
                assertNull(String.format("'%s' link not allowed for locked files. ", Link.REL_LOCK), link);
                link = links.get(Link.REL_UNLOCK);
                assertEquals(null, link.getType());
                assertEquals(Link.REL_UNLOCK, link.getRel());
                assertEquals(
                        UriBuilder.fromPath(SERVICE_URI).path("unlock").path(file.getId())
                                  .queryParam("lockToken", "[lockToken]").build().toString(),
                        link.getHref());
            } else {
                assertNotNull(String.format("'%s' link not found. ", Link.REL_LOCK), link);
                assertEquals(MediaType.APPLICATION_JSON, link.getType());
                assertEquals(Link.REL_LOCK, link.getRel());
                assertEquals(UriBuilder.fromPath(SERVICE_URI).path("lock").path(file.getId()).queryParam("timeout", "[timeout]").build()
                                       .toString(), link.getHref());
                link = links.get(Link.REL_UNLOCK);
                assertNull(String.format("'%s' link not allowed for unlocked files. ", Link.REL_UNLOCK), link);
            }
        } else {
            link = links.get(Link.REL_CHILDREN);
            assertNotNull(String.format("'%s' link not found. ", Link.REL_CHILDREN), link);
            assertEquals(MediaType.APPLICATION_JSON, link.getType());
            assertEquals(Link.REL_CHILDREN, link.getRel());
            assertEquals(UriBuilder.fromPath(SERVICE_URI).path("children").path(item.getId()).build().toString(),
                         link.getHref());

            link = links.get(Link.REL_CREATE_FOLDER);
            assertNotNull(String.format("'%s' link not found. ", Link.REL_CREATE_FOLDER), link);
            assertEquals(MediaType.APPLICATION_JSON, link.getType());
            assertEquals(Link.REL_CREATE_FOLDER, link.getRel());
            assertEquals(
                    UriBuilder.fromPath(SERVICE_URI).path("folder").path(item.getId()).queryParam("name", "[name]")
                              .build().toString(),
                    link.getHref());

            link = links.get(Link.REL_CREATE_FILE);
            assertNotNull(String.format("'%s' link not found. ", Link.REL_CREATE_FILE), link);
            assertEquals(MediaType.APPLICATION_JSON, link.getType());
            assertEquals(Link.REL_CREATE_FILE, link.getRel());
            assertEquals(
                    UriBuilder.fromPath(SERVICE_URI).path("file").path(item.getId()).queryParam("name", "[name]")
                              .build().toString(),
                    link.getHref());

            link = links.get(Link.REL_UPLOAD_FILE);
            assertNotNull(String.format("'%s' link not found. ", Link.REL_UPLOAD_FILE), link);
            assertEquals(MediaType.TEXT_HTML, link.getType());
            assertEquals(Link.REL_UPLOAD_FILE, link.getRel());
            assertEquals(UriBuilder.fromPath(SERVICE_URI).path("uploadfile").path(item.getId()).build().toString(),
                         link.getHref());

            link = links.get(Link.REL_EXPORT);
            assertNotNull(String.format("'%s' link not found. ", Link.REL_EXPORT), link);
            assertEquals(ExtMediaType.APPLICATION_ZIP, link.getType());
            assertEquals(Link.REL_EXPORT, link.getRel());
            assertEquals(UriBuilder.fromPath(SERVICE_URI).path("export").path(item.getId()).build().toString(),
                         link.getHref());

            link = links.get(Link.REL_IMPORT);
            assertNotNull(String.format("'%s' link not found. ", Link.REL_IMPORT), link);
            assertEquals(ExtMediaType.APPLICATION_ZIP, link.getType());
            assertEquals(Link.REL_IMPORT, link.getRel());
            assertEquals(UriBuilder.fromPath(SERVICE_URI).path("import").path(item.getId()).build().toString(),
                         link.getHref());

            link = links.get(Link.REL_DOWNLOAD_ZIP);
            assertNotNull(String.format("'%s' link not found. ", Link.REL_DOWNLOAD_ZIP), link);
            assertEquals(ExtMediaType.APPLICATION_ZIP, link.getType());
            assertEquals(Link.REL_DOWNLOAD_ZIP, link.getRel());
            assertEquals(UriBuilder.fromPath(SERVICE_URI).path("downloadzip").path(item.getId()).build().toString(),
                         link.getHref());

            link = links.get(Link.REL_UPLOAD_ZIP);
            assertNotNull(String.format("'%s' link not found. ", Link.REL_UPLOAD_ZIP), link);
            assertEquals(MediaType.TEXT_HTML, link.getType());
            assertEquals(Link.REL_UPLOAD_ZIP, link.getRel());
            assertEquals(UriBuilder.fromPath(SERVICE_URI).path("uploadzip").path(item.getId()).build().toString(),
                         link.getHref());
        }
    }

    protected void validateUrlTemplates(VirtualFileSystemInfo info) throws Exception {
        Map<String, Link> templates = info.getUrlTemplates();
        //log.info(">>>>>>>>>\n" + templates);

        Link template = templates.get(Link.REL_ITEM);
        assertNotNull("'" + Link.REL_ITEM + "' template not found. ", template);
        assertEquals(MediaType.APPLICATION_JSON, template.getType());
        assertEquals(Link.REL_ITEM, template.getRel());
        assertEquals(UriBuilder.fromPath(SERVICE_URI).path("item").path("[id]").build().toString(), template.getHref());

        template = templates.get(Link.REL_ITEM_BY_PATH);
        assertNotNull("'" + Link.REL_ITEM_BY_PATH + "' template not found. ", template);
        assertEquals(MediaType.APPLICATION_JSON, template.getType());
        assertEquals(Link.REL_ITEM_BY_PATH, template.getRel());
        assertEquals(UriBuilder.fromPath(SERVICE_URI).path("itembypath").path("[path]").build().toString(),
                     template.getHref());

        template = templates.get(Link.REL_COPY);
        assertNotNull("'" + Link.REL_COPY + "' template not found. ", template);
        assertEquals(MediaType.APPLICATION_JSON, template.getType());
        assertEquals(Link.REL_COPY, template.getRel());
        assertEquals(UriBuilder.fromPath(SERVICE_URI).path("copy").path("[id]").queryParam("parentId", "[parentId]")
                               .build().toString(), template.getHref());

        template = templates.get(Link.REL_MOVE);
        assertNotNull("'" + Link.REL_MOVE + "' template not found. ", template);
        assertEquals(MediaType.APPLICATION_JSON, template.getType());
        assertEquals(Link.REL_MOVE, template.getRel());
        assertEquals(UriBuilder.fromPath(SERVICE_URI).path("move").path("[id]").queryParam("parentId", "[parentId]")
                               .queryParam("lockToken", "[lockToken]").build().toString(), template.getHref());

        template = templates.get(Link.REL_CREATE_FILE);
        assertNotNull("'" + Link.REL_CREATE_FILE + "' template not found. ", template);
        assertEquals(MediaType.APPLICATION_JSON, template.getType());
        assertEquals(Link.REL_CREATE_FILE, template.getRel());
        assertEquals(UriBuilder.fromPath(SERVICE_URI).path("file").path("[parentId]").queryParam("name", "[name]")
                               .build().toString(), template.getHref());

        template = templates.get(Link.REL_CREATE_FOLDER);
        assertNotNull("'" + Link.REL_CREATE_FOLDER + "' template not found. ", template);
        assertEquals(MediaType.APPLICATION_JSON, template.getType());
        assertEquals(Link.REL_CREATE_FOLDER, template.getRel());
        assertEquals(UriBuilder.fromPath(SERVICE_URI).path("folder").path("[parentId]").queryParam("name", "[name]")
                               .build().toString(), template.getHref());

        template = templates.get(Link.REL_LOCK);
        assertNotNull("'" + Link.REL_LOCK + "' template not found. ", template);
        assertEquals(MediaType.APPLICATION_JSON, template.getType());
        assertEquals(Link.REL_LOCK, template.getRel());
        assertEquals(UriBuilder.fromPath(SERVICE_URI).path("lock").path("[id]").queryParam("timeout", "[timeout]").build().toString(),
                     template.getHref());

        template = templates.get(Link.REL_UNLOCK);
        assertNotNull("'" + Link.REL_UNLOCK + "' template not found. ", template);
        assertEquals(null, template.getType());
        assertEquals(Link.REL_UNLOCK, template.getRel());
        assertEquals(UriBuilder.fromPath(SERVICE_URI).path("unlock").path("[id]").queryParam("lockToken", "[lockToken]")
                               .build().toString(), template.getHref());

        template = templates.get(Link.REL_SEARCH);
        assertNotNull("'" + Link.REL_SEARCH + "' template not found. ", template);
        assertEquals(MediaType.APPLICATION_JSON, template.getType());
        assertEquals(Link.REL_SEARCH, template.getRel());
        assertEquals(
                UriBuilder.fromPath(SERVICE_URI).path("search").queryParam("statement", "[statement]")
                          .queryParam("maxItems", "[maxItems]").queryParam("skipCount", "[skipCount]").build().toString(),
                template.getHref());

        template = templates.get(Link.REL_SEARCH_FORM);
        assertNotNull("'" + Link.REL_SEARCH_FORM + "' template not found. ", template);
        assertEquals(MediaType.APPLICATION_JSON, template.getType());
        assertEquals(Link.REL_SEARCH_FORM, template.getRel());
        assertEquals(
                UriBuilder.fromPath(SERVICE_URI).path("search").queryParam("maxItems", "[maxItems]")
                          .queryParam("skipCount", "[skipCount]").queryParam("propertyFilter", "[propertyFilter]").build().toString(),
                template.getHref());
    }
}
