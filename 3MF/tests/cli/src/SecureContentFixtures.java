import com.qualitylogic.x3mf.DeCryptModel;
import com.qualitylogic.x3mf.MainFrame;
import com.qualitylogic.x3mf.StartUp;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.zip.*;
import javax.crypto.Cipher;
import javax.crypto.spec.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

/** Test-only encrypted mutations of the official SHA-1/deflate fixture. */
public class SecureContentFixtures {
    static Element element(Document doc, String name) {
        return (Element) doc.getElementsByTagNameNS("*", name).item(0);
    }

    public static void main(String[] args) throws Exception {
        if (args[0].equals("check-keys")) {
            StartUp.brunCli = true;
            MainFrame frame = new MainFrame();
            try {
                var open = MainFrame.class.getDeclaredMethod("cliOpenFile", String.class);
                var keys = MainFrame.class.getDeclaredField("keyinfo");
                open.setAccessible(true);
                keys.setAccessible(true);
                for (int i = 1; i < args.length; i++) {
                    if (!Boolean.TRUE.equals(open.invoke(frame, args[i]))) throw new AssertionError("Open failed");
                    boolean empty = ((List<?>) keys.get(frame)).isEmpty();
                    if (empty != (i > 1)) throw new AssertionError("Incorrect package keys after opening " + args[i]);
                }
                // Also cover the early return when root relationships are absent.
                Files.delete(Path.of("ztmp/_rels/.rels"));
                ((List<?>) keys.get(frame)).add(null);
                var find = MainFrame.class.getDeclaredMethod("findKeystore");
                find.setAccessible(true);
                find.invoke(frame);
                if (!((List<?>) keys.get(frame)).isEmpty()) throw new AssertionError("Keys retained without relationships");
            } catch (Throwable error) {
                error.printStackTrace();
                // MainFrame.dispose exits with zero, so failures must exit first.
                System.exit(1);
            }
            frame.dispose();
            return;
        }
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(Path.of(args[0])))) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null;) {
                if (!entry.isDirectory()) entries.put(entry.getName(), zip.readAllBytes());
            }
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document keys = factory.newDocumentBuilder().parse(new ByteArrayInputStream(entries.get("Secure/keystore.xml")));
        String part = element(keys, "resourcedata").getAttribute("path").substring(1);
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] key = new DeCryptModel().pemdecrypt(new DeCryptModel().getKey(),
                decoder.decode(element(keys, "CipherValue").getTextContent().trim()), false);
        byte[] encrypted = entries.get(part);
        int offset = Byte.toUnsignedInt(encrypted[8]);
        byte[] tag = decoder.decode(element(keys, "tag").getTextContent().trim());
        byte[] payload = Arrays.copyOfRange(encrypted, offset, encrypted.length + tag.length);
        System.arraycopy(tag, 0, payload, encrypted.length - offset, tag.length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec aes = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, aes, new GCMParameterSpec(128,
                decoder.decode(element(keys, "iv").getTextContent().trim())));
        byte[] aad = decoder.decode(element(keys, "aad").getTextContent().trim());
        if (aad.length > 0) cipher.updateAAD(aad);
        String xml;
        Inflater inflater = new Inflater(true);
        try (InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(cipher.doFinal(payload)), inflater)) {
            xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } finally { inflater.end(); }
        String mode = args[2];
        String changed;
        if (mode.equals("schema")) {
            changed = xml.replaceFirst("unit=\"[^\"]+\"", "unit=\"invalid-unit\"");
        } else if (mode.equals("structure")) {
            changed = xml.replaceFirst("v1=\"[0-9]+\"", "v1=\"99999999\"");
        } else if (mode.equals("no-keystore")) {
            // Same encrypted part path, but no keystore relationship or file.
            String rels = new String(entries.get("_rels/.rels"), StandardCharsets.UTF_8);
            entries.put("_rels/.rels", rels.replaceAll("<Relationship\\s[^>]*Target=\"/Secure/keystore.xml\"[^>]*/>", "").getBytes(StandardCharsets.UTF_8));
            entries.remove("Secure/keystore.xml");
            write(entries, args[1]);
            return;
        } else { throw new IllegalArgumentException(mode); }
        if (changed.equals(xml)) throw new IllegalStateException("Mutation did not change decrypted XML");
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        try (DeflaterOutputStream out = new DeflaterOutputStream(compressed, deflater)) {
            out.write(changed.getBytes(StandardCharsets.UTF_8));
        } finally { deflater.end(); }
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, aes, new GCMParameterSpec(128, iv));
        if (aad.length > 0) cipher.updateAAD(aad);
        byte[] result = cipher.doFinal(compressed.toByteArray());
        byte[] file = Arrays.copyOf(encrypted, offset + result.length - 16);
        System.arraycopy(result, 0, file, offset, result.length - 16);
        entries.put(part, file);
        element(keys, "iv").setTextContent(Base64.getEncoder().encodeToString(iv));
        element(keys, "tag").setTextContent(Base64.getEncoder().encodeToString(Arrays.copyOfRange(result, result.length - 16, result.length)));
        ByteArrayOutputStream keyXml = new ByteArrayOutputStream();
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(keys), new StreamResult(keyXml));
        entries.put("Secure/keystore.xml", keyXml.toByteArray());
        write(entries, args[1]);
    }

    static void write(Map<String, byte[]> entries, String path) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(Path.of(path)))) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue());
                zip.closeEntry();
            }
        }
    }
}
