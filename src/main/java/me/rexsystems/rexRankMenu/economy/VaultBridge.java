package me.rexsystems.rexRankMenu.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

final class VaultBridge {

    private VaultBridge() {
    }

    static boolean has(Player player, double amount) {
        Economy economy = economy();
        return economy != null && economy.has(player, amount);
    }

    static boolean withdraw(Player player, double amount) {
        Economy economy = economy();
        if (economy == null) {
            return false;
        }
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response != null && response.transactionSuccess();
    }

    static boolean deposit(OfflinePlayer player, double amount) {
        Economy economy = economy();
        if (economy == null) {
            return false;
        }
        EconomyResponse response = economy.depositPlayer(player, amount);
        return response != null && response.transactionSuccess();
    }

    private static Economy economy() {
        RegisteredServiceProvider<Economy> registration = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (registration == null) {
            return null;
        }
        return registration.getProvider();
    }
}
