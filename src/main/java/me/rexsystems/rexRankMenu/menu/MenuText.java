package me.rexsystems.rexRankMenu.menu;

import me.rexsystems.rexRankMenu.config.MenuConfig;
import me.rexsystems.rexRankMenu.config.RankDress;
import me.rexsystems.rexRankMenu.economy.Amounts;
import me.rexsystems.rexRankMenu.util.TextParser;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.HashMap;
import java.util.Map;

public final class MenuText {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private MenuText() {
    }

    public static Map<String, String> fields(MenuConfig config, String player, String group, String next, String status) {
        RankDress dress = config.dressFor(group);
        String price = Amounts.format(dress.price());
        Map<String, String> map = new HashMap<>();
        map.put("player", player == null ? "" : player);
        map.put("group", group == null ? "" : group);
        map.put("price", price);
        map.put("amount", price);
        map.put("currency", dress.currency());
        map.put("track", config.track());
        map.put("next", next == null ? "" : next);
        map.put("status", status == null ? "" : status);
        map.put("rank", group == null ? "" : group);
        map.put("rank", plain(dress.name(), map));
        return map;
    }

    public static String plain(String template, Map<String, String> fields) {
        String resolved = TextParser.apply(template == null || template.isBlank() ? "{group}" : template, fields);
        return PLAIN.serialize(TextParser.parse(resolved));
    }

    public static String resolve(org.bukkit.entity.Player player, String template, Map<String, String> fields) {
        String resolved = TextParser.apply(template, fields);
        return me.rexsystems.rexRankMenu.util.Placeholders.apply(player, resolved);
    }
}
