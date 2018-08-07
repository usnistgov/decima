<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:res="http://csrc.nist.gov/ns/decima/results/1.0"
	xmlns:req="http://csrc.nist.gov/ns/decima/requirements/1.0"
	xmlns:decima="http://decima.nist.gov/xsl/extensions"
	exclude-result-prefixes="#all" version="2.0">

	<!-- ****************************** -->
	<!-- * Parameters * -->
	<!-- ****************************** -->
	<!-- Parameters to tailor the output -->
	<xsl:param name="ignore-not-tested-results" select="true()" />
	<xsl:param name="ignore-outofscope-results" select="true()" />
	<xsl:param name="generate-xml-output" select="true()" />
	<xsl:param name="xml-output-depth" select="1" />
	<xsl:param name="xml-output-child-limit" select="10" />
	<!-- The test-result-limit parameter indicates the maximum number of test 
		results that should be rendered for a given derived requirement. A postive 
		number will enforce the limit, while a zero or negative number will result 
		in rendering all entries. -->
	<xsl:param name="test-result-limit" select="10" />
	<xsl:param name="html-title" select="'Validation Report'" />

	<!-- Parameters for use in calling stylesheets -->
	<xsl:param name="has-requirement-categorizations"
		select="false()" />

	<!-- doctype-system="about:legacy-compat" -->
	<!-- <xsl:output method="xhtml" encoding="utf-8" indent="yes" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" -->
	<!-- doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" /> -->
	<xsl:output method="html" version="5.0" encoding="utf-8"
		indent="yes" />

	<xsl:variable name="requirements"
		select="document(/res:assessment-results/res:requirements/res:requirement/@href)/req:requirements" />

	<xsl:key name="requirement-index"
		match="req:requirement|req:requirement/req:derived-requirements/req:derived-requirement"
		use="@id" />
	<xsl:key name="resource-index" match="req:resource" use="@id" />

	<!-- ****************************** -->
	<!-- * Extension Points * -->
	<!-- ****************************** -->
	<!-- For use in calling templates to customize output for a specific application -->
	<xsl:template name="process-categorizations" />
	<xsl:template name="process-header" />
	<xsl:template name="process-validation-details" />
	<xsl:template name="process-validation-summary" />

	<!-- ****************************** -->
	<!-- * Main output template * -->
	<!-- ****************************** -->
	<xsl:template match="/">
		<html>
			<head>
				<meta http-equiv="X-UA-Compatible" content="IE=edge" />
				<meta name="viewport"
					content="width=device-width, initial-scale=1" />
				<title>
					<xsl:value-of select="$html-title" />
				</title>
				<script
					src="https://code.jquery.com/jquery-3.2.1.slim.min.js"
					integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN"
					crossorigin="anonymous"></script>
				<script
					src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js"
					integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q"
					crossorigin="anonymous"></script>

				<link rel="stylesheet"
					href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css"
					integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm"
					crossorigin="anonymous"></link>
				<script
					src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js"
					integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl"
					crossorigin="anonymous"></script>

				<link rel="stylesheet"
					href="https://gitcdn.github.io/bootstrap-toggle/2.2.2/css/bootstrap-toggle.min.css"
					integrity="sha384-yBEPaZw444dClEfen526Q6x4nwuzGO6PreKpbRVSLFCci3oYGE5DnD1pNsubCxYW"
					crossorigin="anonymous"></link>
				<script
					src="https://gitcdn.github.io/bootstrap-toggle/2.2.2/js/bootstrap-toggle.min.js"
					integrity="sha384-cd07Jx5KAMCf7qM+DveFKIzHXeCSYUrai+VWCPIXbYL7JraHMFL/IXaCKbLtsxyB"
					crossorigin="anonymous"></script>

				<link rel="stylesheet"
					href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.11.1/bootstrap-table.min.css"
					integrity="sha384-5nsQ1S/tkV3k79V0HFUsh6FgjFOy7J6Y7zOcNtQRswfMabDb4WrPwSfNBD3fkGBV"
					crossorigin="anonymous"></link>
				<script
					src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.11.1/bootstrap-table.min.js"
					integrity="sha384-cKm/6JlIlt9zZV/g+/NqAgMrygFeVtQsI+CWNQtldXF33rCqZYtVL/QH6UTDmQAn"
					crossorigin="anonymous"></script>

				<script
					src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.7.1/Chart.min.js"
					integrity="sha384-U3SHMuMFK4M7q42jpyqaAHJTzci8BikdB2ZgmUEsrElciy0Ty1vxiW4EL3bNCsyT"
					crossorigin="anonymous"></script>

				<script
					src="https://cdnjs.cloudflare.com/ajax/libs/clipboard.js/1.7.1/clipboard.min.js"
					integrity="sha384-cV+rhyOuRHc9Ub/91rihWcGmMmCXDeksTtCihMupQHSsi8GIIRDG0ThDc3HGQFJ3"
					crossorigin="anonymous"></script>

				<style>
					/* fixes for toggle on BS4 */
					.toggle-off {
					box-shadow: inset 0 3px 5px rgba(0, 0, 0, .125);
					}
					.toggle.off {
					border-color: rgba(0, 0, 0, .25);
					}

					.toggle-handle {
					background-color: white;
					border: thin rgba(0, 0, 0, .25) solid;
					}
					blockquote {
					border-left: 5px solid #ccc;
					padding-left: 1rem;
					}
					/* For colorizing XML */
					code.xml {
					padding: 0;
					word-break: break-all;
					}
					.xml-element {
					color: blue;
					padding-left: 2em;
					text-indent: -2em;
					word-wrap: normal;
					overflow-wrap: normal;
					}
					.xml-element-omit {
					padding-left: 2em;
					text-indent: -2em;
					}
					.xml-comment {
					color: #006400;
					padding-left: 10px;
					}
					.xml-element-end {
					padding-left: -2em;
					}
					.xml-element-name {
					color: #000096;
					}
					.xml-attr {
					color: #F5844C;
					}
					.xml-attr-value {
					color: #993300;
					word-wrap: normal;
					overflow-wrap: normal;
					}
					.xml-text {
					color: black;
					}
				</style>
				<script type='text/javascript'>//<![CDATA[

    function filterTableByFailed(pFilter) {
        var $table = $('#summary-table');
        var $data = $table.bootstrapTable('getData');
        if (pFilter) {
            for (i = 0; i < $data.length; i++) {
                if ($data[i][2].indexOf('Fail') >= 0) { 
                    $table.bootstrapTable('showRow', {index: i});
                } else {
                    $table.bootstrapTable('hideRow', {index: i});
                }
            }
        } else {
            for (i = 0; i < $data.length; i++) {
                $table.bootstrapTable('showRow', {index: i});
            }
        }
    }

    $(function() {
      $('#filter-switch').change(function() {
        if ($(this).prop('checked')) {
          filterTableByFailed(true);
        } else {
          filterTableByFailed(false);
        }
      })
    })

    $(document).ready(function() {
      $('#filter-switch').bootstrapToggle('on');
//       filterTableByFailed(true);
    });

					//]]>
				</script>
			</head>
			<body>
				<script>
					var clipboard = new Clipboard('.btn');
				</script>
				<header>
					<div class="container-fluid">

						<div id="report">
							<xsl:call-template name="process-header" />
						</div>

						<div class="card-deck mb-2">
							<!-- <div class="col-xs-12 col-sm-8 col-md-5"> -->
							<div class="card">
								<h4 class="card-header bg-primary text-white">Validation Details</h4>
								<div class="card-block">
									<dl class="row ml-1">
										<dt class="col-sm-3">Target:</dt>
										<xsl:for-each
											select="res:assessment-results/res:subjects/res:subject">
											<dd class="col-sm-9">
												<xsl:value-of select="res:source" />
											</dd>
										</xsl:for-each>
										<dt class="col-sm-3">Start:</dt>
										<dd class="col-sm-9">
											<xsl:value-of
												select="format-dateTime(res:assessment-results/@start,'[MNn] [D1o], [Y0001] [H01]:[m01]:[s01] [z1]')" />
										</dd>
										<dt class="col-sm-3">End:</dt>
										<dd class="col-sm-9">
											<xsl:value-of
												select="format-dateTime(res:assessment-results/@end,'[MNn] [D1o], [Y0001] [H01]:[m01]:[s01] [z1]')" />
										</dd>
										<xsl:call-template
											name="process-validation-details" />
									</dl>
								</div>
							</div>
							<!-- </div> -->
							<!-- <div class="col-xs-12 col-sm-4 col-md-3"> -->
							<div class="card">
								<h4 class="card-header bg-primary text-white">Validation Overview</h4>
								<div class="card-body">
									<canvas id="myChart" />
									<script>

										var ctx = $("#myChart");
										var myChart = new Chart(ctx, {
										type: 'pie',
										data: {
										labels: [
										"Pass"
										<xsl:if
											test="//res:base-requirement/res:status[text() = 'FAIL']">
											, "Error"
										</xsl:if>
										<xsl:if
											test="//res:base-requirement/res:status[text() = 'WARNING']">
											, "Warning"
										</xsl:if>
										<xsl:if
											test="//res:base-requirement/res:status[text() = 'NOT_TESTED'] and not($ignore-not-tested-results)">
											, "Not Tested"
										</xsl:if>
										<xsl:if
											test="//res:base-requirement/res:status[text() = 'INFORMATIONAL']">
											, "Informational"
										</xsl:if>
										<xsl:if
											test="//res:base-requirement/res:status[text() = 'NOT_APPLICABLE']">
											, "Not Applicable"
										</xsl:if>
										],
										datasets: [{
										data: [
										<xsl:value-of
											select="count(//res:base-requirement/res:status[text() = 'PASS'])" />
										,
										<xsl:if
											test="//res:base-requirement/res:status[text() = 'FAIL']">
											<xsl:value-of
												select="count(//res:base-requirement/res:status[text() = 'FAIL'])" />
											,
										</xsl:if>
										<xsl:if
											test="//res:base-requirement/res:status[text() = 'WARNING']">
											<xsl:value-of
												select="count(//res:base-requirement/res:status[text() = 'WARNING'])" />
											,
										</xsl:if>
										<xsl:if
											test="//res:base-requirement/res:status[text() = 'NOT_TESTED'] and not($ignore-not-tested-results)">
											<xsl:value-of
												select="count(//res:base-requirement/res:status[text() = 'NOT_TESTED'])" />
											,
										</xsl:if>
										<xsl:if
											test="//res:base-requirement/res:status[text() = 'INFORMATIONAL']">
											<xsl:value-of
												select="count(//res:base-requirement/res:status[text() = 'INFORMATIONAL'])" />
											,
										</xsl:if>
										<xsl:if
											test="//res:base-requirement/res:status[text() = 'NOT_APPLICABLE']">
											<xsl:value-of
												select="count(//res:base-requirement/res:status[text() = 'NOT_APPLICABLE'])" />
											,
										</xsl:if>
										],
										backgroundColor: [
										"#5cb85c",
										<xsl:if
											test="//res:base-requirement/res:status[text() = 'FAIL']">
											"#d9534f",
										</xsl:if>
										<xsl:if
											test="//res:base-requirement/res:status[text() = 'WARNING']">
											"#f0ad4e",
										</xsl:if>
										<xsl:if
											test="//res:base-requirement/res:status[text() = 'NOT_TESTED'] and not($ignore-not-tested-results)">
											"#777",
										</xsl:if>
										<xsl:if
											test="//res:base-requirement/res:status[text() = 'INFORMATIONAL']">
											"#5bc0de",
										</xsl:if>
										<xsl:if
											test="//res:base-requirement/res:status[text() = 'NOT_APPLICABLE']">
											"#337ab7",
										</xsl:if>
										],
										}]
										},
										options: {
										legend: {
										position: "bottom",
										}
										},
										responsive: true
										});
									</script>
								</div>
							</div>
							<!-- </div> -->
							<!-- <div class="col-xs-12 col-sm-6 col-md-4"> -->
							<div class="card">
								<h4 class="card-header bg-primary text-white">Validation Summary</h4>
								<div class="card-block">
									<dl class="row ml-1">
										<dt class="col-sm-8">Total Requirements:</dt>
										<dd class="col-sm-4">
											<xsl:value-of
												select="count(//res:base-requirement[(res:status/text() != 'NOT_TESTED' or not($ignore-not-tested-results)) and (res:status/text() != 'NOT_IN_SCOPE' or not($ignore-outofscope-results))])" />
										</dd>
										<dt class="col-sm-8">Applicable Requirements:</dt>
										<dd class="col-sm-4">
											<xsl:value-of
												select="count(//res:base-requirement[(res:status/text() != 'NOT_TESTED' or not($ignore-not-tested-results)) and (res:status/text() != 'NOT_IN_SCOPE' or not($ignore-outofscope-results)) and (res:status/text() != 'NOT_APPLICABLE')])" />
										</dd>
										<xsl:if
											test="//res:base-requirement/res:status[text() = 'NOT_TESTED'] and not($ignore-not-tested-results)">
											<dt class="col-sm-8">Requirements Not Tested:</dt>
											<dd class="col-sm-4">
												<xsl:value-of
													select="count(//res:base-requirement/res:status[text() = 'NOT_TESTED'])" />
											</dd>
										</xsl:if>
										<dt class="col-sm-8">Requirements Passed:</dt>
										<dd class="col-sm-4">
											<xsl:value-of
												select="count(//res:base-requirement/res:status[text() = 'PASS' or text() = 'WARNING'])" />
											of
											<xsl:value-of
												select="count(//res:base-requirement/res:status[text() = 'PASS' or text() = 'FAIL' or text() = 'WARNING'])" />
										</dd>
										<dt class="col-sm-8">Requirements Failed:</dt>
										<dd class="col-sm-4">
											<xsl:value-of
												select="count(//res:base-requirement/res:status[text() = 'FAIL'])" />
											of
											<xsl:value-of
												select="count(//res:base-requirement/res:status[text() = 'PASS' or text() = 'FAIL' or text() = 'WARNING'])" />
										</dd>
										<dt class="col-sm-8">Tests Failed:</dt>
										<dd class="col-sm-4">
											<xsl:value-of
												select="count(//res:test/res:status[text() = 'FAIL'])" />
										</dd>
										<dt class="col-sm-8">Overall Result:</dt>
										<dd class="col-sm-4">
											<xsl:choose>
												<xsl:when
													test="count(//res:base-requirement/res:status[text() = 'FAIL']) gt 0">
													<span class="status label label-danger">Fail</span>
												</xsl:when>
												<xsl:otherwise>
													<span class="status label label-success">Pass</span>
												</xsl:otherwise>
											</xsl:choose>
										</dd>
										<xsl:call-template
											name="process-validation-summary" />
									</dl>
								</div>
							</div>
							<!-- </div> -->
						</div>
					</div>
				</header>
				<nav>

				</nav>
				<div class="container-fluid">
					<xsl:apply-templates />
				</div>
				<footer>

				</footer>
			</body>
		</html>
	</xsl:template>

	<!-- ****************************** -->
	<!-- * Ignore these * -->
	<!-- ****************************** -->
	<xsl:template match="res:properties">
		<!-- output nothing -->
	</xsl:template>
	<xsl:template match="res:subjects">
		<!-- output nothing -->
	</xsl:template>

	<!-- ****************************** -->
	<!-- * Summary report table * -->
	<!-- ****************************** -->
	<xsl:template match="res:results">
		<section>
			<!-- <div class="row"> -->

			<div class="col-xs-12 mb-2">
				<div class="card">
					<h1 class="card-header bg-primary text-white">Validation Result Summary</h1>
					<div class="card-block ml-2 mb-2">
						<div class="checkbox m-2">
							<label class="mr-2">Show results:</label>
							<input id="filter-switch" data-toggle="toggle"
								data-on="Failed Only" data-off="All Results" type="checkbox" />
						</div>
						<table id="summary-table" class="table table-sm"
							data-striped="true" data-toggle="table"
							data-custom-search="customSearchMethod">
							<thead>
								<tr>
									<th>Requirement #</th>
									<th>Summary</th>
									<th>Result</th>
								</tr>
								<!-- <tr> <th><input class="filterable" type="text" value=""/></th> 
									<th><input class="filterable" type="text" value=""/></th> <th><input class="filterable" 
									type="text" value=""/></th> </tr> -->
							</thead>
							<tbody>
								<xsl:apply-templates mode="summary" />
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</section>
		<section>
			<div class="card">
				<h1 class="card-header bg-primary text-white">Validation Result Details</h1>
				<div class="card-block p-1">
					<xsl:apply-templates mode="detail" />
				</div>
			</div>
		</section>
	</xsl:template>

	<xsl:template match="res:base-requirement" mode="summary">
		<xsl:if
			test="(res:status/text() != 'NOT_TESTED' or not($ignore-not-tested-results)) and (res:status/text() != 'NOT_IN_SCOPE' or not($ignore-outofscope-results))">
			<xsl:variable name="current-req" select="@id" />
			<xsl:element name="tr">
				<!-- <xsl:if test="res:derived-requirement"> <xsl:attribute name="data-toggle">collapse</xsl:attribute> 
					<xsl:attribute name="data-target">.<xsl:value-of select="@id"/>-collapse</xsl:attribute> 
					</xsl:if> <xsl:attribute name="class">derived-detail-toggle visible</xsl:attribute> -->
				<td>
					<a href="#{$current-req}">
						<xsl:value-of select="@id" />
					</a><!-- <xsl:if test="res:derived-requirement">&#160;<span class="glyphicon 
						glyphicon-chevron-up {@id}-icon-toggle" aria-hidden="true"/><span class="sr-only">Toggle 
						related Derived Requirement:</span></xsl:if> -->
				</td>
				<td>
					<a href="#{$current-req}">
						<xsl:variable name="key"
							select="$requirements/key('requirement-index', $current-req)" />
						<xsl:choose>
							<xsl:when test="not($key/req:summary = '')">
								<xsl:value-of select="$key/req:summary/text()" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$key/req:statement/text()" />
							</xsl:otherwise>
						</xsl:choose>
					</a>
				</td>
				<td class="text-nowrap">
					<xsl:apply-templates select="res:status"
						mode="status-label" />
				</td>
			</xsl:element>
			<!-- <xsl:apply-templates select="res:derived-requirement" mode="#current"/> -->
		</xsl:if>
	</xsl:template>

	<xsl:template match="res:derived-requirement"
		mode="summary">
		<xsl:if
			test="not($ignore-outofscope-results) or res:status/text() != 'NOT_IN_SCOPE'">
			<xsl:variable name="current-req" select="@id" />
			<tr
				class="collapse out {../@id}-collapse active derived-detail-toggle-target">
				<td>
					<xsl:value-of select="@id" />
				</td>
				<td>
					<a href="#{$current-req}">
						<xsl:variable name="key"
							select="$requirements/key('requirement-index', $current-req)" />
						<xsl:choose>
							<xsl:when test="$key/req:summary">
								<xsl:value-of select="$key/req:summary/text()" />
							</xsl:when>
							<xsl:when test="$key/req:statement">
								<xsl:value-of select="$key/req:statement/text()" />
							</xsl:when>
							<xsl:when test="$key/../../req:summary">
								<xsl:value-of
									select="$key/../../req:summary/text()" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of
									select="$key/../../req:statement/text()" />
							</xsl:otherwise>
						</xsl:choose>
					</a>
				</td>
				<td class="text-nowrap text-right">
					<xsl:apply-templates select="res:status"
						mode="status-label" />
				</td>
			</tr>
			<xsl:apply-templates
				select="res:derived-requirement" mode="#current" />
		</xsl:if>
	</xsl:template>

	<!-- use to output the result status by multiple templates -->
	<xsl:template match="res:status" mode="status-label">
		<xsl:variable name="current-req" select="../@id" />
		<xsl:variable name="key"
			select="$requirements/key('requirement-index', $current-req)" />
		<xsl:choose>
			<xsl:when test="text() = 'PASS'">
				<span
					class="border border-success rounded bg-success text-white status p-1">Pass</span>
			</xsl:when>
			<xsl:when test="text() = 'WARNING'">
				<span
					class="border border-warning rounded bg-warning text-white status p-1">Warning</span>
			</xsl:when>
			<xsl:when test="text() = 'FAIL'">
				<span
					class="border border-danger rounded bg-danger text-white status p-1">Fail</span>
			</xsl:when>
			<xsl:when test="text() = 'NOT_IN_SCOPE'">
				<span
					class="border border-muted rounded bg-muted text-black status p-1">Not In Scope</span>
			</xsl:when>
			<xsl:when test="text() = 'NOT_TESTED'">
				<span
					class="border border-warning rounded bg-muted text-black status p-1">Not Tested</span>
			</xsl:when>
			<xsl:when test="text() = 'NOT_APPLICABLE'">
				<span
					class="border border-success rounded bg-muted text-black status p-1">Not Applicable</span>
			</xsl:when>
			<xsl:when test="text() = 'INFORMATIONAL'">
				<span
					class="border border-info rounded bg-info text-white status p-1">Informational</span>
			</xsl:when>
			<xsl:otherwise>
				<span class="status tag tag-default">
					<xsl:value-of select="text()" />
				</span>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ****************************** -->
	<!-- * Requirement details * -->
	<!-- ****************************** -->
	<xsl:template match="res:base-requirement" mode="detail">
		<xsl:if
			test="not($ignore-outofscope-results) or res:status/text() != 'NOT_IN_SCOPE'">
			<xsl:variable name="current-req" select="@id" />
			<xsl:variable name="req-id-key"
				select="$requirements/key('requirement-index', $current-req)" />
			<xsl:variable name="resource-id-key"
				select="$requirements/key('resource-index', $current-req)" />
			<div id="{$current-req}" class="base-req card">
				<div class="card-header bg-secondary text-white">
					<div class="row">
						<div class="col-sm-2 align-middle">
							<h2 class="align-middle mb-0">
								<xsl:value-of select="$current-req" />
							</h2>
						</div>
						<div class="col-sm-8 align-middle">
							<p class="align-middle mb-0">
								<xsl:value-of
									select="$req-id-key/req:summary/text()" />
							</p>
						</div>
						<div class="col-sm-2 align-middle">
							<p class="align-middle text-right mb-0">
								<xsl:apply-templates select="res:status"
									mode="status-label" />
							</p>
						</div>
					</div>
				</div>
				<div class="card-body">
					<div class="row">
						<xsl:element name="div">
							<xsl:choose>
								<!-- Reserve a 4 width column for categorizations -->
								<xsl:when test="$has-requirement-categorizations">
									<xsl:attribute name="class">col-xs-12 col-md-8</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute name="class">col-xs-12</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
							<h3 class="card-title">Requirement</h3>
							<div class="card-block">
								<blockquote class="card-blockquote">
									<p class="req-statement">
										<xsl:choose>
											<xsl:when
												test="$req-id-key/req:reference/@requirement-fragment">
												<a
													href="{$requirements/key('resource-index',$req-id-key/req:reference/@ref)/@href}#{$req-id-key/req:reference/@requirement-fragment}">
													<xsl:value-of
														select="$req-id-key/req:statement/text()" />
												</a>
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of
													select="$req-id-key/req:statement/text()" />
											</xsl:otherwise>
										</xsl:choose>
									</p>
									<footer class="blockquote-footer">
										Section
										<xsl:choose>
											<xsl:when
												test="$req-id-key/req:reference/@section-fragment">
												<a
													href="{$requirements/key('resource-index',$req-id-key/req:reference/@ref)/@href}#{$req-id-key/req:reference/@section-fragment}">
													<xsl:value-of
														select="$req-id-key/req:reference/@section" />
												</a>
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of
													select="$req-id-key/req:reference/@section" />
											</xsl:otherwise>
										</xsl:choose>
										of
										<cite
											title="{$requirements/key('resource-index',$req-id-key/req:reference/@ref)/@name}">
											<xsl:value-of
												select="$requirements/key('resource-index',$req-id-key/req:reference/@ref)/@name" />
										</cite>
									</footer>
								</blockquote>
							</div>
						</xsl:element>
						<xsl:if test="$has-requirement-categorizations">
							<div class="col-xs-12 col-md-4">
								<h3 class="card-title">Requirement Characteristics</h3>
								<div class="card-block">
									<dl class="row">
										<xsl:call-template
											name="process-categorizations" />
									</dl>
								</div>
							</div>
						</xsl:if>
					</div>
					<xsl:if test="res:derived-requirement">
						<h3 class="card-title">Derived Requirement Results</h3>
						<div class="card-block">
							<table class="table table-sm table-hover">
								<thead>
									<tr>
										<th>Derived Requirement #</th>
										<th>Summary</th>
										<th>Result</th>
									</tr>
								</thead>
								<tbody class="">
									<xsl:apply-templates
										select="res:derived-requirement" mode="detail-derived-table" />
								</tbody>
							</table>
						</div>
						<h3 class="card-title">Derived Requirement Details</h3>
						<xsl:apply-templates
							select="res:derived-requirement" mode="#current" />
					</xsl:if>
				</div>
			</div>
		</xsl:if>
	</xsl:template>

	<xsl:template match="res:derived-requirement"
		mode="detail-derived-table">
		<xsl:if
			test="not($ignore-outofscope-results) or res:status/text() != 'NOT_IN_SCOPE'">
			<xsl:variable name="current-req" select="@id" />
			<tr>
				<td>
					<xsl:value-of select="@id" />
				</td>
				<td>
					<a href="#{$current-req}">
						<xsl:variable name="key"
							select="$requirements/key('requirement-index', $current-req)" />
						<xsl:choose>
							<xsl:when test="$key/req:summary">
								<xsl:value-of select="$key/req:summary/text()" />
							</xsl:when>
							<xsl:when test="$key/req:statement">
								<xsl:value-of select="$key/req:statement/text()" />
							</xsl:when>
							<xsl:when test="$key/../../req:summary">
								<xsl:value-of
									select="$key/../../req:summary/text()" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of
									select="$key/../../req:statement/text()" />
							</xsl:otherwise>
						</xsl:choose>
					</a>
				</td>
				<td class="text-nowrap text-right">
					<xsl:apply-templates select="res:status"
						mode="status-label" />
				</td>
			</tr>
		</xsl:if>
	</xsl:template>

	<xsl:template match="res:derived-requirement"
		mode="detail">
		<xsl:if
			test="not($ignore-outofscope-results) or res:status/text() != 'NOT_IN_SCOPE'">
			<xsl:variable name="current-req" select="@id" />
			<xsl:variable name="req-id-key"
				select="$requirements/key('requirement-index', $current-req)" />
			<div id="{$current-req}" class="card mb-1">
				<div class="card-header bg-light text-black">
					<div class="row">
						<div class="col-sm-2 align-middle">
							<h2 class="align-middle mb-0">
								<xsl:value-of select="$current-req" />
							</h2>
						</div>
						<div class="col-sm-8 align-middle">
							<p class="align-middle mb-0">
								<xsl:choose>
									<xsl:when test="$req-id-key/req:statement">
										<xsl:value-of
											select="$req-id-key/req:statement/text()" />
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of
											select="$req-id-key/../../req:summary/text()" />
									</xsl:otherwise>
								</xsl:choose>
							</p>
						</div>
						<div class="col-sm-2 align-middle">
							<p class="align-middle text-right mb-0">
								<xsl:apply-templates select="res:status"
									mode="status-label" />
							</p>
						</div>
					</div>
				</div>
				<div class="card-body">
					<h4 class="card-title">Derived Requirement</h4>
					<div class="card-block">
						<blockquote class="card-blockquote">
							<p class="req-statement">
								<xsl:value-of
									select="$req-id-key/req:statement/text()" />
							</p>
						</blockquote>
					</div>
					<xsl:if test="res:test">
						<!-- <div class="card"> <div class="card-header"> -->
						<h4 class="card-title">Test Details</h4>
						<!-- </div> <div class="table-responsive"> -->
						<div class="card-block">
							<table class="table table-striped">
								<thead>
									<tr>
										<th>#</th>
										<th>Test Result</th>
										<th>Message</th>
										<th>Context (Line/Column)</th>
									</tr>
								</thead>
								<tbody>
									<xsl:apply-templates select="res:test"
										mode="#current" />
								</tbody>
							</table>
						</div>
					</xsl:if>
				</div>
			</div>
		</xsl:if>
	</xsl:template>

	<xsl:template match="res:test" mode="detail">
		<xsl:if
			test="$test-result-limit lt 1 or count(preceding-sibling::res:test) lt $test-result-limit">
			<tr>
				<td>
					<xsl:number />
				</td>
				<td>
					<xsl:apply-templates select="res:status"
						mode="status-label" />
				</td>
				<td>
					<xsl:value-of select="res:message" />
				</td>
				<td>
					<xsl:value-of select="res:location/@line" />
					:
					<xsl:value-of select="res:location/@column" />
					&#160;
					<button class="btn btn-sm" type="button"
						data-clipboard-text="{res:location/@xpath}">Copy XPath</button>
				</td>
			</tr>
			<xsl:if
				test="$generate-xml-output and res:location/@xpath and document(res:location/@href)">
				<tr>
					<td colspan="4">
						<xsl:apply-templates
							select="document(res:location/@href)" mode="xml-to-html">
							<xsl:with-param name="xpath"
								select="res:location/@xpath" />
						</xsl:apply-templates>
					</td>
				</tr>
			</xsl:if>
		</xsl:if>
		<xsl:if
			test="$test-result-limit gt 0 and count(preceding-sibling::res:test) eq $test-result-limit">
			<tr>
				<td colspan="4">
					Omitting
					<xsl:value-of
						select="count(following-sibling::res:test)+1" />
					additional results.
				</td>
			</tr>
		</xsl:if>
	</xsl:template>

	<xsl:template match="res:status" mode="test-status-tag">
		<xsl:variable name="current-req" select="../../@id" />
		<xsl:variable name="key"
			select="$requirements/key('requirement-index', $current-req)" />
		<xsl:choose>
			<xsl:when test="text() = 'PASS'">
				<span class="status tag tag-success">Pass</span>
			</xsl:when>
			<xsl:when test="text() = 'WARNING'">
				<span class="status tag label-warning">Warning</span>
			</xsl:when>
			<xsl:when test="text() = 'FAIL'">
				<span class="status label label-danger">Fail</span>
			</xsl:when>
			<xsl:when test="text() = 'NOT_TESTED'">
				<span class="status label label-default">Not Tested</span>
			</xsl:when>
			<xsl:when test="text() = 'NOT_APPLICABLE'">
				<span class="status label label-default label-not-applicable">Not Applicable</span>
			</xsl:when>
			<xsl:when test="text() = 'INFORMATIONAL'">
				<span class="status label label-info">Informational</span>
			</xsl:when>
			<xsl:otherwise>
				<span class="status label label-default">
					<xsl:value-of select="text()" />
				</span>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ****************************** -->
	<!-- * Output Tested XML as HTML * -->
	<!-- ****************************** -->
	<xsl:template match="/" mode="xml-to-html">
		<xsl:param name="xpath" />
		<code class="xml">
			<xsl:apply-templates
				select="decima:evaluate(.,$xpath)" mode="xml-to-html-target" />
		</code>
	</xsl:template>

	<xsl:template match="*" mode="xml-to-html-target">
		<xsl:if test="parent::*">
			<xsl:apply-templates select="parent::*"
				mode="xml-to-html-parent-start" />
			<xsl:if test="position() > 0">
				<div class="xml-element-omit">...</div>
			</xsl:if>
		</xsl:if>


		<xsl:apply-templates select="."
			mode="xml-to-html-target-output">
			<xsl:with-param name="depth"
				select="$xml-output-depth" />
			<xsl:with-param name="child-limit" select="-1" />
		</xsl:apply-templates>

		<xsl:if test="following-sibling::*">
			<div class="xml-element-omit">...</div>
		</xsl:if>

		<xsl:if test="parent::*">
			<xsl:apply-templates select="parent::*"
				mode="xml-to-html-parent-end" />
		</xsl:if>
	</xsl:template>

	<xsl:template match="*" mode="xml-to-html-parent-start">
		<xsl:if test="parent::*">
			<xsl:apply-templates select="parent::*"
				mode="xml-to-html-parent-start" />
			<xsl:if test="position() > 0">
				<div class="xml-element-omit">...</div>
			</xsl:if>
		</xsl:if>
		<xsl:call-template
			name="xml-to-html-output-element-start" />
	</xsl:template>

	<xsl:template match="*" mode="xml-to-html-parent-end">
		<xsl:call-template
			name="xml-to-html-output-element-end" />
		<xsl:if test="parent::*">
			<xsl:apply-templates select="parent::*"
				mode="xml-to-html-parent-end" />
		</xsl:if>
	</xsl:template>

	<xsl:template match="*" mode="xml-to-html-target-output">
		<xsl:param name="depth" />
		<xsl:param name="child-limit" />

		<xsl:choose>
			<xsl:when
				test="$child-limit = -1 or count(preceding-sibling::*) lt $child-limit">
				<xsl:call-template
					name="xml-to-html-output-element-start" />

				<xsl:if test="child::*">
					<xsl:choose>
						<xsl:when test="$depth > 0">
							<xsl:apply-templates select="child::*"
								mode="#current">
								<xsl:with-param name="depth" select="$depth - 1" />
								<xsl:with-param name="child-limit"
									select="$xml-output-child-limit" />
							</xsl:apply-templates>
						</xsl:when>
						<xsl:otherwise>
							<div class="xml-element-omit">...</div>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>

				<xsl:call-template
					name="xml-to-html-output-element-end" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="count(preceding-sibling::*) eq $child-limit">
					<div class="xml-element-omit">... [other children omitted for brevity]</div>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="xml-to-html-output-element-start">
		<xsl:text disable-output-escaping="yes">&lt;div class="xml-element"&gt;</xsl:text>
		&lt;
		<span class="xml-element-name">
			<xsl:value-of select="name()" />
		</span>
		<xsl:for-each select="@*">
			<xsl:call-template
				name="xml-to-html-output-attribute" />
		</xsl:for-each>
		<xsl:choose>
			<xsl:when test="*|text()|comment()">
				&gt;
			</xsl:when>
			<xsl:otherwise>/&gt;
				<xsl:text disable-output-escaping="yes">&lt;/div&gt;</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="xml-to-html-output-element-end">
		<xsl:if test="*|text()|comment()">
			<div class="xml-element-end">
				&lt;/
				<span class="xml-element-name">
					<xsl:value-of select="name()" />
				</span>
				&gt;
			</div>
			<xsl:text disable-output-escaping="yes">&lt;/div&gt;</xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template name="xml-to-html-output-attribute">
		<span class="xml-attr">
			&#160;
			<xsl:value-of select="name()" />
			<xsl:if test=".">
				=
				<span class="xml-attr-value">
					"
					<xsl:value-of select="." />
					"
				</span>
			</xsl:if>
		</span>
	</xsl:template>
</xsl:stylesheet>