package me.qtill.commons.io;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ClassPathUtilTest {

    @Test
    public void testClasspath() {
        assertEquals("file:/Users/paranoidq/git-repo/qtill/qtill-commons/target/test-classes/", ClassPathUtil.getClasspathAbsolutePath());
    }

    @Test
    public void testRelativePath() throws MalformedURLException {
        URL url = ClassPathUtil.getAbsoluteUrl("test.properties");
        assertEquals("file:/Users/paranoidq/git-repo/qtill/qtill-commons/target/test-classes/test.properties", url.toString());
    }

    @Test
    public void testRelativePathResouce() throws MalformedURLException {
        URL url = ClassPathUtil.getResource("test.properties");
        assertEquals("file:/Users/paranoidq/git-repo/qtill/qtill-commons/target/test-classes/test.properties", url.toString());
    }

    @Test
    public void testWildcardRelativePath() throws MalformedURLException {
        URL url = ClassPathUtil.getResource("../classes/resources.properties");
        assertEquals("file:/Users/paranoidq/git-repo/qtill/qtill-commons/target/classes/resources.properties", url.toString());

        url = ClassPathUtil.getResource("../../pom.xml");
        assertEquals("file:/Users/paranoidq/git-repo/qtill/qtill-commons/pom.xml", url.toString());
    }

    @Test
    public void testAnt() throws MalformedURLException {
        URL url = ClassPathUtil.getResource("*.properties");

    }
}