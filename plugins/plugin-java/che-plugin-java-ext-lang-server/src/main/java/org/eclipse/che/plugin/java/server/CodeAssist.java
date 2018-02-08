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
package org.eclipse.che.plugin.java.server;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.Change;
import org.eclipse.che.ide.ext.java.shared.dto.ConflictImportDTO;
import org.eclipse.che.ide.ext.java.shared.dto.OrganizeImportResult;
import org.eclipse.che.jdt.javaeditor.TextViewer;
import org.eclipse.che.jface.text.contentassist.ICompletionProposal;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation;
import org.eclipse.jdt.internal.corext.format.DocumentChangeListener;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Evgen Vidolob */
@Singleton
public class CodeAssist {
  private static final Logger LOG = LoggerFactory.getLogger(CodeAssist.class);
  private final Cache<String, CodeAssistContext> cache;

  public CodeAssist() {
    // todo configure expire time
    cache =
        CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .removalListener(
                new RemovalListener<String, CodeAssistContext>() {
                  @Override
                  public void onRemoval(
                      RemovalNotification<String, CodeAssistContext> notification) {
                    if (notification.getValue() != null) {
                      notification.getValue().clean();
                    }
                  }
                })
            .build();
  }

  /**
   * Organizes the imports of a compilation unit.
   *
   * @param project current java project
   * @param fqn fully qualified name of the java file
   * @return list of imports which have conflicts
   */
  public OrganizeImportResult organizeImports(IJavaProject project, String fqn)
      throws CoreException, BadLocationException {
    ICompilationUnit compilationUnit = prepareCompilationUnit(project, fqn);
    return createOrganizeImportOperation(compilationUnit, null);
  }

  /**
   * Applies chosen imports after resolving conflicts.
   *
   * @param project current java project
   * @param fqn fully qualified name of the java file
   * @param chosen list of chosen imports as result of resolving conflicts which needed to add to
   *     all imports.
   */
  public List<Change> applyChosenImports(IJavaProject project, String fqn, List<String> chosen)
      throws CoreException, BadLocationException {
    ICompilationUnit compilationUnit = prepareCompilationUnit(project, fqn);
    OrganizeImportResult result = createOrganizeImportOperation(compilationUnit, chosen);
    return result.getChanges();
  }

  private OrganizeImportResult createOrganizeImportOperation(
      ICompilationUnit compilationUnit, List<String> chosen) throws CoreException {
    CodeGenerationSettings settings =
        JavaPreferencesSettings.getCodeGenerationSettings(compilationUnit.getJavaProject());

    OrganizeImportsOperation operation =
        new OrganizeImportsOperation(
            compilationUnit,
            null,
            settings.importIgnoreLowercase,
            !compilationUnit.isWorkingCopy(),
            true,
            chosen,
            null);

    NullProgressMonitor monitor = new NullProgressMonitor();
    TextEdit edit = operation.createTextEdit(monitor);
    OrganizeImportResult result = DtoFactory.newDto(OrganizeImportResult.class);
    TypeNameMatch[][] choices = operation.getChoices();
    // Apply organize import declarations if operation doesn't have conflicts (choices.length == 0)
    // or all conflicts were resolved (!chosen.isEmpty())
    if ((chosen != null && !chosen.isEmpty()) || choices == null || choices.length == 0) {
      IBuffer buffer = compilationUnit.getBuffer();
      IDocument document = new Document(buffer.getContents());
      DocumentChangeListener documentChangeListener = new DocumentChangeListener(document);
      try {
        edit.apply(document);
      } catch (BadLocationException e) {
        LOG.debug("Applying Organize import text edits goes wrong:", e);
      }
      result.setChanges(documentChangeListener.getChanges());
      return result;
    }

    result.setConflicts(createListOfDTOMatches(choices));
    return result;
  }

  private List<ConflictImportDTO> createListOfDTOMatches(TypeNameMatch[][] choices) {
    List<ConflictImportDTO> typeMatches = new ArrayList<>();
    for (int i = 0; i < choices.length; i++) {
      List<String> nameMatches = new ArrayList<>();
      TypeNameMatch[] choice = choices[i];
      for (int j = 0; j < choice.length; j++) {
        nameMatches.add(choice[j].getFullyQualifiedName());
      }
      typeMatches.add(DtoFactory.newDto(ConflictImportDTO.class).withTypeMatches(nameMatches));
    }
    return typeMatches;
  }

  private ICompilationUnit prepareCompilationUnit(IJavaProject project, String fqn)
      throws JavaModelException {
    ICompilationUnit compilationUnit;

    IType type = project.findType(fqn);
    if (type == null) {
      throw new JavaModelException(
          new JavaModelStatus(
              IJavaModelStatusConstants.CORE_EXCEPTION, "Can't find a file: " + fqn));
    }
    if (type.isBinary()) {
      throw new JavaModelException(
          new JavaModelStatus(
              IJavaModelStatusConstants.CORE_EXCEPTION, "Can't organize imports on binary file"));
    } else {
      compilationUnit = type.getCompilationUnit();
    }
    return compilationUnit;
  }

  private class CodeAssistContext {
    private TextViewer viewer;
    private int offset;
    private List<ICompletionProposal> proposals;
    private ICompilationUnit cUnit;

    public CodeAssistContext(
        TextViewer viewer,
        int offset,
        List<ICompletionProposal> proposals,
        ICompilationUnit cUnit) {
      this.viewer = viewer;
      this.offset = offset;
      this.proposals = proposals;
      this.cUnit = cUnit;
    }

    public void clean() {
      if (cUnit != null) {
        try {
          cUnit.discardWorkingCopy();
        } catch (JavaModelException e) {
          // ignore
        }
        try {
          FileBuffers.getTextFileBufferManager()
              .disconnect(cUnit.getPath(), LocationKind.IFILE, new NullProgressMonitor());
        } catch (CoreException e) {
          LOG.error("Can't disconnect from file buffer: " + cUnit.getPath(), e);
        }
      }
    }
  }
}
