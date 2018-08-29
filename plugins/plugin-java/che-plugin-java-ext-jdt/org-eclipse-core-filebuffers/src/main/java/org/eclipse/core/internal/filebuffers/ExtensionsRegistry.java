/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2009 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.internal.filebuffers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.content.IContentType;

/**
 * This registry manages sharable document factories and setup participants that are specified in
 * <code>plugin.xml</code>.
 */
public class ExtensionsRegistry {

  /**
   * Adapts {@link IContentType} with the ability to check equality. This allows to use them in a
   * collection.
   */
  private static class ContentTypeAdapter {

    /** The adapted content type. */
    private IContentType fContentType;

    /**
     * Creates a new content type adapter for the given content type.
     *
     * @param contentType the content type to be adapted
     */
    public ContentTypeAdapter(IContentType contentType) {
      Assert.isNotNull(contentType);
      fContentType = contentType;
    }

    /**
     * Return the id of the adapted content type.
     *
     * @return the id
     */
    public String getId() {
      return fContentType.getId();
    }

    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
      return obj instanceof ContentTypeAdapter
          && fContentType.getId().equals(((ContentTypeAdapter) obj).getId());
    }

    /*
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
      return fContentType.getId().hashCode();
    }
  }

  protected static final String WILDCARD = "*"; // $NON-NLS-1$

  /**
   * The mapping between file attributes and configuration elements describing document factories.
   */
  private Map fFactoryDescriptors = new HashMap();
  /**
   * The mapping between configuration elements for document factories and instantiated document
   * factories.
   */
  private Map fFactories = new HashMap();
  /**
   * The mapping between file attributes and configuration elements describing document setup
   * participants.
   */
  private Map fSetupParticipantDescriptors = new HashMap();
  /**
   * The mapping between configuration elements for setup participants and instantiated setup
   * participants.
   */
  private Map fSetupParticipants = new HashMap();
  /**
   * The mapping between file attributes and configuration elements describing annotation model
   * factories.
   */
  private Map fAnnotationModelFactoryDescriptors = new HashMap();
  /** The mapping between configuration elements for annotation model factories */
  private Map fAnnotationModelFactories = new HashMap();
  /** The content type manager. */
  //	protected IContentTypeManager fContentTypeManager= Platform.getContentTypeManager();

  /**
   * Creates a new document factory registry and initializes it with the information found in the
   * plug-in registry.
   */
  public ExtensionsRegistry() {
    //		initialize("documentCreation", "contentTypeId", true,  fFactoryDescriptors); //$NON-NLS-1$
    // //$NON-NLS-2$
    //		initialize("documentCreation", "fileNames", false, fFactoryDescriptors); //$NON-NLS-1$
    // //$NON-NLS-2$
    //		initialize("documentCreation", "extensions",  false, fFactoryDescriptors); //$NON-NLS-1$
    // //$NON-NLS-2$
    //		initialize("documentSetup", "contentTypeId", true, fSetupParticipantDescriptors);
    // //$NON-NLS-1$ //$NON-NLS-2$
    //		initialize("documentSetup", "fileNames", false, fSetupParticipantDescriptors); //$NON-NLS-1$
    // //$NON-NLS-2$
    //		initialize("documentSetup", "extensions", false, fSetupParticipantDescriptors);
    // //$NON-NLS-1$ //$NON-NLS-2$
    //		initialize("annotationModelCreation", "contentTypeId", true,
    // fAnnotationModelFactoryDescriptors); //$NON-NLS-1$ //$NON-NLS-2$
    //		initialize("annotationModelCreation", "fileNames", false,
    // fAnnotationModelFactoryDescriptors); //$NON-NLS-1$ //$NON-NLS-2$
    //		initialize("annotationModelCreation", "extensions", false,
    // fAnnotationModelFactoryDescriptors); //$NON-NLS-1$ //$NON-NLS-2$

  }

  /**
   * Reads the comma-separated value from the given configuration element for the given attribute
   * name and remembers the configuration element in the given map under the individual tokens of
   * the attribute value.
   *
   * @param attributeName the name of the attribute
   * @param element the configuration element
   * @param map the map which remembers the configuration element
   */
  private void read(String attributeName, IConfigurationElement element, Map map) {
    String value = element.getAttribute(attributeName);
    if (value != null) {
      StringTokenizer tokenizer = new StringTokenizer(value, ","); // $NON-NLS-1$
      while (tokenizer.hasMoreTokens()) {
        String token = tokenizer.nextToken().trim();

        Set s = (Set) map.get(token);
        if (s == null) {
          s = new HashSet();
          map.put(token, s);
        }
        s.add(element);
      }
    }
  }

