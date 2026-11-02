package com.qualitylogic.x3mf;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;

/** Compatibility corrections for the consolidated Core/extension schema. */
final class ModelSchema {
    private ModelSchema() { }

    // Match complete namespace URIs, including the newer schemas.3mf.io extensions.
    private static final java.util.Set<String> SUPPORTED = new java.util.HashSet<>(java.util.Arrays.asList(
            GeometryCheck.CORE, GeometryCheck.SLICE, GeometryCheck.PROD, GeometryCheck.DISP,
            FileStructureCheck.bns, FileStructureCheck.scns,
            "http://schemas.microsoft.com/3dmanufacturing/material/2015/02",
            "http://schemas.microsoft.com/3dmanufacturing/beamlattice/balls/2020/07",
            "http://schemas.microsoft.com/3dmanufacturing/mirroring/2021/07",
            "http://schemas.microsoft.com/3dmanufacturing/trianglesets/2021/07",
            "http://schemas.microsoft.com/3dmanufacturing/production/alternatives/2021/04",
            "http://schemas.3mf.io/3dmanufacturing/booleanoperations/2023/07",
            "http://schemas.3mf.io/3dmanufacturing/volumetric/2022/01",
            "http://schemas.3mf.io/3dmanufacturing/implicit/2023/12",
            "http://schemas.3mf.io/3dmanufacturing/toolpath/2026/03",
            "http://print.ticket.com"));
    static boolean supports(String uri) { return SUPPORTED.contains(uri); }

    static String unsupportedRequired(Document doc) {
        Element root = doc.getDocumentElement();
        if (!GeometryCheck.CORE.equals(root.getNamespaceURI()) || !"model".equals(root.getLocalName())) return null;
        for (String prefix : root.getAttribute("requiredextensions").trim().split("\\s+"))
            if (!prefix.isEmpty() && !supports(root.lookupNamespaceURI(prefix))) return prefix;
        return null;
    }

    /** Inspect only the model header; unsupported alternatives must not be processed. */
    static boolean unsupportedRequired(java.io.InputStream input) throws SAXException, IOException {
        return unsupportedRequired(new InputSource(input));
    }
    static boolean unsupportedRequired(String xml) throws SAXException, IOException {
        return unsupportedRequired(new InputSource(new StringReader(xml)));
    }
    private static boolean unsupportedRequired(InputSource input) throws SAXException, IOException {
        final java.util.Map<String, String> prefixes = new java.util.HashMap<>();
        final boolean[] unsupported = {false};
        final SAXException done = new SAXException("model header inspected");
        org.xml.sax.XMLReader reader = SafeXml.source("").getXMLReader();
        reader.setContentHandler(new DefaultHandler() {
            @Override public void startPrefixMapping(String prefix, String uri) { prefixes.put(prefix, uri); }
            @Override public void startElement(String uri, String local, String name, Attributes attributes) throws SAXException {
                String required = attributes.getValue("requiredextensions");
                if (GeometryCheck.CORE.equals(uri) && "model".equals(local) && required != null)
                    for (String prefix : required.trim().split("\\s+"))
                        if (!prefix.isEmpty() && !supports(prefixes.get(prefix))) unsupported[0] = true;
                throw done;
            }
        });
        try { reader.parse(input); } catch (SAXException ex) { if (ex != done) throw ex; }
        return unsupported[0];
    }

