/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NASA BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.decima.xml.document;

import gov.nist.decima.core.document.Document;
import gov.nist.decima.core.document.DocumentException;
import gov.nist.decima.xml.document.context.XMLContextResolver;

import org.jdom2.Element;
import org.jdom2.output.Format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.xpath.XPathFactoryConfigurationException;

/**
 * Represents an instance of XML that can be reused for various forms of XML processing.
 * Implementations of this class can choose to locally cache the XML and apply any pre-processing
 * steps required to prepare the XML for use.
 */
public interface XMLDocument extends Document, XMLContextResolver {

  /**
   * Retrieves the document as an XML transformation source. This instance of the document is
   * expected to be in a ready-to-be-processed state. Modifications to the returned instance should
   * not affect the actual template. It may be necessary to make a defensive copy to ensure this.
   * 
   * @return an input source based on a copy of the template
   */
  Source getSource();

  /**
   * Returns an XPath 2.0 evaluator.
   * 
   * @return the evaluator
   * @throws XPathFactoryConfigurationException
   *           if an error occurred while instantiating a new XPathFactory instance
   */
  XPathEvaluator newXPathEvaluator() throws XPathFactoryConfigurationException;

  XMLDocumentFragment newXMLDocumentFragment(String xpath) throws DocumentException;

  /**
   * Creates an {@link XMLDocumentFragment} based on a sub-tree of the current document instance.
   * 
   * @param element
   *          the element in the current document instance to use as the base of the sub-tree
   * @return a new {@link XMLDocumentFragment} for the sub-tree
   * @throws DocumentException
   *           if an error occurred while parsing the document
   */
  XMLDocumentFragment newXMLDocumentFragment(Element element) throws DocumentException;

  /**
   * Provides access to a (defensive) copy of the underlying JDOM document.
   * 
   * @return a copy of the underlying JDOM document
   */
  org.jdom2.Document getJDOMDocument();

  /**
   * Writes a copy of the document to the provided file.
   * 
   * @param outputFile
   *          the file to write to
   * @throws FileNotFoundException
   *           if the path to the file does not exist
   * @throws IOException
   *           if an error occurs while writing to the file
   */
  void copyTo(File outputFile) throws FileNotFoundException, IOException;

  /**
   * Retrieves the document as a string.
   * 
   * @param format
   *          the format to use during the conversion
   * @return a string representation of the document
   * @throws IOException
   *           if an error occurs while outputting the document as a dtring
   */
  String asString(Format format) throws IOException;

}
