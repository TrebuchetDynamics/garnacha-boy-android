package com.trebuchetdynamics.emulator.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Writes cartridge saves and save states to a user-owned ZIP archive. */
final class SaveBackup {
    private SaveBackup() {}

    static int write(File filesDir, OutputStream output) throws IOException {
        ZipOutputStream zip = new ZipOutputStream(output);
        int count = add(zip, new File(filesDir, "saves"), "saves/");
        count += add(zip, new File(filesDir, "states"), "states/");
        zip.finish();
        return count;
    }

    private static int add(ZipOutputStream zip, File file, String path) throws IOException {
        File[] children = file.listFiles();
        if (children == null) {
            return 0;
        }
        Arrays.sort(children, (a, b) -> a.getName().compareTo(b.getName()));
        int count = 0;
        byte[] buffer = new byte[8192];
        for (File child : children) {
            String childPath = path + child.getName();
            if (child.isDirectory()) {
                count += add(zip, child, childPath + "/");
            } else if (child.isFile()
                    && (child.getName().endsWith(".sav")
                    || child.getName().endsWith(".state"))) {
                zip.putNextEntry(new ZipEntry(childPath));
                try (FileInputStream in = new FileInputStream(child)) {
                    int read;
                    while ((read = in.read(buffer)) >= 0) {
                        zip.write(buffer, 0, read);
                    }
                }
                zip.closeEntry();
                count++;
            }
        }
        return count;
    }
}
