package software.boos.boosCooldown.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginConfigTest {

    private static final String OLD_CONFIG = """
            options:
              options:
                #should warmups be enabled?
                warmups_enabled: false
                disabled_for_ops: true
              units:
                seconds: sec
            commands:
              groups:
                default:
                  /home:
                    cooldown: 30
              aliases:
                /h: /home
            """;

    private static final Pattern BACKUP =
            Pattern.compile("config\\.yml\\.\\d{4}-\\d\\d-\\d\\d_\\d\\d-\\d\\d-\\d\\d(-\\d+)?\\.bak");
    private static final Pattern INVALID_BACKUP =
            Pattern.compile("config\\.yml\\.\\d{4}-\\d\\d-\\d\\d_\\d\\d-\\d\\d-\\d\\d(-\\d+)?\\.invalid\\.bak");

    @TempDir
    Path dataFolder;

    private JavaPlugin plugin;
    private Path configFile;

    @BeforeEach
    void setUp() {
        plugin = Mockito.mock(JavaPlugin.class);
        Mockito.when(plugin.getDataFolder()).thenReturn(dataFolder.toFile());
        Mockito.when(plugin.getLogger()).thenReturn(Logger.getLogger("PluginConfigTest"));
        Mockito.when(plugin.getResource("config.yml")).thenAnswer(inv -> bundledConfig());
        Mockito.doAnswer(inv -> {
            try (InputStream in = bundledConfig()) {
                Files.copy(in, configFile);
            }
            return null;
        }).when(plugin).saveResource("config.yml", false);
        configFile = dataFolder.resolve("config.yml");
    }

    private List<Path> backups() throws IOException {
        try (Stream<Path> files = Files.list(dataFolder)) {
            return files.filter(f -> f.getFileName().toString().endsWith(".bak")).sorted().toList();
        }
    }

    private Path onlyBackup(Pattern name) throws IOException {
        List<Path> backups = backups();
        assertEquals(1, backups.size(), backups.toString());
        assertTrue(name.matcher(backups.get(0).getFileName().toString()).matches(), backups.toString());
        return backups.get(0);
    }

    private static InputStream bundledConfig() {
        return PluginConfigTest.class.getClassLoader().getResourceAsStream("config.yml");
    }

    @Test
    void addsMissingPluginOptionsWithoutTouchingUserSections() throws IOException {
        Files.writeString(configFile, OLD_CONFIG, StandardCharsets.UTF_8);
        byte[] original = Files.readAllBytes(configFile);

        new PluginConfig(plugin, "config.yml");
        YamlConfiguration saved = YamlConfiguration.loadConfiguration(configFile.toFile());

        assertEquals("sqlite", saved.getString("database.type"));
        assertEquals(4, saved.getInt("database.pool-size"));
        assertEquals(30, saved.getInt("database.cache-ttl-seconds"));
        assertTrue(saved.isBoolean("options.options.debug"));
        assertFalse(saved.getBoolean("options.options.debug"));
        assertTrue(saved.isBoolean("options.action_bar.warmup"));
        assertEquals(10, saved.getInt("options.action_bar.update_interval_ticks"));
        assertEquals(List.of("should debug logging be enabled?"), saved.getComments("options.options.debug"));
        assertEquals(List.of("how long the in-memory cache trusts DB values before refreshing"),
                saved.getComments("database.cache-ttl-seconds"));

        assertFalse(saved.getBoolean("options.options.warmups_enabled"));
        assertEquals("sec", saved.getString("options.units.seconds"));

        assertEquals(Set.of("/h"), saved.getConfigurationSection("commands.aliases").getKeys(false));
        assertEquals(Set.of("default"), saved.getConfigurationSection("commands.groups").getKeys(false));
        assertEquals(Set.of("/home"), saved.getConfigurationSection("commands.groups.default").getKeys(false));
        assertFalse(saved.contains("global"));

        Path backup = onlyBackup(BACKUP);
        assertArrayEquals(original, Files.readAllBytes(backup));

        byte[] upgraded = Files.readAllBytes(configFile);
        new PluginConfig(plugin, "config.yml");
        assertArrayEquals(upgraded, Files.readAllBytes(configFile));
        assertEquals(List.of(backup), backups(), "second load must not back up again");
    }

    @Test
    void laterMigrationKeepsEarlierBackup() throws IOException {
        Files.writeString(configFile, OLD_CONFIG, StandardCharsets.UTF_8);
        new PluginConfig(plugin, "config.yml");
        String firstBackup = Files.readString(onlyBackup(BACKUP));

        YamlConfiguration edited = YamlConfiguration.loadConfiguration(configFile.toFile());
        edited.set("database.pool-size", null);
        edited.save(configFile.toFile());
        String beforeSecond = Files.readString(configFile);

        new PluginConfig(plugin, "config.yml");

        List<Path> backups = backups();
        assertEquals(2, backups.size());
        for (Path backup : backups) {
            assertTrue(BACKUP.matcher(backup.getFileName().toString()).matches(), backup.toString());
        }
        assertEquals(Set.of(firstBackup, beforeSecond),
                Set.of(Files.readString(backups.get(0)), Files.readString(backups.get(1))));
    }

    @Test
    void invalidYamlIsMovedAsideAndDefaultLoaded() throws IOException {
        Files.writeString(configFile, """
                options:
                  options:
                    debug: false
                commands:
                  groups:
                    default:
                      /home:
                        cooldown: 30
                  aliases:
                    /h: "/home
                """, StandardCharsets.UTF_8);
        byte[] original = Files.readAllBytes(configFile);

        PluginConfig config = new PluginConfig(plugin, "config.yml");

        assertArrayEquals(original, Files.readAllBytes(onlyBackup(INVALID_BACKUP)));
        try (InputStream in = bundledConfig()) {
            assertArrayEquals(in.readAllBytes(), Files.readAllBytes(configFile));
        }
        assertEquals("sqlite", config.getString("database.type", null));
        assertTrue(config.save());
    }

    @Test
    void completeConfigIsNotWritten() throws IOException {
        try (InputStream in = bundledConfig()) {
            Files.copy(in, configFile);
        }
        byte[] original = Files.readAllBytes(configFile);

        new PluginConfig(plugin, "config.yml");

        assertArrayEquals(original, Files.readAllBytes(configFile));
        assertEquals(List.of(), backups());
    }
}
