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
 * Output customizer adds an anchor link to the lines that match a .NET C# compilation error or
 * warning message and a stack trace line pattern and installs the handler functions for the links.
 * The handler parses the stack trace line, searches for a candidate C# file to navigate to, opens
 * the found file in editor and reveals it to the required line and column (if available) according
 * to the line information
 *
 * @author Victor Rubezhny
 * @author Oleksandr Andriienko
 */
public class CSharpCompilationWarnErrOutputLinkifier extends AbstractOutputLinkifier {

  private static final String REG_EXPR =
      "((.+\\.(cs|CS)\\(\\d+,\\d+\\)):\\s(error|warning)\\s.+:\\s.+\\s\\[.+\\])";

  public CSharpCompilationWarnErrOutputLinkifier(AppContext appContext, EditorAgent editorAgent) {
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
      int end = text.indexOf("):"),
          openBracket = text.lastIndexOf("(", end),
          comma = text.lastIndexOf(",", end),
          closeSBracket = text.lastIndexOf("]"),
          openSBracket = text.lastIndexOf("[", closeSBracket);
      String fileName = text.substring(0, openBracket).trim();
      String projectFileName = text.substring(openSBracket + "[".length(), closeSBracket).trim();
      int lineNumber = Integer.valueOf(text.substring(openBracket + "(".length(), comma).trim());
      int columnNumber = Integer.valueOf(text.substring(comma + ",".length(), end).trim());

      openFile(fileName, projectFileName, lineNumber, columnNumber);
    } catch (IndexOutOfBoundsException ex) {
      // ignore
    } catch (NumberFormatException ex) {
      // ignore
    }
  }

  public void openFile(
      String fileName, String projectFile, final int lineNumber, final int columnNumber) {
    if (fileName == null || projectFile == null) {
      return;
    }

    openFileInEditorAndReveal(
        appContext,
        editorAgent,
        Path.valueOf(projectFile).removeFirstSegments(1).parent().append(fileName),
        lineNumber,
        columnNumber);
  }

  @Override
  public int getMatchIndex() {
    return 2;
  }
}
