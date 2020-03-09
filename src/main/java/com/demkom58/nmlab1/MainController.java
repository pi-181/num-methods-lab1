package com.demkom58.nmlab1;

import com.demkom58.divine.gui.GuiController;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class MainController extends GuiController {

    @FXML private TextField functionInput;
    @FXML private TextField fromAInput;
    @FXML private TextField toBInput;
    @FXML private TextField accuracyInput;

    @Override
    public void init() {
        super.init();
    }

    @FXML
    public void bisection(MouseEvent event) {

    }

    @FXML
    public void secant(MouseEvent event) {

    }

    @FXML
    public void tanget(MouseEvent event) {

    }

}
