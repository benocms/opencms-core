/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsADEManager.java,v $
 * Date   : $Date: 2009/09/21 12:27:14 $
 * Version: $Revision: 1.1.2.4 $
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

package org.opencms.workplace.editors.ade;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsContainerPageLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.search.CmsSearch;
import org.opencms.search.CmsSearchParameters;
import org.opencms.search.CmsSearchResult;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.collections.list.NodeCachingLinkedList;
import org.apache.commons.logging.Log;

/**
 * ADE server used for client/server communication.<p>
 * 
 * see jsp files under <tt>/system/workplace/editors/ade/</tt>.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.4 $
 * 
 * @since 7.6
 */
public class CmsADEManager extends CmsJspActionElement {

    /** Request parameter action value constant. */
    public static final String ACTION_ALL = "all";

    /** Request parameter action value constant. */
    public static final String ACTION_CNT = "cnt";

    /** Request parameter action value constant. */
    public static final String ACTION_DEL = "del";

    /** Request parameter action value constant. */
    public static final String ACTION_ELEM = "elem";

    /** Request parameter action value constant. */
    public static final String ACTION_FAV = "fav";

    /** Request parameter action value constant. */
    public static final String ACTION_LS = "ls";

    /** Request parameter action value constant. */
    public static final String ACTION_NEW = "new";

    /** Request parameter action value constant. */
    public static final String ACTION_REC = "rec";

    /** Request parameter action value constant. */
    public static final String ACTION_SEARCH = "search";

    /** User additional info key constant. */
    public static final String ADDINFO_ADE_FAVORITE_LIST = "ADE_FAVORITE_LIST";

    /** User additional info key constant. */
    public static final String ADDINFO_ADE_RECENTLIST_SIZE = "ADE_RECENTLIST_SIZE";

    /** User additional info key constant. */
    public static final String ADDINFO_ADE_SEARCHPAGE_SIZE = "ADE_SEARCHPAGE_SIZE";

    // TODO: make the default list sizes configurable in opencms-workplace.xml
    /** default recent list size constant. */
    public static final int DEFAULT_RECENT_LIST_SIZE = 10;

    /** default search page size constant. */
    public static final int DEFAULT_SEARCHPAGE_SIZE = 10;

    /** State Constant for client-side element type 'new element configuration'. */
    public static final String ELEMENT_NEWCONFIG = "NC";

    /** Mime type constant. */
    public static final String MIMETYPE_APPLICATION_JSON = "application/json";

    /** JSON property constant file. */
    public static final String P_ALLOWEDIT = "allowEdit";

    /** JSON property constant file. */
    public static final String P_ALLOWMOVE = "allowMove";

    /** JSON property constant containers. */
    public static final String P_CONTAINERS = "containers";

    /** JSON property constant contents. */
    public static final String P_CONTENTS = "contents";

    /** JSON property constant element. */
    public static final String P_COUNT = "count";

    /** JSON property constant file. */
    public static final String P_DATE = "date";

    /** JSON property constant element. */
    public static final String P_ELEMENTS = "elements";

    /** JSON property constant favorites. */
    public static final String P_FAVORITES = "favorites";

    /** JSON property constant file. */
    public static final String P_FILE = "file";

    /** JSON property constant formatter. */
    public static final String P_FORMATTER = "formatter";

    /** JSON property constant formatters. */
    public static final String P_FORMATTERS = "formatters";

    /** JSON property constant element. */
    public static final String P_HASMORE = "hasmore";

    /** JSON property constant id. */
    public static final String P_ID = "id";

    /** JSON property constant file. */
    public static final String P_LOCALE = "locale";

    /** JSON property constant file. */
    public static final String P_LOCKED = "locked";

    /** JSON property constant file. */
    public static final String P_MAXELEMENTS = "maxElem";

    /** JSON property constant file. */
    public static final String P_OBJTYPE = "objtype";

    /** JSON property constant file. */
    public static final String P_NAME = "name";

    /** JSON property constant file. */
    public static final String P_NAVTEXT = "navText";

    /** JSON property constant recent. */
    public static final String P_RECENT = "recent";

    /** JSON property constant file. */
    public static final String P_STATUS = "status";

    /** JSON property constant file. */
    public static final String P_SUBITEMS = "subItems";

    /** JSON property constant file. */
    public static final String P_TITLE = "title";

    /** JSON property constant file. */
    public static final String P_TYPE = "type";

    /** JSON response property constant. */
    public static final String P_TYPENAME = "typename";

    /** JSON property constant uri. */
    public static final String P_URI = "uri";

    /** JSON property constant file. */
    public static final String P_USER = "user";

