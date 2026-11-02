package com.qualitylogic.x3mf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Semantic constraints not expressible in the consolidated XSD. No model DOMs retained. */
final class ModelRules {
    private static final String C = GeometryCheck.CORE, S = GeometryCheck.SLICE, P = GeometryCheck.PROD;
    private static final String B = FileStructureCheck.bns, D = GeometryCheck.DISP;
    private static final String BO = "http://schemas.3mf.io/3dmanufacturing/booleanoperations/2023/07";
    private static final String M = "http://schemas.microsoft.com/3dmanufacturing/material/2015/02";
    private static final String PA = "http://schemas.microsoft.com/3dmanufacturing/production/alternatives/2021/04";
    private final GeometryCheck.Sink sink;
    private final ValidationConsumer consumer;
    private final Set<String> unavailable = new HashSet<>();
    private final Map<String, Model> models = new LinkedHashMap<>();
    private final Set<String> reported = new HashSet<>();
    private int records, visits;
    private static final int LIMIT = 500000;

    ModelRules(GeometryCheck.Sink sink) { this(sink, ValidationConsumer.EDITOR); }
    ModelRules(GeometryCheck.Sink sink, ValidationConsumer consumer) { this.sink = sink; this.consumer = consumer; }
    void unavailable(String part) { unavailable.add(GeometryCheck.part("/", part)); }
    private void issue(String code, String key, String text) {
        if (reported.add(code + "|" + key)) sink.issue(code, false, text);
    }
    private boolean room() {
        if (++records <= LIMIT) return true;
        if (reported.add("limit")) sink.issue("WR_029", true, "Semantic reference validation incomplete: record budget exceeded.");
        return false;
    }
    private static boolean is(Node n, String ns, String local) {
        return n instanceof Element && ns.equals(n.getNamespaceURI()) && local.equals(n.getLocalName());
    }
    private static List<Element> children(Element parent) {
        List<Element> out = new ArrayList<>();
        if (parent != null) for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling())
            if (n instanceof Element) out.add((Element)n);
        return out;
    }
    private static Element child(Element parent, String ns, String local) {
        if (parent != null) for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling())
            if (is(n, ns, local)) return (Element)n;
        return null;
    }
    private static double number(String text) {
        try { double n = Double.parseDouble(text); return Double.isFinite(n) ? n : Double.NaN; }
        catch (NumberFormatException ex) { return Double.NaN; } // Schema reports lexical errors.
    }
    private static Long index(String text) {
        try { return Long.valueOf(text.trim()); } catch (NumberFormatException ex) { return null; }
    }
    private static String id(String text) {
        String value = text.trim();
        if (value.isEmpty()) return value;
        try { return new java.math.BigInteger(value).toString(); }
        catch (NumberFormatException ex) { return value; }
    }
    private static final class Link {
        String part, id, kind;
        String resolution;
        boolean printable = true;
        Link(String part, String id, String kind) { this.part = part; this.id = id; this.kind = kind; }
    }
    private static final class ObjectInfo {
        String type, resolution = "fullres";
        boolean mesh, beam, displaced, payload, requiredSlices;
        Link slices;
        final List<Link> components = new ArrayList<>(), alternatives = new ArrayList<>(), operands = new ArrayList<>();
    }
    private static final class Stack {
        String key;
        double bottom, first = Double.NaN, last = Double.NaN;
        boolean open;
        final List<Link> refs = new ArrayList<>();
    }
    private static final class Model {
        final Map<String, ObjectInfo> objects = new HashMap<>();
        final Map<String, Stack> stacks = new HashMap<>();
        final List<Link> uses = new ArrayList<>(), build = new ArrayList<>();
        boolean toolpath, unsupported, requiredSlice;
    }
    private Link objectLink(Element element, String part, String kind) {
        String path = element.getAttributeNS(P, "path");
        if (path.isEmpty()) path = element.getAttribute("path");
        path = path.trim();
        return new Link(path.isEmpty() ? part : GeometryCheck.part(part, path), id(element.getAttribute("objectid")), kind);
    }

    void addModel(Document doc, String name) {
        String part = GeometryCheck.part("/", name);
        Element root = doc.getDocumentElement();
        if (part == null || models.containsKey(part) || !room()) return;
        if (!is(root, C, "model")) {
            issue("ER_198", part, "A model part must have a Core model root element: " + part + ".");
            return;
        }
        Model model = new Model();
        models.put(part, model);
        for (String prefix : root.getAttribute("requiredextensions").trim().split("\\s+"))
            if (!prefix.isEmpty() && !ModelSchema.supports(root.lookupNamespaceURI(prefix))) {
                model.unsupported = true;
                issue("ER_192", part, "Unsupported required extension " + prefix + " in " + part + ".");
            } else if (S.equals(root.lookupNamespaceURI(prefix))) model.requiredSlice = true;
        metadata(root, part);
        NodeList groups = root.getElementsByTagNameNS(C, "metadatagroup");
        for (int i = 0; i < groups.getLength(); i++) metadata((Element)groups.item(i), part);
        Element resources = child(root, C, "resources");
        Map<String, Element> ids = new HashMap<>();
        for (Element e : children(resources)) if (e.hasAttribute("id")) ids.put(id(e.getAttribute("id")), e);
        for (Element e : children(resources)) {
            if (!room()) break;
            if (is(e, S, "slicestack")) {
                Stack stack = slices(e, part);
                model.stacks.put(id(e.getAttribute("id")), stack);
            }
            if (!is(e, C, "object")) continue;
            String key = part + " object " + e.getAttribute("id");
            ObjectInfo object = new ObjectInfo();
            object.type = e.hasAttribute("type") ? e.getAttribute("type") : "model";
            object.resolution = e.hasAttributeNS(PA, "modelresolution") ? e.getAttributeNS(PA, "modelresolution") : "fullres";
            Element mesh = child(e, C, "mesh");
            object.mesh = mesh != null;
            object.displaced = child(e, D, "displacementmesh") != null;
            object.payload = object.mesh || object.displaced || child(e, BO, "booleanshape") != null;
            object.requiredSlices = model.requiredSlice && "lowres".equals(e.getAttributeNS(S, "meshresolution"));
            object.beam = mesh != null && child(mesh, B, "beamlattice") != null;
            if (object.mesh) triangles(mesh, ids, key, C);
            if (object.displaced) triangles(child(e, D, "displacementmesh"), ids, key, D);
            if (e.hasAttributeNS(S, "slicestackid")) {
                String path = e.getAttributeNS(S, "slicepath").trim();
                object.slices = new Link(path.isEmpty() ? part : GeometryCheck.part(part, path), id(e.getAttributeNS(S, "slicestackid")), "slice");
            }
            for (Element component : children(child(e, C, "components")))
                if (is(component, C, "component") && room()) object.components.add(objectLink(component, part, "component"));
            if (object.beam) {
                Element lattice = child(mesh, B, "beamlattice");
                if (lattice.hasAttribute("representationmesh"))
                    model.uses.add(new Link(part, id(lattice.getAttribute("representationmesh")), "representation"));
            }
            for (Element shape : children(e)) if (is(shape, BO, "booleanshape")) {
                Link base = objectLink(shape, part, "boolean base");
                model.uses.add(base); object.operands.add(base);
                for (Element operand : children(shape)) if (is(operand, BO, "boolean") && room()) {
                    Link link = objectLink(operand, part, "boolean operand");
                    model.uses.add(link); object.operands.add(link);
                }
            }
            for (Element alternative : children(child(e, PA, "alternatives"))) if (is(alternative, PA, "alternative") && room()) {
                Link link = objectLink(alternative, part, "alternative");
                link.resolution = alternative.hasAttribute("modelresolution") ? alternative.getAttribute("modelresolution") : "fullres";
                object.alternatives.add(link);
            }
            model.objects.put(id(e.getAttribute("id")), object);
        }
        Element build = child(root, C, "build");
        model.toolpath = build != null && build.hasAttributeNS("http://schemas.3mf.io/3dmanufacturing/toolpath/2026/03", "toolpathid");
        for (Element item : children(build)) if (is(item, C, "item") && room()) {
            Link link = objectLink(item, part, "build");
            link.printable = !"false".equals(item.getAttribute("printable").trim()) && !"0".equals(item.getAttribute("printable").trim());
            model.build.add(link);
        }
    }

    /** Schema-only validation uses the same availability graph without a second model DOM. */
    void addConsumerModel(String xml, String name) throws org.xml.sax.SAXException, java.io.IOException {
        String part = GeometryCheck.part("/", name);
        if (part == null || models.containsKey(part)) return;
        Model model = new Model();
        org.xml.sax.XMLReader reader = SafeXml.source("").getXMLReader();
        final boolean[] core = {false};
        reader.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            private final Map<String, String> prefixes = new HashMap<>();
            private int depth, ignored, resourceDepth, objectDepth, componentDepth, alternativeDepth, booleanDepth, buildDepth, stackDepth;
            private ObjectInfo object;
            private Stack stack;
            private Link link(org.xml.sax.Attributes attributes, String kind) {
                String path = attributes.getValue(P, "path");
                if (path == null) path = attributes.getValue("", "path");
                String objectId = attributes.getValue("", "objectid");
                return new Link(path == null || path.trim().isEmpty() ? part : GeometryCheck.part(part, path.trim()),
                    objectId == null ? "" : id(objectId), kind);
            }
            @Override public void startPrefixMapping(String prefix, String uri) { if (depth == 0) prefixes.put(prefix, uri); }
            @Override public void startElement(String ns, String local, String name, org.xml.sax.Attributes attributes) {
                depth++;
                if (depth == 1) {
                    core[0] = C.equals(ns) && "model".equals(local);
                    String required = attributes.getValue("", "requiredextensions");
                    if (required != null) for (String prefix : required.trim().split("\\s+")) if (!prefix.isEmpty()) {
                        model.unsupported |= !ModelSchema.supports(prefixes.get(prefix));
                        model.requiredSlice |= S.equals(prefixes.get(prefix));
                    }
                }
                if (!core[0] || ignored > 0) return;
                if (ns != null && !ns.isEmpty() && !ModelSchema.supports(ns)) { ignored = depth; return; }
                if (depth == 2 && C.equals(ns) && "resources".equals(local)) resourceDepth = depth;
                if (depth == 2 && C.equals(ns) && "build".equals(local)) {
                    buildDepth = depth;
                    model.toolpath = attributes.getValue("http://schemas.3mf.io/3dmanufacturing/toolpath/2026/03", "toolpathid") != null;
                }
                if (resourceDepth > 0 && depth == resourceDepth + 1 && S.equals(ns) && "slicestack".equals(local) && room()) {
                    stack = new Stack(); stackDepth = depth;
                    model.stacks.put(id(attributes.getValue("", "id") == null ? "" : attributes.getValue("", "id")), stack);
                }
                if (stack != null && depth == stackDepth + 1 && S.equals(ns) && "sliceref".equals(local) && room()) {
                    String path = attributes.getValue("", "slicepath"), target = attributes.getValue("", "slicestackid");
                    stack.refs.add(new Link(path == null || path.trim().isEmpty() ? part : GeometryCheck.part(part, path.trim()), target == null ? "" : id(target), "slice"));
                }
                if (resourceDepth > 0 && depth == resourceDepth + 1 && C.equals(ns) && "object".equals(local)) {
                    objectDepth = depth; object = null;
                    if (!room()) return;
                    object = new ObjectInfo();
                    object.type = attributes.getValue("", "type");
                    if (object.type == null) object.type = "model";
                    object.resolution = attributes.getValue(PA, "modelresolution");
                    if (object.resolution == null) object.resolution = "fullres";
                    object.requiredSlices = model.requiredSlice && "lowres".equals(attributes.getValue(S, "meshresolution"));
                    String slice = attributes.getValue(S, "slicestackid"), path = attributes.getValue(S, "slicepath");
                    if (slice != null) object.slices = new Link(path == null || path.trim().isEmpty() ? part : GeometryCheck.part(part, path.trim()), id(slice), "slice");
                    String objectId = attributes.getValue("", "id");
                    if (objectId != null) model.objects.put(id(objectId), object);
                }
                if (object != null && depth == objectDepth + 1) {
                    if ((C.equals(ns) && "mesh".equals(local)) || (D.equals(ns) && "displacementmesh".equals(local))) object.payload = true;
                    if (C.equals(ns) && "components".equals(local)) componentDepth = depth;
                    if (PA.equals(ns) && "alternatives".equals(local)) alternativeDepth = depth;
                    if (BO.equals(ns) && "booleanshape".equals(local)) {
                        object.payload = true; booleanDepth = depth;
                        if (room()) object.operands.add(link(attributes, "boolean base"));
                    }
                }
                if (object != null && roomForReference(ns, local)) {
                    if (componentDepth > 0 && depth == componentDepth + 1 && C.equals(ns) && "component".equals(local)) object.components.add(link(attributes, "component"));
                    if (booleanDepth > 0 && depth == booleanDepth + 1 && BO.equals(ns) && "boolean".equals(local)) object.operands.add(link(attributes, "boolean operand"));
                    if (alternativeDepth > 0 && depth == alternativeDepth + 1 && PA.equals(ns) && "alternative".equals(local)) {
                        Link alternative = link(attributes, "alternative");
                        alternative.resolution = attributes.getValue("", "modelresolution");
                        if (alternative.resolution == null) alternative.resolution = "fullres";
                        object.alternatives.add(alternative);
                    }
                }
                if (buildDepth > 0 && depth == buildDepth + 1 && C.equals(ns) && "item".equals(local) && room()) {
                    Link item = link(attributes, "build");
                    String printable = attributes.getValue("", "printable");
                    item.printable = printable == null || (!"false".equals(printable.trim()) && !"0".equals(printable.trim()));
                    model.build.add(item);
                }
            }
            private boolean roomForReference(String ns, String local) {
                return ((C.equals(ns) && "component".equals(local)) || (BO.equals(ns) && "boolean".equals(local))
                    || (PA.equals(ns) && "alternative".equals(local))) && room();
            }
            @Override public void endElement(String ns, String local, String name) {
                if (ignored > 0) { if (depth == ignored) ignored = 0; depth--; return; }
                if (depth == resourceDepth) resourceDepth = 0;
                if (depth == componentDepth) componentDepth = 0;
                if (depth == alternativeDepth) alternativeDepth = 0;
                if (depth == booleanDepth) booleanDepth = 0;
                if (depth == buildDepth) buildDepth = 0;
                if (depth == stackDepth) { stackDepth = 0; stack = null; }
                if (depth == objectDepth) { object = null; objectDepth = 0; }
                depth--;
            }
        });
        reader.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
        if (core[0]) models.put(part, model);
    }

    private void metadata(Element parent, String part) {
        Set<String> names = new HashSet<>();
        for (Element e : children(parent)) if (is(e, C, "metadata")) {
            String name = e.getAttribute("name");
            int colon = name.indexOf(':');
            String expanded = colon < 0 ? name : "{" + e.lookupNamespaceURI(name.substring(0, colon)) + "}" + name.substring(colon + 1);
            if (!names.add(expanded)) issue("ER_193", part + ":" + expanded,
                "Duplicate metadata name " + name + " within one metadata scope in " + part + ".");
        }
    }
    private void triangles(Element mesh, Map<String, Element> ids, String key, String ns) {
        Element triangles = child(mesh, ns, "triangles");
        if (triangles == null) return;
        boolean duplicateReported = false, propertyReported = false;
        for (Node n = triangles.getFirstChild(); n != null; n = n.getNextSibling()) if (is(n, ns, "triangle")) {
            Element t = (Element)n;
            Long v1 = index(t.getAttribute("v1")), v2 = index(t.getAttribute("v2")), v3 = index(t.getAttribute("v3"));
            if (!duplicateReported && v1 != null && v2 != null && v3 != null && (v1.equals(v2) || v1.equals(v3) || v2.equals(v3))) {
                issue("ER_200", key, "Triangle vertex indices must be distinct in " + key + ".");
                duplicateReported = true;
            }
            Element resource = ids.get(id(t.getAttribute("pid")));
            if (!propertyReported && resource != null && !isProperty(resource) && ModelSchema.supports(resource.getNamespaceURI())) {
                issue("ER_199", key, "Triangle pid " + t.getAttribute("pid") + " does not reference a material property group in " + key + ".");
                propertyReported = true;
            }
        }
    }
    private static boolean isProperty(Element e) {
        if (is(e, C, "basematerials")) return true;
        return M.equals(e.getNamespaceURI()) && java.util.Arrays.asList("colorgroup", "texture2dgroup", "compositematerials", "multiproperties").contains(e.getLocalName());
    }
    private Stack slices(Element e, String part) {
        Stack stack = new Stack();
        stack.key = part + " slicestack " + e.getAttribute("id");
        stack.bottom = e.hasAttribute("zbottom") ? number(e.getAttribute("zbottom")) : 0;
        double previous = stack.bottom;
        boolean hasSlices = false;
        for (Element slice : children(e)) {
            if (is(slice, S, "sliceref") && room()) {
                String path = slice.getAttribute("slicepath").trim();
                stack.refs.add(new Link(path.isEmpty() ? part : GeometryCheck.part(part, path), id(slice.getAttribute("slicestackid")), "slice"));
            }
            if (!is(slice, S, "slice")) continue;
            double top = number(slice.getAttribute("ztop"));
            if (Double.isNaN(stack.first)) stack.first = top;
            if (hasSlices && top <= previous) issue("ER_196", stack.key + ":height", "Successive slice heights must increase in " + stack.key + ".");
            hasSlices = true;
            previous = stack.last = top;
            Element vertices = child(slice, S, "vertices");
            long count = 0;
            if (vertices != null) for (Node n = vertices.getFirstChild(); n != null; n = n.getNextSibling()) if (is(n, S, "vertex")) count++;
            // No polygons means an empty contour; consumers ignore unused vertices (P_1517).
            for (Element polygon : children(slice)) if (is(polygon, S, "polygon")) {
                Long start = index(polygon.getAttribute("startv")), last = start;
                if (start != null && (start < 0 || start >= count)) issue("ER_194", stack.key, "Polygon startv is outside its slice vertex array in " + stack.key + ".");
                int segments = 0;
                for (Node n = polygon.getFirstChild(); n != null; n = n.getNextSibling()) if (is(n, S, "segment")) {
                    Long v = index(((Element)n).getAttribute("v2"));
                    if (v != null && (v < 0 || v >= count)) issue("ER_194", stack.key, "Segment v2 is outside its slice vertex array in " + stack.key + ".");
                    if (v != null && v.equals(last)) issue("ER_195", stack.key, "Adjacent polygon vertex indices must be distinct in " + stack.key + ".");
                    last = v;
                    segments++;
                }
                if (segments == 0) issue("ER_195", stack.key, "A polygon must contain at least one segment in " + stack.key + ".");
                if (start != null && last != null && !start.equals(last)) stack.open = true;
            }
        }
        if (hasSlices && !stack.refs.isEmpty()) issue("ER_196", stack.key, "A slicestack cannot mix slices and slicerefs: " + stack.key + ".");
        return stack;
    }
    private Stack stack(Link ref) {
        Model model = models.get(ref.part);
        return model == null ? null : model.stacks.get(ref.id);
    }
    private ObjectInfo object(Link ref) {
        Model model = models.get(ref.part);
        return model == null ? null : model.objects.get(ref.id);
    }
    void finish(String rootPart) {
        Model root = models.get(GeometryCheck.part("/", rootPart));
        // An absent summary may be an oversized or inaccessible encrypted model.
        // Required part roles/content types are checked by the package validator.
        if (root != null && root.build.isEmpty() && !root.toolpath)
            issue("ER_198", "root", "StartPart must reference a root model with a build: " + rootPart + ".");
        Set<Stack> overridden = new HashSet<>(), natural = new HashSet<>();
        for (Model model : models.values()) {
            for (Stack stack : model.stacks.values()) for (int i = 0; i < stack.refs.size(); i++) {
                Stack target = stack(stack.refs.get(i));
                if (target != null) (i == 0 ? natural : overridden).add(target);
            }
            for (ObjectInfo object : model.objects.values()) if (object.slices != null) {
                Stack target = stack(object.slices);
                if (target != null) natural.add(target);
            }
        }
        for (Map.Entry<String, Model> entry : models.entrySet()) {
            Model model = entry.getValue();
            for (Stack stack : model.stacks.values()) {
                if ((!overridden.contains(stack) || natural.contains(stack)) && stack.first < stack.bottom)
                    issue("ER_196", stack.key + ":height", "First slice ztop must not be below zbottom in " + stack.key + ".");
                double previous = stack.bottom;
                boolean firstReference = true;
                for (Link ref : stack.refs) {
                    Stack target = stack(ref);
                    if (target == null) continue; // Existing reference/decryption checks handle missing/opaque parts.
                    if (!target.refs.isEmpty()) issue("ER_196", stack.key, "A referenced slicestack must not contain slicerefs: " + stack.key + ".");
                    if (firstReference ? target.first < previous : target.first <= previous) issue("ER_196", stack.key + ":boundary", "Referenced slice heights must increase across slicestack boundaries in " + stack.key + ".");
                    firstReference = false;
                    previous = target.last; // At boundaries the preceding ztop overrides the next zbottom.
                    stack.open |= target.open;
                }
            }
            for (Link use : model.uses) {
                ObjectInfo target = object(use);
                if (target == null) continue;
                boolean invalid = !"model".equals(target.type) || !target.components.isEmpty();
                if (!"boolean base".equals(use.kind)) invalid |= !target.mesh || target.beam || target.displaced;
                if (invalid) issue("ER_197", entry.getKey() + use.kind + use.part + use.id,
                    "Invalid " + use.kind + " reference to " + use.part + " object " + use.id + ".");
            }
            for (ObjectInfo object : model.objects.values()) if (object.slices != null && ("model".equals(object.type) || "solidsupport".equals(object.type))) {
                Stack stack = stack(object.slices);
                if (stack != null) {
                    boolean open = stack.open;
                    for (Link ref : stack.refs) { Stack target = stack(ref); if (target != null) open |= target.open; }
                    if (open) issue("ER_195", stack.key + ":closure", "Polygons referenced by solid objects must be closed in " + stack.key + ".");
                }
            }
        }
        if (root != null) for (Link item : root.build) buildType(item, new HashSet<String>(), 0);
        finishConsumer(rootPart);
    }

    /** Accessible full-resolution representations, not rendering or Positive-fill evaluation. */
    void finishConsumer(String rootPart) {
        if (consumer != ValidationConsumer.PRINTER || rootPart == null) return;
        String part = GeometryCheck.part("/", rootPart);
        Model root = models.get(part);
        if (root == null) {
            if (unavailable.contains(part)) issue("ER_203", "print-root", "Printer cannot access the root model " + part + ".");
            return; // Oversized/uninspected models keep their existing incomplete-validation diagnostics.
        }
        if (root.build.isEmpty() && !root.toolpath) issue("ER_203", "print-empty", "Printer requires a root model with build items.");
        int[] work = {0};
        for (Link item : root.build) if (item.printable) {
            Availability status = available(item, new HashSet<String>(), 0, work);
            if (status == Availability.NONE) issue("ER_203", "print:" + item.part + "#" + item.id,
                "Printer requires an accessible, supported full-resolution representation for build object " + item.part + "#" + item.id + ".");
            if (status == Availability.UNKNOWN && reported.add("print-incomplete")) sink.issue("WR_029", true,
                "Printer representation validation incomplete: a model was not inspected or the traversal budget was exceeded.");
        }
    }
    private enum Availability { YES, NONE, UNKNOWN }
    private Availability available(Link link, Set<String> path, int depth, int[] work) {
        if (++work[0] > LIMIT || depth >= 128) return Availability.UNKNOWN;
        Model model = models.get(link.part);
        if (model == null) return unavailable.contains(link.part) ? Availability.NONE : Availability.UNKNOWN;
        if (model.unsupported) return Availability.NONE;
        ObjectInfo object = model.objects.get(link.id);
        if (object == null) return records > LIMIT ? Availability.UNKNOWN : Availability.NONE;
        if ("other".equals(object.type) && object.components.isEmpty()) return Availability.NONE;
        String key = link.part + "#" + link.id;
        if (!path.add(key)) return Availability.NONE;
        try {
            String resolution = link.resolution == null ? object.resolution : link.resolution;
            Availability primary = Availability.NONE;
            if ("fullres".equals(resolution)) {
                primary = object.payload || !object.components.isEmpty() ? Availability.YES : Availability.NONE;
                if (object.requiredSlices && object.slices != null) {
                    primary = all(primary, availableStack(object.slices, path, depth + 1, work));
                }
                for (Link dependency : object.components) if (primary != Availability.NONE) primary = all(primary, available(dependency, path, depth + 1, work));
                for (Link dependency : object.operands) if (primary != Availability.NONE) primary = all(primary, available(dependency, path, depth + 1, work));
                if (primary == Availability.YES) return primary;
            }
            boolean unknown = primary == Availability.UNKNOWN;
            // An alternative's resolution overrides the target object's modelresolution.
            for (Link alternative : object.alternatives) if ("fullres".equals(alternative.resolution)) {
                Availability status = available(alternative, path, depth + 1, work);
                if (status == Availability.YES) return status;
                unknown |= status == Availability.UNKNOWN;
            }
            return unknown ? Availability.UNKNOWN : Availability.NONE;
        } finally { path.remove(key); }
    }
    private static Availability all(Availability first, Availability next) {
        if (first == Availability.NONE || next == Availability.NONE) return Availability.NONE;
        return first == Availability.UNKNOWN || next == Availability.UNKNOWN ? Availability.UNKNOWN : Availability.YES;
    }
    private Availability availableStack(Link link, Set<String> path, int depth, int[] work) {
        if (++work[0] > LIMIT || depth >= 128) return Availability.UNKNOWN;
        Model model = models.get(link.part);
        if (model == null) return unavailable.contains(link.part) ? Availability.NONE : Availability.UNKNOWN;
        Stack stack = model.stacks.get(link.id);
        if (model.unsupported || stack == null) return records > LIMIT ? Availability.UNKNOWN : Availability.NONE;
        String key = "slice:" + link.part + "#" + link.id;
        if (!path.add(key)) return Availability.NONE;
        try {
            Availability result = Availability.YES;
            for (Link ref : stack.refs) if (result != Availability.NONE) result = all(result, availableStack(ref, path, depth + 1, work));
            return result;
        } finally { path.remove(key); }
    }
    private void buildType(Link ref, Set<String> path, int depth) {
        if (++visits > LIMIT || depth >= 128) {
            if (reported.add("traversal-limit")) sink.issue("WR_029", true, "Build type validation incomplete: traversal budget exceeded.");
            return;
        }
        String key = ref.part + "#" + ref.id;
        if (!path.add(key)) return; // Other reference checks diagnose cycles.
        ObjectInfo target = object(ref);
        if (target != null) {
            if (target.components.isEmpty() && "other".equals(target.type)) issue("ER_197", "build:" + key, "Build items must not reference objects of type other: " + key + ".");
            for (Link component : target.components) buildType(component, path, depth + 1);
        }
        path.remove(key);
    }
}
