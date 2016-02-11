/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/

package org.eclipse.che.jdt.javaeditor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.HighlightedPosition;
import org.eclipse.che.ide.ext.java.shared.dto.Problem;
import org.eclipse.che.ide.ext.java.shared.dto.ReconcileResult;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.ClassFileWorkingCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class JavaReconciler {
    private static final Logger LOG = LoggerFactory.getLogger(JavaReconciler.class);


    private SemanticHighlightingReconciler semanticHighlighting;

    @Inject
    public JavaReconciler(SemanticHighlightingReconciler semanticHighlighting) {
        this.semanticHighlighting = semanticHighlighting;
    }

    public ReconcileResult reconcile(IJavaProject javaProject, String fqn) throws JavaModelException {
        final ProblemRequestor requestor = new ProblemRequestor();
        WorkingCopyOwner wcOwner = new WorkingCopyOwner() {
            public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
                return requestor;
            }

            @Override
            public IBuffer createBuffer(ICompilationUnit workingCopy) {
//                return BufferManager.createBuffer(workingCopy);
//                ?????
                return new org.eclipse.jdt.internal.ui.javaeditor.DocumentAdapter(workingCopy, (IFile)workingCopy.getResource());
            }
        };
        List<HighlightedPosition> positions = null;
        ICompilationUnit compilationUnit = null;
        try {
            IType type = javaProject.findType(fqn);
            if (type == null) {
                return null;
            }
            if (type.isBinary()) {
                throw new IllegalArgumentException("Can't reconcile binary type: " + fqn);
            } else {
                compilationUnit = type.getCompilationUnit().getWorkingCopy(wcOwner, null);
            }
            requestor.reset();
            CompilationUnit unit = compilationUnit.reconcile(AST.JLS8, true, wcOwner, null);
            positions = semanticHighlighting.reconcileSemanticHighlight(unit);
            if (compilationUnit instanceof ClassFileWorkingCopy) {
                //we don't wont to show any errors from ".class" files
                requestor.reset();
            }

        } catch (JavaModelException e) {
            LOG.error("Can't reconcile class: " + fqn + " in project:" + javaProject.getPath().toOSString(), e);
            throw e;
        } finally {
            if(compilationUnit!= null && compilationUnit.isWorkingCopy()){
                try {
                    //todo close buffer
                    compilationUnit.getBuffer().close();
                    compilationUnit.discardWorkingCopy();
                } catch (JavaModelException e) {
                    //ignore
                }
            }
        }

        ReconcileResult result = DtoFactory.getInstance().createDto(ReconcileResult.class);
        result.setProblems(convertProblems(requestor.problems));
        result.setHighlightedPositions(positions);
        return result;
    }

    private List<Problem> convertProblems(List<IProblem> problems) {
        List<Problem> result = new ArrayList<>(problems.size());
        for (IProblem problem : problems) {
            result.add(convertProblem(problem));
        }
        return result;
    }

    private Problem convertProblem(IProblem problem) {
        Problem result = DtoFactory.getInstance().createDto(Problem.class);

        result.setArguments(Arrays.asList(problem.getArguments()));
        result.setID(problem.getID());
        result.setMessage(problem.getMessage());
        result.setOriginatingFileName(new String(problem.getOriginatingFileName()));
        result.setError(problem.isError());
        result.setWarning(problem.isWarning());
        result.setSourceEnd(problem.getSourceEnd());
        result.setSourceStart(problem.getSourceStart());
        result.setSourceLineNumber(problem.getSourceLineNumber());

        return result;
    }

    private static class ProblemRequestor implements IProblemRequestor {

        private List<IProblem> problems = new ArrayList<>();

        @Override
        public void acceptProblem(IProblem problem) {
            problems.add(problem);
        }

        @Override
        public void beginReporting() {

        }

        @Override
        public void endReporting() {

        }

        @Override
        public boolean isActive() {
            return true;
        }

        public void reset() {
            problems.clear();
        }
    }
}