    /** Request parameter name constant. */
    public static final String PARAMETER_ACTION = "action";

    /** Request parameter name constant. */
    public static final String PARAMETER_CNTPAGE = "cntpage";

    /** Request parameter name constant. */
    public static final String PARAMETER_DATA = "data";

    /** Request parameter name constant. */
    public static final String PARAMETER_ELEM = "elem";

    /** Request parameter name constant. */
    public static final String PARAMETER_LOCALE = "locale";

    /** Request parameter name constant. */
    public static final String PARAMETER_LOCATION = "location";

    /** Request parameter name constant. */
    public static final String PARAMETER_PAGE = "page";

    /** Request parameter name constant. */
    public static final String PARAMETER_TEXT = "text";

    /** Request parameter action value constant. */
    public static final String PARAMETER_TYPE = "type";

    /** Request parameter name constant. */
    public static final String PARAMETER_URI = "uri";

    /** Request path constant. */
    public static final String REQUEST_GET = "/system/workplace/editors/ade/get.jsp";

    /** Request path constant. */
    public static final String REQUEST_SET = "/system/workplace/editors/ade/set.jsp";

    /** JSON response property constant. */
    public static final String RES_ERROR = "error";

    /** JSON response property constant. */
    public static final String RES_STATE = "state";

    /** JSON response state value constant. */
    public static final String RES_STATE_ERROR = "error";

    /** JSON response state value constant. */
    public static final String RES_STATE_OK = "ok";

    /** JSON response state value constant. */
    public static final String ELEMENT_TYPE = "Element";

