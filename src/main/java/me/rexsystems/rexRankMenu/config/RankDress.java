package me.rexsystems.rexRankMenu.config;

import java.util.List;

public record RankDress(
        double price,
        String currency,
        String name,
        List<String> lore,
        Integer slot,
        String material,
        Integer customModelData
) {
    public RankDress {
        if (Double.isNaN(price) || price < 0d) {
            price = 0d;
        }
        currency = currency == null ? "" : currency;
        name = name == null ? "" : name;
        lore = lore == null ? List.of() : List.copyOf(lore);
        material = material == null ? "" : material;
    }

    public static RankDress unnamed(String currency) {
        return new RankDress(
                0d,
                currency,
                "",
                List.of("<gray>{price} {currency}", "<gray>{status}"),
                null,
                "",
                null
        );
    }
}
