package artofillusion.view;

import artofillusion.ViewerCanvas;
import java.awt.Color;
import java.awt.Point;

public class AntialiasedSoftwareCanvasDrawer extends SoftwareCanvasDrawer implements CanvasDrawer
{

  public AntialiasedSoftwareCanvasDrawer(ViewerCanvas objectPreviewCanvas)
  {
    super (objectPreviewCanvas);
  }
  
  public void drawAliasedLine(Point p1, Point p2, Color color)
  {
    int x1, y1, x2, y2, col;
    int x, y, dx, dy, end, index, edge;

    col = color.getRGB();
    x1 = p1.x;
    y1 = p1.y;
    x2 = p2.x;
    y2 = p2.y;
    if (x1 < 0 && x2 < 0)
      return;
    if (y1 < 0 && y2 < 0)
      return;
    if (x1 >= bounds.width && x2 >= bounds.width)
      return;
    if (y1 >= bounds.height && y2 >= bounds.height)
      return;
    dx = x2 - x1;
    dy = y2 - y1;

    if (dx == 0 && dy == 0)
      return; // TODO FIXME should draw a dot

    // These are to prevent overflow in Math.abs() in the case that dx or dy is Integer.MIN_VALUE.
    // Math.abs(Integer.MIN_VALUE) would return Integer.MIN_VALUE, which lets dx = 0 through, 
    // resulting division by 0 later. Same with dy, further down.
    // Math.abs(Integer.MIN_VALUE+1) returns Integer.MAX_VALUE.

    dx = Math.max(dx, Integer.MIN_VALUE+1);
    dy = Math.max(dy, Integer.MIN_VALUE+1);

    if (Math.abs(dx) > Math.abs(dy))
    {
        // x is the major axis.
        if (dx > 0)
        {
            x = x1;
            y = y1<<16+32768;
            dy = (dy<<16)/dx;
            end = x2 < bounds.width ? x2 : bounds.width;
        }
        else
        {
            x = x2;
            y = y2<<16+32768;
            dy = (dy<<16)/dx;
            end = x1 < bounds.width ? x1 : bounds.width;
        }

        if (x < 0)
        {
            y -= dy*x;
            x = 0;
        }
        edge = bounds.height<<16;
        while (x < end)
        {
            if (y >= 0 && y < edge)
            {
                index = bounds.width*(y>>16)+x;
                pixel[index] = col;
            }
            x++;
            y += dy;
        }
    }
    else
    {
        // y is the major axis.

        if (dy > 0)
        {
            x = x1<<16+32768;
            y = y1;
            dx = (dx<<16)/dy;
            end = y2 < bounds.height ? y2 : bounds.height;
        }
        else
        {
            x = x2<<16+32768;
            y = y2;
            dx = (dx<<16)/dy;
            end = y1 < bounds.height ? y1 : bounds.height;
        }
        if (y < 0)
        {
            x -= dx*y;
            y = 0;
        }
        edge = bounds.width<<16;
        while (y < end)
        {
            if (x >= 0 && x < edge)
            {
                index = y*bounds.width+(x>>16);
                pixel[index] = col;
            }
            x += dx;
            y++;
        }
    }
  }

  @Override
  public void drawLine(Point p1, Point p2, Color color) {
    drawAntialiasedLine(p1.x, p1.y, p2.x, p2.y, color.getRGB());
  }
  
