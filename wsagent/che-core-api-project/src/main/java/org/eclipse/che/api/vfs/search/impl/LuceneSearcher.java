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
package org.eclipse.che.api.vfs.search.impl;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.IOUtils;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileFilter;
import org.eclipse.che.api.vfs.VirtualFileFilters;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.search.MediaTypeFilter;
import org.eclipse.che.api.vfs.search.QueryExpression;
import org.eclipse.che.api.vfs.search.SearchResult;
import org.eclipse.che.api.vfs.search.SearchResultEntry;
import org.eclipse.che.api.vfs.search.Searcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Lucene based searcher.
 *
 * @author andrew00x
 */
public abstract class LuceneSearcher implements Searcher {
    private static final Logger LOG = LoggerFactory.getLogger(LuceneSearcher.class);

    private static final int RESULT_LIMIT = 1000;

    private final List<VirtualFileFilter>                      excludeFileIndexFilters;
    private final AbstractLuceneSearcherProvider.CloseCallback closeCallback;

    private IndexWriter     luceneIndexWriter;
    private SearcherManager searcherManager;

    private boolean closed = true;

    protected LuceneSearcher() {
        this(new MediaTypeFilter(), null);
    }

    protected LuceneSearcher(AbstractLuceneSearcherProvider.CloseCallback closeCallback) {
        this(new MediaTypeFilter(), closeCallback);
    }

    /**
     * @param excludeFileIndexFilter
     *         common filter for files that should not be indexed. If complex excluding rules needed then few filters might be combined
     *         with {@link VirtualFileFilters#createAndFilter} or {@link VirtualFileFilters#createOrFilter} methods
     */
    protected LuceneSearcher(VirtualFileFilter excludeFileIndexFilter, AbstractLuceneSearcherProvider.CloseCallback closeCallback) {
        this.closeCallback = closeCallback;
        excludeFileIndexFilters = new CopyOnWriteArrayList<>();
        excludeFileIndexFilters.add(excludeFileIndexFilter);
    }

    @Override
    public boolean addIndexFilter(VirtualFileFilter indexFilter) {
        return excludeFileIndexFilters.add(indexFilter);
    }

    @Override
    public boolean removeIndexFilter(VirtualFileFilter indexFilter) {
        return excludeFileIndexFilters.remove(indexFilter);
    }

