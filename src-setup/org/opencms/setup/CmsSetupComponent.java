/*
 * File   : $Source: /alkacon/cvs/opencms/src-setup/org/opencms/setup/CmsSetupComponent.java,v $
 * Date   : $Date: 2008/02/21 13:47:37 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2007 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.setup;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Module group data.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 7.0.4
 */
public class CmsSetupComponent {

    private List m_dependencies;
    private String m_description;
    private String m_id;
    private Pattern m_modulesRegex;
    private String m_name;
    private int m_position;

    /**
     * Returns the dependencies.<p>
     *
     * @return the dependencies
     */
    public List getDependencies() {

        return m_dependencies;
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the id.<p>
     *
     * @return the id
     */
    public String getId() {

        return m_id;
    }

    /**
     * Returns the modules regular expression.<p>
     *
     * @return the modules regular expression
     */
    public Pattern getModulesRegex() {

        return m_modulesRegex;
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the position.<p>
     *
     * @return the position
     */
    public int getPosition() {

        return m_position;
    }

    /**
     * Matches the module regular expression against the given module name 
     * 
     * @param module the module name to match
     * 
     * @return <code>true</code> if it matches
     */
    public boolean match(String module) {

        return m_modulesRegex.matcher(module).matches();
    }

    /**
     * Sets the dependencies.<p>
     *
     * @param dependencies the dependencies to set
     */
    public void setDependencies(List dependencies) {

        m_dependencies = new ArrayList(dependencies);
    }

    /**
     * Sets the description.<p>
     *
     * @param description the description to set
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Sets the id.<p>
     *
     * @param id the id to set
     */
    public void setId(String id) {

        m_id = id;
    }

    /**
     * Sets the modules regular expression.<p>
     *
     * @param regex the regular expression to set
     */
    public void setModulesRegex(String regex) {

        m_modulesRegex = Pattern.compile(regex);
    }

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the position.<p>
     *
     * @param position the position to set
     */
    public void setPosition(int position) {

        m_position = position;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer ret = new StringBuffer();
        ret.append("[CmsSetupGroupBean: ");
        ret.append("id=").append(m_id).append(", ");
        ret.append("name=").append(m_name).append(", ");
        ret.append("description=").append(m_description).append(", ");
        ret.append("dependencies=").append(m_dependencies).append(", ");
        ret.append("moduleRegex=").append(m_modulesRegex.pattern()).append("]");
        return ret.toString();
    }
}