package test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class SoapSerializer {

	public static Object deserialize(String content) throws Exception {
        Document document = readXmlDocument(content, false);
        if (document != null) {
            return deserialize(document);
        } else {
            return null;
        }
    }
	
    public static Object deserialize(Document document) throws Exception {
        Element rootElement = document.getDocumentElement();
        // find the first element below the root element, that should be the object
        Node curChild = rootElement.getFirstChild();
        while (curChild != null && curChild.getNodeType() != Node.ELEMENT_NODE) {
            curChild = curChild.getNextSibling();
        }
        if (curChild == null) {
            return null;
        }
        return deserializeSingle((Element) curChild);
    }
    
    public static Object deserializeSingle(Element element) throws Exception {
        String tagName = element.getLocalName();

        if (tagName.equals("null")) return null;

        if (tagName.startsWith("std-")) {
            // - Standard Objects -
            if ("std-String".equals(tagName)) {
                return element.getAttribute("value");
            } else if ("std-Integer".equals(tagName)) {
                String valStr = element.getAttribute("value");
                return Integer.valueOf(valStr);
            } else if ("std-Long".equals(tagName)) {
                String valStr = element.getAttribute("value");
                return Long.valueOf(valStr);
            } else if ("std-Float".equals(tagName)) {
                String valStr = element.getAttribute("value");
                return Float.valueOf(valStr);
            } else if ("std-Double".equals(tagName)) {
                String valStr = element.getAttribute("value");
                return Double.valueOf(valStr);
            } else if ("std-BigDecimal".equals(tagName)) {
                String valStr = element.getAttribute("value");
                return new BigDecimal(valStr);
            } else if ("std-Boolean".equals(tagName)) {
                String valStr = element.getAttribute("value");
                return Boolean.valueOf(valStr);
            } else if ("std-Locale".equals(tagName)) {
                String valStr = element.getAttribute("value");
//                return UtilMisc.parseLocale(valStr);
                return null;
            }
        } else if (tagName.startsWith("sql-")) {
            // - SQL Objects -
            if ("sql-Timestamp".equals(tagName)) {
                String valStr = element.getAttribute("value");
                /*
                 * sql-Timestamp is defined as xsd:dateTime in ModelService.getTypes(),
                 * so try to parse the value as xsd:dateTime first.
                 * Fallback is java.sql.Timestamp because it has been this way all the time.
                 */
                try {
                    Calendar cal = DatatypeConverter.parseDate(valStr);
                    return new java.sql.Timestamp(cal.getTimeInMillis());
                }
                catch (Exception e) {
                    return java.sql.Timestamp.valueOf(valStr);
                }
            } else if ("sql-Date".equals(tagName)) {
                String valStr = element.getAttribute("value");
                return java.sql.Date.valueOf(valStr);
            } else if ("sql-Time".equals(tagName)) {
                String valStr = element.getAttribute("value");
                return java.sql.Time.valueOf(valStr);
            }
        } else if (tagName.startsWith("col-")) {
            // - Collections -
            Collection<Object> value = null;

            if ("col-ArrayList".equals(tagName)) {
                value = new ArrayList<Object>();
            } else if ("col-LinkedList".equals(tagName)) {
                value = new LinkedList<Object>();
            } else if ("col-Stack".equals(tagName)) {
                value = new Stack<Object>();
            } else if ("col-Vector".equals(tagName)) {
                value = new Vector<Object>();
            } else if ("col-TreeSet".equals(tagName)) {
                value = new TreeSet<Object>();
            } else if ("col-HashSet".equals(tagName)) {
                value = new HashSet<Object>();
            } else if ("col-Collection".equals(tagName)) {
                value = new LinkedList<Object>();
            }

            if (value == null) {
//                return deserializeCustom(element);
                return null;
            } else {
                Node curChild = element.getFirstChild();

                while (curChild != null) {
                    if (curChild.getNodeType() == Node.ELEMENT_NODE) {
                        value.add(deserializeSingle((Element) curChild));
                    }
                    curChild = curChild.getNextSibling();
                }
                return value;
            }
        } else if (tagName.startsWith("map-")) {
            // - Maps -
            Map<Object, Object> value = null;

            if ("map-HashMap".equals(tagName)) {
                value = new HashMap<Object, Object>();
            } else if ("map-Properties".equals(tagName)) {
                value = new Properties();
            } else if ("map-Hashtable".equals(tagName)) {
                value = new Hashtable<Object, Object>();
            } else if ("map-WeakHashMap".equals(tagName)) {
                value = new WeakHashMap<Object, Object>();
            } else if ("map-TreeMap".equals(tagName)) {
                value = new TreeMap<Object, Object>();
            } else if ("map-Map".equals(tagName)) {
                value = new HashMap<Object, Object>();
            }

            if (value == null) {
//                return deserializeCustom(element);
                return null;
            } else {
                Node curChild = element.getFirstChild();

                while (curChild != null) {
                    if (curChild.getNodeType() == Node.ELEMENT_NODE) {
                        Element curElement = (Element) curChild;

                        if ("map-Entry".equals(curElement.getLocalName())) {

                            Element mapKeyElement = firstChildElement(curElement, "map-Key");
                            Element keyElement = null;
                            Node tempNode = mapKeyElement.getFirstChild();

                            while (tempNode != null) {
                                if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                                    keyElement = (Element) tempNode;
                                    break;
                                }
                                tempNode = tempNode.getNextSibling();
                            }
                            if (keyElement == null) throw new Exception("Could not find an element under the map-Key");

                            Element mapValueElement = firstChildElement(curElement, "map-Value");
                            Element valueElement = null;

                            tempNode = mapValueElement.getFirstChild();
                            while (tempNode != null) {
                                if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                                    valueElement = (Element) tempNode;
                                    break;
                                }
                                tempNode = tempNode.getNextSibling();
                            }
                            if (valueElement == null) throw new Exception("Could not find an element under the map-Value");

                            value.put(deserializeSingle(keyElement), deserializeSingle(valueElement));
                        }
                    }
                    curChild = curChild.getNextSibling();
                }
                return value;
            }
        } else if (tagName.startsWith("eepk-")) {
//            return delegator.makePK(element);
            return null;
        } else if (tagName.startsWith("eeval-")) {
            return null;
//            return delegator.makeValue(element);
        }

