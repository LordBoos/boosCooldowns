package software.boos.boosCooldown.service;

import software.boos.boosCooldown.config.PluginConfig;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AliasService {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$(player|world|\\*|\\d+)");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private final PluginConfig config;

    public AliasService(PluginConfig config) {
        this.config = config;
    }

    /**
     * Resolves an alias for a command. Supports wildcard patterns like "/ja *" → "/me $*",
     * "/who $player $world", etc. Returns null if no alias applies.
     */
    public String resolve(Player player, String rawCommand) {
        Set<String> aliases = config.getAliases();
        if (aliases.isEmpty()) return null;

        String lower = rawCommand.toLowerCase();
        // Exact match first
        for (String aliasKey : aliases) {
            if (aliasKey.equalsIgnoreCase(lower) && !aliasKey.contains("*")) {
                return substitute(config.getAlias(aliasKey), player, new String[0]);
            }
        }
        // Wildcard match
        for (String aliasKey : aliases) {
            if (!aliasKey.contains("*")) continue;
            String keyNormalized = aliasKey.toLowerCase();
            // Keep the space before '*': "/w *" must not match "/warp" (3.x matched "/w .+").
            String prefix = keyNormalized.substring(0, keyNormalized.indexOf('*'));
            if (lower.startsWith(prefix)) {
                String remainder = rawCommand.substring(prefix.length()).trim();
                String[] args = remainder.isEmpty() ? new String[0] : remainder.split("\\s+");
                return substitute(config.getAlias(aliasKey), player, args);
            }
        }
        return null;
    }

    /**
     * Fills the template in one pass, so placeholder text inside the player's
     * arguments is never substituted. {@code $*} is the arguments after the
     * highest {@code $N} the template uses: "/msg $*" gets all of them,
     * "/me $1 $2 $*" the third onward (as 3.x did).
     */
    private String substitute(String template, Player player, String[] args) {
        if (template == null) return null;
        int restFrom = PLACEHOLDER.matcher(template).results()
                .map(m -> m.group(1))
                .filter(g -> Character.isDigit(g.charAt(0)))
                .mapToInt(Integer::parseInt)
                .max().orElse(0);
        String rest = String.join(" ", Arrays.copyOfRange(args, Math.min(restFrom, args.length), args.length));
        String result = PLACEHOLDER.matcher(template).replaceAll(m -> Matcher.quoteReplacement(switch (m.group(1)) {
            case "player" -> player.getName();
            case "world" -> player.getWorld().getName();
            case "*" -> rest;
            default -> {
                int i = Integer.parseInt(m.group(1));
                yield i >= 1 && i <= args.length ? args[i - 1] : "";
            }
        }));
        return WHITESPACE.matcher(result).replaceAll(" ").trim();
    }
}
