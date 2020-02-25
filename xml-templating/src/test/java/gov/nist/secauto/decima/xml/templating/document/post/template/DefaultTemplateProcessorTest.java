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

package gov.nist.secauto.decima.xml.templating.document.post.template;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import gov.nist.secauto.decima.core.document.DocumentException;
import gov.nist.secauto.decima.core.document.handling.ResourceResolver;
import gov.nist.secauto.decima.xml.document.MutableXMLDocument;
import gov.nist.secauto.decima.xml.templating.document.post.template.Action;
import gov.nist.secauto.decima.xml.templating.document.post.template.ActionException;
import gov.nist.secauto.decima.xml.templating.document.post.template.DefaultTemplateProcessor;

import org.hamcrest.collection.IsIn;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class DefaultTemplateProcessorTest {
  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  private class TestExpectations extends Expectations {
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public TestExpectations(MutableXMLDocument template, URL templateURL, URL transformURL, Document document,
        ResourceResolver<MutableXMLDocument> resolver, Action action) throws DocumentException, ActionException {
      Sequence buildTemplate = context.sequence("build-template");

      oneOf(resolver).resolve(templateURL);
      will(returnValue(template));
      inSequence(buildTemplate);
      oneOf(template).getJDOMDocument();
      will(returnValue(document));
      inSequence(buildTemplate);
      oneOf(document).clone();
      will(returnValue(document));
      inSequence(buildTemplate);
      oneOf(action).execute(document);
      inSequence(buildTemplate);
      ignoring(document);
      inSequence(buildTemplate);
    }
  }

  @Test
  public void test() throws ActionException, DocumentException, JDOMException, IOException {
    Document document = context.mock(Document.class);
    Action action = context.mock(Action.class);
    @SuppressWarnings("unchecked")
    ResourceResolver<MutableXMLDocument> resolver
        = (ResourceResolver<MutableXMLDocument>) context.mock(ResourceResolver.class);
    MutableXMLDocument template = context.mock(MutableXMLDocument.class);
    URL transformURL = new URL("http://test.org/base");
    URL templateURL = new URL("http://test.org/test");

    context.checking(new TestExpectations(template, templateURL, transformURL, document, resolver, action));

    DefaultTemplateProcessor processor
        = new DefaultTemplateProcessor(transformURL, templateURL, Collections.singletonList(action));
    Assert.assertEquals(templateURL, processor.getBaseTemplateURL());
    List<Action> actions = processor.getActions();
    Assert.assertEquals(1, actions.size());
    Assert.assertThat(action, IsIn.isIn(actions));
    Assert.assertNotNull(processor.generate(resolver));
  }

}
