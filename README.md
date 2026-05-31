# Lotka-Volterra Modell

## Projektuebersicht

Dieses Repository enthaelt Versuche zur Implementierung des klassischen Raeuber-Beute-Modells nach Lotka-Volterra in 
verschiedenen Programmiersprachen. Das Ziel ist es, die mathematischen Differentialgleichungen zu loesen 
und die Populationsschwankungen von Beutetieren (z.B. Hasen) und Raeubern (z.B. Woelfe) ueber die Zeit zu visualisieren.

Das Projekt dient dem Vergleich der jeweiligen Staerken, Bibliotheken und Herangehensweisen 
verschiedener Sprachen im Bereich der wissenschaftlichen Berechnung und Datenvisualisierung.

## Das Modell

Das Lotka-Volterra-Modell beschreibt die Dynamik biologischer Systeme, in denen zwei Arten interagieren:

1. Eine Beutepopulation, die exponentiell waechst, wenn keine Raeuber vorhanden sind.
2. Eine Raeuberpopulation, die von der Verfuegbarkeit der Beute abhaengt.

Die Gleichungen lauten:
dH/dt = alpha * H - beta * H * W dW/dt = delta * beta * H * W - gamma * W