    protected Analyzer makeAnalyzer() {
        return new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                Tokenizer tokenizer = new WhitespaceTokenizer();
                TokenStream filter = new LowerCaseFilter(tokenizer);
                return new TokenStreamComponents(tokenizer, filter);
            }
        };
    }

    protected abstract Directory makeDirectory() throws ServerException;

    /**
     * Init lucene index. Need call this method if index directory is clean. Scan all files in virtual filesystem and add to index.
     *
     * @param virtualFileSystem
     *         VirtualFileSystem
     * @throws ServerException
     *         if any virtual filesystem error occurs
     */
    public void init(VirtualFileSystem virtualFileSystem) throws ServerException {
        doInit();
        addTree(virtualFileSystem.getRoot());
    }

    public void initAsynchronously(ExecutorService executor, VirtualFileSystem virtualFileSystem) throws ServerException {
        doInit();
        if (!executor.isShutdown()) {
            executor.execute(() -> {
                try {
                    LuceneSearcher.this.addTree(virtualFileSystem.getRoot());
                } catch (ServerException e) {
                    LOG.error(e.getMessage());
                }
            });
        }
    }

    protected final synchronized void doInit() throws ServerException {
        try {
            luceneIndexWriter = new IndexWriter(makeDirectory(), new IndexWriterConfig(makeAnalyzer()));
            searcherManager = new SearcherManager(luceneIndexWriter, true, new SearcherFactory());
            closed = false;
        } catch (IOException e) {
            throw new ServerException(e);
        }
    }

    public final synchronized void close() {
        if (!closed) {
            try {
                IOUtils.close(getIndexWriter(), getIndexWriter().getDirectory(), searcherManager);
                afterClose();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
            closed = true;
        }
    }

    protected void afterClose() throws IOException {
        if (closeCallback != null) {
            closeCallback.onClose();
        }
    }

    @Override
    public synchronized boolean isClosed() {
        return closed;
    }

    public synchronized IndexWriter getIndexWriter() {
        return luceneIndexWriter;
    }

    @Override
    public SearchResult search(QueryExpression query) throws ServerException {
        IndexSearcher luceneSearcher = null;
        try {
            final long startTime = System.currentTimeMillis();
            searcherManager.maybeRefresh();
            luceneSearcher = searcherManager.acquire();

            Query luceneQuery = createLuceneQuery(query);

            ScoreDoc after = null;
            final int numSkipDocs = Math.max(0, query.getSkipCount());
            if (numSkipDocs > 0) {
                after = skipScoreDocs(luceneSearcher, luceneQuery, numSkipDocs);
            }

            final int numDocs = query.getMaxItems() > 0 ? Math.min(query.getMaxItems(), RESULT_LIMIT) : RESULT_LIMIT;
            TopDocs topDocs = luceneSearcher.searchAfter(after, luceneQuery, numDocs);
            final int totalHitsNum = topDocs.totalHits;

            List<SearchResultEntry> results = newArrayList();
            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                ScoreDoc scoreDoc = topDocs.scoreDocs[i];
                String filePath = luceneSearcher.doc(scoreDoc.doc).getField("path").stringValue();
                results.add(new SearchResultEntry(filePath));
            }

            final long elapsedTimeMillis = System.currentTimeMillis() - startTime;

            boolean hasMoreToRetrieve = numSkipDocs + topDocs.scoreDocs.length + 1 < totalHitsNum;
            QueryExpression nextPageQueryExpression = null;
            if (hasMoreToRetrieve) {
                nextPageQueryExpression = createNextPageQuery(query, numSkipDocs + topDocs.scoreDocs.length);
            }

            return SearchResult.aSearchResult()
                               .withResults(results)
                               .withTotalHits(totalHitsNum)
                               .withNextPageQueryExpression(nextPageQueryExpression)
                               .withElapsedTimeMillis(elapsedTimeMillis)
                               .build();
        } catch (IOException e) {
            throw new ServerException(e.getMessage(), e);
        } finally {
            try {
                searcherManager.release(luceneSearcher);
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }
    }

    private Query createLuceneQuery(QueryExpression query) throws ServerException {
        final BooleanQuery luceneQuery = new BooleanQuery();
        final String name = query.getName();
        final String path = query.getPath();
        final String text = query.getText();
        if (path != null) {
            luceneQuery.add(new PrefixQuery(new Term("path", path)), BooleanClause.Occur.MUST);
        }
        if (name != null) {
            luceneQuery.add(new WildcardQuery(new Term("name", name)), BooleanClause.Occur.MUST);
        }
        if (text != null) {
            QueryParser qParser = new QueryParser("text", makeAnalyzer());
            qParser.setAllowLeadingWildcard(true);
            try {
                luceneQuery.add(qParser.parse(text), BooleanClause.Occur.MUST);
            } catch (ParseException e) {
                throw new ServerException(e.getMessage());
            }
        }
        return luceneQuery;
    }

    private ScoreDoc skipScoreDocs(IndexSearcher luceneSearcher, Query luceneQuery, int numSkipDocs) throws IOException {
        final int readFrameSize = Math.min(numSkipDocs, RESULT_LIMIT);
        ScoreDoc scoreDoc = null;
        int retrievedDocs = 0;
        TopDocs topDocs;
        do {
            topDocs = luceneSearcher.searchAfter(scoreDoc, luceneQuery, readFrameSize);
            if (topDocs.scoreDocs.length > 0) {
                scoreDoc = topDocs.scoreDocs[topDocs.scoreDocs.length - 1];
            }
            retrievedDocs += topDocs.scoreDocs.length;
        } while (retrievedDocs < numSkipDocs && topDocs.scoreDocs.length > 0);

        if (retrievedDocs > numSkipDocs) {
            int lastScoreDocIndex = topDocs.scoreDocs.length - (retrievedDocs - numSkipDocs);
            scoreDoc = topDocs.scoreDocs[lastScoreDocIndex];
        }

        return scoreDoc;
    }

    private QueryExpression createNextPageQuery(QueryExpression originalQuery, int newSkipCount) {
        return new QueryExpression().setText(originalQuery.getText())
                                    .setName(originalQuery.getName())
                                    .setPath(originalQuery.getPath())
                                    .setSkipCount(newSkipCount)
                                    .setMaxItems(originalQuery.getMaxItems());
    }

    @Override
    public final void add(VirtualFile virtualFile) throws ServerException {
        doAdd(virtualFile);
    }

    protected void doAdd(VirtualFile virtualFile) throws ServerException {
        if (virtualFile.isFolder()) {
            addTree(virtualFile);
        } else {
            addFile(virtualFile);
        }
    }

    protected void addTree(VirtualFile tree) throws ServerException {
        final long start = System.currentTimeMillis();
        final LinkedList<VirtualFile> q = new LinkedList<>();
        q.add(tree);
        int indexedFiles = 0;
        while (!q.isEmpty()) {
            final VirtualFile folder = q.pop();
            if (folder.exists()) {
                for (VirtualFile child : folder.getChildren()) {
                    if (child.isFolder()) {
                        q.push(child);
                    } else {
                        addFile(child);
                        indexedFiles++;
                    }
                }
            }
        }
        final long end = System.currentTimeMillis();
        LOG.debug("Indexed {} files from {}, time: {} ms", indexedFiles, tree.getPath(), (end - start));
    }

    protected void addFile(VirtualFile virtualFile) throws ServerException {
        if (virtualFile.exists()) {
            try (Reader fContentReader = shouldIndexContent(virtualFile)
                                         ? new BufferedReader(new InputStreamReader(virtualFile.getContent()))
                                         : null) {
                getIndexWriter()
                        .updateDocument(new Term("path", virtualFile.getPath().toString()), createDocument(virtualFile, fContentReader));
            } catch (OutOfMemoryError oome) {
                close();
                throw oome;
            } catch (IOException e) {
                throw new ServerException(e.getMessage(), e);
            } catch (ForbiddenException e) {
                throw new ServerException(e.getServiceError());
            }
        }
    }

    @Override
    public final void delete(String path, boolean isFile) throws ServerException {
        try {
            if (isFile) {
                Term term = new Term("path", path);
                getIndexWriter().deleteDocuments(term);
            } else {
                Term term = new Term("path", path + "/");
                getIndexWriter().deleteDocuments(new PrefixQuery(term));
            }
        } catch (OutOfMemoryError oome) {
            close();
            throw oome;
        } catch (IOException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    public final void update(VirtualFile virtualFile) throws ServerException {
        doUpdate(new Term("path", virtualFile.getPath().toString()), virtualFile);
    }

    protected void doUpdate(Term deleteTerm, VirtualFile virtualFile) throws ServerException {
        try (Reader fContentReader = shouldIndexContent(virtualFile)
                                     ? new BufferedReader(new InputStreamReader(virtualFile.getContent()))
                                     : null) {
            getIndexWriter().updateDocument(deleteTerm, createDocument(virtualFile, fContentReader));
        } catch (OutOfMemoryError oome) {
            close();
            throw oome;
        } catch (IOException e) {
            throw new ServerException(e.getMessage(), e);
        } catch (ForbiddenException e) {
            throw new ServerException(e.getServiceError());
        }
    }

    protected Document createDocument(VirtualFile virtualFile, Reader reader) throws ServerException {
        final Document doc = new Document();
        doc.add(new StringField("path", virtualFile.getPath().toString(), Field.Store.YES));
        doc.add(new StringField("name", virtualFile.getName(), Field.Store.YES));
        if (reader != null) {
            doc.add(new TextField("text", reader));
        }
        return doc;
    }

    private boolean shouldIndexContent(VirtualFile virtualFile) {
        for (VirtualFileFilter indexFilter : excludeFileIndexFilters) {
            if (indexFilter.accept(virtualFile)) {
                return false;
            }
        }
        return true;
    }
}
