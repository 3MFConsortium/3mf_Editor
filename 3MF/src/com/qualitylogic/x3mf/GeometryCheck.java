package com.qualitylogic.x3mf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** Bounded checks on base meshes, not a renderer or a Positive-fill evaluator. */
final class GeometryCheck {
    static final String CORE = "http://schemas.microsoft.com/3dmanufacturing/core/2015/02";
    static final String SLICE = "http://schemas.microsoft.com/3dmanufacturing/slice/2015/07";
    static final String PROD = "http://schemas.microsoft.com/3dmanufacturing/production/2015/06";
    static final String DISP = "http://schemas.3mf.io/3dmanufacturing/displacement/2023/10";
    interface Sink { void issue(String code, boolean warning, String message); }
    private final Sink sink;
    private final int budget;
    private int elements;
    private int visits;
    private final Map<String, Model> models = new LinkedHashMap<>();
    private final Set<String> reported = new HashSet<>();
    private final Set<Shape> visited = new HashSet<>();
    GeometryCheck(Sink sink) { this(sink, 500000); }
    GeometryCheck(Sink sink, int budget) { this.sink = sink; this.budget = budget; }

    private void issue(String code, boolean warning, String key, String text) {
        if (reported.add(code + "|" + key)) sink.issue(code, warning, text);
    }
    private void incomplete(String key, String text) {
        issue("WR_029", true, key, "Geometry validation incomplete: " + text);
    }
    private static boolean is(Element e, String ns, String name) {
        return name.equals(e.getLocalName()) && ns.equals(e.getNamespaceURI());
    }
    private static List<Element> children(Element parent) {
        List<Element> result = new ArrayList<>();
        if (parent != null) for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling())
            if (n instanceof Element) result.add((Element)n);
        return result;
    }
    private static Element child(Element parent, String ns, String name) {
        for (Element e : children(parent)) if (is(e, ns, name)) return e;
        return null;
    }
    static String part(String base, String path) {
        try {
            URI resolved = new URI(base).resolve(new URI(path.replace('\\', '/'))).normalize();
            if (resolved.isAbsolute() || resolved.getRawAuthority() != null || resolved.getQuery() != null
                    || resolved.getFragment() != null) return null;
            String p = resolved.getPath();
            return p != null && p.startsWith("/") && !p.contains("/../") ? p : null;
        } catch (Exception ex) { return null; } // Existing reference/schema checks diagnose invalid URIs.
    }
    private static final class Model {
        final Map<String, Shape> objects = new LinkedHashMap<>();
        final List<Reference> build = new ArrayList<>();
        double unit;
    }
    private static final class Reference {
        String part, id;
        double[] matrix;
        boolean printable;
    }
    private static final class Shape {
        String key;
        boolean solid, sliced, displaced;
        double[] points;
        BitSet used;
        boolean nonManifold, inconsistentOrientation, inverted;
        final List<Reference> components = new ArrayList<>();
    }
    void addModel(Document doc, String name, ZipFile zip, Function<String, String> contentType) throws IOException {
        String nameKey = part("/", name);
        if (nameKey == null || models.containsKey(nameKey)) return;
        Element root = doc.getDocumentElement();
        if (!is(root, CORE, "model")) return;
        Model model = new Model();
        model.unit = unit(root.getAttribute("unit"));
        models.put(nameKey, model);
        boolean requiredSlice = false;
        for (String prefix : root.getAttribute("requiredextensions").trim().split("\\s+"))
            if (SLICE.equals(root.lookupNamespaceURI(prefix))) requiredSlice = true;
        for (Element obj : children(child(root, CORE, "resources"))) {
            if (is(obj, DISP, "displacement2d")) checkImage(obj, nameKey, zip, contentType);
            if (!is(obj, CORE, "object")) continue;
            if (++elements > budget) { incomplete("objects", "object budget exceeded in " + nameKey + "."); break; }
            Shape shape = new Shape();
            shape.key = nameKey + " object " + obj.getAttribute("id");
            String type = obj.getAttribute("type");
            shape.solid = type.isEmpty() || type.equals("model") || type.equals("solidsupport");
            shape.sliced = requiredSlice && obj.hasAttributeNS(SLICE, "slicestackid")
                    && "lowres".equals(obj.getAttributeNS(SLICE, "meshresolution"));
            Element mesh = child(obj, CORE, "mesh");
            if (mesh == null) { mesh = child(obj, DISP, "displacementmesh"); shape.displaced = mesh != null; }
            if (mesh != null) readMesh(mesh, shape);
            for (Element component : children(child(obj, CORE, "components")))
                if (is(component, CORE, "component")) {
                    if (++elements > budget) { incomplete("references", "component reference budget exceeded."); break; }
                    shape.components.add(reference(component, nameKey));
                }
            model.objects.put(obj.getAttribute("id"), shape);
        }
        for (Element item : children(child(root, CORE, "build")))
            if (is(item, CORE, "item")) {
                if (++elements > budget) { incomplete("references", "build reference budget exceeded."); break; }
                model.build.add(reference(item, nameKey));
            }
    }
    private Reference reference(Element e, String base) {
        Reference ref = new Reference();
        String path = e.getAttributeNS(PROD, "path").trim();
        ref.part = path.isEmpty() ? base : part(base, path);
        ref.id = e.getAttribute("objectid").trim();
        ref.printable = !"false".equals(e.getAttribute("printable")) && !"0".equals(e.getAttribute("printable"));
        ref.matrix = matrix(e.getAttribute("transform"));
        if (ref.matrix != null && singular(ref.matrix)) issue("WR_027", true, base + e.getTagName() + ref.id + e.getAttribute("transform"),
                "Singular or nearly singular transform on " + e.getTagName() + " referencing object " + ref.id + " in " + base + ".");
        return ref;
    }
    private void checkImage(Element resource, String base, ZipFile zip, Function<String, String> contentType) throws IOException {
        String target = part(base, resource.getAttribute("path").trim());
        if (target == null) return;
        ZipEntry entry = zip.getEntry(target.substring(1));
        if (entry == null) return; // Existing relationship checks report missing targets.
        byte[] header = new byte[8];
        try (InputStream in = zip.getInputStream(entry)) {
            // Read the fixed signature without requiring a post-Java-8 stream API.
            for (int i = 0; i < header.length; i++) {
                int value = in.read();
                if (value < 0) { header = java.util.Arrays.copyOf(header, i); break; }
                header[i] = (byte)value;
            }
        }
        if (header.length >= 5 && header[0] == '%' && header[1] == '3' && header[2] == 'M' && header[3] == 'c' && header[4] == 'F') {
            incomplete(target, "encrypted displacement image " + target + " was not inspected.");
            return;
        }
        byte[] png = {(byte)137, 80, 78, 71, 13, 10, 26, 10};
        if (!java.util.Arrays.equals(header, png) || !"image/png".equals(contentType.apply(target)))
            issue("ER_181", false, target, "Displacement image " + target + " must contain PNG data and be declared image/png.");
    }
    private void readMesh(Element mesh, Shape shape) {
        String ns = shape.displaced ? DISP : CORE;
        List<Element> vertices = children(child(mesh, ns, "vertices"));
        List<Element> triangles = children(child(mesh, ns, "triangles"));
        if ((long)elements + vertices.size() + triangles.size() > budget) {
            incomplete(shape.key, "base mesh budget exceeded for " + shape.key + "."); return;
        }
        elements += vertices.size() + triangles.size();
        double[] points = new double[vertices.size() * 3];
        try {
            for (int i = 0; i < vertices.size(); i++) for (int axis = 0; axis < 3; axis++) {
                double value = Double.parseDouble(vertices.get(i).getAttribute(new String[]{"x", "y", "z"}[axis]));
                if (!Double.isFinite(value)) return;
                points[i * 3 + axis] = value;
            }
            BitSet used = new BitSet(vertices.size());
            int[] indices = new int[triangles.size() * 3];
            for (int i = 0; i < triangles.size(); i++) for (int corner = 0; corner < 3; corner++) {
                int index = Integer.parseInt(triangles.get(i).getAttribute("v" + (corner + 1)));
                if (index < 0 || index >= vertices.size()) return; // Existing index checks diagnose malformed meshes.
                indices[i * 3 + corner] = index;
                used.set(index);
            }
            Map<Long, int[]> edges = new HashMap<>();
            // Volume around a nearby reference point avoids cancellation after large translations.
            double volume = 0, compensation = 0;
            int first = used.nextSetBit(0);
            double[] origin = first < 0 ? new double[3] : java.util.Arrays.copyOfRange(points, first*3, first*3+3);
            double scale = 0;
            for (int i = first; i >= 0; i = used.nextSetBit(i+1)) for (int axis = 0; axis < 3; axis++)
                scale = Math.max(scale, Math.abs(points[3*i+axis] - origin[axis]));
            for (int i = 0; i < indices.length; i += 3) {
                int a = indices[i], b = indices[i+1], c = indices[i+2];
                edge(edges, a, b); edge(edges, b, c); edge(edges, c, a);
                if (scale > 0) {
                    double[] u = relative(points, a, origin, scale), v = relative(points, b, origin, scale), w = relative(points, c, origin, scale);
                    double term = (u[0]*(v[1]*w[2]-v[2]*w[1]) + u[1]*(v[2]*w[0]-v[0]*w[2]) + u[2]*(v[0]*w[1]-v[1]*w[0])) / 6;
                    double corrected = term - compensation, sum = volume + corrected;
                    compensation = (sum - volume) - corrected; volume = sum;
                }
            }
            shape.points = points; shape.used = used;
            for (int[] counts : edges.values()) {
                if (counts[0] != 2) shape.nonManifold = true;
                else if (counts[1] != 0) shape.inconsistentOrientation = true;
            }
            // A negative aggregate can include valid nested shells or self-intersections.
            // Only diagnose an inverted shell for a single connected, closed surface.
            if (!shape.nonManifold && !shape.inconsistentOrientation && volume < -1e-12 && connected(edges, used)) shape.inverted = true;
        } catch (NumberFormatException ex) { /* Schema/index checks diagnose malformed meshes. */ }
    }
    private static double[] relative(double[] p, int i, double[] origin, double scale) {
        return new double[]{(p[3*i]-origin[0])/scale, (p[3*i+1]-origin[1])/scale, (p[3*i+2]-origin[2])/scale};
    }
    private static void edge(Map<Long, int[]> edges, int a, int b) {
        long key = ((long)Math.min(a,b) << 32) | (Math.max(a,b) & 0xffffffffL);
        int[] counts = edges.computeIfAbsent(key, k -> new int[2]);
        counts[0]++; counts[1] += a < b ? 1 : -1;
    }
    private static boolean connected(Map<Long, int[]> edges, BitSet used) {
        if (used.isEmpty()) return false;
        int[] parents = new int[used.length()];
        for (int i = 0; i < parents.length; i++) parents[i] = i;
        for (long key : edges.keySet()) {
            int a = find(parents, (int)(key >>> 32)), b = find(parents, (int)key);
            parents[a] = b;
        }
        int first = find(parents, used.nextSetBit(0));
        for (int i = used.nextSetBit(0); i >= 0; i = used.nextSetBit(i + 1)) if (find(parents, i) != first) return false;
        return true;
    }
    private static int find(int[] parents, int n) {
        while (n != parents[n]) { parents[n] = parents[parents[n]]; n = parents[n]; }
        return n;
    }
    void finish(String rootPart) {
        Model root = models.get(part("/", rootPart));
        if (root != null) for (Reference ref : root.build) if (ref.printable)
            walk(ref, identity(), root.unit, false, new HashSet<>(), 0);
        // Unused resources still need topology validation, without a fabricated build placement.
        for (Model m : models.values()) for (Shape s : m.objects.values()) if (!visited.contains(s)) topology(s, s.sliced);
    }
    private void topology(Shape shape, boolean sliced) {
        if (!shape.solid || sliced) return;
        if (shape.nonManifold) issue("ER_178", false, shape.key, "Non-manifold indexed edges in " + shape.key + "; each edge must belong to exactly two triangles.");
        if (shape.inconsistentOrientation) issue("ER_179", false, shape.key, "Inconsistent adjacent triangle orientation in " + shape.key + ".");
        if (shape.inverted) issue("ER_180", false, shape.key, "Inverted closed base mesh (negative signed volume) in " + shape.key + ".");
    }
    private void walk(Reference ref, double[] parent, double parentUnit, boolean sliced, Set<String> path, int depth) {
        if (++visits > 100000 || depth > 128) { incomplete("traversal", "component traversal budget exceeded."); return; }
        Model model = models.get(ref.part);
        if (model == null || ref.matrix == null) return;
        Shape shape = model.objects.get(ref.id);
        if (shape == null) return;
        if (!path.add(shape.key)) { incomplete("cycle", "cyclic component graph."); return; }
        visited.add(shape);
        boolean useSlices = sliced || shape.sliced;
        topology(shape, useSlices);
        double[] local = identity();
        local[0] = local[4] = local[8] = model.unit / parentUnit;
        double[] world = multiply(multiply(local, ref.matrix), parent);
        if (singular(world)) issue("WR_027", true, "composed:" + shape.key, "Singular or nearly singular composed transform for " + shape.key + ".");
        if (!useSlices && !shape.displaced && shape.points != null) {
            double[] min = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
            double[] magnitude = {1, 1, 1};
            for (int i = shape.used.nextSetBit(0); i >= 0; i = shape.used.nextSetBit(i + 1)) {
                for (int j = 0; j < 3; j++) {
                    double value = shape.points[3*i]*world[j] + shape.points[3*i+1]*world[3+j] + shape.points[3*i+2]*world[6+j] + world[9+j];
                    min[j] = Math.min(min[j], value); magnitude[j] = Math.max(magnitude[j], Math.abs(value));
                }
            }
            if (!Double.isFinite(magnitude[0]) || !Double.isFinite(magnitude[1]) || !Double.isFinite(magnitude[2])) incomplete("numeric:" + shape.key, "transformed coordinates overflow for " + shape.key + ".");
            else if (min[0] < -1e-9*magnitude[0] || min[1] < -1e-9*magnitude[1] || min[2] < -1e-9*magnitude[2])
                issue("WR_028", true, shape.key, "Transformed base mesh extends outside the positive octant: " + shape.key
                        + " (minimum coordinates " + min[0] + ", " + min[1] + ", " + min[2] + "). Printer bed bounds were not evaluated.");
        }
        for (Reference component : shape.components) walk(component, world, model.unit, useSlices, path, depth + 1);
        path.remove(shape.key);
    }
    private static double unit(String unit) {
        switch (unit) {
        case "micron": return .001; case "centimeter": return 10; case "inch": return 25.4;
        case "foot": return 304.8; case "meter": return 1000; default: return 1;
        }
    }
    private static double[] identity() { return new double[]{1,0,0,0,1,0,0,0,1,0,0,0}; }
    private static double[] matrix(String value) {
        if (value.trim().isEmpty()) return identity();
        String[] fields = value.trim().split("\\s+");
        if (fields.length != 12) return null;
        double[] result = new double[12];
        try { for (int i = 0; i < 12; i++) { result[i] = Double.parseDouble(fields[i]); if (!Double.isFinite(result[i])) return null; } }
        catch (NumberFormatException ex) { return null; }
        return result;
    }
    private static boolean singular(double[] m) {
        double scale = 0;
        for (int i = 0; i < 9; i++) scale = Math.max(scale, Math.abs(m[i]));
        if (scale == 0 || !Double.isFinite(scale)) return true;
        double[] n = new double[9]; for (int i = 0; i < 9; i++) n[i] = m[i]/scale;
        double det = n[0]*(n[4]*n[8]-n[5]*n[7])-n[1]*(n[3]*n[8]-n[5]*n[6])+n[2]*(n[3]*n[7]-n[4]*n[6]);
        return Math.abs(det) < 1e-12;
    }
    private static double[] multiply(double[] a, double[] b) {
        double[] result = new double[12];
        for (int row = 0; row < 4; row++) for (int col = 0; col < 3; col++) {
            for (int k = 0; k < 3; k++) result[row*3+col] += a[row*3+k] * b[k*3+col];
            if (row == 3) result[row*3+col] += b[9+col];
        }
        return result;
    }
}
