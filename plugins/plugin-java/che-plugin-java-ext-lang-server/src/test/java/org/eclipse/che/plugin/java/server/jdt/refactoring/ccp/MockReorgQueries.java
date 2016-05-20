/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.java.server.jdt.refactoring.ccp;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.corext.refactoring.reorg.IConfirmQuery;
import org.eclipse.jdt.internal.corext.refactoring.reorg.INewNameQueries;
import org.eclipse.jdt.internal.corext.refactoring.reorg.INewNameQuery;
import org.eclipse.jdt.internal.corext.refactoring.reorg.IReorgQueries;

import java.util.ArrayList;
import java.util.List;

public class MockReorgQueries implements IReorgQueries, INewNameQueries {
	private final List fQueriesRun= new ArrayList();

	public IConfirmQuery createYesNoQuery(String queryTitle, boolean allowCancel, int queryID) {
		run(queryID);
		return yesQuery;
	}

	public IConfirmQuery createYesYesToAllNoNoToAllQuery(String queryTitle, boolean allowCancel, int queryID) {
		run(queryID);
		return yesQuery;
	}

	private void run(int queryID) {
		fQueriesRun.add(new Integer(queryID));
	}

	//List<Integer>
	public List getRunQueryIDs() {
		return fQueriesRun;
	}

	private final IConfirmQuery yesQuery= new IConfirmQuery() {
		public boolean confirm(String question) throws OperationCanceledException {
			return true;
		}

		public boolean confirm(String question, Object[] elements) throws OperationCanceledException {
			return true;
		}
	};

	public IConfirmQuery createSkipQuery(String queryTitle, int queryID) {
		run(queryID);
		return yesQuery;
	}

	private static class NewNameQuery implements INewNameQuery {
		private final String fName;
		public NewNameQuery(String name) {
			fName= name;
		}
		public String getNewName() throws OperationCanceledException {
			return fName;
		}
	}


	public INewNameQuery createNewCompilationUnitNameQuery(ICompilationUnit cu, String initialSuggestedName) throws OperationCanceledException {
		return new NewNameQuery(initialSuggestedName + '1');
	}

	public INewNameQuery createNewPackageFragmentRootNameQuery(IPackageFragmentRoot root, String initialSuggestedName) throws OperationCanceledException {
		return new NewNameQuery(initialSuggestedName + '1');
	}

	public INewNameQuery createNewPackageNameQuery(IPackageFragment pack, String initialSuggestedName) throws OperationCanceledException {
		return new NewNameQuery(initialSuggestedName + '1');
	}

	public INewNameQuery createNewResourceNameQuery(IResource res, String initialSuggestedName) throws OperationCanceledException {
		return new NewNameQuery(initialSuggestedName + '1');
	}

	public INewNameQuery createNullQuery() {
		return new NewNameQuery(null);
	}

	public INewNameQuery createStaticQuery(String newName) throws OperationCanceledException {
		return new NewNameQuery(newName);
	}
}
