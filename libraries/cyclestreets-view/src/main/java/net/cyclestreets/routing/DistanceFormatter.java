package net.cyclestreets.routing;

public abstract class DistanceFormatter
{
  public abstract String distance(int metres);
  public abstract String totalDistance(int metres);
  public abstract String speed(float metresPerSec);
  public abstract String speedUnit();

  public static DistanceFormatter formatter(final String name) {
    if ("miles".equals(name))
      return milesFormatter;
    return kmFormatter;
  }

  private static DistanceFormatter kmFormatter = new KmFormatter();
  private static DistanceFormatter milesFormatter = new MilesFormatter();

  private static class KmFormatter extends DistanceFormatter  {
    public String distance(int metres) {
      if (metres < 2000)
        return String.format("%dm", roundDistance(metres));
      return totalDistance(metres);
    }

    public String totalDistance(int metres) {
      int km = metres / 1000;
      int frackm = (int)((metres % 1000) / 10.0);
      return String.format("%d.%02dkm", km, frackm);
    }

    public String speed(float metresPerSec) {
      final double kph = metresPerSec * 60.0 * 60.0 / 1000.0;
      if (kph < 10)
        return String.format("%.1f", kph);
      return String.format("%d", (int)kph);
    }

    public String speedUnit() {
      return "km/h";
    }
  }

  private static class MilesFormatter extends DistanceFormatter  {
    private int metresToYards(int metres) { return (int)(metres * 1.0936133); }

    public String distance(int metres) {
      int yards = metresToYards(metres);
      if (yards <= 750)
        return String.format("%d yards", roundDistance(yards));
      return totalDistance(metres);
    }

    public String totalDistance(int metres) {
      int yards = metresToYards(metres);
      int miles = yards / 1760;
      int frackm = (int)((yards % 1760) / 17.6);
      return String.format("%d.%02d miles", miles, frackm);
    }

    public String speed(float metresPerSec) {
      final double metresPerHour = metresPerSec * 60.0 * 60.0;
      final int yardsPerHour = metresToYards((int)metresPerHour);
      final double mph = yardsPerHour / 1760.0;
      if (mph < 10)
        return String.format("%.1f", mph);
      return String.format("%d", (int)mph);
    }

    public String speedUnit() {
      return "mph";
    }
  }

  static protected int roundDistance(int units) {
    if (units < 500)
      return (int)
          (units/5.0) * 5;
    return (int)(units/10.0) * 10;
  }
}
