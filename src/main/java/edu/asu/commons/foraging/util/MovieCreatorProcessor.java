package edu.asu.commons.foraging.util;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.AddClientEvent;
import edu.asu.commons.foraging.facilitator.GroupView;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.ServerDataModel;

/**
 * $Id$
 * 
 * Foraging save file processor to create quicktime movies.
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 522 $
 */
class MovieCreatorProcessor extends SaveFileProcessor.Base {

    private VideoFormat videoFormat;

    public MovieCreatorProcessor() {
        this(VideoFormat.PNG);
    }

    public MovieCreatorProcessor(VideoFormat videoFormat) {
        this.videoFormat = videoFormat;
    }

    @Override
    public void process(SavedRoundData savedRoundData, OutputStream stream) {
        // hmm, there needs to be one output stream per group because we write 1 video per group.
        //                QuickTimeOutputStream quickTimeOutputStream = new QuickTimeOutputStream(stream, videoFormat);
        ServerDataModel serverDataModel = (ServerDataModel) savedRoundData.getDataModel();
        RoundConfiguration roundConfiguration = (RoundConfiguration) savedRoundData.getRoundParameters();
        final List<GroupView> groupViewList = new ArrayList<GroupView>();
        final Map<GroupView, QuickTimeOutputStream> groupViewMap = new HashMap<GroupView, QuickTimeOutputStream>();
        serverDataModel.reinitialize(roundConfiguration);
        List<JFrame> jframes = new ArrayList<JFrame>();
        Dimension dimension = new Dimension(800, 800);
        File savedRoundDataFile = new File(savedRoundData.getSaveFilePath());
        String saveFilePath = savedRoundDataFile.getName();
        for (GroupDataModel groupDataModel: serverDataModel.getGroups()) {
            GroupView groupView = new GroupView(dimension, groupDataModel);
            groupView.setup(roundConfiguration);
            groupViewList.add(groupView);
            try {
                File groupMovieFile = new File(savedRoundDataFile.getCanonicalPath() + "-group-" + groupDataModel.getGroupId() + "-" + saveFilePath + ".mov");
                groupViewMap.put(groupView, new QuickTimeOutputStream(groupMovieFile, videoFormat));
            }
            catch (IOException exception) {
                exception.printStackTrace();
            }
            JFrame jframe = new JFrame("Group: " + groupDataModel.getGroupId());
            jframe.add(groupView);
            jframe.pack();
            jframe.setVisible(true);
            jframes.add(jframe);
        }
        // grab out all add client events to initialize the state of the game properly.
        for (PersistableEvent event: savedRoundData.getActions()) {
            if (event instanceof AddClientEvent) {
                System.err.println("Adding client: " + event);
                serverDataModel.apply(event);
            }
        }
        IntervalChecker intervalChecker = new IntervalChecker();
        // 10 frames per second
        intervalChecker.setUnitsPerInterval(100);

        for (PersistableEvent event: savedRoundData.getActions()) {
            final long elapsedTimeInMillis = savedRoundData.getElapsedTime(event);

            serverDataModel.apply(event);
            if ( intervalChecker.isIntervalElapsed(elapsedTimeInMillis) && serverDataModel.isDirty() ) {

                for (final GroupView groupView: groupViewList) {
                    try {
                        //                        	groupView.repaint();
                        final Dimension groupViewSize = groupView.getSize();
                        SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                Graphics2D graphics = (Graphics2D) groupView.getGraphics();
                                BufferedImage bufferedImage = graphics.getDeviceConfiguration().createCompatibleImage(groupViewSize.width, groupViewSize.height);
                                Graphics2D bufferedImageGraphics = bufferedImage.createGraphics();
                                groupView.paint(bufferedImageGraphics);
                                try {
                                    // 600 time scale units = 1 s
                                	// 10 fps = one frame per 60 time scale units
                                    groupViewMap.get(groupView).writeFrame(bufferedImage, 60);
                                }
                                catch (IOException exception) {
                                    exception.printStackTrace();
                                    throw new RuntimeException(exception);
                                }
                            }
                        });
                    } 
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    } 
                    catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
                serverDataModel.setDirty(false);

            }

        }
        for (QuickTimeOutputStream out : groupViewMap.values()) {
            try {
                out.close();
            }
            catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        for (JFrame jframe : jframes) {
            jframe.setVisible(false);
            jframe.dispose();
        }
        groupViewMap.clear();
    }


    @Override
    public void process(SavedRoundData savedRoundData, PrintWriter writer) {
        throw new UnsupportedOperationException("This method is not suitable for this class (consider removing as requirement for super class)");
    }

    @Override
    public String getOutputFileExtension() {
        String videoFormatString = videoFormat.toString().toLowerCase();
        return String.format("-%s-movie.%s", videoFormatString, videoFormatString);
    }
}
