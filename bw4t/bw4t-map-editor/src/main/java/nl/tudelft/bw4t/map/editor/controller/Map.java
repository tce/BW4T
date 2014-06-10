package nl.tudelft.bw4t.map.editor.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import nl.tudelft.bw4t.map.Door;
import nl.tudelft.bw4t.map.Entity;
import nl.tudelft.bw4t.map.NewMap;
import nl.tudelft.bw4t.map.Point;
import nl.tudelft.bw4t.map.Rectangle;
import nl.tudelft.bw4t.map.RenderOptions;
import nl.tudelft.bw4t.map.Zone;
import nl.tudelft.bw4t.map.editor.MapEditor;
import nl.tudelft.bw4t.map.editor.controller.ColorSequence;
import nl.tudelft.bw4t.map.editor.controller.Room;

/**
 * This holds the map that the user designed. This is an abstract map contianing
 * only number of rows and columns, do not confuse with {@link NewMap}.
 */
public class Map implements TableModel {

    /** basic size of the map */
    private int rows;
    private int columns;
    private ArrayList<List<Room>> rooms;
    private int numberOfEntities = 0;
    private boolean randomize;
    /**
     * the target sequence.
     * */
    private ColorSequence sequence = new ColorSequence();
    private List<TableModelListener> listeners = new ArrayList<TableModelListener>();

    /**
     * constants that map rooms to real positions on the map.
     */
    static final double ROOMHEIGHT = 10;
    static final double ROOMWIDTH = 10;
    static final int CORRIDORWIDTH = 10;
    static final int CORRIDORHEIGHT = 10;

    /**
     * bot initial displacements
     */
    static final int XDISP = 4;
    static final int YDISP = 2;
    private static final String DROPZONE = "DropZone";
    private static final String FRONTDROPZONE = "FrontDropZone";

    private boolean isLabelsVisible;

