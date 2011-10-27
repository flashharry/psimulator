package psimulator.userInterface.Editor.DrawPanel;

import psimulator.userInterface.Editor.DrawPanel.Graph.Graph;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;
import psimulator.dataLayer.ColorMixerSignleton;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.userInterface.Editor.DrawPanel.MouseActionListeners.DrawPanelListenerStrategy;
import psimulator.userInterface.Editor.DrawPanel.MouseActionListeners.DrawPanelListenerStrategyAddCable;
import psimulator.userInterface.Editor.DrawPanel.MouseActionListeners.DrawPanelListenerStrategyAddHwComponent;
import psimulator.userInterface.Editor.DrawPanel.MouseActionListeners.DrawPanelListenerStrategyHand;
import psimulator.userInterface.Editor.DrawPanel.Actions.ActionOnDelete;
import psimulator.userInterface.Editor.DrawPanel.Components.AbstractComponent;
import psimulator.userInterface.Editor.DrawPanel.Components.AbstractHwComponent;
import psimulator.userInterface.Editor.DrawPanel.Enums.ComponentAction;
import psimulator.userInterface.Editor.DrawPanel.Enums.MainTool;
import psimulator.userInterface.Editor.DrawPanel.Graph.GraphInterface;
import psimulator.userInterface.Editor.DrawPanel.UndoCommands.UndoableAlignComponentsToGrid;
import psimulator.userInterface.MainWindowInnerInterface;
import psimulator.userInterface.imageFactories.AbstractImageFactory;

/**
 *
 * @author Martin
 */
public final class DrawPanel extends DrawPanelOuterInterface implements DrawPanelInnerInterface, Observer {
    // mouse listeners

    private DrawPanelListenerStrategy mouseListenerHand;
    private DrawPanelListenerStrategy mouseListenerAddHwComponent;
    private DrawPanelListenerStrategy mouseListenerCable;
    private DrawPanelListenerStrategy currentMouseListener;
    // END mouse listenrs
    private Graph graph = new Graph();
    private UndoManager undoManager = new UndoManager();
    private ZoomManager zoomManager = new ZoomManager();
    private AbstractImageFactory imageFactory;
    private MainWindowInnerInterface mainWindow;
    // variables for creating cables
    private boolean lineInProgress = false;
    private Point lineStart;
    private Point lineEnd;
    // variables for marking components with transparent rectangle
    private boolean rectangleInProgress = false;
    private Rectangle rectangle;
    //
    private List<AbstractComponent> markedCables = new ArrayList<AbstractComponent>();
    private List<AbstractComponent> markedComponents = new ArrayList<AbstractComponent>();
    private Dimension defaultZoomAreaMin = new Dimension(800, 600);
    private Dimension defaultZoomArea = new Dimension(defaultZoomAreaMin);
    private Dimension actualZoomArea = new Dimension(defaultZoomArea);
    //
    private Grid grid;
    private DataLayerFacade dataLayer;
    
    private EnumMap<ComponentAction, AbstractAction> actions;

    public DrawPanel(MainWindowInnerInterface mainWindow, AbstractImageFactory imageFactory, DataLayerFacade dataLayer) {
        super();

        this.mainWindow = mainWindow;
        this.imageFactory = imageFactory;
        this.dataLayer = dataLayer;

        actualZoomArea.width = zoomManager.doScaleToActual(defaultZoomArea.width);
        actualZoomArea.height = zoomManager.doScaleToActual(defaultZoomArea.height);

        this.setPreferredSize(actualZoomArea);
        this.setMinimumSize(actualZoomArea);
        this.setMaximumSize(actualZoomArea);

        this.setBackground(ColorMixerSignleton.drawPanelColor);

        createDrawPaneMouseListeners();
        createAllActions();

        // add key binding for delete
        mainWindow.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DELETE"), "DELETE");
        mainWindow.getRootPane().getActionMap().put("DELETE", getAbstractAction(ComponentAction.DELETE));

        // init grid
        grid = new Grid(this, zoomManager);

        zoomManager.addObserver(this);

    }
    
    /**
     * creates all actions according to ComponentAction Enum
     */
    private void createAllActions(){
        actions = new EnumMap<ComponentAction, AbstractAction>(ComponentAction.class);
        
        for(ComponentAction ca : ComponentAction.values()){
            switch(ca){
                case ALIGN_TO_GRID:
                    break;
                case DELETE:
                    actions.put(ca, new ActionOnDelete(graph, undoManager, this, mainWindow));
                    break;
                case PROPERTIES:
                    break;
            }
        }
    }
    
    /**
     * Creates mouse listeners for all tools
     */
    private void createDrawPaneMouseListeners() {
        mouseListenerHand = new DrawPanelListenerStrategyHand(this, undoManager, zoomManager, mainWindow, dataLayer);
        mouseListenerAddHwComponent = new DrawPanelListenerStrategyAddHwComponent(this, undoManager, zoomManager, mainWindow, dataLayer);
        mouseListenerCable = new DrawPanelListenerStrategyAddCable(this, undoManager, zoomManager, mainWindow, dataLayer);
    }
    


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        
        // GRID PAINT
        /*
        g2.setColor(Color.gray);
        grid.paintComponent(g);
        g2.setColor(Color.black);*/


