<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:import href="../../main/xslt/markdown-defs.h.xsl"/>
<xsl:output method="text" encoding="UTF-8"/>

<xsl:param name="href"/>

<xsl:template match="/">

<xsl:call-template name="href">
	<xsl:with-param name="href" select="$href"/>
</xsl:call-template>

</xsl:template>

</xsl:stylesheet>