/**
 * ***************************************************************************** Copyright (c) 2007,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.search;

import java.util.ArrayList;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.ui.JavaElementLabels;

public class MatchLocations {

  //	public static class MatchLocationSelectionDialog extends TrayDialog {
  //
  //		private final ArrayList<Button> fButtons;
  //		private final int fSearchFor;
  //		private int fCurrentSelection;
  //
  //		public MatchLocationSelectionDialog(Shell parent, int initialSelection, int searchFor) {
  //			super(parent);
  //			fSearchFor= searchFor;
  //			fButtons= new ArrayList<Button>();
  //			fCurrentSelection= initialSelection;
  //		}
  //
  //		/* (non-Javadoc)
  //		 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
  //		 */
  //		@Override
  //		protected void configureShell(Shell shell) {
  //			super.configureShell(shell);
  //			shell.setText(SearchMessages.MatchLocations_dialog_title);
  //		}
  //
  //		/* (non-Javadoc)
  //		 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
  //		 */
  //		@Override
  //		protected boolean isResizable() {
  //			return true;
  //		}
  //
  //		/* (non-Javadoc)
  //		 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
  //		 */
  //		@Override
  //		protected Control createDialogArea(Composite parent) {
  //			Composite contents= (Composite) super.createDialogArea(parent);
  //			GridLayout layout= (GridLayout) contents.getLayout();
  //			layout.numColumns= 2;
  //			layout.makeColumnsEqualWidth= true;
  //
  //			Label label= new Label(contents, SWT.NONE);
  //			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
  //			label.setText(SearchMessages.MatchLocations_dialog_description);
  //
  //			if (fSearchFor == IJavaSearchConstants.TYPE) {
  //				createTypeMatchLocationsControls(contents);
  //			} else {
  //				createMethodFieldMatchLocationsControls(contents);
  //			}
  //
  //			Composite buttonComposite= new Composite(contents, SWT.NONE);
  //			buttonComposite.setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, true, true, 2, 1));
  //			GridLayout blayout= new GridLayout(2, false);
  //			blayout.marginWidth= 0;
  //			blayout.marginHeight= 0;
  //			buttonComposite.setLayout(blayout);
  //
  //			Button selectAllButton= new Button(buttonComposite, SWT.PUSH);
  //			selectAllButton.setLayoutData(new GridData());
  //			selectAllButton.setText(SearchMessages.MatchLocations_select_all_button_label);
  //			selectAllButton.addSelectionListener(new SelectionAdapter() {
  //				@Override
  //				public void widgetDefaultSelected(SelectionEvent e) {
  //					performSelectAction(true);
  //				}
  //				@Override
  //				public void widgetSelected(SelectionEvent e) {
  //					performSelectAction(true);
  //				}
  //			});
  //
  //			SWTUtil.setButtonDimensionHint(selectAllButton);
  //
  //			Button deselectAllButton= new Button(buttonComposite, SWT.PUSH);
  //			deselectAllButton.setLayoutData(new GridData());
  //			deselectAllButton.setText(SearchMessages.MatchLocations_deselect_all_button_label);
  //			deselectAllButton.addSelectionListener(new SelectionAdapter() {
  //				@Override
  //				public void widgetDefaultSelected(SelectionEvent e) {
  //					performSelectAction(false);
  //				}
  //				@Override
  //				public void widgetSelected(SelectionEvent e) {
  //					performSelectAction(false);
  //				}
  //			});
  //			SWTUtil.setButtonDimensionHint(deselectAllButton);
  //
  //			Dialog.applyDialogFont(contents);
  //
  //			return contents;
  //		}
  //
  //		private void createMethodFieldMatchLocationsControls(Composite contents) {
  //
  //			Composite composite= new Composite(contents, SWT.NONE);
  //			GridData gd= new GridData(SWT.LEFT, SWT.BEGINNING, true, true, 2, 1);
  //			gd.minimumWidth= convertHorizontalDLUsToPixels(200);
  //			composite.setLayoutData(gd);
  //			GridLayout blayout= new GridLayout(1, false);
  //			blayout.marginWidth= 0;
  //			blayout.marginHeight= 0;
  //			composite.setLayout(blayout);
  //
  //			if (fSearchFor == IJavaSearchConstants.METHOD || fSearchFor == IJavaSearchConstants.FIELD) {
  //				createButton(composite, SearchMessages.MatchLocations_this_label,
  // IJavaSearchConstants.THIS_REFERENCE);
  //				createButton(composite, SearchMessages.MatchLocations_implicit_this_label,
  // IJavaSearchConstants.IMPLICIT_THIS_REFERENCE);
  //
  //				createButton(composite, SearchMessages.MatchLocations_super_label,
  // IJavaSearchConstants.SUPER_REFERENCE);
  //				createButton(composite, SearchMessages.MatchLocations_qualified_label,
  // IJavaSearchConstants.QUALIFIED_REFERENCE);
  //			}
  //
  //			if (fSearchFor == IJavaSearchConstants.METHOD || fSearchFor ==
  // IJavaSearchConstants.CONSTRUCTOR) {
  //				createButton(composite, SearchMessages.MatchLocations_method_reference_label,
  // IJavaSearchConstants.METHOD_REFERENCE_EXPRESSION);
  //			}
  //		}
  //
  //		private void createTypeMatchLocationsControls(Composite contents) {
  //			Group group= new Group(contents, SWT.NONE);
  //			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2));
  //			group.setLayout(new GridLayout(1, false));
  //			group.setText(SearchMessages.MatchLocations_declaration_group_label);
  //
  //			createButton(group, SearchMessages.MatchLocations_imports_label,
  // IJavaSearchConstants.IMPORT_DECLARATION_TYPE_REFERENCE);
  //			createButton(group, SearchMessages.MatchLocations_super_types_label,
  // IJavaSearchConstants.SUPERTYPE_TYPE_REFERENCE);
  //			addSeparator(group);
  //
  //			createButton(group, SearchMessages.MatchLocations_annotations_label ,
  // IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE);
  //			addSeparator(group);
  //
  //			createButton(group, SearchMessages.MatchLocations_field_types_label,
  // IJavaSearchConstants.FIELD_DECLARATION_TYPE_REFERENCE);
  //			createButton(group, SearchMessages.MatchLocations_local_types_label,
  // IJavaSearchConstants.LOCAL_VARIABLE_DECLARATION_TYPE_REFERENCE);
  //			addSeparator(group);
  //
  //			createButton(group, SearchMessages.MatchLocations_method_types_label,
  // IJavaSearchConstants.RETURN_TYPE_REFERENCE);
  //			createButton(group, SearchMessages.MatchLocations_parameter_types_label,
  // IJavaSearchConstants.PARAMETER_DECLARATION_TYPE_REFERENCE);
  //			createButton(group, SearchMessages.MatchLocations_thrown_exceptions_label,
  // IJavaSearchConstants.THROWS_CLAUSE_TYPE_REFERENCE);
  //
  //			Group ptGroup= new Group(contents, SWT.NONE);
  //			ptGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
  //			ptGroup.setLayout(new GridLayout(1, false));
  //			ptGroup.setText(SearchMessages.MatchLocations_in_parameterized_types_group_label);
  //
  //			createButton(ptGroup, SearchMessages.MatchLocations_type_parameter_bounds_label,
  // IJavaSearchConstants.TYPE_VARIABLE_BOUND_TYPE_REFERENCE);
  //			createButton(ptGroup, SearchMessages.MatchLocations_wildcard_bounds_label,
  // IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE);
  //
  //			createButton(ptGroup, SearchMessages.MatchLocations_type_arguments_label,
  // IJavaSearchConstants.TYPE_ARGUMENT_TYPE_REFERENCE);
  //
  //			Group statementGroup= new Group(contents, SWT.NONE);
  //			statementGroup.setLayoutData(new GridData(SWT.FILL, SWT.END, true, false));
  //			statementGroup.setLayout(new GridLayout(1, false));
  //			statementGroup.setText(SearchMessages.MatchLocations_expression_group_label);
  //
  //			createButton(statementGroup, SearchMessages.MatchLocations_casts_label,
  // IJavaSearchConstants.CAST_TYPE_REFERENCE);
  //			createButton(statementGroup, SearchMessages.MatchLocations_catch_clauses_label,
  // IJavaSearchConstants.CATCH_TYPE_REFERENCE);
  //			addSeparator(statementGroup);
  //			createButton(statementGroup, SearchMessages.MatchLocations_class_instance_label,
  // IJavaSearchConstants.CLASS_INSTANCE_CREATION_TYPE_REFERENCE);
  //			createButton(statementGroup, SearchMessages.MatchLocations_instanceof_label,
  // IJavaSearchConstants.INSTANCEOF_TYPE_REFERENCE);
  //		}
  //
  //		protected final void performSelectAction(boolean selectAll) {
  //			for (int i= 0; i < fButtons.size(); i++) {
  //				Button button= fButtons.get(i);
  //				button.setSelection(selectAll);
  //			}
  //			validateSettings();
  //		}
  //
  //		private void addSeparator(Composite parent) {
  //			Label label= new Label(parent, SWT.NONE);
  //			GridData data= new GridData();
  //			data.heightHint= 4;
  //			label.setLayoutData(data);
  //		}
  //
  //
  //		private Button createButton(Composite parent, String text, int data) {
  //			boolean isSelected= (fCurrentSelection & data) != 0;
  //
  //			Button button= new Button(parent, SWT.CHECK);
  //			button.setText(text);
  //			button.setData(new Integer(data));
  //			button.setLayoutData(new GridData());
  //			button.setSelection(isSelected);
  //			button.addSelectionListener(new SelectionAdapter() {
  //				@Override
  //				public void widgetDefaultSelected(SelectionEvent e) {
  //					performOptionChanged();
  //				}
  //				@Override
  //				public void widgetSelected(SelectionEvent e) {
  //					widgetDefaultSelected(e);
  //				}
  //			});
  //
  //
  //			fButtons.add(button);
  //
  //			return button;
  //		}
  //
  //		private int getIntValue(Button button) {
  //			Integer bData= (Integer) button.getData();
  //			if (bData != null) {
  //				return bData.intValue();
  //			}
  //			return 0;
  //		}
  //
  //		protected final void performOptionChanged() {
  //			validateSettings();
  //		}
  //
  //		private void validateSettings() {
  //			int selected= 0;
  //			for (int i= 0; i < fButtons.size(); i++) {
  //				Button button= fButtons.get(i);
  //				if (button.getSelection()) {
  //					selected |= getIntValue(button);
  //				}
  //			}
  //			fCurrentSelection= selected;
  //
  //			getButton(IDialogConstants.OK_ID).setEnabled(selected != 0);
  //		}
  //
  //		public int getCurrentSelection() {
  //			return fCurrentSelection;
  //		}
  //	}

  public static String getMatchLocationDescription(int locations, int entryLimit) {
    int nOptions = getNumberOfSelectedSettings(locations, entryLimit);
    if (nOptions > entryLimit) {
      return SearchMessages.MatchLocations_match_locations_description;
    }
    ArrayList<String> args = new ArrayList<String>(3);
    if (isSet(locations, IJavaSearchConstants.IMPORT_DECLARATION_TYPE_REFERENCE)) {
      args.add(SearchMessages.MatchLocations_imports_description);
    }
    if (isSet(locations, IJavaSearchConstants.SUPERTYPE_TYPE_REFERENCE)) {
      args.add(SearchMessages.MatchLocations_super_types_description);
    }
    if (isSet(locations, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE)) {
      args.add(SearchMessages.MatchLocations_annotations_description);
    }
    if (isSet(locations, IJavaSearchConstants.FIELD_DECLARATION_TYPE_REFERENCE)) {
      args.add(SearchMessages.MatchLocations_field_types_description);
    }
    if (isSet(locations, IJavaSearchConstants.LOCAL_VARIABLE_DECLARATION_TYPE_REFERENCE)) {
      args.add(SearchMessages.MatchLocations_local_types_description);
    }
    if (isSet(locations, IJavaSearchConstants.RETURN_TYPE_REFERENCE)) {
      args.add(SearchMessages.MatchLocations_method_types_description);
    }
    if (isSet(locations, IJavaSearchConstants.PARAMETER_DECLARATION_TYPE_REFERENCE)) {
      args.add(SearchMessages.MatchLocations_parameter_types_description);
    }
    if (isSet(locations, IJavaSearchConstants.THROWS_CLAUSE_TYPE_REFERENCE)) {
      args.add(SearchMessages.MatchLocations_thrown_exceptions_description);
    }
    if (isSet(locations, IJavaSearchConstants.TYPE_VARIABLE_BOUND_TYPE_REFERENCE)) {
      args.add(SearchMessages.MatchLocations_type_parameter_bounds_description);
    }
    if (isSet(locations, IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE)) {
      args.add(SearchMessages.MatchLocations_wildcard_bounds_description);
    }
    if (isSet(locations, IJavaSearchConstants.INSTANCEOF_TYPE_REFERENCE)) {
      args.add(SearchMessages.MatchLocations_instanceof_description);
    }
    if (isSet(locations, IJavaSearchConstants.TYPE_ARGUMENT_TYPE_REFERENCE)) {
      args.add(SearchMessages.MatchLocations_type_arguments_description);
    }
    if (isSet(locations, IJavaSearchConstants.CAST_TYPE_REFERENCE)) {
      args.add(SearchMessages.MatchLocations_casts_description);
    }
    if (isSet(locations, IJavaSearchConstants.CATCH_TYPE_REFERENCE)) {
      args.add(SearchMessages.MatchLocations_catch_clauses_description);
    }
    if (isSet(locations, IJavaSearchConstants.CLASS_INSTANCE_CREATION_TYPE_REFERENCE)) {
      args.add(SearchMessages.MatchLocations_class_instance_description);
    }
    if (isSet(locations, IJavaSearchConstants.SUPER_REFERENCE)) {
      args.add(SearchMessages.MatchLocations_super_description);
    }
    if (isSet(locations, IJavaSearchConstants.QUALIFIED_REFERENCE)) {
      args.add(SearchMessages.MatchLocations_qualified_description);
    }
    if (isSet(locations, IJavaSearchConstants.METHOD_REFERENCE_EXPRESSION)) {
      args.add(SearchMessages.MatchLocations_method_reference_description);
    }
    if (isSet(locations, IJavaSearchConstants.THIS_REFERENCE)) {
      args.add(SearchMessages.MatchLocations_this_description);
    }
    if (isSet(locations, IJavaSearchConstants.IMPLICIT_THIS_REFERENCE)) {
      args.add(SearchMessages.MatchLocations_implicit_this_description);
    }
    if (args.size() == 1) {
      return args.get(0);
    }
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < args.size(); i++) {
      if (i > 0) {
        buf.append(JavaElementLabels.COMMA_STRING);
      }
      buf.append(args.get(i));
    }
    return buf.toString();
  }

  private static boolean isSet(int flags, int flag) {
    return (flags & flag) != 0;
  }

  public static int getTotalNumberOfSettings(int searchFor) {
    if (searchFor == IJavaSearchConstants.TYPE) {
      return 15;
    } else if (searchFor == IJavaSearchConstants.CONSTRUCTOR) {
      return 1;
    } else if (searchFor == IJavaSearchConstants.METHOD) {
      return 5;
    } else if (searchFor == IJavaSearchConstants.FIELD) {
      return 4;
    }
    return 0;
  }

  public static int getNumberOfSelectedSettings(int locations, int searchFor) {
    int count = 0;
    if (searchFor == IJavaSearchConstants.TYPE) {

      if (isSet(locations, IJavaSearchConstants.IMPORT_DECLARATION_TYPE_REFERENCE)) {
        count++;
      }
      if (isSet(locations, IJavaSearchConstants.SUPERTYPE_TYPE_REFERENCE)) {
        count++;
      }
      if (isSet(locations, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE)) {
        count++;
      }
      if (isSet(locations, IJavaSearchConstants.FIELD_DECLARATION_TYPE_REFERENCE)) {
        count++;
      }
      if (isSet(locations, IJavaSearchConstants.LOCAL_VARIABLE_DECLARATION_TYPE_REFERENCE)) {
        count++;
      }
      if (isSet(locations, IJavaSearchConstants.RETURN_TYPE_REFERENCE)) {
        count++;
      }
      if (isSet(locations, IJavaSearchConstants.PARAMETER_DECLARATION_TYPE_REFERENCE)) {
        count++;
      }
      if (isSet(locations, IJavaSearchConstants.THROWS_CLAUSE_TYPE_REFERENCE)) {
        count++;
      }
      if (isSet(locations, IJavaSearchConstants.TYPE_VARIABLE_BOUND_TYPE_REFERENCE)) {
        count++;
      }
      if (isSet(locations, IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE)) {
        count++;
      }
      if (isSet(locations, IJavaSearchConstants.INSTANCEOF_TYPE_REFERENCE)) {
        count++;
      }
      if (isSet(locations, IJavaSearchConstants.TYPE_ARGUMENT_TYPE_REFERENCE)) {
        count++;
      }
      if (isSet(locations, IJavaSearchConstants.CAST_TYPE_REFERENCE)) {
        count++;
      }
      if (isSet(locations, IJavaSearchConstants.CATCH_TYPE_REFERENCE)) {
        count++;
      }
      if (isSet(locations, IJavaSearchConstants.CLASS_INSTANCE_CREATION_TYPE_REFERENCE)) {
        count++;
      }
    } else if (searchFor == IJavaSearchConstants.METHOD
        || searchFor == IJavaSearchConstants.FIELD) {
      if (isSet(locations, IJavaSearchConstants.SUPER_REFERENCE)) {
        count++;
      }
      if (isSet(locations, IJavaSearchConstants.QUALIFIED_REFERENCE)) {
        count++;
      }
      if (isSet(locations, IJavaSearchConstants.THIS_REFERENCE)) {
        count++;
      }
      if (isSet(locations, IJavaSearchConstants.IMPLICIT_THIS_REFERENCE)) {
        count++;
      }
    }
    if (searchFor == IJavaSearchConstants.METHOD || searchFor == IJavaSearchConstants.CONSTRUCTOR) {
      if (isSet(locations, IJavaSearchConstants.METHOD_REFERENCE_EXPRESSION)) {
        count++;
      }
    }
    return count;
  }
}
