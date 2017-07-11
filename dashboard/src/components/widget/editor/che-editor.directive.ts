/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * @ngdoc directive
 * @name components.directive:cheEditor
 * @restrict E
 * @element
 *
 * @description
 * `<che-editor editor-content="ctrl.editorContent"></che-editor>` for displaying the editor
 *
 * @usage
 *   <che-editor editor-content="ctrl.editorContent"
 *               editor-state="ctrl.editorState"></che-editor>
 *
 * @author Oleksii Orel
 */
export class CheEditor implements ng.IDirective {

  restrict: string = 'E';
  templateUrl: string = 'components/widget/editor/che-editor.html';
  controller: string = 'CheEditorController';
  controllerAs: string = 'cheEditorController';
  bindToController: boolean = true;

  scope: any = {
    editorContent: '=',
    editorState: '=?',
    editorMode: '@?',
    validator: '&?',
    onContentChange: '&?'
  };
}
