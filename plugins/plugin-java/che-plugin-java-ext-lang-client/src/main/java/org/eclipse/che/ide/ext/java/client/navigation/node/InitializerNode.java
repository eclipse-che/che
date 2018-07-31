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
 */
package org.eclipse.che.ide.ext.java.client.navigation.node;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.navigation.filestructure.FileStructurePresenter;
import org.eclipse.che.ide.ext.java.client.util.Flags;
import org.eclipse.che.ide.ext.java.shared.dto.model.Initializer;
import org.eclipse.che.ide.ui.smartTree.data.HasAction;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Representation of java initializer block for the java navigation tree.
 *
 * @author Valeriy Svydenko
 */
public class InitializerNode extends AbstractPresentationNode implements HasAction {
  private final JavaResources resources;
  private final boolean isFromSuper;
  private final FileStructurePresenter fileStructurePresenter;
  private final Initializer initializer;

  @Inject
  public InitializerNode(
      JavaResources resources,
      @Assisted Initializer initializer,
      @Assisted("showInheritedMembers") boolean showInheritedMembers,
      @Assisted("isFromSuper") boolean isFromSuper,
      FileStructurePresenter fileStructurePresenter) {
    this.initializer = initializer;
    this.resources = resources;
    this.isFromSuper = isFromSuper;
    this.fileStructurePresenter = fileStructurePresenter;
  }

  /** {@inheritDoc} */
  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    updatePresentationField(isFromSuper, presentation, initializer.getLabel(), resources);

    SVGResource icon;
    int flag = initializer.getFlags();
    if (Flags.isPublic(flag)) {
      icon = resources.publicMethod();
    } else if (Flags.isPrivate(flag)) {
      icon = resources.privateMethod();
    } else if (Flags.isProtected(flag)) {
      icon = resources.protectedMethod();
    } else {
      icon = resources.publicMethod();
    }
    presentation.setPresentableIcon(icon);
  }

  /** {@inheritDoc} */
  @Override
  public String getName() {
    return initializer.getElementName();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isLeaf() {
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed() {
    fileStructurePresenter.actionPerformed(initializer);
  }
}
