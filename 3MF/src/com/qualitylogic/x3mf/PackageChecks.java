package com.qualitylogic.x3mf;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/** Small package checks that do not decode images or allocate mesh data. */
final class PackageChecks {
    private PackageChecks() { }
    static String invalidName(String name) {
        for (int i = 0; i < name.length(); i++)
            if (name.charAt(i) > 127)
                return "Unescaped Unicode in ZIP part name; use percent-encoded UTF-8: " + name;
        // OPC's special package relationship part is not an ordinary part name.
        if (name.equals("_rels/.rels")) return null;
        for (String segment : name.split("/"))
            if (segment.startsWith(".")) return "Part name segment must not begin with a period: " + name;
        return null;
    }
    static boolean cmykJpeg(InputStream stream) throws IOException {
        DataInputStream in = new DataInputStream(stream);
        if (in.read() != 0xff || in.read() != 0xd8) return false;
        while (true) {
            if (in.read() != 0xff) return false;
            int marker;
            do { marker = in.read(); } while (marker == 0xff);
            if (marker < 0 || marker == 0xda || marker == 0xd9) return false;
            if (marker == 0x01 || (marker >= 0xd0 && marker <= 0xd7)) continue;
            int length = in.readUnsignedShort() - 2;
            if (length < 0) return false;
            // Skip APP/EXIF payloads as segments: they may contain another JPEG.
            if (marker >= 0xc0 && marker <= 0xcf && marker != 0xc4 && marker != 0xc8 && marker != 0xcc) {
                if (length < 6) return false;
                in.readUnsignedByte(); in.readUnsignedShort(); in.readUnsignedShort();
                return in.readUnsignedByte() == 4; // CMYK and YCCK both use four components.
            }
            while (length > 0) {
                int skipped = in.skipBytes(length);
                if (skipped == 0) { if (in.read() < 0) return false; skipped = 1; }
                length -= skipped;
            }
        }
    }
}
