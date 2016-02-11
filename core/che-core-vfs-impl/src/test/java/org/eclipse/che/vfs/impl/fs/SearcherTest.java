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

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.vfs.server.SystemPathsFilter;
import org.eclipse.che.api.vfs.server.VirtualFileFilter;
import org.eclipse.che.api.vfs.shared.dto.Item;
import org.eclipse.che.api.vfs.shared.dto.ItemList;
import org.eclipse.che.commons.lang.Pair;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

public class SearcherTest extends LocalFileSystemTest {
    private static final String FILE_NAME          = "SearcherTest_File1";
    private static final String SEARCH_FOLDER_PATH = "SearcherTest_Folder";
    private Pair<String[], String>[] queryToResult;

    private String searchTestPath;
    private String file1;
    private String file2;
    private String file3;
    private String file4;

    private CleanableSearcher searcher;
    private SearcherManager   searcherManager;

    @SuppressWarnings("unchecked")
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("java.io.tmpdir", root.getParent());

        searchTestPath = createDirectory(testRootPath, SEARCH_FOLDER_PATH);

        file1 = createFile(searchTestPath, "SearcherTest_File01.xml", "to be or not to be".getBytes());
        writeProperties(file1, Collections
                .singletonMap("vfs:mimeType", new String[]{"text/xml"})); // text/xml just for test, it is not xml content

        file2 = createFile(searchTestPath, "SearcherTest_File02.txt", "to be or not to be".getBytes());
        writeProperties(file2, Collections.singletonMap("vfs:mimeType", new String[]{MediaType.TEXT_PLAIN}));

        String folder1 = createDirectory(searchTestPath, "folder01");
        file3 = createFile(folder1, "SearcherTest_File03.txt", "to be or not to be".getBytes());
        writeProperties(file3, Collections.singletonMap("vfs:mimeType", new String[]{MediaType.TEXT_PLAIN}));

        file4 = createFile(searchTestPath, FILE_NAME, "maybe you should think twice".getBytes());

        queryToResult = new Pair[10];
        // text
        queryToResult[0] = new Pair<>(new String[]{file1, file2, file3}, "text=to%20be%20or%20not%20to%20be");
        queryToResult[1] = new Pair<>(new String[]{file1, file2, file3}, "text=to%20be%20or");
        // text + media type
        queryToResult[2] = new Pair<>(new String[]{file2, file3}, "text=to%20be%20or&mediaType=text/plain");
        queryToResult[3] = new Pair<>(new String[]{file1}, "text=to%20be%20or&mediaType=text/xml");
        // text + name
        queryToResult[4] = new Pair<>(new String[]{file2}, "text=to%20be%20or&name=*File02.txt");
        queryToResult[5] = new Pair<>(new String[]{file1, file2, file3}, "text=to%20be%20or&name=SearcherTest*");
        // text + path
        queryToResult[6] = new Pair<>(new String[]{file3}, "text=to%20be%20or&path=" + folder1);
        queryToResult[7] = new Pair<>(new String[]{file1, file2, file3}, "text=to%20be%20or&path=" + searchTestPath);
        // name + media type
        queryToResult[8] = new Pair<>(new String[]{file2, file3}, "name=SearcherTest*&mediaType=text/plain");
        queryToResult[9] = new Pair<>(new String[]{file1}, "name=SearcherTest*&mediaType=text/xml");

        CleanableSearcherProvider searcherProvider = new CleanableSearcherProvider(root.getParentFile(), Collections.<VirtualFileFilter>emptySet());
        // Re-register virtual file system with searching enabled.
        // remove old one first
        provider.close();
        assertFalse(provider.isMounted());
        virtualFileSystemRegistry.unregisterProvider(MY_WORKSPACE_ID);
        // create new one
        provider = new LocalFileSystemProvider(MY_WORKSPACE_ID, new WorkspaceHashLocalFSMountStrategy(root, root), new EventService(),
                                               searcherProvider, SystemPathsFilter.ANY, virtualFileSystemRegistry);
        provider.mount(testFsIoRoot);
        mountPoint = provider.getMountPoint(true);
        virtualFileSystemRegistry.registerProvider(MY_WORKSPACE_ID, provider);

