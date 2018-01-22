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
import org.eclipse.che.ide.util.loging.Log;

/**
 * Java customizer adds an anchor link to the lines that match a stack trace line pattern and
 * installs a handler function for the link. The handler parses the stack trace line, searches for
 * the candidate Java files to navigate to, opens the first file (of the found candidates) in editor
 * and reveals it to the required line according to the stack trace line information
 *
 * @author Victor Rubezhny
 * @author Oleksandr Andriienko
 */
public class JavaOutputLinkifier extends AbstractOutputLinkifier {

  private static final String REG_EXPR = "(\\s+at\\s.+\\((.+\\.java:\\d+)\\))";

  public JavaOutputLinkifier(AppContext appContext, EditorAgent editorAgent) {
    super(appContext, editorAgent);
  }

  @Override
  public String getRegExpr() {
    return REG_EXPR;
  }

  @Override
  public int getMatchIndex() {
    return 2;
  }

  @Override
  public void onClickLink(String text) {
    text = text.replaceAll("\u00a0", " ");
    try {
      int start = text.indexOf("at", 0) + "at".length(),
          openBracket = text.indexOf("(", start),
          column = text.indexOf(":", openBracket),
          closingBracket = text.indexOf(")", column);
      String qualifiedName = text.substring(start, openBracket).trim();
      String fileName = text.substring(openBracket + "(".length(), column).trim();
      int lineNumber =
          Integer.valueOf(text.substring(column + ":".length(), closingBracket).trim());

      openJavaFile(qualifiedName, fileName, lineNumber);
    } catch (IndexOutOfBoundsException ex) {
      // ignore
    } catch (NumberFormatException ex) {
      // ignore
    }
  }

  /**
   * Open java file by {@code qualifiedName}, {@code fileName} in the {@code lineNumber}
   *
   * @param qualifiedName
   * @param fileName
   * @param lineNumber
   */
  private void openJavaFile(
      String qualifiedName, String fileName, int lineNumber) { // todo final int lineNumber
    Log.info(
        getClass(),
        "qualifiedName " + qualifiedName + " fileName " + fileName + " lineNumber " + lineNumber);
    if (qualifiedName == null || fileName == null) {
      return;
    }

    String qualifiedClassName =
        qualifiedName.lastIndexOf('.') != -1
            ? qualifiedName.substring(0, qualifiedName.lastIndexOf('.'))
            : qualifiedName;
    final String packageName =
        qualifiedClassName.lastIndexOf('.') != -1
            ? qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'))
            : "";

    String relativeFilePath =
        (packageName.isEmpty() ? "" : (packageName.replace(".", "/") + "/")) + fileName;

    Log.info(getClass(), "Relative path = " + relativeFilePath);
    collectChildren(appContext.getWorkspaceRoot(), Path.valueOf(relativeFilePath))
        .then(
            files -> {
              if (!files.isEmpty()) {
                openFileInEditorAndReveal(
                    appContext, editorAgent, files.get(0).getLocation(), lineNumber, 0);
              }
            });
  }
}
