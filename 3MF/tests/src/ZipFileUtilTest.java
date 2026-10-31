import com.qualitylogic.x3mf.ZipFileUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Filesystem regressions, runnable without Swing or official fixtures. */
public class ZipFileUtilTest {
    private static final List<String> failures = new ArrayList<>();
    private static int assertions;

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) failures.add(message);
    }

    private static void archive(Path zip, String name) throws IOException {
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry(name));
            if (!name.endsWith("/")) out.write("payload".getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        }
    }

    private static void extract(int mode, Path zip, Path destination, String entry) {
        switch (mode) {
            case 0: ZipFileUtil.decompressZip(zip.toString(), destination.toString()); break;
            case 1: ZipFileUtil.decompressZipAlt(zip.toString(), destination.toString()); break;
            case 2: ZipFileUtil.decompressZipFile(zip.toString(), destination.toString(), entry); break;
            default: throw new AssertionError("Unknown mode");
        }
    }

    public static void main(String[] args) throws Exception {
        Path root = Paths.get(args[0]).toAbsolutePath();
        Files.createDirectories(root);
        for (int mode = 0; mode < 3; mode++) {
            Path work = Files.createDirectories(root.resolve("mode-" + mode));
            Path zip = work.resolve("model.3mf");
            Path output = Files.createDirectories(work.resolve("output"));
            String valid = "3D/nested/model.model";
            archive(zip, valid);
            extract(mode, zip, output, valid);
            check(Files.exists(output.resolve(valid)), "Nested extraction mode " + mode);
            check("payload".equals(new String(Files.readAllBytes(output.resolve(valid)), StandardCharsets.UTF_8)),
                    "Extracted content mode " + mode);

            // Opening an archive must not silently change its original attributes.
            DosFileAttributeView attributes = Files.getFileAttributeView(zip, DosFileAttributeView.class);
            if (attributes != null) {
                attributes.setReadOnly(true);
                try {
                    extract(mode, zip, output, valid);
                    check(attributes.readAttributes().isReadOnly(), "Preserve read-only archive mode " + mode);
                } finally {
                    attributes.setReadOnly(false);
                }
            }

            String[] unsafe = { "../escape.txt", "..\\escape.txt", "../output-sibling/escape.txt",
                    "../escaped-directory/", work.resolve("absolute.txt").toString().replace('\\', '/'),
                    "C:/archive-escape.txt", "/archive-escape.txt" };
            for (String entry : unsafe) {
                archive(zip, entry);
                extract(mode, zip, output, entry);
                check(!Files.exists(work.resolve("escape.txt")), "Reject traversal " + entry + " mode " + mode);
                check(!Files.exists(work.resolve("output-sibling")), "Reject sibling traversal mode " + mode);
                check(!Files.exists(work.resolve("escaped-directory")), "Reject directory traversal mode " + mode);
                check(!Files.exists(work.resolve("absolute.txt")), "Reject absolute path mode " + mode);
                check(!Files.exists(output.resolve("archive-escape.txt")), "Reject rooted path mode " + mode);
            }
        }

        // Failure recovery must not clear an unrelated folder in the current directory.
        Path sentinel = Paths.get("ztmp", "keep.txt");
        Files.createDirectories(sentinel.getParent());
        Files.write(sentinel, Arrays.asList("keep"), StandardCharsets.UTF_8);
        Path broken = root.resolve("broken.3mf");
        Files.write(broken, new byte[] { 1, 2, 3 });
        ZipFileUtil.decompressZip(broken.toString(), root.resolve("broken-output").toString());
        check(Files.exists(sentinel), "Fallback preserves unrelated ztmp contents");

        Comparator<String> comparator = new ZipFileUtil.WindowsExplorerComparator();
        List<String> names = Arrays.asList("_a", "_b", "_1", "_2", "a", "b", "model1", "model2", "model9", "model10",
                "model100000000000000000000", "model01");
        for (String a : names) {
            check(comparator.compare(a, a) == 0, "Comparator reflexivity: " + a);
            for (String b : names) {
                check(Integer.signum(comparator.compare(a, b)) == -Integer.signum(comparator.compare(b, a)),
                        "Comparator antisymmetry: " + a + ", " + b);
                for (String c : names) {
                    if (comparator.compare(a, b) <= 0 && comparator.compare(b, c) <= 0) {
                        check(comparator.compare(a, c) <= 0, "Comparator transitivity: " + a + ", " + b + ", " + c);
                    }
                }
            }
        }
        check(comparator.compare("_a", "a") < 0, "Underscores sort first");
        check(comparator.compare("model2", "model10") < 0, "Natural numeric ordering");
        if (!failures.isEmpty()) {
            for (String failure : failures) System.err.println("FAIL: " + failure);
            throw new AssertionError(failures.size() + " failures in " + assertions + " assertions");
        }
        System.out.println("Passed " + assertions + " archive and comparator assertions.");
    }
}
