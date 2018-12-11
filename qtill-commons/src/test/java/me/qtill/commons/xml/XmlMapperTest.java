package me.qtill.commons.xml;

import org.junit.Test;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static org.junit.Assert.assertEquals;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class XmlMapperTest {

    @XmlRootElement
    public static class User {
        @XmlElement
        private String name;
        @XmlElement
        private String type;


        public User() {
        }

        public User(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    @Test
    public void toXml() {
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<user>\n" +
            "    <name>qq</name>\n" +
            "    <type>t</type>\n" +
            "</user>\n";
        User user = new User("qq", "t");
        assertEquals(expected, XmlMapper.toXml(user));

    }

    @Test
    public void fromXml() {
    }
}