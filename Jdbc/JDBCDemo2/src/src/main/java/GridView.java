import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GridView extends Panel implements MouseMotionListener, MouseListener, MouseWheelListener   {

    private Dimension gridDimension;
    private Font font;
    private Font font_uri;
    private FontMetrics fontMetrics;
    private Graphics gridGraphics;
    private Image gridImage;

    private int viewWidth, viewHeight;
    private int gridWidth;
    private int rowHeight;
    private int posX, posY;
    private int curCol, curRow;

    private ArrayList<String> headers;
    private ArrayList<String[]> data = new ArrayList<String[]>(16);;
    private ArrayList<boolean[]> data_isuri = new ArrayList<boolean[]>(16);;
    private int colWidth[];
    private int countCols;
    private int countRows;

    private Scrollbar sbHoriz, sbVert;
    private int sbWidth, sbHeight;

    private boolean isResized;
    private int posResize, resizeCol;

    private Cursor defCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    private Cursor uriCursor = new Cursor(Cursor.HAND_CURSOR);
    private Cursor resizeCursor = new Cursor(Cursor.E_RESIZE_CURSOR);

    public GridView() {
        super();
        font  = new Font("Dialog", Font.PLAIN, 12);
        Map<TextAttribute, Integer> fontAttributes = new HashMap<TextAttribute, Integer>();
        fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        font_uri = font.deriveFont(fontAttributes);
        setLayout(null);
        sbHoriz = new Scrollbar(Scrollbar.HORIZONTAL);
        add(sbHoriz);
        sbVert = new Scrollbar(Scrollbar.VERTICAL);
        add(sbVert);

        sbVert.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                handleScroll();
            }
        });
        sbHoriz.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                handleScroll();
            }
        });

        addMouseMotionListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);
    }

    java.util.List<String> getHead() {
        return headers;
    }

    java.util.List<String[]> getData() {
        return data;
    }

    public void setMinimumSize(Dimension d) {
        gridDimension = d;
    }

    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);

        sbHeight = sbHoriz.getPreferredSize().height;
        sbWidth = sbVert.getPreferredSize().width;
        viewHeight = h - sbHeight;
        viewWidth = w - sbWidth;

        sbHoriz.setBounds(0, viewHeight, viewWidth, sbHeight);
        sbVert.setBounds(viewWidth, 0, sbWidth, viewHeight);
        recalcScroll();

        gridImage = null;

        repaint();
    }

    public void setHead(String head[]) {
        countCols = head.length;
        data.clear();
        data_isuri.clear();
        headers = new ArrayList<String>(countCols);
        colWidth = new int[countCols];

        for (int i = 0; i < countCols; i++) {
            headers.add(head[i]);
            colWidth[i] = 100;
        }

        rowHeight = 0;
        countRows = 0;
    }

    boolean isURI(String s)
    {
        return (s.startsWith("http://") || s.startsWith("https://"));
    }

    public void addRow(String data[]) {
        if (data.length != countCols)
            return;

        String row[] = new String[countCols];
        boolean row_isuri[] = new boolean[countCols];

        for (int i = 0; i < countCols; i++) {
            row[i] = data[i];
            row_isuri[i] = isURI(data[i]);
        }

        this.data.add(row);
        this.data_isuri.add(row_isuri);
        countRows++;
    }

    public void update() {
        recalcScroll();
        repaint();
    }

    void handleScroll() {
        posX = sbHoriz.getValue();
        posY = rowHeight * sbVert.getValue();
        repaint();
    }

    void recalcScroll() {
        if (rowHeight == 0)
            return;

        int v = posY / rowHeight;
        int h = viewHeight / rowHeight;

        gridWidth = 0;
        for (int i = 0; i < countCols; i++)
            gridWidth += colWidth[i];

        sbHoriz.setValues(posX, viewWidth, 0, gridWidth);
        sbVert.setValues(v, h, 0, countRows + 1);

        posX = sbHoriz.getValue();
        posY = rowHeight * sbVert.getValue();
    }


    public void paint(Graphics g) {
        if (g == null)
            return;

        if (viewWidth <= 0 || viewHeight <= 0)
            return;

        g.setColor(SystemColor.control);
        g.fillRect(viewWidth, viewHeight, sbWidth, sbHeight);

        if (gridImage == null) {
            gridImage = createImage(viewWidth, viewHeight);
            gridGraphics = gridImage.getGraphics();

            gridGraphics.setFont(font);
            if (fontMetrics == null)
                fontMetrics = gridGraphics.getFontMetrics();
        }

        if (rowHeight == 0) {
            rowHeight = getMaxHeight(fontMetrics);

            for (int col = 0; col < countCols; col++)
                calcAutoWidth(col);

            recalcScroll();
        }

        gridGraphics.setColor(Color.white);
        gridGraphics.fillRect(0, 0, viewWidth, viewHeight);
        gridGraphics.setColor(Color.darkGray);
        gridGraphics.drawLine(0, rowHeight, viewWidth, rowHeight);

        int x = -posX;

        for (int col = 0; col < countCols; col++) {
            int w = colWidth[col];

            gridGraphics.setColor(SystemColor.control);
            gridGraphics.fillRect(x + 1, 0, w - 2, rowHeight);
            gridGraphics.setColor(Color.black);
            gridGraphics.drawString(headers.get(col), x + 2, rowHeight - 5);
            gridGraphics.setColor(Color.darkGray);
            gridGraphics.drawLine(x + w - 1, 0, x + w - 1, rowHeight - 1);
            gridGraphics.setColor(Color.white);
            gridGraphics.drawLine(x + w, 0, x + w, rowHeight - 1);

            x += w;
        }

        gridGraphics.setColor(SystemColor.control);
        gridGraphics.fillRect(0, 0, 1, rowHeight);
        gridGraphics.fillRect(x + 1, 0, viewWidth - x, rowHeight);
        gridGraphics.drawLine(0, 0, 0, rowHeight - 1);

        int y = rowHeight + 1 - posY;
        int row = 0;
        while (y < rowHeight + 1) {
            row++;
            y += rowHeight;
        }

        y = rowHeight + 1;

        for (; y < viewHeight && row < countRows; row++, y += rowHeight) {
            x = -posX;

            for (int col = 0; col < countCols; col++) {
                int w = colWidth[col];

                gridGraphics.setColor(Color.white);
                gridGraphics.fillRect(x, y, w - 1, rowHeight - 1);

                boolean isURI = isCellUri(col, row);
                gridGraphics.setColor(isURI?Color.blue:Color.black);
                gridGraphics.setFont(isURI?font_uri:font);
                gridGraphics.drawString(getCellData(col, row), x + 2, y + rowHeight - 5);
                gridGraphics.setFont(font);

                gridGraphics.setColor(Color.lightGray);
                gridGraphics.drawLine(x + w - 1, y, x + w - 1, y + rowHeight - 1);
                gridGraphics.drawLine(x, y + rowHeight - 1, x + w - 1, y + rowHeight - 1);

                x += w;
            }

            gridGraphics.setColor(Color.white);
            gridGraphics.fillRect(x, y, viewWidth - x, rowHeight - 1);
        }

        g.drawImage(gridImage, 0, 0, this);
    }

    public void update(Graphics g) {
        paint(g);
    }

    public Dimension preferredSize() {
        return minimumSize();
    }

    public Dimension getPreferredSize() {
        return minimumSize();
    }

    public Dimension getMinimumSize() {
        return minimumSize();
    }

    public Dimension minimumSize() {
        return gridDimension;
    }

    public String getCellData(int c, int r) {
        return (c>=0 && r>=0 && c<countCols && r<countRows) ? data.get(r)[c] : null;
    }

    public String getCurCellData() {
        return getCellData(curCol, curRow);
    }

    public boolean isCellUri(int c, int r) {
        return (c>=0 && r>=0 && c<countCols && r<countRows) && data_isuri.get(r)[c];
    }

    public boolean isCurCellUri() {
        return isCellUri(curCol, curRow);
    }

    public int getCurCol() {
        return curCol;
    }

    public int getCurRow() {
        return curRow;
    }

    private void calcAutoWidth(int i) {
        int w = 10;

        w = Math.max(w, fontMetrics.stringWidth(headers.get(i)));

        for (int j = 0; j < countRows; j++) {
            w = Math.max(w, fontMetrics.stringWidth(getCellData(i, j)));
        }

        colWidth[i] = w + 16;
    }


    private static int getMaxHeight(FontMetrics f) {
        return f.getHeight() + 4;
    }


    /// MouseMotionListener
    @Override
    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        if (isResized && x < viewWidth) {
            int w = x - posResize;

            if (w < 0)
                w = 0;

            colWidth[resizeCol] = w;
            recalcScroll();
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int i = countCols - 1;

        if (y <= rowHeight) {
            x += posX - gridWidth;

            for ( ; i >= 0; i--) {
                if (x > -6 && x < 6)
                    break;

                x += colWidth[i];
            }

            if (i >= 0) {
                if (!isResized) {
                    setCursor(resizeCursor);
                    isResized = true;
                    posResize = e.getX() - colWidth[i];
                    resizeCol = i;
                }
            } else {
                if (isResized) {
                    setCursor(defCursor);
                    isResized = false;
                }
            }
        }
        else {
            isResized = false;
            int xx = x;
            int yy = y - rowHeight;

            xx += posX - gridWidth;

            for (; i >= 0; i--) {
                if (xx > 0)
                    break;

                xx += colWidth[i];
            }
            i++;
            curCol = (i>=0 && i< countCols) ? i : -1;
            curRow = -1;

            if (rowHeight>0) {
                yy += posY;
                yy = yy / rowHeight;
                curRow = (yy >= 0 && yy < countRows) ? yy : -1;
            }

            setCursor(isCellUri(curCol, curRow) ? uriCursor: defCursor);
        }
    }


    /// MouseListener
    @Override
    public void mouseExited(MouseEvent e) {
        if (isResized) {
            setCursor(defCursor);
            isResized = false;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) { }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }


    /// MouseWheelListener
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() < 0) {
            sbVert.setValue(sbVert.getValue()-e.getScrollAmount());
        } else {
            sbVert.setValue(sbVert.getValue()+e.getScrollAmount());
        }
        handleScroll();
    }


}
