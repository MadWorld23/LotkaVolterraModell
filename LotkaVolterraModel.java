import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class LotkaVolterraModel extends JFrame {
    
    // Modell-Parameter
    private double alpha = 1.0;
    private double beta = 0.1;
    private double delta = 0.075;
    private double gamma = 1.5;
    
    // Anfangsbedingungen
    private double H0 = 40;
    private double W0 = 10;
    private double tMax = 80;
    private int numPoints = 4000;
    
    // Lösungsdaten
    private List<Double> time = new ArrayList<>();
    private List<Double> hares = new ArrayList<>();
    private List<Double> wolves = new ArrayList<>();
    
    // Animationszustand
    private int currentFrame = 0;
    private boolean isPlaying = true;
    private int stepsPerUpdate = 5;
    
    // UI-Komponenten
    private TimePlotPanel timePanel;
    private PhasePlotPanel phasePanel;
    private JLabel infoLabel;
    private JButton playButton;
    private JButton resetButton;
    private JSlider speedSlider;
    private JLabel speedLabel;
    
    private Timer animationTimer;
    
    public LotkaVolterraModel() {
        setTitle("Interaktives Lotka-Volterra Modell (RK4)");
        setSize(1250, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Berechnung der Lösung mit RK4
        calculateSolutionRK4();
        
        // Panels erstellen
        timePanel = new TimePlotPanel();
        phasePanel = new PhasePlotPanel();
        
        // Info-Label
        infoLabel = new JLabel("Zeit: 0.0\nHasen: 0  |  Wölfe: 0");
        infoLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        infoLabel.setBackground(new Color(245, 222, 179));
        infoLabel.setOpaque(true);
        
        // Buttons
        playButton = new JButton("Pause ⏸");
        playButton.addActionListener(e -> togglePlay());
        
        resetButton = new JButton("Reset ↺");
        resetButton.addActionListener(e -> reset());
        
        // Speed Slider (0.1 bis 5.0 in 0.1 Schritten)
        speedSlider = new JSlider(JSlider.HORIZONTAL, 1, 50, 5);
        speedSlider.setMajorTickSpacing(10);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(false);
        
        speedLabel = new JLabel("Speed: 1.0x");
        speedLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        speedSlider.addChangeListener(e -> {
            int sliderValue = speedSlider.getValue();
            double speed = sliderValue / 10.0; 
            stepsPerUpdate = (int)(speed * 5); 
            speedLabel.setText(String.format("Speed: %.1fx", speed));
        });
        
        // Layout
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel topControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        topControls.add(speedLabel);
        topControls.add(Box.createHorizontalStrut(10));
        topControls.add(speedSlider);
        
        JPanel bottomControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        bottomControls.add(resetButton);
        bottomControls.add(playButton);
        
        controlPanel.add(topControls, BorderLayout.NORTH);
        controlPanel.add(infoLabel, BorderLayout.CENTER);
        controlPanel.add(bottomControls, BorderLayout.SOUTH);
        
        JPanel chartPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        chartPanel.add(timePanel);
        chartPanel.add(phasePanel);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(chartPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        
        // Animation starten
        animationTimer = new Timer(20, e -> {
            if (isPlaying) {
                currentFrame += stepsPerUpdate;
                if (currentFrame >= numPoints) {
                    currentFrame = 0;
                }
                timePanel.repaint();
                phasePanel.repaint();
                updateInfoLabel();
            }
        });
        animationTimer.start();
        
        // Initial zeichnen
        updateInfoLabel();
    }
    
    // RK4 Integration
    private void calculateSolutionRK4() {
        double dt = tMax / numPoints;
        
        time.add(0.0);
        hares.add(H0);
        wolves.add(W0);
        
        double H = H0;
        double W = W0;
        
        for (int i = 1; i < numPoints; i++) {
            double k1_H = alpha * H - beta * H * W;
            double k1_W = delta * beta * H * W - gamma * W;
            
            double H2 = H + 0.5 * dt * k1_H;
            double W2 = W + 0.5 * dt * k1_W;
            double k2_H = alpha * H2 - beta * H2 * W2;
            double k2_W = delta * beta * H2 * W2 - gamma * W2;
            
            double H3 = H + 0.5 * dt * k2_H;
            double W3 = W + 0.5 * dt * k2_W;
            double k3_H = alpha * H3 - beta * H3 * W3;
            double k3_W = delta * beta * H3 * W3 - gamma * W3;
            
            double H4 = H + dt * k3_H;
            double W4 = W + dt * k3_W;
            double k4_H = alpha * H4 - beta * H4 * W4;
            double k4_W = delta * beta * H4 * W4 - gamma * W4;
            
            H += (dt / 6.0) * (k1_H + 2*k2_H + 2*k3_H + k4_H);
            W += (dt / 6.0) * (k1_W + 2*k2_W + 2*k3_W + k4_W);
            
            time.add(i * dt);
            hares.add(H);
            wolves.add(W);
        }
    }
    
    private void togglePlay() {
        isPlaying = !isPlaying;
        playButton.setText(isPlaying ? "Pause ⏸" : "Play ▶");
    }
    
    private void reset() {
        isPlaying = false;
        playButton.setText("Play ▶");
        currentFrame = 0;
        timePanel.repaint();
        phasePanel.repaint();
        updateInfoLabel();
    }
    
    private void updateInfoLabel() {
        if (currentFrame < time.size()) {
            infoLabel.setText(String.format(
                "Zeit: %.1f\nHasen: %.0f  |  Wölfe: %.0f",
                time.get(currentFrame),
                hares.get(currentFrame),
                wolves.get(currentFrame)
            ));
        }
    }
    
    // Panel für Zeitplot
    class TimePlotPanel extends JPanel {
        public TimePlotPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(600, 300));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            int margin = 50;
            
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, width, height);
            
            // Gitter
            g2.setColor(new Color(200, 200, 200, 100));
            g2.setStroke(new BasicStroke(1));
            for (int i = 0; i <= 10; i++) {
                int x = margin + (width - 2 * margin) * i / 10;
                int y = margin + (height - 2 * margin) * i / 10;
                g2.drawLine(margin, y, width - margin, y);
                g2.drawLine(x, margin, x, height - margin);
            }
            
            // Achsen
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(margin, margin, margin, height - margin);
            g2.drawLine(margin, height - margin, width - margin, height - margin);
            
            double maxPop = Math.max(getMax(hares), getMax(wolves)) * 1.1;
            
            // Hasen (blau)
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(2));
            Path2D pathH = new Path2D.Double();
            for (int i = 0; i <= currentFrame && i < hares.size(); i++) {
                double x = margin + (time.get(i) / tMax) * (width - 2 * margin);
                double y = height - margin - (hares.get(i) / maxPop) * (height - 2 * margin);
                if (i == 0) pathH.moveTo(x, y);
                else pathH.lineTo(x, y);
            }
            g2.draw(pathH);
            
            // Wölfe (rot)
            g2.setColor(Color.RED);
            Path2D pathW = new Path2D.Double();
            for (int i = 0; i <= currentFrame && i < wolves.size(); i++) {
                double x = margin + (time.get(i) / tMax) * (width - 2 * margin);
                double y = height - margin - (wolves.get(i) / maxPop) * (height - 2 * margin);
                if (i == 0) pathW.moveTo(x, y);
                else pathW.lineTo(x, y);
            }
            g2.draw(pathW);
            
            // Aktuelle Punkte
            if (currentFrame < hares.size()) {
                double x = margin + (time.get(currentFrame) / tMax) * (width - 2 * margin);
                double yH = height - margin - (hares.get(currentFrame) / maxPop) * (height - 2 * margin);
                double yW = height - margin - (wolves.get(currentFrame) / maxPop) * (height - 2 * margin);
                
                g2.setColor(Color.BLUE);
                g2.fillOval((int)x - 4, (int)yH - 4, 8, 8);
                
                g2.setColor(Color.RED);
                g2.fillOval((int)x - 4, (int)yW - 4, 8, 8);
            }
            
            // Titel
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString("Population über die Zeit", width / 2 - 70, 20);
            
            // X-Achsen-Beschriftung
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.drawString("Zeit (t)", width / 2 - 25, height - 15);
            
            // Y-Achsen-Beschriftung (vertikal)
            g2.rotate(-Math.PI / 2, 15, height / 2);
            g2.drawString("Anzahl Tiere", -40, 0);
            g2.rotate(Math.PI / 2, 15, height / 2);
            
            // X-Achsen-Werte
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            for (int i = 0; i <= 8; i += 2) {
                double t = i * 10;
                int x = (int)(margin + (t / tMax) * (width - 2 * margin));
                g2.drawString(String.valueOf((int)t), x - 5, height - margin + 15);
            }
            
            // Y-Achsen-Werte
            for (int i = 0; i <= 10; i += 2) {
                double pop = i * maxPop / 10;
                int y = (int)(height - margin - (pop / maxPop) * (height - 2 * margin));
                g2.drawString(String.format("%.0f", pop), margin - 25, y + 4);
            }
            
            // Legende
            g2.setColor(Color.BLUE);
            g2.fillOval(width - 120, 25, 12, 12);
            g2.setColor(Color.BLACK);
            g2.drawString("Hasen", width - 105, 35);
            
            g2.setColor(Color.RED);
            g2.fillOval(width - 120, 45, 12, 12);
            g2.setColor(Color.BLACK);
            g2.drawString("Wölfe", width - 105, 55);
            
            g2.dispose();
        }
    }
    
    // Panel für Phasenraum
    class PhasePlotPanel extends JPanel {
        public PhasePlotPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(600, 300));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            int margin = 50;
            
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, width, height);
            
            // Gitter
            g2.setColor(new Color(200, 200, 200, 100));
            g2.setStroke(new BasicStroke(1));
            for (int i = 0; i <= 10; i++) {
                int x = margin + (width - 2 * margin) * i / 10;
                int y = margin + (height - 2 * margin) * i / 10;
                g2.drawLine(margin, y, width - margin, y);
                g2.drawLine(x, margin, x, height - margin);
            }
            
            // Achsen
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(margin, margin, margin, height - margin);
            g2.drawLine(margin, height - margin, width - margin, height - margin);
            
            double maxHares = getMax(hares) * 1.1;
            double maxWolves = getMax(wolves) * 1.1;
            
            // Phasenraum-Trajektorie (grün)
            g2.setColor(Color.GREEN);
            g2.setStroke(new BasicStroke(1.5f));
            Path2D pathPhase = new Path2D.Double();
            for (int i = 0; i <= currentFrame && i < hares.size(); i++) {
                double x = margin + (hares.get(i) / maxHares) * (width - 2 * margin);
                double y = height - margin - (wolves.get(i) / maxWolves) * (height - 2 * margin);
                if (i == 0) pathPhase.moveTo(x, y);
                else pathPhase.lineTo(x, y);
            }
            g2.draw(pathPhase);
            
            // Aktueller Punkt
            if (currentFrame < hares.size()) {
                double x = margin + (hares.get(currentFrame) / maxHares) * (width - 2 * margin);
                double y = height - margin - (wolves.get(currentFrame) / maxWolves) * (height - 2 * margin);
                
                g2.setColor(Color.BLACK);
                g2.fillOval((int)x - 5, (int)y - 5, 10, 10);
            }
            
            // Titel
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString("Phasenraum (Wölfe vs. Hasen)", width / 2 - 110, 20);
            
            // X-Achsen-Beschriftung
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.drawString("Anzahl Hasen (H)", width / 2 - 50, height - 15);
            
            // Y-Achsen-Beschriftung (vertikal)
            g2.rotate(-Math.PI / 2, 15, height / 2);
            g2.drawString("Anzahl Wölfe (W)", -40, 0);
            g2.rotate(Math.PI / 2, 15, height / 2);
            
            // X-Achsen-Werte
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            for (int i = 0; i <= 10; i += 2) {
                double haresVal = i * maxHares / 10;
                int x = (int)(margin + (haresVal / maxHares) * (width - 2 * margin));
                g2.drawString(String.format("%.0f", haresVal), x - 10, height - margin + 15);
            }
            
            // Y-Achsen-Werte
            for (int i = 0; i <= 10; i += 2) {
                double wolvesVal = i * maxWolves / 10;
                int y = (int)(height - margin - (wolvesVal / maxWolves) * (height - 2 * margin));
                g2.drawString(String.format("%.0f", wolvesVal), margin - 35, y + 4);
            }
            
            g2.dispose();
        }
    }
    
    private double getMax(List<Double> list) {
        double max = Double.MIN_VALUE;
        for (double val : list) {
            if (val > max) max = val;
        }
        return max;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LotkaVolterraModel().setVisible(true);
        });
    }
}