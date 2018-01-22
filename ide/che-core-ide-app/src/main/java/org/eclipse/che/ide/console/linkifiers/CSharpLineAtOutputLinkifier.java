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
public class CSharpLineAtOutputLinkifier extends AbstractOutputLinkifier {

  private static final String REG_EXPR = "(\\s+at\\s.+\\sin\\s(.+\\.(cs|CS):line\\s\\d+))";

  public CSharpLineAtOutputLinkifier(AppContext appContext, EditorAgent editorAgent) {
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
      int start = text.lastIndexOf(" in ") + " in ".length(), end = text.indexOf(":line ", start);

      String fileName = text.substring(start, end).trim();
      int lineNumber = Integer.valueOf(text.substring(end + ":line ".length()).trim());

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