        // set antialiasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // paint line that is being currently made
        if (lineInProgress) {
            g2.drawLine(lineStart.x, lineStart.y, lineEnd.x, lineEnd.y);
        }

        // DRAW cables
        markedCables.clear();
        for (AbstractComponent c : graph.getBundlesOfCables()) {
            if (!c.isMarked()) {
                //g2.draw(c);
                c.paint(g2);
            } else {
                markedCables.add(c);
            }
        }
        for (AbstractComponent c : markedCables) {
            c.paint(g2);
        }


        // DRAW HWcomponents
        markedComponents.clear();
        for (AbstractComponent c : graph.getHwComponents()) {
            if (!c.isMarked()) {
                c.paint(g2);
            } else {
                markedComponents.add(c);
            }
        }
        for (AbstractComponent c : markedComponents) {
            c.paint(g2);
        }


        // DRAW makring rectangle
        if (rectangleInProgress) {
            g2.setColor(Color.BLUE);
            g2.draw(rectangle);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
            g2.fill(rectangle);

        }
    }

    /**
     * aligns all AbstractHWcomponents in graph to grid
     */
    public void alignComponentsToGrid() {

        HashMap<AbstractHwComponent, Dimension> movedComponentsMap = new HashMap<AbstractHwComponent, Dimension>();

        for (AbstractHwComponent c : graph.getHwComponents()) {
            Point originalLocation = c.getCenterLocation();
            Point newLocation = grid.getNearestGridPoint(originalLocation);

            Dimension differenceInActualZoom = new Dimension(originalLocation.x - newLocation.x,
                    originalLocation.y - newLocation.y);

            // if component moved, add to moved 
            if (differenceInActualZoom.getWidth() != 0 || differenceInActualZoom.getHeight() != 0) {
                c.doChangePosition(zoomManager.doScaleToDefault(differenceInActualZoom), false);

                movedComponentsMap.put(c, differenceInActualZoom);
            }

        }

        // if map not empty set undoable edit
        if (!movedComponentsMap.isEmpty()) { 
            // add to undo manager
            undoManager.undoableEditHappened(new UndoableEditEvent(this,
                    new UndoableAlignComponentsToGrid(movedComponentsMap, this)));
            
            // update Undo and Redo buttons
            mainWindow.updateUndoRedoButtons();
            
            // update size of this
            this.updateSize(this.getGraph().getGraphLowerRightBound());
              
            // repaint
            this.repaint();
        }else{
            movedComponentsMap = null;
        }
    }

    
    
// ====================  IMPLEMENTATION OF Observer ======================   
    /**
     * Reaction to notification from zoom manager
     * @param o
     * @param o1 
     */
    @Override
    public void update(Observable o, Object o1) {
        //set new sizes of this (JDrawPanel)
        actualZoomArea.width = zoomManager.doScaleToActual(defaultZoomArea.width);
        actualZoomArea.height = zoomManager.doScaleToActual(defaultZoomArea.height);

        this.setSize(actualZoomArea);
        this.setPreferredSize(actualZoomArea);
        this.setMinimumSize(actualZoomArea);
        this.setMaximumSize(actualZoomArea);
    }  
// END ====================  IMPLEMENTATION OF Observer ======================      
    
    
// ================  IMPLEMENTATION OF ToolChangeInterface =================
    
    @Override
    public void removeCurrentMouseListener() {
        if (currentMouseListener != null) {
            currentMouseListener.deInitialize();
        }

        this.removeMouseListener(currentMouseListener);
        this.removeMouseMotionListener(currentMouseListener);
        this.removeMouseWheelListener(currentMouseListener);
    }

    @Override
    public DrawPanelListenerStrategy getMouseListener(MainTool tool) {
        switch (tool) {
            case HAND:
                return mouseListenerHand;
            case ADD_CABLE:
                return mouseListenerCable;
            case ADD_REAL_PC:
            case ADD_END_DEVICE:
            case ADD_SWITCH:
            case ADD_ROUTER:
                return mouseListenerAddHwComponent;

        }

        // this should never happen
        System.out.println("chyba v DrawPanel metoda getMouseListener(MainTool tool)");
        return mouseListenerHand;
    }

    @Override
    public void setCurrentMouseListener(DrawPanelListenerStrategy mouseListener) {
        currentMouseListener = mouseListener;

        this.addMouseListener(currentMouseListener);
        this.addMouseMotionListener(currentMouseListener);
        this.addMouseWheelListener(currentMouseListener);
    }