//        return deserializeCustom(element);
        return null;
    }
    
    /** Return the first child Element with the given name; if name is null
     * returns the first element. */
    public static Element firstChildElement(Element element, String childElementName) {
        if (element == null) return null;
        if (null == childElementName) return null;
        // get the first element with the given name
        Node node = element.getFirstChild();

        if (node != null) {
            do {
                String nodeName = node.getLocalName();
                if (nodeName == null){
                    nodeName = getNodeNameIgnorePrefix(node);
                }
                if (node.getNodeType() == Node.ELEMENT_NODE && (childElementName == null ||
                    childElementName.equals(nodeName))) {
                    Element childElement = (Element) node;
                    return childElement;
                }
            } while ((node = node.getNextSibling()) != null);
        }
        return null;
    }
    
    /**
     * get node name without any prefix
     * @param node
     * @return
     */
    public static String getNodeNameIgnorePrefix(Node node){
        if (node==null) return null;
        String nodeName = node.getNodeName();
        if (nodeName.contains(":")){
            // remove any possible prefix
            nodeName = nodeName.split(":")[1];
        }
        return nodeName;
    }
    
//    public static Object deserializeCustom(Element element) throws Exception {
//        String tagName = element.getLocalName();
//        if ("cus-obj".equals(tagName)) {
//            String value = UtilXml.elementValue(element);
//            if (value != null) {
//                byte[] valueBytes = StringUtil.fromHexString(value);
//                if (valueBytes != null) {
//                    Object obj = UtilObject.getObject(valueBytes);
//                    if (obj != null) {
//                        return obj;
//                    }
//                }
//            }
//            throw new Exception("Problem deserializing object from byte array + " + element.getLocalName());
//        } else {
//            throw new Exception("Cannot deserialize element named " + element.getLocalName());
//        }
//    }
	
	public static Document readXmlDocument(String content, boolean validate)
            throws SAXException, ParserConfigurationException, java.io.IOException {
        if (content == null) {
            return null;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes("UTF-8"));
        return readXmlDocument(bis, validate, "Internal Content");
    }
	
	public static Document readXmlDocument(InputStream is, boolean validate, String docDescription)
            throws SAXException, ParserConfigurationException, java.io.IOException {
        if (is == null) {
            return null;
        }

        long startTime = System.currentTimeMillis();

        Document document = null;

        /* Standard JAXP (mostly), but doesn't seem to be doing XML Schema validation, so making sure that is on... */
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(validate);
        factory.setNamespaceAware(true);

        factory.setAttribute("http://xml.org/sax/features/validation", validate);
        factory.setAttribute("http://apache.org/xml/features/validation/schema", validate);

        // with a SchemaUrl, a URL object
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(is);

        double totalSeconds = (System.currentTimeMillis() - startTime)/1000.0;
        return document;
    }

}
