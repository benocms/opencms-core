/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagContentLoop.java,v $
 * Date   : $Date: 2006/03/27 14:52:19 $
 * Version: $Revision: 1.18 $
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

package org.opencms.jsp;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.I_CmsXmlDocument;

import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Used to loop through the element values of an XML content item.<p>
 * 
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.18 $ 
 * 
 * @since 6.0.0 
 */
public class CmsJspTagContentLoop extends TagSupport implements I_CmsXmlContentContainer {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 8832749526732064836L;

    /** Reference to the parent 'contentload' tag. */
    private I_CmsXmlContentContainer m_container;

    /** Reference to the looped content element. */
    private I_CmsXmlDocument m_content;

    /** Name of the current element (including the index). */
    private String m_currentElement;

    /** Name of the content node element to show. */
    private String m_element;

    /** Indicates if this is the first content iteration loop. */
    private boolean m_firstLoop;

    /** Index of the content node element to show. */
    private int m_index = -1;

    /** Refenence to the currently selected locale. */
    private Locale m_locale;

    /**
     * Empty constructor, required for JSP tags.<p> 
     */
    public CmsJspTagContentLoop() {

        super();
    }

    /**
     * Constructor used when using <code>contentloop</code> from scriptlet code.<p> 
     * 
     * @param container the parent content container that provides the content element to loop
     * @param element the element to loop in the content
     */
    public CmsJspTagContentLoop(I_CmsXmlContentContainer container, String element) {

        m_element = element;
        init(container);
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doAfterBody()
     */
    public int doAfterBody() {

        if (hasMoreContent()) {
            // one more element with the same name is available, loop again
            return EVAL_BODY_AGAIN;
        }
        // no more elements with this name available, finish the loop
        return SKIP_BODY;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {

        // need to release manually, JSP container may not call release as required (happens with Tomcat)
        release();
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {

        // get a reference to the parent "content container" class
        Tag ancestor = findAncestorWithClass(this, I_CmsXmlContentContainer.class);
        if (ancestor == null) {
            CmsMessageContainer errMsgContainer = Messages.get().container(Messages.ERR_PARENTLESS_TAG_1, "contentloop");
            String msg = Messages.getLocalizedMessage(errMsgContainer, pageContext);
            throw new JspTagException(msg);
        }
        I_CmsXmlContentContainer container = (I_CmsXmlContentContainer)ancestor;

        // initialize the content 
        init(container);

        if (hasMoreContent()) {
            // selected element is available at last once in content
            return EVAL_BODY_INCLUDE;
        } else {
            // no value available for the selected element name, so we skip the whole body
            return SKIP_BODY;
        }
    }

    /**
     * @see org.opencms.jsp.I_CmsXmlContentContainer#getCollectorName()
     */
    public String getCollectorName() {

        return m_container.getCollectorName();
    }

    /**
     * @see org.opencms.jsp.I_CmsXmlContentContainer#getCollectorParam()
     */
    public String getCollectorParam() {

        return m_container.getCollectorParam();
    }

    /**
     * @see org.opencms.jsp.I_CmsXmlContentContainer#getCollectorResult()
     */
    public List getCollectorResult() {

        return m_container.getCollectorResult();
    }

    /**
     * Returns the name of the content node element to show.<p>
     * 
     * @return the name of the content node element to show
     */
    public String getElement() {

        return (m_element != null) ? m_element : "";
    }

    /**
     * @see org.opencms.jsp.I_CmsXmlContentContainer#getResourceName()
     */
    public String getResourceName() {

        return m_container.getResourceName();
    }

    /**
     * @see org.opencms.jsp.I_CmsXmlContentContainer#getXmlDocument()
     */
    public I_CmsXmlDocument getXmlDocument() {

        return m_content;
    }

    /**
     * @see org.opencms.jsp.I_CmsXmlContentContainer#getXmlDocumentElement()
     */
    public String getXmlDocumentElement() {

        return m_currentElement;
    }

    /**
     * @see org.opencms.jsp.I_CmsXmlContentContainer#getXmlDocumentLocale()
     */
    public Locale getXmlDocumentLocale() {

        return m_locale;
    }

    /**
     * @see org.opencms.jsp.I_CmsXmlContentContainer#hasMoreContent()
     */
    public boolean hasMoreContent() {

        if (m_firstLoop) {
            m_firstLoop = false;
        } else {
            m_index++;
        }
        if (m_content.hasValue(m_element, m_locale, m_index)) {
            m_currentElement = CmsXmlUtils.createXpath(m_element, m_index + 1);
            // one more element with the same name is available, loop again
            return true;
        } else {
            // no more elements with this name available, finish the loop
            return false;
        }
    }

    /**
     * @see org.opencms.jsp.I_CmsXmlContentContainer#isPreloader()
     */
    public boolean isPreloader() {

        return m_container.isPreloader();
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {

        m_element = null;
        m_currentElement = null;
        m_content = null;
        m_locale = null;
        m_container = null;
        m_index = 0;
        super.release();
    }

    /**
     * Sets the name of the content node element to show.<p>
     * 
     * @param element the name of the content node element to show
     */
    public void setElement(String element) {

        m_element = element;
    }

    /**
     * Initializes this content loop tag.<p>
     * 
     * @param container the parent content container that provides the content element to loop
     */
    protected void init(I_CmsXmlContentContainer container) {

        m_container = container;

        // append to parent element name (required for nested schemas)
        m_element = CmsXmlUtils.concatXpath(m_container.getXmlDocumentElement(), m_element);

        // get loaded content from parent <contentload> tag
        m_content = m_container.getXmlDocument();
        m_locale = m_container.getXmlDocumentLocale();
        m_index = 0;
        m_currentElement = null;

        // the next loop is the first loop
        m_firstLoop = true;
    }
}