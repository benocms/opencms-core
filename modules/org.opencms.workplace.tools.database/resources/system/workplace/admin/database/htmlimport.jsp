<%@ page import ="org.opencms.jsp.CmsJspActionElement,
  				 	 	org.opencms.file.CmsObject,
				 		java.util.*,
				 		org.opencms.workplace.*,
						org.opencms.workplace.commons.*,
				 		org.opencms.workplace.editors.CmsDialogElements,
				 		org.opencms.workplace.tools.database.*,
				 		org.opencms.workplace.*, 
						org.opencms.workplace.administration.*" %>
										 
<%	
	// initialise Cms Action Element
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
    
	// Collect the objects required to access the OpenCms VFS from the request
	CmsObject cmsObject = cms.getCmsObject();
	String uri = cmsObject.getRequestContext().getUri(); 
	String errorMessage = "";	
	
	// initialize the workplace class
	CmsHtmlImportReport wp = new CmsHtmlImportReport(pageContext, request, response);	
		
	// initialize the import class	
	CmsHtmlImport imp = new CmsHtmlImport( cms, request);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsHtmlImportReport.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;

case CmsDialog.ACTION_CONFIRMED:
case CmsDialog.ACTION_REPORT_BEGIN:
case CmsDialog.ACTION_REPORT_UPDATE:
case CmsDialog.ACTION_REPORT_END:
//////////////////// ACTION: clear history			
	
	boolean isValid = true;															
	if (wp.getAction() == CmsDialog.ACTION_CONFIRMED) {
		imp.checkParameters();
	}
	if (isValid) {
		wp.setHtmlImport(imp);
		wp.actionReport();
		break;
	}


//////////////////// ACTION: show start dialog
case CmsDialog.ACTION_DEFAULT:
default:

	wp.setParamAction(CmsDialog.DIALOG_CONFIRMED);	
%>

<%= wp.htmlStart("administration/index.html") %>

<%= wp.bodyStart("onunload='top.closeTreeWin();'") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" class="nomargin" action="<%= wp.getDialogUri() %>" method="post" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<% if(errorMessage.length()>0) { %>
<%= wp.dialogBlockStart(null) %>
<table border="0" cellpadding="4" cellspacing="0">
	<tr>
		<td style="vertical-align: middle;"><img src="<%=wp.getSkinUri() %>commons/error.png" border="0"></td>
		<td style="vertical-align: middle;"><%= errorMessage %></td> 
	</tr> 
</table>
<%= wp.dialogBlockEnd() %>
<% } %>

<table border="0">
	<tr>
		<td><%= wp.key("htmlimport.input.source") %>:	</td>
		<td colspan="2"><input name="inputDir" type="text" size="80" maxlength="1024" value="<%= imp.getInputDir() %>"></td>
	</tr>
	<tr>
		<td><%= wp.key("htmlimport.input.destination") %>:</td>
		<td><input name="destinationDir" type="text" size="80" maxlength="256" value="<%= imp.getDestinationDir() %>"></td>
		<td><%=wp.button("javascript:top.openTreeWin('html_import1', false, 'main', 'destinationDir', document);", null, "folder", org.opencms.workplace.tools.database.Messages.GUI_BUTTON_SEARCH_0, 0)%></td>
	</tr>
	<tr>
		<td><%= wp.key("htmlimport.input.imagegallery") %>: </td>
		<td><input name="imageGallery" type="text" size="80" maxlength="256"  value="<%= imp.getImageGallery() %>"></td>
		<td><%=wp.button("javascript:top.openTreeWin('copy', false, 'main', 'imageGallery', document);", null, "folder", org.opencms.workplace.tools.database.Messages.GUI_BUTTON_SEARCH_0, 0)%></td>

	</tr>
	<tr>
		<td><%= wp.key("htmlimport.input.linkgallery") %>:</td>
		<td><input name="linkGallery" type="text" size="80" maxlength="256" value="<%= imp.getLinkGallery() %>"></td>
		<td><%=wp.button("javascript:top.openTreeWin('copy', false, 'main', 'linkGallery', document);", null, "folder", org.opencms.workplace.tools.database.Messages.GUI_BUTTON_SEARCH_0, 0)%></td>

	</tr>
	<tr>
		<td><%= wp.key("htmlimport.input.downloadgallery") %>:</td>
		<td><input name="downloadGallery" type="text" size="80" maxlength="256" value="<%= imp.getDownloadGallery() %>"></td>
		<td><%=wp.button("javascript:top.openTreeWin('copy', false, 'main', 'downloadGallery', document);", null, "folder", org.opencms.workplace.tools.database.Messages.GUI_BUTTON_SEARCH_0, 0)%></td>
	</tr>
	<tr>
		<td><%= wp.key("htmlimport.input.template") %>:</td>
		<td><%= CmsHtmlImportBackoffice.getTemplates(cmsObject,imp.getTemplate()) %></td>
	</tr>
	<tr>
		<td><%= wp.key("htmlimport.input.element") %>:</td>
		<td colspan="2" ><input name="element" type="text" size="80" maxlength="80" value="<%= imp.getElement() %>"></td>
	</tr>
	<tr>
		<td><%= wp.key("htmlimport.input.locale") %>:</td>
		<td colspan="2" ><%= CmsHtmlImportBackoffice.getLocales(imp.getLocale()) %></td>
	</tr>
	<tr>
		<td><%= wp.key("htmlimport.input.encoding") %>:</td>
		<td colspan="2" ><input name="encoding" type="text" size="80" maxlength="80" value="<%= imp.getInputEncoding() %>"></td>
	</tr>
	<tr>
		<td><%= wp.key("htmlimport.input.startregexp") %>:</td>
		<td colspan="2" ><input name="startPattern" type="text" size="80" maxlength="80" value="<%= imp.getStartPattern() %>"></td>
	</tr>
	<tr>
		<td><%= wp.key("htmlimport.input.endregexp") %>:</td>
		<td colspan="2" ><input name="endPattern" type="text" size="80" maxlength="80" value="<%= imp.getEndPattern() %>"></td>
	</tr>
	<tr>
		<td><%= wp.key("htmlimport.input.overwrite") %>: <input name="overwrite" type="checkbox" <%= imp.getOverwrite() %> value="checked" ></td>
		<td colspan="2" ></td>
	</tr>
</table>

<%= wp.dialogContentEnd() %>
<%= wp.dialogButtonsOkCancel() %>

</form>

<%= wp.dialogEnd() %>

<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>