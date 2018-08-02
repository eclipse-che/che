/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
