package org.telegram.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

public class LauncherIconController {

    private static final String[] ALL_OLD_ALIASES = {
        "AquaIcon", "PremiumIcon", "NoxIcon", "MerioIcon",
        "RainbowIcon", "OldSchoolIcon", "MusheenIcon", "SpaceIcon", "CloudIcon",
        "NeonIcon", "VintageIcon"
    };

    public static void tryFixLauncherIconIfNeeded() {
        Context ctx = ApplicationLoader.applicationContext;
        if (ctx == null) return;
        PackageManager pm = ctx.getPackageManager();
        String pkg = ctx.getPackageName();

        for (String alias : ALL_OLD_ALIASES) {
            try {
                pm.setComponentEnabledSetting(
                    new ComponentName(pkg, "org.telegram.messenger." + alias),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                );
            } catch (Throwable ignored) {}
        }

        boolean anyEnabled = false;
        for (LauncherIcon icon : LauncherIcon.values()) {
            int state = pm.getComponentEnabledSetting(icon.getComponentName(ctx));
            if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                anyEnabled = true;
                break;
            }
        }

        if (!anyEnabled) {
            setIcon(LauncherIcon.DEFAULT);
        }
    }

    public static boolean isEnabled(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        if (ctx == null) return true;
        int i = ctx.getPackageManager().getComponentEnabledSetting(icon.getComponentName(ctx));
        return i == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || (i == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && icon == LauncherIcon.DEFAULT);
    }

    public static void setIcon(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        if (ctx == null) return;
        PackageManager pm = ctx.getPackageManager();
        String pkg = ctx.getPackageName();

        for (String alias : ALL_OLD_ALIASES) {
            try {
                pm.setComponentEnabledSetting(
                    new ComponentName(pkg, "org.telegram.messenger." + alias),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                );
            } catch (Throwable ignored) {}
        }

        for (LauncherIcon i : LauncherIcon.values()) {
            if (i != icon) {
                try {
                    pm.setComponentEnabledSetting(
                        i.getComponentName(ctx),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    );
                } catch (Throwable ignored) {}
            }
        }

        try {
            pm.setComponentEnabledSetting(
                icon.getComponentName(ctx),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            );
        } catch (Throwable ignored) {}
    }

    public enum LauncherIcon {
        DEFAULT("DefaultIcon", R.drawable.ic_launcher_adaptive_bg, R.drawable.ic_launcher_adaptive_fg, R.string.AppIconDefault),
        DARK("TurboIcon", R.drawable.ic_launcher_adaptive_dark_bg, R.drawable.ic_launcher_adaptive_dark_fg, R.string.AppIconDark),
        MONO("MaterialIcon", R.drawable.ic_launcher_adaptive_mono_bg, R.drawable.ic_launcher_adaptive_mono_fg, R.string.AppIconMaterial);

        public final String key;
        public final int background;
        public final int foreground;
        public final int title;
        public final boolean premium;

        private ComponentName componentName;

        public ComponentName getComponentName(Context ctx) {
            if (componentName == null) {
                componentName = new ComponentName(ctx.getPackageName(), "org.telegram.messenger." + key);
            }
            return componentName;
        }

        LauncherIcon(String key, int background, int foreground, int title) {
            this(key, background, foreground, title, false);
        }

        LauncherIcon(String key, int background, int foreground, int title, boolean premium) {
            this.key = key;
            this.background = background;
            this.foreground = foreground;
            this.title = title;
            this.premium = premium;
        }
    }
}
