package net.cyclestreets.api;

public abstract class DistanceFormatter
{
  public abstract String distance(int metres);
  public abstract String total_distance(int metres);

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
        return String.format("%dm", metres);
      return total_distance(metres);
    }

    public String total_distance(int metres) {
      int km = metres / 1000;
      int frackm = (int)((metres % 1000) / 10.0);
      return String.format("%d.%02dkm", km, frackm);
    }
  }

  private static class MilesFormatter extends DistanceFormatter  {
    private int metresToYards(int metres) { return (int)(metres * 1.0936133); }

    public String distance(int metres) {
      int yards = metresToYards(metres);
      if (yards <= 750)
        return String.format("%dyds", yards);
      return total_distance(metres);
    }

    public String total_distance(int metres) {
      int yards = metresToYards(metres);
      int miles = yards / 1760;
      int frackm = (int)((yards % 1760) / 17.6);
      return String.format("%d.%02d miles", miles, frackm);
    }
  }
}
