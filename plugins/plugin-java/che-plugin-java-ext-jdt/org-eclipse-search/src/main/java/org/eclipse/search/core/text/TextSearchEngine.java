/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.search.core.text;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.search.internal.core.text.PatternConstructor;
import org.eclipse.search.internal.core.text.TextSearchVisitor;

/**
 * A {@link TextSearchEngine} searches the content of a workspace file resources for matches to a
 * given search pattern.
 *
 * <p>{@link #create()} gives access to an instance of the search engine. By default this is the
 * default text search engine (see {@link #createDefault()}) but extensions can offer more
 * sophisticated search engine implementations.
 *
 * @since 3.2
 */
public abstract class TextSearchEngine {

  /**
   * Creates an instance of the search engine. By default this is the default text search engine
   * (see {@link #createDefault()}), but extensions can offer more sophisticated search engine
   * implementations.
   *
   * @return the created {@link TextSearchEngine}.
   */
  public static TextSearchEngine create() {
    return createDefault(); // SearchPlugin.getDefault().getTextSearchEngineRegistry().getPreferred();
  }

  /**
   * Creates the default, built-in, text search engine that implements a brute-force search, not
   * using any search index. Note that clients should always use the search engine provided by
   * {@link #create()}.
   *
   * @return an instance of the default text search engine {@link TextSearchEngine}.
   */
  public static TextSearchEngine createDefault() {
    return new TextSearchEngine() {
      public IStatus search(
          TextSearchScope scope,
          TextSearchRequestor requestor,
          Pattern searchPattern,
          IProgressMonitor monitor) {
        return new TextSearchVisitor(requestor, searchPattern).search(scope, monitor);
      }

      public IStatus search(
          IFile[] scope,
          TextSearchRequestor requestor,
          Pattern searchPattern,
          IProgressMonitor monitor) {
        return new TextSearchVisitor(requestor, searchPattern).search(scope, monitor);
      }
    };
  }

  /**
   * Uses a given search pattern to find matches in the content of workspace file resources. If a
   * file is open in an editor, the editor buffer is searched.
   *
   * @param requestor the search requestor that gets the search results
   * @param scope the scope defining the resources to search in
   * @param searchPattern The search pattern used to find matches in the file contents.
   * @param monitor the progress monitor to use
   * @return the status containing information about problems in resources searched.
   */
  public abstract IStatus search(
      TextSearchScope scope,
      TextSearchRequestor requestor,
      Pattern searchPattern,
      IProgressMonitor monitor);

  /**
   * Uses a given search pattern to find matches in the content of workspace file resources. If a
   * file is open in an editor, the editor buffer is searched.
   *
   * @param requestor the search requestor that gets the search results
   * @param scope the files to search in
   * @param searchPattern The search pattern used to find matches in the file contents.
   * @param monitor the progress monitor to use
   * @return the status containing information about problems in resources searched.
   */
  public abstract IStatus search(
      IFile[] scope,
      TextSearchRequestor requestor,
      Pattern searchPattern,
      IProgressMonitor monitor);

  /**
   * Creates a pattern for the given search string and the given options.
   *
   * @param pattern the search pattern. If <code>isRegex</code> is:
   *     <ul>
   *       <li><code>false</code>: a string including '*' and '?' wildcards and '\' for escaping the
   *           literals '*', '?' and '\'
   *       <li><code>true</code>: a regex as specified by {@link Pattern} plus "\R" denoting a line
   *           delimiter (platform independent)
   *     </ul>
   *
   * @param isRegex <code>true</code> if the given string follows the {@link Pattern} including "\R"
   * @param isCaseSensitive Set to <code>true</code> to create a case insensitive pattern
   * @return the created pattern
   * @throws PatternSyntaxException if "\R" is at an illegal position
   * @see Pattern
   * @since 3.8
   */
  public static Pattern createPattern(String pattern, boolean isCaseSensitive, boolean isRegex)
      throws PatternSyntaxException {
    return PatternConstructor.createPattern(pattern, isRegex, true, isCaseSensitive, false);
  }
}
