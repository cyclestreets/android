package net.cyclestreets.routing;

public abstract class ElevationFormatter {
  public abstract String height(int metres);
  public abstract String distance(int metres);

  static public ElevationFormatter formatter(final String name) {
    if ("miles".equals(name))
      return imperialFormatter;
    return metricFormatter;
  }

  static private ElevationFormatter metricFormatter = new MetricFormatter();
  static private ElevationFormatter imperialFormatter = new ImperialFormatter();

  static private class MetricFormatter extends ElevationFormatter {
    @Override
    public String height(int metres) {
      return String.format("%dm", metres);
    }

    @Override
    public String distance(int metres) {
      if (metres < 2000)
        return String.format("%dm", round_distance(metres));

      int km = metres / 1000;
      return String.format("%dkm", km);
    }
  }

  static private class ImperialFormatter extends ElevationFormatter {
    @Override
    public String height(int metres) {
      int yards = metresToYards(metres);
      return String.format("%d yards", yards);
    }

    @Override
    public String distance(int metres) {
      int yards = metresToYards(metres);
      if (yards <= 750)
        return String.format("%d yards", round_distance(yards));
      int miles = yards / 1760;
      return String.format("%d miles", miles);
    }

    private int metresToYards(int metres) { return (int)(metres * 1.0936133); }
  }

  static private int round_distance(int units) {
    return (units < 500) ?
              (int)(units/5.0) * 5 :
              (int)(units/10.0) * 10;
  }
}
