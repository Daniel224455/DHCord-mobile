/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.dhcord.updater;

import android.content.Context;

import com.dhcord.*;
import com.dhcord.settings.AliucordPageKt;
import com.dhcord.utils.ReflectUtils;

import java.io.File;
import java.io.IOException;

public class Updater {
    /**
     * Compares two versions of a plugin to determine whether it is outdated
     *
     * @param plugin     The name of the plugin
     * @param version    The local version of the plugin
     * @param newVersion The latest version of the plugin
     * @return Whether newVersion is newer than version
     */
    public static boolean isOutdated(String plugin, String version, String newVersion) {
        try {
            String[] versions = version.split("\\.");
            String[] newVersions = newVersion.split("\\.");
            int len = versions.length;
            if (len > newVersions.length) return false;
            for (int i = 0; i < len; i++) {
                int newInt = Integer.parseInt(newVersions[i]);
                int oldInt = Integer.parseInt(versions[i]);
                if (newInt > oldInt) return true;
                if (newInt < oldInt) return false;
            }
        } catch (NullPointerException | NumberFormatException th) {
            PluginUpdater.logger.error(String.format("Failed to check updates for plugin %s due to an invalid updater/manifest version", plugin), th);
        }

        return false;
    }

    private static class AliucordData {
        public String aliucordHash;
        public int versionCode;
    }

    private static Boolean isAliucordOutdated = null;
    private static Boolean isDiscordOutdated = null;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean fetchDHCordData() {
        try (var req = new Http.Request("https://raw.githubusercontent.com/Daniel224455/DHCord-mobile/main/builds/data.json")) {
            var res = req.execute().json(DHCordData.class);
            isDHCordOutdated = !BuildConfig.GIT_REVISION.equals(res.aliucordHash);
            isDiscordOutdated = Constants.DISCORD_VERSION < res.versionCode;
            return true;
        } catch (IOException ex) {
            PluginUpdater.logger.error("Failed to check updates for DHCord", ex);
            return false;
        }
    }

    /**
     * Determines whether Aliucord is outdated
     *
     * @return Whether latest remote Aliucord commit hash is newer than the installed one
     */
    public static boolean isDHCordOutdated() {
        if (usingDexFromStorage() || isUpdaterDisabled()) return false;
        if (isDHCordOutdated == null && !fetchDHCordData()) return false;
        return isDHCordOutdated;
    }

    /**
     * Determines whether the Base Discord is outdated
     *
     * @return Whether Aliucord's currently supported Discord version is newer than the installed one
     */
    public static boolean isDiscordOutdated() {
        if (isUpdaterDisabled()) return false;
        if (isDiscordOutdated == null && !fetchDHCordData()) return false;
        return isDiscordOutdated;
    }

    /**
     * Replaces the local Aliucord version with the latest from Github
     *
     * @param ctx Context
     * @throws Throwable If an error occurred
     */
    public static void updateAliucord(Context ctx) throws Throwable {
        Class<?> c;
        try {
            c = Class.forName("com.dhcord.injector.InjectorKt");
        } catch (ClassNotFoundException e) {
            c = Class.forName("com.dhcord.injector.Injector");
        }
        ReflectUtils.invokeMethod(
            c,
            (Object) null,
            "downloadLatestDHCordDex",
            new File(ctx.getCodeCacheDir(), "DHCord.zip")
        );
    }

    /**
     * Determines whether the updater is disabled
     *
     * @return Whether preference "disableAliucordUpdater" is set to true
     */
    public static boolean isUpdaterDisabled() {
        return Main.settings.getBool("disableDHCordUpdater", true);
    }

    /**
     * Determines whether the Aliucord dex is being loaded from storage
     *
     * @return Whether preference {@link AliucordPageKt#ALIUCORD_FROM_STORAGE_KEY} is set to true
     */
    public static boolean usingDexFromStorage() {
        return Main.settings.getBool(DHCordPageKt.ALIUCORD_FROM_STORAGE_KEY, false);
    }
}
