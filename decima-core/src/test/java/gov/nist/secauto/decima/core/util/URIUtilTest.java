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

package gov.nist.secauto.decima.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class URIUtilTest {

  @Test
  public void testRelativizeChildPathResourceNoTrailing() throws URISyntaxException {
    URI thisUri = URI.create("http://test.com/foo");
    URI thatUri = URI.create("http://test.com/foo/bar");

    URI result = URIUtil.relativize(thisUri, thatUri, true);
    Assert.assertEquals("bar", result.toString());
  }

  @Test
  public void testRelativizeChildPathResourceTrailing() throws URISyntaxException {
    URI thisUri = URI.create("http://test.com/foo/");
    URI thatUri = URI.create("http://test.com/foo/bar");

    URI result = URIUtil.relativize(thisUri, thatUri, true);
    Assert.assertEquals("bar", result.toString());
  }

  @Test
  public void testRelativizeSiblingPathResource() throws URISyntaxException {
    URI thisUri = URI.create("http://test.com/foo/bar");
    URI thatUri = URI.create("http://test.com/foo/new");

    URI result = URIUtil.relativize(thisUri, thatUri, true);
    Assert.assertEquals("new", result.toString());
  }

  @Test
  public void testRelativizeCousinPathResource() throws URISyntaxException {
    URI thisUri = URI.create("http://test.com/foo/bar/cousinA");
    URI thatUri = URI.create("http://test.com/foo/new/cousinB");

    URI result = URIUtil.relativize(thisUri, thatUri, true);
    Assert.assertEquals("../new/cousinB", result.toString());
  }

  @Test
  public void testRelativizeChildPath() throws URISyntaxException {
    URI thisUri = URI.create("http://test.com/foo/");
    URI thatUri = URI.create("http://test.com/foo/bar");

    URI result = URIUtil.relativize(thisUri, thatUri, true);
    Assert.assertEquals("bar", result.toString());
  }

  @Test
  public void testRelativizeSiblingPath() throws URISyntaxException {
    URI thisUri = URI.create("http://test.com/foo/bar/");
    URI thatUri = URI.create("http://test.com/foo/new/");

    URI result = URIUtil.relativize(thisUri, thatUri, true);
    Assert.assertEquals("../new/", result.toString());
  }

  @Test
  public void testRelativizeCousinPath() throws URISyntaxException {
    URI thisUri = URI.create("http://test.com/foo/bar/cousinA/");
    URI thatUri = URI.create("http://test.com/foo/new/cousinB");

    URI result = URIUtil.relativize(thisUri, thatUri, true);
    Assert.assertEquals("../../new/cousinB", result.toString());
  }
}
