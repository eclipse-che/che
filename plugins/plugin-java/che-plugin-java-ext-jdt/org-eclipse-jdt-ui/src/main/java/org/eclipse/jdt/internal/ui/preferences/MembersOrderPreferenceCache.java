/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.preferences;

import java.util.StringTokenizer;
import org.eclipse.jdt.core.Flags;

/** */
public class MembersOrderPreferenceCache /*implements IPropertyChangeListener*/ {
  private static final String STR = new String("T,SF,SI,SM,F,I,C,M");
  private static final String MODIFIERS = new String("B,V,R,D");
  public static final int TYPE_INDEX = 0;
  public static final int CONSTRUCTORS_INDEX = 1;
  public static final int METHOD_INDEX = 2;
  public static final int FIELDS_INDEX = 3;
  public static final int INIT_INDEX = 4;
  public static final int STATIC_FIELDS_INDEX = 5;
  public static final int STATIC_INIT_INDEX = 6;
  public static final int STATIC_METHODS_INDEX = 7;
  public static final int ENUM_CONSTANTS_INDEX = 8;
  public static final int N_CATEGORIES = ENUM_CONSTANTS_INDEX + 1;

  private static final int PUBLIC_INDEX = 0;
  private static final int PRIVATE_INDEX = 1;
  private static final int PROTECTED_INDEX = 2;
  private static final int DEFAULT_INDEX = 3;
  private static final int N_VISIBILITIES = DEFAULT_INDEX + 1;

  private int[] fCategoryOffsets = null;

  private boolean fSortByVisibility;
  private int[] fVisibilityOffsets = null;

  //	private IPreferenceStore fPreferenceStore;

  public MembersOrderPreferenceCache() {
    //		fPreferenceStore= null;
    fCategoryOffsets = null;
    fSortByVisibility = false;
    fVisibilityOffsets = null;
  }

  //	public void install(IPreferenceStore store) {
  //		fPreferenceStore= store;
  //		store.addPropertyChangeListener(this);
  //		fSortByVisibility=
  // store.getBoolean(PreferenceConstants.APPEARANCE_ENABLE_VISIBILITY_SORT_ORDER);
  //	}
  //
  //	public void dispose() {
  //		fPreferenceStore.removePropertyChangeListener(this);
  //		fPreferenceStore= null;
  //	}

  //	public static boolean isMemberOrderProperty(String property) {
  //		return PreferenceConstants.APPEARANCE_MEMBER_SORT_ORDER.equals(property)
  //			|| PreferenceConstants.APPEARANCE_VISIBILITY_SORT_ORDER.equals(property)
  //			|| PreferenceConstants.APPEARANCE_ENABLE_VISIBILITY_SORT_ORDER.equals(property);
  //	}
  //
  //	public void propertyChange(PropertyChangeEvent event) {
  //		String property= event.getProperty();
  //
  //		if (PreferenceConstants.APPEARANCE_MEMBER_SORT_ORDER.equals(property)) {
  //			fCategoryOffsets= null;
  //		} else if (PreferenceConstants.APPEARANCE_VISIBILITY_SORT_ORDER.equals(property)) {
  //			fVisibilityOffsets= null;
  //		} else if (PreferenceConstants.APPEARANCE_ENABLE_VISIBILITY_SORT_ORDER.equals(property)) {
  //			fSortByVisibility=
  // fPreferenceStore.getBoolean(PreferenceConstants.APPEARANCE_ENABLE_VISIBILITY_SORT_ORDER);
  //		}
  //	}
  //
  public int getCategoryIndex(int kind) {
    if (fCategoryOffsets == null) {
      fCategoryOffsets = getCategoryOffsets();
    }
    return fCategoryOffsets[kind];
  }

  private int[] getCategoryOffsets() {
    int[] offsets = new int[N_CATEGORIES];
    //		IPreferenceStore store= fPreferenceStore;
    //		String key= PreferenceConstants.APPEARANCE_MEMBER_SORT_ORDER;
    boolean success = fillCategoryOffsetsFromPreferenceString(STR, offsets);
    //		if (!success) {
    //			store.setToDefault(key);
    //			fillCategoryOffsetsFromPreferenceString(store.getDefaultString(key), offsets);
    //		}
    return offsets;
  }

  private boolean fillCategoryOffsetsFromPreferenceString(String str, int[] offsets) {
    StringTokenizer tokenizer = new StringTokenizer(str, ","); // $NON-NLS-1$
    int i = 0;
    offsets[ENUM_CONSTANTS_INDEX] = i++; // enum constants always on top

    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken().trim();
      if ("T".equals(token)) { // $NON-NLS-1$
        offsets[TYPE_INDEX] = i++;
      } else if ("M".equals(token)) { // $NON-NLS-1$
        offsets[METHOD_INDEX] = i++;
      } else if ("F".equals(token)) { // $NON-NLS-1$
        offsets[FIELDS_INDEX] = i++;
      } else if ("I".equals(token)) { // $NON-NLS-1$
        offsets[INIT_INDEX] = i++;
      } else if ("SF".equals(token)) { // $NON-NLS-1$
        offsets[STATIC_FIELDS_INDEX] = i++;
      } else if ("SI".equals(token)) { // $NON-NLS-1$
        offsets[STATIC_INIT_INDEX] = i++;
      } else if ("SM".equals(token)) { // $NON-NLS-1$
        offsets[STATIC_METHODS_INDEX] = i++;
      } else if ("C".equals(token)) { // $NON-NLS-1$
        offsets[CONSTRUCTORS_INDEX] = i++;
      }
    }
    return i == N_CATEGORIES;
  }

  public boolean isSortByVisibility() {
    return fSortByVisibility;
  }

  public int getVisibilityIndex(int modifierFlags) {
    if (fVisibilityOffsets == null) {
      fVisibilityOffsets = getVisibilityOffsets();
    }
    int kind = DEFAULT_INDEX;
    if (Flags.isPublic(modifierFlags)) {
      kind = PUBLIC_INDEX;
    } else if (Flags.isProtected(modifierFlags)) {
      kind = PROTECTED_INDEX;
    } else if (Flags.isPrivate(modifierFlags)) {
      kind = PRIVATE_INDEX;
    }

    return fVisibilityOffsets[kind];
  }

  private int[] getVisibilityOffsets() {
    int[] offsets = new int[N_VISIBILITIES];
    //		IPreferenceStore store= fPreferenceStore;
    //		String key= PreferenceConstants.APPEARANCE_VISIBILITY_SORT_ORDER;
    boolean success = fillVisibilityOffsetsFromPreferenceString(MODIFIERS, offsets);
    //		if (!success) {
    //			store.setToDefault(key);
    //			fillVisibilityOffsetsFromPreferenceString(store.getDefaultString(key), offsets);
    //		}
    return offsets;
  }

  private boolean fillVisibilityOffsetsFromPreferenceString(String str, int[] offsets) {
    StringTokenizer tokenizer = new StringTokenizer(str, ","); // $NON-NLS-1$
    int i = 0;
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken().trim();
      if ("B".equals(token)) { // $NON-NLS-1$
        offsets[PUBLIC_INDEX] = i++;
      } else if ("V".equals(token)) { // $NON-NLS-1$
        offsets[PRIVATE_INDEX] = i++;
      } else if ("R".equals(token)) { // $NON-NLS-1$
        offsets[PROTECTED_INDEX] = i++;
      } else if ("D".equals(token)) { // $NON-NLS-1$
        offsets[DEFAULT_INDEX] = i++;
      }
    }
    return i == N_VISIBILITIES;
  }
}
