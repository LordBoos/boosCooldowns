package software.boos.boosCooldown.service;

import software.boos.boosCooldown.config.PluginConfig;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AliasServiceTest {

    private PluginConfig config;
    private Player player;

    @BeforeEach
    void setUp() {
        config = Mockito.mock(PluginConfig.class);
        player = Mockito.mock(Player.class);
        Mockito.when(player.getName()).thenReturn("Steve");
        World world = Mockito.mock(World.class);
        Mockito.when(world.getName()).thenReturn("world");
        Mockito.when(player.getWorld()).thenReturn(world);
    }

    @Test
    void resolveReturnsNullWhenNoAliases() {
        Mockito.when(config.getAliases()).thenReturn(Set.of());
        AliasService service = new AliasService(config);
        assertNull(service.resolve(player, "/foo"));
    }

    @Test
    void resolveExactMatch() {
        Mockito.when(config.getAliases()).thenReturn(Set.of("/ja"));
        Mockito.when(config.getAlias("/ja")).thenReturn("/me $player");
        AliasService service = new AliasService(config);
        assertEquals("/me Steve", service.resolve(player, "/ja"));
    }

    @Test
    void resolveWildcardPattern() {
        Mockito.when(config.getAliases()).thenReturn(Set.of("/ja *"));
        Mockito.when(config.getAlias("/ja *")).thenReturn("/me $*");
        AliasService service = new AliasService(config);
        assertEquals("/me hello world", service.resolve(player, "/ja hello world"));
    }

    @Test
    void resolvePositionalArgs() {
        Mockito.when(config.getAliases()).thenReturn(Set.of("/who *"));
        Mockito.when(config.getAlias("/who *")).thenReturn("/whois $1 at $world");
        AliasService service = new AliasService(config);
        assertEquals("/whois Alice at world", service.resolve(player, "/who Alice extra"));
    }

    @Test
    void resolveNonMatchingReturnsNull() {
        Mockito.when(config.getAliases()).thenReturn(Set.of("/ja"));
        Mockito.when(config.getAlias("/ja")).thenReturn("/me $player");
        AliasService service = new AliasService(config);
        assertNull(service.resolve(player, "/jiny"));
    }

    @Test
    void resolveWildcardDoesNotMatchLongerCommandSharingPrefix() {
        Mockito.when(config.getAliases()).thenReturn(Set.of("/w *"));
        Mockito.when(config.getAlias("/w *")).thenReturn("/msg $*");
        AliasService service = new AliasService(config);
        assertNull(service.resolve(player, "/warp spawn"));
        assertNull(service.resolve(player, "/warps"));
        assertEquals("/msg Alice hi", service.resolve(player, "/w Alice hi"));
    }

    @Test
    void resolveRestArgsSkipsPositionalsTheTemplateUses() {
        Mockito.when(config.getAliases()).thenReturn(Set.of("/ja *", "/tell *"));
        Mockito.when(config.getAlias("/ja *")).thenReturn("/me $1 $2 $* $world $player");
        Mockito.when(config.getAlias("/tell *")).thenReturn("/msg $1 $*");
        AliasService service = new AliasService(config);
        assertEquals("/me a b c d world Steve", service.resolve(player, "/ja a b c d"));
        assertEquals("/msg Alice hi there", service.resolve(player, "/tell Alice hi there"));
    }

    @Test
    void resolveLeavesPlaceholderTextInArgsAlone() {
        Mockito.when(config.getAliases()).thenReturn(Set.of("/w *"));
        Mockito.when(config.getAlias("/w *")).thenReturn("/msg $*");
        AliasService service = new AliasService(config);
        assertEquals("/msg Alice it costs $5", service.resolve(player, "/w Alice it costs $5"));
        assertEquals("/msg Alice $1 $player $*", service.resolve(player, "/w Alice $1 $player $*"));
    }

    @Test
    void resolveIgnoresMissingPositional() {
        Mockito.when(config.getAliases()).thenReturn(Set.of("/tell *"));
        Mockito.when(config.getAlias("/tell *")).thenReturn("/msg $1 $2");
        AliasService service = new AliasService(config);
        // only $1 provided → $2 drops
        assertEquals("/msg Alice", service.resolve(player, "/tell Alice"));
    }
}
