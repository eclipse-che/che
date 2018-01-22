/**
 * ***************************************************************************** Copyright (c) 2017
 * Red Hat, Inc. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.ide.console.linkifiers;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.resource.Path;

/**
 * Cpp linker message customizer adds an anchor link to the lines that match a stack trace line
 * pattern and installs a handler function for the link. The handler parses the stack trace line,
 * searches for the candidate cpp files to navigate to, opens the first file (of the found
 * candidates) in editor and reveals it to the required line according to the stack trace line
 * information
 *
 * @author Victor Rubezhny
 * @author Oleksander Andriienko
 */
public class CppLinkerMsgOutputLinkifier extends AbstractOutputLinkifier {

  private static final String REG_EXPR = "((.+\\.(c|C|cc|CC|cpp|CPP|h|H|hpp|HPP):\\d+):\\s.+)";

  public CppLinkerMsgOutputLinkifier(AppContext appContext, EditorAgent editorAgent) {
    super(appContext, editorAgent);
  }

  @Override
  public String getRegExpr() {
    return REG_EXPR;
  }

  @Override
  public void onClickLink(String text) {
    text = text.replaceAll("\u00a0", " ");
    try {
      int lineStart = text.indexOf(":") + ":".length(), lineEnd = text.indexOf(":", lineStart);
      String fileName = text.substring(0, text.indexOf(":")).trim();
      int lineNumber = Integer.valueOf(text.substring(lineStart, lineEnd).trim());

      openFileInEditorAndReveal(
          appContext, editorAgent, Path.valueOf(fileName).removeFirstSegments(1), lineNumber, 0);
    } catch (IndexOutOfBoundsException ex) {
      // ignore
    } catch (NumberFormatException ex) {
      // ignore
    }
  }

  @Override
  public int getMatchIndex() {
    return 2;
  }
}