  //	/**
  //	 * Reads the value from the given configuration element for the given attribute name and
  // remembers
  //	 * the configuration element in the given map under the individual content type of the
  // attribute value.
  //	 *
  //	 * @param attributeName the name of the attribute
  //	 * @param element the configuration element
  //	 * @param map the map which remembers the configuration element
  //	 */
  //	private void readContentType(String attributeName, IConfigurationElement element, Map map) {
  //		String value= element.getAttribute(attributeName);
  //		if (value != null) {
  //			IContentType contentType= fContentTypeManager.getContentType(value);
  //			if (contentType == null) {
  //				log(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, NLSUtility
  //						.format(FileBuffersMessages.ExtensionsRegistry_error_contentTypeDoesNotExist, value),
  // null));
  //				return;
  //			}
  //			ContentTypeAdapter adapter= new ContentTypeAdapter(contentType);
  //			Set s= (Set) map.get(adapter);
  //			if (s == null) {
  //				s= new HashSet();
  //				map.put(adapter, s);
  //			}
  //			s.add(element);
  //		}
  //	}

  /**
   * Adds an entry to the log of this plug-in for the given status
   *
   * @param status the status to log
   */
  private void log(IStatus status) {
    //		ILog log= FileBuffersPlugin.getDefault().getLog();
    ResourcesPlugin.log(status);
  }

  //	/**
  //	 * Initializes this registry. It retrieves all implementers of the given
  //	 * extension point and remembers those implementers based on the
  //	 * file name extensions in the given map.
  //	 *
  //	 * @param extensionPointName the name of the extension point
  //	 * @param childElementName the name of the child elements
  //	 * @param isContentTypeId the child element is a content type id
  //	 * @param descriptors the map to be filled
  //	 */
  //	private void initialize(String extensionPointName, String childElementName, boolean
  // isContentTypeId, Map descriptors) {
  //
  //		IExtensionPoint extensionPoint=
  // Platform.getExtensionRegistry().getExtensionPoint(FileBuffersPlugin.PLUGIN_ID,
  // extensionPointName);
  //		if (extensionPoint == null) {
  //			log(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, 0, NLSUtility
  //					.format(FileBuffersMessages.ExtensionsRegistry_error_extensionPointNotFound,
  // extensionPointName), null));
  //			return;
  //		}
  //
  //		IConfigurationElement[] elements= extensionPoint.getConfigurationElements();
  //		for (int i= 0; i < elements.length; i++) {
  //			if (isContentTypeId)
  //				readContentType(childElementName, elements[i], descriptors);
  //			else
  //				read(childElementName, elements[i], descriptors);
  //		}
  //	}

  /**
   * Returns the executable extension for the given configuration element. If there is no
   * instantiated extension remembered for this element, a new extension is created and put into the
   * cache if it is of the requested type.
   *
   * @param entry the configuration element
   * @param extensions the map of instantiated extensions
   * @param extensionType the requested result type
   * @return the executable extension for the given configuration element.
   */
  private Object getExtension(IConfigurationElement entry, Map extensions, Class extensionType) {
    Object extension = extensions.get(entry);
    if (extension != null) return extension;

    try {
      extension = entry.createExecutableExtension("class"); // $NON-NLS-1$
    } catch (CoreException x) {
      log(x.getStatus());
    }

    if (extensionType.isInstance(extension)) {
      extensions.put(entry, extension);
      return extension;
    }

    return null;
  }

  /**
   * Returns the first enumerated element of the given set.
   *
   * @param set the set from which to choose
   * @return the selected configuration element
   */
  private IConfigurationElement selectConfigurationElement(Set set) {
    if (set != null && !set.isEmpty()) {
      Iterator e = set.iterator();
      return (IConfigurationElement) e.next();
    }
    return null;
  }

  /**
   * Returns a sharable document factory for the given file name or file extension.
   *
   * @param nameOrExtension the name or extension to be used for lookup
   * @return the sharable document factory or <code>null</code>
   * @deprecated As of 3.5
   */
  protected org.eclipse.core.filebuffers.IDocumentFactory getDocumentFactory(
      String nameOrExtension) {
    Set set = (Set) fFactoryDescriptors.get(nameOrExtension);
    if (set != null) {
      IConfigurationElement entry = selectConfigurationElement(set);
      return (org.eclipse.core.filebuffers.IDocumentFactory)
          getExtension(entry, fFactories, org.eclipse.core.filebuffers.IDocumentFactory.class);
    }
    return null;
  }

  /**
   * Returns a sharable document factory for the given content types.
   *
   * @param contentTypes the content types used to find the factory
   * @return the sharable document factory or <code>null</code>
   * @deprecated As of 3.5
   */
  protected org.eclipse.core.filebuffers.IDocumentFactory doGetDocumentFactory(
      IContentType[] contentTypes) {
    Set set = null;
    int i = 0;
    while (i < contentTypes.length && set == null) {
      set = (Set) fFactoryDescriptors.get(new ContentTypeAdapter(contentTypes[i++]));
    }

    if (set != null) {
      IConfigurationElement entry = selectConfigurationElement(set);
      return (org.eclipse.core.filebuffers.IDocumentFactory)
          getExtension(entry, fFactories, org.eclipse.core.filebuffers.IDocumentFactory.class);
    }
    return null;
  }

