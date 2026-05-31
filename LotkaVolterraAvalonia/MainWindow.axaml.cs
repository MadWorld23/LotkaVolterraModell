using System;
using System.Collections.Generic;
using Avalonia;
using Avalonia.Controls;
using Avalonia.Input;
using Avalonia.Interactivity;       // <-- für RoutedEventArgs
using Avalonia.Media;
using Avalonia.Threading;
using Avalonia.Controls.Primitives;  // <-- für RangeBaseValueChangedEventArgs

namespace LotkaVolterraAvalonia;

public partial class MainWindow : Window
{
    private double alpha = 1.0, beta = 0.1, delta = 0.075, gamma = 1.5;
    private double H0 = 40, W0 = 10, tMax = 80;
    private int numPoints = 4000;
    private List<double> time = new(), hares = new(), wolves = new();
    private int currentFrame = 0;
    private bool isPlaying = true;
    private int stepsPerUpdate = 5;
    private DispatcherTimer? animationTimer;

    public MainWindow()
    {
        InitializeComponent();
        CalculateSolutionRK4();
        animationTimer = new DispatcherTimer { Interval = TimeSpan.FromMilliseconds(20) };
        animationTimer.Tick += OnTimerTick;
        animationTimer.Start();
        UpdateInfoLabel();
    }

    private void OnTimerTick(object? sender, EventArgs e)
    {
        if (!isPlaying) return;
        currentFrame += stepsPerUpdate;
        if (currentFrame >= numPoints) currentFrame = 0;
        Redraw();
        UpdateInfoLabel();
    }

    private void CalculateSolutionRK4()
    {
        double dt = tMax / numPoints;
        time.Add(0.0); hares.Add(H0); wolves.Add(W0);
        double H = H0, W = W0;
        for (int i = 1; i < numPoints; i++)
        {
            double k1H = alpha * H - beta * H * W;
            double k1W = delta * beta * H * W - gamma * W;
            double H2 = H + 0.5 * dt * k1H, W2 = W + 0.5 * dt * k1W;
            double k2H = alpha * H2 - beta * H2 * W2;
            double k2W = delta * beta * H2 * W2 - gamma * W2;
            double H3 = H + 0.5 * dt * k2H, W3 = W + 0.5 * dt * k2W;
            double k3H = alpha * H3 - beta * H3 * W3;
            double k3W = delta * beta * H3 * W3 - gamma * W3;
            double H4 = H + dt * k3H, W4 = W + dt * k3W;
            double k4H = alpha * H4 - beta * H4 * W4;
            double k4W = delta * beta * H4 * W4 - gamma * W4;
            H += (dt / 6.0) * (k1H + 2 * k2H + 2 * k3H + k4H);
            W += (dt / 6.0) * (k1W + 2 * k2W + 2 * k3W + k4W);
            time.Add(i * dt); hares.Add(H); wolves.Add(W);
        }
    }

    private void OnPlayClick(object? sender, RoutedEventArgs e)
    {
        isPlaying = !isPlaying;
        PlayButton.Content = isPlaying ? "Pause ⏸" : "Play ▶";
    }

    private void OnResetClick(object? sender, RoutedEventArgs e)
    {
        isPlaying = false; PlayButton.Content = "Play ▶";
        currentFrame = 0; Redraw(); UpdateInfoLabel();
    }

    private void OnSpeedChanged(object? sender, RangeBaseValueChangedEventArgs e)
    {
        double speed = SpeedSlider.Value / 10.0;
        stepsPerUpdate = Math.Max(1, (int)(speed * 5));
        SpeedLabel.Text = $"{speed:F1}x";
    }

    private void UpdateInfoLabel()
    {
        if (currentFrame < time.Count)
            InfoLabel.Text = $"Zeit: {time[currentFrame]:F1} | Hasen: {hares[currentFrame]:F0} | Wölfe: {wolves[currentFrame]:F0}";
    }

    private void Redraw() { DrawTimePlot(); DrawPhasePlot(); }

