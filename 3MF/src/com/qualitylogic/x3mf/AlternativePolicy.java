package com.qualitylogic.x3mf;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/** Skip unsupported alternatives only when each use has a fallback for the selected consumer. */
final class AlternativePolicy {
    private static final String PA = "http://schemas.microsoft.com/3dmanufacturing/production/alternatives/2021/04";
    private static final String MODEL = "application/vnd.ms-package.3dmanufacturing-3dmodel+xml";
    private static final int LIMIT = 500000;
    private static final class Ref {
        String part, id;
        boolean fullres, preview;
    }
    private static final class Group { final List<Ref> alternatives = new ArrayList<>(); boolean primary; }
    private AlternativePolicy() { }
    private static String id(String text) {
        if (text == null) return null;
        try { return new java.math.BigInteger(text.trim()).toString(); }
        catch (NumberFormatException ex) { return text; }
    }
    private static org.w3c.dom.Document document(ZipFile zip, String name) throws Exception {
        ZipEntry entry = zip.getEntry(name);
        if (entry == null || entry.getSize() > 1000000) return null;
        try (InputStream input = zip.getInputStream(entry)) {
            return ModelSchema.parse(org.apache.commons.io.IOUtils.toString(input, "UTF-8"));
        }
    }
    static Set<String> skippedParts(ZipFile zip) {
        return skippedParts(zip, ValidationConsumer.PRINTER);
    }
    static Set<String> skippedParts(ZipFile zip, ValidationConsumer consumer) {
        try {
            org.w3c.dom.Document types = document(zip, "[Content_Types].xml");
            org.w3c.dom.Document rels = document(zip, "_rels/.rels");
            if (types == null || rels == null) return Collections.emptySet();
            Set<String> ordinary = new HashSet<>();
            NodeList links = rels.getElementsByTagNameNS("http://schemas.openxmlformats.org/package/2006/relationships", "Relationship");
            for (int i = 0; i < links.getLength(); i++) {
                Element e = (Element)links.item(i);
                if ("http://schemas.microsoft.com/3dmanufacturing/2013/01/3dmodel".equals(e.getAttribute("Type")))
                    ordinary.add(GeometryCheck.part("/", e.getAttribute("Target").trim()));
            }
            Map<String, String> overrides = new HashMap<>(), defaults = new HashMap<>();
            NodeList entries = types.getDocumentElement().getChildNodes();
            for (int i = 0; i < entries.getLength(); i++) if (entries.item(i) instanceof Element) {
                Element e = (Element)entries.item(i);
                if ("Override".equals(e.getLocalName())) overrides.put(e.getAttribute("PartName").trim().toLowerCase(java.util.Locale.ROOT), e.getAttribute("ContentType").trim());
                if ("Default".equals(e.getLocalName())) defaults.put(e.getAttribute("Extension").trim().toLowerCase(java.util.Locale.ROOT), e.getAttribute("ContentType").trim());
            }
            Map<String, ZipEntry> models = new HashMap<>();
            Set<String> unsupported = new HashSet<>();
            Enumeration<? extends ZipEntry> inventory = zip.entries();
            while (inventory.hasMoreElements()) {
                ZipEntry entry = inventory.nextElement();
                String part = "/" + entry.getName(), lower = part.toLowerCase(java.util.Locale.ROOT);
                String type = overrides.get(lower);
                if (type == null && lower.lastIndexOf('.') >= 0) type = defaults.get(lower.substring(lower.lastIndexOf('.') + 1));
                if (!MODEL.equals(type) || entry.getSize() > 200000000 || entry.isDirectory()) continue;
                if (models.size() >= LIMIT) return Collections.emptySet();
                models.put(part, entry);
                try (java.io.BufferedInputStream input = new java.io.BufferedInputStream(zip.getInputStream(entry))) {
                    input.mark(1);
                    int first = input.read();
                    input.reset();
                    if (first != '%' && ModelSchema.unsupportedRequired(input)) unsupported.add(part);
                } catch (Exception ex) { /* Ordinary validators diagnose malformed/opaque content. */ }
            }
            if (unsupported.isEmpty()) return Collections.emptySet();
            Map<String, Set<String>> ids = new HashMap<>();
            List<Group> groups = new ArrayList<>();
            final int[] records = {0};
            for (Map.Entry<String, ZipEntry> model : models.entrySet()) {
                String part = model.getKey();
                if (unsupported.contains(part)) continue; // Inspect only its header, not unsupported payload content.
                Set<String> objectIds = new HashSet<>();
                ids.put(part, objectIds);
                XMLReader reader = SafeXml.source("").getXMLReader();
                reader.setContentHandler(new DefaultHandler() {
                    private List<Ref> alternatives;
                    private final List<Group> objectGroups = new ArrayList<>();
                    private boolean primaryShape, primaryResolution;
                    private int depth, resourceDepth, objectDepth, groupDepth, ignoredDepth;
                    private boolean coreModel;
                    private void record() throws org.xml.sax.SAXException {
                        if (++records[0] > LIMIT) throw new org.xml.sax.SAXException("alternative policy reference budget exceeded");
                    }
                    @Override public void startElement(String uri, String local, String name, Attributes attributes) throws org.xml.sax.SAXException {
                        depth++;
                        if (depth == 1) coreModel = GeometryCheck.CORE.equals(uri) && "model".equals(local);
                        if (ignoredDepth > 0) return;
                        if (coreModel && uri != null && !uri.isEmpty() && !ModelSchema.supports(uri)) { ignoredDepth = depth; return; }
                        if (coreModel && depth == 2 && GeometryCheck.CORE.equals(uri) && "resources".equals(local)) resourceDepth = depth;
                        if (resourceDepth > 0 && depth == resourceDepth + 1 && GeometryCheck.CORE.equals(uri) && "object".equals(local)) {
                            record(); objectDepth = depth; objectIds.add(id(attributes.getValue("id")));
                            objectGroups.clear(); primaryShape = false;
                            String resolution = attributes.getValue(PA, "modelresolution");
                            primaryResolution = consumer == ValidationConsumer.EDITOR || resolution == null || "fullres".equals(resolution);
                        }
                        if (objectDepth > 0 && depth == objectDepth + 1 && ((GeometryCheck.CORE.equals(uri) && ("mesh".equals(local) || "components".equals(local)))
                            || (GeometryCheck.DISP.equals(uri) && "displacementmesh".equals(local))
                            || ("http://schemas.3mf.io/3dmanufacturing/booleanoperations/2023/07".equals(uri) && "booleanshape".equals(local)))) primaryShape = true;
                        if (objectDepth > 0 && depth == objectDepth + 1 && PA.equals(uri) && "alternatives".equals(local)) {
                            record(); Group group = new Group(); objectGroups.add(group); groups.add(group); alternatives = group.alternatives; groupDepth = depth;
                        }
                        if (PA.equals(uri) && "alternative".equals(local) && depth == groupDepth + 1) {
                            if (alternatives == null) return;
                            record();
                            Ref ref = new Ref();
                            String path = attributes.getValue("path");
                            ref.part = path == null || path.trim().isEmpty() ? part : GeometryCheck.part(part, path.trim());
                            ref.id = id(attributes.getValue("objectid"));
                            String resolution = attributes.getValue("modelresolution");
                            ref.fullres = resolution == null || "fullres".equals(resolution);
                            ref.preview = "lowres".equals(resolution) || "obfuscated".equals(resolution);
                            alternatives.add(ref);
                        } else {
                            String path = null;
                            if (GeometryCheck.CORE.equals(uri) && ("component".equals(local) || "item".equals(local))) path = attributes.getValue(GeometryCheck.PROD, "path");
                            if (GeometryCheck.CORE.equals(uri) && "object".equals(local)) path = attributes.getValue(GeometryCheck.SLICE, "slicepath");
                            if (GeometryCheck.SLICE.equals(uri) && "sliceref".equals(local)) path = attributes.getValue("", "slicepath");
                            if ("http://schemas.3mf.io/3dmanufacturing/booleanoperations/2023/07".equals(uri)
                                && ("boolean".equals(local) || "booleanshape".equals(local))) path = attributes.getValue("", "path");
                            if (path != null && !path.trim().isEmpty()) { record(); ordinary.add(GeometryCheck.part(part, path.trim())); }
                        }
                    }
                    @Override public void endElement(String uri, String local, String name) {
                        if (ignoredDepth > 0) { if (depth == ignoredDepth) ignoredDepth = 0; depth--; return; }
                        if (depth == groupDepth) { alternatives = null; groupDepth = 0; }
                        if (depth == objectDepth) {
                            for (Group group : objectGroups) group.primary = primaryShape && primaryResolution;
                            objectGroups.clear(); objectDepth = 0;
                        }
                        if (depth == resourceDepth) resourceDepth = 0;
                        depth--;
                    }
                });
                try (InputStream input = zip.getInputStream(model.getValue())) { reader.parse(new InputSource(input)); }
                catch (Exception ex) { return Collections.emptySet(); } // Never waive a check when eligibility is uncertain.
            }
            Set<String> candidates = new HashSet<>(), blocked = new HashSet<>(ordinary);
            for (Group group : groups) {
                boolean fallback = group.primary;
                for (Ref ref : group.alternatives) if ((ref.fullres || (consumer == ValidationConsumer.EDITOR && ref.preview)) && !unsupported.contains(ref.part)
                    && ids.containsKey(ref.part) && ids.get(ref.part).contains(ref.id)) fallback = true;
                for (Ref ref : group.alternatives) if (unsupported.contains(ref.part)) {
                    candidates.add(ref.part);
                    if (!fallback) blocked.add(ref.part);
                }
            }
            candidates.removeAll(blocked);
            return candidates;
        } catch (Exception ex) { return Collections.emptySet(); }
    }
}
