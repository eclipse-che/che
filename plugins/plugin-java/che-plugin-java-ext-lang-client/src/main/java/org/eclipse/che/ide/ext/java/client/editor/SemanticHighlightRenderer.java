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
package org.eclipse.che.ide.ext.java.client.editor;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.ide.api.editor.texteditor.HasTextMarkers;
import org.eclipse.che.ide.ext.java.client.Resources;
import org.eclipse.che.ide.ext.java.shared.dto.HighlightedPosition;

/**
 * Render semantic highlight for java files.
 *
 * @author Evgen Vidolob
 */
public class SemanticHighlightRenderer {

  private HasTextMarkers editor;
  private Document document;
  private Map<String, String> styleMap = new HashMap<>();
  private List<HasTextMarkers.MarkerRegistration> markers = new ArrayList<>();

  @Inject
  public SemanticHighlightRenderer(Resources resources) {
    Resources.SemanticCss css = resources.css();
    styleMap.put(HighlightedPosition.DEPRECATED_MEMBER, css.deprecatedMember());
    styleMap.put(HighlightedPosition.FIELD, css.field());
    styleMap.put(HighlightedPosition.METHOD_DECLARATION, css.methodDeclaration());
    styleMap.put(HighlightedPosition.STATIC_FIELD, css.staticField());
    styleMap.put(HighlightedPosition.STATIC_FINAL_FIELD, css.staticFinalField());
    styleMap.put(HighlightedPosition.STATIC_METHOD_INVOCATION, css.staticMethodInvocation());
    styleMap.put(HighlightedPosition.TYPE_VARIABLE, css.typeParameter());
  }

  public void init(HasTextMarkers editor, final Document document) {
    this.editor = editor;
    this.document = document;
  }

  public void reconcile(List<HighlightedPosition> positions) {
    for (HasTextMarkers.MarkerRegistration marker : markers) {
      marker.clearMark();
    }
    markers.clear();

    for (HighlightedPosition position : positions) {
      final TextPosition from = this.document.getPositionFromIndex(position.getOffset());
      final TextPosition to =
          this.document.getPositionFromIndex(position.getOffset() + position.getLength());
      HasTextMarkers.MarkerRegistration registration =
          editor.addMarker(new TextRange(from, to), styleMap.get(position.getType()));
      if (registration != null) {
        markers.add(registration);
      }
    }
  }
}
