<%@ page import="
	org.opencms.workplace.*,
	org.opencms.workplace.galleries.*,
	org.opencms.main.*,
	org.opencms.file.CmsResource" 
%><%	
	CmsDialog wp = new CmsDialog(pageContext, request, response);
    A_CmsGallery.initGallery(wp);    
    response.sendRedirect(wp.getJsp().link("/system/workplace/views/explorer/explorer_files.jsp?resource=/system/workplace/&mode=galleryview"));
%>