  /**
   * Returns a sharable document factory for the given content types. This method considers the base
   * content types of the given set of content types.
   *
   * @param contentTypes the content types used to find the factory
   * @return the sharable document factory or <code>null</code>
   * @deprecated As of 3.5
   */
  protected org.eclipse.core.filebuffers.IDocumentFactory getDocumentFactory(
      IContentType[] contentTypes) {
    org.eclipse.core.filebuffers.IDocumentFactory factory = doGetDocumentFactory(contentTypes);
    while (factory == null) {
      contentTypes = computeBaseContentTypes(contentTypes);
      if (contentTypes == null) break;
      factory = doGetDocumentFactory(contentTypes);
    }
    return factory;
  }

  /**
   * Returns the set of setup participants for the given file name or extension.
   *
   * @param nameOrExtension the name or extension to be used for lookup
   * @return the sharable set of document setup participants
   */
  protected List getDocumentSetupParticipants(String nameOrExtension) {
    Set set = (Set) fSetupParticipantDescriptors.get(nameOrExtension);
    if (set == null) return null;

    List participants = new ArrayList();
    Iterator e = set.iterator();
    while (e.hasNext()) {
      IConfigurationElement entry = (IConfigurationElement) e.next();
      Object participant = getExtension(entry, fSetupParticipants, IDocumentSetupParticipant.class);
      if (participant != null) participants.add(participant);
    }

    return participants;
  }

  /**
   * Returns the set of setup participants for the given content types.
   *
   * @param contentTypes the contentTypes to be used for lookup
   * @return the sharable set of document setup participants
   */
  private List doGetDocumentSetupParticipants(IContentType[] contentTypes) {
    Set resultSet = new HashSet();
    int i = 0;
    while (i < contentTypes.length) {
      Set set = (Set) fSetupParticipantDescriptors.get(new ContentTypeAdapter(contentTypes[i++]));
      if (set != null) resultSet.addAll(set);
    }

    List participants = new ArrayList();
    Iterator e = resultSet.iterator();
    while (e.hasNext()) {
      IConfigurationElement entry = (IConfigurationElement) e.next();
      Object participant = getExtension(entry, fSetupParticipants, IDocumentSetupParticipant.class);
      if (participant != null) participants.add(participant);
    }

    return participants.isEmpty() ? null : participants;
  }

  /**
   * Returns the set of setup participants for the given content types. This method considers the
   * base content types of the given set of content types.
   *
   * @param contentTypes the contentTypes to be used for lookup
   * @return the sharable set of document setup participants
   */
  protected List getDocumentSetupParticipants(IContentType[] contentTypes) {
    List participants = doGetDocumentSetupParticipants(contentTypes);
    while (participants == null) {
      contentTypes = computeBaseContentTypes(contentTypes);
      if (contentTypes == null) break;
      participants = doGetDocumentSetupParticipants(contentTypes);
    }
    return participants;
  }
  //
  //	/**
  //	 * Returns a sharable annotation model factory for the given content types.
  //	 *
  //	 * @param contentTypes the content types used to find the factory
  //	 * @return the sharable annotation model factory or <code>null</code>
  //	 */
  //	private IAnnotationModelFactory doGetAnnotationModelFactory(IContentType[] contentTypes) {
  //		Set set= null;
  //		int i= 0;
  //		while (i < contentTypes.length && set == null) {
  //			set= (Set) fAnnotationModelFactoryDescriptors.get(new ContentTypeAdapter(contentTypes[i++]));
  //		}
  //
  //		if (set != null) {
  //			IConfigurationElement entry= selectConfigurationElement(set);
  //			return (IAnnotationModelFactory) getExtension(entry, fAnnotationModelFactories,
  // IAnnotationModelFactory.class);
  //		}
  //		return null;
  //	}

  //	/**
  //	 * Returns a sharable annotation model factory for the given content types.
  //	 * This method considers the base content types of the given set of content
  //	 * types.
  //	 *
  //	 * @param contentTypes the content types used to find the factory
  //	 * @return the sharable annotation model factory or <code>null</code>
  //	 */
  //	protected IAnnotationModelFactory getAnnotationModelFactory(IContentType[] contentTypes) {
  //		IAnnotationModelFactory factory= doGetAnnotationModelFactory(contentTypes);
  //		while (factory == null) {
  //			contentTypes= computeBaseContentTypes(contentTypes);
  //			if (contentTypes == null)
  //				break;
  //			factory= doGetAnnotationModelFactory(contentTypes);
  //		}
  //		return factory;
  //	}

