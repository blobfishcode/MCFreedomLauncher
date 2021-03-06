package net.minecraft.launcher;

import java.net.URI;
import java.net.URISyntaxException;

public class LauncherConstants {
    public static final String VERSION_NAME = "1.2.5";
    public static final int VERSION_NUMERIC = 8;
    public static final URI URL_REGISTER = constantURI("https://account.mojang.com/register");
    public static final String URL_DOWNLOAD_BASE = "https://s3.amazonaws.com/Minecraft.Download/";
    public static final String URL_RESOURCE_BASE = "https://s3.amazonaws.com/Minecraft.Resources/";
    public static final String URL_BLOG = "http://mcupdate.tumblr.com";
    public static final String URL_SUPPORT = "http://help.mojang.com/?ref=launcher";
    public static final String URL_STATUS_CHECKER = "http://status.mojang.com/check";
    public static final int UNVERSIONED_BOOTSTRAP_VERSION = 0;
    public static final int MINIMUM_BOOTSTRAP_SUPPORTED = 4;
    public static final String URL_BOOTSTRAP_DOWNLOAD = "https://mojang.com/2013/06/minecraft-1-6-pre-release/";
    public static final String[] BOOTSTRAP_OUT_OF_DATE_BUTTONS = {"Go to URL", "Close"};

    public static final String[] CONFIRM_PROFILE_DELETION_OPTIONS = {"Delete profile", "Cancel"};

    public static final URI URL_FORGOT_USERNAME = constantURI("http://help.mojang.com/customer/portal/articles/1233873?ref=launcher");
    public static final URI URL_FORGOT_PASSWORD_MINECRAFT = constantURI("http://help.mojang.com/customer/portal/articles/329524-change-or-forgot-password?ref=launcher");
    public static final URI URL_FORGOT_MIGRATED_EMAIL = constantURI("http://help.mojang.com/customer/portal/articles/1205055-minecraft-launcher-error---migrated-account?ref=launcher");
    public static final int MAX_NATIVES_LIFE_IN_SECONDS = 3600;
    public static final String DEFAULT_VERSION_INCOMPATIBILITY_REASON = "This version is incompatible with your computer. Please try another one by going into Edit Profile and selecting one through the dropdown. Sorry!";

    public static URI constantURI(String input) {
        try {
            return new URI(input);
        } catch (URISyntaxException e) {
            throw new Error(e);
        }
    }
}
