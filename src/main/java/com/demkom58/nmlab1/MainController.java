package com.demkom58.nmlab1;

import com.demkom58.divine.gui.GuiController;
import com.demkom58.divine.util.Language;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import org.jetbrains.annotations.NotNull;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.Function;

public class MainController extends GuiController {

    @FXML
    private TextField functionInput;
    @FXML
    private TextField fromAInput;
    @FXML
    private TextField toBInput;
    @FXML
    private TextField accuracyInput;

    private Function function;
    private Expression expression;
    private double aInterval;
    private double bInterval;
    private double precision;

    private double a;
    private double b;

    private int iteration = 1;

    @Override
    public void init() {
        super.init();
        read();
    }

    @FXML
    public void bisection(MouseEvent event) {
        try {
            check();
        } catch (Exception e) {
            showErrorMessage(e);
            return;
        }

        double fa = function.calculate(a);
        double x = (a + b) / 2;

        if ((fa * function.calculate(x)) < 0)
            this.b = x;
        else
            this.a = x;

        if (Math.abs(b - a) <= precision) {
            showResult(iteration, precision, x);
            read();
            return;
        }

        showProgress(iteration, x);

        iteration++;
    }

    @FXML
    public void secant(MouseEvent event) {
        try {
            check();
        } catch (Exception e) {
            showErrorMessage(e);
            return;
        }

        double fa = function.calculate(a);
        double fb = function.calculate(b);
        double m = (fb - fa) / (b - a);
        double diferencia;

        double x = ((-fa + m * a) / m);
        if ((fa * function.calculate(x)) < 0) {
            diferencia = Math.abs(b - x);
            b = x;
        } else {
            diferencia = Math.abs(a - x);
            a = x;
        }

        if (((function.calculate(x)) <= precision) && (diferencia <= precision)) {
            showResult(iteration, precision, x);
            read();
            return;
        }

        showProgress(iteration, x);

        iteration++;
    }

    @FXML
    public void tanget(MouseEvent event) {
        try {
            check();
        } catch (Exception e) {
            showErrorMessage(e);
            return;
        }

        final double fa = function.calculate(a);
        final Expression ex = new Expression(
                "der(" + expression.getExpressionString() + ", x, " + a + ")"
        );

        final double calculate = ex.calculate();
        if (calculate == 0) {
            showErrorMessage("Помилка. Ділення на нуль.");
            read();
            return;
        }

        b = a - (fa / calculate);
        if ((Math.abs(b - a) <= precision)) {
            showResult(iteration, precision, b);
            read();
            return;
        }

        showProgress(iteration, b);

        a = b;
        iteration++;
    }

    @FXML
    public void onChanged(KeyEvent event) {
        read();
    }

    private void showResult(int iteration, double precision, double result) {
        final String plural = Language.plural(iteration, "ітерацію", "ітерації", "ітерацій");
        showInfoMessage(
                "Результат знайдений за " + iteration + " " + plural + ".",
                "Результат з точністю " + precision + " дорівнює " + result
        );
    }

    private void showProgress(int iteration, double result) {
        showInfoMessage(
                "Ітерація: " + iteration + ".",
                "Орієнтовний корінь: " + result + "."
        );
    }

    private void check() throws IllegalStateException {
        if (!function.checkSyntax())
            throw new IllegalStateException("Перевірте введену функцію.\n" + function.getErrorMessage());

        if (aInterval == Double.MIN_VALUE)
            throw new IllegalStateException("Перевірте введені дані для змінної 'a'.");

        if (bInterval == Double.MIN_VALUE)
            throw new IllegalStateException("Перевірте введені дані для змінної 'b'.");

        if (precision == Double.MIN_VALUE)
            throw new IllegalStateException("Перевірте задану точність.");
    }

    private void read() {
        iteration = 0;

        String functionText = functionInput.getText();
        if (functionText.isBlank()) {
            final String promptText = functionInput.getPromptText();
            functionText = promptText.substring(promptText.indexOf("f(x) = "));
        }

        function = new Function(functionText);
        expression = new Expression(functionText.substring("f(x) = ".length()));

        final String aIntervalText = fromAInput.getText();

        try {
            aInterval = aIntervalText.isBlank()
                    ? Double.parseDouble(fromAInput.getPromptText())
                    : Double.parseDouble(aIntervalText);
        } catch (NumberFormatException e) {
            aInterval = Double.MIN_VALUE;
            return;
        }

        final String bIntervalText = fromAInput.getText();
        try {
            bInterval = bIntervalText.isBlank()
                    ? Double.parseDouble(toBInput.getPromptText())
                    : Double.parseDouble(bIntervalText);
        } catch (NumberFormatException e) {
            bInterval = Double.MIN_VALUE;
            return;
        }

        final String precisionText = accuracyInput.getText();
        try {
            precision = precisionText.isBlank()
                    ? Double.parseDouble(accuracyInput.getPromptText())
                    : Double.parseDouble(precisionText);

            if (precision <= 0) {
                precision = 0.1;
            }
        } catch (NumberFormatException e) {
            precision = Double.MIN_VALUE;
            return;
        }


        a = aInterval;
        b = bInterval;
    }

    private void showErrorMessage(@NotNull final Exception exception) {
        showErrorMessage(exception.getMessage());
    }

    private void showErrorMessage(@NotNull final String error) {
        alert(Alert.AlertType.ERROR, "Помилка", error, ButtonType.OK);
    }

    private void showInfoMessage(@NotNull final String header, @NotNull final String info) {
        alert(Alert.AlertType.INFORMATION, header, info, ButtonType.OK);
    }

    private void alert(Alert.AlertType type, String header, String message, ButtonType... types) {
        Alert alert = new Alert(type, message, types);
        alert.setHeaderText(header);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.show();
    }

}
