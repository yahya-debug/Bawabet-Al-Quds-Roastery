package app.db_proj.UI;

import app.db_proj.ReportDAO;
import app.db_proj.SystemHandling;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.Connection;
import java.util.Map;

/**
 * Builds the Reports section for the Admin panel.
 * Contains JavaFX charts driven by live DB aggregation queries.
 */
public class ReportsUI {

    public static VBox build(SystemHandling sys) {
        VBox section = new VBox(18);
        section.setPadding(new Insets(14, 0, 14, 14));
        VBox.setVgrow(section, Priority.ALWAYS);

        Label heading = new Label("Reports & Statistics");
        heading.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 28));
        heading.setTextFill(Color.hsb(48, 1, 0.92, 1));

        Connection conn = sys.getConn();
        double[] stats = ReportDAO.summaryStats(conn);

        HBox kpiRow = buildKpiRow(stats);

        // First row of charts
        HBox row1 = new HBox(14);
        row1.getChildren().addAll(
            chartCard("Revenue by Category",   buildRevenueByCategory(conn)),
            chartCard("Orders by Status",      buildOrdersByStatus(conn))
        );

        // Second row
        HBox row2 = new HBox(14);
        row2.getChildren().addAll(
            chartCard("Top 5 Best-Selling Items", buildTopItems(conn)),
            chartCard("Revenue by Customer Type", buildRevenueByCustomerType(conn))
        );

        // Full-width monthly revenue chart
        VBox monthlyCard = chartCard("Monthly Revenue (Last 12 Months)", buildMonthlyRevenue(conn));
        monthlyCard.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(14, kpiRow, row1, row2, monthlyCard);
        content.setPadding(new Insets(0, 14, 14, 0));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        section.getChildren().addAll(heading, scroll);
        return section;
    }

    // summary chips

    private static HBox buildKpiRow(double[] stats) {
        HBox row = new HBox(14);
        row.setPadding(new Insets(0, 14, 0, 0));
        row.getChildren().addAll(
            kpiChip("Total Revenue",   String.format("₪ %.2f", stats[0])),
            kpiChip("Total Orders",    String.format("%.0f",   stats[1])),
            kpiChip("Avg Order Value", String.format("₪ %.2f", stats[2]))
        );
        return row;
    }

    private static VBox kpiChip(String label, String value) {
        VBox chip = new VBox(4);
        chip.setPadding(new Insets(14, 22, 14, 22));
        chip.setAlignment(Pos.CENTER);
        chip.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.20, 0.22, 1), new CornerRadii(12), null)));
        HBox.setHgrow(chip, Priority.ALWAYS);

        Label val = new Label(value);
        val.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 22));
        val.setTextFill(Color.hsb(48, 1, 0.92, 1));

        Label lbl = new Label(label);
        lbl.setFont(Font.font("Nunito", 13));
        lbl.setTextFill(Color.hsb(30, 0.12, 0.65, 1));

        chip.getChildren().addAll(val, lbl);
        return chip;
    }

    // Chart card wrapper

    private static VBox chartCard(String title, javafx.scene.Node chart) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.20, 0.22, 1), new CornerRadii(12), null)));
        HBox.setHgrow(card, Priority.ALWAYS);

        Label lbl = new Label(title);
        lbl.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
        lbl.setTextFill(Color.hsb(30, 0.12, 0.72, 1));

        card.getChildren().addAll(lbl, chart);
        return card;
    }

    // Individual charts
    private static PieChart buildRevenueByCategory(Connection conn) {
        PieChart chart = new PieChart();
        Map<String, Double> data = ReportDAO.revenueByCategory(conn);
        if (data.isEmpty()) {
            chart.getData().add(new PieChart.Data("No data yet", 1));
        } else {
            data.forEach((k, v) -> chart.getData().add(new PieChart.Data(k + "  ₪" + String.format("%.0f", v), v)));
        }
        styleChart(chart);
        chart.setPrefHeight(220);
        return chart;
    }

    private static PieChart buildOrdersByStatus(Connection conn) {
        PieChart chart = new PieChart();
        Map<String, Integer> data = ReportDAO.ordersByStatus(conn);
        if (data.isEmpty()) {
            chart.getData().add(new PieChart.Data("No orders yet", 1));
        } else {
            data.forEach((k, v) -> chart.getData().add(new PieChart.Data(k + " (" + v + ")", v)));
        }
        styleChart(chart);
        chart.setPrefHeight(220);
        return chart;
    }

    private static BarChart<String, Number> buildTopItems(Connection conn) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        xAxis.setLabel("Item");
        yAxis.setLabel("Units Sold");
        styleAxis(xAxis); styleAxis(yAxis);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        styleChart(chart);
        chart.setPrefHeight(220);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Integer> data = ReportDAO.topSellingItems(conn, 5);
        if (data.isEmpty()) {
            series.getData().add(new XYChart.Data<>("No data", 0));
        } else {
            data.forEach((k, v) -> series.getData().add(new XYChart.Data<>(k, v)));
        }
        chart.getData().add(series);
        return chart;
    }

    private static BarChart<String, Number> buildRevenueByCustomerType(Connection conn) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        xAxis.setLabel("Type");
        yAxis.setLabel("Revenue (₪)");
        styleAxis(xAxis); styleAxis(yAxis);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        styleChart(chart);
        chart.setPrefHeight(220);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Double> data = ReportDAO.revenueByCustomerType(conn);
        if (data.isEmpty()) {
            series.getData().add(new XYChart.Data<>("No data", 0));
        } else {
            data.forEach((k, v) -> series.getData().add(new XYChart.Data<>(k, v)));
        }
        chart.getData().add(series);
        return chart;
    }

    private static BarChart<String, Number> buildMonthlyRevenue(Connection conn) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        xAxis.setLabel("Month");
        yAxis.setLabel("Revenue (₪)");
        styleAxis(xAxis); styleAxis(yAxis);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        styleChart(chart);
        chart.setPrefHeight(250);
        chart.setMaxWidth(Double.MAX_VALUE);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Double> data = ReportDAO.monthlyRevenue(conn);
        if (data.isEmpty()) {
            series.getData().add(new XYChart.Data<>("No data", 0));
        } else {
            data.forEach((k, v) -> series.getData().add(new XYChart.Data<>(k, v)));
        }
        chart.getData().add(series);
        return chart;
    }

    // Style helpers

    private static void styleChart(Chart chart) {
        chart.setStyle("-fx-background-color: transparent;");
        chart.setAnimated(false);
        chart.setTitleSide(javafx.geometry.Side.TOP);
        // legend text color via inline style
        chart.lookupAll(".chart-legend-item").forEach(n ->
            n.setStyle("-fx-text-fill: #d4c0a0;"));
    }

    private static void styleAxis(Axis<?> axis) {
        axis.setStyle("-fx-tick-label-fill: #d4c0a0; -fx-text-fill: #d4c0a0;");
    }
}
