package com.quant.altdata.visualization;

import com.quant.altdata.data.model.FactorData;
import com.quant.altdata.backtest.BacktestResult;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 高级因子可视化器 - 提供丰富的因子表现和回测结果可视化功能
 */
public class AdvancedFactorVisualizer implements FactorVisualizer {
    private static final Logger logger = LoggerFactory.getLogger(AdvancedFactorVisualizer.class);

    @Override
    public void visualizeFactorPerformance(List<FactorData> factorDataList, BacktestResult backtestResult) {
        if (factorDataList == null || factorDataList.isEmpty()) {
            throw new IllegalArgumentException("Factor data cannot be null or empty");
        }

        logger.info("Visualizing factor performance with {} data points", factorDataList.size());

        // 创建主窗口
        ApplicationFrame frame = new ApplicationFrame("Advanced Factor Performance Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 创建图表
        JFreeChart chart = createCombinedChart(factorDataList, backtestResult);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(1200, 800));

        // 添加到窗口
        frame.setContentPane(chartPanel);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);

        logger.info("Factor performance visualization completed");
    }

    private JFreeChart createCombinedChart(List<FactorData> factorDataList, BacktestResult backtestResult) {
        // 创建共用的X轴（日期轴）
        DateAxis domainAxis = new DateAxis("Date");
        domainAxis.setAutoRange(true);
        domainAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 12));

        // 创建主图表（因子值走势）
        XYDataset factorDataset = createFactorDataset(factorDataList);
        XYPlot factorPlot = new XYPlot(factorDataset, domainAxis, new NumberAxis("Factor Value"), new XYLineAndShapeRenderer(true, true));
        factorPlot.getRangeAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 12));

        // 创建第二个图表（收益率）
        XYDataset returnDataset = createReturnDataset(factorDataList);
        XYPlot returnPlot = new XYPlot(returnDataset, domainAxis, new NumberAxis("Return"), new XYLineAndShapeRenderer(true, true));
        returnPlot.getRangeAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 12));

        // 创建组合图表
        CombinedDomainXYPlot combinedPlot = new CombinedDomainXYPlot(domainAxis);
        combinedPlot.add(factorPlot, 3);
        combinedPlot.add(returnPlot, 2);

        // 如果有回测结果，添加回测相关图表
        if (backtestResult != null) {
            XYDataset equityDataset = createEquityDataset(backtestResult);
            XYPlot equityPlot = new XYPlot(equityDataset, domainAxis, new NumberAxis("Equity"), new XYLineAndShapeRenderer(true, true));
            equityPlot.getRangeAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 12));
            combinedPlot.add(equityPlot, 2);

            XYDataset drawdownDataset = createDrawdownDataset(backtestResult);
            XYPlot drawdownPlot = new XYPlot(drawdownDataset, domainAxis, new NumberAxis("Drawdown (%)"), new XYLineAndShapeRenderer(true, true));
            drawdownPlot.getRangeAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 12));
            combinedPlot.add(drawdownPlot, 2);
        }

        combinedPlot.setOrientation(PlotOrientation.VERTICAL);
        combinedPlot.setGap(10.0);

        JFreeChart chart = new JFreeChart("Factor Performance Analysis",
                new Font("SansSerif", Font.BOLD, 16),
                combinedPlot,
                true);

        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 16));
        return chart;
    }

    private XYDataset createFactorDataset(List<FactorData> factorDataList) {
        TimeSeries series = new TimeSeries("Factor Value");
        for (FactorData data : factorDataList) {
            LocalDate date = data.getDate();
            Date utilDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            series.add(new Day(utilDate), data.getFactorValue());
        }
        return new TimeSeriesCollection(series);
    }

    private XYDataset createReturnDataset(List<FactorData> factorDataList) {
        TimeSeries series = new TimeSeries("Return");
        for (FactorData data : factorDataList) {
            LocalDate date = data.getDate();
            Date utilDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            series.add(new Day(utilDate), data.getReturn());
        }
        return new TimeSeriesCollection(series);
    }

    private XYDataset createEquityDataset(BacktestResult backtestResult) {
        TimeSeries series = new TimeSeries("Equity Curve");
        Map<LocalDate, Double> equityCurve = backtestResult.getEquityCurve();
        for (Map.Entry<LocalDate, Double> entry : equityCurve.entrySet()) {
            Date utilDate = Date.from(entry.getKey().atStartOfDay(ZoneId.systemDefault()).toInstant());
            series.add(new Day(utilDate), entry.getValue());
        }
        return new TimeSeriesCollection(series);
    }

    private XYDataset createDrawdownDataset(BacktestResult backtestResult) {
        TimeSeries series = new TimeSeries("Drawdown");
        Map<LocalDate, Double> drawdown = backtestResult.getDrawdown();
        for (Map.Entry<LocalDate, Double> entry : drawdown.entrySet()) {
            Date utilDate = Date.from(entry.getKey().atStartOfDay(ZoneId.systemDefault()).toInstant());
            series.add(new Day(utilDate), entry.getValue() * 100); // 转换为百分比
        }
        return new TimeSeriesCollection(series);
    }

    /**
     * 烛台图展示
     * @param factorDataList 因子数据列表
     */
    public void visualizeCandlestick(List<FactorData> factorDataList) {
        if (factorDataList == null || factorDataList.isEmpty()) {
            throw new IllegalArgumentException("Factor data cannot be null or empty");
        }

        logger.info("Visualizing candlestick chart with {} data points", factorDataList.size());

        ApplicationFrame frame = new ApplicationFrame("Candlestick Chart");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 准备烛台图数据
        int n = factorDataList.size();
        Date[] dates = new Date[n];
        double[] high = new double[n];
        double[] low = new double[n];
        double[] open = new double[n];
        double[] close = new double[n];
        double[] volume = new double[n];

        for (int i = 0; i < n; i++) {
            FactorData data = factorDataList.get(i);
            LocalDate date = data.getDate();
            dates[i] = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            high[i] = data.getHigh();
            low[i] = data.getLow();
            open[i] = data.getOpen();
            close[i] = data.getClose();
            volume[i] = data.getVolume();
        }

        DefaultHighLowDataset dataset = new DefaultHighLowDataset("Price", dates, high, low, open, close, volume);
        JFreeChart chart = ChartFactory.createCandlestickChart(
                "Price Movement", "Date", "Price", dataset, false);

        XYPlot plot = (XYPlot) chart.getPlot();
        CandlestickRenderer renderer = (CandlestickRenderer) plot.getRenderer();
        renderer.setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_SMALLEST);

        ChartPanel chartPanel = new ChartPanel(chart);
        frame.setContentPane(chartPanel);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);

        logger.info("Candlestick visualization completed");
    }
}