<%@page import="org.opencms.ade.galleries.CmsGalleryActionElement, org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants" taglibs="cms"%><%
  CmsGalleryActionElement gallery = new CmsGalleryActionElement(pageContext, request, response);
%><!DOCTYPE HTML>
<html>
  <head>
  	<title><%= gallery.getTitle() %></title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <%= gallery.exportAll() %>
    <style type="text/css">
    	* { 
    		zoom: 1; 
    	}
    </style>
    <% if (gallery.isEditorMode()){ /* opened from rich text editor (FCKEditor, CKEditor...) include necessary scripts */ %>
    <script type="text/javascript" src="<cms:link>/system/workplace/editors/fckeditor/plugins/galleries/integrator.js</cms:link>"></script>
    <% } else if (gallery.isWidgetMode()){ /* opened as widget include necessary scripts */ %>
    <script type="text/javascript">
    	var <%= I_CmsGalleryProviderConstants.KEY_FIELD_ID %> = '<%= request.getParameter(I_CmsGalleryProviderConstants.ReqParam.fieldid.name())%>';
    	var <%= I_CmsGalleryProviderConstants.KEY_HASH_ID %> = '<%= request.getParameter(I_CmsGalleryProviderConstants.ReqParam.hashid.name())%>';
    </script>
    <% } %>
  </head>
  <body>
  	<div id="<%= I_CmsGalleryProviderConstants.GALLERY_DIALOG_ID %>"></div>
  </body>
</html>
