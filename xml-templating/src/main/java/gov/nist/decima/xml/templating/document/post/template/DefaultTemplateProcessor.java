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
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.decima.core.document.post.template;

import gov.nist.decima.core.document.DocumentException;
import gov.nist.decima.core.document.handling.ResourceResolver;
import gov.nist.decima.xml.document.JDOMDocument;
import gov.nist.decima.xml.document.MutableXMLDocument;
import gov.nist.decima.xml.document.XMLDocument;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.net.URL;
import java.util.List;

public class DefaultTemplateProcessor implements TemplateProcessor {
  private static final Logger log = LogManager.getLogger(DefaultTemplateProcessor.class);
  private static final XMLOutputter DEFAULT_XML_OUTPUTTER = new XMLOutputter();

  // TODO: Use URI?
  private final URL contextSystemId;
  private final URL baseTemplateURL;
  private final List<Action> actions;

  /**
   * Construct a new XML template processor.
   * 
   * @param contextSystemId
   *          the system id for the document containing this template
   * @param baseTemplateURL
   *          the base template referenced within the current template to use as a starting point
   *          for tansformation
   * @param actions
   *          the actions to perform on the base template
   */
  public DefaultTemplateProcessor(URL contextSystemId, URL baseTemplateURL, List<Action> actions) {
    this.contextSystemId = contextSystemId;
    this.baseTemplateURL = baseTemplateURL;
    this.actions = actions;
  }

  @Override
  public URL getContextSystemId() {
    return contextSystemId;
  }

  @Override
  public URL getBaseTemplateURL() {
    return baseTemplateURL;
  }

  @Override
  public List<Action> getActions() {
    return actions;
  }

  protected XMLOutputter getXMLOutputter() {
    return DEFAULT_XML_OUTPUTTER;
  }

  @Override
  public MutableXMLDocument generate(ResourceResolver<MutableXMLDocument> templateResolver) throws DocumentException {
    if (templateResolver == null) {
      throw new DocumentException(new NullPointerException("a resolver was not provided"));
    }
    XMLDocument template = templateResolver.resolve(getBaseTemplateURL());
    Document doc = template.getJDOMDocument().clone();

    if (log.isDebugEnabled()) {
      log.debug("Processing template '{}' using '{}' as the base.", getContextSystemId(), getBaseTemplateURL());
    }
    for (Action action : getActions()) {
      try {
        if (log.isDebugEnabled()) {
          log.debug("Executing action: {}", action.getClass().getName());
        }
        action.execute(doc);
      } catch (ActionException e) {
        // log.error("An error occured while executing an action", e);
        throw new DocumentException("Unable to process template", e);
      }
      if (log.isTraceEnabled()) {
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        log.trace("Resulting XML: {}", out.outputString(doc));
      }
    }
    doc.setBaseURI(getContextSystemId().toString());
    return new JDOMDocument(doc, getContextSystemId());
    // File file;
    // try {
    // file = getOutputFile();
    // } catch (IOException e) {
    // throw new XMLDocumentException(e);
    // }
    //
    // // Ensure that directories for the file exist
    // File dir = file.getParentFile();
    // if (!dir.exists()) {
    // if (!dir.mkdirs()) {
    // throw new XMLDocumentException("Unable to create the parent directory for the template:
    // "+dir.getPath());
    // }
    // }
    //
    // OutputStream os;
    // try {
    // os = new BufferedOutputStream(new FileOutputStream(file));
    // } catch (FileNotFoundException e) {
    // // this should not happen
    // throw new XMLDocumentException(e);
    // }
    // try {
    // getXMLOutputter().output(doc, os);
    // } catch (IOException e) {
    // String msg = "Unable to write template to file: "+file.getPath();
    //// log.error(msg);
    // throw new XMLDocumentException(msg, e);
    // } finally {
    // try {
    // os.close();
    // } catch (IOException e) {
    // }
    // }
    // try {
    // // Bug
    // return new FileTemplate(file, getBaseURI());
    // } catch (MalformedURLException e) {
    // String msg = "Unable to comstruct the template object";
    //// log.error(msg);
    // throw new XMLDocumentException(msg, e);
    // }
  }

}
