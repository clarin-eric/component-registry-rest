<%-- Configuration hosted next to the front end index page, from which it is loaded --%>
<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.net.URI" %>
<%@ page import="clarin.cmdi.componentregistry.rest.ComponentRegistryRestService" %>
<%
    final String loglevelParam = request.getParameter("loglevel");
    final String loglevel;
    if(loglevelParam != null) {
        loglevel = loglevelParam;
    } else {
        loglevel = "info" ;
    }
    
    final String appUrl = ComponentRegistryRestService.getApplicationBaseURI(getServletContext(), request);
%>
{
  "loglevel": "<%= loglevel %>",
  "cors": false,
  "REST": {
    "url": "<%= appUrl %>"
  },
  "backEndVersion": "${project.version}",
  "deploy": {
    "path": "<%= 
                URI.create(appUrl).getPath() //at same URL as app, so simply take path
            %>"
  }
}
