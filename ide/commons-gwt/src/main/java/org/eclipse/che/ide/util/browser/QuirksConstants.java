/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.che.ide.util.browser;

import com.google.gwt.core.client.GWT;

/**
 * Collection of constants defining various browser quirky behaviours.
 *
 * <p>Each constant should be accompanied by a detailed comment, and a "Tested:" section detailing
 * which browsers and operating systems the quirk has been tested on, so that it's easy to know
 * what's untested as new browser versions come out, etc.
 *
 * <p>Sometimes an "Untested exceptions:" field is appropriate, to note exceptions to the "Tested:"
 * field, in particular if they are concerning and represent a reasonable doubt as to the
 * correctness of the field's value.
 *
 * <p>It is preferable to use the constants in this class than to have logic that switches on
 * explicit browser checks.
 *
 * @author danilatos@google.com (Daniel Danilatos)
 */
public final class QuirksConstants {

  /**
   * Whether we get DOM Mutation events
   *
   * <p>Tested: Safari 3-4, Firefox 3-3.5, Chrome 1-2, IE7, IE8
   *
   * <p>Will IE9 give us mutation events? probably not.
   */
  public static final boolean PROVIDES_MUTATION_EVENTS =
      UserAgent.isFirefox() || UserAgent.isWebkit();

  /**
   * Whether the browser left normalises the caret in most cases (There are exceptions, usually to
   * do with links).
   *
   * <p>Tested: Safari 3*, Safari 4 beta, Firefox 3.0, IE7, IE8
   */
  public static final boolean USUALLY_LEFT_NORMALISES = UserAgent.isWebkit();

  /**
   * Certain versions of webkit have a specific hack implemented in them, where they go against the
   * regular left-normalised behaviour at the end of an anchor boundary, if it has an href. They
   * still report the cursor as being left-normalised, but if the user types, text goes to the
   * right, outside the link.
   *
   * <p>Tested: All OS: Safari 3.2.1, Safari 4 beta, Chrome 1.0, Chrome 2.0 special sauce
   */
  public static final boolean LIES_ABOUT_CARET_AT_LINK_END_BOUNDARY =
      UserAgent.isWebkit() && UserAgent.isAtLeastVersion(528, 0);

  /**
   * Similar to {@link #LIES_ABOUT_CARET_AT_LINK_END_BOUNDARY}, but does not actually lie, just
   * doesn't like reporting carets as being inside link boundaries, and typing occurs outside as
   * well.
   *
   * <p>Tested: IE8 beta
   *
   * <p>TODO(danilatos): check IE7
   */
  public static final boolean DOES_NOT_LEFT_NORMALISE_AT_LINK_END_BOUNDARY = UserAgent.isIE();

  /**
   * If the user is typing, we always get a key event before the browser changes the dom.
   *
   * <p>Tested: All OS: Safari 3.2.1, 4 beta, Chrome 1, 2, Firefox 3, IE7, IE8
   *
   * <p>Untested exceptions: Any IME on Linux!
   */
  public static final boolean ALWAYS_GIVES_KEY_EVENT_BEFORE_CHANGING_DOM =
      UserAgent.isFirefox() || UserAgent.isIE7();

  /**
   * Whether we get the magic 229 keycode for IME key events, at least for the first one (sometimes
   * we don't get key events for subsequent mutations).
   *
   * <p>Tested: All OS: Safari 3.2.1, 4 beta, Chrome 1, 2, Firefox 3.0
   *
   * <p>Untested exceptions: Any IME on Linux!
   */
  public static final boolean CAN_TELL_WHEN_FIRST_KEY_EVENT_IS_IME =
      UserAgent.isIE() || UserAgent.isWebkit() || UserAgent.isWin();

  /**
   * Whether the old school Ctrl+Insert, Shift+Delete, Shift+Insert shortcuts for Copy, Cut, Paste
   * work.
   *
   * <p>Tested: All OS: Firefox 3, IE 7/8, Safari 3, Chrome 2
   *
   * <p>Untested exceptions: Safari on Windows
   */
  public static final boolean HAS_OLD_SCHOOL_CLIPBOARD_SHORTCUTS =
      UserAgent.isWin() || UserAgent.isLinux();

  // NOTE(danilatos): These selection constants, unless mentioned otherwise,
  // refer to selection boundaries (e.g. the start, or end, of a ranged
  // selection, or a collapsed selection).

  /**
   * Whether the selection is either cleared or correctly transformed by the browser in at least the
   * following scenarios: - textNode.insertData adds to the cursor location, if it is after the
   * deletion point - textNode.deleteData subtracts from the cursor location, if it is after the
   * deletion point
   *
   * <p>Tested: Safari 3.2.1, FF 3.0, 3.5, Chrome 1, IE7, IE8
   */
  public static final boolean OK_SELECTION_ACROSS_TEXT_NODE_DATA_CHANGES =
      UserAgent.isFirefox() || UserAgent.isIE();

