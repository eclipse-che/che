/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.search.server.impl;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.che.api.fs.server.WsPathUtils.nameOf;
import static org.eclipse.che.commons.lang.IoUtil.deleteRecursive;

import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.*;
import java.util.concurrent.ExecutorService;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
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
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.apache.lucene.util.IOUtils;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.search.server.SearchResult;
import org.eclipse.che.api.search.server.Searcher;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lucene based searcher.
 *
 * @author andrew00x
 */
@Singleton
public class LuceneSearcher implements Searcher {

  private static final Logger LOG = LoggerFactory.getLogger(LuceneSearcher.class);

  private static final int RESULT_LIMIT = 1000;
  private static final String PATH_FIELD = "path";
  private static final String NAME_FIELD = "name";
  private static final String TEXT_FIELD = "text";

  private final Set<PathMatcher> excludePatterns;
  private final ExecutorService executor;
  private final File indexDirectory;
  private final PathTransformer pathTransformer;

  private File root;
  private IndexWriter luceneIndexWriter;
  private SearcherManager searcherManager;

  private boolean closed = true;

  @Inject
  protected LuceneSearcher(
      @Named("vfs.index_filter_matcher") Set<PathMatcher> excludePatterns,
      @Named("vfs.local.fs_index_root_dir") File indexDirectory,
      @Named("che.user.workspaces.storage") File root,
      PathTransformer pathTransformer) {
    this.indexDirectory = indexDirectory;
    this.root = root;
    this.excludePatterns = excludePatterns;
    this.pathTransformer = pathTransformer;

    executor =
        newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                .setDaemon(true)
                .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                .setNameFormat("LuceneSearcherInitThread")
                .build());
  }

  @PostConstruct
  private void initialize() throws ServerException {
    doInitialize();
    if (!executor.isShutdown()) {
      executor.execute(
          () -> {
            try {
              addDirectory(root.toPath());
            } catch (ServerException e) {
              LOG.error(e.getMessage());
            }
          });
    }
  }

  @PreDestroy
  private void terminate() {
    doTerminate();
    executor.shutdown();
    try {
      if (!executor.awaitTermination(5, SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException ie) {
      executor.shutdownNow();
    }
  }

  private Analyzer makeAnalyzer() throws IOException {
    return CustomAnalyzer.builder()
        .withTokenizer(WhitespaceTokenizerFactory.class)
        .addTokenFilter(LowerCaseFilterFactory.class)
        .build();
  }

  private Directory makeDirectory() throws ServerException {
    try {
      Files.createDirectories(indexDirectory.toPath());
      return FSDirectory.open(indexDirectory.toPath(), new SingleInstanceLockFactory());
    } catch (IOException e) {
      throw new ServerException(e);
    }
  }

  private void doInitialize() throws ServerException {
    try {
      luceneIndexWriter = new IndexWriter(makeDirectory(), new IndexWriterConfig(makeAnalyzer()));
      searcherManager = new SearcherManager(luceneIndexWriter, true, true, new SearcherFactory());
      closed = false;
    } catch (IOException e) {
      throw new ServerException(e);
    }
  }

  private void doTerminate() {
    if (!closed) {
      try {
        IOUtils.close(luceneIndexWriter, luceneIndexWriter.getDirectory(), searcherManager);
        if (!deleteRecursive(indexDirectory)) {
          LOG.warn("Unable delete index directory '{}', add it in FileCleaner", indexDirectory);
          FileCleaner.addFile(indexDirectory);
        }
      } catch (IOException e) {
        LOG.error(e.getMessage(), e);
      }
      closed = true;
    }
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

      final int numDocs =
          query.getMaxItems() > 0 ? Math.min(query.getMaxItems(), RESULT_LIMIT) : RESULT_LIMIT;
      TopDocs topDocs = luceneSearcher.searchAfter(after, luceneQuery, numDocs);
      final long totalHitsNum = topDocs.totalHits;

      List<SearchResultEntry> results = newArrayList();
      List<OffsetData> offsetData = Collections.emptyList();
      for (int i = 0; i < topDocs.scoreDocs.length; i++) {
        ScoreDoc scoreDoc = topDocs.scoreDocs[i];
        int docId = scoreDoc.doc;
        Document doc = luceneSearcher.doc(docId);
        if (query.isIncludePositions()) {
          offsetData = new ArrayList<>();
          String txt = doc.get(TEXT_FIELD);
          if (txt != null) {
            IndexReader reader = luceneSearcher.getIndexReader();

            TokenStream tokenStream =
                TokenSources.getTokenStream(
                    TEXT_FIELD,
                    reader.getTermVectors(docId),
                    txt,
                    luceneIndexWriter.getAnalyzer(),
                    -1);

            CharTermAttribute termAtt = tokenStream.addAttribute(CharTermAttribute.class);
            OffsetAttribute offsetAtt = tokenStream.addAttribute(OffsetAttribute.class);

            QueryScorer queryScorer = new QueryScorer(luceneQuery);
            // TODO think about this constant
            queryScorer.setMaxDocCharsToAnalyze(1_000_000);
            TokenStream newStream = queryScorer.init(tokenStream);
            if (newStream != null) {
              tokenStream = newStream;
            }
            queryScorer.startFragment(null);

            tokenStream.reset();

            int startOffset, endOffset;
            // TODO think about this constant
            for (boolean next = tokenStream.incrementToken();
                next && (offsetAtt.startOffset() < 1_000_000);
                next = tokenStream.incrementToken()) {
              startOffset = offsetAtt.startOffset();
              endOffset = offsetAtt.endOffset();

              if ((endOffset > txt.length()) || (startOffset > txt.length())) {
                throw new ServerException(
                    "Token "
                        + termAtt.toString()
                        + " exceeds length of provided text size "
                        + txt.length());
              }

              float res = queryScorer.getTokenScore();
              if (res > 0.0F && startOffset <= endOffset) {
                try {
                  IDocument document = new org.eclipse.jface.text.Document(txt);
                  int lineNum = document.getLineOfOffset(startOffset);
                  IRegion lineInfo = document.getLineInformation(lineNum);
                  String foundLine = document.get(lineInfo.getOffset(), lineInfo.getLength());
                  String tokenText = document.get(startOffset, endOffset - startOffset);

                  offsetData.add(
                      new OffsetData(
                          tokenText, startOffset, endOffset, docId, res, lineNum, foundLine));
                } catch (BadLocationException e) {
                  LOG.error(e.getLocalizedMessage(), e);
                  throw new ServerException("Can not provide data for token " + termAtt.toString());
                }
              }
            }
          }
        }
        String filePath = doc.getField(PATH_FIELD).stringValue();
        results.add(new SearchResultEntry(filePath, offsetData));
      }

      final long elapsedTimeMillis = System.currentTimeMillis() - startTime;

      boolean hasMoreToRetrieve = numSkipDocs + topDocs.scoreDocs.length + 1 < totalHitsNum;
      QueryExpression nextPageQueryExpression = null;
      if (hasMoreToRetrieve) {
        nextPageQueryExpression =
            createNextPageQuery(query, numSkipDocs + topDocs.scoreDocs.length);
      }

      return SearchResult.aSearchResult()
          .withResults(results)
          .withTotalHits(totalHitsNum)
          .withNextPageQueryExpression(nextPageQueryExpression)
          .withElapsedTimeMillis(elapsedTimeMillis)
          .build();
    } catch (IOException | ParseException e) {
      throw new ServerException(e.getMessage(), e);
    } finally {
      try {
        searcherManager.release(luceneSearcher);
      } catch (IOException e) {
        LOG.error(e.getMessage());
      }
    }
  }

  private Query createLuceneQuery(QueryExpression query) throws ParseException, IOException {
    BooleanQuery.Builder luceneQueryBuilder = new BooleanQuery.Builder();
    final String name = query.getName();
    final String path = query.getPath();
    final String text = query.getText();
    if (path != null) {
      luceneQueryBuilder.add(new PrefixQuery(new Term(PATH_FIELD, path)), BooleanClause.Occur.MUST);
    }
    if (name != null) {
      QueryParser qParser = new QueryParser(NAME_FIELD, makeAnalyzer());
      qParser.setAllowLeadingWildcard(true);
      luceneQueryBuilder.add(qParser.parse(name), BooleanClause.Occur.MUST);
    }
    if (text != null) {
      QueryParser qParser = new QueryParser(TEXT_FIELD, makeAnalyzer());
      qParser.setAllowLeadingWildcard(true);
      luceneQueryBuilder.add(qParser.parse(text), BooleanClause.Occur.MUST);
    }
    return luceneQueryBuilder.build();
  }

  private ScoreDoc skipScoreDocs(IndexSearcher luceneSearcher, Query luceneQuery, int numSkipDocs)
      throws IOException {
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
    return new QueryExpression()
        .setText(originalQuery.getText())
        .setName(originalQuery.getName())
        .setPath(originalQuery.getPath())
        .setSkipCount(newSkipCount)
        .setMaxItems(originalQuery.getMaxItems());
  }

  @Override
  public final void add(Path fsPath) throws ServerException, NotFoundException {
    if (fsPath.toFile().isDirectory()) {
      addDirectory(fsPath);
    } else {
      addFile(fsPath);
    }
  }

  private void addDirectory(Path fsPath) throws ServerException {
    long start = System.currentTimeMillis();
    LinkedList<File> queue = new LinkedList<>();
    queue.add(fsPath.toFile());
    int indexedFiles = 0;
    while (!queue.isEmpty()) {
      File folder = queue.pop();
      if (folder.exists() && folder.isDirectory()) {
        File[] files = folder.listFiles();
        if (files == null) {
          continue;
        }
        for (File child : files) {
          if (child.isDirectory()) {
            queue.push(child);
          } else {
            addFile(child.toPath());
            indexedFiles++;
          }
        }
      }
    }

    long end = System.currentTimeMillis();
    LOG.debug("Indexed {} files from {}, time: {} ms", indexedFiles, fsPath, (end - start));
  }

  private void addFile(Path fsPath) throws ServerException {
    if (!fsPath.toFile().exists()) {
      return;
    }

    if (!isNotExcluded(fsPath)) {
      return;
    }

    String wsPath = pathTransformer.transform(fsPath);

    try (Reader reader =
        new BufferedReader(new InputStreamReader(new FileInputStream(fsPath.toFile()), "utf-8"))) {
      luceneIndexWriter.updateDocument(
          new Term(PATH_FIELD, wsPath), createDocument(wsPath, reader));
    } catch (OutOfMemoryError oome) {
      doTerminate();
      throw oome;
    } catch (IOException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  @Override
  public final void delete(Path fsPath) throws ServerException {
    String wsPath = pathTransformer.transform(fsPath);
    try {
      if (fsPath.toFile().isFile()) {
        Term term = new Term(PATH_FIELD, wsPath);
        luceneIndexWriter.deleteDocuments(term);
      } else {
        Term term = new Term(PATH_FIELD, wsPath + '/');
        luceneIndexWriter.deleteDocuments(new PrefixQuery(term));
      }
    } catch (OutOfMemoryError oome) {
      doTerminate();
      throw oome;
    } catch (IOException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  @Override
  public final void update(Path fsPath) throws ServerException {
    String wsPath = pathTransformer.transform(fsPath);
    doUpdate(new Term(PATH_FIELD, wsPath), fsPath);
  }

  private void doUpdate(Term deleteTerm, Path fsPath) throws ServerException {
    if (!fsPath.toFile().exists()) {
      return;
    }

    if (!isNotExcluded(fsPath)) {
      return;
    }

    String wsPath = pathTransformer.transform(fsPath);

    try (Reader reader =
        new BufferedReader(new InputStreamReader(new FileInputStream(fsPath.toFile()), "utf-8"))) {
      luceneIndexWriter.updateDocument(deleteTerm, createDocument(wsPath, reader));
    } catch (OutOfMemoryError oome) {
      doTerminate();
      throw oome;
    } catch (IOException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  private Document createDocument(String wsPath, Reader reader) throws ServerException {
    String name = nameOf(wsPath);
    Document doc = new Document();
    doc.add(new StringField(PATH_FIELD, wsPath, Field.Store.YES));
    doc.add(new TextField(NAME_FIELD, name, Field.Store.YES));
    try {
      doc.add(new TextField(TEXT_FIELD, CharStreams.toString(reader), Field.Store.YES));
    } catch (MalformedInputException e) {
      LOG.warn("Can't index file: {}", wsPath);
    } catch (IOException e) {
      LOG.error("Can't index file: {}", wsPath);
      throw new ServerException(e.getLocalizedMessage(), e);
    }
    return doc;
  }

  private boolean isNotExcluded(Path fsPath) {
    for (PathMatcher matcher : excludePatterns) {
      if (matcher.matches(fsPath)) {
        return false;
      }
    }
    return true;
  }

  public static class OffsetData {

    public String phrase;
    public int startOffset;
    public int endOffset;
    public int docId;
    public float score;
    public int lineNum;
    public String line;

    public OffsetData(
        String phrase,
        int startOffset,
        int endOffset,
        int docId,
        float score,
        int lineNum,
        String line) {
      this.phrase = phrase;
      this.startOffset = startOffset;
      this.endOffset = endOffset;
      this.docId = docId;
      this.score = score;
      this.lineNum = lineNum;
      this.line = line;
    }
  }
}
