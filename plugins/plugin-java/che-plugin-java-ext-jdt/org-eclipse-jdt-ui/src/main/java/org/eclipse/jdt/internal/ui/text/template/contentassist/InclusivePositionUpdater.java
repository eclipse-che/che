/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.text.template.contentassist;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.Position;

/**
 * Position updater that takes any change in [position.offset, position.offset + position.length] as
 * belonging to the position.
 *
 * @since 3.0
 */
class InclusivePositionUpdater implements IPositionUpdater {

	/** The position category. */
	private final String fCategory;

	/**
	 * Creates a new updater for the given <code>category</code>.
	 *
	 * @param category the new category.
	 */
	public InclusivePositionUpdater(String category) {
		fCategory= category;
	}

	/*
	 * @see org.eclipse.jface.text.IPositionUpdater#update(org.eclipse.jface.text.DocumentEvent)
	 */
	public void update(DocumentEvent event) {

		int eventOffset= event.getOffset();
		int eventOldLength= event.getLength();
		int eventNewLength= event.getText() == null ? 0 : event.getText().length();
		int deltaLength= eventNewLength - eventOldLength;

		try {
			Position[] positions= event.getDocument().getPositions(fCategory);

			for (int i= 0; i != positions.length; i++) {

				Position position= positions[i];

				if (position.isDeleted())
					continue;

				int offset= position.getOffset();
				int length= position.getLength();
				int end= offset + length;

				if (offset > eventOffset + eventOldLength)
					// position comes way
					// after change - shift
					position.setOffset(offset + deltaLength);
				else if (end < eventOffset) {
					// position comes way before change -
					// leave alone
				} else if (offset <= eventOffset && end >= eventOffset + eventOldLength) {
					// event completely internal to the position - adjust length
					position.setLength(length + deltaLength);
				} else if (offset < eventOffset) {
					// event extends over end of position - adjust length
					int newEnd= eventOffset + eventNewLength;
					position.setLength(newEnd - offset);
				} else if (end > eventOffset + eventOldLength) {
					// event extends from before position into it - adjust offset
					// and length
					// offset becomes end of event, length ajusted acordingly
					// we want to recycle the overlapping part
					position.setOffset(eventOffset);
					int deleted= eventOffset + eventOldLength - offset;
					position.setLength(length - deleted + eventNewLength);
				} else {
					// event consumes the position - delete it
					position.delete();
				}
			}
		} catch (BadPositionCategoryException e) {
			// ignore and return
		}
	}

	/**
	 * Returns the position category.
	 *
	 * @return the position category
	 */
	public String getCategory() {
		return fCategory;
	}

}