    private void DrawTimePlot()
    {
        var canvas = this.FindControl<Canvas>("TimeCanvas");
        if (canvas == null) return;
        canvas.Children.Clear();
        double width = canvas.Bounds.Width, height = canvas.Bounds.Height;
        if (width <= 0 || height <= 0) return;
        double margin = 50, plotW = width - 2 * margin, plotH = height - 2 * margin;
        double maxPop = Math.Max(GetMax(hares), GetMax(wolves)) * 1.1;

        for (int i = 0; i <= 10; i++)
        {
            double x = margin + plotW * i / 10, y = margin + plotH * i / 10;
            AddLine(canvas, margin, y, width - margin, y, Color.Parse("#C8C8C8"), 1);
            AddLine(canvas, x, margin, x, height - margin, Color.Parse("#C8C8C8"), 1);
        }
        AddLine(canvas, margin, margin, margin, height - margin, Colors.Black, 2);
        AddLine(canvas, margin, height - margin, width - margin, height - margin, Colors.Black, 2);

        if (currentFrame > 0)
        {
            var harePts = new List<Point>();
            for (int i = 0; i <= currentFrame && i < hares.Count; i++)
                harePts.Add(new Point(margin + (time[i] / tMax) * plotW, height - margin - (hares[i] / maxPop) * plotH));
            AddPolyline(canvas, harePts, Colors.Blue, 2);

            var wolfPts = new List<Point>();
            for (int i = 0; i <= currentFrame && i < wolves.Count; i++)
                wolfPts.Add(new Point(margin + (time[i] / tMax) * plotW, height - margin - (wolves[i] / maxPop) * plotH));
            AddPolyline(canvas, wolfPts, Colors.Red, 2);
        }

        if (currentFrame < hares.Count)
        {
            double cx = margin + (time[currentFrame] / tMax) * plotW;
            AddDot(canvas, cx, height - margin - (hares[currentFrame] / maxPop) * plotH, Colors.Blue, 5);
            AddDot(canvas, cx, height - margin - (wolves[currentFrame] / maxPop) * plotH, Colors.Red, 5);
        }

        AddText(canvas, "Population über die Zeit", width / 2 - 100, 10, 14, true);
        AddDot(canvas, width - 120, 30, Colors.Blue, 6); AddText(canvas, "Hasen", width - 108, 22, 11, false);
        AddDot(canvas, width - 120, 50, Colors.Red, 6); AddText(canvas, "Wölfe", width - 108, 42, 11, false);
        AddText(canvas, "Zeit (t)", width / 2 - 25, height - 25, 10, false);
        AddText(canvas, "Anzahl Tiere", 5, margin - 15, 10, false);
    }

    private void DrawPhasePlot()
    {
        var canvas = this.FindControl<Canvas>("PhaseCanvas");
        if (canvas == null) return;
        canvas.Children.Clear();
        double width = canvas.Bounds.Width, height = canvas.Bounds.Height;
        if (width <= 0 || height <= 0) return;
        double margin = 50, plotW = width - 2 * margin, plotH = height - 2 * margin;
        double maxH = GetMax(hares) * 1.1, maxW = GetMax(wolves) * 1.1;

        for (int i = 0; i <= 10; i++)
        {
            double x = margin + plotW * i / 10, y = margin + plotH * i / 10;
            AddLine(canvas, margin, y, width - margin, y, Color.Parse("#C8C8C8"), 1);
            AddLine(canvas, x, margin, x, height - margin, Color.Parse("#C8C8C8"), 1);
        }
        AddLine(canvas, margin, margin, margin, height - margin, Colors.Black, 2);
        AddLine(canvas, margin, height - margin, width - margin, height - margin, Colors.Black, 2);

        if (currentFrame > 0)
        {
            var pts = new List<Point>();
            for (int i = 0; i <= currentFrame && i < hares.Count; i++)
                pts.Add(new Point(margin + (hares[i] / maxH) * plotW, height - margin - (wolves[i] / maxW) * plotH));
            AddPolyline(canvas, pts, Colors.DarkMagenta, 2);
        }

        if (currentFrame < hares.Count)
        {
            double cx = margin + (hares[currentFrame] / maxH) * plotW;
            double cy = height - margin - (wolves[currentFrame] / maxW) * plotH;
            AddDot(canvas, cx, cy, Colors.Black, 6);
        }

        AddText(canvas, "Phasenraum (Wölfe vs. Hasen)", width / 2 - 120, 10, 14, true);
        AddText(canvas, "Anzahl Hasen (H)", width / 2 - 55, height - 25, 10, false);
        AddText(canvas, "Anzahl Wölfe (W)", 5, margin - 15, 10, false);
    }

    private void AddLine(Canvas c, double x1, double y1, double x2, double y2, Color col, double th)
    {
        var l = new Avalonia.Controls.Shapes.Line { StartPoint = new Point(x1, y1), EndPoint = new Point(x2, y2), Stroke = new SolidColorBrush(col), StrokeThickness = th };
        c.Children.Add(l);
    }

    private void AddPolyline(Canvas c, List<Point> pts, Color col, double th)
    {
        if (pts.Count < 2) return;
        var p = new Avalonia.Controls.Shapes.Polyline { Points = pts, Stroke = new SolidColorBrush(col), StrokeThickness = th };
        c.Children.Add(p);
    }

    private void AddDot(Canvas c, double cx, double cy, Color col, double r)
    {
        var e = new Avalonia.Controls.Shapes.Ellipse { Width = r * 2, Height = r * 2, Fill = new SolidColorBrush(col) };
        Canvas.SetLeft(e, cx - r); Canvas.SetTop(e, cy - r);
        c.Children.Add(e);
    }

    private void AddText(Canvas c, string t, double x, double y, double fs, bool bold)
    {
        var tb = new TextBlock { Text = t, FontSize = fs, FontWeight = bold ? FontWeight.Bold : FontWeight.Normal, Foreground = new SolidColorBrush(Colors.Black) };
        Canvas.SetLeft(tb, x); Canvas.SetTop(tb, y);
        c.Children.Add(tb);
    }

    private double GetMax(List<double> list)
    {
        double m = double.MinValue;
        foreach (double v in list) if (v > m) m = v;
        return m;
    }
}