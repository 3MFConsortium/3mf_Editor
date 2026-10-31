package com.qualitylogic.x3mf;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;

/** Independent tetrahedron fixtures for topology, transforms, exceptions and limits. */
public final class GeometryCheckTest {
    private static int checks;
    private static final String IDENTITY = "1 0 0 0 1 0 0 0 1 0 0 0";
    private static String vertex(double x, double y, double z) {
        return "<vertex x='" + x + "' y='" + y + "' z='" + z + "'/>";
    }
    private static String tetra(boolean inverted, boolean reversedFace, boolean open, int offset) {
        int[][] faces = {{0,2,1},{0,1,3},{0,3,2},{1,2,3}};
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < faces.length - (open ? 1 : 0); i++) {
            int a = faces[i][0], b = faces[i][1], c = faces[i][2];
            if (inverted ^ (reversedFace && i == 0)) { int tmp = b; b = c; c = tmp; }
            result.append("<triangle v1='").append(a+offset).append("' v2='").append(b+offset)
                .append("' v3='").append(c+offset).append("'/>");
        }
        return result.toString();
    }
    private static String vertices(double offset, double scale) {
        return vertex(offset,offset,offset) + vertex(offset+scale,offset,offset)
            + vertex(offset,offset+scale,offset) + vertex(offset,offset,offset+scale);
    }
    private static String mesh(String vertices, String triangles) {
        return "<mesh><vertices>" + vertices + "</vertices><triangles>" + triangles + "</triangles></mesh>";
    }
    private static String shape(String attributes, String mesh) { return "<object id='1' " + attributes + ">" + mesh + "</object>"; }
    private static String item(String id, String transform) { return "<item objectid='" + id + "' transform='" + transform + "'/>"; }
    private static String model(String resources, String build, String attributes) {
        return "<model xmlns='" + GeometryCheck.CORE + "' xmlns:s='" + GeometryCheck.SLICE
            + "' xmlns:alias='" + GeometryCheck.PROD + "' xmlns:d='" + GeometryCheck.DISP + "' " + attributes + ">"
            + "<resources>" + resources + "</resources><build>" + build + "</build></model>";
    }
    private static void add(GeometryCheck check, String part, String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        check.addModel(factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml))), part, null, p -> "image/png");
    }
    private static List<String> run(String resources, String build, String attributes) throws Exception {
        List<String> issues = new ArrayList<>();
        GeometryCheck check = new GeometryCheck((code, warning, text) -> issues.add(code));
        add(check, "/3D/main.model", model(resources, build, attributes));
        check.finish("/3D/main.model");
        return issues;
    }
    private static void expect(String name, List<String> actual, String... expected) {
        if (!new java.util.HashSet<>(actual).equals(new java.util.HashSet<>(List.of(expected))))
            throw new AssertionError(name + ": expected " + List.of(expected) + ", got " + actual);
        checks++;
    }
    public static void main(String[] args) throws Exception {
        String positive = mesh(vertices(0,1), tetra(false,false,false,0));
        String negative = mesh(vertices(0,1), tetra(true,false,false,0));
        expect("closed positive", run(shape("",positive),item("1",IDENTITY),""));
        expect("inverted shell", run(shape("",negative),item("1",IDENTITY),""),"ER_180");
        expect("inward face", run(shape("",mesh(vertices(0,1),tetra(false,true,false,0))),item("1",IDENTITY),""),"ER_179");
        expect("open surface", run(shape("",mesh(vertices(0,1),tetra(false,false,true,0))),item("1",IDENTITY),""),"ER_178");
        for (String type : List.of("support","surface","other")) {
            expect("exempt type " + type,run(shape("type='"+type+"'",negative),"",""));
            expect("exempt open " + type,run(shape("type='"+type+"'",mesh(vertices(0,1),tetra(false,false,true,0))),"",""));
        }
        expect("solid support is checked",run(shape("type='solidsupport'",negative),item("1",IDENTITY),""),"ER_180");
        expect("mirror is valid", run(shape("",positive),item("1","-1 0 0 0 1 0 0 0 1 2 0 0"),""));
        expect("mirror does not repair inverted shell",run(shape("",negative),item("1","-1 0 0 0 1 0 0 0 1 2 0 0"),""),"ER_180");
        expect("singular",run(shape("",positive),item("1","1 0 0 0 1 0 0 0 0 0 0 0"),""),"WR_027");
        expect("nearly singular",run(shape("",positive),item("1","1 0 0 0 1 0 0 0 1e-15 0 0 0"),""),"WR_027");
        expect("small uniform scale",run(shape("",positive),item("1","1e-20 0 0 0 1e-20 0 0 0 1e-20 0 0 0"),""));
        expect("negative placement",run(shape("",positive),item("1","1 0 0 0 1 0 0 0 1 -2 0 0"),""),"WR_028");
        expect("large Z does not hide negative X",run(shape("",positive),item("1","1 0 0 0 1 0 0 0 1 -2 0 1e12"),""),"WR_028");
        expect("nonprintable item",run(shape("",positive),"<item objectid='1' printable='false' transform='1 0 0 0 1 0 0 0 1 -2 0 0'/>",""));
        expect("unused vertex is not geometry",run(shape("",mesh(vertices(0,1)+vertex(-100,0,0),tetra(false,false,false,0))),item("1",IDENTITY),""));
        expect("unused far vertex does not hide inverted shell",run(shape("",mesh(vertex(1e20,0,0)+vertices(0,1),tetra(true,false,false,1))),item("1",IDENTITY),""),"ER_180");
        expect("translated local coordinates",run(shape("",mesh(vertices(-10,1),tetra(false,false,false,0))),item("1","1 0 0 0 1 0 0 0 1 10 10 10"),""));
        expect("large coordinate volume stability",run(shape("",mesh(vertices(1e12,1),tetra(true,false,false,0))),item("1",IDENTITY),""),"ER_180");
        String hollow = mesh(vertices(0,1)+vertices(.1,.1),tetra(false,false,false,0)+tetra(true,false,false,4));
        expect("nested cavity preserves positive fill",run(shape("",hollow),item("1",IDENTITY),""));
        String wrapper = "<object id='2'><components><component objectid='1' transform='1 0 0 0 1 0 0 0 1 -4 0 0'/></components></object>";
        expect("composed translation cancels",run(shape("",positive)+wrapper,item("2","1 0 0 0 1 0 0 0 1 5 0 0"),""));
        expect("composed translation negative",run(shape("",positive)+wrapper,item("2",IDENTITY),""),"WR_028");
        expect("every instance is examined",run(shape("",positive),item("1",IDENTITY)+item("1","1 0 0 0 1 0 0 0 1 -2 0 0"),""),"WR_028");
        // A component's translation must rotate with its enclosing build item (row-vector order).
        String rotatedWrapper = wrapper.replace("-4 0 0", "0 4 0");
        expect("rotation composition order",run(shape("",positive)+rotatedWrapper,item("2","0 1 0 -1 0 0 0 0 1 6 0 0"),""));
        String sliceAttributes = "s:slicestackid='7' s:meshresolution='lowres'";
        expect("authoritative slice",run(shape(sliceAttributes,negative),item("1",IDENTITY),"requiredextensions='s'"));
        expect("slice namespace alone is insufficient",run(shape(sliceAttributes,negative),item("1",IDENTITY),""),"ER_180");
        expect("fullres mesh still checked",run(shape("s:slicestackid='7'",negative),item("1",IDENTITY),"requiredextensions='s'"),"ER_180");
        String slicedWrapper = wrapper.replace("<object id='2'>","<object id='2' "+sliceAttributes+">");
        expect("ancestor slices override component mesh",run(shape("",negative)+slicedWrapper,item("2",IDENTITY),"requiredextensions='s'"));
        expect("standalone use is still checked",run(shape("",negative)+slicedWrapper,item("2",IDENTITY)+item("1",IDENTITY),"requiredextensions='s'"),"ER_180");
        String displacement = positive.replace("<mesh>","<d:displacementmesh>").replace("</mesh>","</d:displacementmesh>")
            .replace("<vertices>","<d:vertices>").replace("</vertices>","</d:vertices>")
            .replace("<vertex ","<d:vertex ").replace("<triangles>","<d:triangles>").replace("</triangles>","</d:triangles>").replace("<triangle ","<d:triangle ");
        expect("displacement base placement is not final placement",run(shape("",displacement),item("1","1 0 0 0 1 0 0 0 1 -100 0 0"),""));
        expect("displacement shared topology",run(shape("",displacement.replace("v1='0' v2='2' v3='1'","v1='0' v2='1' v3='2'")),item("1",IDENTITY),""),"ER_179");
        List<String> cross = new ArrayList<>();
        GeometryCheck checker = new GeometryCheck((code,w,text)->cross.add(code));
        add(checker,"/3D/main.model",model("<object id='2'><components><component objectid='1' alias:path='/3D/child.model' transform='1 0 0 0 1 0 0 0 1 2 0 0'/></components></object>",item("2",IDENTITY),""));
        add(checker,"/3D/child.model",model(shape("",positive),item("1","1 0 0 0 1 0 0 0 1 -100 0 0"),""));
        checker.finish("/3D/main.model");
        expect("cross-part alias and root build selection",cross);
        cross.clear();
        checker = new GeometryCheck((code,w,text)->cross.add(code));
        add(checker,"/3D/main.model",model("<object id='2'><components><component objectid='1' alias:path='/3D/child.model' transform='1 0 0 0 1 0 0 0 1 -2 0 0'/></components></object>",item("2",IDENTITY),"unit='centimeter'"));
        add(checker,"/3D/child.model",model(shape("",mesh(vertices(10,1),tetra(false,false,false,0))),"","unit='millimeter'"));
        checker.finish("/3D/main.model"); expect("cross-part units",cross,"WR_028");
        cross.clear();
        checker = new GeometryCheck((code,w,text)->cross.add(code), 1);
        add(checker,"/3D/main.model",model(shape("",positive),item("1",IDENTITY),"")); checker.finish("/3D/main.model");
        expect("bounded mesh memory",cross,"WR_029");
        expect("cycle bounded",run("<object id='1'><components><component objectid='1'/></components></object>",item("1",IDENTITY),""),"WR_029");
        // Signature and declared content type are checked independently; no bitmap decoding/allocation.
        Path archive = Path.of(args[0],"image-signatures.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(archive))) {
            out.putNextEntry(new ZipEntry("image.png")); out.write(new byte[]{(byte)137,80,78,71,13,10,26,10}); out.closeEntry();
            out.putNextEntry(new ZipEntry("image.jpg")); out.write(new byte[]{(byte)255,(byte)216,(byte)255,0,0,0,0,0}); out.closeEntry();
        }
        for (boolean png : new boolean[]{true,false}) for (boolean declaredPng : new boolean[]{true,false}) {
            cross.clear(); checker = new GeometryCheck((code,w,text)->cross.add(code));
            String xml = model("<d:displacement2d id='9' path='/image."+(png?"png":"jpg")+"'/>","","");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); factory.setNamespaceAware(true);
            try (ZipFile zip = new ZipFile(archive.toFile())) {
                checker.addModel(factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml))),"/3D/main.model",zip,p->declaredPng?"image/png":"image/jpeg");
            }
            expect("signature="+png+", declaredPNG="+declaredPng,cross,(png&&declaredPng)?new String[]{}:new String[]{"ER_181"});
        }
        System.out.println("Geometry checks passed: " + checks);
    }
}
