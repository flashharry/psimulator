package psimulator.userInterface.SimulatorEditor.AnimationPanel;

import java.awt.Image;
import java.awt.Point;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTarget;
import psimulator.AbstractNetwork.HwTypeEnum;
import psimulator.userInterface.SimulatorEditor.DrawPanel.ZoomManager;
import psimulator.userInterface.imageFactories.AbstractImageFactory;

/**
 *
 * @author Martin
 */
public class Animation implements TimingTarget {

    //
    private Animator animator;
    //
    private AnimationPanelInnerInterface animationPanelInnerInterface;
    private ZoomManager zoomManager;
    private AbstractImageFactory imageFactory;
    //
    private int defaultZoomStartX;
    private int defaultZoomStartY;
    private int defaultZoomEndX;
    private int defaultZoomEndY;
    //
    private Image image;
    //
    private boolean visible;
    //
    private double defautlZoomWidthDifference =0.0;
    private double defautlZoomHeightDifference =0.0;

    public Animation(final AnimationPanelInnerInterface animationPanelInnerInterface,
            AbstractImageFactory imageFactory, ZoomManager zoomManager,
            Point defaultZoomSource, Point defaultZoomDest, int durationInMilliseconds) {

        this.animationPanelInnerInterface = animationPanelInnerInterface;
        this.zoomManager = zoomManager;
        this.imageFactory = imageFactory;

        //ImageIcon ii = new ImageIcon(this.getClass().getResource("/resources/toolbarIcons/editor_toolbar/cursor_hand_mod_2.png"));
        //image = ii.getImage();
        image = imageFactory.getImage(HwTypeEnum.END_DEVICE_PC, zoomManager.getIconWidth(), false);

        defaultZoomStartX = defaultZoomSource.x;
        defaultZoomStartY = defaultZoomSource.y;

        defaultZoomEndX = defaultZoomDest.x;
        defaultZoomEndY = defaultZoomDest.y;

        // single        
        animator = new Animator.Builder().
                setDuration(durationInMilliseconds, TimeUnit.MILLISECONDS).
                setStartDirection(Animator.Direction.FORWARD).
                addTarget((TimingTarget)this).build();
        
        // loop
//        animator = new Animator.Builder().
//                setDuration(durationInMilliseconds, TimeUnit.MILLISECONDS).
//                setRepeatCount(Animator.INFINITE).
//                setStartDirection(Animator.Direction.FORWARD).
//                addTarget((TimingTarget)this).build();
        
        animator.start();
    }

    public void stopAnimator() {
        animator.stop();

    }

    public Image getImage() {
        image = imageFactory.getImage(HwTypeEnum.END_DEVICE_PC, zoomManager.getIconWidth(), false);
        return image;
    }

    public int getX() {
        return (int)zoomManager.doScaleToActual(defaultZoomStartX + defautlZoomWidthDifference - (zoomManager.getIconWidthDefaultZoom()/2.0));
    }

    public int getY() {
        return (int)zoomManager.doScaleToActual(defaultZoomStartY + defautlZoomHeightDifference - (zoomManager.getIconWidthDefaultZoom()/2.0));
    }

    public boolean isVisible() {
        return visible;
    }

    private void move(double fraction) {
        defautlZoomWidthDifference = (defaultZoomEndX - defaultZoomStartX) * fraction;
        defautlZoomHeightDifference = (defaultZoomEndY - defaultZoomStartY) * fraction;
    }

    @Override
    public void begin(Animator source) {
        //
    }

    @Override
    public void end(Animator source) {
        animationPanelInnerInterface.removeAnimation(this);
    }

    @Override
    public void repeat(Animator source) {
        //
    }

    @Override
    public void reverse(Animator source) {
        //
    }

    @Override
    public void timingEvent(Animator source, double fraction) {
        move(fraction);
        //System.out.println("Fraction" + fraction);
    }
}