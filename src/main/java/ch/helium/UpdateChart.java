package ch.helium;

import static java.awt.Color.lightGray;
import static java.awt.Color.white;
import static org.jfree.chart.axis.DateTickUnitType.MONTH;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.google.gson.Gson;

import ch.helium.models.DataEntry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateChart {

    public static void main(final String[] args) throws IOException {
        LOG.info("Update bar chart image");

        final HashMap<String, Float> rewardsPerMonth = parseData();
        final TimeSeries timeSeries = createTimeSeries(rewardsPerMonth);
        final JFreeChart chart = createChart(timeSeries);
        saveChart(chart);
    }

    private static HashMap<String, Float> parseData() {
        final Gson gson = new Gson();
        final HashMap<String, Float> rewardsPerMonth = new HashMap<>();
        final File dataDir = Paths.get("data").toFile();
        final File[] files = dataDir.listFiles();
        Arrays.stream(files).forEach(file -> {
            try {
                final DataEntry dataEntry = gson.fromJson(new FileReader(file), DataEntry.class);
                final float rewards = rewardsPerMonth.getOrDefault(getYearAndMonth(file), 0f) + dataEntry.getData().getTotal();
                rewardsPerMonth.put(getYearAndMonth(file), rewards);
            } catch (final Exception e) {
                LOG.warn(e.getMessage());
            }
        });
        return rewardsPerMonth;
    }

    private static TimeSeries createTimeSeries(final HashMap<String, Float> rewardsPerMonth) {
        final TimeSeries timeSeries = new TimeSeries("Rewards per month", "Month", "Rewards");
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        rewardsPerMonth.forEach((key, value) -> {
            try {
                LOG.debug("Add timeseries {} {}", key, value);
                timeSeries.add(new Month(formatter.parse(key)), value);
            } catch (final ParseException e) {
                LOG.warn(e.getMessage());
            }
        });
        return timeSeries;
    }

    private static JFreeChart createChart(final TimeSeries timeSeries) {
        final JFreeChart chart = ChartFactory.createXYBarChart("Helium Miner Rewards",
                "Month",
                true,
                "HNT Rewards",
                new TimeSeriesCollection(timeSeries),
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        chart.getXYPlot().setBackgroundPaint(white);
        chart.getXYPlot().setDomainGridlinePaint(lightGray);
        chart.getXYPlot().setRangeGridlinePaint(lightGray);

        final XYBarRenderer renderer = (XYBarRenderer) chart.getXYPlot().getRenderer();
        renderer.setSeriesPaint(0, new Color(255, 166, 0));
        renderer.setBarPainter(new StandardXYBarPainter());

        final DateAxis axis = (DateAxis) chart.getXYPlot().getDomainAxis();
        axis.setTickUnit(new DateTickUnit(MONTH, 1, new SimpleDateFormat("MMM-yyyy")));
        return chart;
    }

    private static void saveChart(final JFreeChart chart) throws IOException {
        final int width = 800;
        final int height = 500;
        final File outputFile = new File("chart.png");
        ChartUtils.saveChartAsPNG(outputFile, chart, width, height);
    }

    private static String getYearAndMonth(final File file) {
        return file.getName().subSequence(0, 7).toString();
    }
}
