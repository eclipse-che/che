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
package org.eclipse.che.plugin.java.server.che;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.editor.server.impl.EditorWorkingCopyManager;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.ide.ext.java.shared.dto.HighlightedPosition;
import org.eclipse.che.ide.ext.java.shared.dto.ReconcileResult;
import org.eclipse.che.jdt.javaeditor.JavaReconciler;
import org.eclipse.che.jdt.javaeditor.SemanticHighlightingReconciler;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/** @author Evgen Vidolob */
// TODO: rework after new Project API
@Ignore
public class ReconcileTest extends BaseTest {
  protected ICompilationUnit workingCopy;

  private JavaReconciler reconciler;

  void setWorkingCopyContents(String contents) throws JavaModelException {
    IPath path = workingCopy.getPath();
    File file = new File(wsPath, path.toOSString());
    try {
      file.delete();
      Files.write(file.toPath(), contents.getBytes(), StandardOpenOption.CREATE);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Before
  public void init() throws Exception {
    RequestTransmitter requestTransmitter = mock(RequestTransmitter.class);
    EventService eventService = new EventService();

    EditorWorkingCopyManager editorWorkingCopyManager =
        new EditorWorkingCopyManager(
            eventService, requestTransmitter, mock(FsManager.class), mock(ProjectManager.class));
    reconciler =
        new JavaReconciler(
            new SemanticHighlightingReconciler(),
            eventService,
            requestTransmitter,
            null,
            editorWorkingCopyManager);
    this.workingCopy =
        project.findType("p1.X").getCompilationUnit(); // .getWorkingCopy(this.wcOwner, null);
  }

  @Test
  public void testCompilationUnitReconcile() throws Exception {
    setWorkingCopyContents(
        "package p1;\n"
            + "public class X {\n"
            + "  public void foo() {\n"
            + "  }\n"
            + "  public void foo() {\n"
            + "  }\n"
            + "}");

    ReconcileResult reconcile = reconciler.reconcile(project, "p1.X");
    assertThat(reconcile).isNotNull();
    assertThat(reconcile.getProblems()).hasSize(2);
    assertThat(reconcile.getProblems())
        .onProperty("message")
        .containsSequence("Duplicate method foo() in type X");
    assertThat(reconcile.getProblems()).onProperty("error").containsSequence(true);
  }

  @Test
  public void testWarnings() throws Exception {
    project.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);
    project.setOption(JavaCore.COMPILER_PB_UNUSED_PARAMETER, JavaCore.ERROR);
    setWorkingCopyContents(
        "package p1;\n"
            + "public class X {\n"
            + "  public void foo() {\n"
            + "     int i = 0;\n"
            + "     String b = new String();\n"
            + "     System.out.println(b);\n"
            + "  }\n"
            + "}");
    ReconcileResult reconcile = reconciler.reconcile(project, "p1.X");
    assertThat(reconcile.getProblems()).onProperty("error").containsSequence(true);
  }

  @Test
  public void testSemanticHighlight() throws Exception {
    IType type = project.findType("java.lang.Object");
    ICompilationUnit copy =
        type.getClassFile().getWorkingCopy(DefaultWorkingCopyOwner.PRIMARY, null);
    CompilationUnit unit = copy.reconcile(AST.JLS8, true, DefaultWorkingCopyOwner.PRIMARY, null);
    SemanticHighlightingReconciler reconciler = new SemanticHighlightingReconciler();
    List<HighlightedPosition> positions = reconciler.reconcileSemanticHighlight(unit);
    assertThat(positions).isNotNull().isNotEmpty();
  }
}
