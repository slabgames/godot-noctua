package com.slabgames.noctua;

import static java.util.Collections.emptyList;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import java.util.HashMap;
import java.util.Objects;

import org.godotengine.godot.Dictionary;
import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;

import com.noctuagames.sdk.Noctua;
import com.noctuagames.sdk.models.NoctuaBillingConfig;

/**
 * Godot Android plugin bridging GDScript to the Noctua SDK.
 *
 * Noctua is a Kotlin `object` singleton — all methods are accessed via
 * Noctua.INSTANCE (not Noctua.Companion).
 *
 * The SDK initialises itself automatically in onMainCreate() by reading
 * noctuagg.json from the project root. No token or config is required
 * from GDScript.
 */
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

    @Override
    public View onMainCreate(Activity activity) {
        activity.runOnUiThread(() -> {
            // Single initialisation — reads clientId/gameId from noctuagg.json.
            // Noctua is a Kotlin object singleton: use INSTANCE, not Companion.
            Noctua.INSTANCE.init(
                activity.getApplicationContext(),
                emptyList(),
                new NoctuaBillingConfig()
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
            if (_inited) Noctua.INSTANCE.onResume();
        });
    }

    @Override
    public void onMainPause() {
        super.onMainPause();
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            if (_inited) Noctua.INSTANCE.onPause();
        });
    }

    @Override
    public void onMainDestroy() {
        super.onMainDestroy();
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            if (_inited) Noctua.INSTANCE.onDestroy();
        });
    }

    // ── Event Tracking ────────────────────────────────────────────────────────

    /** Tracks a named custom event with optional parameters. */
    @UsedByGodot
    public void track_event(final String event, final Dictionary params) {
        if (!_inited) return;
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            Noctua.INSTANCE.trackCustomEvent(event, toSafeMap(params));
            Log.d(TAG, "track_event: " + event);
        });
    }

    /**
     * Tracks an IAP purchase.
     * amount is a String from GDScript, converted to Double to match native SDK.
     */
    @UsedByGodot
    public void track_purchase(final String orderId, final String amount,
                               final String currency, final Dictionary payload) {
        if (!_inited) return;
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            Noctua.INSTANCE.trackPurchase(
                orderId,
                Double.parseDouble(amount),
                currency,
                toSafeMap(payload)
            );
            Log.d(TAG, "track_purchase: order=" + orderId + " " + amount + " " + currency);
        });
    }

    /**
     * Tracks ad revenue.
     * revenue is a String from GDScript, converted to Double to match native SDK.
     */
    @UsedByGodot
    public void track_ad_revenue(final String adSource, final String revenue,
                                 final String currency, final Dictionary params) {
        if (!_inited) return;
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            Noctua.INSTANCE.trackAdRevenue(
                adSource,
                Double.parseDouble(revenue),
                currency,
                toSafeMap(params)
            );
            Log.d(TAG, "track_ad_revenue: " + adSource + " " + revenue);
        });
    }

    /** Tracks a custom event that also carries a revenue value. */
    @UsedByGodot
    public void track_custom_event_with_revenue(final String eventName, final String revenue,
                                                final String currency, final Dictionary payload) {
        if (!_inited) return;
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            Noctua.INSTANCE.trackCustomEventWithRevenue(
                eventName,
                Double.parseDouble(revenue),
                currency,
                toSafeMap(payload)
            );
            Log.d(TAG, "track_custom_event_with_revenue: " + eventName);
        });
    }

    // ── Session ───────────────────────────────────────────────────────────────

    /** Tags the current session for segmentation in analytics. */
    @UsedByGodot
    public void set_session_tag(final String sessionName) {
        if (!_inited) return;
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            Noctua.INSTANCE.setSessionTag(sessionName);
            Log.d(TAG, "set_session_tag: " + sessionName);
        });
    }

    /** Returns the current session tag. */
    @UsedByGodot
    public String get_session_tag() {
        if (!_inited) return "";
        return Noctua.INSTANCE.getSessionTag();
    }

    /**
     * Attaches extra key-value metadata to every subsequent session event.
     * params is a flat String→String Dictionary from GDScript.
     */
    @UsedByGodot
    public void set_session_extra_params(final Dictionary params) {
        if (!_inited) return;
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            Noctua.INSTANCE.setSessionExtraParams(toSafeMap(params));
            Log.d(TAG, "set_session_extra_params");
        });
    }

    // ── Experiments ───────────────────────────────────────────────────────────

    /** Sets the A/B experiment bucket for this session. */
    @UsedByGodot
    public void set_experiment(final String experiment) {
        if (!_inited) return;
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            Noctua.INSTANCE.setExperiment(experiment);
            Log.d(TAG, "set_experiment: " + experiment);
        });
    }

    /** Returns the current experiment bucket. */
    @UsedByGodot
    public String get_experiment() {
        if (!_inited) return "";
        return Noctua.INSTANCE.getExperiment();
    }

    /** Sets a general experiment value by key. */
    @UsedByGodot
    public void set_general_experiment(final String experiment) {
        if (!_inited) return;
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            Noctua.INSTANCE.setGeneralExperiment(experiment);
            Log.d(TAG, "set_general_experiment: " + experiment);
        });
    }

    /** Gets a general experiment value by key. */
    @UsedByGodot
    public String get_general_experiment(final String experimentKey) {
        if (!_inited) return "";
        return Noctua.INSTANCE.getGeneralExperiment(experimentKey);
    }

    // ── Network state ─────────────────────────────────────────────────────────

    /** Call when the device comes back online. */
    @UsedByGodot
    public void on_online() {
        if (!_inited) return;
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            Noctua.INSTANCE.onOnline();
            Log.d(TAG, "on_online");
        });
    }

    /** Call when the device goes offline. */
    @UsedByGodot
    public void on_offline() {
        if (!_inited) return;
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            Noctua.INSTANCE.onOffline();
            Log.d(TAG, "on_offline");
        });
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    @Override
    public void onMainActivityResult(int requestCode, int resultCode, Intent data) {
    }

    /**
     * Converts a Godot Dictionary to a safe HashMap<String, Object>:
     *  - Double  → Float  (Godot sends all decimals as Double)
     *  - Integer → Long
     *  - Float, Long, Boolean, String passed through unchanged
     *  - Other types serialised via toString()
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