    /** JSON response state value constant. */
    public static final String CONTAINER_TYPE = "Container";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEManager.class);

    /** The memory monitor instance. */
    private CmsMemoryMonitor m_cache = OpenCms.getMemoryMonitor();

    /**
     * Constructor.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsADEManager(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * Main method that handles all requests.<p>
     * 
     * @throws IOException if there is any problem while writing the result to the response 
     * @throws JSONException if there is any problem with JSON
     */
    public void serve() throws JSONException, IOException {

        // set the mime type to application/json
        CmsFlexController controller = CmsFlexController.getController(getRequest());
        controller.getTopResponse().setContentType(MIMETYPE_APPLICATION_JSON);

        JSONObject result = new JSONObject();
        try {
            // handle request depending on the requested jsp
            String action = getRequest().getPathInfo();
            if (action.equals(REQUEST_GET)) {
                result = executeActionGet();
            } else if (action.equals(REQUEST_SET)) {
                result = executeActionSet();
            } else {
                result.put(RES_ERROR, Messages.get().getBundle().key(Messages.ERR_JSON_INVALID_ACTION_URL_1, action));
            }
        } catch (Exception e) {
            // a serious error occurred, should not...
            result.put(RES_ERROR, e.getLocalizedMessage() == null ? "NPE" : e.getLocalizedMessage());
            LOG.error(Messages.get().getBundle().key(
                Messages.ERR_SERVER_EXCEPTION_1,
                CmsRequestUtil.appendParameters(
                    getRequest().getRequestURL().toString(),
                    CmsRequestUtil.createParameterMap(getRequest().getQueryString()),
                    false)), e);
        }
        // add state info
        if (result.has(RES_ERROR)) {
            // add state=error in case an error occurred 
            result.put(RES_STATE, RES_STATE_ERROR);
        } else if (!result.has(RES_STATE)) {
            // add state=ok i case no error occurred
            result.put(RES_STATE, RES_STATE_OK);
        }
        // write the result
        result.write(getResponse().getWriter());
    }

    /**
     * Compares two search option objects.<p>
     * 
     * Better than to implement the {@link CmsSearchOptions#equals(Object)} method,
     * since the page number is not considered in this comparison.<p>
     * 
     * @param o1 the first search option object
     * @param o2 the first search option object
     * 
     * @return <code>true</code> if they are equal
     */
    protected boolean compareSearchOptions(CmsSearchOptions o1, CmsSearchOptions o2) {

        if (o1 == o2) {
            return true;
        }
        if ((o1 == null) || (o2 == null)) {
            return false;
        }
        if (!o1.getLocation().equals(o2.getLocation())) {
            return false;
        }
        if (!o1.getText().equals(o2.getText())) {
            return false;
        }
        if (!o1.getType().equals(o2.getType())) {
            return false;
        }
        return true;

    }

    /**
     * Deletes the given elements from server.<p>
     * 
     * @param elems the array of client-side element ids
     * 
     * @throws CmsException if something goes wrong 
     */
    protected void deleteElements(JSONArray elems) throws CmsException {

        CmsObject cms = getCmsObject();
        for (int i = 0; i < elems.length(); i++) {
            CmsResource res = cms.readResource(CmsElementUtil.parseId(elems.optString(i)));
            if (cms.getLock(res).isUnlocked()) {
                cms.lockResource(cms.getSitePath(res));
            }
            cms.deleteResource(cms.getSitePath(res), CmsResource.DELETE_PRESERVE_SIBLINGS);
        }
    }

    /**
     * Handles all ADE get requests.<p>
     * 
     * @return the result
     * 
     * @throws JSONException if there is any problem with JSON
     * @throws CmsException if there is a problem with the cms context
     */
    protected JSONObject executeActionGet() throws CmsException, JSONException {

        JSONObject result = new JSONObject();

        HttpServletRequest request = getRequest();
        if (!checkParameters(request, result, PARAMETER_ACTION, PARAMETER_CNTPAGE, PARAMETER_LOCALE, PARAMETER_URI)) {
            return result;
        }
        String actionParam = request.getParameter(PARAMETER_ACTION);
        String cntPageParam = request.getParameter(PARAMETER_CNTPAGE);
        String localeParam = request.getParameter(PARAMETER_LOCALE);
        String uriParam = request.getParameter(PARAMETER_URI);

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setLocale(CmsLocaleManager.getLocale(localeParam));
        CmsResource cntPageRes = cms.readResource(cntPageParam);
        CmsContainerPageBean cntPage = CmsContainerPageCache.getInstance().getCache(
            cms,
            cntPageRes,
            cms.getRequestContext().getLocale());

        if (actionParam.equals(ACTION_ALL)) {
            // first load, get everything
            result = getContainerPage(cntPageRes, cntPage, uriParam.equals(cntPageParam) ? null : uriParam);
        } else if (actionParam.equals(ACTION_ELEM)) {
            // get element data
            String elemParam = request.getParameter(PARAMETER_ELEM);
            if (elemParam == null) {
                storeErrorMissingParam(result, PARAMETER_ELEM);
                return result;
            }
            CmsElementUtil elemUtil = new CmsElementUtil(cms, request, getResponse(), uriParam);
            JSONObject resElements = new JSONObject();
            if (elemParam.startsWith("[")) {
                // element list
                JSONArray elems = new JSONArray(elemParam);
                for (int i = 0; i < elems.length(); i++) {
                    String elem = elems.getString(i);
                    try {
                        resElements.put(elem, elemUtil.getElementData(CmsElementUtil.parseId(elem), cntPage.getTypes()));
                    } catch (Exception e) {
                        // ignore any problems
                        if (!LOG.isDebugEnabled()) {
                            LOG.warn(e.getLocalizedMessage());
                        }
                        LOG.debug(e.getLocalizedMessage(), e);
                    }
                }
            } else {
                // single element
                try {
                    resElements.put(elemParam, elemUtil.getElementData(
                        CmsElementUtil.parseId(elemParam),
                        cntPage.getTypes()));
                } catch (Exception e) {
                    // ignore any problems
                    if (!LOG.isDebugEnabled()) {
                        LOG.warn(e.getLocalizedMessage());
                    }
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            }
            result.put(P_ELEMENTS, resElements);
        } else if (actionParam.equals(ACTION_FAV)) {
            // get the favorite list
            result.put(P_FAVORITES, getFavoriteList(null, cntPage.getTypes()));
        } else if (actionParam.equals(ACTION_REC)) {
            // get recent list
            result.put(P_RECENT, getRecentList(null, cntPage.getTypes()));
        } else if (actionParam.equals(ACTION_SEARCH)) {
            // new search
            String containerPageUri = request.getParameter(PARAMETER_CNTPAGE);

            if (containerPageUri == null) {
                storeErrorMissingParam(result, PARAMETER_CNTPAGE);
                return result;
            }
            CmsSearchOptions searchOptions = new CmsSearchOptions(request);
            JSONObject searchResult = getSearchResult(searchOptions, cntPage.getTypes());
            result.merge(searchResult, true, false);
        } else if (actionParam.equals(ACTION_LS)) {
            // last search
            String containerPageUri = request.getParameter(PARAMETER_CNTPAGE);
            if (containerPageUri == null) {
                storeErrorMissingParam(result, PARAMETER_CNTPAGE);
                return result;
            }
            CmsSearchOptions searchOptions = new CmsSearchOptions(request);
            JSONObject searchResult = getLastSearchResult(searchOptions, cntPage.getTypes());

            // we need those on the client side to make scrolling work
            CmsSearchOptions oldOptions = getSearchOptionsFromCache();
            if (oldOptions != null) {
                result.put(PARAMETER_TYPE, oldOptions.getTypes());
                result.put(PARAMETER_TEXT, oldOptions.getText());
                result.put(PARAMETER_LOCATION, oldOptions.getLocation());
            }
            result.merge(searchResult, true, false);
        } else if (actionParam.equals(ACTION_NEW)) {
            // get a new element
            if (!checkParameters(request, result, PARAMETER_DATA)) {
                return result;
            }
            String dataParam = request.getParameter(PARAMETER_DATA);
            String type = dataParam;
            CmsElementCreator elemCreator = new CmsElementCreator(cms, cntPage.getResTypeConfig());
            CmsResource newResource = elemCreator.createElement(cms, type);
            result.put(P_ID, CmsElementUtil.createId(newResource.getStructureId()));
            result.put(P_URI, cms.getSitePath(newResource));
        } else {
            result.put(RES_ERROR, Messages.get().getBundle().key(
                Messages.ERR_JSON_WRONG_PARAMETER_VALUE_2,
                PARAMETER_ACTION,
                actionParam));
        }
        return result;
    }

    /**
     * Handles all ADE set requests.<p>
     * 
     * @return the result
     * 
     * @throws JSONException if there is any problem with JSON
     * @throws CmsException if there is a problem with the cms context
     */
    protected JSONObject executeActionSet() throws JSONException, CmsException {

        HttpServletRequest request = getRequest();
        JSONObject result = new JSONObject();
        if (!checkParameters(request, result, PARAMETER_ACTION, PARAMETER_LOCALE)) {
            return result;
        }
        String actionParam = getRequest().getParameter(PARAMETER_ACTION);
        String localeParam = getRequest().getParameter(PARAMETER_LOCALE);

        getCmsObject().getRequestContext().setLocale(CmsLocaleManager.getLocale(localeParam));
        if (actionParam.equals(ACTION_FAV)) {
            // save the favorite list
            String dataParam = getRequest().getParameter(PARAMETER_DATA);
            if (dataParam == null) {
                storeErrorMissingParam(result, PARAMETER_DATA);
                return result;
            }
            JSONArray list = new JSONArray(dataParam);
            setFavoriteList(list);
        } else if (actionParam.equals(ACTION_REC)) {
            // save the recent list
            String dataParam = getRequest().getParameter(PARAMETER_DATA);
            if (dataParam == null) {
                storeErrorMissingParam(result, PARAMETER_DATA);
                return result;
            }
            JSONArray list = new JSONArray(dataParam);
            setRecentList(list);
        } else if (actionParam.equals(ACTION_CNT)) {
            // save the container page
            if (!checkParameters(request, result, PARAMETER_CNTPAGE, PARAMETER_DATA)) {
                return result;
            }
            String cntPageParam = getRequest().getParameter(PARAMETER_CNTPAGE);
            String dataParam = getRequest().getParameter(PARAMETER_DATA);
            JSONObject cntPage = new JSONObject(dataParam);
            setContainerPage(cntPageParam, cntPage);
        } else if (actionParam.equals(ACTION_DEL)) {
            if (!checkParameters(request, result, PARAMETER_DATA)) {
                return result;
            }
            String dataParam = getRequest().getParameter(PARAMETER_DATA);
            JSONArray elems = new JSONArray(dataParam);
            deleteElements(elems);
        } else {
            result.put(RES_ERROR, Messages.get().getBundle().key(
                Messages.ERR_JSON_WRONG_PARAMETER_VALUE_2,
                PARAMETER_ACTION,
                actionParam));
        }
        return result;
    }

    /**
     * Returns the data for the given container page.<p>
     * 
     * @param resource the container page's resource 
     * @param cntPage the container page to use
     * @param elemUri the current element uri, <code>null</code> if not to be used as template
     * 
     * @return the data for the given container page
     * 
     * @throws CmsException if something goes wrong with the cms context
     * @throws JSONException if something goes wrong with the JSON manipulation
     */
    protected JSONObject getContainerPage(CmsResource resource, CmsContainerPageBean cntPage, String elemUri)
    throws CmsException, JSONException {

        CmsObject cms = getCmsObject();

        // create empty result object
        JSONObject result = new JSONObject();
        JSONObject resElements = new JSONObject();
        JSONObject resContainers = new JSONObject();
        result.put(P_ELEMENTS, resElements);
        result.put(P_CONTAINERS, resContainers);
        result.put(P_LOCALE, cms.getRequestContext().getLocale().toString());

        // get the container page itself
        CmsResourceUtil resUtil = new CmsResourceUtil(cms, resource);
        Set<String> types = cntPage.getTypes();

        // collect some basic data
        result.put(CmsADEManager.P_ALLOWEDIT, resUtil.getLock().isLockableBy(cms.getRequestContext().currentUser())
            && resUtil.isEditable());
        result.put(CmsADEManager.P_LOCKED, resUtil.getLockedByName());

        // collect resource type elements
        if (cntPage.getResTypeConfig() != null) {
            resElements.merge(getResourceTypes(cntPage.getResTypeConfig(), types), true, false);
        }

        // collect page elements
        CmsElementUtil elemUtil = new CmsElementUtil(cms, getRequest(), getResponse(), getRequest().getParameter(
            PARAMETER_URI));
        Set<CmsUUID> ids = new HashSet<CmsUUID>();
        for (Map.Entry<String, CmsContainerBean> entry : cntPage.getContainers().entrySet()) {
            CmsContainerBean container = entry.getValue();

            // set the container data
            JSONObject resContainer = new JSONObject();
            resContainer.put(P_OBJTYPE, CONTAINER_TYPE);
            resContainer.put(P_NAME, container.getName());
            resContainer.put(P_TYPE, container.getType());
            resContainer.put(P_MAXELEMENTS, container.getMaxElements());
            JSONArray resContainerElems = new JSONArray();
            resContainer.put(P_ELEMENTS, resContainerElems);

            // get the actual number of elements to render
            int renderElems = container.getElements().size();
            if ((container.getMaxElements() > -1) && (renderElems > container.getMaxElements())) {
                renderElems = container.getMaxElements();
            }
            // add the template element
            if ((elemUri != null) && container.getType().equals(CmsContainerPageBean.TYPE_TEMPLATE)) {
                renderElems--;

                CmsResource elemRes = cms.readResource(elemUri);
                // check if the element already exists
                String id = CmsElementUtil.createId(elemRes.getStructureId());
                // collect ids
                resContainerElems.put(id);
                if (ids.contains(elemRes.getStructureId())) {
                    continue;
                }
                // get the element data
                JSONObject resElement = elemUtil.getElementData(elemRes, types);
                // store element data
                ids.add(elemRes.getStructureId());
                resElements.put(id, resElement);
            }
            // iterate the elements
            for (CmsContainerElementBean element : container.getElements()) {

                if (renderElems < 1) {
                    // just collect as many elements as allowed in the template
                    break;
                }
                renderElems--;

                // check if the element already exists
                String id = CmsElementUtil.createId(element.getElement().getStructureId());
                // collect ids
                resContainerElems.put(id);
                if (ids.contains(element.getElement().getStructureId())) {
                    continue;
                }
                // get the element data
                JSONObject resElement = elemUtil.getElementData(element.getElement(), types);

                // get subcontainer elements
                if (resElement.has(P_SUBITEMS)) {
                    JSONArray subItems = resElement.getJSONArray(P_SUBITEMS);
                    for (int i = 0; i < subItems.length(); i++) {
                        String subItemId = subItems.getString(i);
                        CmsUUID subItemUuid = CmsElementUtil.parseId(subItemId);
                        if (!ids.contains(subItemUuid)) {
                            CmsResource subItemResource = cms.readResource(subItemUuid);
                            JSONObject subItemData = elemUtil.getElementData(subItemResource, types);
                            ids.add(subItemUuid);
                            resElements.put(subItemId, subItemData);
                        }
                    }
                }

                // store element data
                ids.add(element.getElement().getStructureId());
                resElements.put(id, resElement);
            }

            resContainers.put(container.getName(), resContainer);
        }
        // collect the favorites
        JSONArray resFavorites = getFavoriteList(resElements, types);
        result.put(P_FAVORITES, resFavorites);
        // collect the recent list
        JSONArray resRecent = getRecentList(resElements, types);
        result.put(P_RECENT, resRecent);

        return result;
    }

    /**
     * Returns the current user's favorites list.<p>
     * 
     * @param resElements the current page's element list
     * @param types the supported container page types
     * 
     * @return the current user's favorites list
     * 
     * @throws JSONException if something goes wrong in the json manipulation
     */
    protected JSONArray getFavoriteList(JSONObject resElements, Collection<String> types) throws JSONException {

        JSONArray result = new JSONArray();
        CmsElementUtil elemUtil = new CmsElementUtil(
            getCmsObject(),
            getRequest(),
            getResponse(),
            getRequest().getParameter(PARAMETER_URI));

        // iterate the list and create the missing elements
        JSONArray favList = getFavoriteListFromStore();
        for (int i = 0; i < favList.length(); i++) {
            String id = favList.optString(i);
            if ((resElements != null) && !resElements.has(id)) {
                try {
                    resElements.put(id, elemUtil.getElementData(CmsElementUtil.parseId(id), types));
                    result.put(id);
                } catch (Exception e) {
                    // ignore any problems
                    if (!LOG.isDebugEnabled()) {
                        LOG.warn(e.getLocalizedMessage());
                    }
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            } else {
                result.put(id);
            }
        }

        return result;
    }

    /**
     * Returns the cached list, or creates it if not available.<p>
     * 
     * @return the cached recent list
     * 
     * @throws JSONException if something goes wrong
     */
    protected JSONArray getFavoriteListFromStore() throws JSONException {

        CmsUser user = getCmsObject().getRequestContext().currentUser();
        String favListStr = (String)user.getAdditionalInfo(ADDINFO_ADE_FAVORITE_LIST);
        JSONArray favoriteList = new JSONArray();
        if (favListStr != null) {
            favoriteList = new JSONArray(favListStr);
        }
        return favoriteList;
    }

    /**
     * Returns elements for the search result matching the given options.<p>
     * 
     * @param options the search options
     * @param types the supported container types
     * 
     * @return JSON object with 2 properties, {@link CmsADEManager#P_ELEMENTS} and {@link CmsADEManager#P_HASMORE}
     * 
     * @throws JSONException if something goes wrong
     */
    protected JSONObject getLastSearchResult(CmsSearchOptions options, Set<String> types) throws JSONException {

        CmsSearchOptions lastOptions = getSearchOptionsFromCache();
        if ((lastOptions == null) || compareSearchOptions(lastOptions, options)) {
            return new JSONObject();
        }
        return getSearchResult(lastOptions, types);
    }

    /**
     * Returns the current user's recent list.<p>
     * 
     * @param resElements the current page's element list
     * @param types the supported container types
     * 
     * @return the current user's recent list
     */
    protected JSONArray getRecentList(JSONObject resElements, Collection<String> types) {

        JSONArray result = new JSONArray();
        CmsElementUtil elemUtil = new CmsElementUtil(
            getCmsObject(),
            getRequest(),
            getResponse(),
            getRequest().getParameter(PARAMETER_URI));

        // get the cached list
        List<CmsUUID> recentList = getRecentListFromCache();
        // iterate the list and create the missing elements
        for (CmsUUID structureId : recentList) {
            String id = CmsElementUtil.createId(structureId);
            if ((resElements != null) && !resElements.has(id)) {
                try {
                    resElements.put(id, elemUtil.getElementData(structureId, types));
                    result.put(id);
                } catch (Exception e) {
                    // ignore any problems
                    if (!LOG.isDebugEnabled()) {
                        LOG.warn(e.getLocalizedMessage());
                    }
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            } else {
                result.put(id);
            }
        }

        return result;
    }

    /**
     * Returns the cached list, or creates it if not available.<p>
     * 
     * @return the cached recent list
     */
    @SuppressWarnings("unchecked")
    protected List<CmsUUID> getRecentListFromCache() {

        CmsUser user = getCmsObject().getRequestContext().currentUser();
        List<CmsUUID> recentList = m_cache.getADERecentList(user.getId().toString());
        if (recentList == null) {
            Integer maxElems = (Integer)user.getAdditionalInfo(ADDINFO_ADE_RECENTLIST_SIZE);
            if (maxElems == null) {
                maxElems = new Integer(DEFAULT_RECENT_LIST_SIZE);
            }
            recentList = new NodeCachingLinkedList(maxElems.intValue());
            m_cache.cacheADERecentList(user.getId().toString(), recentList);
        }
        return recentList;
    }

    /**
     * Returns the data for new elements from the given configuration file.<p>
     * 
     * @param resTypeConfig the configuration file to use 
     * @param types the supported container page types
     * 
     * @return the data for the given container page
     * 
     * @throws CmsException if something goes wrong with the cms context
     * @throws JSONException if something goes wrong with the JSON manipulation
     */
    protected JSONObject getResourceTypes(CmsResource resTypeConfig, Set<String> types)
    throws CmsException, JSONException {

        JSONObject resElements = new JSONObject();
        CmsElementUtil elemUtil = new CmsElementUtil(
            getCmsObject(),
            getRequest(),
            getResponse(),
            getRequest().getParameter(PARAMETER_URI));
        CmsElementCreator creator = new CmsElementCreator(getCmsObject(), resTypeConfig);
        Map<String, CmsTypeConfigurationItem> typeConfig = creator.getConfiguration();
        for (Map.Entry<String, CmsTypeConfigurationItem> entry : typeConfig.entrySet()) {
            String type = entry.getKey();
            String elementUri = entry.getValue().getSourceFile();
            JSONObject resElement = elemUtil.getElementData(elementUri, types);
            // overwrite some special fields for new elements
            resElement.put(P_ID, type);
            resElement.put(P_STATUS, ELEMENT_NEWCONFIG);
            resElement.put(P_TYPE, type);
            resElement.put(P_TYPENAME, CmsWorkplaceMessages.getResourceName(
                getCmsObject().getRequestContext().getLocale(),
                type));
            resElements.put(type, resElement);
        }
        return resElements;
    }

    /**
     * Returns the cached search options.<p>
     * 
     * @return the cached search options
     */
    protected CmsSearchOptions getSearchOptionsFromCache() {

        CmsUser user = getCmsObject().getRequestContext().currentUser();
        CmsSearchOptions searchOptions = m_cache.getADESearchOptions(user.getId().toString());
        return searchOptions;
    }

    /**
     * Returns elements for the search result matching the given options.<p>
     * 
     * @param options the search options
     * @param types the supported container types
     * 
     * @return JSON object with 2 properties, {@link CmsADEManager#P_ELEMENTS} and {@link CmsADEManager#P_HASMORE}
     * 
     * @throws JSONException if something goes wrong
     */
    protected JSONObject getSearchResult(CmsSearchOptions options, Set<String> types) throws JSONException {

        CmsObject cms = getCmsObject();

        JSONObject result = new JSONObject();
        JSONArray elements = new JSONArray();
        result.put(CmsADEManager.P_ELEMENTS, elements);

        CmsUser user = cms.getRequestContext().currentUser();

        // if there is no type or no text to search, no search is needed 
        if (options.isValid()) {
            // get the configured search index 
            String indexName = new CmsUserSettings(user).getWorkplaceSearchIndexName();

            // get the page size
            Integer pageSize = (Integer)user.getAdditionalInfo(ADDINFO_ADE_SEARCHPAGE_SIZE);
            if (pageSize == null) {
                pageSize = new Integer(DEFAULT_SEARCHPAGE_SIZE);
            }

            // set the search parameters
            CmsSearchParameters params = new CmsSearchParameters(options.getText());
            params.setIndex(indexName);
            params.setMatchesPerPage(pageSize.intValue());
            params.setSearchPage(options.getPage() + 1);
            params.setResourceTypes(options.getTypesAsList());

            // search
            CmsSearch searchBean = new CmsSearch();
            searchBean.init(cms);
            searchBean.setParameters(params);
            searchBean.setSearchRoot(options.getLocation());
            List<CmsSearchResult> searchResults = searchBean.getSearchResult();
            if (searchResults != null) {
                // helper
                CmsElementUtil elemUtil = new CmsElementUtil(
                    cms,
                    getRequest(),
                    getResponse(),
                    getRequest().getParameter(PARAMETER_URI));

                // iterate result list and generate the elements
                Iterator<CmsSearchResult> it = searchResults.iterator();
                while (it.hasNext()) {
                    CmsSearchResult sr = it.next();
                    // get the element data
                    String uri = cms.getRequestContext().removeSiteRoot(sr.getPath());
                    try {
                        JSONObject resElement = elemUtil.getElementData(uri, types);
                        // store element data
                        elements.put(resElement);
                    } catch (Exception e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                    }
                }
            }
            // check if there are more search pages
            int results = searchBean.getSearchPage() * searchBean.getMatchesPerPage();
            boolean hasMore = (searchBean.getSearchResultCount() > results);
            result.put(CmsADEManager.P_HASMORE, hasMore);
            result.put(CmsADEManager.P_COUNT, searchBean.getSearchResultCount());
        } else {
            // no search
            result.put(CmsADEManager.P_HASMORE, false);
            result.put(CmsADEManager.P_COUNT, 0);
        }

        // cache the search options, but with page=0
        m_cache.cacheADESearchOptions(user.getId().toString(), options.resetPage());

        return result;
    }

    /**
     * Saves the new state of the container page.<p>
     * 
     * @param uri the uri of the container page to save
     * @param cntPage the container page data
     * 
     * @throws CmsException if something goes wrong with the cms context
     * @throws JSONException if something goes wrong with the JSON manipulation
     */
    protected void setContainerPage(String uri, JSONObject cntPage) throws CmsException, JSONException {

        CmsObject cms = getCmsObject();
        String paramUri = getRequest().getParameter(PARAMETER_URI);

        cms.lockResourceTemporary(uri);
        CmsFile containerPage = cms.readFile(uri);
        CmsXmlContent xmlCnt = CmsXmlContentFactory.unmarshal(cms, containerPage);
        Locale locale = CmsLocaleManager.getLocale(cntPage.getString(P_LOCALE));
        if (xmlCnt.hasLocale(locale)) {
            // remove the locale 
            xmlCnt.removeLocale(locale);
        }
        xmlCnt.addLocale(cms, locale);

        JSONObject cnts = cntPage.getJSONObject(P_CONTAINERS);
        int cntCount = 0;
        Iterator<String> itCnt = cnts.keys();
        while (itCnt.hasNext()) {
            String cntKey = itCnt.next();
            JSONObject cnt = cnts.getJSONObject(cntKey);

            I_CmsXmlContentValue cntValue = xmlCnt.getValue(CmsContainerPageLoader.N_CONTAINER, locale, cntCount);
            if (cntValue == null) {
                cntValue = xmlCnt.addValue(cms, CmsContainerPageLoader.N_CONTAINER, locale, cntCount);
            }

            String name = cnt.getString(P_NAME);
            xmlCnt.getValue(CmsXmlUtils.concatXpath(cntValue.getPath(), CmsContainerPageLoader.N_NAME), locale, 0).setStringValue(
                cms,
                name);

            String type = cnt.getString(P_TYPE);
            xmlCnt.getValue(CmsXmlUtils.concatXpath(cntValue.getPath(), CmsContainerPageLoader.N_TYPE), locale, 0).setStringValue(
                cms,
                type);

            JSONArray elems = cnt.getJSONArray(P_ELEMENTS);
            for (int i = 0; i < elems.length(); i++) {
                JSONObject elem = cnt.getJSONArray(P_ELEMENTS).getJSONObject(i);

                String formatter = elem.getString(P_FORMATTER);
                String elemUri = elem.getString(P_URI);
                if (type.equals(CmsContainerPageBean.TYPE_TEMPLATE) && elemUri.equals(paramUri)) {
                    // skip main-content if acting as template
                    continue;
                }

                I_CmsXmlContentValue elemValue = xmlCnt.addValue(cms, CmsXmlUtils.concatXpath(
                    cntValue.getPath(),
                    CmsContainerPageLoader.N_ELEMENT), locale, i);
                xmlCnt.getValue(CmsXmlUtils.concatXpath(elemValue.getPath(), CmsContainerPageLoader.N_URI), locale, 0).setStringValue(
                    cms,
                    elemUri);
                xmlCnt.getValue(
                    CmsXmlUtils.concatXpath(elemValue.getPath(), CmsContainerPageLoader.N_FORMATTER),
                    locale,
                    0).setStringValue(cms, formatter);

            }
            cntCount++;
        }
        containerPage.setContents(xmlCnt.marshal());
        cms.writeFile(containerPage);
    }

    /**
     * Sets the favorite list.<p>
     * 
     * @param list the element id list
     * 
     * @throws CmsException if something goes wrong 
     */
    protected void setFavoriteList(JSONArray list) throws CmsException {

        CmsUser user = getCmsObject().getRequestContext().currentUser();
        user.setAdditionalInfo(ADDINFO_ADE_FAVORITE_LIST, list.toString());
        getCmsObject().writeUser(user);
    }

    /**
     * Sets the recent list.<p>
     * 
     * @param list the element id list
     */
    protected void setRecentList(JSONArray list) {

        List<CmsUUID> recentList = getRecentListFromCache();
        recentList.clear();
        for (int i = 0; i < list.length(); i++) {
            try {
                recentList.add(CmsElementUtil.parseId(list.optString(i)));
            } catch (CmsIllegalArgumentException t) {
                LOG.warn(Messages.get().container(Messages.ERR_INVALID_ID_1, list.optString(i)), t);
            }
        }
    }

    /**
     * Checks whether a list of parameters are present as attributes of a request.<p>
     * 
     * If this isn't the case, an error message is written to the JSON result object.
     * 
     * @param request the request which contains the parameters
     * @param result the JSON object which the error message should be written into
     * @param params the array of parameter names which should be checked
     * @return true if and only if all parameters are present in the request
     * @throws JSONException if something goes wrong with JSON
     */
    private boolean checkParameters(HttpServletRequest request, JSONObject result, String... params)
    throws JSONException {

        for (String param : params) {
            String value = request.getParameter(param);
            if (value == null) {
                storeErrorMissingParam(result, param);
                return false;
            }
        }
        return true;
    }

    /**
     * Stores an error message for a missing parameter in a JSON object.
     * 
     * @param result the JSON object in which the error message should be stored
     * @param parameterName the name of the missing parameter
     * @throws JSONException if something goes wrong.
     */
    private void storeErrorMissingParam(JSONObject result, String parameterName) throws JSONException {

        result.put(RES_ERROR, Messages.get().getBundle().key(Messages.ERR_JSON_MISSING_PARAMETER_1, parameterName));
    }
}