    /**
     * size of map is fixed, you can't change it after construction.
     * 
     * @param rows
     * @param columns
     * @param isLabelsVisible
     *            true if labels should be shown by renderers
     */
    public Map(int rows, int columns, int entities, boolean rand,
            boolean labelsVisible) {
        isLabelsVisible = labelsVisible;
        if (rows < 1 || rows > 100) {
            throw new IllegalArgumentException("illegal value for row:" + rows);
        }
        if (columns < 1 || columns > 100) {
            throw new IllegalArgumentException("illegal value for columns:"
                    + columns);
        }
        if (entities < 1 || entities > 20) {
            throw new IllegalArgumentException("illegal value for entities:"
                    + entities);
        }

        this.rows = rows;
        this.columns = columns;
        this.numberOfEntities = entities;
        this.randomize = rand;
        rooms = new ArrayList<List<Room>>();

        // fill new array with rooms
        for (int row = 0; row < this.rows; row++) {
            rooms.add(new ArrayList<Room>());
            for (int col = 0; col < this.columns; col++) {
                rooms.get(row).add(new Room(row, col));
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public ColorSequence getSequence() {
        return sequence;
    }

    public void setSequence(ColorSequence colorSequence) {
        this.sequence = colorSequence;
    }

    /**
     * Check that given row and column are inside the actual map.
     * 
     * @param r
     *            is row
     * @param c
     *            is column
     * @throws IllegalArgumentException
     *             if r<0, c<0, r>=rows or c>=columns
     */
    public void checkCoord(int r, int c) throws IllegalArgumentException {
        if (r < 0) {
            throw new IllegalArgumentException("row is negative");
        }
        if (c < 0) {
            throw new IllegalArgumentException("column is negative");
        }
        if (r >= rows) {
            throw new IllegalArgumentException("rownr is too high");
        }
        if (c >= columns) {
            throw new IllegalArgumentException("columnnr is too high");
        }
    }

    /**
     * Get room at given row, col
     * 
     * @param r
     *            is row
     * @param c
     *            is column
     * @return room at given position.
     */
    public Room getRoom(int r, int c) {
        checkCoord(r, c);
        return rooms.get(r).get(c);
    }

    /**
     * get the number of entities on the map.
     * 
     * @return
     */
    public int getNumberOfEntities() {
        return numberOfEntities;
    }

    /**
     * set the number of entities on the map.
     */
    public void setNumberOfEntities(int numberOfEntities) {
        this.numberOfEntities = numberOfEntities;
    }

    /**
     * Save the real map to given file
     * 
     * @param file
     * @throws IOException
     * @throws JAXBException
     */
    public void save(File file) throws IOException, JAXBException {
        System.out.println("SAVE to " + file);

        String error = checkConsistency();
        if (error != null) {
            throw new IllegalStateException("save failed: " + error);
        }

        NewMap map = createMap();
        JAXBContext context = JAXBContext.newInstance(NewMap.class);

        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        m.marshal(map, new FileOutputStream(file));

    }

    /**
     * Add entities to map
     * 
     * @param map
     * @param centerx
     *            the position of first entity
     * @param centery
     *            the position of first entity
     */
    void addEntities(NewMap map, double centerx, double centery) {

        // set entities. start at 1.
        for (int n = 1; n <= numberOfEntities; n++) {
            int n6 = n / 6; // floor
            int m6 = n % 6;
            int m6sign = (m6 % 2) * 2 - 1; // 1 if m6=odd, -1 if even

            int extrax = 1;
            if (m6 <= 1)
                extrax = 0;
            double x = centerx + (XDISP * (2 * n6 + extrax)) * m6sign;

            int yoffset = 0;
            if (m6 == 2 || m6 == 3)
                yoffset = -1;
            if (m6 == 4 || m6 == 5)
                yoffset = 1;
            double y = centery + YDISP * yoffset;
            map.addEntity(new Entity("Bot" + n, new Point(x, y)));
        }

    }

    /**
     * Create the real map object using the settings
     * 
     * @return
     */
    public NewMap createMap() {
        NewMap map = new NewMap();

        // compute a number of key values
        double mapwidth = columns * ROOMWIDTH + 2 * CORRIDORWIDTH;
        double mapheight = (rows + 1) * (ROOMHEIGHT + CORRIDORHEIGHT);
        double dropzonex = mapwidth / 2;
        double dropzoney = getY(rows);
        double dropzonewidth = mapwidth / 2;

        // set the general fields of the map
        map.setArea(new Point(mapwidth, mapheight));
        map.setSequence(sequence.getColors());
        if (randomize) {
            map.setRandomBlocks((int) (2.5 * rows * columns));
            map.setRandomSequence(2 * rows * columns / 3);
        }

        addEntities(map, dropzonex, dropzoney - ROOMHEIGHT / 2 - CORRIDORHEIGHT
                / 2);

        // generate zones for each row:
        // write room zones with their doors. and the zone in frront
        // also generate the lefthall and righthall for each row.
        // connect room and corridor in front of it.
        // connect all corridor with each other and with left and right hall.
        for (int row = 0; row < rows; row++) {
            // add left and right hall corridor zones
            Zone lefthall = new Zone(CorridorLabel(row, -1), new Rectangle(
                    getX(-1), getNavY(row), ROOMWIDTH, CORRIDORHEIGHT
                            + ROOMHEIGHT), Zone.Type.CORRIDOR);
            Zone righthall = new Zone(CorridorLabel(row, columns),
                    new Rectangle(getX(columns), getNavY(row), ROOMWIDTH,
                            CORRIDORHEIGHT + ROOMHEIGHT), Zone.Type.CORRIDOR);
            map.addZone(lefthall);
            map.addZone(righthall);

            for (int col = 0; col < columns; col++) {
                Room room = rooms.get(row).get(col);
                Zone roomzone = new Zone(room.toString(), new Rectangle(
                        getX(col), getY(row), ROOMWIDTH, ROOMHEIGHT),
                        Zone.Type.ROOM);
                map.addZone(roomzone);
                roomzone.addDoor(new Door(new Point(getX(col),
                        (getY(row) - ROOMHEIGHT / 2)),
                        Door.Orientation.HORIZONTAL));
                roomzone.setBlocks(rooms.get(row).get(col).getColors()
                        .getColors());

                // add the zone in front of the room.
                Zone corridor = new Zone(CorridorLabel(row, col),
                        new Rectangle(getX(col), getNavY(row), ROOMWIDTH,
                                CORRIDORHEIGHT), Zone.Type.CORRIDOR);
                map.addZone(corridor);

                // connect them wth each other
                connect(roomzone, corridor);
                Zone left = map.getZone(CorridorLabel(row, col - 1));
                connect(corridor, left);
            }
            // and connect the last one to the right hall
            Zone lastcorridor = map.getZone(CorridorLabel(row, columns - 1));
            connect(righthall, lastcorridor);
        }

        // add drop zone with its door and zones left and rright
        Zone dropzone = new Zone(DROPZONE, new Rectangle(dropzonex, dropzoney,
                dropzonewidth, ROOMHEIGHT), Zone.Type.ROOM);
        dropzone.addDoor(new Door(new Point(dropzonex,
                (dropzoney - ROOMHEIGHT / 2)), Door.Orientation.HORIZONTAL));
        map.addZone(dropzone);
        // add the FrontDropZone and connect
        Zone frontdropzone = new Zone(FRONTDROPZONE, new Rectangle(dropzonex,
                getNavY(rows), columns * ROOMWIDTH, CORRIDORHEIGHT),
                Zone.Type.CORRIDOR);
        map.addZone(frontdropzone);
        connect(frontdropzone, dropzone);
        // and add the last row
        Zone lastrowleft = new Zone(CorridorLabel(rows, -1), new Rectangle(
                getX(-1), getNavY(rows), ROOMWIDTH, CORRIDORHEIGHT),
                Zone.Type.CORRIDOR);
        Zone lastrowright = new Zone(CorridorLabel(rows, columns),
                new Rectangle(getX(columns), getNavY(rows), ROOMWIDTH,
                        CORRIDORHEIGHT), Zone.Type.CORRIDOR);
        map.addZone(lastrowleft);
        map.addZone(lastrowright);
        // connect the three
        connect(lastrowleft, frontdropzone);
        connect(lastrowright, frontdropzone);

        // make vertical connection for left and right halls.

        for (int row = 0; row < rows; row++) {
            Zone below;

            // connect lefthall to below.
            Zone lefthall = map.getZone(CorridorLabel(row, -1));
            below = map.getZone(CorridorLabel(row + 1, -1));
            connect(lefthall, below);

            // add all righthall to left and below.
            Zone righthall = map.getZone(CorridorLabel(row, columns));
            below = map.getZone(CorridorLabel(row + 1, columns));
            connect(righthall, below);
        }

        setRenderOptions(map);

        return map;
    }

    /**
     * Set all the render options of the map.
     * 
     * @param map
     */
    private void setRenderOptions(NewMap map) {
        // do we need the render options? Check against defaults.
        if (isLabelsVisible) {
            return;
        }
        RenderOptions opts = new RenderOptions();
        opts.setLabelVisible(isLabelsVisible);

        for (Zone zone : map.getZones()) {
            zone.setRenderOptions(opts);
        }

    }

    private void connect(Zone a, Zone b) {
        a.addNeighbour(b);
        b.addNeighbour(a);
    }

    /**
     * get x coordinate of center of the room on the map.
     * 
     * @return x coordinate of room on the map.
     */
    public double getX(int column) {
        return CORRIDORWIDTH + column * ROOMWIDTH + ROOMWIDTH / 2;
    }

    /**
     * get y coordinate of center of room on the map.
     * 
     * @return y coordinate of room on the map.
     */
    public double getY(int row) {
        return row * (CORRIDORHEIGHT + ROOMHEIGHT) + ROOMHEIGHT / 2
                + CORRIDORHEIGHT;
    }

    /**
     * get the y coordinate of the navpoints for a given row.
     * 
     * @param row
     *            is the row
     * @return y coordinate of the navpoints for a given row
     */
    private double getNavY(int row) {
        return row * (CORRIDORHEIGHT + ROOMHEIGHT) + CORRIDORHEIGHT / 2;
    }

    /**
     * get a navpoint label for a corridor location.
     * 
     * @param row
     *            allowed is 0..{@link #rows}-1
     * @param col
     *            col=-1 means the LeftHall, col={@link #columns} means
     *            RightHall.
     * @return
     */
    private String CorridorLabel(int row, int col) {
        if (col == -1) {
            return "LeftHall" + (char) (row + 65);
        }
        if (col == columns) {
            return "RightHall" + (char) (row + 65);
        }
        return "Front" + rooms.get(row).get(col).toString();
    }

    /**
     * check consistency of the map. The sequence length must be 1 at least.
     * returns string with error message, or null if all ok
     * 
     * @return null, or string with error message.
     */
    public String checkConsistency() {
        if (numberOfEntities < 1) {
            return "There should be at least 1 entity";
        }
        if (sequence.size() <= 0 && !randomize) {
            return "Sequence must contain at least 1 block color";
        }

        // check if all blocks for sequence are there.
        // first accumulate all blocks from all rooms
        ColorSequence allblocks = new ColorSequence();
        for (List<Room> row : rooms) {
            for (Room room : row) {
                allblocks.addAll(room.getColors());
            }
        }

        // first check if there are blocks while random is on
        if (randomize && (!allblocks.isEmpty() || !sequence.isEmpty())) {
            MapEditor.showDialog("There are blocks on the map\nbut the map is set to random.\nWe proceed anyway.");
        }

        // remove all colors from the sequence. That will throw exception if
        // impossible.
        try {
            allblocks.removeAll(sequence);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        return null;

    }

    /**
     * Ask user where to save and then call {@link #save(File)}
     */
    public void saveAsFile() {
        try {
            // check before user puts effort in
            String state = checkConsistency();
            if (state != null) {
                throw new IllegalStateException("Map is not ready for save.\n"
                        + state);
            }
            // TODO Auto-generated method stub
            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                save(chooser.getSelectedFile());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            MapEditor.showDialog(e, "Save failed: " + e.getMessage());
        }
    }

    /*
     * ************** IMPLEMENTS JTableModel ***************
     */

    @Override
    /**
     * {@inheritDoc}
     */
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);

    }

    @Override
    /**
     * {@inheritDoc}
     */
    public Class<?> getColumnClass(int columnIndex) {
        return Room.class;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public int getColumnCount() {
        return columns;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public String getColumnName(int columnIndex) {
        return null;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public int getRowCount() {
        return rows;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        return rooms.get(rowIndex).get(columnIndex);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);

    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (!(aValue instanceof Room)) {
            throw new IllegalArgumentException(
                    "value must be a Room but found " + aValue + " of type "
                            + aValue.getClass());
        }
        rooms.get(rowIndex).get(columnIndex).setValue((Room) aValue);
    }

}