  //	/**
  //	 * Returns a sharable annotation model factory for the given file name or file extension.
  //	 *
  //	 * @param extension the name or extension to be used for lookup
  //	 * @return the sharable document factory or <code>null</code>
  //	 */
  //	protected IAnnotationModelFactory getAnnotationModelFactory(String extension) {
  //		Set set= (Set) fAnnotationModelFactoryDescriptors.get(extension);
  //		if (set != null) {
  //			IConfigurationElement entry= selectConfigurationElement(set);
  //			return (IAnnotationModelFactory) getExtension(entry, fAnnotationModelFactories,
  // IAnnotationModelFactory.class);
  //		}
  //		return null;
  //	}

  //	/**
  //	 * Returns the set of content types for the given location.
  //	 *
  //	 * @param location the location for which to look up the content types
  //	 * @param locationKind the kind of the given location
  //	 * @return the set of content types for the location
  //	 * @since 3.3
  //	 */
  //	protected IContentType[] findContentTypes(IPath location, LocationKind locationKind) {
  //		Assert.isLegal(locationKind != LocationKind.IFILE);
  //		return fContentTypeManager.findContentTypesFor(location.lastSegment());
  //	}

  /**
   * Returns the set of direct base content types for the given set of content types. Returns <code>
   * null</code> if non of the given content types has a direct base content type.
   *
   * @param contentTypes the content types
   * @return the set of direct base content types
   */
  private IContentType[] computeBaseContentTypes(IContentType[] contentTypes) {
    List baseTypes = new ArrayList();
    for (int i = 0; i < contentTypes.length; i++) {
      IContentType baseType = contentTypes[i].getBaseType();
      if (baseType != null) baseTypes.add(baseType);
    }

    IContentType[] result = null;
    int size = baseTypes.size();
    if (size > 0) {
      result = new IContentType[size];
      baseTypes.toArray(result);
    }
    return result;
  }

  //	/**
  //	 * Returns the sharable document factory for the given location.
  //	 *
  //	 * @param location the location for which to looked up the factory
  //	 * @param locationKind the kind of the given location
  //	 * @return the sharable document factory
  //	 * @since 3.3
  //	 * @deprecated As of 3.5
  //	 */
  //	public org.eclipse.core.filebuffers.IDocumentFactory getDocumentFactory(IPath location,
  // LocationKind locationKind) {
  //		org.eclipse.core.filebuffers.IDocumentFactory factory=
  // getDocumentFactory(findContentTypes(location, locationKind));
  //		if (factory == null)
  //			factory= getDocumentFactory(location.lastSegment());
  //		if (factory == null)
  //			factory= getDocumentFactory(location.getFileExtension());
  //		if (factory == null)
  //			factory= getDocumentFactory(WILDCARD);
  //		return factory;
  //	}
  //
  //	/**
  //	 * Returns the sharable set of document setup participants for the given location.
  //	 *
  //	 * @param location the location for which to look up the setup participants
  //	 * @param locationKind the kind of the given location
  //	 * @return the sharable set of document setup participants
  //	 * @since 3.3
  //	 */
  //	public IDocumentSetupParticipant[] getDocumentSetupParticipants(IPath location, LocationKind
  // locationKind) {
  //		Set participants= new HashSet();
  //
  //		List p= getDocumentSetupParticipants(findContentTypes(location, locationKind));
  //		if (p != null)
  //			participants.addAll(p);
  //
  //		p= getDocumentSetupParticipants(location.lastSegment());
  //		if (p != null)
  //			participants.addAll(p);
  //
  //		p= getDocumentSetupParticipants(location.getFileExtension());
  //		if (p != null)
  //			participants.addAll(p);
  //
  //		p= getDocumentSetupParticipants(WILDCARD);
  //		if (p != null)
  //			participants.addAll(p);
  //
  //		IDocumentSetupParticipant[] result= new IDocumentSetupParticipant[participants.size()];
  //		participants.toArray(result);
  //		return result;
  //	}

  //	/**
  //	 * Returns the sharable annotation model factory for the given location.
  //	 *
  //	 * @param location the location for which to look up the factory
  //	 * @param locationKind the kind of the given location
  //	 * @return the sharable annotation model factory
  //	 * @since 3.3
  //	 */
  //	public IAnnotationModelFactory getAnnotationModelFactory(IPath location, LocationKind
  // locationKind) {
  //		IAnnotationModelFactory factory= getAnnotationModelFactory(findContentTypes(location,
  // locationKind));
  //		if (factory == null)
  //			factory= getAnnotationModelFactory(location.lastSegment());
  //		if (factory == null)
  //			factory= getAnnotationModelFactory(location.getFileExtension());
  //		if (factory == null)
  //			factory= getAnnotationModelFactory(WILDCARD);
  //		return factory;
  //	}

}
