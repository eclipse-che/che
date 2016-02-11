/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;

import org.eclipse.ltk.core.refactoring.ContentStamp;


public class ContentStamps {

	private static class ContentStampImpl extends ContentStamp {
		private int fKind;
		private long fValue;
		private long fFileStamp;

		public static final int FILE= 1;
		public static final int DOCUMENT= 2;

		private static ContentStamp createFileStamp(long value) {
			return new ContentStampImpl(FILE, value, value);
		}

		private static ContentStamp createDocumentStamp(long value, long fileValue) {
			return new ContentStampImpl(DOCUMENT, value, fileValue);
		}

		private ContentStampImpl(int kind, long value, long filestamp) {
			fKind= kind;
			fValue= value;
			fFileStamp= filestamp;
		}
		public boolean isDocumentStamp() {
			return fKind == DOCUMENT;
		}
		public long getValue() {
			return fValue;
		}
		public long getFileValue() {
			return fFileStamp;
		}
		public boolean isNullStamp() {
			return false;
		}
		public boolean equals(Object obj) {
			if (!(obj instanceof ContentStampImpl))
				return false;
			return ((ContentStampImpl)obj).fValue == fValue;
		}
		public int hashCode() {
			return (int)fValue;
		}
		public String toString() {
			return "Stamp: " + fValue; //$NON-NLS-1$
		}
	}

	private static class NullContentStamp extends ContentStamp {
		public boolean isNullStamp() {
			return true;
		}
		public String toString() {
			return "Null Stamp"; //$NON-NLS-1$
		}
	}

	public static final ContentStamp NULL_CONTENT_STAMP= new NullContentStamp();

	public static ContentStamp get(IFile file, IDocument document) {
		if (document instanceof IDocumentExtension4) {
			long stamp= ((IDocumentExtension4)document).getModificationStamp();
			if (stamp == IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP)
				return NULL_CONTENT_STAMP;
			return ContentStampImpl.createDocumentStamp(stamp, file.getModificationStamp());
		}
		long stamp= file.getModificationStamp();
		if (stamp == IResource.NULL_STAMP)
			return NULL_CONTENT_STAMP;
		return ContentStampImpl.createFileStamp(stamp);
	}

	public static void set(IFile file, ContentStamp s) throws CoreException {
		if (!(s instanceof ContentStampImpl))
			return;
		ContentStampImpl stamp= (ContentStampImpl)s;
		long value= stamp.getFileValue();
		Assert.isTrue(value != IResource.NULL_STAMP);
		file.revertModificationStamp(value);
	}

	public static boolean set(IDocument document, ContentStamp s) throws CoreException {
		if (!(s instanceof ContentStampImpl))
			return false;
		ContentStampImpl stamp= (ContentStampImpl)s;
		if (document instanceof IDocumentExtension4 && stamp.isDocumentStamp()) {
			try {
				((IDocumentExtension4)document).replace(0, 0, "", stamp.getValue()); //$NON-NLS-1$
				return true;
			} catch (BadLocationException e) {
				throw Changes.asCoreException(e);
			}
		}
		return false;
	}
}
