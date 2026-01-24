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

    private boolean _inited = false;


    /*
    @Override
    public Set<SignalInfo> getPluginSignals() {
        return Collections.singleton(loggedInSignal);
    }
    */

    @Override
    public View onMainCreate(Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<String> publishedApps= emptyList();
                Noctua.Companion.initNoctuaApp(activity,publishedApps);
            }
        });

        return null;
    }

    @Override
    public void onMainResume() {
        super.onMainResume();
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(_inited)
                    Noctua.Companion.onResume();
            }
        });
    }

    @Override
    public void onMainPause() {
        super.onMainPause();
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(_inited)
                    Noctua.Companion.onPause();
            }
        });
    }


    @Override
    public void onMainDestroy() {
        super.onMainDestroy();
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(_inited)
                    Noctua.Companion.onDestroy();
            }
        });
    }



    // Public methods
    @UsedByGodot
    public void init() {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {


                List<String> publishedApps= emptyList();
//                noctuaSDK = new Noctua((Context) Objects.requireNonNull(getActivity()), publishedApps );

                Noctua.Companion.init(Objects.requireNonNull(getActivity()),publishedApps);

                Log.d(TAG, "Noctua plugin inited on Java");
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
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Noctua.Companion.onResume();
                }
            });

        }

        @Override
        public void onActivityPaused(Activity activity) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Noctua.Companion.onPause();
                }
            });

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
    public void track_purchase(final String orderId, final String amount, final String currency, final Dictionary payload)
    {
        if(!_inited)
            return;
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
//            if(noctuaSDK!= null)
//            {
                Noctua.Companion.trackPurchase(
                        orderId,
                        Float.parseFloat(amount),
                        currency,
                        toSafeMap(payload)
                );
                Log.d(TAG, "Noctua track purchase called");
//            }
        }
    });
    }

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
            } else if (
                    value instanceof Float ||
                    value instanceof Long ||
                    value instanceof Boolean ||
                    value instanceof String
            ) {
                map.put(k, value);
            } else if (value != null) {
                map.put(k, value.toString());
            }
        }
        return map;
    }

    private HashMap<String, Object> toMap(Dictionary dict) {
        HashMap<String, Object> map = new HashMap<>();
        for (Object key : dict.keySet()) {
            map.put(String.valueOf(key), dict.get(key));
        }
        return map;
    }


    @UsedByGodot
    public void track_event(final String event, final Dictionary params)
    {
        if(!_inited)
            return;
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if(noctuaSDK!= null) {
                    Noctua.Companion.trackCustomEvent(event, toSafeMap(params));
                    Log.d(TAG, "Noctua track event "+event + " called");
//                }
            }
        });


    }

    @UsedByGodot
    public void track_ad_revenue(final String adSource, final String revenue, final String currency, final Dictionary params)
    {
        if(!_inited)
            return;
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {

//                if(noctuaSDK!= null) {
                    Noctua.Companion.trackAdRevenue(
                            adSource,
                            Float.parseFloat(revenue),
                            currency,
                            toSafeMap(params)
                    );
                    Log.d(TAG, "Noctua track ad revenue called. From : " + adSource);
//                }
            }
        });


    }

    @UsedByGodot
    public void track_custom_event_with_revenue(final String eventName, final String revenue, final String currency, final Dictionary payload)
    {
        if(!_inited)
            return;
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if (noctuaSDK!=null)
//                {
                    Noctua.Companion.trackCustomEventWithRevenue(eventName,Float.parseFloat(revenue), currency,toSafeMap(payload));
                    Log.d(TAG, "Noctua track custom event called. Event = "+eventName);
//                }
            }
        });
    }

    @UsedByGodot
    public void set_session_tag(final String sessionName)
    {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (_inited){
                    Noctua.Companion.setSessionTag(sessionName);
                    Log.d(TAG, "Noctua set session tag = "+sessionName);
                }

            }
        });

    }



    // Internal methods

    @Override
    public void onMainActivityResult (int requestCode, int resultCode, Intent data)
    {
    }
}
