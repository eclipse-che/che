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
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.LinkedRenameRefactoringApply;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringChange;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringPreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameRefactoringSession;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameSettings;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ValidateNewName;
import org.eclipse.che.plugin.java.server.dto.DtoServerImpls;
import org.eclipse.che.plugin.java.server.refactoring.RefactoringManager;
import org.eclipse.jdt.core.ICompilationUnit;
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
public class RenameRefactoringTest extends RefactoringTest {


    private final RefactoringTestSetup setup;
    private       RefactoringManager   manager;
    public RenameRefactoringTest() {
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
    }

    @After
    public void after() throws Exception {
        setup.tearDown();
    }

    @Test
    public void testCreateLinkedRename() throws Exception {
        StringBuilder b = new StringBuilder();
        b.append("package p;\n");
        b.append("public class A{}\n");

        ICompilationUnit unit = getPackageP().createCompilationUnit("A.java", b.toString(), false, null);
        IType type = unit.getAllTypes()[0];
        RenameRefactoringSession refactoring = manager.createRenameRefactoring(type, unit, b.indexOf("A"), true);
        assertThat(refactoring).isNotNull();
        assertThat(refactoring.getSessionId()).isNotNull().isNotEmpty();
        assertThat(refactoring.getLinkedModeModel()).isNotNull();
    }

    @Test
    public void testApplyLinkedRename() throws Exception {
        StringBuilder b = new StringBuilder();
        b.append("package p;\n");
        b.append("public class A{}\n");

        ICompilationUnit unit = getPackageP().createCompilationUnit("A.java", b.toString(), false, null);
        IType type = unit.getAllTypes()[0];
        RenameRefactoringSession refactoring = manager.createRenameRefactoring(type, unit, b.indexOf("A"), true);
        assertThat(refactoring).isNotNull();
        assertThat(refactoring.getSessionId()).isNotNull().isNotEmpty();
        LinkedRenameRefactoringApply apply = new DtoServerImpls.LinkedRenameRefactoringApplyImpl();
        apply.setSessionId(refactoring.getSessionId());
        apply.setNewName("Test");
        RefactoringStatus status = manager.applyLinkedRename(apply);
        assertThat(status).isNotNull();
        assertThat(status.getSeverity()).isEqualTo(RefactoringStatus.OK);
        assertThat(unit.exists()).isFalse();
        ICompilationUnit newUnit = getPackageP().getCompilationUnit("Test.java");
        assertThat(newUnit.exists()).isTrue();
    }

    @Test
    public void testRenamePackageAlwaysWithWizard() throws Exception {
        StringBuilder b = new StringBuilder();
        b.append("package p;\n");
        b.append("public class A{}\n");

        ICompilationUnit unit = getPackageP().createCompilationUnit("A.java", b.toString(), false, null);
        RenameRefactoringSession refactoring = manager.createRenameRefactoring(getPackageP(), unit, b.indexOf("p;"), true);
        assertThat(refactoring).isNotNull();
        assertThat(refactoring.getSessionId()).isNotNull().isNotEmpty();
        assertThat(refactoring.getLinkedModeModel()).isNull();
        assertThat(refactoring.getWizardType()).isEqualTo(RenameRefactoringSession.RenameWizard.PACKAGE);
    }


    @Test
    public void testRenamePackage() throws Exception {
        final IPackageFragment packageFragment = getRoot().createPackageFragment("p.a.b.c", true, null);
        RenameRefactoringSession refactoring = manager.createRenameRefactoring(packageFragment, null, -1, false);
        assertThat(refactoring).isNotNull();
        assertThat(refactoring.getSessionId()).isNotNull().isNotEmpty();
        final ValidateNewName validateNewName = new DtoServerImpls.ValidateNewNameImpl().withNewName("p.x.s.z");
        validateNewName.setSessionId(refactoring.getSessionId());
        final RefactoringStatus status = manager.renameValidateNewName(validateNewName);
        assertThat(status.getSeverity()).isEqualTo(RefactoringStatus.OK);
    }


    @Test
    public void testCreateRename() throws Exception {
        StringBuilder b = new StringBuilder();
        b.append("package p;\n");
        b.append("public class A{}\n");

        ICompilationUnit unit = getPackageP().createCompilationUnit("A.java", b.toString(), false, null);
        IType type = unit.getAllTypes()[0];
        RenameRefactoringSession refactoring = manager.createRenameRefactoring(type, unit, b.indexOf("A"), false);
        assertThat(refactoring).isNotNull();
        assertThat(refactoring.getSessionId()).isNotNull().isNotEmpty();
        assertThat(refactoring.getLinkedModeModel()).isNull();
        assertThat(refactoring.getWizardType()).isEqualTo(RenameRefactoringSession.RenameWizard.TYPE);
    }

