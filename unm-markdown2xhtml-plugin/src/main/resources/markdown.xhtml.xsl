<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:import href="markdown-defs.h.xsl"/>
<xsl:output method="xml" encoding="UTF-8"/>

<xsl:param name="currentGitHubRepository" select="'unm-devel'"/>
<xsl:param name="projectVersion" select="'0.0.3'"/>

<xsl:template match="/*">
<html>
<body>
<xsl:apply-templates/>
</body>
</html>
</xsl:template>

<xsl:template match="*">
<xsl:message terminate="yes">Unknown element: <xsl:value-of
	select="name()"/>
</xsl:message>
</xsl:template>

<xsl:template match="h1 | h2 | h3 | h4 | h5 | h6
	| a | img
	| p | ul | ol | li
	| em | strong | code
	| pre">

<xsl:copy>
<xsl:copy-of select="@*"/>

<xsl:apply-templates/>

</xsl:copy>
</xsl:template>

<xsl:template match="a">

<xsl:variable name="href">
	<xsl:call-template name="href"/>
</xsl:variable>

<xsl:if test="contains($href, '/.html')">
	<xsl:message>$href=<xsl:value-of select="$href"/>
	</xsl:message>
	<xsl:message terminate="yes">Error when extracting @href: <xsl:value-of
		select="@href"/>	
	</xsl:message>
</xsl:if>

<xsl:copy>
<xsl:copy-of select="@*"/>
<xsl:attribute name="href">
	<xsl:call-template name="href"/>
</xsl:attribute>
<xsl:apply-templates/>
</xsl:copy>
</xsl:template>

<xsl:template match="pre[code]">
<xsl:copy>
<xsl:copy-of select="@*"/>
<xsl:attribute name="style">
	border: 1px solid #ccc;
	padding: 8px 12px;
</xsl:attribute>
<xsl:apply-templates/>
</xsl:copy>
</xsl:template>

</xsl:stylesheet>