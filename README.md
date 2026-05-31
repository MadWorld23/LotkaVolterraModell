# Lotka-Volterra Modell

## Projektübersicht

Dieses Repository enthaelt Versuche zur Implementierung des klassischen Räuber-Beute-Modells nach Lotka-Volterra in 
verschiedenen Programmiersprachen. Das Ziel ist es, die mathematischen Differentialgleichungen zu lösen 
und die Populationsschwankungen von Beutetieren (z.B. Hasen) und Räubern (z.B. Wölfe) über die Zeit zu visualisieren.

Das Projekt dient dem Vergleich der jeweiligen Stärken, Bibliotheken und Herangehensweisen 
verschiedener Sprachen im Bereich der wissenschaftlichen Berechnung und Datenvisualisierung.

## Das Modell

Das Lotka-Volterra-Modell beschreibt die Dynamik biologischer Systeme, in denen zwei Arten interagieren:

1. Eine Beutepopulation, die exponentiell wächst, wenn keine Räuber vorhanden sind.
2. Eine Räuberpopulation, die von der Verfügbarkeit der Beute abhängt.

Die Gleichungen lauten:

dH/dt = α * H - β * H * W  
dW/dt = δ * β * H * W - γ * W
