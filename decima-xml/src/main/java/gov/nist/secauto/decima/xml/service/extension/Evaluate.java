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

package gov.nist.secauto.decima.xml.service.extension;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

public class Evaluate
    extends ExtensionFunctionDefinition {
  private static final StructuredQName FUNCTION_QNAME
      = new StructuredQName("", "http://decima.nist.gov/xsl/extensions", "evaluate");

  private static final SequenceType[] FUNCTION_ARGUMENTS
      = new SequenceType[] { SequenceType.NODE_SEQUENCE, SequenceType.STRING_SEQUENCE };

  @Override
  public StructuredQName getFunctionQName() {
    return FUNCTION_QNAME;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return FUNCTION_ARGUMENTS;
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.NODE_SEQUENCE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new FunctionCall();
  }

  private static class FunctionCall
      extends ExtensionFunctionCall {
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
      Sequence left = arguments[0];
      String xpath = arguments[1].head().getStringValue();

      Processor processor = (Processor) context.getConfiguration().getProcessor();
      XPathCompiler compiler = processor.newXPathCompiler();
      try {
        // XdmItem contextItem = (XdmItem) XdmValue.wrap(context.getContextItem());
        XdmItem contextItem = (XdmItem) XdmValue.wrap(left.head());
        XdmValue result = compiler.evaluate(xpath, contextItem);
        return result.getUnderlyingValue();
      } catch (SaxonApiException e) {
        throw new XPathException(e);
      }
    }
  }
}
