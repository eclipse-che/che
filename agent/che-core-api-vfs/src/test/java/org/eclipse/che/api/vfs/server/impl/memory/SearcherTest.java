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
package org.eclipse.che.api.vfs.server.impl.memory;

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.search.LuceneSearcher;
import org.eclipse.che.api.vfs.shared.dto.Item;
import org.eclipse.che.api.vfs.shared.dto.ItemList;
import org.eclipse.che.commons.lang.Pair;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author andrew00x
 */
public class SearcherTest extends MemoryFileSystemTest {
    private Pair<String[], String>[] queryToResult;
    private VirtualFile              searchTestFolder;
    private String                   searchTestPath;
    private String                   file1;
    private String                   file2;
    private String                   file3;

    private LuceneSearcher  searcher;
    private SearcherManager searcherManager;

    @SuppressWarnings("unchecked")
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        searchTestFolder = mountPoint.getRoot().createFolder("SearcherTest");
        searcher = (LuceneSearcher)mountPoint.getSearcherProvider().getSearcher(mountPoint, true);
        searcherManager = new SearcherManager(searcher.getIndexWriter(), true, new SearcherFactory());

        VirtualFile searchTestFolder = this.searchTestFolder.createFolder("SearcherTest_Folder");
        searchTestPath = searchTestFolder.getPath();

        file1 = searchTestFolder.createFile("SearcherTest_File01.xml", new ByteArrayInputStream("to be or not to be".getBytes()))
                                .getPath();

        file2 = searchTestFolder.createFile("SearcherTest_File02.txt", new ByteArrayInputStream("to be or not to be".getBytes()))
                                .getPath();

        VirtualFile folder = searchTestFolder.createFolder("folder01");
        String folder1 = folder.getPath();
        file3 = folder.createFile("SearcherTest_File03.txt", new ByteArrayInputStream("to be or not to be".getBytes())).getPath();
        
        String file4 = searchTestFolder.createFile("SearcherTest_File04.txt", new ByteArrayInputStream("(1+1):2=1 is right".getBytes())).getPath();
        String file5 = searchTestFolder.createFile("SearcherTest_File05.txt", new ByteArrayInputStream("Copyright (c) 2012-2015 * All rights reserved".getBytes())).getPath();

        queryToResult = new Pair[16];
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
        queryToResult[8] = new Pair<>(new String[]{file2, file3, file4, file5}, "name=SearcherTest*&mediaType=text/plain");
        queryToResult[9] = new Pair<>(new String[]{file1}, "name=SearcherTest*&mediaType=text/xml");
        // text is a "contains" query
        queryToResult[10] = new Pair<>(new String[]{file4, file5}, "text=/.*right.*/");
        queryToResult[11] = new Pair<>(new String[]{file5}, "text=/.*rights.*/");
        // text is a regular expression
        queryToResult[12] = new Pair<>(new String[]{file4, file5}, "text=/.*\\(.*\\).*/");
        queryToResult[13] = new Pair<>(new String[]{file5}, "text=/.*\\([a-z]\\).*/");
        // text contains special characters
        queryToResult[14] = new Pair<>(new String[]{file4}, "text=\\(1\\%2B1\\)\\:2=1");
        queryToResult[15] = new Pair<>(new String[]{file5}, "text=\\(c\\)%202012\\-2015%20\\*");
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

    public void testDelete() throws Exception {
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

    public void testDelete2() throws Exception {
        searcherManager.maybeRefresh();
        IndexSearcher luceneSearcher = searcherManager.acquire();
        TopDocs topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", searchTestPath)), 10);
        assertEquals(5, topDocs.totalHits);
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
        assertEquals(5, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
        mountPoint.getVirtualFile(searchTestPath).createFile("new_file", new ByteArrayInputStream(DEFAULT_CONTENT_BYTES));

        searcherManager.maybeRefresh();
        luceneSearcher = searcherManager.acquire();
        topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", searchTestPath)), 10);
        assertEquals(6, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
    }

    public void testUpdate() throws Exception {
        searcherManager.maybeRefresh();
        IndexSearcher luceneSearcher = searcherManager.acquire();
        TopDocs topDocs = luceneSearcher.search(
                new QueryParser("text", new SimpleAnalyzer()).parse("updated"), 10);
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
        String destination = searchTestFolder.createFolder("___destination").getPath();
        String expected = destination + '/' + "SearcherTest_File03";
        TopDocs topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", expected)), 10);
        assertEquals(0, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
        mountPoint.getVirtualFile(file3).moveTo(mountPoint.getVirtualFile(destination), null, false, null);

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
        String destination = searchTestFolder.createFolder("___destination").getPath();
        String expected = destination + '/' + "SearcherTest_File03";
        TopDocs topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", expected)), 10);
        assertEquals(0, topDocs.totalHits);
        searcherManager.release(luceneSearcher);
        mountPoint.getVirtualFile(file3).copyTo(mountPoint.getVirtualFile(destination), null, false);

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
        TopDocs topDocs = luceneSearcher.search(new PrefixQuery(new Term("path", file3)), 10);
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
}
