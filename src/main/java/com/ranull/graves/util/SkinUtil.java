package com.ranull.graves.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.ranull.skulltextureapi.SkullTextureAPI;
import org.bukkit.Bukkit;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.UUID;

public final class SkinUtil {
    private static String GAMEPROFILE_METHOD;
    private static Method property_getValue;
    private static Method property_getSignature;

    static {
        // Determine which method to use during class loading
        try {
            property_getValue = Property.class.getMethod("value");
            property_getSignature = Property.class.getMethod("signature");
        } catch (NoSuchMethodException e) {
            try {
                property_getValue = Property.class.getMethod("getValue");
                property_getSignature = Property.class.getMethod("getSignature");
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException("Failed to find a valid method for Property value/signature retrieval", ex);
            }
        }
    }

    public static void setSkullBlockTexture(Skull skull, String name, String base64) {
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name);

        gameProfile.getProperties().put("textures", new Property("textures", base64));

        try {
            Field profileField = skull.getClass().getDeclaredField("profile");

            profileField.setAccessible(true);
            profileField.set(skull, gameProfile);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }

    private static String getPlayerProperty(Entity entity, Method call_this) {
        if (entity instanceof Player) {
            GameProfile gameProfile = getPlayerGameProfile((Player) entity);

            if (gameProfile != null) {
                PropertyMap propertyMap = gameProfile.getProperties();

                if (propertyMap.containsKey("textures")) {
                    Collection<Property> propertyCollection = propertyMap.get("textures");

                    if (propertyCollection.isEmpty())
                        return null;

                    Property prop = propertyCollection.stream().findFirst().get();
                    try {
                        return (String) call_this.invoke(prop);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return null;
    }

    public static String getTexture(Entity entity) {
        if (entity instanceof Player) {
            return getPlayerProperty(entity, property_getValue);
        }

        Plugin skullTextureAPIPlugin = Bukkit.getServer().getPluginManager().getPlugin("SkullTextureAPI");

        if (skullTextureAPIPlugin != null && skullTextureAPIPlugin.isEnabled()
                && skullTextureAPIPlugin instanceof SkullTextureAPI) {
            try {
                String base64 = SkullTextureAPI.getTexture(entity);

                if (base64 != null && !base64.equals("")) {
                    return base64;
                }
            } catch (NoSuchMethodError ignored) {
            }
        }

        return null;
    }

    public static String getSignature(Entity entity) {
        return getPlayerProperty(entity, property_getSignature);
    }

    public static GameProfile getPlayerGameProfile(Player player) {
        try {
            Object playerObject = player.getClass().getMethod("getHandle").invoke(player);

            if (GAMEPROFILE_METHOD == null) {
                findGameProfileMethod(playerObject);
            }

            if (GAMEPROFILE_METHOD != null && !GAMEPROFILE_METHOD.equals("")) {
                Method gameProfile = playerObject.getClass().getMethod(GAMEPROFILE_METHOD);

                gameProfile.setAccessible(true);

                return (GameProfile) gameProfile.invoke(playerObject);
            }
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException ignored) {
        }

        return null;
    }

    private static void findGameProfileMethod(Object playerObject) {
        for (Method method : playerObject.getClass().getMethods()) {
            if (method.getReturnType().getName().endsWith("GameProfile")) {
                GAMEPROFILE_METHOD = method.getName();

                return;
            }
        }

        GAMEPROFILE_METHOD = "";
    }
}
