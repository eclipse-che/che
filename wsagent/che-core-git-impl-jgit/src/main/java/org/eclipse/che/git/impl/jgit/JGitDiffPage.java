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
 *   SAP           - implementation
 */
package org.eclipse.che.git.impl.jgit;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.System.lineSeparator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.api.git.DiffPage;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.params.DiffParams;
import org.eclipse.che.api.git.shared.DiffType;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.jgit.diff.ContentSource;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains information about difference between two commits, commit and working tree, working tree
 * and index, commit and index.
 *
 * @author Andrey Parfonov
 */
class JGitDiffPage extends DiffPage {
  private final DiffParams params;
  private final Repository repository;
  private final GitConnection gitConnection;

  private static final Logger LOG = LoggerFactory.getLogger(JGitDiffPage.class);

  JGitDiffPage(DiffParams params, Repository repository, GitConnection gitConnection) {
    this.params = params;
    this.repository = repository;
    this.gitConnection = gitConnection;
  }

  @Override
  public final void writeTo(OutputStream out) throws IOException {
    DiffFormatter formatter = new DiffFormatter(new BufferedOutputStream(out));
    formatter.setRepository(repository);
    List<String> rawFileFilter = params.getFileFilter();
    TreeFilter pathFilter =
        (rawFileFilter != null && rawFileFilter.size() > 0)
            ? PathFilterGroup.createFromStrings(rawFileFilter)
            : TreeFilter.ALL;
    formatter.setPathFilter(AndTreeFilter.create(TreeFilter.ANY_DIFF, pathFilter));

    try {
      String commitA = params.getCommitA();
      String commitB = params.getCommitB();
      boolean cached = params.isCached();

      List<DiffEntry> diff;
      if (commitA == null && commitB == null && !cached) {
        diff = indexToWorkingTree(formatter);
      } else if (commitA != null && commitB == null && !cached) {
        diff = commitToWorkingTree(commitA, formatter);
      } else if (commitA == null && commitB != null) {
        diff = emptyToCommit(commitB, formatter);
      } else if (commitB == null) {
        diff = commitToIndex(commitA, formatter);
      } else {
        diff = commitToCommit(commitA, commitB, formatter);
      }

      DiffType type = params.getType();
      if (type == DiffType.NAME_ONLY) {
        writeNames(diff, out);
      } else if (type == DiffType.NAME_STATUS) {
        writeNamesAndStatus(diff, out);
      } else {
        writeRawDiff(diff, formatter);
      }
    } catch (GitException e) {
      LOG.error(e.getMessage());
    } finally {
      formatter.close();
      repository.close();
    }
  }

  /**
   * Show changes between specified revision and empty tree.
   *
   * @param commitId id of commit
   * @param formatter diff formatter
   * @return list of diff entries
   * @throws IOException if any i/o errors occurs
   */
  private List<DiffEntry> emptyToCommit(String commitId, DiffFormatter formatter)
      throws IOException {
    ObjectId commit = repository.resolve(commitId);
    checkArgument(commit != null, "Invalid commit id " + commitId);
    RevTree tree;
    try (RevWalk revWalkA = new RevWalk(repository)) {
      tree = revWalkA.parseTree(commit);
    }

    List<DiffEntry> diff;
    try (ObjectReader reader = repository.newObjectReader()) {
      CanonicalTreeParser iterator = new CanonicalTreeParser();
      iterator.reset(reader, tree);
      diff = formatter.scan(new EmptyTreeIterator(), iterator);
    }
    return diff;
  }

