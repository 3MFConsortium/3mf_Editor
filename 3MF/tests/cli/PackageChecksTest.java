package com.qualitylogic.x3mf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PackageChecksTest {
    private static int count;
    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
        count++;
    }
    private static byte[] jpeg(int components, boolean embeddedRgb) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(255); out.write(216);
        if (embeddedRgb) {
            byte[] embedded = jpeg(3, false);
            out.write(255); out.write(225); out.write(0); out.write(embedded.length + 2); out.write(embedded);
        }
        out.write(new byte[]{(byte)255, (byte)192, 0, 8, 8, 0, 1, 0, 1, (byte)components});
        return out.toByteArray();
    }
    public static void main(String[] args) throws Exception {
        check(PackageChecks.invalidName("3D/%D4%AAmodel.model") == null, "encoded Unicode");
        check(PackageChecks.invalidName("3D/\u052amodel.model") != null, "raw Unicode");
        check(PackageChecks.invalidName("3D/.model") != null, "leading period");
        check(PackageChecks.invalidName("_rels/.rels") == null, "root relationship exception");
        check(PackageChecks.invalidName("3D/_rels/model.model.rels") == null, "part relationship");
        check(PackageChecks.invalidName("3D/model.v2.model") == null, "internal periods");
        for (int components : new int[]{1,3,4})
            check(PackageChecks.cmykJpeg(new ByteArrayInputStream(jpeg(components, false))) == (components == 4), "JPEG components " + components);
        check(PackageChecks.cmykJpeg(new ByteArrayInputStream(jpeg(4, true))), "CMYK outside embedded RGB thumbnail");
        check(!PackageChecks.cmykJpeg(new ByteArrayInputStream(new byte[]{1,2,3})), "non JPEG");
        for (String dtd : new String[]{"<!DOCTYPE model []>",
                "<!DOCTYPE model [<!ENTITY text 'value'>]>",
                "<!DOCTYPE model SYSTEM 'file:///unavailable-issue9.dtd'>",
                "<!DOCTYPE model [<!ENTITY ext SYSTEM 'file:///unavailable-issue9.txt'>]>"}) {
            String xml = "<?xml version='1.0'?>" + dtd + "<model/>";
            boolean rejected = false;
            try { SafeXml.factory().newDocumentBuilder().parse(new InputSource(new StringReader(xml))); }
            catch (SAXException expected) { rejected = expected.getMessage().contains("DOCTYPE"); }
            check(rejected, "DOM DTD rejection");
            rejected = false;
            try { SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema().newValidator().validate(SafeXml.source(xml)); }
            catch (SAXException expected) { rejected = expected.getMessage().contains("DOCTYPE"); }
            check(rejected, "schema DTD rejection");
        }
        String xml = "<?xml version='1.0'?><!-- <!DOCTYPE fake> --><model><![CDATA[<!DOCTYPE text>]]></model>";
        check(SafeXml.factory().newDocumentBuilder().parse(new InputSource(new StringReader(xml))).getDocumentElement().getTagName().equals("model"), "literal DTD text allowed");
        System.out.println("Passed " + count + " package/XML checks");
    }
}
