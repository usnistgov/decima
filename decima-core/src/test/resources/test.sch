<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron"
    xmlns:swid="http://standards.iso.org/iso/19770/-2/2015/schema.xsd"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://standards.iso.org/iso/19770/-2/2015/schema.xsd swid-schema-2015-06-08.xsd"
    queryBinding="xslt">
    <title>ISO/IEC 19770-2 SWID Tag Checker based on NISTIR 8060</title>
    <ns uri="http://standards.iso.org/iso/19770/-2/2015/schema.xsd" prefix="swid"/>

    <pattern id="test">
        <rule id="TEST-1" context="swid:Entity[contains(@role,'tagCreator')]">
            <report id="INFO-1-1" role="info" test="$authoritative">The tag is authoritative.</report>
            <report id="INFO-1-2" role="info" test="not($authoritative)">The tag is non-authoritative.</report>
    	    <assert id="GEN-2-1" test="count(@*[(name() = 'patch' or name() = 'corpus' or name() = 'supplemental') and . = 'true']) &lt;= 1"><value-of select="@patch"/>|<value-of select="@corpus"/>|<value-of select="@supplemental"/></assert>
        </rule>
    </pattern>
</schema>