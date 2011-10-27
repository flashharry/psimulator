package psimulator.userInterface.Editor;

import psimulator.userInterface.Editor.DrawPanel.ZoomEventWrapper;
import psimulator.userInterface.Editor.DrawPanel.DrawPanelOuterInterface;
import psimulator.userInterface.Editor.DrawPanel.DrawPanel;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.userInterface.MainWindowInnerInterface;
import psimulator.userInterface.imageFactories.AbstractImageFactory;
import psimulator.userInterface.imageFactories.AwtImageFactory;

/**
 *
 * @author Martin
 */
public class EditorPanel extends EditorOuterInterface implements Observer{

    private EditorToolBar jToolBarEditor;
    //private DrawPanel jPanelDraw;
    private DrawPanelOuterInterface jPanelDraw;
    private JScrollPane jScrollPane;
    private AbstractImageFactory imageFactory;
    private MainWindowInnerInterface mainWindow;
    private DataLayerFacade dataLayer;

    public EditorPanel(MainWindowInnerInterface mainWindow, DataLayerFacade dataLayer) {
        super(new BorderLayout());
        
        this.mainWindow = mainWindow;
        this.dataLayer = dataLayer;
        
        imageFactory = new AwtImageFactory();

        // set border
        this.setBorder(new BevelBorder(BevelBorder.LOWERED));

        // create draw panel
        jPanelDraw = new DrawPanel(mainWindow, imageFactory, dataLayer);
        
        //create scroll pane
        jScrollPane = new JScrollPane(jPanelDraw);

        // add scroll bars
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // add scroll pane to panel
        this.add(jScrollPane, BorderLayout.CENTER);
        
        // create tool bar and add to panel
        jToolBarEditor = new EditorToolBar(dataLayer, imageFactory, jPanelDraw);
        
        // add tool bar to panel
        this.add(jToolBarEditor, BorderLayout.WEST);

        // add listener for FitToSize button
        jToolBarEditor.addToolActionFitToSizeListener(new JMenuToolFitToSizeActionListener());
        
        // add listener for AlignToGrid button
        jToolBarEditor.addToolActionAlignToGridListener(new JMenuToolAlignToGridActionListener());
        
        // set default tool in ToolBar
        jToolBarEditor.setDefaultTool();
    }
    
    @Override
    public void init(){
        //jPanelDraw.getZoomManager().addObserver(this);
        jPanelDraw.addObserverToZoomManager(this);
    }

    /**
     * reaction to zoom event
     * @param o
     * @param o1 
     */
    @Override
    public void update(Observable o, Object o1) {
        ZoomEventWrapper zoomWrapper = (ZoomEventWrapper) o1;
        // set viewport
        Point newViewPos = new Point();
        Rectangle oldView = jScrollPane.getViewport().getViewRect();
        
        
        // update zoom buttons in main window
        mainWindow.updateZoomButtons();
        // repaint
        this.revalidate();
        this.repaint();
    }
   
    /////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for FitToSize button
     */
    class JMenuToolFitToSizeActionListener implements ActionListener {

        /**
         * calls zoom operation on jPanelEditor according to actionCommand
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            // update jPanelDraw size
            //jPanelDraw.updateSizeToFitComponents();
        }
    }
    
    /////////////////////-----------------------------------////////////////////
    /**
     * Action Listener for AlignToGrid button
     */
    class JMenuToolAlignToGridActionListener implements ActionListener {

        /**
         * calls zoom operation on jPanelEditor according to actionCommand
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            // align components in DrawPanel
            //jPanelDraw.alignComponentsToGrid();
            
        }
    }
    

    @Override
    public boolean canUndo() {
        return jPanelDraw.canUndo();
        //return jPanelDraw.getUndoManager().canUndo();
    }

    @Override
    public boolean canRedo() {
        return jPanelDraw.canRedo();
        //return jPanelDraw.getUndoManager().canRedo();
    }

    @Override
    public void undo() {
        jPanelDraw.undo();
        //jPanelDraw.getUndoManager().undo();
    }

    @Override
    public void redo() {
        jPanelDraw.redo();
        //jPanelDraw.getUndoManager().redo();
    }

    @Override
    public boolean canZoomIn() {
        return jPanelDraw.canZoomIn();
        //return jPanelDraw.getZoomManager().canZoomIn();
    }

    @Override
    public boolean canZoomOut() {
        return jPanelDraw.canZoomOut();
        //return jPanelDraw.getZoomManager().canZoomOut();
    }

    @Override
    public void zoomIn() {
        // TODO: Point of zoom in parameter
        jPanelDraw.zoomIn();
        //jPanelDraw.getZoomManager().zoomIn();
    }

    @Override
    public void zoomOut() {
        // TODO: Point of zoom in parameter
        jPanelDraw.zoomOut();
        //jPanelDraw.getZoomManager().zoomOut();
    }

    @Override
    public void zoomReset() {
        // TODO: Point of zoom in parameter
        jPanelDraw.zoomReset();
        //jPanelDraw.getZoomManager().zoomReset();
    }

}