package com.qualitylogic.x3mf;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** Eligibility controls: unsupported optional candidates cannot hide mandatory references. */
public final class AlternativePolicyTest {
    private static int count;
    private static String model(String required, String body) {
        return "<?xml version='1.0'?><model xmlns='" + GeometryCheck.CORE + "' xmlns:p='" + GeometryCheck.PROD
            + "' xmlns:pa='http://schemas.microsoft.com/3dmanufacturing/production/alternatives/2021/04' xmlns:x='urn:unknown' requiredextensions='"
            + required + "'>" + body + "</model>";
    }
    private static String alt(String path, String id, String resolution) {
        return "<pa:alternative path='" + path + "' objectid='" + id + "' modelresolution='" + resolution + "'/>";
    }
    private static final String CANDIDATE = "/3D/unknown.model";
    private static final String BAD = alt(CANDIDATE, "3", "fullres");
    private static String root(String alternatives, String ordinary, String extra) {
        return model("", "<resources><object id='2'><pa:alternatives>" + alternatives + "</pa:alternatives></object><object id='4'/>"
            + extra + "</resources><build><item objectid='2'/>" + ordinary + "</build>");
    }
    private static void entry(ZipOutputStream zip, String name, String text) throws Exception {
        zip.putNextEntry(new ZipEntry(name)); zip.write(text.getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
    }
    private static void check(Path directory, String root, String unknown, String fallback, boolean expected) throws Exception {
        check(directory, root, unknown, fallback, expected, ValidationConsumer.PRINTER);
    }
    private static void check(Path directory, String root, String unknown, String fallback, boolean expected, ValidationConsumer consumer) throws Exception {
        Path file = directory.resolve("alternative-policy-" + count + ".3mf");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(file))) {
            entry(zip, "[Content_Types].xml", "<Types xmlns='http://schemas.openxmlformats.org/package/2006/content-types'><Default Extension='model' ContentType='application/vnd.ms-package.3dmanufacturing-3dmodel+xml'/></Types>");
            entry(zip, "_rels/.rels", "<Relationships xmlns='http://schemas.openxmlformats.org/package/2006/relationships'><Relationship Id='root' Target='/3D/root.model' Type='http://schemas.microsoft.com/3dmanufacturing/2013/01/3dmodel'/></Relationships>");
            entry(zip, "3D/root.model", root);
            entry(zip, "3D/unknown.model", unknown);
            if (fallback != null) entry(zip, "3D/fallback.model", fallback);
        }
        try (ZipFile zip = new ZipFile(file.toFile())) {
            Set<String> skipped = AlternativePolicy.skippedParts(zip, consumer);
            if (skipped.contains(CANDIDATE) != expected || (expected && skipped.size() != 1))
                throw new AssertionError("Expected skip=" + expected + "; got " + skipped);
        }
        count++;
    }
    public static void main(String[] args) throws Exception {
        Path directory = Paths.get(args[0]);
        String unknown = model("x", "<resources><object id='3'/></resources><build/>");
        String good = alt("", "4", "fullres");
        check(directory, root(BAD + good, "", ""), unknown, null, true);
        check(directory, root(BAD + good, "<item objectid='3' p:path='" + CANDIDATE + "'/>", ""), unknown, null, false);
        check(directory, root(BAD + alt("", "999", "fullres"), "", ""), unknown, null, false);
        check(directory, root(BAD + alt("", "4", "lowres"), "", ""), unknown, null, false);
        check(directory, root(BAD + good, "", "").replace("requiredextensions=''", "requiredextensions='x'"), unknown, null, false);
        check(directory, root(BAD + good, "", "<object id='5'><pa:alternatives>" + BAD + "</pa:alternatives></object>"), unknown, null, false);
        check(directory, root(BAD + good, "", ""), model("", "<resources><object id='3'/></resources><build/>"), null, false);
        check(directory, root(BAD + good, "", "<object id='5'><components><component objectid='3' p:path='" + CANDIDATE + "'/></components></object>"), unknown, null, false);
        String fallback = model("", "<resources><object id='6'/></resources><build/>");
        check(directory, root(BAD + alt("/3D/fallback.model", "6", "fullres"), "", ""), unknown, fallback, true);
        check(directory, root(BAD + alt("", "004", "fullres"), "", "").replace("pa:", "alias:").replace("xmlns:pa=", "xmlns:alias="), unknown, null, true);
        check(directory, root(BAD + good, "", "").replace("<object id='4'/>", "<x:ignored><object id='4'/></x:ignored>"), unknown, null, false);
        check(directory, root(BAD + good, "", "<x:ignored path='" + CANDIDATE + "'/>"), unknown, null, true);
        check(directory, root(BAD + good, "", "<x:ignored><component objectid='3' p:path='" + CANDIDATE + "'/></x:ignored>"), unknown, null, true);
        check(directory, root(BAD + alt("", "4", "lowres"), "", ""), unknown, null, true, ValidationConsumer.EDITOR);
        check(directory, root(BAD, "", "").replace("<pa:alternatives>", "<mesh/><pa:alternatives>"), unknown, null, true);
        String previewPrimary = root(BAD, "", "").replace("<object id='2'>", "<object id='2' pa:modelresolution='lowres'><mesh/>");
        check(directory, previewPrimary, unknown, null, false, ValidationConsumer.PRINTER);
        check(directory, previewPrimary, unknown, null, true, ValidationConsumer.EDITOR);
        check(directory, root(BAD + good, "", "").replace(" modelresolution='fullres'", ""), unknown, null, true);
        check(directory, root(BAD + good, "<item objectid='3' p:path='" + CANDIDATE + "'/>", ""), unknown, null, false, ValidationConsumer.EDITOR);
        System.out.println("Passed " + count + " alternative eligibility controls");
    }
}
