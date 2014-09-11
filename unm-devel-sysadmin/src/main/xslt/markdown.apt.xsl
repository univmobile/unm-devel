<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text" encoding="UTF-8"/>

<xsl:param name="currentGitHubRepository" select="'unm-devel'"/>
<xsl:param name="projectVersion" select="'0.0.3'"/>
<!--  
<xsl:param name="currentMdFile" select="'unm-devel-sysadmin/README.md'"/>
-->

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

<!--  
<xsl:template match="h1[not(preceding::*)
	and not(preceding::text()[not(normalize-space() = '')])]">
-->
<!-- Do nothing: Already handle with mode="title" -->
<!--  
</xsl:template>
-->

<xsl:template match="*">
<xsl:message terminate="yes">Unknown element: <xsl:value-of
	select="name()"/>
</xsl:message>
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

<xsl:template match="h1 | h2 | h3 | h4 | h5 | h6">
<xsl:text>
</xsl:text>
<xsl:value-of select="normalize-space()"/>
<xsl:text>
</xsl:text>
</xsl:template>

<xsl:variable name="WORKSPACE"
	select="document('../workspace/workspace.xml')/workspace"/>
	
<xsl:variable name="PROJECTS" select="$WORKSPACE/*/gitRepository/@name
	| $WORKSPACE/*/mavenProject/@id"/>

<xsl:variable name="OTHER_JOBS" select="document('markdown.apt.xsl')
	/xsl:stylesheet/xsl:variable[@name = 'OTHER_JOBS']/xsl:comment/job">
<xsl:comment>
	<job>unm-ios-it_ios6</job>
</xsl:comment>	
</xsl:variable>

<xsl:template name="extract-project">
<xsl:param name="text" select="@href"/>
<xsl:choose>
	<xsl:when test="$text = $PROJECTS or $text = 'UnivMobile'">
		<xsl:value-of select="$text"/>	
	</xsl:when>
	<xsl:otherwise>
		<xsl:message terminate="yes">Cannot extract project from: <xsl:value-of
			select="$text"/>
		</xsl:message>
	</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="href">
<xsl:param name="href" select="@href"/>
<!--  
<xsl:message>
	<xsl:value-of select="$href"/>
</xsl:message>
-->
<xsl:choose>
<!-- Online GitHub Repository -->
<xsl:when test="$href = '../README.md'">
	<xsl:value-of select="concat('../../', $currentGitHubRepository,
		'/', $projectVersion, '/index.html')"/>
</xsl:when>
<!-- Online Sub-Project -->
<xsl:when test="starts-with($href, 'https://github.com/univmobile/')
		and contains($href, '/tree/develop/')">
	<xsl:variable name="project">
		<xsl:call-template name="extract-project">
		<xsl:with-param name="text" select="substring-after($href, '/develop/')"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:value-of select="concat('../../', $project,
		'/', $projectVersion, '/index.html')"/>
</xsl:when>
<!-- Online GitHub Repository README.md file -->
<xsl:when test="starts-with($href, 'https://github.com/univmobile/')
		and contains($href, '/blob/develop/README.md')">
	<xsl:variable name="project">
		<xsl:call-template name="extract-project">
		<xsl:with-param name="text" select="substring-before(
			substring-after($href, 'https://github.com/univmobile/'),
				'/blob/develop/README.md')"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:value-of select="concat('../../', $project,
		'/', $projectVersion, '/index.html')"/>
</xsl:when>
<!-- Online GitHub Repository .md file -->
<xsl:when test="starts-with($href, 'https://github.com/univmobile/')
		and contains($href, '/blob/develop/src/site/markdown/')
		and contains($href, '.md')">
	<xsl:variable name="project">
		<xsl:call-template name="extract-project">
		<xsl:with-param name="text" select="substring-before(
			substring-after($href, 'https://github.com/univmobile/'),
				'/blob/develop/src/site/markdown/')"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:value-of select="concat('../../', $project,
		'/', $projectVersion, '/',
		substring-before(substring-after($href, 'src/site/markdown/'), '.md'),
		'.html')"/>
</xsl:when>
<!-- Sub-Project README.md file -->
<xsl:when test="starts-with($href, 'https://github.com/univmobile/')
		and contains($href, '/blob/develop/')
		and contains($href, '/README.md')">
	<xsl:variable name="project">
		<xsl:call-template name="extract-project">
		<xsl:with-param name="text" select="substring-before(
			substring-after($href, '/blob/develop/'),
				'/README.md')"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:value-of select="concat('../../', $project,
		'/', $projectVersion, '/index.html')"/>
</xsl:when>
<!-- Sub-Project .md file -->
<xsl:when test="starts-with($href, 'https://github.com/univmobile/unm-')
		and contains($href, '/blob/develop/')
		and contains($href, '/src/site/markdown/')
		and contains($href, '.md')">
	<xsl:variable name="project">
		<xsl:call-template name="extract-project">
		<xsl:with-param name="text" select="substring-before(
			substring-after($href, '/blob/develop/'),
				'/src/site/markdown/')"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:value-of select="concat('../../', $project,
		'/', $projectVersion, '/',
		substring-before(substring-after($href, 'src/site/markdown/'), '.md'),
		'.html')"/>
</xsl:when>
<!-- Online GitHub Repository -->
<xsl:when test="starts-with($href, 'https://github.com/univmobile/unm-')">
	<xsl:variable name="project">
		<xsl:call-template name="extract-project">
		<xsl:with-param name="text" select="substring-after($href, '/univmobile/')"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:value-of select="concat('../../', $project,
		'/', $projectVersion, '/index.html')"/>
</xsl:when>
<!-- Online GitHub Repository -->
<xsl:when test="starts-with($href, '../../../unm-')
		and contains($href, '/blob/develop/README.md')">
	<xsl:variable name="project">
		<xsl:call-template name="extract-project">
		<xsl:with-param name="text"
			select="substring-before(substring-after($href, '../../../'), '/blob/develop/')"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:value-of select="concat('../../', $project,
		'/', $projectVersion, '/index.html')"/>
</xsl:when>
<!-- Online Maven Generated Sites -->
<xsl:when test="starts-with($href, 'http://univmobile.vswip.com/nexus/content/sites/pub/')">
	<xsl:value-of select="$href"/>
</xsl:when>
<!-- Jenkins Jobs -->
<xsl:when test="starts-with($href, 'http://univmobile.vswip.com/job/')
		and (substring-before(substring-after($href, 'http://univmobile.vswip.com/job/'), '/')
			= $PROJECTS
		or substring-before(substring-after($href, 'http://univmobile.vswip.com/job/'), '/')
			= $OTHER_JOBS)">
	<xsl:value-of select="$href"/>
</xsl:when>
<!-- Known URLs -->
<xsl:when test="$href = 'http://univmobile.vswip.com/unm-backend-mock/'">
	<xsl:value-of select="$href"/>
</xsl:when>
<!-- Direct link to .md file -->
<xsl:when test="not(contains($href, '/')) and contains($href, '.md')">
	<xsl:value-of select="concat('./', substring-before($href, '.md'), '.html')"/>
</xsl:when>
<!-- Direct link to a README.md file -->
<xsl:when test="contains($href, '/') and contains($href, 'README.md')">
	<xsl:variable name="project">
		<xsl:call-template name="extract-project">
		<xsl:with-param name="text"
			select="substring-before($href, '/')"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:value-of select="concat('../../', $project, 
		'/', $projectVersion, '/index.html')"/>
</xsl:when>
<!-- Direct link to a .md file -->
<xsl:when test="contains($href, '/') and contains($href, '.md')">
	<xsl:variable name="project">
		<xsl:call-template name="extract-project">
		<xsl:with-param name="text"
			select="substring-before($href, '/')"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:value-of select="concat('../../', $project, 
		'/', $projectVersion, '/', substring-before($href, '.md'), '.html')"/>
</xsl:when>
<!-- Default: Error -->
<xsl:otherwise>
	<xsl:message terminate="yes">Unknown @href: <xsl:value-of
		select="$href"/>
	</xsl:message>
</xsl:otherwise>
</xsl:choose>
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