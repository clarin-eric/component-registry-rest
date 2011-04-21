<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html
	xmlns="http://www.w3.org/1999/xhtml" lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

<!--  BEGIN Browser History required section -->
<link rel="stylesheet" type="text/css" href="history/history.css" />
<!--  END Browser History required section -->

<title>${title}</title>
<script src="AC_OETags.js" language="javascript"></script>

<!--  BEGIN Browser History required section -->
<script src="history/history.js" language="javascript"></script>
<!--  END Browser History required section -->

<style>
body {
	margin: 0px;
	overflow: hidden
}
</style>
<script type="text/javascript" src="./js/extMouseWheel.js"></script>
<script language="JavaScript" type="text/javascript">
<!--
// -----------------------------------------------------------------------------
// Globals
// Major version of Flash required
var requiredMajorVersion = ${version_major};
// Minor version of Flash required
var requiredMinorVersion = ${version_minor};
// Minor version of Flash required
var requiredRevision = ${version_revision};
// -----------------------------------------------------------------------------
// -->
</script>
</head>

<body scroll="no">
<div id="flashContainerDiv"
	style="position: absolute; width: 100%; height: 100%"><script
	language="JavaScript" type="text/javascript">
<!--
// Version check for the Flash Player that has the ability to start Player Product Install (6.0r65)
var hasProductInstall = DetectFlashVer(6, 0, 65);

// Version check based upon the values defined in globals
var hasRequestedVersion = DetectFlashVer(requiredMajorVersion, requiredMinorVersion, requiredRevision);

if ( hasProductInstall && !hasRequestedVersion ) {
	// DO NOT MODIFY THE FOLLOWING FOUR LINES
	// Location visited after installation is complete if installation is required
	var MMPlayerType = (isIE == true) ? "ActiveX" : "PlugIn";
	var MMredirectURL = window.location;
    document.title = document.title.slice(0, 47) + " - Flash Player Installation";
    var MMdoctitle = document.title;

	AC_FL_RunContent(
		"src", "playerProductInstall",
		"FlashVars", "MMredirectURL="+MMredirectURL+'&MMplayerType='+MMPlayerType+'&MMdoctitle='+MMdoctitle+"",
		"width", "${width}",
		"height", "${height}",
		"align", "middle",
		"id", "${application}",
		"quality", "high",
		"bgcolor", "${bgcolor}",
		"name", "${application}",
		"allowScriptAccess","sameDomain",
		"type", "application/x-shockwave-flash",
		"pluginspage", "http://www.adobe.com/go/getflashplayer"
	);
} else if (hasRequestedVersion) {
	// if we've detected an acceptable version
	// embed the Flash Content SWF when all tests are passed
	AC_FL_RunContent(
			"src", "${ComponentRegistrySwfName}",
			"width", "${width}",
			"height", "${height}",
			"align", "middle",
			"id", "${application}",
			"quality", "high",
			"bgcolor", "${bgcolor}",
			"name", "${application}",
			"allowScriptAccess","sameDomain",
			"type", "application/x-shockwave-flash",
			"pluginspage", "http://www.adobe.com/go/getflashplayer",
			"FlashVars", "serviceRootUrl=${serviceRootUrl}&userName=${pageContext.request.remoteUser}&item=${param.item}&view=${param.view}&space=${param.space}&debug=${flexDebug}"
			    
	);
  } else {  // flash is too old or we can't detect the plugin
    var alternateContent = 'This content requires the Adobe Flash Player. '
   	+ '<a href=http://www.adobe.com/go/getflash/>Get Flash</a>';
    document.write(alternateContent);  // insert non-flash content
  }
// -->
</script>
<noscript><object
	classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
	id="${application}" width="${width}" height="${height}"
	codebase="http://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab">
	<param name="movie" value="${ComponentRegistrySwfName}.swf" />
	<param name="quality" value="high" />
	<param name="bgcolor" value="${bgcolor}" />
	<param name="allowScriptAccess" value="sameDomain" />
	<embed src="${ComponentRegistrySwfName}.swf" quality="high" bgcolor="${bgcolor}"
		width="${width}" height="${height}" name="${application}"
		align="middle" play="true" loop="false" quality="high"
		allowScriptAccess="sameDomain" type="application/x-shockwave-flash"
		pluginspage="http://www.adobe.com/go/getflashplayer"
		FlashVars="serviceRootUrl=${serviceRootUrl}&userName=${pageContext.request.remoteUser}&item=${param.item}&view=${param.view}&space=${param.space}&debug=${flexDebug}">
	</embed> 
</object>
</noscript>
</div>
</body>
</html>
