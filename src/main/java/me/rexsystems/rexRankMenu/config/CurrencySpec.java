package me.rexsystems.rexRankMenu.config;

public record CurrencySpec(
        String id,
        String type,
        String balancePlaceholder,
        String giveCommand,
        String takeCommand
) {
    public CurrencySpec {
        id = id == null ? "" : id;
        type = type == null || type.isBlank() ? "vault" : type.trim();
        balancePlaceholder = balancePlaceholder == null ? "" : balancePlaceholder;
        giveCommand = giveCommand == null ? "" : giveCommand;
        takeCommand = takeCommand == null ? "" : takeCommand;
    }

    public boolean vault() {
        return type.equalsIgnoreCase("vault");
    }

    public boolean placeholder() {
        return type.equalsIgnoreCase("placeholder");
    }
}
