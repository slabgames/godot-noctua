package com.slabgames.noctua;

import static java.util.Collections.emptyList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.Bundle;
import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Objects;

import org.godotengine.godot.plugin.UsedByGodot;
import com.noctuagames.sdk.Noctua;

import android.app.Application.ActivityLifecycleCallbacks;



import org.godotengine.godot.Dictionary;
import org.godotengine.godot.Godot;
import org.godotengine.godot.GodotLib;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;

public class GodotNoctua extends GodotPlugin {

    private final String TAG = GodotNoctua.class.getName();
    private Noctua noctuaSDK;

    public GodotNoctua(Godot godot) {
        super(godot);
    }

    @Override
    public String getPluginName() {
        return "GodotNoctua";
    }


    /*
    @Override
    public Set<SignalInfo> getPluginSignals() {
        return Collections.singleton(loggedInSignal);
    }
    */

    @Override
    public View onMainCreate(Activity activity) {

        return null;
    }


    // Public methods
    @UsedByGodot
//    public void init(final String token, final boolean ProductionMode) {
    public void init(final String token, final boolean ProductionMode) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {

//                String appToken = token;
//                String environment;

                List<String> publishedApps;
                noctuaSDK = new Noctua((Context) Objects.requireNonNull(getActivity()), publishedApps = emptyList());
//                if (ProductionMode == true) {
//                } else {
//                }


                Log.d(TAG, "Adjust plugin inited on Java");
            }
        });
    }

    private static final class NoctuaLifecycleCallbacks implements ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(Activity activity,
                                      Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }

        //...
    }

    @UsedByGodot
    public void track_purchase(final String event, final Dictionary params)
    {
        // Equivalent to val map = mutableMapOf<String, String>() in Kotlin
        Map<String, Object> mutableMap = new HashMap<>();

        // Equivalent to map["key1"] = "value1" in Kotlin
        mutableMap.put("name", "Alice");
        mutableMap.put("type", "User");
        if(noctuaSDK!= null)
        {
            noctuaSDK.trackPurchase(
                    "order-123",
                    9.99,
                    "USD",
                    params
                    //extraPayload = mutableMapOf("sku" to "premium_upgrade")
            );
        }
    }

    @UsedByGodot
    public void track_event(final String event, final Dictionary params)
    {

        noctuaSDK.trackCustomEvent(event, params);
    }

    @UsedByGodot
    public void track_revenue(final String adSource, final String revenue, final String currency, final Dictionary params)
    {
        noctuaSDK.trackAdRevenue(
//                "admob_sdk",
                adSource,
                Double.parseDouble(revenue),
                currency,
                params
        );
    }




    // Internal methods

    @Override
    public void onMainActivityResult (int requestCode, int resultCode, Intent data)
    {
    }
}
