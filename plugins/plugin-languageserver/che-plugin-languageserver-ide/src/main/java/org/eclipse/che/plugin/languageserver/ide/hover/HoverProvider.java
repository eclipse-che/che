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
package org.eclipse.che.plugin.languageserver.ide.hover;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.html.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.JsPromise;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.editor.orion.client.OrionHoverHandler;
import org.eclipse.che.ide.editor.orion.client.jso.OrionHoverContextOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionHoverOverlay;
import org.eclipse.che.ide.util.StringUtils;
import org.eclipse.che.ide.util.browser.BrowserUtils;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorConfiguration;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.ide.util.DtoBuildHelper;
import org.eclipse.che.plugin.languageserver.ide.util.OpenFileInEditorHelper;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * Provides hover LS functionality for Orion editor.
 *
 * @author Evgen Vidolob
 */
@Singleton
public class HoverProvider implements OrionHoverHandler {

  private final EditorAgent editorAgent;
  private final TextDocumentServiceClient client;
  private final DtoBuildHelper helper;
  private final OpenFileInEditorHelper openFileInEditorHelper;
  private final DtoBuildHelper dtoBuildHelper;
  private final String[] openableSchemes = new String[] {"file:/", "jdt:/"};

  @Inject
  public HoverProvider(
      EditorAgent editorAgent,
      TextDocumentServiceClient client,
      DtoBuildHelper helper,
      OpenFileInEditorHelper openFileInEditorHelper,
      DtoBuildHelper dtoBuildHelper) {
    this.editorAgent = editorAgent;
    this.client = client;
    this.helper = helper;
    this.openFileInEditorHelper = openFileInEditorHelper;
    this.dtoBuildHelper = dtoBuildHelper;
  }

  @Override
  public JsPromise<OrionHoverOverlay> computeHover(OrionHoverContextOverlay context) {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
    if (activeEditor == null || !(activeEditor instanceof TextEditor)) {
      return null;
    }

    TextEditor editor = ((TextEditor) activeEditor);
    if (!(editor.getConfiguration() instanceof LanguageServerEditorConfiguration)) {
      return null;
    }

    LanguageServerEditorConfiguration configuration =
        (LanguageServerEditorConfiguration) editor.getConfiguration();
    if (configuration.getServerCapabilities().getHoverProvider() == null
        || !configuration.getServerCapabilities().getHoverProvider()) {
      return null;
    }

    Document document = editor.getDocument();
    TextDocumentPositionParams paramsDTO = helper.createTDPP(document, context.getOffset());

    Promise<Hover> promise = client.hover(paramsDTO);
    Promise<OrionHoverOverlay> then =
        promise.then(
            (Hover arg) -> {
              OrionHoverOverlay hover = OrionHoverOverlay.create();
              hover.setType("markdown");
              String content = renderContent(arg);
              // do not show hover with only white spaces
              if (StringUtils.isNullOrWhitespace(content)) {
                return null;
              }
              hover.setContent(content);

              return hover;
            });

    createHoverLinkListener(activeEditor);

    return (JsPromise<OrionHoverOverlay>) then;
  }

  private String renderContent(Hover hover) {
    Either<List<Either<String, MarkedString>>, MarkupContent> contents = hover.getContents();
    if (contents.isLeft()) {

      return renderContent(contents.getLeft());
    } else {
      return contents.getRight().getValue();
    }
  }

  private String renderContent(List<Either<String, MarkedString>> contents) {
    List<String> result = new ArrayList<>();
    for (Either<String, MarkedString> dto : contents) {
      if (dto.isLeft()) {
        // plain markdown text
        result.add(dto.getLeft());
      } else {
        result.add(dto.getRight().getValue());
      }
    }
    return Joiner.on("\n\n").join(result);
  }

  private void createHoverLinkListener(EditorPartPresenter activeEditor) {
    final Window window = Elements.getWindow();
    window.addEventListener(
        Event.MOUSEDOWN,
        evt -> {
          Element anchorEle = (Element) evt.getTarget();

          // Register the onClick and open only if the element is in a tooltip.
          if (anchorEle.getOffsetParent() != null
              && anchorEle.getOffsetParent().getClassList() != null
              && anchorEle.getOffsetParent().getClassList().contains("textViewTooltipOnHover")
              && anchorEle.hasAttribute("href")) {
            String hrefContent = anchorEle.getAttribute("href");
            Location uriLocation = getLocationFromURI(hrefContent);
            anchorEle.setOnclick(
                anchorEleClick -> {
                  anchorEleClick.preventDefault();
                  anchorEleClick.stopPropagation();
                  if (Arrays.stream(openableSchemes)
                          .filter(scheme -> hrefContent.startsWith(scheme))
                          .count()
                      > 0) {
                    openFileInEditorHelper.openLocation(uriLocation);
                  } else {
                    BrowserUtils.openInNewTab(hrefContent);
                  }
                  ((TextEditor) activeEditor).getEditorWidget().hideTooltip();
                });
          }
        });
  }

  /**
   * Create a workable URI for opening files in the workspace
   *
   * @param uri The URI to fix
   * @return A URI that can be opened by the editor
   */
  private String createWorkableURI(String uri) {
    String fixedURI = uri;
    if (uri.indexOf("#") != -1) {
      fixedURI = fixedURI.substring(0, uri.lastIndexOf("#"));
    }

    // Because of the javadoc2markdown library in jdt.ls file:///projects gets stripped to
    // file:/projects when converting the markdown to html
    if (uri.startsWith("file:/projects")) {
      fixedURI = fixedURI.replace("file:/projects", "");
    }

    return fixedURI;
  }

  /**
   * Gets the location from a URI that is in the form of URI#LineNumber
   *
   * @param uri The URI to get the location from
   * @return Location object from the URI
   */
  private Location getLocationFromURI(String uri) {
    Location uriLoc = new Location();
    uriLoc.setUri(createWorkableURI(uri));

    Position uriPos = new Position();

    int lineIndex = uri.lastIndexOf('#');

    // We have line information from the URI
    if (lineIndex != -1) {
      String lastMatch = uri.substring(lineIndex + 1);
      Integer lineNumber = Integer.parseInt(lastMatch);

      if (lineNumber > 0) {
        uriPos.setLine(lineNumber - 1);
      } else {
        uriPos.setLine(lineNumber);
      }
    } else {
      uriPos.setLine(0);
    }

    uriLoc.setRange(new Range(uriPos, uriPos));

    return uriLoc;
  }
}
