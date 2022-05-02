package artofillusion.view;

import artofillusion.ViewerCanvas;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ROBIN
 */

class TestViewerCanvas extends ViewerCanvas
{
  static
  {
    backgroundColor = Color.WHITE;
    lineColor = Color.BLACK;
  }
  
  TestViewerCanvas ()
  {
    super (false);
  }

  @Override
  public double[] estimateDepthRange()
  {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  }
}

public class CanvasDrawerTest
{
  final static int SIZE = 4;
  static AntialiasedSoftwareCanvasDrawer drawer;
  
  @BeforeClass
  public static void setUpClass()
  {
    drawer = new AntialiasedSoftwareCanvasDrawer(new TestViewerCanvas ());
    drawer.bounds = new Rectangle();
    drawer.bounds.width = SIZE;
    drawer.bounds.height = SIZE;
    BufferedImage im = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
    drawer.pixel = ((DataBufferInt) ((BufferedImage) im).getRaster().getDataBuffer()).getData();
  }
  
  public void setUp() {   
    System.out.println ("Cleaning up pixels..");
    for (int i = 0; i < SIZE; i++)
      for (int j = 0; j < SIZE; j++)
        drawer.pixel [i+j*SIZE] = Color.WHITE.getRGB();
  }


    @Test
  public void testAliasing()
  {
    setUp();
    drawer.drawLine(new Point (0,0), new Point (2,1), Color.BLACK);
    assertEquals ("Pixel drawn", Color.BLACK.getRGB(), drawer.pixel [0]);
    assertEquals ("Pixel at (1,1) should be white", Color.WHITE.getRGB(), drawer.pixel [1+SIZE]);
  }
    @Test
  public void testAntialiasing()
  {
    setUp();
    drawer.drawAntialiasedLine(0,0, 2,1, Color.BLACK.getRGB());
    assertNotEquals ("Pixel drawn", Color.WHITE.getRGB(), drawer.pixel [0]);
    assertNotEquals ("Pixel at (1,1) should not be white", Color.WHITE.getRGB(), drawer.pixel [1+SIZE]);
  }
  
  @Test
  public void testOutOfBounds()
  {
    setUp();
    // None of these methods should fail
    drawer.drawAntialiasedLine (0,0, SIZE, 0, Color.BLACK.getRGB());
    assertEquals ("No border wrap", Color.WHITE.getRGB (), drawer.pixel [SIZE]);
    drawer.drawAntialiasedLine(0, 0, 0, SIZE, Color.BLACK.getRGB());
    drawer.drawAntialiasedLine (-1,0, 0, 0, Color.BLACK.getRGB());
    drawer.drawAntialiasedLine(0, -1, 0, 0, Color.BLACK.getRGB());
    drawer.drawAntialiasedLine(-1, -1, SIZE + 1, SIZE + 1, Color.BLACK.getRGB());
  }
}
