package com.trebuchetdynamics.emulator.app;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.Test;

public final class SaveBackupTest {
    @Test
    public void exportsOnlyCartridgeSavesAndStates() throws Exception {
        File root = Files.createTempDirectory("save-backup").toFile();
        File saves = new File(root, "saves");
        File states = new File(root, "states/game-id");
        File roms = new File(root, "roms");
        saves.mkdirs();
        states.mkdirs();
        roms.mkdirs();
        Files.write(new File(saves, "game-id.sav").toPath(), new byte[] {1, 2});
        Files.write(new File(states, "slot1.state").toPath(), new byte[] {3, 4});
        Files.write(new File(saves, "game-id.sav.tmp").toPath(), new byte[] {9});
        Files.write(new File(roms, "game-id.gba").toPath(), new byte[] {8});

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertEquals(2, SaveBackup.write(root, output));

        Map<String, byte[]> entries = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(
                new ByteArrayInputStream(output.toByteArray()))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entries.put(entry.getName(), zip.readAllBytes());
            }
        }
        assertEquals(2, entries.size());
        assertArrayEquals(new byte[] {1, 2}, entries.get("saves/game-id.sav"));
        assertArrayEquals(new byte[] {3, 4}, entries.get("states/game-id/slot1.state"));
    }
}
