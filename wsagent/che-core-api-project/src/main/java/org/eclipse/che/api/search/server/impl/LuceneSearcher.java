/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.search.server.impl;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.che.api.fs.server.WsPathUtils.nameOf;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.CharStreams;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.MalformedInputException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import javax.annotation.PostConstruct;
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
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.apache.lucene.util.BytesRef;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.eclipse.che.api.search.server.InvalidQueryException;
import org.eclipse.che.api.search.server.OffsetData;
import org.eclipse.che.api.search.server.QueryExecutionException;
import org.eclipse.che.api.search.server.QueryExpression;
import org.eclipse.che.api.search.server.SearchResult;
import org.eclipse.che.api.search.server.Searcher;
import org.eclipse.che.commons.schedule.ScheduleRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lucene based searcher.
 *
 * @author andrew00x
 * @author Sergii Kabashniuk
 */
@Singleton
public class LuceneSearcher implements Searcher {

  private static final Logger LOG = LoggerFactory.getLogger(LuceneSearcher.class);

  private static final int RESULT_LIMIT = 1000;
  private static final String PATH_FIELD = "path";
  private static final String NAME_FIELD = "name";
  private static final String TEXT_FIELD = "text";

  private final Set<PathMatcher> excludePatterns;
  private final PathTransformer pathTransformer;

  private final Path root;
  private final IndexWriter luceneIndexWriter;
  private final SearcherManager searcherManager;
  private final Analyzer analyzer;
  private final CountDownLatch initialIndexingLatch = new CountDownLatch(1);
  private final Sort sort;

  @Inject
  public LuceneSearcher(
      @Named("vfs.index_filter_matcher") Set<PathMatcher> excludePatterns,
      @Named("vfs.local.fs_index_root_dir") File indexDirectory,
      RootDirPathProvider pathProvider,
      PathTransformer pathTransformer)
      throws IOException {

    if (indexDirectory.exists()) {
      if (indexDirectory.isFile()) {
        throw new IOException("Wrong configuration `vfs.local.fs_index_root_dir` is a file");
      }
    } else {
      Files.createDirectories(indexDirectory.toPath());
    }

    this.root = Paths.get(pathProvider.get());
    this.excludePatterns = excludePatterns;
    this.pathTransformer = pathTransformer;
    this.analyzer =
        CustomAnalyzer.builder()
            .withTokenizer(WhitespaceTokenizerFactory.class)
            .addTokenFilter(LowerCaseFilterFactory.class)
            .build();
    this.luceneIndexWriter =
        new IndexWriter(
            FSDirectory.open(indexDirectory.toPath(), new SingleInstanceLockFactory()),
            new IndexWriterConfig(analyzer));
    this.searcherManager =
        new SearcherManager(luceneIndexWriter, true, true, new SearcherFactory());
    this.sort = new Sort(SortField.FIELD_SCORE, new SortField(PATH_FIELD, SortField.Type.STRING));
  }

  @PostConstruct
  @VisibleForTesting
  void initialize() {
    Thread initializer =
        new Thread(
            () -> {
              try {
                long start = System.currentTimeMillis();
                add(root);
                LOG.info(
                    "Initial indexing complete after {} msec ", System.currentTimeMillis() - start);
              } finally {
                initialIndexingLatch.countDown();
              }
            });
    initializer.setName("LuceneSearcherInitThread");
    initializer.setDaemon(true);
    initializer.start();
  }

  @VisibleForTesting
  CountDownLatch getInitialIndexingLatch() {
    return initialIndexingLatch;
  }

  @ScheduleRate(period = 30, initialDelay = 30)
  private void commitIndex() throws IOException {
    luceneIndexWriter.commit();
  }

  @Override
  public SearchResult search(QueryExpression query)
      throws InvalidQueryException, QueryExecutionException {
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
      TopDocs topDocs = luceneSearcher.searchAfter(after, luceneQuery, numDocs, sort, true, true);
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
                throw new QueryExecutionException(
                    "Token "
                        + termAtt.toString()
                        + " exceeds length of provided text size "
                        + txt.length());
              }

