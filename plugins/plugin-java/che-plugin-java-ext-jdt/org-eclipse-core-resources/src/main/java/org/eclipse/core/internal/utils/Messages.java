/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Serge Beauchamp (Freescale Semiconductor) - [252996] add resource filtering
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Group and Project Path Variable Support
 * Francis Lynch (Wind River) - [301563] Save and load tree snapshots
 * Martin Oberhuber (Wind River) - [306575] Save snapshot location with project
 *******************************************************************************/
package org.eclipse.core.internal.utils;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.core.internal.utils.messages"; //$NON-NLS-1$

	// dtree
	public static String dtree_immutable;
	public static String dtree_malformedTree;
	public static String dtree_missingChild;
	public static String dtree_notFound;
	public static String dtree_notImmutable;
	public static String dtree_reverse;
	public static String dtree_subclassImplement;
	public static String dtree_switchError;

	// events
	public static String events_builderError;
	public static String events_building_0;
	public static String events_building_1;
	public static String events_errors;
	public static String events_instantiate_1;
	public static String events_invoking_1;
	public static String events_invoking_2;
	public static String events_skippingBuilder;
	public static String events_unknown;

	public static String history_copyToNull;
	public static String history_copyToSelf;
	public static String history_errorContentDescription;
	public static String history_notValid;
	public static String history_problemsCleaning;

	public static String links_creating;
	public static String links_errorLinkReconcile;
	public static String links_invalidLocation;
	public static String links_localDoesNotExist;
	public static String links_locationOverlapsLink;
	public static String links_locationOverlapsProject;
	public static String links_natureVeto;
	public static String links_noPath;
	public static String links_overlappingResource;
	public static String links_parentNotAccessible;
	public static String links_notFileFolder;
	public static String links_updatingDuplicate;
	public static String links_vetoNature;
	public static String links_workspaceVeto;
	public static String links_wrongLocalType;
	public static String links_resourceIsNotALink;
	public static String links_setLocation;

	public static String group_invalidParent;
	public static String filters_missingFilterType;

	// local store
	public static String localstore_copying;
	public static String localstore_copyProblem;
	public static String localstore_couldnotDelete;
	public static String localstore_couldNotMove;
	public static String localstore_couldNotRead;
	public static String localstore_couldNotWrite;
	public static String localstore_couldNotWriteReadOnly;
	public static String localstore_deleteProblem;
	public static String localstore_deleting;
	public static String localstore_failedReadDuringWrite;
	public static String localstore_fileExists;
	public static String localstore_fileNotFound;
	public static String localstore_locationUndefined;
	public static String localstore_refreshing;
	public static String localstore_refreshingRoot;
	public static String localstore_resourceExists;
	public static String localstore_resourceDoesNotExist;
	public static String localstore_resourceIsOutOfSync;

	// resource mappings and models
	public static String mapping_invalidDef;
	public static String mapping_wrongType;
	public static String mapping_noIdentifier;
	public static String mapping_validate;
	public static String mapping_multiProblems;

	// internal.resources
	public static String natures_duplicateNature;
	public static String natures_hasCycle;
	public static String natures_invalidDefinition;
	public static String natures_invalidRemoval;
	public static String natures_invalidSet;
	public static String natures_missingIdentifier;
	public static String natures_missingNature;
	public static String natures_missingPrerequisite;
	public static String natures_multipleSetMembers;

	public static String pathvar_beginLetter;
	public static String pathvar_invalidChar;
	public static String pathvar_invalidValue;
	public static String pathvar_length;
	public static String pathvar_undefined;
	public static String pathvar_whitespace;

	public static String preferences_deleteException;
	public static String preferences_loadException;
	public static String preferences_operationCanceled;
	public static String preferences_removeNodeException;
	public static String preferences_clearNodeException;
	public static String preferences_saveProblems;
	public static String preferences_syncException;

	public static String projRead_badArguments;
	public static String projRead_badFilterName;
	public static String projRead_badFilterID;
	public static String projRead_badFilterType;
	public static String projRead_badFilterType2;
	public static String projRead_badID;
	public static String projRead_badLinkLocation;
	public static String projRead_badLinkName;
	public static String projRead_badLinkType;
	public static String projRead_badLinkType2;
	public static String projRead_badLocation;
	public static String projRead_badSnapshotLocation;
	public static String projRead_cannotReadSnapshot;
	public static String projRead_emptyFilterName;
	public static String projRead_emptyLinkName;
	public static String projRead_emptyVariableName;
	public static String projRead_failureReadingProjectDesc;
	public static String projRead_notProjectDescription;
	public static String projRead_whichKey;
	public static String projRead_whichValue;
	public static String projRead_missingProjectName;
	
	public static String properties_couldNotClose;
	public static String properties_qualifierIsNull;
	public static String properties_readProperties;
	public static String properties_valueTooLong;

	// auto-refresh
	public static String refresh_installError;
	public static String refresh_jobName;
	public static String refresh_pollJob;
	public static String refresh_refreshErr;
	public static String refresh_task;

	public static String resources_cannotModify;
	public static String resources_changeInAdd;
	public static String resources_charsetBroadcasting;
	public static String resources_charsetUpdating;
	public static String resources_closing_0;
	public static String resources_closing_1;
	public static String resources_copyDestNotSub;
	public static String resources_copying;
	public static String resources_copying_0;
	public static String resources_copyNotMet;
	public static String resources_copyProblem;
	public static String resources_couldnotDelete;
	public static String resources_create;
	public static String resources_creating;
	public static String resources_deleteMeta;
	public static String resources_deleteProblem;
	public static String resources_deleting;
	public static String resources_deleting_0;
	public static String resources_destNotNull;
	public static String resources_errorContentDescription;
	public static String resources_errorDeleting;
	public static String resources_errorMarkersDelete;
	public static String resources_errorMarkersMove;
	public static String resources_wrongMarkerAttributeValueType;
	public static String resources_errorMembers;
	public static String resources_errorMoving;
	public static String resources_errorMultiRefresh;
	public static String resources_errorNature;
	public static String resources_errorPropertiesMove;
	public static String resources_errorReadProject;
	public static String resources_errorRefresh;
	public static String resources_errorValidator;
	public static String resources_errorVisiting;
	public static String resources_existsDifferentCase;
	public static String resources_existsLocalDifferentCase;
	public static String resources_exMasterTable;
	public static String resources_exReadProjectLocation;
	public static String resources_exSafeRead;
	public static String resources_exSafeSave;
	public static String resources_exSaveMaster;
	public static String resources_exSaveProjectLocation;
	public static String resources_fileExists;
	public static String resources_fileToProj;
	public static String resources_flushingContentDescriptionCache;
	public static String resources_folderOverFile;
	public static String resources_format;
	public static String resources_initHook;
	public static String resources_initTeamHook;
	public static String resources_initValidator;
	public static String resources_invalidCharInName;
	public static String resources_invalidCharInPath;
	public static String resources_invalidName;
	public static String resources_invalidPath;
	public static String resources_invalidProjDesc;
	public static String resources_invalidResourceName;
	public static String resources_invalidRoot;
	public static String resources_markerNotFound;
	public static String resources_missingProjectMeta;
	public static String resources_missingProjectMetaRepaired;
	public static String resources_moveDestNotSub;
	public static String resources_moveMeta;
	public static String resources_moveNotMet;
	public static String resources_moveNotProject;
	public static String resources_moveProblem;
	public static String resources_moveRoot;
	public static String resources_moving;
	public static String resources_moving_0;
	public static String resources_mustBeAbsolute;
	public static String resources_mustBeLocal;
	public static String resources_mustBeOpen;
	public static String resources_mustExist;
	public static String resources_mustNotExist;
	public static String resources_nameEmpty;
	public static String resources_nameNull;
	public static String resources_natureClass;
	public static String resources_natureDeconfig;
	public static String resources_natureExtension;
	public static String resources_natureFormat;
	public static String resources_natureImplement;
	public static String resources_notChild;
	public static String resources_oneHook;
	public static String resources_oneTeamHook;
	public static String resources_oneValidator;
	public static String resources_opening_1;
	public static String resources_overlapWorkspace;
	public static String resources_overlapProject;
	public static String resources_pathNull;
	public static String resources_projectDesc;
	public static String resources_projectDescSync;
	public static String resources_projectMustNotBeOpen;
	public static String resources_projectPath;
	public static String resources_pruningHistory;
	public static String resources_reading;
	public static String resources_readingEncoding;
	public static String resources_readingSnap;
	public static String resources_readMarkers;
	public static String resources_readMeta;
	public static String resources_readMetaWrongVersion;
	public static String resources_readOnly;
	public static String resources_readOnly2;
	public static String resources_readProjectMeta;
	public static String resources_readProjectTree;
	public static String resources_readSync;
	public static String resources_readWorkspaceMeta;
	public static String resources_readWorkspaceMetaValue;
	public static String resources_readWorkspaceSnap;
	public static String resources_readWorkspaceTree;
	public static String resources_refreshing;
	public static String resources_refreshingRoot;
	public static String resources_resetMarkers;
	public static String resources_resetSync;
	public static String resources_resourcePath;
	public static String resources_saveOp;
	public static String resources_saveProblem;
	public static String resources_saveWarnings;
	public static String resources_saving_0;
	public static String resources_savingEncoding;
	public static String resources_setDesc;
	public static String resources_setLocal;
	public static String resources_settingCharset;
	public static String resources_settingContents;
	public static String resources_settingDefaultCharsetContainer;
	public static String resources_settingDerivedFlag;
	public static String resources_shutdown;
	public static String resources_shutdownProblems;
	public static String resources_snapInit;
	public static String resources_snapRead;
	public static String resources_snapRequest;
	public static String resources_snapshot;
	public static String resources_startupProblems;
	public static String resources_touch;
	public static String resources_updating;
	public static String resources_updatingEncoding;
	public static String resources_workspaceClosed;
	public static String resources_workspaceOpen;
	public static String resources_writeMeta;
	public static String resources_writeWorkspaceMeta;
	public static String resources_errorResourceIsFiltered;

	public static String synchronizer_partnerNotRegistered;

	// URL
	public static String url_badVariant;
	public static String url_couldNotResolve_projectDoesNotExist;
	public static String url_couldNotResolve_URLProtocolHandlerCanNotResolveURL;
	public static String url_couldNotResolve_resourceLocationCanNotBeDetermined;

	// utils
	public static String utils_clone;
	public static String utils_stringJobName;
	// watson
	public static String watson_elementNotFound;
	public static String watson_illegalSubtree;
	public static String watson_immutable;
	public static String watson_noModify;
	public static String watson_nullArg;
	public static String watson_unknown;

	// auto-refresh win32 native
	public static String WM_beginTask;
	public static String WM_errCloseHandle;
	public static String WM_errCreateHandle;
	public static String WM_errFindChange;
	public static String WM_errors;
	public static String WM_jobName;
	public static String WM_nativeErr;

	static {
		// initialize resource bundles
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
