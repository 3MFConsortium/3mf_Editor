package com.qualitylogic.x3mf;

import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

/** Check instance-dependent schema rules independently of CLI preference filtering. */
public final class ModelSchemaTest {
    private static int count;
    private static void check(File schema, String xml, boolean packageProduction, String error) throws Exception {
        try {
            ModelSchema.load(schema, xml, packageProduction).newValidator().validate(SafeXml.source(xml));
            if (error != null) throw new AssertionError("Expected schema rejection: " + error);
        } catch (SAXException ex) {
            if (error == null || !ex.getMessage().contains(error)) throw ex;
        }
        count++;
    }
    public static void main(String[] args) throws Exception {
        File schema = new File(args[0]);
        String core;
        try (ZipFile zip = new ZipFile(args[1]); InputStream in = zip.getInputStream(zip.getEntry("3D/3dmodel.model"))) {
            core = IOUtils.toString(in, "UTF-8");
        }
        String prod = "http://schemas.microsoft.com/3dmanufacturing/production/2015/06";
        check(schema, core, false, null);
        check(schema, core.replaceFirst("<model\\b", "<model xmlns:unused='" + prod + "'"), false, null);
        check(schema, core.replaceFirst("<object\\b", "<object xmlns:z='urn:unknown' z:unrecognized='yes'"), false, null);
        check(schema, core.replaceFirst("<object\\b", "<object unrecognized='yes'"), false, "unrecognized");
        check(schema, core.replaceFirst("<object\\b", "<object xmlns:q='" + prod + "' q:UUID='invalid'"), false, "invalid");
        String required = core.replaceFirst("<model\\b", "<model xmlns:q='" + prod + "'")
                .replaceFirst("requiredextensions=\"[^\"]*\"", "requiredextensions='q'");
        if (!ModelSchema.usesProduction(ModelSchema.parse(required))) throw new AssertionError("Production alias not recognized");
        count++;
        check(schema, required, false, "UUID");
        check(schema, core, true, "UUID");
        // A component with no Production attributes must still require UUID when the root uses Production.
        String component = core.replaceFirst("(?s)<mesh>.*?</mesh>", "<components><component objectid='2'/></components>");
        if (component.equals(core)) throw new AssertionError("Component mutation failed");
        check(schema, component, false, null);
        check(schema, component, true, "UUID");
        check(schema, core.replaceFirst("<\\?xml[^>]*\\?>", ""), false, null);
        // Production context must not leak into the next unextended validation.
        check(schema, core, false, null);
        String dtd = core.replaceFirst("<model\\b", "<!DOCTYPE model []><model");
        check(schema, dtd, false, "DOCTYPE");
        System.out.println("Passed " + count + " model-schema checks");
    }
}
