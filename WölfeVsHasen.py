import numpy as np
import matplotlib.pyplot as plt
from scipy.integrate import odeint
from matplotlib.animation import FuncAnimation
from matplotlib.widgets import Button, Slider

# --- Lotka-Volterra Modell ---
def predator_prey_model(y, t, alpha, beta, gamma, delta):
    H, W = y
    dHdt = alpha * H - beta * H * W
    dWdt = delta * beta * H * W - gamma * W
    return [dHdt, dWdt]

# --- Parameter ---
alpha = 1.0
beta  = 0.1
delta = 0.075
gamma = 1.5

# --- Anfangsbedingungen & Zeit ---
H0, W0 = 40, 10
t_max = 80
num_points = 4000
t = np.linspace(0, t_max, num_points)

# --- Lösung berechnen ---
solution = odeint(predator_prey_model, [H0, W0], t, args=(alpha, beta, gamma, delta))
H_sol = solution[:, 0]
W_sol = solution[:, 1]

# --- Plot Setup ---
fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 6))
fig.suptitle("Interaktives Lotka-Volterra Modell", fontsize=16, fontweight='bold')

plt.subplots_adjust(bottom=0.25)

# --- Diagramme ---
ax1.set_xlim(0, t_max)
ax1.set_ylim(0, max(max(H_sol), max(W_sol)) * 1.1)
ax1.set_title("Population über die Zeit")
ax1.set_xlabel("Zeit")
ax1.set_ylabel("Anzahl Tiere")
ax1.grid(True, alpha=0.3)

line_h, = ax1.plot([], [], 'b-', label='Hasen', linewidth=2)
line_w, = ax1.plot([], [], 'r-', label='Wölfe', linewidth=2)
dot_h, = ax1.plot([], [], 'bo', markersize=8)
dot_w, = ax1.plot([], [], 'ro', markersize=8)
ax1.legend(loc='upper right', fontsize=12)

ax2.set_xlim(0, max(H_sol) * 1.1)
ax2.set_ylim(0, max(W_sol) * 1.1)
ax2.set_title("Phasenraum (Wölfe vs. Hasen)")
ax2.set_xlabel("Anzahl Hasen")
ax2.set_ylabel("Anzahl Wölfe")
ax2.grid(True, alpha=0.3)

line_phase, = ax2.plot([], [], 'g-', linewidth=1.5, alpha=0.7)
dot_phase, = ax2.plot([], [], 'ko', markersize=10, zorder=5)

info_text = ax1.text(0.02, 0.95, '', transform=ax1.transAxes,
                     fontsize=11, verticalalignment='top',
                     bbox=dict(boxstyle='round', facecolor='wheat', alpha=0.8))

# Globale Variablen
current_frame = 0
is_playing = True  # Startet automatisch
steps_per_update = 5

# --- Update Funktion ---
def update(frame):
    global current_frame, is_playing, steps_per_update
    
    if is_playing:
        current_frame += steps_per_update
        
        if current_frame >= len(t):
            current_frame = 0  # Loop
        
        i = current_frame
        
        # Daten aktualisieren
        line_h.set_data(t[:i], H_sol[:i])
        line_w.set_data(t[:i], W_sol[:i])
        dot_h.set_data([t[i]], [H_sol[i]])
        dot_w.set_data([t[i]], [W_sol[i]])
        
        line_phase.set_data(H_sol[:i], W_sol[:i])
        dot_phase.set_data([H_sol[i]], [W_sol[i]])
        
        # Text aktualisieren
        info_text.set_text(
            f'Zeit: {t[i]:.1f}\n'
            f'Hasen: {H_sol[i]:.0f}  |  Wölfe: {W_sol[i]:.0f}'
        )
    
    return line_h, line_w, dot_h, dot_w, line_phase, dot_phase, info_text

# --- Widget Callbacks ---

def on_play_click(event):
    global is_playing
    is_playing = not is_playing
    
    if is_playing:
        play_button.label.set_text("Pause ⏸")
    else:
        play_button.label.set_text("Play ▶")

def on_speed_change(val):
    global steps_per_update
    steps_per_update = max(1, int(val * 5))
    speed_label.set_text(f'Speed: {val:.1f}x')

def on_reset_click(event):
    global current_frame, is_playing
    
    # 1. Spiel stoppen
    is_playing = False
    play_button.label.set_text("Play ▶")
    
    # 2. Frame zurücksetzen
    current_frame = 0
    
    # 3. Plot komplett zurücksetzen (leere Linien)
    line_h.set_data([], [])
    line_w.set_data([], [])
    dot_h.set_data([], [])
    dot_w.set_data([], [])
    line_phase.set_data([], [])
    dot_phase.set_data([], [])
    
    # 4. Text zurücksetzen
    info_text.set_text('Zeit: 0.0\nHasen: 0 | Wölfe: 0')
    
    # 5. Neu zeichnen
    fig.canvas.draw_idle()

# --- Widgets ---
ax_play = plt.axes([0.75, 0.05, 0.1, 0.05])
play_button = Button(ax_play, 'Pause ⏸', color='lightgoldenrodyellow', hovercolor='yellowgreen')
play_button.on_clicked(on_play_click)

ax_reset = plt.axes([0.60, 0.05, 0.1, 0.05])
reset_button = Button(ax_reset, 'Reset ↺', color='lightcoral', hovercolor='salmon')
reset_button.on_clicked(on_reset_click)

ax_speed = plt.axes([0.25, 0.05, 0.3, 0.05])
speed_slider = Slider(ax_speed, 'Speed', 0.1, 5.0, valinit=1.0, valstep=0.1)
speed_label = ax_speed.text(0.5, 1.1, 'Speed: 1.0x', transform=ax_speed.transAxes, ha='center')
speed_slider.on_changed(on_speed_change)

# Animation starten
anim = FuncAnimation(fig, update, frames=len(t), interval=20, blit=True, repeat=True)

# Initialer Zustand (Startet bei Frame 0, aber is_playing=True, also läuft es sofort)
# Wir setzen initial den Text auf 0, damit es sauber aussieht
info_text.set_text('Zeit: 0.0\nHasen: 0 | Wölfe: 0')
fig.canvas.draw_idle()

plt.show()