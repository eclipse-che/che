/*******************************************************************************
 * Copyright (c) 2017 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.che.plugin.languageserver.ide.editor.quickassist;

import com.google.gwt.json.client.JSONValue;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.plugin.languageserver.ide.util.DtoBuildHelper;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.List;


/**
 * Applies a list of {@link org.eclipse.lsp4j.TextEdit} changes to the current editor.
 * 
 * @author Thomas Mäder
 *
 */
@Singleton
public class ApplyTextEditAction extends Action {
	private EditorAgent editorAgent;
	private DtoFactory dtoFactory;

	@Inject
	public ApplyTextEditAction(EditorAgent editorAgent,
                               DtoFactory dtoFactory,
                               DtoBuildHelper dtoBuildHelper) {
		super("Apply Text Edit");
		this.editorAgent = editorAgent;
		this.dtoFactory = dtoFactory;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
		if (!(activeEditor instanceof TextEditor)) {
			return;
		}
		Document document = ((TextEditor) activeEditor).getDocument();
		// We expect the arguments to be of the correct type: static misconfiguration is a programming error.
		List<Object> arguments = ((QuickassistActionEvent) e).getArguments();
		for (Object arg : arguments) {
			if ((arg instanceof JSONValue)) {
				TextEdit edit = dtoFactory.createDtoFromJson(arg.toString(), TextEdit.class);
				Range range = edit.getRange();
				Position start = range.getStart();
				Position end = range.getEnd();
				document.replace(start.getLine(), start.getCharacter(), end.getLine(), end.getCharacter(), edit.getNewText());
			}
		}
	}
}
