/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.format;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.Change;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

/** @author Roman Nikitenko */
public class DocumentChangeListener implements IDocumentListener {
  private List<Change> changes;

  public DocumentChangeListener(IDocument document) {
    document.addDocumentListener(this);
    changes = new ArrayList<>();
  }

  public List<Change> getChanges() {
    return this.changes;
  }

  @Override
  public void documentAboutToBeChanged(DocumentEvent documentEvent) {}

  @Override
  public void documentChanged(DocumentEvent event) {
    final DtoFactory dtoFactory = DtoFactory.getInstance();
    Change dto = dtoFactory.createDto(Change.class);
    dto.setLength(event.getLength());
    dto.setOffset(event.getOffset());
    dto.setText(event.getText());
    changes.add(dto);
  }
}
