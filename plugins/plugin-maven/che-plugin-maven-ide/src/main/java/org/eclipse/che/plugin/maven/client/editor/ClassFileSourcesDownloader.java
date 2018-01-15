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
package org.eclipse.che.plugin.maven.client.editor;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;

import com.google.gwt.dom.client.Element;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import elemental.dom.Text;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.AnchorElement;
import elemental.html.DivElement;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.editor.EditorOpenedEvent;
import org.eclipse.che.ide.api.editor.EditorOpenedEventHandler;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.events.FileContentUpdateEvent;
import org.eclipse.che.ide.api.editor.texteditor.HasNotificationPanel;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPartView;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.tree.library.JarFileNode;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.plugin.maven.client.MavenLocalizationConstant;
import org.eclipse.che.plugin.maven.client.MavenResources;
import org.eclipse.che.plugin.maven.client.service.MavenServerServiceClient;

/**
 * Shows message about decompiled class and invoke downloading sources
 *
 * @author Evgen Vidolob
 */
@Singleton
public class ClassFileSourcesDownloader implements EditorOpenedEventHandler {

  private final EventBus eventBus;
  private final MavenServerServiceClient client;
  private final MavenLocalizationConstant constant;
  private final MavenResources resources;
  private final NotificationManager notificationManager;

  @Inject
  public ClassFileSourcesDownloader(
      EventBus eventBus,
      MavenServerServiceClient client,
      MavenLocalizationConstant constant,
      MavenResources resources,
      NotificationManager notificationManager) {
    this.eventBus = eventBus;
    this.client = client;
    this.constant = constant;
    this.resources = resources;
    this.notificationManager = notificationManager;
    eventBus.addHandler(EditorOpenedEvent.TYPE, this);
    resources.css().ensureInjected();
  }

  @Override
  public void onEditorOpened(EditorOpenedEvent event) {
    EditorPartPresenter editor = event.getEditor();
    VirtualFile file = editor.getEditorInput().getFile();
    if (file instanceof JarFileNode) {
      final JarFileNode jarFileNode = (JarFileNode) file;
      if (jarFileNode.isContentGenerated()) {
        if (editor instanceof TextEditor) {
          final TextEditor presenter = (TextEditor) editor;
          TextEditorPartView view = presenter.getView();
          final DivElement divElement = Elements.createDivElement();
          divElement.setClassName(resources.css().editorInfoPanel());
          Text textNode = Elements.createTextNode(constant.mavenClassDecompiled());
          DivElement decompiledElement = Elements.createDivElement();
          decompiledElement.appendChild(textNode);
          decompiledElement.setClassName(resources.css().editorMessage());
          divElement.appendChild(decompiledElement);
          AnchorElement anchorElement = Elements.createAnchorElement();
          anchorElement.appendChild(Elements.createTextNode(constant.mavenDownloadSources()));
          anchorElement.setHref("#");
          anchorElement.setClassName(resources.css().downloadLink());

          divElement.appendChild(anchorElement);
          final HasNotificationPanel.NotificationRemover remover =
              view.addNotification((Element) divElement);
          anchorElement.setOnclick(
              new EventListener() {
                @Override
                public void handleEvent(Event evt) {
                  downloadSources(jarFileNode, remover);
                }
              });
        }
      }
    }
  }

  private void downloadSources(
      JarFileNode jarFileNode, final HasNotificationPanel.NotificationRemover remover) {
    final String path = jarFileNode.getLocation().toString();
    Promise<Boolean> promise =
        client.downloadSources(jarFileNode.getProjectLocation().toString(), path);
    promise.then(
        new Operation<Boolean>() {
          @Override
          public void apply(Boolean arg) throws OperationException {
            if (arg) {
              eventBus.fireEvent(new FileContentUpdateEvent(path));
            } else {
              notificationManager.notify(
                  constant.mavenClassDownloadFailed(path),
                  StatusNotification.Status.FAIL,
                  EMERGE_MODE);
            }
            remover.remove();
          }
        });
  }
}
