<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text" encoding="UTF-8"/>

<xsl:param name="currentGitHubRepository" select="'unm-devel'"/>
<xsl:param name="projectVersion" select="'0.0.3'"/>

<xsl:template match="*">
<xsl:message terminate="yes">Unknown element: <xsl:value-of
	select="name()"/>
</xsl:message>
</xsl:template>

<xsl:variable name="WORKSPACE"
	select="document('workspace.xml')/workspace"/>
	
<xsl:variable name="PROJECTS" select="$WORKSPACE/*/gitRepository/@name
	| $WORKSPACE/*/mavenProject/@id"/>

<xsl:variable name="OTHER_JOBS" select="document('markdown-defs.h.xsl')
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
<xsl:message>href: <xsl:value-of select="$href"/></xsl:message>
-->
<xsl:choose>
<!-- Current GitHub Repository -->
<xsl:when test="$href = '../README.md'">
	<xsl:value-of select="concat('../../', $currentGitHubRepository,
		'/', $projectVersion, '/index.html')"/>
</xsl:when>
<!-- Online GitHub Repository -->
<xsl:when test="starts-with($href, 'https://github.com/univmobile/')
		and not(contains(substring-after($href,
			'https://github.com/univmobile/'), '/'))">
	<xsl:value-of select="$href"/>
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
		and (contains($href, '/blob/develop/README.md')
		or contains($href, '/blob/develop//README.md'))">
	<xsl:variable name="project">
		<xsl:call-template name="extract-project">
		<xsl:with-param name="text" select="substring-before(
			substring-after($href, 'https://github.com/univmobile/'),
				'/blob/develop/')"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:value-of select="concat('../../', $project,
		'/', $projectVersion, '/index.html')"/>
</xsl:when>
<!-- Online GitHub Repository .md file -->
<xsl:when test="starts-with($href, 'https://github.com/univmobile/')
		and contains($href, '/blob/develop/')
		and contains($href, '.md')
		and not(contains(substring-after($href, '/blob/develop/'), '/'))">
	<xsl:variable name="project">
		<xsl:call-template name="extract-project">
		<xsl:with-param name="text" select="substring-before(
			substring-after($href, 'https://github.com/univmobile/'),
				'/blob/develop/')"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:value-of select="concat('../../', $project,
		'/', $projectVersion, '/', substring-before(
			substring-after($href, '/blob/develop/'), '.md'), '.html')"/>
</xsl:when>
<!-- Online Sub-Project README.md file -->
<xsl:when test="starts-with($href, 'https://github.com/univmobile/unm-')
		and contains($href, '/blob/develop/')
		and contains($href, 'README.md')">
	<xsl:variable name="project">
		<xsl:call-template name="extract-project">
		<xsl:with-param name="text" select="substring-before(
			substring-after($href, '/blob/develop/'), '/')"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:value-of select="concat('../../', $project,
		'/', $projectVersion, '/index.html')"/>
</xsl:when>
<!-- Online GitHub Repository .md file -->
<xsl:when test="starts-with($href, 'https://github.com/univmobile/unm-')
		and contains($href, '/blob/develop/')
		and contains($href, '.md')">
	<xsl:variable name="project">
		<xsl:call-template name="extract-project">
		<xsl:with-param name="text" select="substring-before(
			substring-after($href, '/blob/develop/'), '/')"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:value-of select="concat('../../', $project,
		'/', $projectVersion, '/',
		substring-before(substring-after(
			substring-after($href, '/blob/develop/'), '/'), '.md'),
		'.html')"/>
</xsl:when>
<!-- Sub-Project .md file -->
<!-- 
<xsl:when test="starts-with($href, 'https://github.com/univmobile/unm-')
		and contains($href, '/blob/develop/')
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
-->
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
<xsl:when test="$href = 'http://univmobile.vswip.com/unm-backend-mock/'
		or starts-with($href, 'http://univmobile.vswip.com/nexus/content/repositories/')
		or starts-with($href, 'http://repo.avcompris.net/content/repositories/')
		or $href = 'http://univmobile.vswip.com/job/unm-ios-it_ios6/'
		or starts-with($href, 'http://www.u-paris10.fr/')
		or starts-with($href, 'http://deimos.apple.com/WebObjects/Core.woa/Browse/univ-paris1.fr')
		or starts-with($href, 'http://epi.univ-paris1.fr/')
		or $href = 'https://admin.univmobile.fr/'
		or starts-with($href, 'http://www.univ-paris3.fr/')">
	<xsl:value-of select="$href"/>
</xsl:when>
<!-- Direct link to the README.md file -->
<xsl:when test="not(contains($href, '/')) and contains($href, 'README.md')">
	<xsl:value-of select="concat('./index.html')"/>
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

</xsl:stylesheet>