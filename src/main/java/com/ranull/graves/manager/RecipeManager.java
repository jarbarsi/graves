package com.ranull.graves.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class RecipeManager {
    private final Graves plugin;

    public RecipeManager(Graves plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        onDisable();
        loadRecipes();
    }

    public void loadRecipes() {
        ConfigurationSection configurationSection = plugin.getConfig().getConfigurationSection("settings.token");

        if (configurationSection != null) {
            for (String key : configurationSection.getKeys(false)) {
                if (plugin.getConfig().getBoolean("settings.token." + key + ".craft")) {
                    addTokenRecipe(key, getToken(key));
                    plugin.debugMessage("Added recipe " + key, 1);
                }
            }
        }
    }

    public ItemStack getToken(String token) {
        if (plugin.getConfig().isConfigurationSection("settings.token." + token)) {
            Material material = Material.matchMaterial(plugin.getConfig()
                    .getString("settings.token." + token + ".material", "SUNFLOWER"));
            ItemStack itemStack = new ItemStack(material != null ? material : Material.CHEST);

            setRecipeData(token, itemStack);

            if (itemStack.hasItemMeta()) {
                ItemMeta itemMeta = itemStack.getItemMeta();

                if (itemMeta != null) {
                    String name = ChatColor.WHITE + StringUtil.parseString(plugin.getConfig()
                            .getString("settings.token." + token + ".name"), plugin);
                    List<String> loreList = new ArrayList<>();

                    itemMeta.setDisplayName(name);

                    for (String string : plugin.getConfig().getStringList("settings.token." + token + ".lore")) {
                        loreList.add(ChatColor.GRAY + StringUtil.parseString(string, plugin));
                    }

                    itemMeta.setLore(loreList);
                    itemStack.setItemMeta(itemMeta);
                }
            }

            return itemStack;
        }

        return null;
    }

    public void onDisable() {
        Iterator<Recipe> iterator = plugin.getServer().recipeIterator();

        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();

            if (recipe != null) {
                ItemStack itemStack = recipe.getResult();

                if (itemStack.hasItemMeta() && isToken(itemStack)) {
                    iterator.remove();
                }
            }
        }
    }

    public void addTokenRecipe(String token, ItemStack itemStack) {
        ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey(plugin, token + "GraveToken"), itemStack);

        shapedRecipe.shape("ABC", "DEF", "GHI");

        List<String> lineList = plugin.getConfig().getStringList("settings.token." + token + ".recipe");
        int recipeKey = 1;

        for (String string : lineList.get(0).split(" ")) {
            Material material = Material.matchMaterial(string);

            if (material != null) {
                shapedRecipe.setIngredient(getChar(recipeKey), material);
            }

            recipeKey++;
        }

        for (String string : lineList.get(1).split(" ")) {
            Material material = Material.matchMaterial(string);

            if (material != null) {
                shapedRecipe.setIngredient(getChar(recipeKey), material);
            }

            recipeKey++;
        }

        for (String string : lineList.get(2).split(" ")) {
            Material material = Material.matchMaterial(string);

            if (material != null) {
                shapedRecipe.setIngredient(getChar(recipeKey), material);
            }

            recipeKey++;
        }

        plugin.getServer().addRecipe(shapedRecipe);
    }

    public ItemStack getGraveTokenFromPlayer(String token, List<ItemStack> itemStackList) {
        for (ItemStack itemStack : itemStackList) {
            if (itemStack != null && isToken(token, itemStack)) {
                return itemStack;
            }
        }

        return null;
    }

    public void setRecipeData(String token, ItemStack itemStack) {
        if (plugin.getVersionManager().hasPersistentData()) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta != null) {
                itemMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "token"),
                        PersistentDataType.STRING, token);
                itemStack.setItemMeta(itemMeta);
            }
        }
    }

    public boolean isToken(String token, ItemStack itemStack) {
        if (plugin.getVersionManager().hasPersistentData()) {
            if (itemStack.getItemMeta() != null && itemStack.getItemMeta().getPersistentDataContainer()
                    .has(new NamespacedKey(plugin, "token"), PersistentDataType.STRING)) {
                String string = itemStack.getItemMeta().getPersistentDataContainer()
                        .get(new NamespacedKey(plugin, "token"), PersistentDataType.STRING);

                return string != null && string.equals(token);
            }
        } else {
            // TODO
            return false;
        }

        return false;
    }

    public boolean isToken(ItemStack itemStack) {
        return plugin.getVersionManager().hasPersistentData() && itemStack.getItemMeta() != null
                && itemStack.getItemMeta().getPersistentDataContainer()
                .has(new NamespacedKey(plugin, "token"), PersistentDataType.STRING);
    }

    private char getChar(int count) {
        switch (count) {
            case 1:
                return 'A';
            case 2:
                return 'B';
            case 3:
                return 'C';
            case 4:
                return 'D';
            case 5:
                return 'E';
            case 6:
                return 'F';
            case 7:
                return 'G';
            case 8:
                return 'H';
            case 9:
                return 'I';
            default:
                return '*';
        }
    }
}