    /** Ignore unsupported optional element subtrees, never errors in supported content. */
    static SAXSource validationSource(String xml) throws SAXException {
        SAXSource source = SafeXml.source(xml);
        XMLFilterImpl filter = new XMLFilterImpl(source.getXMLReader()) {
            private final java.util.Map<String, String> prefixes = new java.util.HashMap<>();
            private boolean rootSeen, model;
            private int ignored;
            @Override public void startPrefixMapping(String prefix, String uri) throws SAXException {
                if (!rootSeen) prefixes.put(prefix, uri);
                super.startPrefixMapping(prefix, uri);
            }
            @Override public void startElement(String uri, String local, String name, Attributes attributes) throws SAXException {
                if (!rootSeen) {
                    rootSeen = true;
                    model = GeometryCheck.CORE.equals(uri) && "model".equals(local);
                    String required = attributes.getValue("requiredextensions");
                    if (model && required != null) for (String prefix : required.trim().split("\\s+"))
                        if (!prefix.isEmpty() && !supports(prefixes.get(prefix)))
                            throw new SAXException("Unsupported required extension: " + prefix + " (" + prefixes.get(prefix) + ").");
                }
                if (ignored > 0 || (model && uri != null && !uri.isEmpty() && !supports(uri))) { ignored++; return; }
                super.startElement(uri, local, name, attributes);
            }
            @Override public void endElement(String uri, String local, String name) throws SAXException {
                if (ignored > 0) { ignored--; return; }
                super.endElement(uri, local, name);
            }
            @Override public void characters(char[] ch, int start, int length) throws SAXException {
                if (ignored == 0) super.characters(ch, start, length);
            }
            @Override public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
                if (ignored == 0) super.ignorableWhitespace(ch, start, length);
            }
        };
        return new SAXSource(filter, source.getInputSource());
    }

    static Document parse(String xml) throws SAXException, IOException {
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = SafeXml.factory();
            factory.setNamespaceAware(true);
            return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        } catch (ParserConfigurationException ex) { throw new SAXException(ex); }
    }

    static boolean usesProduction(Document doc) {
        Element root = doc.getDocumentElement();
        if (!GeometryCheck.CORE.equals(root.getNamespaceURI()) || !"model".equals(root.getLocalName())) return false;
        for (String prefix : root.getAttribute("requiredextensions").trim().split("\\s+"))
            if (!prefix.isEmpty() && GeometryCheck.PROD.equals(root.lookupNamespaceURI(prefix))) return true;
        NodeList nodes = doc.getElementsByTagNameNS(GeometryCheck.CORE, "*");
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.NamedNodeMap attributes = nodes.item(i).getAttributes();
            for (int j = 0; j < attributes.getLength(); j++)
                if (GeometryCheck.PROD.equals(attributes.item(j).getNamespaceURI())) return true;
        }
        return false;
    }

    /** Inspect large model strings without retaining another model DOM. */
    static boolean usesProduction(String xml) throws SAXException, IOException {
        final java.util.Map<String, String> prefixes = new java.util.HashMap<String, String>();
        final boolean[] state = new boolean[3]; // root seen, Core model root, Production used
        SAXSource source = SafeXml.source(xml);
        source.getXMLReader().setContentHandler(new DefaultHandler() {
            @Override public void startPrefixMapping(String prefix, String uri) {
                if (!state[0]) prefixes.put(prefix, uri);
            }
            @Override public void startElement(String uri, String local, String name, Attributes attributes) {
                if (!state[0]) {
                    state[0] = true;
                    state[1] = GeometryCheck.CORE.equals(uri) && "model".equals(local);
                    String required = attributes.getValue("requiredextensions");
                    if (state[1] && required != null) for (String prefix : required.trim().split("\\s+"))
                        if (!prefix.isEmpty() && GeometryCheck.PROD.equals(prefixes.get(prefix))) state[2] = true;
                }
                if (state[1] && GeometryCheck.CORE.equals(uri)) for (int j = 0; j < attributes.getLength(); j++)
                    if (GeometryCheck.PROD.equals(attributes.getURI(j))) state[2] = true;
            }
        });
        source.getXMLReader().parse(source.getInputSource());
        return state[2];
    }

    static Schema load(File file, String xml, boolean packageProduction) throws SAXException, IOException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        if (!"qli_3MF.xsd".equals(file.getName())) return factory.newSchema(file);
        boolean production = packageProduction || usesProduction(xml);
        Document schema;
        try {
            javax.xml.parsers.DocumentBuilderFactory builder = SafeXml.factory();
            builder.setNamespaceAware(true);
            schema = builder.newDocumentBuilder().parse(file);
        } catch (ParserConfigurationException ex) { throw new SAXException(ex); }
        if (!GeometryCheck.CORE.equals(schema.getDocumentElement().getAttribute("targetNamespace")))
            return factory.newSchema(file);
        NodeList types = schema.getDocumentElement().getElementsByTagNameNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "complexType");
        for (int i = 0; i < types.getLength(); i++) {
            Element type = (Element)types.item(i);
            String name = type.getAttribute("name");
            if (!"CT_Object".equals(name) && !"CT_Component".equals(name) && !"CT_Item".equals(name)) continue;
            boolean wildcard = false;
            for (Node child = type.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (!(child instanceof Element) || !XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(child.getNamespaceURI())) continue;
                Element attribute = (Element)child;
                if ("anyAttribute".equals(attribute.getLocalName())) wildcard = true;
                String ref = attribute.getAttribute("ref");
                int colon = ref.indexOf(':');
                if ("attribute".equals(attribute.getLocalName()) && colon > 0 && "UUID".equals(ref.substring(colon + 1))
                        && GeometryCheck.PROD.equals(attribute.lookupNamespaceURI(ref.substring(0, colon))))
                    attribute.setAttribute("use", production ? "required" : "optional");
            }
            if ("CT_Object".equals(name) && !wildcard) {
                Element any = schema.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "xs:anyAttribute");
                any.setAttribute("namespace", "##other");
                any.setAttribute("processContents", "lax");
                type.appendChild(any);
            }
        }
        // Keep the original base URI so all imports/includes resolve as before.
        return factory.newSchema(new DOMSource(schema, file.toURI().toString()));
    }
}
