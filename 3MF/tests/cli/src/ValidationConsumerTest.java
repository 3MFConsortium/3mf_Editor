package com.qualitylogic.x3mf;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Configuration and identical DOM/streaming availability decisions for both consumers. */
public final class ValidationConsumerTest {
    private static int count;
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); count++; }
    private static String model(String required, String resources, String build) {
        return "<model xmlns='" + GeometryCheck.CORE + "' xmlns:p='" + GeometryCheck.PROD + "' xmlns:s='" + GeometryCheck.SLICE
            + "' xmlns:pa='http://schemas.microsoft.com/3dmanufacturing/production/alternatives/2021/04' requiredextensions='" + required
            + "'><resources>" + resources + "</resources><build>" + build + "</build></model>";
    }
    private static final String ITEM = "<item objectid='2'/>";
    private static final String LOCKED = "<components><component objectid='3' p:path='/3D/locked.model'/></components>";
    private static final String ALT = "<pa:alternatives><pa:alternative objectid='4' modelresolution='fullres'/></pa:alternatives>";
    private static String object(String resolution, String shape) {
        return "<object id='2' pa:modelresolution='" + resolution + "'>" + shape + "</object>";
    }
    private static void check(String xml, ValidationConsumer consumer, String expected, String... additional) throws Exception {
        for (boolean streaming : new boolean[]{false, true}) {
            List<String> issues = new ArrayList<>();
            ModelRules rules = new ModelRules((code, warning, text) -> { if (code.equals("ER_203") || code.equals("WR_029")) issues.add(code); }, consumer);
            if (streaming) rules.addConsumerModel(xml, "/3D/root.model"); else rules.addModel(ModelSchema.parse(xml), "/3D/root.model");
            for (int i = 0; i < additional.length; i++) {
                if (streaming) rules.addConsumerModel(additional[i], "/3D/part" + i + ".model");
                else rules.addModel(ModelSchema.parse(additional[i]), "/3D/part" + i + ".model");
            }
            rules.unavailable("/3D/locked.model");
            rules.finishConsumer("/3D/root.model");
            require(expected == null ? issues.isEmpty() : issues.contains(expected), consumer + " streaming=" + streaming + " expected " + expected + "; got " + issues);
        }
    }
    public static void main(String[] args) throws Exception {
        Path ini = Paths.get("3MF.ini");
        if (Files.exists(ini)) throw new AssertionError("Run in an isolated working directory without 3MF.ini");
        IniData settings = new IniData();
        settings.readIni(); require(settings.validationConsumer == ValidationConsumer.PRINTER, "Missing INI must default to printer");
        Files.write(ini, "darkMode = \"false\"\n".getBytes(StandardCharsets.UTF_8));
        settings.readIni(); require(settings.validationConsumer == ValidationConsumer.PRINTER, "Legacy INI must default to printer");
        Files.write(ini, "validationConsumer = \"EdItOr\"\n".getBytes(StandardCharsets.UTF_8));
        settings.readIni(); require(settings.validationConsumer == ValidationConsumer.EDITOR, "INI value must be case-insensitive");
        settings.writeIni();
        require(new String(Files.readAllBytes(ini), StandardCharsets.UTF_8).contains("validationConsumer = \"editor\""), "Consumer must survive INI rewrite");
        Files.write(ini, "validationConsumer = \"viewer\"\n".getBytes(StandardCharsets.UTF_8));
        boolean rejected = false;
        try { settings.readIni(); } catch (IllegalArgumentException ex) { rejected = ex.getMessage().contains("validationConsumer"); }
        require(rejected, "Invalid INI consumer must not silently select another policy");
        Files.delete(ini);
        settings.readIni(); require(settings.validationConsumer == ValidationConsumer.PRINTER, "INI reread must reset the default");
        require(ValidationConsumer.parse(" PRINTER ") == ValidationConsumer.PRINTER, "Consumer parser must normalize case/space");
        String primary = model("", object("fullres", "<mesh/>"), ITEM);
        check(primary, ValidationConsumer.PRINTER, null);
        check(primary, ValidationConsumer.EDITOR, null);
        check(model("", object("lowres", "<mesh/>"), ITEM), ValidationConsumer.PRINTER, "ER_203");
        check(model("", object("lowres", "<mesh/>"), ITEM), ValidationConsumer.EDITOR, null);
        check(model("", object("obfuscated", "<mesh/>"), ITEM), ValidationConsumer.PRINTER, "ER_203");
        check(model("", object("fullres", LOCKED), ITEM), ValidationConsumer.PRINTER, "ER_203");
        check(model("", object("fullres", LOCKED), ITEM), ValidationConsumer.EDITOR, null);
        check(model("", object("fullres", LOCKED + ALT) + "<object id='4'><mesh/></object>", ITEM), ValidationConsumer.PRINTER, null);
        check(model("", object("fullres", LOCKED + ALT.replace("fullres", "lowres")) + "<object id='4'><mesh/></object>", ITEM), ValidationConsumer.PRINTER, "ER_203");
        // The alternative element's resolution wins over its target object's resolution.
        check(model("", object("lowres", "<mesh/>" + ALT) + "<object id='4' pa:modelresolution='lowres'><mesh/></object>", ITEM), ValidationConsumer.PRINTER, null);
        check(model("", object("fullres", LOCKED), "<item objectid='2' printable='false'/>"), ValidationConsumer.PRINTER, null);
        check(model("", object("fullres", LOCKED) + "<object id='4'><mesh/></object>", ITEM + "<item objectid='4'/>"), ValidationConsumer.PRINTER, "ER_203");
        String sliced = model("s", "<s:slicestack id='1'><s:sliceref slicepath='/3D/locked.model' slicestackid='3'/></s:slicestack>"
            + "<object id='2' s:meshresolution='lowres' s:slicestackid='1'><mesh/></object>", ITEM);
        check(sliced, ValidationConsumer.PRINTER, "ER_203");
        check(sliced.replace("/3D/locked.model", "/3D/part0.model"), ValidationConsumer.PRINTER, null,
            model("", "<s:slicestack id='3'><s:slice ztop='1'/></s:slicestack>", ""));
        check(model("", "<object id='2' type='other'><mesh/></object>", ITEM), ValidationConsumer.PRINTER, "ER_203");
        check(model("", "<object id='2'><components><component objectid='2'/></components></object>", ITEM), ValidationConsumer.PRINTER, "ER_203");
        StringBuilder deep = new StringBuilder();
        for (int i = 2; i < 133; i++) deep.append("<object id='").append(i).append("'><components><component objectid='").append(i + 1).append("'/></components></object>");
        deep.append("<object id='133'><mesh/></object>");
        check(model("", deep.toString(), ITEM), ValidationConsumer.PRINTER, "WR_029");
        System.out.println("Passed " + count + " consumer/configuration controls");
    }
}
