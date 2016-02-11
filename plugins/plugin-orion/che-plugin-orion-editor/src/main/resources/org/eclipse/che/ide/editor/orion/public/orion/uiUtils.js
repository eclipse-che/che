/*******************************************************************************
 * @license
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 
 * (http://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution 
 * License v1.0 (http://www.eclipse.org/org/documents/edl-v10.html). 
 *
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

/*eslint-env browser, amd*/
define("uiUtils", [], function() {
	/**
	 * This class contains static utility methods. It is not intended to be instantiated.
	 * @class This class contains static utility methods.
	 * @name orion.uiUtils
	 */

	var isMac = navigator.platform.indexOf("Mac") !== -1; //$NON-NLS-0$

	var messages = {
		"KeyCTRL": "Ctrl",
		"KeySHIFT": "Shift",
		"KeyALT": "Alt",
		"KeyBKSPC": "Backspace",
		"KeyDEL": "Del",
		"KeyEND": "End",
		"KeyENTER": "Enter",
		"KeyESCAPE": "Esc",
		"KeyHOME": "Home",
		"KeyINSERT": "Ins",
		"KeyPAGEDOWN": "Page Down",
		"KeyPAGEUP": "Page Up",
		"KeySPACE": "Space",
		"KeyTAB": "Tab"
	};

	var KEY = {
		BKSPC: 8,
		TAB: 9,
		ENTER: 13,
		ESCAPE: 27,
		SPACE: 32,
		PAGEUP: 33,
		PAGEDOWN: 34,
		END: 35,
		HOME: 36,
		LEFT: 37,
		UP: 38,
		RIGHT: 39,
		DOWN: 40,
		INSERT: 45,
		DEL: 46
	};

	var KEY_CODE = Object.create(null);
	Object.keys(KEY).forEach(function(name) {
		KEY_CODE[KEY[name]] = name;
	});

	// Maps keyCode to display symbol
	var keySymbols = Object.create(null);
	keySymbols[KEY.DOWN]  = "\u2193"; //$NON-NLS-0$
	keySymbols[KEY.UP]    = "\u2191"; //$NON-NLS-0$
	keySymbols[KEY.RIGHT] = "\u2192"; //$NON-NLS-0$
	keySymbols[KEY.LEFT]  = "\u2190"; //$NON-NLS-0$
	if (isMac) {
		keySymbols[KEY.BKSPC]    = "\u232b"; //$NON-NLS-0$
		keySymbols[KEY.DEL]      = "\u2326"; //$NON-NLS-0$
		keySymbols[KEY.END]      = "\u21f2"; //$NON-NLS-0$
		keySymbols[KEY.ENTER]    = "\u23ce"; //$NON-NLS-0$
		keySymbols[KEY.ESCAPE]   = "\u238b"; //$NON-NLS-0$
		keySymbols[KEY.HOME]     = "\u21f1"; //$NON-NLS-0$
		keySymbols[KEY.PAGEDOWN] = "\u21df"; //$NON-NLS-0$
		keySymbols[KEY.PAGEUP]   = "\u21de"; //$NON-NLS-0$
		keySymbols[KEY.SPACE]    = "\u2423"; //$NON-NLS-0$
		keySymbols[KEY.TAB]      = "\u21e5"; //$NON-NLS-0$
	}

	function getKeyName(keyCode) {
		return KEY_CODE[keyCode] || null;
	}

	function getUserKeyStrokeString(binding) {
		var userString = "";

		if (isMac) {
			if (binding.mod4) {
				userString+= "\u2303"; //Ctrl //$NON-NLS-0$
			}
			if (binding.mod3) {
				userString+= "\u2325"; //Alt //$NON-NLS-0$
			}
			if (binding.mod2) {
				userString+= "\u21e7"; //Shift //$NON-NLS-0$
			}
			if (binding.mod1) {
				userString+= "\u2318"; //Command //$NON-NLS-0$
			}
		} else {
			var PLUS = "+"; //$NON-NLS-0$;
			if (binding.mod1)
				userString += messages.KeyCTRL + PLUS;
			if (binding.mod2)
				userString += messages.KeySHIFT + PLUS;
			if (binding.mod3)
				userString += messages.KeyALT + PLUS;
		}
		
		if (binding.alphaKey) {
			return userString+binding.alphaKey;
		}
		if (binding.type === "keypress") {
			return userString+binding.keyCode; 
		}

		// Check if it has a special symbol defined
		var keyCode = binding.keyCode;
		var symbol = keySymbols[keyCode];
		if (symbol) {
			return userString + symbol;
		}

		// Check if it's a known named key from KEY
		var keyName = getKeyName(keyCode);
		if (keyName) {
			// Some key names are translated, so check for that.
			keyName = messages["Key" + keyName] || keyName; //$NON-NLS-0$
			return userString + keyName;
		}

		var character;
		switch (binding.keyCode) {
			case 59:
				character = binding.mod2 ? ":" : ";"; //$NON-NLS-1$ //$NON-NLS-0$
				break;
			case 61:
				character = binding.mod2 ? "+" : "="; //$NON-NLS-1$ //$NON-NLS-0$
				break;
			case 188:
				character = binding.mod2 ? "<" : ","; //$NON-NLS-1$ //$NON-NLS-0$
				break;
			case 190:
				character = binding.mod2 ? ">" : "."; //$NON-NLS-1$ //$NON-NLS-0$
				break;
			case 191:
				character = binding.mod2 ? "?" : "/"; //$NON-NLS-1$ //$NON-NLS-0$
				break;
			case 192:
				character = binding.mod2 ? "~" : "`"; //$NON-NLS-1$ //$NON-NLS-0$
				break;
			case 219:
				character = binding.mod2 ? "{" : "["; //$NON-NLS-1$ //$NON-NLS-0$
				break;
			case 220:
				character = binding.mod2 ? "|" : "\\"; //$NON-NLS-1$ //$NON-NLS-0$
				break;
			case 221:
				character = binding.mod2 ? "}" : "]"; //$NON-NLS-1$ //$NON-NLS-0$
				break;
			case 222:
				character = binding.mod2 ? '"' : "'"; //$NON-NLS-1$ //$NON-NLS-0$
				break;
			}
		if (character) {
			return userString+character;
		}
		if (binding.keyCode >= 112 && binding.keyCode <= 123) {
			return userString+"F"+ (binding.keyCode - 111); //$NON-NLS-0$
		}
		return userString+String.fromCharCode(binding.keyCode);
	}

	function getUserKeyString(binding) {
		var result = "";
		var keys = binding.getKeys();
		for (var i = 0; i < keys.length; i++) {
			if (i !== 0) {
				result += " "; //$NON-NLS-0$
			}
			result += getUserKeyStrokeString(keys[i]);
		}
		return result;
	}

	//return module exports
	return {
		getUserKeyString: getUserKeyString
	};
});
