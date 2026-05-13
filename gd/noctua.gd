extends Node

# Noctua SDK — GDScript bridge
# Auto-registered as the "adjust" autoload singleton via project.godot.
#
# The SDK initialises itself automatically in GodotNoctua.onMainCreate()
# by reading noctuagg.json from the project root — no manual token needed.

var _noctua = null

func _ready() -> void:
	if Engine.has_singleton("GodotNoctua"):
		_noctua = Engine.get_singleton("GodotNoctua")
		print("Noctua: native SDK connected")
	else:
		push_warning("Noctua plugin not found! Running without native SDK.")

# ── Event Tracking ────────────────────────────────────────────────────────────

func track_event(event: String) -> void:
	if _noctua != null:
		_noctua.track_event(event, {})

func track_event_with_params(event: String, params: Dictionary) -> void:
	if _noctua != null:
		_noctua.track_event(event, params)

# ── Revenue Tracking ──────────────────────────────────────────────────────────

## Tracks a named revenue event (maps to trackCustomEventWithRevenue on native).
func track_revenue(event: String, revenue: float, currency := "USD") -> void:
	if _noctua != null:
		_noctua.track_custom_event_with_revenue(event, str(revenue), currency, {})

func track_purchase(order_id: String, amount: String, currency: String, payload: Dictionary) -> void:
	if _noctua != null:
		_noctua.track_purchase(order_id, amount, currency, payload)

func track_ad_revenue(ad_source: String, revenue: String, currency: String, params: Dictionary) -> void:
	if _noctua != null:
		_noctua.track_ad_revenue(ad_source, revenue, currency, params)

func track_custom_event_with_revenue(event_name: String, revenue: String, currency: String, payload: Dictionary) -> void:
	if _noctua != null:
		_noctua.track_custom_event_with_revenue(event_name, revenue, currency, payload)

# ── Session ───────────────────────────────────────────────────────────────────

func set_session_tag(session_name: String) -> void:
	if _noctua != null:
		_noctua.set_session_tag(session_name)

func get_session_tag() -> String:
	if _noctua != null:
		return _noctua.get_session_tag()
	return ""

func set_session_extra_params(params: Dictionary) -> void:
	if _noctua != null:
		_noctua.set_session_extra_params(params)

# ── Experiments ───────────────────────────────────────────────────────────────

func set_experiment(experiment: String) -> void:
	if _noctua != null:
		_noctua.set_experiment(experiment)

func get_experiment() -> String:
	if _noctua != null:
		return _noctua.get_experiment()
	return ""

func set_general_experiment(experiment: String) -> void:
	if _noctua != null:
		_noctua.set_general_experiment(experiment)

func get_general_experiment(key: String) -> String:
	if _noctua != null:
		return _noctua.get_general_experiment(key)
	return ""

# ── Network state ─────────────────────────────────────────────────────────────

func on_online() -> void:
	if _noctua != null:
		_noctua.on_online()

func on_offline() -> void:
	if _noctua != null:
		_noctua.on_offline()
