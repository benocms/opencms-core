/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.containerpage;

import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * One element of a container in a container page.<p>
 * 
 * @since 8.0
 */
public class CmsContainerElementBean {

    /** Flag indicating if a new element should be created replacing the given one on first edit of a container-page. */
    private final boolean m_createNew;

    /** The client ADE editor hash. */
    private transient String m_editorHash;

    /** The element's structure id. */
    private final CmsUUID m_elementId;

    /** The formatter's structure id. */
    private final CmsUUID m_formatterId;

    /** The configured properties. */
    private final Map<String, String> m_individualSettings;

    /** Indicates whether the represented resource is in memory only and not in the VFS. */
    private boolean m_inMemoryOnly;

    /** The resource of this element. */
    private transient CmsResource m_resource;

    /** The settings of this element containing also default values. */
    private transient Map<String, String> m_settings;

    /** The element site path, only set while rendering. */
    private String m_sitePath;

    /**
     * Creates a new container page element bean.<p> 
     *  
     * @param elementId the element's structure id
     * @param formatterId the formatter's structure id, could be <code>null</code>
     * @param individualSettings the element settings as a map of name/value pairs
     * @param createNew <code>true</code> if a new element should be created replacing the given one on first edit of a container-page
     **/
    public CmsContainerElementBean(
        CmsUUID elementId,
        CmsUUID formatterId,
        Map<String, String> individualSettings,
        boolean createNew) {

        m_elementId = elementId;
        m_formatterId = formatterId;
        Map<String, String> newSettings = (individualSettings == null
        ? new HashMap<String, String>()
        : individualSettings);
        m_individualSettings = Collections.unmodifiableMap(newSettings);
        String clientId = m_elementId.toString();
        if (!m_individualSettings.isEmpty()) {
            int hash = m_individualSettings.toString().hashCode();
            clientId += CmsADEManager.CLIENT_ID_SEPERATOR + hash;
        }
        m_editorHash = clientId;
        m_createNew = createNew;
    }

    /**
     * Clones the given element bean with a different set of settings.<p>
     * 
     * @param source the element to clone
     * @param settings the new settings
     * 
     * @return the element bean
     */
    public static CmsContainerElementBean cloneWithSettings(CmsContainerElementBean source, Map<String, String> settings) {

        CmsContainerElementBean result = new CmsContainerElementBean(
            source.m_elementId,
            source.m_formatterId,
            settings,
            source.m_createNew);
        result.m_resource = source.m_resource;
        result.m_sitePath = source.m_sitePath;
        result.m_inMemoryOnly = source.m_inMemoryOnly;
        if (result.m_inMemoryOnly) {
            String editorHash = source.m_editorHash;
            if (editorHash.contains(CmsADEManager.CLIENT_ID_SEPERATOR)) {
                editorHash = editorHash.substring(0, editorHash.indexOf(CmsADEManager.CLIENT_ID_SEPERATOR));
            }
            editorHash += result.getSettingsHash();
            result.m_editorHash = editorHash;
        }
        return result;
    }

    /**
     * Creates an element bean for the given resource type.<p>
     * <b>The represented resource will be in memory only and not in the VFS!!!.</b><p>
     * 
     * @param cms the CMS context 
     * @param resourceType the resource type
     * @param targetFolder the parent folder of the resource
     * @param individualSettings the element settings as a map of name/value pairs
     * @param locale the locale to use
     * 
     * @return the created element bean
     * @throws CmsException 
     * @throws IllegalArgumentException if the resource type not instance of {@link org.opencms.file.types.CmsResourceTypeXmlContent}
     */
    public static CmsContainerElementBean createElementForResourceType(
        CmsObject cms,
        I_CmsResourceType resourceType,
        String targetFolder,
        Map<String, String> individualSettings,
        Locale locale) throws CmsException {

        if (!(resourceType instanceof CmsResourceTypeXmlContent)) {
            throw new IllegalArgumentException();
        }
        CmsContainerElementBean elementBean = new CmsContainerElementBean(
            CmsUUID.getNullUUID(),
            null,
            individualSettings,
            true);
        elementBean.m_inMemoryOnly = true;
        elementBean.m_editorHash = resourceType.getTypeName() + elementBean.getSettingsHash();
        byte[] content = new byte[0];
        String schema = ((CmsResourceTypeXmlContent)resourceType).getSchema();
        if (schema != null) {
            // must set URI of OpenCms user context to parent folder of created resource, 
            // in order to allow reading of properties for default values
            CmsObject newCms = OpenCms.initCmsObject(cms);
            newCms.getRequestContext().setUri(targetFolder);
            // unmarshal the content definition for the new resource
            CmsXmlContentDefinition contentDefinition = CmsXmlContentDefinition.unmarshal(cms, schema);
            CmsXmlContent xmlContent = CmsXmlContentFactory.createDocument(
                newCms,
                locale,
                OpenCms.getSystemInfo().getDefaultEncoding(),
                contentDefinition);
            // adding all other available locales
            for (Locale otherLocale : OpenCms.getLocaleManager().getAvailableLocales()) {
                if (!locale.equals(otherLocale)) {
                    xmlContent.addLocale(newCms, otherLocale);
                }
            }
            content = xmlContent.marshal();
        }

        elementBean.m_resource = new CmsFile(
            CmsUUID.getNullUUID(),
            CmsUUID.getNullUUID(),
            targetFolder + "~",
            resourceType.getTypeId(),
            0,
            cms.getRequestContext().getCurrentProject().getUuid(),
            CmsResource.STATE_NEW,
            0,
            cms.getRequestContext().getCurrentUser().getId(),
            0,
            cms.getRequestContext().getCurrentUser().getId(),
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT,
            1,
            content.length,
            0,
            0,
            content);
        return elementBean;
    }

