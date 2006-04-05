/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/security/CmsAccessControlList.java,v $
 * Date   : $Date: 2006/03/27 14:52:48 $
 * Version: $Revision: 1.21 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.security;

import org.opencms.file.CmsUser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;
import java.util.Set;

/**
 * An access control list contains the permission sets of all principals for a distinct resource
 * that are calculated on the permissions defined by various access control entries.<p>
 * 
 * <p>To each single resource, access control entries of type <code>CmsAccessControlEntry</code> can be assigned.
 * An access control entry defines the permissions (both allowed and explicitly denied) of a user or group for this resource.</p>
 * 
 * <p>By calling the method <code>getAccessControlList</code> the list is generated on the resource. It contains the result of 
 * merging both access control entries defined immediately on the resource and inherited along the folder hierarchie in the 
 * OpenCms virtual file system (controlled by flags in the entry).</p>
 * 
 * <p>To check the permissions of a user on a distinct resource, the method <code>hasPermissions</code> in the driver manager
 * is called in each operation. This method acts as access guard and matches the required permissions for the operation
 * against the allowed and denied permissions defined for the user or groups of this user.</p>
 * 
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.21 $ 
 * 
 * @since 6.0.0 
 */
public class CmsAccessControlList {

    /**
     * Collected permissions of a principal on this resource .
     */
    private HashMap m_permissions;

    /**
     * Constructor to create an empty access control list for a given resource.<p>
     *
     */
    public CmsAccessControlList() {

        m_permissions = new HashMap();
    }

    /**
     * Adds an access control entry to the access control list.<p>
     * 
     * @param entry the access control entry to add
     */
    public void add(CmsAccessControlEntry entry) {

        CmsPermissionSetCustom p = (CmsPermissionSetCustom)m_permissions.get(entry.getPrincipal());
        if (p == null) {
            p = new CmsPermissionSetCustom();
            m_permissions.put(entry.getPrincipal(), p);
        }
        p.addPermissions(entry.getPermissions());
    }

    /**
     * Returns a clone of this Objects instance.<p>
     * 
     * @return a clone of this instance
     */
    public Object clone() {

        CmsAccessControlList acl = new CmsAccessControlList();
        Iterator i = m_permissions.keySet().iterator();
        while (i.hasNext()) {
            Object key = i.next();
            acl.m_permissions.put(key, ((CmsPermissionSetCustom)m_permissions.get(key)).clone());
        }
        return acl;
    }

    /**
     * Returns the permission map of this access control list.<p>
     * 
     * @return permission map
     */
    public HashMap getPermissionMap() {

        return m_permissions;
    }

    /**
     * Calculates the permissions of the given user and his groups from the access control list.<p>
     *  
     * @param user the user
     * @param groups the groups of this user
     * 
     * @return the summarized permission set of the user
     */
    public CmsPermissionSetCustom getPermissions(CmsUser user, List groups) {

        CmsPermissionSetCustom sum = new CmsPermissionSetCustom();
        CmsPermissionSet p = (CmsPermissionSet)m_permissions.get(user.getId());
        if (p != null) {
            sum.addPermissions(p);
        }
        if (groups != null) {
            I_CmsPrincipal principal;
            if (groups instanceof RandomAccess) {
                int size = groups.size();
                for (int i = 0; i < size; i++) {
                    principal = (I_CmsPrincipal)groups.get(i);
                    p = (CmsPermissionSet)m_permissions.get(principal.getId());
                    if (p != null) {
                        sum.addPermissions(p);
                    }
                }
            } else {
                Iterator it = groups.iterator();
                while (it.hasNext()) {
                    principal = (I_CmsPrincipal)it.next();
                    p = (CmsPermissionSet)m_permissions.get(principal.getId());
                    if (p != null) {
                        sum.addPermissions(p);
                    }
                }
            }
        }
        return sum;
    }

    /**
     * Returns the permission set of a principal as stored in the access control list.<p>
     * 
     * @param principal the principal (group or user)
     * 
     * @return the current permissions of this single principal
     */
    public CmsPermissionSetCustom getPermissions(I_CmsPrincipal principal) {

        return (CmsPermissionSetCustom)m_permissions.get(principal.getId());
    }

    /**
     * Calculates the permissions of the given user and his groups from the access control list.<p>
     * The permissions are returned as permission string in the format {{+|-}{r|w|v|c|i}}*.
     * 
     * @param user the user
     * @param groups the groups oft this user
     * 
     * @return a string that displays the permissions
     */
    public String getPermissionString(CmsUser user, List groups) {

        return getPermissions(user, groups).getPermissionString();
    }

    /**
     * Returns the principals with specific permissions stored in this access control list.<p>
     * 
     * @return enumeration of principals (each group or user)
     */
    public Set getPrincipals() {

        return m_permissions.keySet();
    }

    /**
     * Sets the allowed permissions of a given access control entry as allowed permissions in the access control list.<p>
     * The denied permissions are left unchanged.
     * 
     * @param entry the access control entry
     */
    public void setAllowedPermissions(CmsAccessControlEntry entry) {

        CmsPermissionSetCustom p = (CmsPermissionSetCustom)m_permissions.get(entry.getPrincipal());
        if (p == null) {
            p = new CmsPermissionSetCustom();
            m_permissions.put(entry.getPrincipal(), p);
        }
        p.setPermissions(entry.getAllowedPermissions(), p.getDeniedPermissions());
    }

    /**
     * Sets the denied permissions of a given access control entry as denied permissions in the access control list.<p>
     * The allowed permissions are left unchanged.
     * 
     * @param entry the access control entry
     */
    public void setDeniedPermissions(CmsAccessControlEntry entry) {

        CmsPermissionSetCustom p = (CmsPermissionSetCustom)m_permissions.get(entry.getPrincipal());
        if (p == null) {
            p = new CmsPermissionSetCustom();
            m_permissions.put(entry.getPrincipal(), p);
        }
        p.setPermissions(p.getAllowedPermissions(), entry.getDeniedPermissions());
    }
}