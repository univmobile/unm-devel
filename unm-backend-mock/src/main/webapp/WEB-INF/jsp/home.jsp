<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="en" dir="ltr">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-Language" content="en">
<title>unm-backend-mock</title>
<style type="text/css">
body {
	background-color: #fff;
	color: #000;
	position: relative;
}
div.layout {
	display: table;
}
#div-form {
	float: right;
	margin-left: 0.5em;
	padding-left: 0.8em;
	border-left: 1px dashed #000;
}
#div-pathlist {
	xbackground-color: #ff0;
	margin-right: 0.5em;
	display: table;
	white-space: nowrap;
}
#div-pathlist ul {
	margin: 0;
	padding: 0;
	list-style-type: none;
}
#div-pathlist li {
	margin: 2px 0 0;
	padding: 1px 0.8em;
	background-color: #ccc;
}
#div-pathlist li a,
#div-pathlist li a:visited {
	color: #00f;
	text-decoration: none;
}
#div-pathlist li,
#text-path,
#text-content {
	font-family: monospace;
	font-size: 10px;
}
#text-path {
	width: 20em;
}
#text-content {
	width: 800px;
	height: 400px;
	margin-top: 0.4em;
}
#div-form table {
	border-collapse: collapse;
	margin: 0;
}
#div-form td,
#div-form th {
	vertical-align: top;
}
#div-form th {
	font-weight: bold;
	text-align: right;
}
span.error {
	color: #900;
	font-weight: bold;
	font-variant: small-caps;
	background-color: #ddd;
	padding: 0 0.4em;
}
</style>
</head>
<body>

<h1>unm-backend-mock</h1>

<div class="layout">

<div id="div-form">
<form method="POST" action="${contextRoot}">

	Path:
	<br>
	<input type="text" name="path" id="text-path" value="${path}">
	<input type="submit" name="submit" id="submit" value="Save">
	
	<c:if test="${pathIsInvalid}">
	<span class="error">
	Path is invalid
	</span>
	</c:if>
	
	<br>
	<br>

	JSON Content:
	<input type="button" name="clear" id="button-clear" value="Clear"
		onclick="document.getElementById('text-content').value = '';">
	<br>
	<textarea name="content" id="text-content"><c:out
		value="${content}"/></textarea>

</form>
</div>

<div id="div-pathlist">
Saved paths:
<ul>
<c:forEach var="key" items="${keys}">

<li>
	<a href="${contextRoot}${key}">JSON</a>
	<a href="${contextRoot}?path=${key}">${key}</a>

</c:forEach>

</ul>
</div>

</div>

</body>
</html>