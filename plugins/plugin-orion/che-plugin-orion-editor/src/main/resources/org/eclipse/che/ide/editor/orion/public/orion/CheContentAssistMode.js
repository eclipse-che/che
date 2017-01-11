/*
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
define("che/editor/contentAssist", [
    'orion/keyBinding',
    'orion/editor/keyModes',
    'orion/webui/littlelib'
], function(mKeyBinding, mKeyModes, lib){

    function CheContentAssistMode(textView){
        mKeyModes.KeyMode.call(this, textView)
    }
    CheContentAssistMode.prototype = new mKeyModes.KeyMode();

    CheContentAssistMode.prototype.createKeyBindings = function() {
            var KeyBinding = mKeyBinding.KeyBinding;
            var bindings = [];
            bindings.push({actionID: "cheContentAssistApply", keyBinding: new KeyBinding(13)}); //$NON-NLS-0$
            bindings.push({actionID: "cheContentAssistCancel", keyBinding: new KeyBinding(27)}); //$NON-NLS-0$
            bindings.push({actionID: "cheContentAssistNextProposal", keyBinding: new KeyBinding(40)}); //$NON-NLS-0$
            bindings.push({actionID: "cheContentAssistPreviousProposal", keyBinding: new KeyBinding(38)}); //$NON-NLS-0$
            bindings.push({actionID: "cheContentAssistNextPage", keyBinding: new KeyBinding(34)}); //$NON-NLS-0$
            bindings.push({actionID: "cheContentAssistPreviousPage", keyBinding: new KeyBinding(33)}); //$NON-NLS-0$
            bindings.push({actionID: "cheContentAssistHome", keyBinding: new KeyBinding(lib.KEY.HOME)}); //$NON-NLS-0$
            bindings.push({actionID: "cheContentAssistEnd", keyBinding: new KeyBinding(lib.KEY.END)}); //$NON-NLS-0$
            bindings.push({actionID: "cheContentAssistTab", keyBinding: new KeyBinding(9)}); //$NON-NLS-0$
            return bindings;
    };
    return {
        CheContentAssist : CheContentAssistMode
    };
});