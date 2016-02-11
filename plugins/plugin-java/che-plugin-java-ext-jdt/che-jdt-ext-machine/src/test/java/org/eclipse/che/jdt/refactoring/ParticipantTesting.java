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
package org.eclipse.che.jdt.refactoring;

import org.eclipse.che.ltk.core.refactoring.participants.CheRefactoringParticipantsRegistry;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.corext.util.JavaElementResourceMapping;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;


public class ParticipantTesting {

	public static void init(){
		CheRefactoringParticipantsRegistry.registerParticipant("renameParticipants", TestRenameParticipantShared.class);
        CheRefactoringParticipantsRegistry.registerParticipant("renameParticipants", TestRenameParticipantSingle.class);

        CheRefactoringParticipantsRegistry.registerParticipant("moveParticipants", TestMoveParticipantShared.class);
        CheRefactoringParticipantsRegistry.registerParticipant("moveParticipants", TestMoveParticipantSingle.class);
        CheRefactoringParticipantsRegistry.registerParticipant("createParticipants", TestCreateParticipantSingle.class);
        CheRefactoringParticipantsRegistry.registerParticipant("createParticipants", TestCreateParticipantShared.class);

        CheRefactoringParticipantsRegistry.registerParticipant("deleteParticipants", TestDeleteParticipantShared.class);
        CheRefactoringParticipantsRegistry.registerParticipant("deleteParticipants", TestDeleteParticipantSingle.class);

//        CheRefactoringParticipantsRegistry.registerParticipant("copyParticipants", TestCopyParticipantSingle.class);

    }


	public static void reset() {
//		TestCreateParticipantShared.reset();
		TestDeleteParticipantShared.reset();
		TestMoveParticipantShared.reset();
		TestRenameParticipantShared.reset();
//		TestCopyParticipantShared.reset();

		TestCreateParticipantSingle.reset();
		TestDeleteParticipantSingle.reset();
		TestMoveParticipantSingle.reset();
		TestRenameParticipantSingle.reset();
//		TestCopyParticipantSingle.reset();
	}

	public static String[] createHandles(Object object) {
		return createHandles(new Object[] { object });
	}

	public static String[] createHandles(Object obj1, Object obj2) {
		return createHandles(new Object[] { obj1, obj2 });
	}

	public static String[] createHandles(Object obj1, Object obj2, Object obj3) {
		return createHandles(new Object[] { obj1, obj2, obj3 });
	}

	public static String[] createHandles(Object obj1, Object obj2, Object obj3, Object obj4) {
		return createHandles(new Object[] { obj1, obj2, obj3, obj4 });
	}

	public static String[] createHandles(Object[] elements) {
		List result= new ArrayList();
		for (int i= 0; i < elements.length; i++) {
			Object element= elements[i];
			if (element instanceof IJavaElement) {
				result.add(((IJavaElement)element).getHandleIdentifier());
			} else if (element instanceof IResource) {
				result.add(((IResource)element).getFullPath().toString());
			} else if (element instanceof JavaElementResourceMapping) {
				result.add(((JavaElementResourceMapping)element).
					getJavaElement().getHandleIdentifier() + "_mapping");
			}
		}
		return (String[])result.toArray(new String[result.size()]);
	}

	public static void testRename(String[] expectedHandles, RenameArguments[] args) {
		Assert.assertEquals(expectedHandles.length, args.length);
		if (expectedHandles.length == 0) {
			TestRenameParticipantShared.testNumberOfElements(0);
			TestRenameParticipantSingle.testNumberOfInstances(0);
		} else {
			testElementsShared(expectedHandles, TestRenameParticipantShared.fgInstance.fHandles);
			TestRenameParticipantShared.testArguments(args);

			TestRenameParticipantSingle.testNumberOfInstances(expectedHandles.length);
			TestRenameParticipantSingle.testElements(expectedHandles);
			TestRenameParticipantSingle.testArguments(args);
		}
	}

	public static void testMove(String[] expectedHandles, MoveArguments[] args) {
		Assert.assertEquals(expectedHandles.length, args.length);
		if (expectedHandles.length == 0) {
			TestMoveParticipantShared.testNumberOfElements(0);
			TestMoveParticipantSingle.testNumberOfInstances(0);
		} else {
			testElementsShared(expectedHandles, TestMoveParticipantShared.fgInstance.fHandles);
			TestMoveParticipantShared.testArguments(args);

			TestMoveParticipantSingle.testNumberOfInstances(expectedHandles.length);
			TestMoveParticipantSingle.testElements(expectedHandles);
			TestMoveParticipantSingle.testArguments(args);
		}
	}

	public static void testDelete(String[] expectedHandles) {
		if (expectedHandles.length == 0) {
			TestDeleteParticipantShared.testNumberOfElements(0);
			TestDeleteParticipantSingle.testNumberOfInstances(0);
		} else {
			testElementsShared(expectedHandles, TestDeleteParticipantShared.fgInstance.fHandles);

			TestDeleteParticipantSingle.testNumberOfInstances(expectedHandles.length);
			TestDeleteParticipantSingle.testElements(expectedHandles);
		}
	}

	public static void testCreate(String[] expectedHandles) {
		if (expectedHandles.length == 0)  {
			TestCreateParticipantShared.testNumberOfElements(0);
			TestCreateParticipantSingle.testNumberOfInstances(0);
		} else {
			testElementsShared(expectedHandles, TestCreateParticipantShared.fgInstance.fHandles);

			TestCreateParticipantSingle.testNumberOfInstances(expectedHandles.length);
			TestCreateParticipantSingle.testElements(expectedHandles);
		}
	}

//	public static void testCopy(String[] expectedHandles, CopyArguments[] arguments) {
//		if (expectedHandles.length == 0)  {
//			TestCopyParticipantShared.testNumberOfElements(0);
//			TestCopyParticipantSingle.testNumberOfInstances(0);
//		} else {
//			testElementsShared(expectedHandles, TestCopyParticipantShared.fgInstance.fHandles);
//			TestCopyParticipantShared.testArguments(arguments);
//
//			TestCopyParticipantSingle.testNumberOfInstances(expectedHandles.length);
//			TestCopyParticipantSingle.testElements(expectedHandles);
//			TestCopyParticipantSingle.testArguments(arguments);
//		}
//	}

	public static void testSimilarElements(List similarList, List similarNewNameList, List similarNewHandleList) {
		Assert.assertEquals(similarList.size(), similarNewNameList.size());
		if (similarList.size() == 0) {
			TestRenameParticipantShared.testNumberOfSimilarElements(0);
		} else {
			TestRenameParticipantShared.testSimilarElements(similarList, similarNewNameList, similarNewHandleList);
		}

	}

	private static void testElementsShared(String[] expected, List actual) {
		for (int i= 0; i < expected.length; i++) {
			String handle= expected[i];
			Assert.assertTrue("Expected handle not found: " + handle, actual.contains(handle));
		}
		testNumberOfElements(expected.length, actual);
	}

	private static void testNumberOfElements(int expected, List actual) {
		if (expected == 0 && actual == null)
			return;
		Assert.assertEquals(expected, actual.size());
	}

}
