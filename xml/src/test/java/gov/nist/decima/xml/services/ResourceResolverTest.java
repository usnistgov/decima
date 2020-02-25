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

package gov.nist.decima.xml.services;

import gov.nist.decima.core.classpath.ClasspathHandler;
import gov.nist.decima.xml.service.ResourceResolverExtensionService;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ResourceResolverTest {
  private static final ResourceResolverExtensionService service = ResourceResolverExtensionService.getInstance();

  private static final Map<String, String> testSystemIdMap = new HashMap<>();
  static {
    testSystemIdMap.put("http://www.w3.org/2001/xml.xsd", "classpath:schema/xml/xml.xsd");
    testSystemIdMap.put("http://www.w3.org/2001/XMLSchema.dtd", "classpath:schema/xml/XMLSchema.dtd");
    testSystemIdMap.put("http://www.w3.org/TR/xmldsig-core/xmldsig-core-schema.xsd", "classpath:schema/");
    testSystemIdMap.put("http://www.w3.org/TR/xmldsig-core/xmldsig-core-schema.xsd",
        "classpath:schema/xmldsig/xmldsig-core-schema.xsd");
    testSystemIdMap.put("https://www.w3.org/TR/xmldsig-core/xmldsig-core-schema.xsd",
        "classpath:schema/xmldsig/xmldsig-core-schema.xsd");
    testSystemIdMap.put("http://csrc.nist.gov/schema/decima/requirements/decima-requirements-0.1.xsd",
        "classpath:schema/decima/decima-requirements.xsd");
    testSystemIdMap.put("http://csrc.nist.gov/schema/decima/results/decima-results-0.1.xsd",
        "classpath:schema/decima/decima-results.xsd");
  }

  @BeforeClass
  public static void initialize() {
    ClasspathHandler.initialize();
  }

  @Test
  public void TestSystemIds() throws SAXException, IOException {
    EntityResolver2 resolver = service.getEntityResolver();
    for (Map.Entry<String, String> entry : testSystemIdMap.entrySet()) {
      InputSource source = resolver.resolveEntity(null, entry.getKey());
      Assert.assertNotNull(source);
      Assert.assertEquals(entry.getValue(), source.getSystemId());
    }
  }
}