  /**
   * Show changes between index and working tree.
   *
   * @param formatter diff formatter
   * @return list of diff entries
   * @throws IOException if any i/o errors occurs
   */
  private List<DiffEntry> indexToWorkingTree(DiffFormatter formatter) throws IOException {
    DirCache dirCache = null;
    ObjectReader reader = repository.newObjectReader();
    List<DiffEntry> diff;
    try {
      dirCache = repository.lockDirCache();
      DirCacheIterator iterA = new DirCacheIterator(dirCache);
      FileTreeIterator iterB = new FileTreeIterator(repository);
      // Seems bug in DiffFormatter when work with working. Disable detect
      // renames by formatter and do it later.
      formatter.setDetectRenames(false);
      diff = formatter.scan(iterA, iterB);
      if (!params.isNoRenames()) {
        // Detect renames.
        RenameDetector renameDetector = createRenameDetector();
        ContentSource.Pair sourcePairReader =
            new ContentSource.Pair(ContentSource.create(reader), ContentSource.create(iterB));
        renameDetector.addAll(diff);
        diff = renameDetector.compute(sourcePairReader, NullProgressMonitor.INSTANCE);
      }
    } finally {
      reader.close();
      if (dirCache != null) {
        dirCache.unlock();
      }
    }
    return diff;
  }

  /**
   * Show changes between specified revision and working tree.
   *
   * @param commitId id of commit
   * @param formatter diff formatter
   * @return list of diff entries
   * @throws IOException if any i/o errors occurs
   */
  private List<DiffEntry> commitToWorkingTree(String commitId, DiffFormatter formatter)
      throws IOException {
    ObjectId commitA = repository.resolve(commitId);
    if (commitA == null) {
      File heads = new File(repository.getWorkTree().getPath() + "/.git/refs/heads");
      if (heads.exists() && heads.list().length == 0) {
        return Collections.emptyList();
      }
      throw new IllegalArgumentException("Invalid commit id " + commitId);
    }
    RevTree treeA;
    try (RevWalk revWalkA = new RevWalk(repository)) {
      treeA = revWalkA.parseTree(commitA);
    }

    List<DiffEntry> diff;
    try (ObjectReader reader = repository.newObjectReader()) {
      CanonicalTreeParser iterA = new CanonicalTreeParser();
      iterA.reset(reader, treeA);
      FileTreeIterator iterB = new FileTreeIterator(repository);
      // Seems bug in DiffFormatter when work with working. Disable detect
      // renames by formatter and do it later.
      formatter.setDetectRenames(false);
      diff = formatter.scan(iterA, iterB);
      if (!params.isNoRenames()) {
        // Detect renames.
        RenameDetector renameDetector = createRenameDetector();
        ContentSource.Pair sourcePairReader =
            new ContentSource.Pair(ContentSource.create(reader), ContentSource.create(iterB));
        renameDetector.addAll(diff);
        diff = renameDetector.compute(sourcePairReader, NullProgressMonitor.INSTANCE);
      }
    }
    return diff;
  }

  /**
   * Show changes between specified revision and index. If <code>commitId == null</code> then view
   * changes between HEAD and index.
   *
   * @param commitId id of commit, pass <code>null</code> is the same as pass HEAD
   * @param formatter diff formatter
   * @return list of diff entries
   * @throws IOException if any i/o errors occurs
   */
  private List<DiffEntry> commitToIndex(String commitId, DiffFormatter formatter)
      throws IOException {
    if (commitId == null) {
      commitId = Constants.HEAD;
    }

    ObjectId commitA = repository.resolve(commitId);
    if (commitA == null) {
      throw new IllegalArgumentException("Invalid commit id " + commitId);
    }
    RevTree treeA;
    try (RevWalk revWalkA = new RevWalk(repository)) {
      treeA = revWalkA.parseTree(commitA);
    }

    DirCache dirCache = null;
    List<DiffEntry> diff;
    try (ObjectReader reader = repository.newObjectReader()) {
      dirCache = repository.lockDirCache();
      CanonicalTreeParser iterA = new CanonicalTreeParser();
      iterA.reset(reader, treeA);
      DirCacheIterator iterB = new DirCacheIterator(dirCache);
      if (!params.isNoRenames()) {
        // Use embedded RenameDetector it works well with index and
        // revision history.
        formatter.setDetectRenames(true);
        int renameLimit = params.getRenameLimit();
        if (renameLimit > 0) {
          formatter.getRenameDetector().setRenameLimit(renameLimit);
        }
      }
      diff = formatter.scan(iterA, iterB);
    } finally {
      if (dirCache != null) {
        dirCache.unlock();
      }
    }
    return diff;
  }