    /**
     * Returns the ADE client editor has value.<p>
     * 
     * @return the ADE client editor has value
     */
    public String editorHash() {

        return m_editorHash;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof CmsContainerElementBean)) {
            return false;
        }
        return editorHash().equals(((CmsContainerElementBean)obj).editorHash());
    }

    /**
     * Returns the structure id of the formatter of this element.<p>
     *
     * @return the structure id of the formatter of this element
     */
    public CmsUUID getFormatterId() {

        return m_formatterId;
    }

    /**
     * Returns the structure id of the resource of this element.<p>
     *
     * @return the structure id of the resource of this element
     */
    public CmsUUID getId() {

        return m_elementId;
    }

    /**
     * Returns the settings of this element.<p>
     * 
     * @return the settings of this element
     */
    public Map<String, String> getIndividualSettings() {

        return m_individualSettings;
    }

    /**
     * Returns the resource of this element.<p>
     * 
     * It is required to call {@link #initResource(CmsObject)} before this method can be used.<p>
     * 
     * @return the resource of this element
     * 
     * @see #initResource(CmsObject)
     */
    public CmsResource getResource() {

        return m_resource;
    }

    /**
     * Returns the element settings including default values for settings not set.<p>
     * Will return <code>null</code> if the element bean has not been initialized with {@link #initResource(org.opencms.file.CmsObject)}.<p>
     * 
     * @return the element settings
     */
    public Map<String, String> getSettings() {

        return m_settings;
    }

    /**
     * Returns the site path of the resource of this element.<p>
     * 
     * It is required to call {@link #initResource(CmsObject)} before this method can be used.<p>
     * 
     * @return the site path of the resource of this element
     * 
     * @see #initResource(CmsObject)
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_editorHash.hashCode();
    }

    /**
     * Initializes the resource and the site path of this element.<p>
     * 
     * @param cms the CMS context 
     * 
     * @throws CmsException if something goes wrong reading the element resource
     */
    public void initResource(CmsObject cms) throws CmsException {

        if (m_resource == null) {
            m_resource = cms.readResource(getId());
        } else if (!isInMemoryOnly()) {
            CmsUUID id = m_resource.getStructureId();
            if (id == null) {
                id = getId();
            }
            // the resource object may have a wrong root path, e.g. if it was created before the resource was moved
            m_resource = cms.readResource(id);
        }
        if (m_settings == null) {
            m_settings = CmsXmlContentPropertyHelper.mergeDefaults(cms, m_resource, m_individualSettings);
        }
        // redo on every init call to ensure sitepath is calculated for current site
        m_sitePath = cms.getSitePath(m_resource);
    }

    /**
     * Returns if a new element should be created replacing the given one on first edit of a container-page.<p>
     * 
     * @return <code>true</code> if a new element should be created replacing the given one on first edit of a container-page
     */
    public boolean isCreateNew() {

        return m_createNew;
    }

    /**
     * Tests whether this element refers to a group container.<p>
     * 
     * @param cms the CmsObject used for VFS operations
     *  
     * @return true if the container element refers to a group container
     * 
     * @throws CmsException if something goes wrong 
     */
    public boolean isGroupContainer(CmsObject cms) throws CmsException {

        initResource(cms);
        return CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME.equals(OpenCms.getResourceManager().getResourceType(
            m_resource).getTypeName());
    }

    /**
     * Returns if the represented resource is in memory only and not persisted in the VFS.<p>
     * 
     * @return <code>true</code> if the represented resource is in memory only and not persisted in the VFS
     */
    public boolean isInMemoryOnly() {

        return m_inMemoryOnly;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return editorHash();
    }

    /**
     * Gets the hash code for the element settings.<p>
     * 
     * @return the hash code for the element settings 
     */
    private String getSettingsHash() {

        if (!m_individualSettings.isEmpty()) {
            int hash = m_individualSettings.toString().hashCode();
            return CmsADEManager.CLIENT_ID_SEPERATOR + hash;
        }
        return "";
    }
}
