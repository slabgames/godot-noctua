package com.slabgames.noctua;

import static java.util.Collections.emptyList;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.os.Bundle;
import android.view.View;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.godotengine.godot.Dictionary;
import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;

import com.noctuagames.sdk.Noctua;

public class GodotNoctua extends GodotPlugin {

    private static final String TAG = GodotNoctua.class.getName();
    private boolean _inited = false;

    public GodotNoctua(Godot godot) {
        super(godot);
    }

    @Override
    public String getPluginName() {
        return "GodotNoctua";
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Called once when the Activity is created.
     * Initialises the Noctua SDK using noctuagg.json from the project root —
     * no manual token or config needed from GDScript.
     */
    @Override
    public View onMainCreate(Activity activity) {
        activity.runOnUiThread(() -> {
            // Single initialisation — reads clientId / gameId from noctuagg.json automatically.
            Noctua.Companion.init(
                activity.getApplicationContext(),
                emptyList()
            );
            _inited = true;
            Log.d(TAG, "Noctua SDK initialized");
        });
        return null;
    }

    @Override
    public void onMainResume() {
        super.onMainResume();
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            if (_inited) {
                Noctua.Companion.onResume();
            }
        });
    }

    @Override
    public void onMainPause() {
        super.onMainPause();
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            if (_inited) {
                Noctua.Companion.onPause();
            }
        });
    }

    @Override
    public void onMainDestroy() {
        super.onMainDestroy();
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            if (_inited) {
                Noctua.Companion.onDestroy();
            }
        });
    }

    // ── Tracking ──────────────────────────────────────────────────────────────

    /**
     * Tracks a named custom event with optional parameters.
     * Maps to Noctua.trackCustomEvent(eventName, payload).
     */
    @UsedByGodot
    public void track_event(final String event, final Dictionary params) {
        if (!_inited) return;
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            Noctua.Companion.trackCustomEvent(event, toSafeMap(params));
            Log.d(TAG, "track_event: " + event);
        });
    }

    /**
     * Tracks an IAP purchase.
     * amount is passed as a String from GDScript and converted to Double here.
     * Maps to Noctua.trackPurchase(orderId, amount, currency, extraPayload).
     */
    @UsedByGodot
    public void track_purchase(final String orderId, final String amount,
                               final String currency, final Dictionary payload) {
        if (!_inited) return;
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            Noctua.Companion.trackPurchase(
                orderId,
                Double.parseDouble(amount),   // native SDK expects Double, not Float
                currency,
                toSafeMap(payload)
            );
            Log.d(TAG, "track_purchase: order=" + orderId + " amount=" + amount + " " + currency);
        });
    }

    /**
     * Tracks ad revenue.
     * revenue is passed as a String from GDScript and converted to Double here.
     * Maps to Noctua.trackAdRevenue(source, revenue, currency, extraPayload).
     */
    @UsedByGodot
    public void track_ad_revenue(final String adSource, final String revenue,
                                 final String currency, final Dictionary params) {
        if (!_inited) return;
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            Noctua.Companion.trackAdRevenue(
                adSource,
                Double.parseDouble(revenue),  // native SDK expects Double, not Float
                currency,
                toSafeMap(params)
            );
            Log.d(TAG, "track_ad_revenue: source=" + adSource + " revenue=" + revenue);
        });
    }

    /**
     * Tracks a custom event that also carries a revenue value.
     * Maps to Noctua.trackCustomEventWithRevenue(eventName, revenue, currency, payload).
     */
    @UsedByGodot
    public void track_custom_event_with_revenue(final String eventName, final String revenue,
                                                final String currency, final Dictionary payload) {
        if (!_inited) return;
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            Noctua.Companion.trackCustomEventWithRevenue(
                eventName,
                Double.parseDouble(revenue),  // native SDK expects Double, not Float
                currency,
                toSafeMap(payload)
            );
            Log.d(TAG, "track_custom_event_with_revenue: " + eventName);
        });
    }

    // ── Session ───────────────────────────────────────────────────────────────

    /**
     * Tags the current session for segmentation in analytics.
     * Maps to Noctua.setSessionTag(tag).
     */
    @UsedByGodot
    public void set_session_tag(final String sessionName) {
        if (!_inited) return;
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            Noctua.Companion.setSessionTag(sessionName);
            Log.d(TAG, "set_session_tag: " + sessionName);
        });
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    @Override
    public void onMainActivityResult(int requestCode, int resultCode, Intent data) {
    }

    /**
     * Converts a Godot Dictionary to a HashMap with safe type coercion.
     * - Double  → Float  (Godot passes all decimals as Double)
     * - Integer → Long
     * - Float, Long, Boolean, String passed through unchanged
     * - Other types serialised via toString()
     */
    private HashMap<String, Object> toSafeMap(Dictionary dict) {
        HashMap<String, Object> map = new HashMap<>();
        if (dict == null) return map;
        for (Object key : dict.keySet()) {
            Object value = dict.get(key);
            String k = String.valueOf(key);
            if (value instanceof Double) {
                map.put(k, ((Double) value).floatValue());
            } else if (value instanceof Integer) {
                map.put(k, ((Integer) value).longValue());
            } else if (value instanceof Float
                    || value instanceof Long
                    || value instanceof Boolean
                    || value instanceof String) {
                map.put(k, value);
            } else if (value != null) {
                map.put(k, value.toString());
            }
        }
        return map;
    }
}
