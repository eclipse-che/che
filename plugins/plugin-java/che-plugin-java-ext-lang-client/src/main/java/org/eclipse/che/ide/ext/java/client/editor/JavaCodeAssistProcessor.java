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
package org.eclipse.che.ide.ext.java.client.editor;

import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.resolveFQN;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.gwt.resources.client.ImageResource;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistCallback;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.editor.link.HasLinkedMode;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactoringUpdater;
import org.eclipse.che.ide.ext.java.shared.dto.ProposalPresentation;
import org.eclipse.che.ide.ext.java.shared.dto.Proposals;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.loging.Log;
import org.vectomatic.dom.svg.ui.SVGResource;

public class JavaCodeAssistProcessor implements CodeAssistProcessor {

  private static Map<String, ImageResource> images;
  private static Map<String, SVGResource> svgs;

  private final EditorPartPresenter editor;
  private final JavaResources resources;
  private final RefactoringUpdater refactoringUpdater;

  private final JavaCodeAssistClient client;
  private final EditorAgent editorAgent;
  private final DtoUnmarshallerFactory unmarshallerFactory;
  private final JavaLocalizationConstant localizationConstant;

  private String errorMessage;

  @AssistedInject
  public JavaCodeAssistProcessor(
      @Assisted final EditorPartPresenter editor,
      final JavaCodeAssistClient client,
      final JavaResources javaResources,
      RefactoringUpdater refactoringUpdater,
      EditorAgent editorAgent,
      DtoUnmarshallerFactory unmarshallerFactory,
      JavaLocalizationConstant localizationConstant) {
    this.editor = editor;
    this.client = client;
    this.resources = javaResources;
    this.refactoringUpdater = refactoringUpdater;
    this.editorAgent = editorAgent;
    this.unmarshallerFactory = unmarshallerFactory;
    this.localizationConstant = localizationConstant;
    if (images == null) {
      initImages(javaResources);
    }
  }

  private void initImages(JavaResources resources) {
    images = new HashMap<>();
    svgs = new HashMap<>();

    svgs.put("template", resources.template());
    svgs.put("javadoc", resources.javadoc());
    svgs.put("annotation", resources.annotationItem());
    // todo create images for annotations
    svgs.put("privateAnnotation", resources.annotationItem());
    svgs.put("protectedAnnotation", resources.annotationItem());
    svgs.put("defaultAnnotation", resources.annotationItem());

    svgs.put("enum", resources.enumItem());
    svgs.put("defaultEnum", resources.enumItem());
    svgs.put("privateEnum", resources.enumItem());
    svgs.put("protectedEnum", resources.enumItem());

    svgs.put("interface", resources.interfaceItem());
    svgs.put("defaultInterface", resources.interfaceItem());
    svgs.put("innerInterfacePublic", resources.interfaceItem());
    svgs.put("innerInterfacePrivate", resources.interfaceItem());
    svgs.put("innerInterfaceProtected", resources.interfaceItem());

    svgs.put("class", resources.svgClassItem());
    svgs.put("defaultClass", resources.svgClassItem());
    svgs.put("innerClassPrivate", resources.svgClassItem());
    svgs.put("innerClassProtected", resources.svgClassItem());
    svgs.put("innerClassDefault", resources.svgClassItem());

    svgs.put("privateMethod", resources.privateMethod());
    svgs.put("publicMethod", resources.publicMethod());
    svgs.put("protectedMethod", resources.protectedMethod());
    svgs.put("defaultMethod", resources.defaultMethod());

    svgs.put("publicField", resources.publicField());
    svgs.put("protectedField", resources.protectedField());
    svgs.put("privateField", resources.privateField());
    svgs.put("defaultField", resources.defaultField());

    svgs.put("localVariable", resources.localVar());
    svgs.put("package", resources.packageItem());

    svgs.put("correctionLocal", resources.correctionChange());
    svgs.put("correctionChange", resources.correctionChange());
    svgs.put("correctionAdd", resources.correctionChange());
    svgs.put("jexception", resources.exceptionProp());
    svgs.put("correctionRemove", resources.correctionRemove());
    svgs.put("correctionCast", resources.correctionCast());
    svgs.put("correctionMove", resources.correctionRemove());
    svgs.put("correctionDeleteImport", resources.correctionDeleteImport());
    svgs.put("impObj", resources.add());
    svgs.put("toolDelete", resources.remove());
    svgs.put("linkedRename", resources.linkedRename());
  }

  public static String insertStyle(final JavaResources javaResources, final String display) {
    if (display.contains("#FQN#")) {
      return display.replace("#FQN#", javaResources.css().fqnStyle());
    } else if (display.contains("#COUNTER#")) {
      return display.replace("#COUNTER#", javaResources.css().counter());
    } else {
      return display;
    }
  }

  public static Icon getIcon(final String image) {
    if (svgs.containsKey(image)) {
      return new Icon("", svgs.get(image));
    }

    return new Icon("", images.get(image));
  }

  @Override
  public void computeCompletionProposals(
      final TextEditor textEditor,
      final int offset,
      final boolean triggered,
      final CodeAssistCallback callback) {
    if (errorMessage != null) {
      return;
    }
    final VirtualFile file = editor.getEditorInput().getFile();

    if (file instanceof Resource) {
      final Optional<Project> project = ((Resource) file).getRelatedProject();
      Unmarshallable<Proposals> unmarshaller = unmarshallerFactory.newUnmarshaller(Proposals.class);
      client.computeProposals(
          project.get().getLocation().toString(),
          resolveFQN(file),
          offset,
          textEditor.getDocument().getContents(),
          new AsyncRequestCallback<Proposals>(unmarshaller) {
            @Override
            protected void onSuccess(Proposals proposals) {
              showProposals(callback, proposals);
            }

            @Override
            protected void onFailure(Throwable throwable) {
              Log.error(JavaCodeAssistProcessor.class, throwable);
            }
          });
    }
  }

  private void showProposals(final CodeAssistCallback callback, final Proposals respons) {
    List<ProposalPresentation> presentations = respons.getProposals();
    final List<CompletionProposal> proposals = new ArrayList<>(presentations.size());
    HasLinkedMode linkedEditor = editor instanceof HasLinkedMode ? (HasLinkedMode) editor : null;
    for (final ProposalPresentation proposal : presentations) {
      final CompletionProposal completionProposal =
          new JavaCompletionProposal(
              proposal.getIndex(),
              insertStyle(resources, proposal.getDisplayString()),
              getIcon(proposal.getImage()),
              client,
              respons.getSessionId(),
              linkedEditor,
              refactoringUpdater,
              editorAgent);

      proposals.add(completionProposal);
    }

    callback.proposalComputed(proposals);
  }

  @Override
  public String getErrorMessage() {
    return this.errorMessage;
  }

  public void disableCodeAssistant(String errorMessage) {
    this.errorMessage =
        Strings.isNullOrEmpty(errorMessage)
            ? localizationConstant.codeAssistDefaultErrorMessage()
            : errorMessage;
  }

  public void enableCodeAssistant() {
    this.errorMessage = null;
  }
}