        // Touch Searcher to initialize it.
        searcher = (CleanableSearcher)searcherProvider.getSearcher(mountPoint, true);
        searcherManager = new SearcherManager(searcher.getIndexWriter(), true, new SearcherFactory());

        Throwable error;
        while ((error = searcher.initializationError()) == null && !searcher.initialized()) {
            Thread.sleep(100);
        }
        if (error != null) {
            fail(error.getMessage());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testSearch() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "search";
        Map<String, List<String>> h = new HashMap<>(1);
        h.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_FORM_URLENCODED));
        for (Pair<String[], String> pair : queryToResult) {
            ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, h, pair.second.getBytes(), writer, null);
            //log.info(new String(writer.getBody()));
            assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
            List<Item> result = ((ItemList)response.getEntity()).getItems();
            assertEquals(String.format(
                    "Expected %d but found %d for query %s", pair.first.length, result.size(), pair.second),
                         pair.first.length,
                         result.size());
            List<String> resultPaths = new ArrayList<>(result.size());
            for (Item item : result) {
                resultPaths.add(item.getPath());
            }
            List<String> copy = new ArrayList<>(resultPaths);
            copy.removeAll(Arrays.asList(pair.first));
            assertTrue(String.format("Expected result is %s but found %s", Arrays.toString(pair.first), resultPaths), copy.isEmpty());
            writer.reset();
        }
    }

    public void testDeleteFile() throws Exception {
        searcherManager.maybeRefresh();
        IndexSearcher luceneSearcher = searcherManager.acquire();
        TopDocs topDocs = luceneSearcher.search(new TermQuery(new Term("path", file1)), 10);
        assertEquals(1, topDocs.totalHits);
        searcherManager.release(luceneSearcher);

        mountPoint.getVirtualFile(file1).delete(null);
        searcherManager.maybeRefresh();
        luceneSearcher = searcherManager.acquire();
        topDocs = luceneSearcher.search(new TermQuery(new Term("path", file1)), 10);
        assertEquals(0, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
    }

    public void testDeleteFolder() throws Exception {
        searcherManager.maybeRefresh();
        IndexSearcher luceneSearcher = searcherManager.acquire();
        TopDocs topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", searchTestPath)), 10);
        assertEquals(4, topDocs.totalHits);
        searcherManager.release(luceneSearcher);

        mountPoint.getVirtualFile(searchTestPath).delete(null);
        searcherManager.maybeRefresh();
        luceneSearcher = searcherManager.acquire();
        topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", searchTestPath)), 10);
        assertEquals(0, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
    }

    public void testAdd() throws Exception {
        searcherManager.maybeRefresh();
        IndexSearcher luceneSearcher = searcherManager.acquire();
        TopDocs topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", searchTestPath)), 10);
        assertEquals(4, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
        mountPoint.getVirtualFile(searchTestPath).createFile("new_file.txt", new ByteArrayInputStream(DEFAULT_CONTENT_BYTES));

        searcherManager.maybeRefresh();
        luceneSearcher = searcherManager.acquire();
        topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", searchTestPath)), 10);
        assertEquals(5, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
    }

    public void testUpdate() throws Exception {
        searcherManager.maybeRefresh();
        IndexSearcher luceneSearcher = searcherManager.acquire();
        TopDocs topDocs = luceneSearcher.search(new QueryParser("text", new SimpleAnalyzer()).parse("updated"), 10);
        assertEquals(0, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
        mountPoint.getVirtualFile(file2).updateContent(new ByteArrayInputStream("updated content".getBytes()), null);

        searcherManager.maybeRefresh();
        luceneSearcher = searcherManager.acquire();
        topDocs = luceneSearcher.search(new QueryParser("text", new SimpleAnalyzer()).parse("updated"), 10);
        assertEquals(1, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
    }

    public void testMove() throws Exception {
        searcherManager.maybeRefresh();
        IndexSearcher luceneSearcher = searcherManager.acquire();
        String destination = createDirectory(testRootPath, "___destination");
        String expected = destination + '/' + "SearcherTest_File03";
        TopDocs topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", expected)), 10);
        assertEquals(0, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
        mountPoint.getVirtualFile(file3).moveTo(mountPoint.getVirtualFile(destination), null);

        searcherManager.maybeRefresh();
        luceneSearcher = searcherManager.acquire();
        topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", expected)), 10);
        assertEquals(1, topDocs.totalHits);
        topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", file3)), 10);
        assertEquals(0, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
    }

    public void testCopy() throws Exception {
        searcherManager.maybeRefresh();
        IndexSearcher luceneSearcher = searcherManager.acquire();
        String destination = createDirectory(testRootPath, "___destination");
        String expected = destination + '/' + "SearcherTest_File03";
        TopDocs topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", expected)), 10);
        assertEquals(0, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
        mountPoint.getVirtualFile(file3).copyTo(mountPoint.getVirtualFile(destination));

        searcherManager.maybeRefresh();
        luceneSearcher = searcherManager.acquire();
        topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", expected)), 10);
        assertEquals(1, topDocs.totalHits);
        topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", file3)), 10);
        assertEquals(1, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
    }

    public void testRename() throws Exception {
        String newName = "___renamed";
        searcherManager.maybeRefresh();
        IndexSearcher luceneSearcher = searcherManager.acquire();
        TopDocs topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", file2)), 10);
        assertEquals(1, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
        mountPoint.getVirtualFile(file2).rename(newName, null, null);

        searcherManager.maybeRefresh();
        luceneSearcher = searcherManager.acquire();
        topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", searchTestPath + '/' + newName)), 10);
        assertEquals(1, topDocs.totalHits);
        topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", file2)), 10);
        assertEquals(0, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
    }

    public void testRenameFileByAddingFewNewSymbol() throws Exception {
        String newName = FILE_NAME + "A";
        String newPath =searchTestPath + '/' + newName;

        searcherManager.maybeRefresh();
        IndexSearcher luceneSearcher = searcherManager.acquire();
        TopDocs topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", file4)), 10);
        assertEquals(1, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
        mountPoint.getVirtualFile(file4).rename(newName, null, null);

        searcherManager.maybeRefresh();
        luceneSearcher = searcherManager.acquire();
        topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", newPath)), 10);
        assertEquals(1, topDocs.totalHits);
        topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", file4)), 10);
        assertEquals(1, topDocs.totalHits);

        String searchResult = luceneSearcher.doc(topDocs.scoreDocs[0].doc).getField("path").stringValue();

        assertEquals(searchResult, newPath);

        assertEquals(1, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
    }

    public void testRenameFolder() throws Exception {
        String newName = "___renamed";
        searcherManager.maybeRefresh();
        IndexSearcher luceneSearcher = searcherManager.acquire();
        TopDocs topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", searchTestPath)), 10);
        assertEquals(4, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
        mountPoint.getVirtualFile(searchTestPath).rename(newName, null, null);

        searcherManager.maybeRefresh();
        luceneSearcher = searcherManager.acquire();

        String newPath = testRootPath + "/" + newName;

        topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", newPath)), 10);
        assertEquals(4, topDocs.totalHits);
        topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", searchTestPath)), 10);
        assertEquals(0, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
    }

    public void testRenameFolderByAddingFewNewSymbol() throws Exception {
        String newName = SEARCH_FOLDER_PATH + "A";
        String newPath = searchTestPath + "A";
        searcherManager.maybeRefresh();
        IndexSearcher luceneSearcher = searcherManager.acquire();
        TopDocs topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", searchTestPath)), 10);
        assertEquals(4, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
        mountPoint.getVirtualFile(searchTestPath).rename(newName, null, null);

        searcherManager.maybeRefresh();
        luceneSearcher = searcherManager.acquire();

        topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", newPath)), 10);
        assertEquals(4, topDocs.totalHits);
        topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", searchTestPath)), 10);
        assertEquals(4, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
    }
}
