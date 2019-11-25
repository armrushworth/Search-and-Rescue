package colourSensorModel;

/**
 * A point on the colour graph used in ColourSampleChart.
 * @author James Daniels
 *
 */
public class LabelledColourSample {

  //This point has an associated colourLabel which represents the colour the RGB value is known to represent.
  private String colourLabel;
  
  //X coordinate is the RGB red value.
  private float red;
  
  //Y coordinate is the RGB green value.
  private float blue;
  
  //Z coordinate is the RGB blue value.
  private float green;
  
  /**
   * Constructor a float array is used since this how RGB values are returned when using the Ev3 colour sensor.
   * @param cl Colour label.
   * @param colourSample A measured RGB value.
   */
  public LabelledColourSample (String cl, float[] colourSample) {
    this.colourLabel = cl;
    this.red = colourSample[0];
    this.green = colourSample[1];
    this.blue = colourSample[2];
  }
  
  /**
   * GET method for the red value of this point.
   * @return red value between 0-1
   */
  public float getRed() {
    return this.red;
  }
  
  /**
   * GET method for the green value of this point.
   * @return green value between 0-1
   */
  public float getGreen() {
    return this.green;
  }
  
  /**
   * GET method for the blue value of this point.
   * @return blue value between 0-1
   */
  public float getBlue() {
    return this.blue;
  }
  
  /**
   * GET method for the label value of this point.
   * @return label value possible string value all possibilitys can be seen in the {@link ColourSampleChart#colours} list.
   */
  public String getColourLabel() {
    return this.colourLabel;
  }
  
  /**
   * Calculates the vector difference between a new RGB sample and this point.
   * Equation: squareRoot( (ri - rz)^2 + (gi - gz)^2 + (bi - bz)^2 )
   *    i being a new sample and z being a previous sample.
   * @param colourSample A new colour sample retrieved from the Ev3 colour sensor.
   * @return The vector distance between a the new sample and this labelled colour sample.
   */
  public double getVectorDistance(float[] colourSample) { 
    return Math.sqrt(
        Math.pow((red - colourSample[0]), 2) +
        Math.pow((green - colourSample[1]), 2) +
        Math.pow((blue - colourSample[2]), 2)
        );
  }
 
}
