package simulation.main;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import simulation.camera.Camera;
import simulation.display.SpatialEntityCanvas;
import simulation.entity.SpatialEntity;
import simulation.physics.UniversePhysics;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application {

    private int mFrameDelay = 40;
    private boolean mRunning = true;

    protected List<SpatialEntity> mEntities;
    private SpatialEntityCanvas mCanvas;
    private Camera mCamera;

    private ExecutorService mExecutorService;

    private SimulationTask mSimulationTask;

    /**
     * http://cowboyprogramming.com/2008/04/01/practical-fluid-mechanics/
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Fluid simulation");

        mCamera = new Camera(0, 0, 0);

        Group root = new Group();
        mCanvas = new SpatialEntityCanvas(1200, 800, mCamera);

        root.getChildren().add(mCanvas);
        stage.setScene(new Scene(root));
        stage.show();

        // TODO: Add all the other buttons later, but for now, just start things.
        mEntities = Setup.create();

        runSimulation();
    }

    public int getFrameDelay() {
        return mFrameDelay;
    }

    public boolean isRunning() {
        return mRunning;
    }

    public void runSimulation() {
        mExecutorService = Executors.newSingleThreadExecutor();
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        // TODO: It looks like this is not calling this every getFrameDelay() amount of time, but instead
        // calling it, then waiting getFrameDelay() amount of time, and calling it again.
        KeyFrame increment = new KeyFrame(Duration.millis(getFrameDelay()), e -> increment());
        timeline.getKeyFrames().add(increment);
        timeline.play();
    }


    protected void increment() {
        // Perform physics simulations

        if (mSimulationTask == null || !mSimulationTask.isRunning()) {
            mSimulationTask = new SimulationTask(mEntities);

            // TODO:
            // Looks like this is not returning to the main UI thread, unlike Android Async tasks.
            // Unsure though, how to do the painting, other than calling Platform.runLater()
            mSimulationTask.setOnSucceeded(e -> {
                mEntities = mSimulationTask.getValue();

                // Repaint everything, after physics simulations are done.
                // TODO: How does Javafx handle buffering?
                Platform.runLater(() -> {
                    // TODO: Perhaps create a separate pause camera button?
                    mCamera.move();

                    // tell graphics to repaint
                    mCanvas.drawEntities(mEntities);
                });
            });
            mExecutorService.submit(mSimulationTask);
        }

    }

    private static class SimulationTask extends Task<List<SpatialEntity>> {

        List<SpatialEntity> mEntities;

        public SimulationTask(List<SpatialEntity> entities) {
            mEntities = entities;
        }

        @Override
        protected List<SpatialEntity> call() throws Exception {
            return UniversePhysics.updateUniverseState(mEntities);
        }
    }
}