// END ==============  IMPLEMENTATION OF ToolChangeInterface ===============
    
    
    
    
// ============== IMPLEMENTATION OF DrawPanelInnerInterface ================
    
    /**
     * Gets AbstractAction corresponding to ComponentAction
     * @param action
     * @return 
     */
    @Override
    public AbstractAction getAbstractAction(ComponentAction action){
        return actions.get(action);
    }
    
    /**
     * Updates size of panel according to parameter if lowerRightCorner is
     * placed out of panel
     * @param rightDownCorner 
     */
    @Override
    public void updateSize(Point lowerRightCorner) {
        // if nothing to resize
        if (!(lowerRightCorner.x > actualZoomArea.width || lowerRightCorner.y > actualZoomArea.height)) {
            return;
        }

        // if lowerRightCorner.x is out of area
        if (lowerRightCorner.x > actualZoomArea.width) {
            // update area width
            actualZoomArea.width = lowerRightCorner.x;
        }

        // if lowerRightCorner.y is out of area
        if (lowerRightCorner.y > actualZoomArea.height) {
            // update area height
            actualZoomArea.height = lowerRightCorner.y;
        }

        // update default zoom size
        defaultZoomArea.setSize(zoomManager.doScaleToDefault(actualZoomArea.width),
                zoomManager.doScaleToDefault(actualZoomArea.height));

        // let scrool pane in editor know about the change
        this.revalidate();
    }
    
    /**
     * returns graph
     * @return 
     */
    @Override
    public GraphInterface getGraph() {
        return graph;
    }
    
    /**
     * Gets image factory
     * @return 
     */
    @Override
    public AbstractImageFactory getImageFactory() {
        return imageFactory;
    }
    
    /**
     * Sets that cable is being paint
     * @param lineInProgres
     * @param start
     * @param end 
     */
    @Override
    public void setLineInProgras(boolean lineInProgres, Point start, Point end) {
        this.lineInProgress = lineInProgres;
        lineStart = start;
        lineEnd = end;
    }
    
    
    /**
     * Sets transparent rectangle that is being paint
     * @param rectangleInProgress
     * @param rectangle 
     */
    @Override
    public void setTransparetnRectangleInProgress(boolean rectangleInProgress, Rectangle rectangle) {
        this.rectangleInProgress = rectangleInProgress;
        this.rectangle = rectangle;
    }
    
// END ============ IMPLEMENTATION OF DrawPanelOuterInterface ==============
    
    
// ============== IMPLEMENTATION OF DrawPanelOuterInterface ================
    
    @Override
    public boolean canUndo() {
        return undoManager.canRedo();
    }

    @Override
    public boolean canRedo() {
        return undoManager.canRedo();
    }

    @Override
    public void undo() {
        undoManager.undo();
    }

    @Override
    public void redo() {
        undoManager.redo();
    }

    @Override
    public boolean canZoomIn() {
        return zoomManager.canZoomIn();
    }

    @Override
    public boolean canZoomOut() {
        return zoomManager.canZoomOut();
    }

    @Override
    public void zoomIn() {
        zoomManager.zoomIn();
    }

    @Override
    public void zoomOut() {
        zoomManager.zoomOut();
    }

    @Override
    public void zoomReset() {
        zoomManager.zoomReset();
    }

    @Override
    public void addObserverToZoomManager(Observer obsrvr) {
        zoomManager.addObserver(obsrvr);
    }
    
// END ============ IMPLEMENTATION OF DrawPanelOuterInterface ==============
    
}





    /**
     * Updates size of draw panel after graph change
     
    public void updateSizeToFitComponents() {
        // find max X and max Y point in components
        int maxX = 0;
        int maxY = 0;

        for (AbstractHwComponent c : graph.getHwComponents()) {
            Point p = c.getLowerRightCornerLocation();
            if (p.x > maxX) {
                maxX = p.x;
            }
            if (p.y > maxY) {
                maxY = p.y;
            }
            //System.out.println("maxx = "+maxX + ", maxy = "+maxY);
        }

        // validate if new size is not smaller than defaultZoomAreaMin
        if (zoomManager.doScaleToDefault(maxX) < defaultZoomAreaMin.getWidth()
                && zoomManager.doScaleToDefault(maxY) < defaultZoomAreaMin.getHeight()) {
            // new size is smaller than defaultZoomAreaMin
            // set default zoom to default zoom min
            defaultZoomArea.setSize(defaultZoomAreaMin.width, defaultZoomAreaMin.height);

            // set area according to defaultZoomArea
            actualZoomArea.setSize(zoomManager.doScaleToActual(defaultZoomArea.width),
                    zoomManager.doScaleToActual(defaultZoomArea.height));
        } else {
            // update area size
            actualZoomArea.setSize(maxX, maxY);
            // update default zoom size
            defaultZoomArea.setSize(zoomManager.doScaleToDefault(actualZoomArea.width),
                    zoomManager.doScaleToDefault(actualZoomArea.height));
        }

        //System.out.println("area update");


        // let scrool pane in editor know about the change
        this.revalidate();
    }*/