////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2015 Saxonica Limited.
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.option.jdom2;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.parser.Location;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.value.Whitespace;

import org.jdom2.Attribute;
import org.jdom2.AttributeType;
import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Parent;
import org.jdom2.ProcessingInstruction;
import org.jdom2.Text;

import java.util.Stack;

/**
 * JDOMWriter is a Receiver that constructs a JDOM2 document from the stream of events
 */

public class JDOM2Writer extends net.sf.saxon.event.Builder {

  private Document document;
  private Stack<Parent> ancestors = new Stack<Parent>();
  private boolean implicitDocumentNode = false;
  private FastStringBuffer textBuffer = new FastStringBuffer(FastStringBuffer.C256);

  /**
   * Create a JDOM2Writer using the default node factory
   *
   * @param pipe
   *          information about the Saxon pipeline
   */

  public JDOM2Writer(PipelineConfiguration pipe) {
    super(pipe);
  }

  /**
   * Notify an unparsed entity URI.
   *
   * @param name
   *          The name of the unparsed entity
   * @param systemID
   *          The system identifier of the unparsed entity
   * @param publicID
   *          The public identifier of the unparsed entity
   */

  public void setUnparsedEntity(String name, String systemID, String publicID)
      throws XPathException {
    // no-op
  }

  /**
   * Start of the document.
   */

  public void open() {
  }

  /**
   * End of the document.
   */

  public void close() {
  }

  /**
   * Start of a document node.
   */

  public void startDocument(int properties) throws XPathException {
    document = new Document();
    document.setBaseURI(systemId);
    ancestors.push(document);
    textBuffer.setLength(0);
  }

  /**
   * Notify the end of a document node
   */

  public void endDocument() throws XPathException {
    ancestors.pop();
  }

  /**
   * Start of an element.
   */

  public void startElement(NodeName nameCode, SchemaType typeCode, Location location,
      int properties) throws XPathException {
    flush();
    String local = nameCode.getLocalPart();
    String uri = nameCode.getURI();
    String prefix = nameCode.getPrefix();
    Element element;
    if (ancestors.isEmpty()) {
      startDocument(0);
      implicitDocumentNode = true;
    }
    element = new Element(local, prefix, uri);
    if (ancestors.size() == 1) {
      document.setRootElement(element);
    } else {
      ancestors.peek().addContent(element);
    }
    ancestors.push(element);
  }

  public void namespace(NamespaceBinding namespaceBinding, int properties) throws XPathException {
    String prefix = namespaceBinding.getPrefix();
    String uri = namespaceBinding.getURI();
    if (uri.isEmpty() && prefix.length() != 0) {
      // ignore XML 1.1 namespace undeclarations because JDOM can't handle them
      return;
    }
    Namespace ns
        = prefix.isEmpty() ? Namespace.getNamespace(uri) : Namespace.getNamespace(prefix, uri);
    ((Element) ancestors.peek()).addNamespaceDeclaration(ns);
  }

  public void attribute(NodeName nameCode, SimpleType typeCode, CharSequence value,
      Location locationId, int properties) throws XPathException {
    String local = nameCode.getLocalPart();
    String uri = nameCode.getURI();
    String prefix = nameCode.getPrefix();
    Namespace ns
        = prefix.isEmpty() ? Namespace.getNamespace(uri) : Namespace.getNamespace(prefix, uri);
    Attribute att = new Attribute(local, value.toString(), ns);
    if (uri.equals(NamespaceConstant.XML) && local.equals("id")) {
      value = Whitespace.trim(value);
      att.setAttributeType(AttributeType.ID);
    }
    ((Element) ancestors.peek()).getAttributes().add(att);
  }

  public void startContent() throws XPathException {
    flush();
  }

  /**
   * End of an element.
   */

  public void endElement() throws XPathException {
    flush();
    ancestors.pop();
    Object parent = ancestors.peek();
    if (parent == document && implicitDocumentNode) {
      endDocument();
    }
  }

  /**
   * Character data.
   */

  public void characters(CharSequence chars, Location locationId, int properties)
      throws XPathException {
    textBuffer.append(chars);
  }

  private void flush() {
    if (textBuffer.length() != 0) {
      Text text = new Text(textBuffer.toString());
      ancestors.peek().addContent(text);
      textBuffer.setLength(0);
    }
  }

  /**
   * Handle a processing instruction.
   */

  public void processingInstruction(String target, CharSequence data, Location locationId,
      int properties) throws XPathException {
    flush();
    ProcessingInstruction pi = new ProcessingInstruction(target, data.toString());
    ancestors.peek().addContent(pi);
  }

  /**
   * Handle a comment.
   */

  public void comment(CharSequence chars, Location locationId, int properties)
      throws XPathException {
    flush();
    Comment comment = new Comment(chars.toString());
    ancestors.peek().addContent(comment);
  }

  /**
   * Ask whether this Receiver (or the downstream pipeline) makes any use of the type annotations
   * supplied on element and attribute events
   *
   * @return true if the Receiver makes any use of this information. If false, the caller may supply
   *         untyped nodes instead of supplying the type annotation
   */

  public boolean usesTypeAnnotations() {
    return false;
  }

  /**
   * Get the constructed document node
   *
   * @return the document node of the constructed XOM tree
   */

  public Document getDocument() {
    return document;
  }

  /**
   * Get the current root node.
   *
   * @return a Saxon wrapper around the constructed XOM document node
   */

  public NodeInfo getCurrentRoot() {
    return new JDOM2DocumentWrapper(document, config).getRootNode();
  }
}

// Original Code is Copyright (c) 2009 Saxonica Limited. All rights reserved.
