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
package gov.nist.decima.xml.document;

import gov.nist.decima.core.document.DocumentException;
import gov.nist.decima.core.document.handling.CachingStrategy;
import gov.nist.decima.core.document.handling.DocumentPostProcessor;
import gov.nist.decima.core.document.handling.ResourceResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.SAXEngine;
import org.jdom2.located.LocatedJDOMFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;

public class DefaultXMLDocumentFactory implements XMLDocumentFactory {
  private static final Logger log = LogManager.getLogger(DefaultXMLDocumentFactory.class);

  private final CachingStrategy<MutableXMLDocument> cachingStrategy;
  private final SAXEngine saxEngine;
  private final LinkedList<DocumentPostProcessor<MutableXMLDocument>> postProcessors = new LinkedList<>();

  public DefaultXMLDocumentFactory() {
    this(new NoCachingStrategy());
    // this(new MappedCachingStrategy());
  }

  /**
   * Constructs the XML document factory with the provided caching strategy.
   * 
   * @param cachingStrategy
   *          the strategy to use
   */
  public DefaultXMLDocumentFactory(CachingStrategy<MutableXMLDocument> cachingStrategy) {
    this.cachingStrategy = cachingStrategy;
    try {
      this.saxEngine = initSAXEngine();
    } catch (JDOMException e) {
      throw new RuntimeException(e.getLocalizedMessage(), e);
    }
  }

  protected SAXEngine initSAXEngine() throws JDOMException {
    SAXBuilder builder = new SAXBuilder();
    builder.setJDOMFactory(new LocatedJDOMFactory());
    return builder.buildEngine();
  }

  public CachingStrategy<MutableXMLDocument> getCachingStrategy() {
    return cachingStrategy;
  }

  public SAXEngine getSAXEngine() {
    return saxEngine;
  }

  public void registerPostProcessor(DocumentPostProcessor<MutableXMLDocument> processor) {
    postProcessors.add(processor);
  }

  public LinkedList<DocumentPostProcessor<MutableXMLDocument>> getPostProcessors() {
    return postProcessors;
  }

  protected JDOMDocument newJDOMDocument(Document document, URL originalLocation) throws DocumentException {
    return new JDOMDocument(document, originalLocation);
  }

  protected JDOMDocument newJDOMDocument(File file, URL originalLocation)
      throws DocumentException, FileNotFoundException {
    return new JDOMDocument(file, originalLocation);
  }

  protected MutableXMLDocument postProcessXMLDocument(MutableXMLDocument subject,
      ResourceResolver<MutableXMLDocument> resolver) throws DocumentException {
    for (DocumentPostProcessor<MutableXMLDocument> processor : getPostProcessors()) {
      if (processor.handles(subject)) {
        subject = processor.process(subject, resolver);
      }

    }
    return subject;
  }

  private static String getSystemId(URL location) {
    return location.toString();
  }

  private static String getSystemId(File location) {
    return location.toURI().toString();
  }

  private MutableXMLDocument loadURLInternal(URL location, Deque<String> visitedUrls) throws DocumentException {
    log.debug("Loading: " + location.toString());
    MutableXMLDocument retval = getCachingStrategy().retrieve(getSystemId(location));
    if (retval == null) {

      String systemId = getSystemId(location);
      Document document;
      try (InputStream is = location.openStream()) {
        SAXEngine saxEngine = getSAXEngine();
        document = saxEngine.build(is, systemId);
      } catch (IOException | JDOMException e) {
        throw new DocumentException(e.getLocalizedMessage(), e);
      }

      MutableXMLDocument xmlDocument = newJDOMDocument(document, location);
      retval = postProcessXMLDocument(xmlDocument, new CyclePreventingDocumentResolver());

      getCachingStrategy().store(retval);
    }
    return retval;
  }

  private MutableXMLDocument loadFileInternal(File file, URL originalLocation)
      throws FileNotFoundException, DocumentException {
    if (originalLocation == null) {
      try {
        originalLocation = file.toURI().toURL();
      } catch (MalformedURLException e) {
        throw new DocumentException(e.getMessage(), e);
      }
    }

    MutableXMLDocument xmlDocument = newJDOMDocument(file, originalLocation);
    return postProcessXMLDocument(xmlDocument, new CyclePreventingDocumentResolver());
  }

  @Override
  public XMLDocument load(URL location) throws DocumentException {
    Objects.requireNonNull(location, "The location argument must be non-null");

    return loadURLInternal(location, new LinkedList<>());
  }

  @Override
  public XMLDocument load(URL location, File destinationFile) throws DocumentException {
    Objects.requireNonNull(location, "The location argument must be non-null");
    Objects.requireNonNull(destinationFile, "The destinationFile argument must be non-null");

    MutableXMLDocument retval = getCachingStrategy().retrieve(getSystemId(location));
    if (retval == null) {
      // Make a local copy
      try (InputStream is = location.openStream()) {
        Files.copy(is, destinationFile.toPath());
      } catch (IOException e) {
        throw new DocumentException(e.getLocalizedMessage(), e);
      }

      // Handle as a local file
      try {
        retval = loadFileInternal(destinationFile, location);
      } catch (FileNotFoundException e) {
        throw new DocumentException(e.getLocalizedMessage(), e);
      }

      getCachingStrategy().store(retval);
    }
    return retval;
  }

  @Override
  public XMLDocument load(File location) throws DocumentException, FileNotFoundException {
    Objects.requireNonNull(location, "The location argument must be non-null");

    MutableXMLDocument retval = getCachingStrategy().retrieve(getSystemId(location));
    if (retval == null) {
      // Handle as a local file
      retval = loadFileInternal(location, null);

      getCachingStrategy().store(retval);
    }
    return retval;
  }

  private static class NoCachingStrategy implements CachingStrategy<MutableXMLDocument> {

    @Override
    public MutableXMLDocument retrieve(String systemId) {
      return null;
    }

    @Override
    public void store(MutableXMLDocument retval) {
    }
  }

  public class CyclePreventingDocumentResolver implements ResourceResolver<MutableXMLDocument> {
    private final Deque<String> visitedUrls;

    public CyclePreventingDocumentResolver() {
      this(new LinkedList<>());
    }

    public CyclePreventingDocumentResolver(Deque<String> visitedUrls) {
      this.visitedUrls = visitedUrls;
    }

    @Override
    public MutableXMLDocument resolve(URL url) throws DocumentException {
      String uri = url.toString();
      if (visitedUrls.contains(uri)) {
        throw new DocumentException("Loading url '" + url
            + "' would cause a document loop. The previous url loaded was: " + visitedUrls.peek());
      }
      visitedUrls.push(uri);
      MutableXMLDocument retval = loadURLInternal(url, visitedUrls);
      if (!uri.equals(visitedUrls.pop())) {
        throw new DocumentException("Tracking URLs for cycles has become inconsistant.");
      }

      return retval;
    }
  }

  @Override
  public MutableXMLDocument resolve(URL url) throws DocumentException {
    return loadURLInternal(url, new LinkedList<>());
  }
}
