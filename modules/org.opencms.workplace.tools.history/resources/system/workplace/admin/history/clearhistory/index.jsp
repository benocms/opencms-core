<%@ page
	import="org.opencms.workplace.tools.history.CmsAdminHistoryClear,
                org.opencms.workplace.CmsDialog" %>
<%	
	// initialize the workplace class
	CmsAdminHistoryClear wp = new CmsAdminHistoryClear(pageContext, request, response);

//////////////////// start of switch statement 
switch (wp.getAction()) {
    case CmsAdminHistoryClear.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed
	wp.actionCloseDialog();
	break;

    case CmsAdminHistoryClear.ACTION_SAVE_EDIT:
    case CmsDialog.ACTION_REPORT_BEGIN:
    case CmsDialog.ACTION_REPORT_UPDATE:
    case CmsDialog.ACTION_REPORT_END:
//////////////////// ACTION: clear history
	wp.actionEdit();
	break;

    case CmsAdminHistoryClear.ACTION_DEFAULT:
    default:
//////////////////// ACTION: show history settings dialog (default)
	wp.setParamAction(wp.DIALOG_SAVE_EDIT);

%>

<%= wp.htmlStart("administration/index.html") %>
<%= wp.bodyStart(null) %>
<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%= wp.calendarIncludes() %>
<script type="text/javascript">
<!--
function disallowDate(date) {
  var curDate = new Date();
  if (date.getTime() > curDate.getTime()) {
  	return true;
  }
  return false;
}

function validateForm() {
  // exactly one of the values must be given
  if (document.main.date.value == "" && document.main.versions.value == "") {
    alert("<%= wp.key(org.opencms.workplace.tools.history.Messages.ERR_MESSAGE_INVALIDCLEARHISTORYDATA_0) %>");
    return false;
  }
  if (document.main.date.value != "" && document.main.versions.value != "") {
    alert("<%= wp.key(org.opencms.workplace.tools.history.Messages.ERR_MESSAGE_DATAMISSING_0) %>");
    return false;
  } 
  return true;
}

//-->
</script>


<form name="main" class="nomargin" action="<%= wp.getDialogUri() %>" method="post" onsubmit="if(validateForm()) {return submitAction('<%= wp.DIALOG_OK %>', null, 'main');} return false;">
<%= wp.paramsAsHidden() %>
<% if (wp.getParamFramename()==null) { %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">
<%  } %>

<%= wp.buildClearForm() %>
<%= wp.dialogContentEnd() %>
<%= wp.dialogButtonsOkCancel() %>

</form>

<%= wp.dialogEnd() %>

<%
    /**
     * @param inputFieldId the ID of the input field where the date is pasted to
     * @param triggerButtonId the ID of the button which triggers the calendar
     * @param align initial position of the calendar popup element
     * @param singleClick if true, a single click selects a date and closes the calendar, otherwise calendar is closed by doubleclick
     * @param weekNumbers show the week numbers in the calendar or not
     * @param mondayFirst show monday as first day of week
     * @param disableFunc JS function which determines if a date should be disabled or not
     */

%><%
	if (wp.isHistoryEnabled()) {
		out.print(wp.calendarInit("date", "triggercalendar", "tR", false, false, true, "disallowDate"));
	}
%>

<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>