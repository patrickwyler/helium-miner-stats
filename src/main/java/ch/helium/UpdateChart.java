package ch.helium;

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
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateChart {

    public static void main(final String[] args) throws IOException {
        LOG.info("Update bar chart image");

        final File dataDir = Paths.get("data").toFile();
        final File[] files = dataDir.listFiles();

        final TimeSeries timeSeries = new TimeSeries("Rewards per month", "Month", "Rewards");

        final Gson gson = new Gson();
        final HashMap<String, Float> rewardsPerMonth = new HashMap<>();
        Arrays.stream(files).forEach(file -> {
            try {
                final DataEntry dataEntry = gson.fromJson(new FileReader(file), DataEntry.class);
                final float rewards = rewardsPerMonth.getOrDefault(getYearAndMonth(file), 0f) + dataEntry.getData().getTotal();
                rewardsPerMonth.put(getYearAndMonth(file), rewards);
            } catch (final Exception e) {
                LOG.warn(e.getMessage());
            }
        });

        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");

        rewardsPerMonth.forEach((key, value) -> {
            try {
                timeSeries.add(new Month(formatter.parse(key)), value);
            } catch (final ParseException e) {
                LOG.warn(e.getMessage());
            }
        });

        final JFreeChart chart = ChartFactory.createXYBarChart("Helium Miner Rewards",
                "Month",
                true,
                "HNT Rewards",
                new TimeSeriesCollection(timeSeries),
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        chart.getXYPlot().setBackgroundPaint(Color.white);
        chart.getXYPlot().setDomainGridlinePaint(Color.lightGray);
        chart.getXYPlot().setRangeGridlinePaint(Color.lightGray);
        final XYBarRenderer renderer = (XYBarRenderer) chart.getXYPlot().getRenderer();
        renderer.setSeriesPaint(0, new Color(255, 166, 0));
        renderer.setSeriesPaint(1, new Color(255, 99, 97));
        renderer.setSeriesPaint(2, new Color(188, 80, 144));
        renderer.setSeriesPaint(3, new Color(88, 80, 141));
        renderer.setSeriesPaint(4, new Color(0, 63, 92));
        renderer.setBarPainter(new StandardXYBarPainter());
        final int width = 1000;
        final int height = 500;
        final File outputFile = new File("chart.png");
        ChartUtils.saveChartAsPNG(outputFile, chart, width, height);
    }

    private static String getYearAndMonth(final File file) {
        return file.getName().subSequence(0, 7).toString();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class DataEntry {
        Meta meta;
        Data data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class Data {
        private float total;
        private float sum;
        private float stddev;
        private float min;
        private float median;
        private float max;
        private float avg;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class Meta {
        private String min_time;
        private String max_time;
    }
}
