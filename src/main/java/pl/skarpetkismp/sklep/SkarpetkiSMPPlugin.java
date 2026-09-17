package pl.skarpetkismp.sklep;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class SkarpetkiSMPPlugin extends JavaPlugin {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("SkarpetkiSMP Sklep wlaczony!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!command.getName().equalsIgnoreCase("sklep")) {
            return false;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§e/sklep test §7- test API");
            sender.sendMessage("§e/sklep give <nick> <produkt> [ilosc]");
            return true;
        }

        if (args[0].equalsIgnoreCase("test")) {

            if (!sender.hasPermission("skarpetkismp.admin")) {
                sender.sendMessage("§cBrak uprawnien.");
                return true;
            }

            String api = getConfig().getString("api-url", "").trim();

            if (api.isEmpty()) {
                sender.sendMessage("§cBrak adresu API w config.yml.");
                return true;
            }

            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(api))
                            .timeout(Duration.ofSeconds(10))
                            .GET()
                            .build();

                    HttpResponse<String> response =
                            http.send(request,
                                    HttpResponse.BodyHandlers.ofString());

                    sender.sendMessage(
                            "§aAPI HTTP: §f" + response.statusCode()
                    );

                    getLogger().info(
                            "API odpowiedzialo: "
                                    + response.statusCode()
                                    + " "
                                    + response.body()
                    );

                } catch (Exception e) {
                    sender.sendMessage(
                            "§cBlad polaczenia z API: §f"
                                    + e.getMessage()
                    );

                    getLogger().warning(
                            "Blad API: " + e.getMessage()
                    );
                }
            });

            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {

            if (!sender.hasPermission("skarpetkismp.admin")) {
                sender.sendMessage("§cBrak uprawnien.");
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage(
                        "§cUzycie: /sklep give <nick> <produkt> [ilosc]"
                );
                return true;
            }

            String nick = args[1];
            String produkt = args[2].toLowerCase();
            int ilosc = 1;

            if (args.length >= 4) {
                try {
                    ilosc = Math.max(1, Integer.parseInt(args[3]));
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cIlosc musi byc liczba.");
                    return true;
                }
            }

            String komenda = getConfig().getString(
                    "products." + produkt + ".command", ""
            ).trim();

            if (komenda.isEmpty()) {
                sender.sendMessage(
                        "§cNie znaleziono produktu: " + produkt
                );
                return true;
            }

            komenda = komenda
                    .replace("%player%", nick)
                    .replace("%amount%", String.valueOf(ilosc));

            String finalCommand = komenda.startsWith("/")
                    ? komenda.substring(1)
                    : komenda;

            Bukkit.getScheduler().runTask(this, () -> {

                boolean success = Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        finalCommand
                );

                if (success) {
                    sender.sendMessage(
                            "§aKomenda wydania zostala wykonana."
                    );
                } else {
                    sender.sendMessage(
                            "§cKomenda wydania zwrocila blad."
                    );
                }
            });

            return true;
        }

        sender.sendMessage("§cNieznana opcja. Uzyj /sklep help");
        return true;
    }
                                   }