  /**
   * Whether the selection is either cleared or correctly transformed by the browser when text nodes
   * are deleted.
   *
   * <p>Gecko/IE preserve, Webkit clears selection. Both OK behaviours.
   *
   * <p>Tested: Safari 3.2.1, FF 3.0, 3.5, Chrome 1, IE7, IE8
   */
  public static final boolean OK_SELECTION_ACROSS_NODE_REMOVALS = true;

  /**
   * Whether the browser moves the selection into the neighbouring text node after a text node split
   * before the selection point, or at least clears the selection.
   *
   * <p>Tested: Safari 3.2.1, FF 3.0, 3.5, Chrome 1, IE7, IE8
   */
  public static final boolean OK_SELECTION_ACROSS_TEXT_NODE_SPLITS = UserAgent.isIE();

  /**
   * In this case, only clearing occurs by Webkit. Other two browsers move the selection to the
   * point where the moved node was. Which is BAD for wrapping!
   *
   * <p>Tested: Safari 3.2.1, FF 3.0, 3.5, Chrome 1, IE7, IE8
   *
   * @see #PRESERVES_SEMANTIC_SELECTION_ACROSS_MUTATIONS_OR_CLEARS_IT
   */
  public static final boolean OK_SELECTION_ACROSS_MOVES = UserAgent.isWebkit();

  /**
   * Preserves changes to text nodes made by calling methods on the text nodes directly (i.e. not
   * moving or deleting the text nodes).
   */
  public static final boolean PRESERVES_SEMANTIC_SELECTION_ACROSS_INTRINSIC_TEXT_NODE_CHANGES =
      OK_SELECTION_ACROSS_TEXT_NODE_DATA_CHANGES && OK_SELECTION_ACROSS_TEXT_NODE_SPLITS;

  /**
   * Whether the selection preservation is completely reliable across mutations in terms of
   * correctness. It might get cleared in some circumstances, but that's OK, we can just check if
   * the selection is gone and restore it. We don't need to transform it for correctness.
   *
   * <p>The biggest problem here is wrap. Currently implemented with insertBefore, it breaks
   * selections in all browsers, even IE, AND IE doesn't clear the selection, just moves it. Damn!
   * Anyway, there might be a smarter way to implement wrap - perhaps using an exec command to apply
   * a styling to a region and using the resulting wrapper nodes... but that's a long way off.
   *
   * <p>Tested: Safari 3.2.1, FF 3.0, 3.5, Chrome 1, IE7, IE8
   */
  public static final boolean PRESERVES_SEMANTIC_SELECTION_ACROSS_MUTATIONS_OR_CLEARS_IT =
      OK_SELECTION_ACROSS_TEXT_NODE_DATA_CHANGES
          && OK_SELECTION_ACROSS_TEXT_NODE_SPLITS
          && OK_SELECTION_ACROSS_MOVES;

  /**
   * Whether changing stuff in the middle of a ranged selection (that doesn't affect the selection
   * end points in any way), such as splitting some entirely contained text node, affects the ranged
   * selection. With firefox, it appears that the selection is still "correct", but visually new
   * text nodes inserted don't get highlighted as selected, which is bad.
   *
   * <p>Tested: Safari 3.2.1, FF 3.0, IE8
   */
  public static final boolean RANGED_SELECTION_AFFECTED_BY_INTERNAL_CHANGED = UserAgent.isFirefox();

  /**
   * True if IME input in not totally munged by adjacent text node mutations
   *
   * <p>Tested: Safari 3.2.1, FF 3.0, 3.5, Chrome 1, IE7, IE8
   */
  public static final boolean PRESERVES_IME_STATE_ACROSS_ADJACENT_CHANGES = UserAgent.isIE();

  /**
   * True if we can do the __proto__ override trick to remove defaults method in a JSO.
   * WARNING(dnailatos/reuben) Should be kept as static constant for speed reasons in string map
   * implementation.
   *
   * <p>Tested: Safari 4, FF 3.0, 3.5, Chrome 3-4, IE8
   */
  public static final boolean DOES_NOT_SUPPORT_JSO_PROTO_FIELD = UserAgent.isIE();

  /**
   * It appears that in some browsers, if you stopPropagation() an IME composition or contextmenu
   * event, the default action for the event is not executed (as if you had cancelled it)
   *
   * <p>TODO(danilatos): File a bug
   *
   * <p>Tested: FF 3.0
   *
   * <p>Untested: Everything else (at time of writing, nothing else has composition events...)
   */
  public static final boolean CANCEL_BUBBLING_CANCELS_IME_COMPOSITION_AND_CONTEXTMENU =
      UserAgent.isFirefox();

  /**
   * True if mouse events have rangeParent and rangeOffset fields.
   *
   * <p>Tested: FF 3.0, 3.6 Safari 4 Chrome 5
   */
  public static final boolean SUPPORTS_EVENT_GET_RANGE_METHODS = UserAgent.isFirefox();

  /**
   * True if preventDefault stops a native context menu from being shown. In firefox this is not the
   * case when dom.event.contextmenu.enabled is set.
   *
   * <p>Tested: FF 3.0, 3.6
   */
  public static final boolean PREVENT_DEFAULT_STOPS_CONTEXTMENT = !UserAgent.isFirefox();

