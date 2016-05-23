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

package org.eclipse.che.plugin.java.server.jdt.refactoring;

import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeCreationResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangePreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.MoveSettings;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringChange;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringPreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ReorgDestination;
import org.eclipse.che.plugin.java.server.dto.DtoServerImpls;
import org.eclipse.che.plugin.java.server.refactoring.RefactoringManager;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Evgen Vidolob
 */
public class MoveRefactoringSessionTest extends RefactoringTest {


    private final RefactoringTestSetup setup;
    private       RefactoringManager   manager;
    private       IPackageFragment     p1;

    public MoveRefactoringSessionTest() {
        this.setup = new RefactoringTestSetup();
    }

    @BeforeClass
    public static void prepareClass() {
        ParticipantTesting.init();

    }

    @Before
    public void setUp() throws Exception {
        setup.setUp();
        super.setUp();
        manager = new RefactoringManager();
        getPackageP().createCompilationUnit("A.java", "package p;class A{}", false, new NullProgressMonitor());
        p1 = getRoot().createPackageFragment("p1", false, new NullProgressMonitor());
    }

    @After
    public void after() throws Exception {
        setup.tearDown();
    }

    @Test
    public void testCreateMoveSession() throws Exception {
        IType type = fProject.findType("p.A");
        ICompilationUnit unit = type.getCompilationUnit();
        String sessionId = manager.createMoveRefactoringSession(new IJavaElement[]{unit});
        assertThat(sessionId).isNotNull().isNotEmpty();
    }

    @Test
    public void testSetMoveDestination() throws Exception {
        IType type = fProject.findType("p.A");
        ICompilationUnit unit = type.getCompilationUnit();
        String sessionId = manager.createMoveRefactoringSession(new IJavaElement[]{unit});
        ReorgDestination destination = new DtoServerImpls.ReorgDestinationImpl();
        destination.setSessionId(sessionId);
        destination.setProjectPath(RefactoringTestSetup.getProject().getPath().toOSString());
        destination.setDestination(p1.getPath().toOSString());
        destination.setType(ReorgDestination.DestinationType.PACKAGE);
        RefactoringStatus status = manager.setRefactoringDestination(destination);
        assertThat(status).isNotNull();
        assertThat(status.getSeverity()).isEqualTo(RefactoringStatus.OK);
    }

    @Test
    public void testCtrateMoveChanges() throws Exception {
        IType type = fProject.findType("p.A");
        ICompilationUnit unit = type.getCompilationUnit();
        String sessionId = manager.createMoveRefactoringSession(new IJavaElement[]{unit});
        ReorgDestination destination = new DtoServerImpls.ReorgDestinationImpl();
        destination.setSessionId(sessionId);
        destination.setProjectPath(RefactoringTestSetup.getProject().getPath().toOSString());
        destination.setDestination(p1.getPath().toOSString());
        destination.setType(ReorgDestination.DestinationType.PACKAGE);
        manager.setRefactoringDestination(destination);
        MoveSettings settings = new DtoServerImpls.MoveSettingsImpl();
        settings.setUpdateReferences(true);
        settings.setSessionId(sessionId);
        manager.setMoveSettings(settings);
        ChangeCreationResult change = manager.createChange(sessionId);
        assertThat(change).isNotNull();
        assertThat(change.isCanShowPreviewPage()).isTrue();
        assertThat(change.getStatus().getSeverity()).isEqualTo(RefactoringStatus.OK);
    }

    @Test
    public void testGetMoveChanges() throws Exception {
        IType type = fProject.findType("p.A");
        ICompilationUnit unit = type.getCompilationUnit();
        String sessionId = manager.createMoveRefactoringSession(new IJavaElement[]{unit});
        ReorgDestination destination = new DtoServerImpls.ReorgDestinationImpl();
        destination.setSessionId(sessionId);
        destination.setProjectPath(RefactoringTestSetup.getProject().getPath().toOSString());
        destination.setDestination(p1.getPath().toOSString());
        destination.setType(ReorgDestination.DestinationType.PACKAGE);
        manager.setRefactoringDestination(destination);
        MoveSettings settings = new DtoServerImpls.MoveSettingsImpl();
        settings.setUpdateReferences(true);
        settings.setSessionId(sessionId);
        manager.setMoveSettings(settings);
        manager.createChange(sessionId);
        RefactoringPreview change = manager.getRefactoringPreview(sessionId);
        assertThat(change).isNotNull();
        assertThat(change.getText()).isEqualTo("Move");
        assertThat(change.getChildrens()).isNotNull().hasSize(2);

    }

    @Test
    public void testPreviewChanges() throws Exception {
        IType type = fProject.findType("p.A");
        ICompilationUnit unit = type.getCompilationUnit();
        String sessionId = manager.createMoveRefactoringSession(new IJavaElement[]{unit});
        ReorgDestination destination = new DtoServerImpls.ReorgDestinationImpl();
        destination.setSessionId(sessionId);
        destination.setProjectPath(RefactoringTestSetup.getProject().getPath().toOSString());
        destination.setDestination(p1.getPath().toOSString());
        destination.setType(ReorgDestination.DestinationType.PACKAGE);
        manager.setRefactoringDestination(destination);
        MoveSettings settings = new DtoServerImpls.MoveSettingsImpl();
        settings.setUpdateReferences(true);
        settings.setSessionId(sessionId);
        manager.setMoveSettings(settings);
        manager.createChange(sessionId);
        RefactoringPreview change = manager.getRefactoringPreview(sessionId);

        RefactoringChange change1 = new DtoServerImpls.ChangeEnabledStateImpl();
        change1.setSessionId(sessionId);
        change1.setChangeId(change.getChildrens().get(0).getId());
        ChangePreview preview = manager.getChangePreview(change1);

        assertThat(preview).isNotNull();
        assertThat(preview.getFileName()).isNotNull().isNotEmpty();
        assertThat(preview.getOldContent()).isNotNull().isNotEmpty();
        assertThat(preview.getNewContent()).isNotNull().isNotEmpty();

    }

    @Test
    public void testApplyMove() throws Exception {
        IType type = fProject.findType("p.A");
        ICompilationUnit unit = type.getCompilationUnit();
        String sessionId = manager.createMoveRefactoringSession(new IJavaElement[]{unit});
        ReorgDestination destination = new DtoServerImpls.ReorgDestinationImpl();
        destination.setSessionId(sessionId);
        destination.setProjectPath(RefactoringTestSetup.getProject().getPath().toOSString());
        destination.setDestination(p1.getPath().toOSString());
        destination.setType(ReorgDestination.DestinationType.PACKAGE);
        manager.setRefactoringDestination(destination);
        MoveSettings settings = new DtoServerImpls.MoveSettingsImpl();
        settings.setUpdateReferences(true);
        settings.setSessionId(sessionId);
        manager.setMoveSettings(settings);
        manager.createChange(sessionId);
        RefactoringStatus status = manager.applyRefactoring(sessionId);
        assertThat(status).isNotNull();
        assertThat(status.getSeverity()).isEqualTo(RefactoringStatus.OK);
        IType movedType = fProject.findType("p1.A");
        assertThat(movedType).isNotNull();
        assertThat(movedType.exists()).isTrue();
    }

}