  public void drawAntialiasedLine(int x1, int y1,
                                 int x2, int y2, int color) {
     if (x1 < 0 && x2 < 0)
      return;
    if (y1 < 0 && y2 < 0)
      return;
    if (x1 >= bounds.width && x2 >= bounds.width)
      return;
    if (y1 >= bounds.height && y2 >= bounds.height)
      return;
    if (x1 == x2 && y1 == y2)
      return; // TODO FIXME draw a dot

    // TODO FIXME Le cas où on dessine en-dehors des bornes n'est pas pris en compte et plante
    // TODO FIXME Le blend n'est pas terrible..?
    double gradient;
    boolean isSteep = Math.abs(y2-y1)> Math.abs(x2-x1);

    if(isSteep){
        int c = y1;
        y1 = x1;
        x1 = c;
        c = x2;
        x2 = y2;
        y2 = c;
    }
    
    if(x2 < x1) {
        int d = x2;
        x2 = x1;
        x1 = d;
        d = y2;
        y2 = y1;
        y1 = d;
    }

    double dx = x2 - x1;
    double dy = y2 - y1;

    if (dx == 0.0){
        gradient =1;
    }else {
        gradient = dy / dx;
    }

    //first end point
    double xEnd = round(x1);
    double yEnd = (int)(y1+gradient*(xEnd-x1));
    double xGap = reverseFractionalPart(0.5+x1);

    //System.out.println ("xGap=" + xGap);
    final int xPxl1 = (int) xEnd;
    final int yPxl1 = (int) integerPart (yEnd);

    //System.out.println ("fractionalPart(yEnd)=" + fractionalPart(yEnd));
    //System.out.println ("reverseFractionalPart(yEnd)=" + reverseFractionalPart(yEnd));
    if (isSteep) {
        plot(yPxl1, xPxl1, reverseFractionalPart(yEnd)*xGap, color);
        plot(yPxl1 + 1, xPxl1, fractionalPart(yEnd)*xGap, color);
    } else {
        plot(xPxl1, yPxl1, reverseFractionalPart(yEnd) * xGap, color);
        plot(xPxl1, yPxl1 + 1, fractionalPart(yEnd) * xGap, color);
    }

    double intery = yEnd + gradient;

    //second end point
    xEnd = round(x2);
    //System.out.println ("xGap=" + xGap);
    yEnd = y2+gradient*(xEnd-x2);
    xGap = reverseFractionalPart(x2+0.5);

    int xPxl2 = (int) xEnd;
    int yPxl2 = (int) integerPart(yEnd);

    //System.out.println ("fractionalPart(yEnd)=" + fractionalPart(yEnd));
    //System.out.println ("reverseFractionalPart(yEnd)=" + reverseFractionalPart(yEnd));

    if (isSteep) {
        plot(yPxl2, xPxl2, reverseFractionalPart(yEnd)*xGap, color);
        plot((yPxl2 + 1), xPxl2, fractionalPart(yEnd)*xGap, color);
    } else {
        plot(xPxl2, yPxl2, reverseFractionalPart(yEnd) * xGap, color);
        plot(xPxl2, yPxl2 + 1, fractionalPart(yEnd) * xGap, color);
    }

    if(isSteep){
        for(int x = xPxl1+1; x<=xPxl2-1; x++){
            plot((int)integerPart(intery),x,reverseFractionalPart(intery), color);
            plot((int)integerPart(intery)+1,x,fractionalPart(intery), color);
            intery +=gradient;
        }
    }else {
        for(int x = xPxl1+1; x<=xPxl2-1; x++){
            plot(x,(int)integerPart(intery),reverseFractionalPart(intery), color);
            plot(x,(int)integerPart(intery)+1,fractionalPart(intery), color);
            intery +=gradient;
        }
    }
}

private double integerPart (double x){
    return Math.floor(x);
}

private int round (double x){
    return (int) integerPart(x+0.5);
}

private double fractionalPart (double x){
    return x-Math.floor(x);
}

private double reverseFractionalPart (double x){
    return 1-fractionalPart(x);
}
 private int blend (int a, int b, float ratio) {
    if (ratio > 1f) {
        ratio = 1f;
    } else if (ratio < 0f) {
        ratio = 0f;
    }

    float iRatio = 1.0f - ratio;

    int aA = (a >> 24 & 0xff);
    int aR = ((a & 0xff0000) >> 16);
    int aG = ((a & 0xff00) >> 8);
    int aB = (a & 0xff);

    int bA = (b >> 24 & 0xff);
    int bR = ((b & 0xff0000) >> 16);
    int bG = ((b & 0xff00) >> 8);
    int bB = (b & 0xff);

    int A = (int)((aA * iRatio) + (bA * ratio));
    int R = (int)((aR * iRatio) + (bR * ratio));
    int G = (int)((aG * iRatio) + (bG * ratio));
    int B = (int)((aB * iRatio) + (bB * ratio));

    return A << 24 | R << 16 | G << 8 | B;
}


  private void plot(int x, int y, double intensity, int drawingColor){
    double alfaChanel = Math.round(intensity*100.0)/100.0;
    final int index = x + y*bounds.width;
    pixel [index] = blend(pixel [index],drawingColor,(float)alfaChanel);
  }

  // TODO Override renderline!
}
