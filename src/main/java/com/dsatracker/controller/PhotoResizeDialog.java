package com.dsatracker.controller;

import com.dsatracker.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;

import java.io.File;

/**
 * Lets the user zoom and pan a chosen photo inside a circular viewport before it is cropped
 * and saved as the profile picture. Rendering a fixed, known-resolution crop here - rather than
 * copying the source file as-is and letting the avatar circle stretch whatever pixels it has -
 * is what keeps the avatar sharp regardless of the source photo's original size or aspect ratio.
 */
final class PhotoResizeDialog extends Dialog<WritableImage> {

    private static final double VIEWPORT_SIZE = 260;
    private static final double OUTPUT_SIZE = 512;

    private final ImageView imageView = new ImageView();
    // A plain Pane never resizes or repositions its children (unlike StackPane, which would
    // fight our manual fitWidth/translate math on every layout pass to force the image to fill
    // the pane) - it just clips whatever we place at whatever translate we set, which is exactly
    // the manual control a drag/zoom crop needs.
    private final Pane viewport = new Pane(imageView);

    private double baseWidth;
    private double baseHeight;
    private double dragStartX;
    private double dragStartY;
    private double dragStartTranslateX;
    private double dragStartTranslateY;

    PhotoResizeDialog(final ThemeManager themeManager, final File sourceFile) {
        setTitle("Resize Profile Picture");
        getDialogPane().getStylesheets().add(
                getClass().getResource(themeManager.getStylesheetPath()).toExternalForm());
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final Image source = new Image(sourceFile.toURI().toString());

        imageView.setImage(source);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        final double coverScale = Math.max(VIEWPORT_SIZE / source.getWidth(), VIEWPORT_SIZE / source.getHeight());
        baseWidth = source.getWidth() * coverScale;
        baseHeight = source.getHeight() * coverScale;
        imageView.setFitWidth(baseWidth);
        imageView.setFitHeight(baseHeight);
        centerImage();

        final Rectangle clip = new Rectangle(VIEWPORT_SIZE, VIEWPORT_SIZE);
        clip.setArcWidth(VIEWPORT_SIZE);
        clip.setArcHeight(VIEWPORT_SIZE);
        viewport.setClip(clip);
        // Min must be pinned too, not just pref/max - otherwise a tall or wide source photo
        // (whose "cover" fit can be much bigger than VIEWPORT_SIZE in one dimension) inflates
        // the pane's computed minimum past the cap, which wins over maxSize and balloons
        // the whole dialog, shoving the OK/Cancel buttons off the bottom.
        viewport.setMinSize(VIEWPORT_SIZE, VIEWPORT_SIZE);
        viewport.setPrefSize(VIEWPORT_SIZE, VIEWPORT_SIZE);
        viewport.setMaxSize(VIEWPORT_SIZE, VIEWPORT_SIZE);
        viewport.setStyle("-fx-background-color: black; -fx-cursor: move;");
        viewport.setOnMousePressed(this::onDragStart);
        viewport.setOnMouseDragged(this::onDrag);

        final Slider zoomSlider = new Slider(1, 3, 1);
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> applyZoom(newVal.doubleValue()));

        final Label hint = new Label("Drag the photo to reposition it, use the slider to zoom.");
        hint.getStyleClass().add("page-subtitle");
        hint.setWrapText(true);

        final VBox content = new VBox(14, viewport, zoomSlider, hint);
        content.setPadding(new Insets(16));
        content.setAlignment(Pos.CENTER);
        content.setPrefWidth(VIEWPORT_SIZE + 32);

        getDialogPane().setContent(content);
        getDialogPane().getStyleClass().add("form-dialog");

        setResultConverter(buttonType -> buttonType == ButtonType.OK ? renderCrop() : null);
    }

    private void onDragStart(final MouseEvent event) {
        dragStartX = event.getSceneX();
        dragStartY = event.getSceneY();
        dragStartTranslateX = imageView.getTranslateX();
        dragStartTranslateY = imageView.getTranslateY();
    }

    private void onDrag(final MouseEvent event) {
        final double deltaX = event.getSceneX() - dragStartX;
        final double deltaY = event.getSceneY() - dragStartY;
        setClampedTranslate(dragStartTranslateX + deltaX, dragStartTranslateY + deltaY);
    }

    private void applyZoom(final double zoom) {
        imageView.setFitWidth(baseWidth * zoom);
        imageView.setFitHeight(baseHeight * zoom);
        setClampedTranslate(imageView.getTranslateX(), imageView.getTranslateY());
    }

    /** Re-centers the image (translate 0,0 is the pane's top-left corner, not its middle). */
    private void centerImage() {
        setClampedTranslate((VIEWPORT_SIZE - imageView.getFitWidth()) / 2,
                (VIEWPORT_SIZE - imageView.getFitHeight()) / 2);
    }

    /** Keeps the image covering the whole viewport at all times - the crop must never expose empty space. */
    private void setClampedTranslate(final double x, final double y) {
        final double minX = VIEWPORT_SIZE - imageView.getFitWidth();
        final double minY = VIEWPORT_SIZE - imageView.getFitHeight();
        imageView.setTranslateX(clamp(x, minX, 0));
        imageView.setTranslateY(clamp(y, minY, 0));
    }

    private static double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    private WritableImage renderCrop() {
        final SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        params.setTransform(new Scale(OUTPUT_SIZE / VIEWPORT_SIZE, OUTPUT_SIZE / VIEWPORT_SIZE));
        return viewport.snapshot(params, new WritableImage((int) OUTPUT_SIZE, (int) OUTPUT_SIZE));
    }
}
