extends Node

var _noctua = null

func _ready():
    if Engine.has_singleton("GodotNoctua"):
        _noctua = Engine.get_singleton("GodotNoctua")
    else:
        push_warning('Noctua plugin not found!')
    if ProjectSettings.has_setting('Noctua/AppToken'):
        var token = ProjectSettings.get_setting('Noctua/AppToken')
        init(token, not OS.is_debug_build())
    else:
        push_error('You should set Noctua/AppToken to SDK initialization')

func init(token: String, production := false) -> void:
    if _noctua != null:
        _noctua.init(token, production)
        print('Noctua plugin inited!')
        
func track_event(event: String) -> void:
    if _noctua != null:
        _noctua.trackEvent(event)

func track_revenue(event: String, revenue: float, currency := 'USD') -> void:
    if _noctua != null:
        _noctua.trackRevenue(event, revenue, currency)

