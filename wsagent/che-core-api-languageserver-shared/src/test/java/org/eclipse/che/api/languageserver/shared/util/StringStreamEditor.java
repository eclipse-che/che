/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver.shared.util;

import java.util.Collection;
import java.util.function.Supplier;
import org.eclipse.lsp4j.TextEdit;

public class StringStreamEditor extends CharStreamEditor {

  public StringStreamEditor(Collection<TextEdit> edits, String contents, StringBuilder output) {
    super(edits, StringStreamEditor.forString(contents), CharStreamEditor.forStringBuilder(output));
  }

  public static Supplier<Integer> forString(String text) {
    return new Supplier<Integer>() {
      int index = 0;

      @Override
      public Integer get() {
        return (index >= text.length() ? -1 : text.charAt(index++));
      }
    };
  }
}
