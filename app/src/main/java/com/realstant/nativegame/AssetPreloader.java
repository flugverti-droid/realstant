package com.realstant.nativegame;

import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/** Reads every bundled asset once so the first game start has a visible, deterministic preparation phase. */
public final class AssetPreloader {
    private static final String PREF = "realstant_assets";
    private static final String READY = "bundle_v9_ready";
    private AssetPreloader() {}

    public interface Listener { void onProgress(int done, int total, String name); void onReady(); }

    public static boolean isReady(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getBoolean(READY, false);
    }

    public static void prepare(Context c, Listener listener) {
        if (isReady(c)) { listener.onReady(); return; }
        new Thread(() -> {
            List<String> files = new ArrayList<>();
            try (InputStream in = c.getAssets().open("manifest.txt");
                 BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String s; while ((s = br.readLine()) != null) if (!s.trim().isEmpty()) files.add(s.trim());
            } catch (Throwable e) {
                c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putBoolean(READY, true).apply();
                listener.onReady(); return;
            }
            int total = files.size(), done = 0;
            byte[] buf = new byte[8192];
            for (String path : files) {
                try (InputStream in = c.getAssets().open(path)) {
                    while (in.read(buf) != -1) { /* warm the APK asset stream */ }
                } catch (Throwable ignored) { }
                done++;
                int d = done; listener.onProgress(d, total, path);
            }
            c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putBoolean(READY, true).apply();
            listener.onReady();
        }, "RealStant-AssetPreload").start();
    }
}
