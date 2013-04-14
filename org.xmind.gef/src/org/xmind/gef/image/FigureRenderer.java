/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/

package org.xmind.gef.image;

import java.util.List;
import java.util.Stack;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.GC;

/**
 * @author Frank Shaka
 * 
 */
public class FigureRenderer implements IRenderer {

    private IFigure[] figures = null;

    private Rectangle bounds = null;

    private double scale = -1;

    /**
     * 
     */
    public FigureRenderer() {
        super();
    }

    public void init(IExportSourceProvider source, IExportAreaProvider area) {
        setFigures(source.getContents());
        setBounds(area.getExportArea());
        setScale(area.getScale());
    }

    /**
     * @param bounds
     *            the bounds to set
     */
    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    /**
     * @param scale
     *            the scale to set
     */
    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
     * @param figures
     *            the figures to set
     */
    public void setFigures(IFigure[] figures) {
        this.figures = figures;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * @return the scale
     */
    public double getScale() {
        return scale;
    }

    /**
     * @return the figures
     */
    public IFigure[] getFigures() {
        return figures;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.image.IRenderer#render(org.eclipse.swt.graphics.GC)
     */
    public void render(GC gc) {
        if (figures == null)
            return;

        Stack<Graphics> graphicsStack = new Stack<Graphics>();
        SWTGraphics baseGraphics = new SWTGraphics(gc);
        graphicsStack.push(baseGraphics);
        createGraphics(baseGraphics, graphicsStack);
        Graphics graphics = graphicsStack.peek();
        try {
            graphics.pushState();
            try {
                for (int i = 0; i < figures.length; i++) {
                    IFigure figure = figures[i];
                    figure.paint(graphics);
                    graphics.restoreState();
                }
            } finally {
                graphics.popState();
            }
        } finally {
            while (!graphicsStack.isEmpty()) {
                graphicsStack.pop().dispose();
            }
        }
    }

    /**
     * @param graphics
     * @param stack
     */
    protected void createGraphics(Graphics graphics, Stack<Graphics> stack) {
        if (bounds != null) {
            graphics.translate(-bounds.x, -bounds.y);
        }
        if (scale > 0) {
            graphics.scale(scale);
//            ScaledGraphics scaledGraphics = new ScaledGraphics(graphics);
//            scaledGraphics.scale(scale);
//            stack.push(scaledGraphics);
//            graphics = scaledGraphics;
        }
    }

    protected Graphics addScaledGraphics(Graphics graphics,
            List<Graphics> additionalGraphics) {
        return graphics;
    }

}
