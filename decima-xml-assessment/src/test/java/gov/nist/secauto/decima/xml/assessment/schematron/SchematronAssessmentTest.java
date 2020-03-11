/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.decima.xml.assessment.schematron;

import gov.nist.secauto.decima.xml.assessment.schematron.SchematronAssessment;
import gov.nist.secauto.decima.xml.assessment.schematron.SchematronHandler;
import gov.nist.secauto.decima.xml.schematron.Schematron;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SchematronAssessmentTest {
  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery();

  @Test
  public void testSchematronAssessment() {
    Schematron schematron = context.mock(Schematron.class);
    SchematronHandler schematronHandler = context.mock(SchematronHandler.class);

    context.checking(new Expectations() {
      {
        allowing(schematron).getPath();
        will(returnValue("/here/schematron.xsl"));
      }
    });

    // with null phase
    SchematronAssessment assessment = new SchematronAssessment(schematron, null, schematronHandler);
    Assert.assertSame(schematron, assessment.getSchematron());
    Assert.assertSame(schematronHandler, assessment.getSchematronHandler());
    Assert.assertNull(assessment.getPhase());

    // with actual phase
    assessment = new SchematronAssessment(schematron, "phase", schematronHandler);
    Assert.assertSame(schematron, assessment.getSchematron());
    Assert.assertSame(schematronHandler, assessment.getSchematronHandler());
    Assert.assertEquals("phase", assessment.getPhase());
  }

  @Test
  public void testParameters() {
    Schematron schematron = context.mock(Schematron.class);
    SchematronHandler schematronHandler = context.mock(SchematronHandler.class);

    context.checking(new Expectations() {
      {
        allowing(schematron).getPath();
        will(returnValue("/here/schematron.xsl"));
      }
    });

    SchematronAssessment assessment = new SchematronAssessment(schematron, null, schematronHandler);
    assessment.addParameter("name", "value");

    Map<String, String> params = assessment.getParameters();
    Assert.assertNotNull(params);
    Assert.assertEquals(1, params.size());
    Assert.assertEquals("value", params.get("name"));

    // Map interface
    assessment.addParameters(Collections.singletonMap("name2", "value2"));

    params = assessment.getParameters();
    Assert.assertNotNull(params);
    Assert.assertEquals(2, params.size());
    Assert.assertEquals("value", params.get("name"));
    Assert.assertEquals("value2", params.get("name2"));

    // hash map
    Map<String, String> newMap = new HashMap<>();
    newMap.put("name3", "value3");
    newMap.put("name4", "value4");
    assessment.addParameters(newMap);

    params = assessment.getParameters();
    Assert.assertNotNull(params);
    Assert.assertEquals(4, params.size());
    Assert.assertEquals("value", params.get("name"));
    Assert.assertEquals("value2", params.get("name2"));
    Assert.assertEquals("value3", params.get("name3"));
    Assert.assertEquals("value4", params.get("name4"));
  }

  @Test
  public void testGetAssessmentType() {
    Schematron schematron = context.mock(Schematron.class);
    SchematronHandler schematronHandler = context.mock(SchematronHandler.class);

    context.checking(new Expectations() {
      {
        allowing(schematron).getPath();
        will(returnValue("/here/schematron.xsl"));
      }
    });

    SchematronAssessment assessment = new SchematronAssessment(schematron, null, schematronHandler);
    Assert.assertEquals("Schematron", assessment.getAssessmentType());
  }

}