  /**
   * True if displaying a context menu updates the current selection. Safari selects the word
   * clicked unless you click on the current selection, Firefox does not change the selection and
   * Chrome and IE clears the selection unless you click on the current selection.
   *
   * <p>The selection that is active on mousedown will, in all browsers, be the original selection
   * and the selection on the contextmenu event will be the new one.
   *
   * <p>Tested: FF 3.0, 3.5 Chrome 5 Safari 4 IE 8
   */
  public static final boolean CONTEXTMENU_SETS_SELECTION = !UserAgent.isFirefox();

  /**
   * True if the browser has the setBaseAndExtent JS method to allow better setting of the selection
   * within the browser.
   *
   * <p>So far, only webkit browsers have this in their API, and documentation is scarce. See:
   * http://developer.apple.com/DOCUMENTATION/AppleApplications/Reference/WebKitDOMRef
   * /DOMSelection_idl/Classes/DOMSelection/index.html#//apple_ref/idl/instm
   * /DOMSelection/setBaseAndExtent/void/(inNode,inlong,inNode,inlong)
   */
  public static final boolean HAS_BASE_AND_EXTENT = UserAgent.isWebkit();

  /**
   * Chrome on Mac generates doesn't keypress for command combos, only keydown.
   *
   * <p>In general, it makes sense to only fire the keypress event if the combo generates content.
   * https://bugs.webkit.org/show_bug.cgi?id=30397
   *
   * <p>However since it is the odd one out here, it is listed as a quirk.
   */
  public static final boolean COMMAND_COMBO_DOESNT_GIVE_KEYPRESS =
      UserAgent.isMac() && UserAgent.isChrome();

  /**
   * True if the browser has native support for getElementsByClassName.
   *
   * <p>Tested: Chrome, Safari 3.1, Firefox 3.0
   */
  public static final boolean SUPPORTS_GET_ELEMENTS_BY_CLASSNAME =
      GWT.isScript() && checkGetElementsByClassNameSupport();

  /**
   * True if the browser supports composition events.
   *
   * <p>(This does not differentiate between the per-browser composition event quirks, such as
   * whether they provide a text vs a compositionupdate event, or other glitches).
   *
   * <p>Tested: Chrome 3.0, 4.0; Safari 4; FF 3.0, 3.5; IE 7,8
   */
  public static final boolean SUPPORTS_COMPOSITION_EVENTS =
      UserAgent.isFirefox() || (UserAgent.isWebkit() && UserAgent.isAtLeastVersion(532, 5));

  /**
   * True if the browser does an extra modification of the DOM after the compositionend event, and
   * also fires a text input event if the composition was not cancelled.
   *
   * <p>Tested: Chrome 4.0; FF 3.0, 3.5;
   */
  public static final boolean MODIFIES_DOM_AND_FIRES_TEXTINPUT_AFTER_COMPOSITION =
      UserAgent.isWebkit(); // Put an upper bound on the version here when it's fixed...

  /**
   * True if the browser keeps the selection in an empty span after the app has programmatically set
   * it there.
   *
   * <p>Tested: Chrome 3.0, 4.0; Safari 3, 4; FF 3.0, 3.5; IE 7,8
   */
  public static final boolean SUPPORTS_CARET_IN_EMPTY_SPAN = UserAgent.isFirefox();

  /**
   * True if the browser automatically scrolls a contentEditable element into view when we set focus
   * on the element
   *
   * <p>Tested: Chrome 5.0.307.11 beta / linux, Safari 4.0.4 / mac, Firefox 3.0.7 + 3.6 / linux
   */
  public static final boolean ADJUSTS_SCROLL_TOP_WHEN_FOCUSING = UserAgent.isWebkit();

  /**
   * True if the browser does not emit a paste event for plaintext paste. This was a bug on Webkit
   * and has been fixed and pushed to Chrome 4+
   *
   * <p>Tested: Chrome 4.0.302.3; Safari 4.05 Mac
   */
  public static final boolean PLAINTEXT_PASTE_DOES_NOT_EMIT_PASTE_EVENT = UserAgent.isSafari();

  /**
   * True if the browser supports input type 'search'.
   *
   * <p>Tested: Chrome 9.0, Chrome 4.0
   */
  public static final boolean SUPPORTS_SEARCH_INPUT = UserAgent.isWebkit();

  /**
   * True if the browser sanitizes pasted content to contenteditable to prevent script execution.
   *
   * <p>Tested: Chrome 9.0, Safari 5, FF 3.5, FF 4.0
   */
  public static final boolean SANITIZES_PASTED_CONTENT =
      (UserAgent.isWebkit() && UserAgent.isAtLeastVersion(533, 16))
          || (UserAgent.isFirefox() && UserAgent.isAtLeastVersion(4, 0));

  private static native boolean checkGetElementsByClassNameSupport() /*-{
        return !!document.body.getElementsByClassName;
    }-*/;

  private QuirksConstants() {}
}
