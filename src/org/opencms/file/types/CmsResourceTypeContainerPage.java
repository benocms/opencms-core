/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/Attic/CmsResourceTypeContainerPage.java,v $
 * Date   : $Date: 2009/08/13 10:47:27 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.types;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.file.CmsResource;
import org.opencms.loader.CmsContainerPageLoader;
import org.opencms.main.OpenCms;

/**
 * Resource type descriptor for the type "containerpage".<p>
 *
 * It is just a xml content with a fixed schema.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 7.6 
 */
public class CmsResourceTypeContainerPage extends CmsResourceTypeXmlContent {

    /** Indicates that the static configuration of the resource type has been frozen. */
    private static boolean m_staticFrozen;

    /** The static type id of this resource type. */
    private static int m_staticTypeId;

    /** The type id of this resource type. */
    private static final int RESOURCE_TYPE_ID = 13;

    /** The name of this resource type. */
    private static final String RESOURCE_TYPE_NAME = "containerpage";

    /** Fixed schema for container pages. */
    private static final String SCHEMA = "/system/workplace/editors/ade/schemas/container_page.xsd";

    /**
     * Default constructor that sets the fixed schema for container pages.<p>
     */
    public CmsResourceTypeContainerPage() {

        super();
        m_typeName = RESOURCE_TYPE_NAME;
        m_typeId = CmsResourceTypeContainerPage.RESOURCE_TYPE_ID;
        addConfigurationParameter(CONFIGURATION_SCHEMA, SCHEMA);
    }

    /**
     * Returns the static type id of this (default) resource type.<p>
     * 
     * @return the static type id of this (default) resource type
     */
    public static int getStaticTypeId() {

        return m_staticTypeId;
    }

    /**
     * Returns the static type name of this (default) resource type.<p>
     * 
     * @return the static type name of this (default) resource type
     */
    public static String getStaticTypeName() {

        return RESOURCE_TYPE_NAME;
    }

    /**
     * Returns <code>true</code> in case the given resource is a container page.<p>
     * 
     * Internally this checks if the type id for the given resource is 
     * identical type id of the container page.<p>
     * 
     * @param resource the resource to check
     * 
     * @return <code>true</code> in case the given resource is a container page
     */
    public static boolean isContainerPage(CmsResource resource) {

        boolean result = false;
        if (resource != null) {
            result = resource.getTypeId() == m_staticTypeId;
        }
        return result;
    }

    /**
     * @see org.opencms.file.types.CmsResourceTypeXmlContent#getLoaderId()
     */
    public int getLoaderId() {

        return CmsContainerPageLoader.RESOURCE_LOADER_ID;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#initConfiguration(java.lang.String, java.lang.String, String)
     */
    public void initConfiguration(String name, String id, String className) throws CmsConfigurationException {

        if ((OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) && m_staticFrozen) {
            // configuration already frozen
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_CONFIG_FROZEN_3,
                this.getClass().getName(),
                getStaticTypeName(),
                new Integer(getStaticTypeId())));
        }

        if (!RESOURCE_TYPE_NAME.equals(name)) {
            // default resource type MUST have default name
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_INVALID_RESTYPE_CONFIG_NAME_3,
                this.getClass().getName(),
                RESOURCE_TYPE_NAME,
                name));
        }

        // freeze the configuration
        m_staticFrozen = true;

        super.initConfiguration(RESOURCE_TYPE_NAME, id, className);
        // set static members with values from the configuration        
        m_staticTypeId = m_typeId;
    }
}