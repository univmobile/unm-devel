<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:import href="markdown-defs.h.xsl"/>
<xsl:output method="text" encoding="UTF-8"/>

<xsl:template match="/*">

<xsl:apply-templates select="h1[not(preceding::*)
	and not(preceding::text()[not(normalize-space() = '')])]" mode="title"/>
	
<xsl:apply-templates/>

</xsl:template>

<xsl:template match="h1" mode="title">
<xsl:text>  ---</xsl:text>
<xsl:call-template name="par-indent"/>
<xsl:value-of select="normalize-space()"/>
<xsl:call-template name="par-indent"/>
<xsl:text>---</xsl:text>
<xsl:call-template name="par-indent"/>
</xsl:template>

<xsl:template match="p">
<xsl:call-template name="par-indent"/>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="text()">
<xsl:call-template name="text"/>
</xsl:template>

<xsl:template name="text">
<xsl:param name="text" select="."/>
<xsl:param name="offset" select="1"/>

<xsl:if test="$offset &lt;= string-length($text)">
	<xsl:variable name="c" select="substring($text, $offset, 1)"/>
	<xsl:choose>
		<xsl:when test="$c = '&#10;' or $c = '&#13;'">
			<xsl:call-template name="par-indent"/>			
		</xsl:when>
		<xsl:when test="$c = '&lt;' or $c = '&gt;'
			or $c = '~' or $c = '=' or $c = '-' or $c = '+'
			or $c = '*' or $c = '\' 
			or $c= '[' or $c = ']' or $c = '{' or $c = '}'">
		<xsl:text>\</xsl:text>
		<xsl:value-of select="$c"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="$c"/>
		</xsl:otherwise>
	</xsl:choose>
	<!-- terminal recursion -->
	<xsl:call-template name="text">
		<xsl:with-param name="text" select="$text"/>
		<xsl:with-param name="offset" select="$offset + 1"/>
	</xsl:call-template>
</xsl:if>

</xsl:template>

<xsl:template match="em">
<xsl:text>&lt;</xsl:text>
<xsl:apply-templates/>
<xsl:text>&gt;</xsl:text>
</xsl:template>

<xsl:template match="strong">
<xsl:text>&lt;&lt;</xsl:text>
<xsl:apply-templates/>
<xsl:text>&gt;&gt;</xsl:text>
</xsl:template>

<xsl:template match="code">
<xsl:text>&lt;&lt;&lt;</xsl:text>
<xsl:apply-templates/>
<xsl:text>&gt;&gt;&gt;</xsl:text>
</xsl:template>

<xsl:template match="ul">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="ol">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="li">
<xsl:call-template name="par-indent"/>
<xsl:choose>
	<xsl:when test="parent::ul">  * </xsl:when>
	<xsl:when test="parent::ol">  [[1]] </xsl:when>
	<xsl:otherwise>
		<xsl:message terminate="yes">Unknown li/parent: <xsl:value-of
			select="name(parent::*)"/>
		</xsl:message>
	</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates/>
</xsl:template>

<xsl:template name="par-indent">
<xsl:text>
  </xsl:text>
<xsl:for-each select="ancestor::li[parent::ul]">
	<xsl:text>    </xsl:text>
</xsl:for-each>
<xsl:for-each select="ancestor::li[parent::ol]">
	<xsl:text>        </xsl:text>
</xsl:for-each>
</xsl:template>
 
<xsl:template name="parent-header">
<xsl:param name="header" select="self::*"/>
<xsl:variable name="level" select="substring(name($header), 2, 1)"/>
<xsl:variable name="preceding" select="$header/preceding::h1
	| $header/preceding::h2 | $header/preceding::h3
	| $header/preceding::h4 | $header/preceding::h5 | $header/preceding::h6"/>
<xsl:variable name="higher"
	select="$preceding[substring(name(), 2, 1) &lt; $level]"/>
<xsl:value-of select="name($higher[last()])"/>
</xsl:template>

<xsl:template name="header-level">
<xsl:variable name="parent-headers">
	<xsl:call-template name="parent-headers"/>
</xsl:variable>
<!--  
<xsl:message>
<xsl:value-of select="name()"/>: <xsl:value-of select="$parent-headers"/>
</xsl:message>
-->
<xsl:value-of select="1 + string-length($parent-headers) div 2"/>
</xsl:template>

<xsl:template name="parent-headers">
<xsl:param name="header" select="self::*"/>
<xsl:variable name="parent-header">
	<xsl:call-template name="parent-header">
		<xsl:with-param name="header" select="$header"/>
	</xsl:call-template>
</xsl:variable>
<xsl:if test="not($parent-header = '')">
	<xsl:call-template name="parent-headers">
		<xsl:with-param name="header" select="$header/preceding::*
			[name() = $parent-header][last()]"/>
	</xsl:call-template>
	<xsl:value-of select="$parent-header"/>
</xsl:if>

</xsl:template>

<xsl:template match="h1 | h2 | h3 | h4 | h5 | h6">
<xsl:text>
</xsl:text>
<xsl:variable name="header-level">
	<xsl:call-template name="header-level"/>
</xsl:variable>
<xsl:if test="$header-level = '2'">* </xsl:if>
<xsl:if test="$header-level = '3'">** </xsl:if>
<xsl:if test="$header-level = '4'">*** </xsl:if>
<xsl:if test="$header-level = '5'">**** </xsl:if>
<xsl:if test="$header-level = '6'">**** </xsl:if>
<xsl:value-of select="normalize-space()"/>
<xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="a">
<xsl:comment>
	href: <xsl:call-template name="href"/>
</xsl:comment>
<xsl:variable name="href">
	<xsl:call-template name="href"/>
</xsl:variable>
<xsl:text>{{{</xsl:text>
<xsl:value-of select="$href"/>
<xsl:text>}</xsl:text>
<xsl:value-of select="."/>
<xsl:text>}}</xsl:text>
</xsl:template>

<xsl:template match="pre[code]">
+---
<xsl:value-of select="."/>
+---
</xsl:template>

</xsl:stylesheet>