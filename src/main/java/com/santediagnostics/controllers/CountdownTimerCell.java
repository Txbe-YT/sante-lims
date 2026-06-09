package com.santediagnostics.controllers;
import com.santediagnostics.models.TestRequest;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.util.Duration;


public class CountdownTimerCell extends TableCell<TestRequest, String>{
    private Label timerLabel;
    private Timeline timeline;
    private TestRequest currentRequest;

    public CountdownTimerCell(){
        timerLabel = new Label();
        timerLabel.setStyle("-fx-font-weight: bold; -fx-font-family: monospace; -fx-font-size:12px;");
        setGraphic(timerLabel);
        setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    protected void updateItem(String item, boolean empty){
        super.updateItem(item, empty);

        if (empty || getTableRow() == null || getTableRow().getItem() == null){
            stopTimer();
            timerLabel.setText("");
            currentRequest = null;
            return;
        }

        currentRequest = (TestRequest) getTableRow().getItem();

        if (currentRequest.getPaymentStatus().equals("PAID") && !currentRequest.getStatus().equals("COMPLETED")) {
            startTimer();
        }
        else {
            stopTimer();
            if(currentRequest.getStatus().equals("COMPLETED")){
                timerLabel.setText("Completed!");
                timerLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold;");
            }
            else if (!currentRequest.getPaymentStatus().equals("PAID")) {
                timerLabel.setText("Awaiting Payment");
                timerLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold;");
            }
            else{
                timerLabel.setText("");
            }
        }

    }

    private void startTimer(){
        stopTimer();

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateDisplay()));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        updateDisplay();
    }

    private void stopTimer(){
        if (timeline != null){
            timeline.stop();
            timeline = null;
        }

    }

    private void updateDisplay(){
        if(currentRequest == null) return;

        String timeRemaining = currentRequest.getTimeRemainingFormatted();
        timerLabel.setText(timeRemaining);

        long remainingSeconds = currentRequest.getRemainingSeconds();
        if(remainingSeconds <= 0){
            timerLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold; -fx-font-family: monospace;");
        }
        else if(remainingSeconds < 3600){
            timerLabel.setStyle("-fx-text-fill: #ed8936; -fx-font-weight: bold; -fx-font-family: monospace;");
        }
        else if(remainingSeconds < 10800){
            timerLabel.setStyle("-fx-text-fill: #ecc94b; -fx-font-weight: bold; -fx-font-family: monospace;");
        }
        else{
            timerLabel.setStyle("-fx-text-fill: #48bb78; -fx-font-weight: bold; -fx-font-family: monospace;");
        }
    }
}