    @Test
    public void testRenameValidateName() throws Exception {
        StringBuilder b = new StringBuilder();
        b.append("package p;\n");
        b.append("public class A{}\n");

        ICompilationUnit unit = getPackageP().createCompilationUnit("A.java", b.toString(), false, null);
        IType type = unit.getAllTypes()[0];
        RenameRefactoringSession refactoring = manager.createRenameRefactoring(type, unit, b.indexOf("A"), false);
        DtoServerImpls.ValidateNewNameImpl validateNewName = new DtoServerImpls.ValidateNewNameImpl();
        validateNewName.setSessionId(refactoring.getSessionId());
        validateNewName.setNewName("MyClass");
        RefactoringStatus status = manager.renameValidateNewName(validateNewName);
        assertThat(status).isNotNull();
        assertThat(status.getSeverity()).isEqualTo(RefactoringStatus.OK);
    }

    @Test
    public void testRenamePreviewChanges() throws Exception {
        StringBuilder b = new StringBuilder();
        b.append("package p;\n");
        b.append("public class A{\n private A a; \n}\n");

        ICompilationUnit unit = getPackageP().createCompilationUnit("A.java", b.toString(), false, null);
        IType type = unit.getAllTypes()[0];
        RenameRefactoringSession refactoring = manager.createRenameRefactoring(type, unit, b.indexOf("A"), false);
        DtoServerImpls.ValidateNewNameImpl validateNewName = new DtoServerImpls.ValidateNewNameImpl();
        validateNewName.setSessionId(refactoring.getSessionId());
        validateNewName.setNewName("MyClass");
        RefactoringStatus status = manager.renameValidateNewName(validateNewName);
        manager.createChange(refactoring.getSessionId());
        RefactoringPreview preview = manager.getRefactoringPreview(refactoring.getSessionId());

        RefactoringChange change1 = new DtoServerImpls.ChangeEnabledStateImpl();
        change1.setSessionId(refactoring.getSessionId());
        change1.setChangeId(preview.getChildrens().get(0).getId());
        ChangePreview changePreview = manager.getChangePreview(change1);

        assertThat(changePreview).isNotNull();
        assertThat(changePreview.getFileName()).isNotNull().isNotEmpty();
        assertThat(changePreview.getOldContent()).isNotNull().isNotEmpty();
        assertThat(changePreview.getNewContent()).isNotNull().isNotEmpty();
        assertThat(changePreview.getNewContent()).isNotEqualTo(changePreview.getOldContent());
    }

    @Test
    public void testRenameValidateInvalidName() throws Exception {
        StringBuilder b = new StringBuilder();
        b.append("package p;\n");
        b.append("public class A{}\n");

        ICompilationUnit unit = getPackageP().createCompilationUnit("A.java", b.toString(), false, null);
        IType type = unit.getAllTypes()[0];
        RenameRefactoringSession refactoring = manager.createRenameRefactoring(type, unit, b.indexOf("A"), false);
        DtoServerImpls.ValidateNewNameImpl validateNewName = new DtoServerImpls.ValidateNewNameImpl();
        validateNewName.setSessionId(refactoring.getSessionId());
        validateNewName.setNewName("My#Class");
        RefactoringStatus status = manager.renameValidateNewName(validateNewName);
        assertThat(status).isNotNull();
        assertThat(status.getSeverity()).isEqualTo(RefactoringStatus.FATAL);
    }
    @Test
    public void testDoRename() throws Exception {
        StringBuilder b = new StringBuilder();
        b.append("package p;\n");
        b.append("public class A{}\n");

        ICompilationUnit unit = getPackageP().createCompilationUnit("A.java", b.toString(), false, null);
        IType type = unit.getAllTypes()[0];
        RenameRefactoringSession refactoring = manager.createRenameRefactoring(type, unit, b.indexOf("A"), false);
        DtoServerImpls.ValidateNewNameImpl validateNewName = new DtoServerImpls.ValidateNewNameImpl();
        validateNewName.setSessionId(refactoring.getSessionId());
        validateNewName.setNewName("MyClass");
        manager.renameValidateNewName(validateNewName);

        RenameSettings settings = new DtoServerImpls.RenameSettingsImpl();
        settings.setSessionId(refactoring.getSessionId());
        settings.setDeprecateDelegates(true);
        settings.setUpdateReferences(true);
        manager.setRenameSettings(settings);

        ChangeCreationResult change = manager.createChange(refactoring.getSessionId());
        assertThat(change).isNotNull();

        RefactoringStatus applyRefactoring = manager.applyRefactoring(refactoring.getSessionId());
        assertThat(applyRefactoring).isNotNull();
        assertThat(applyRefactoring.getSeverity()).isEqualTo(RefactoringStatus.OK);
        assertThat(unit.exists()).isFalse();
        ICompilationUnit newUnit = getPackageP().getCompilationUnit("MyClass.java");
        assertThat(newUnit.exists()).isTrue();
    }

}
