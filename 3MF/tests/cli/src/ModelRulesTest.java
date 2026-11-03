package com.qualitylogic.x3mf;

import java.util.ArrayList;
import java.util.List;

/** Small independent controls for semantic rules, including allowed boundary cases. */
public final class ModelRulesTest {
    private static final String C = GeometryCheck.CORE, S = GeometryCheck.SLICE;
    private static final String NS = " xmlns='" + C + "' xmlns:s='" + S + "' xmlns:q='" + GeometryCheck.PROD
        + "' xmlns:b='" + FileStructureCheck.bns + "' xmlns:bo='http://schemas.3mf.io/3dmanufacturing/booleanoperations/2023/07'"
        + " xmlns:d='" + GeometryCheck.DISP + "'";
    private static int count;
    private static String model(String metadata, String resources, String item) {
        return "<model" + NS + ">" + metadata + "<resources>" + resources + "</resources><build>" + item + "</build></model>";
    }
    private static String obj(String extra, String content) { return "<object id='2' " + extra + ">" + content + "</object>"; }
    private static final String ITEM = "<item objectid='2'/>";
    private static final String V = "<s:vertices><s:vertex x='0' y='0'/><s:vertex x='1' y='0'/><s:vertex x='1' y='1'/></s:vertices>";
    private static String slice(String top, String vertices, String end) {
        return "<s:slice ztop='" + top + "'>" + vertices + "<s:polygon startv='0'><s:segment v2='1'/><s:segment v2='" + end + "'/></s:polygon></s:slice>";
    }
    private static String sliced(String type, String slices) {
        return model("", "<s:slicestack id='1' zbottom='0'>" + slices + "</s:slicestack>" + obj("type='" + type + "' s:slicestackid='1'", "<mesh/>"), ITEM);
    }
    private static void check(String xml, String expected, String... otherParts) throws Exception {
        List<String> errors = new ArrayList<>();
        ModelRules rules = new ModelRules((code, warning, text) -> { if (warning) throw new AssertionError(text); errors.add(code); });
        rules.addModel(ModelSchema.parse(xml), "/3D/root.model");
        for (int i = 0; i < otherParts.length; i++) rules.addModel(ModelSchema.parse(otherParts[i]), "/2D/part" + i + ".model");
        rules.finish("/3D/root.model");
        if (expected == null ? !errors.isEmpty() : !errors.contains(expected)) throw new AssertionError("Expected " + expected + "; got " + errors);
        count++;
    }
    public static void main(String[] args) throws Exception {
        check(model("", obj("", "<mesh/>"), ITEM), null);
        check(model("", obj("", "<mesh/>").replace("id='2'", "id='02'"), ITEM), null);
        check(model("<metadata name='Title'>a</metadata><metadata name='Title'>b</metadata>", obj("", "<mesh/>"), ITEM), "ER_193");
        check(model("<metadata name='Title'>a</metadata>", obj("", "<mesh/><metadatagroup><metadata name='Title'>b</metadata></metadatagroup>"), ITEM), null);
        check(model("<metadata xmlns:x='urn:v' name='x:a'>a</metadata><metadata xmlns:y='urn:v' name='y:a'>b</metadata>", obj("", "<mesh/>"), ITEM), "ER_193");
        check(sliced("model", slice("1", V, "0")), null);
        check(sliced("support", slice("1", V, "2")), null);
        check(sliced("model", slice("1", V, "2")), "ER_195");
        check(sliced("solidsupport", slice("1", V, "2")), "ER_195");
        check(sliced("model", slice("1", V, "9")), "ER_194");
        check(sliced("model", slice("1", "", "0")), "ER_194");
        check(sliced("model", slice("1", V, "1")), "ER_195");
        check(sliced("model", slice("-0.1", V, "0")), "ER_196");
        check(sliced("model", slice("0", V, "0")), null); // The first slice may be exactly at zbottom.
        check(sliced("model", slice("1", V, "0") + slice("1", V, "0")), "ER_196");
        // Empty slices are allowed; coincident coordinates with distinct indices are allowed.
        check(sliced("model", "<s:slice ztop='1'/>"), null);
        check(sliced("model", "<s:slice ztop='1'>" + V + "</s:slice>"), null);
        check(sliced("model", slice("1", V.replace("x='1'", "x='0'").replace("y='1'", "y='0'"), "0")), null);
        String triangle = "<mesh><triangles><triangle v1='0' v2='1' v3='1'/></triangles></mesh>";
        check(model("", obj("", triangle), ITEM), "ER_200");
        check(model("", "<d:disp2dgroup id='6'/>" + obj("", triangle.replace("v3='1'", "v3='2' pid='6'")), ITEM), "ER_199");
        check(model("", "<d:disp2dgroup id='6'/>" + obj("", triangle.replace("v3='1'", "v3='2' pid='006'")), ITEM), "ER_199");
        check(model("", obj("type='other'", "<mesh/>"), ITEM), "ER_197");
        check(model("", "<object id='1' type='other'><mesh/></object>" + obj("", "<components><component objectid='1'/></components>"), ITEM), "ER_197");
        check(model("", "<object id='1'><mesh/></object>" + obj("type='other'", "<components><component objectid='1'/></components>"), ITEM), null);
        String lattice = "<object id='1'><mesh><b:beamlattice/></mesh></object>";
        check(model("", lattice + obj("", "<mesh><b:beamlattice representationmesh='1'/></mesh>"), ITEM), "ER_197");
        check(model("", lattice + obj("", "<bo:booleanshape objectid='1'><bo:boolean objectid='1'/></bo:booleanshape>"), ITEM), "ER_197");
        check(model("", lattice + obj("", "<bo:booleanshape objectid='1'/>"), ITEM), null); // A Boolean base can be an extension shape.
        check(model("", obj("", "<mesh/>"), ""), "ER_198");
        String refs = "<s:slicestack id='1'><s:sliceref slicepath='/2D/part0.model' slicestackid='3'/><s:sliceref slicepath='/2D/part1.model' slicestackid='4'/></s:slicestack>" + obj("s:slicestackid='1'", "<mesh/>");
        String a = model("", "<s:slicestack id='3'>" + slice("1", V, "0") + "</s:slicestack>", "");
        String b = model("", "<s:slicestack id='4' zbottom='0.9'>" + slice("2", V, "0") + "</s:slicestack>", "");
        check(model("", refs, ITEM), null, a, b); // zbottom disagreement alone is not an error.
        check(model("", refs, ITEM), null, a, b.replace("zbottom='0.9'", "zbottom='3'")); // Ignore the later stack's zbottom.
        check(model("", refs, ITEM), "ER_196", a, b.replace("ztop='2'", "ztop='0.95'"));
        check(model("", refs, ITEM), "ER_196", a.replace(slice("1", V, "0"), "<s:sliceref slicepath='/2D/part1.model' slicestackid='4'/>"), b);
        check(model("", refs, ITEM), "ER_195", a.replace("v2='0'", "v2='2'"), b);
        check(model("", obj("", "<mesh/>"), ITEM).replace("<model", "<model requiredextensions='mystery' xmlns:mystery='urn:unsupported'"), "ER_192");
        check("<unexpected/>", "ER_198");
        List<String> incompleteErrors = new ArrayList<>();
        ModelRules incomplete = new ModelRules((code, warning, text) -> incompleteErrors.add(code));
        incomplete.finish("/3D/opaque.model");
        if (!incompleteErrors.isEmpty()) throw new AssertionError("Opaque models are not proven to have an invalid root");
        count++;
        FileStructureCheck failure = new FileStructureCheck();
        failure.unexpectedFailure(new IllegalStateException("<&>"));
        ModelSchema.parse("<issues>" + failure.fxmlErrorReturn + "</issues>");
        if (!failure.fxmlErrorReturn.contains("ER_077") || !failure.fErrorReturn.contains("&lt;&amp;&gt;"))
            throw new AssertionError("Unexpected structural exceptions must produce escaped errors in both formats");
        count++;
        System.out.println("Passed " + count + " semantic controls");
    }
}