  /**
   * Show changes between specified two revisions and index. If <code>commitAId == null</code> then
   * view changes between HEAD and revision commitBId.
   *
   * @param commitAId id of commit A, pass <code>null</code> is the same as pass HEAD
   * @param commitBId id of commit B
   * @param formatter diff formatter
   * @return list of diff entries
   * @throws IOException if any i/o errors occurs
   */
  private List<DiffEntry> commitToCommit(
      String commitAId, String commitBId, DiffFormatter formatter) throws IOException {
    if (commitAId == null) {
      commitAId = Constants.HEAD;
    }

    ObjectId commitA = repository.resolve(commitAId);
    if (commitA == null) {
      throw new IllegalArgumentException("Invalid commit id " + commitAId);
    }
    ObjectId commitB = repository.resolve(commitBId);
    if (commitB == null) {
      throw new IllegalArgumentException("Invalid commit id " + commitBId);
    }

    RevTree treeA;
    try (RevWalk revWalkA = new RevWalk(repository)) {
      treeA = revWalkA.parseTree(commitA);
    }

    RevTree treeB;
    try (RevWalk revWalkB = new RevWalk(repository)) {
      treeB = revWalkB.parseTree(commitB);
    }

    if (!params.isNoRenames()) {
      // Use embedded RenameDetector it works well with index and revision
      // history.
      formatter.setDetectRenames(true);
      int renameLimit = params.getRenameLimit();
      if (renameLimit > 0) {
        formatter.getRenameDetector().setRenameLimit(renameLimit);
      }
    }
    return formatter.scan(treeA, treeB);
  }

  private RenameDetector createRenameDetector() {
    RenameDetector renameDetector = new RenameDetector(repository);
    int renameLimit = params.getRenameLimit();
    if (renameLimit > 0) {
      renameDetector.setRenameLimit(renameLimit);
    }
    return renameDetector;
  }

  private void writeRawDiff(List<DiffEntry> diff, DiffFormatter formatter) throws IOException {
    formatter.format(diff);
    formatter.flush();
  }

  private void writeNames(List<DiffEntry> diff, OutputStream out) throws IOException {
    PrintWriter writer = new PrintWriter(out);
    for (DiffEntry de : diff) {
      writer.print(
          (de.getChangeType() == ChangeType.DELETE ? de.getOldPath() : de.getNewPath())
              + (diff.size() != diff.indexOf(de) + 1 ? lineSeparator() : ""));
    }
    writer.flush();
  }

  private void writeNamesAndStatus(List<DiffEntry> diff, OutputStream out)
      throws IOException, GitException {
    PrintWriter writer = new PrintWriter(out);
    int diffSize = diff.size();

    Status status =
        gitConnection.status(diff.stream().map(DiffEntry::getNewPath).collect(Collectors.toList()));

    for (DiffEntry de : diff) {
      switch (de.getChangeType()) {
        case ADD:
          writer.print(
              (status.getUntracked().contains(de.getNewPath()) ? "U" : "A")
                  + "\t"
                  + de.getNewPath()
                  + (diffSize != diff.indexOf(de) + 1 ? lineSeparator() : ""));
          break;
        case DELETE:
          writer.print(
              "D\t" + de.getOldPath() + (diffSize != diff.indexOf(de) + 1 ? lineSeparator() : ""));
          break;
        case MODIFY:
          writer.print(
              "M\t" + de.getNewPath() + (diffSize != diff.indexOf(de) + 1 ? lineSeparator() : ""));
          break;
        case COPY:
          writer.print(
              "C\t"
                  + de.getOldPath()
                  + '\t'
                  + de.getNewPath()
                  + (diffSize != diff.indexOf(de) + 1 ? lineSeparator() : ""));
          break;
        case RENAME:
          writer.print(
              "R\t"
                  + de.getOldPath()
                  + '\t'
                  + de.getNewPath()
                  + (diffSize != diff.indexOf(de) + 1 ? lineSeparator() : ""));
          break;
      }
    }
    writer.flush();
  }
}