              float res = queryScorer.getTokenScore();
              if (res > 0.0F && startOffset <= endOffset) {
                String tokenText = txt.substring(startOffset, endOffset);
                Scanner sc = new Scanner(txt);
                int lineNum = 1;
                long len = 0;
                String foundLine = "";
                while (sc.hasNextLine()) {
                  foundLine = sc.nextLine();

                  len += foundLine.length();
                  if (len > startOffset) {
                    break;
                  }
                  lineNum++;
                }
                offsetData.add(
                    new OffsetData(tokenText, startOffset, endOffset, res, lineNum, foundLine));
              }
            }
          }
        }

        String filePath = doc.getField(PATH_FIELD).stringValue();
        LOG.debug("Doc {} path {} score {} ", docId, filePath, scoreDoc.score);
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
    } catch (ParseException e) {
      throw new InvalidQueryException(e.getMessage(), e);
    } catch (IOException e) {
      throw new QueryExecutionException(e.getMessage(), e);
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
      QueryParser qParser = new QueryParser(NAME_FIELD, analyzer);
      qParser.setAllowLeadingWildcard(true);
      luceneQueryBuilder.add(qParser.parse(name), BooleanClause.Occur.MUST);
    }
    if (text != null) {
      QueryParser qParser = new QueryParser(TEXT_FIELD, analyzer);
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
      topDocs = luceneSearcher.searchAfter(scoreDoc, luceneQuery, readFrameSize, sort, true, true);
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
  public final void add(Path fsPath) {

    try {
      if (fsPath.toFile().isDirectory()) {
        try {
          Files.walkFileTree(
              fsPath,
              new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                  addFile(file);
                  return FileVisitResult.CONTINUE;
                }
              });
        } catch (IOException ignore) {
          LOG.warn("Not able to index {} because {} ", fsPath.toString(), ignore.getMessage());
        }
      } else {
        addFile(fsPath);
      }
      printStatistic();
    } catch (IOException e) {
      LOG.warn(
          "Can't commit changes to index for: {} because {} ",
          fsPath.toAbsolutePath().toString(),
          e.getMessage());
    }
  }

  private void addFile(Path fsPath) {
    if (!fsPath.toFile().exists()) {
      return;
    }

    if (!isNotExcluded(fsPath)) {
      return;
    }
    String wsPath = pathTransformer.transform(fsPath);
    LOG.debug("Adding file {} ", wsPath);

    try (Reader reader =
        new BufferedReader(new InputStreamReader(new FileInputStream(fsPath.toFile()), "utf-8"))) {
      String name = nameOf(wsPath);
      Document doc = new Document();
      doc.add(new StringField(PATH_FIELD, wsPath, Field.Store.YES));
      doc.add(new SortedDocValuesField(PATH_FIELD, new BytesRef(wsPath)));
      doc.add(new TextField(NAME_FIELD, name, Field.Store.YES));
      try {
        doc.add(new TextField(TEXT_FIELD, CharStreams.toString(reader), Field.Store.YES));
      } catch (MalformedInputException e) {
        LOG.warn("Can't index file: {}", wsPath);
      }
      luceneIndexWriter.updateDocument(new Term(PATH_FIELD, wsPath), doc);

    } catch (IOException oome) {
      LOG.warn("Can't index file: {}", wsPath);
    }
  }

  @Override
  public final void delete(Path fsPath) {

    String wsPath = pathTransformer.transform(fsPath);
    try {

      // Since in most cases this is post action there is no way to find out is this a file
      // or directory. Lets try to delete both
      BooleanQuery.Builder deleteFileOrFolder = new BooleanQuery.Builder();
      deleteFileOrFolder.setMinimumNumberShouldMatch(1);
      deleteFileOrFolder.add(new TermQuery(new Term(PATH_FIELD, wsPath)), Occur.SHOULD);
      deleteFileOrFolder.add(new PrefixQuery(new Term(PATH_FIELD, wsPath + "/")), Occur.SHOULD);
      luceneIndexWriter.deleteDocuments(deleteFileOrFolder.build());
      printStatistic();
    } catch (IOException e) {
      LOG.warn("Can't delete index for file: {}", wsPath);
    }
  }

  private void printStatistic() throws IOException {
    if (LOG.isDebugEnabled()) {
      IndexSearcher luceneSearcher = null;
      try {
        searcherManager.maybeRefresh();
        luceneSearcher = searcherManager.acquire();
        IndexReader reader = luceneSearcher.getIndexReader();
        LOG.debug(
            "IndexReader numDocs={} numDeletedDocs={} maxDoc={} hasDeletions={}. Writer numDocs={} numRamDocs={} hasPendingMerges={}  hasUncommittedChanges={} hasDeletions={}",
            reader.numDocs(),
            reader.numDeletedDocs(),
            reader.maxDoc(),
            reader.hasDeletions(),
            luceneIndexWriter.numDocs(),
            luceneIndexWriter.numRamDocs(),
            luceneIndexWriter.hasPendingMerges(),
            luceneIndexWriter.hasUncommittedChanges(),
            luceneIndexWriter.hasDeletions());
      } finally {
        searcherManager.release(luceneSearcher);
      }
    }
  }

  @Override
  public final void update(Path fsPath) {
    addFile(fsPath);
  }

  private boolean isNotExcluded(Path fsPath) {
    for (PathMatcher matcher : excludePatterns) {
      if (matcher.matches(fsPath)) {
        return false;
      }
    }
    return true;
  }
}
