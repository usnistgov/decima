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

package gov.nist.decima.xml.templating.document.post.template;

import gov.nist.decima.core.classpath.ClasspathHandler;

import org.hamcrest.collection.IsIn;
import org.jdom2.JDOMException;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

public class TemplateProcessorBuilderTest {
  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery();

  @BeforeClass
  public static void initialize() {
    ClasspathHandler.initialize();
  }

  @Test
  public void test() throws MalformedURLException, JDOMException, IOException {
    Action testAction = context.mock(Action.class);

    TemplateProcessorBuilder builder = new TemplateProcessorBuilder();
    URL url = new URL("classpath:template.xml");
    builder.setTemplateURL(url);

    // check empty actions
    Assert.assertTrue(builder.getActions().isEmpty());
    TemplateProcessor template = builder.build();
    Assert.assertTrue(template.getActions().isEmpty());

    // Check added actions
    builder.addAction(testAction);
    builder.addActions(Collections.emptyList());

    Assert.assertEquals(url, builder.getTemplateURL());
    Assert.assertEquals(1, builder.getActions().size());
    Assert.assertThat(testAction, IsIn.isIn(builder.getActions()));

    template = builder.build();
    Assert.assertEquals(url, template.getBaseTemplateURL());
    Assert.assertEquals(1, template.getActions().size());
    Assert.assertThat(testAction, IsIn.isIn(template.getActions()));
  }

}
