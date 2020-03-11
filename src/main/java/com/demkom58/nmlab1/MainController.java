package com.demkom58.nmlab1;

import com.demkom58.divine.chart.ExtendedLineChart;
import com.demkom58.divine.gui.GuiController;
import com.demkom58.divine.util.AlertUtil;
import com.demkom58.divine.util.Language;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.Function;

import java.util.ArrayList;
import java.util.List;

public class MainController extends GuiController {
    @FXML
    private TextField functionInput;
    @FXML
    private TextField fromAInput;
    @FXML
    private TextField toBInput;
    @FXML
    private TextField accuracyInput;
    @FXML
    private ExtendedLineChart<Double, Double> lineChart;
    @FXML
    private CheckBox onceCheckBox;

    private Function function;
    private Expression expression;
    private double aInterval;
    private double bInterval;
    private double precision;

    private double a;
    private double b;

    private int iteration = 1;

    private List<String> iterations = new ArrayList<>();

    private XYChart.Series<Double, Double> functionSeries =
            new XYChart.Series<>("Функція", FXCollections.observableArrayList());

    @Override
    public void init() {
        super.init();
        lineChart.getData().add(functionSeries);
        read();
    }

    @FXML
    public void bisection(MouseEvent event) {
        try {
            check();
        } catch (Exception e) {
            AlertUtil.showErrorMessage(e);
            return;
        }

        double fa = function.calculate(a);
        double x = (a + b) / 2;

        if ((fa * function.calculate(x)) < 0)
            this.b = x;
        else
            this.a = x;

        lineChart.addVerticalValueMarker(new XYChart.Data<>(x, 0d));

        if (Math.abs(b - a) <= precision) {
            showResult(iteration, precision, x);
            read();
            return;
        }

        showProgress(iteration, x);

        iteration++;

        if (onceCheckBox.isSelected())
            bisection(event);
    }

    @FXML
    public void secant(MouseEvent event) {
        try {
            check();
        } catch (Exception e) {
            AlertUtil.showErrorMessage(e);
            return;
        }

        double fa = function.calculate(a);
        double fb = function.calculate(b);
        double m = (fb - fa) / (b - a);
        double diferencia;

        final XYChart.Series<Double, Double> iterationSeries = new XYChart.Series<>();
        final ObservableList<XYChart.Data<Double, Double>> data = iterationSeries.getData();
        iterationSeries.setName("Ітерація " + iteration);
        data.addAll(new XYChart.Data<>(a, fa), new XYChart.Data<>(b, fb));
        lineChart.getData().add(iterationSeries);

        double x = ((-fa + m * a) / m);
        if ((fa * function.calculate(x)) < 0) {
            diferencia = Math.abs(b - x);
            b = x;
        } else {
            diferencia = Math.abs(a - x);
            a = x;
        }
        lineChart.addVerticalValueMarker(new XYChart.Data<>(x, 0d));

        if (((function.calculate(x)) <= precision) && (diferencia <= precision)) {
            showResult(iteration, precision, x);
            read();
            return;
        }

        showProgress(iteration, x);

        iteration++;

        if (onceCheckBox.isSelected())
            secant(event);
    }

    @SuppressWarnings("unchecked")
    @FXML
    public void tangent(MouseEvent event) {
        try {
            check();
        } catch (Exception e) {
            AlertUtil.showErrorMessage(e);
            return;
        }

        final double fa = function.calculate(a);
        final Expression ex = new Expression(
                "der(" + expression.getExpressionString() + ", x, " + a + ")"
        );

        final double calculate = ex.calculate();
        if (calculate == 0) {
            AlertUtil.showErrorMessage("Помилка. Ділення на нуль.");
            read();
            return;
        }

        final XYChart.Series<Double, Double> iterationSeries = new XYChart.Series<>();
        final ObservableList<XYChart.Data<Double, Double>> data = iterationSeries.getData();
        iterationSeries.setName("Ітерація " + iteration);

        lineChart.addVerticalValueMarker(new XYChart.Data<>(b, 0d));
        if (a <= b)
            data.addAll(new XYChart.Data<>(a, fa), new XYChart.Data<>(b, 0d));
        else
            data.addAll(new XYChart.Data<>(b, 0d), new XYChart.Data<>(a, fa));

        lineChart.getData().add(iterationSeries);

        b = a - (fa / calculate);
        if ((Math.abs(b - a) <= precision)) {
            showResult(iteration, precision, b);
            read();
            return;
        }

        showProgress(iteration, b);

        a = b;
        iteration++;

        if (onceCheckBox.isSelected())
            tangent(event);
    }

    @FXML
    public void onChanged(KeyEvent event) {
        read();
    }

    private void showResult(int iteration, double precision, double result) {
        final String plural = Language.plural(iteration, "ітерацію", "ітерації", "ітерацій");

        String numIterations = "Результат знайдений за " + iteration + " " + plural + ".";
        String precisionResult = "Результат з точністю " + precision + " дорівнює " + result + ".";

        if (onceCheckBox.isSelected()) {
            AlertUtil.showInfoMessage(numIterations + "\n" + precisionResult, String.join("\n", iterations));
        } else {
            AlertUtil.showInfoMessage(numIterations, precisionResult);
        }
    }

    private void showProgress(int iteration, double result) {
        String iter = "Ітерація: " + iteration + ".";
        String root = "Орієнтовний корінь: " + result + ".";
        if (onceCheckBox.isSelected()) {
            iterations.add(iter + " " + root);
            return;
        }

        AlertUtil.showInfoMessage(iter, root);
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

    private void fillFunctionSeries(Double intervalA, Double intervalB) {
        lineChart.getData().add(functionSeries);
        lineChart.removeHorizontalValueMarkers();
        lineChart.removeVerticalValueMarkers();
        functionSeries.setData(FXCollections.observableArrayList());

        double y, x;
        x = intervalA - 5.0;

        final double end = 2 * (intervalB * 10 - intervalA * 10) + 50;
        for (int i = 0; i < end; i++) {
            x = x + 0.1;
            y = function.calculate(x);
            final XYChart.Data<Double, Double> data = new XYChart.Data<>(x, y);

            if (i != 0 && i != end - 1) {
                final Rectangle rectangle = new Rectangle(0, 0);
                rectangle.setVisible(false);
                data.setNode(rectangle);
            }

            functionSeries.getData().add(data);
        }


        final XYChart.Series<Double, Double> intervalSeries =
                new XYChart.Series<>("Проміжок", FXCollections.observableArrayList());

        intervalSeries.getData().addAll(
                new XYChart.Data<>(intervalA, 0d),
                new XYChart.Data<>(intervalB, 0d)
        );

        lineChart.getData().add(intervalSeries);
    }

    private void read() {
        iteration = 1;
        iterations.clear();

        if (!lineChart.getData().isEmpty())
            lineChart.setData(FXCollections.observableArrayList());

        String functionText = functionInput.getText();
        if (functionText.isBlank()) {
            final String promptText = functionInput.getPromptText();
            functionText = promptText.substring(promptText.indexOf("f(x) = "));
        }

        if (!functionText.isEmpty() && !functionText.startsWith("f(x) = "))
            return;

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

        final String bIntervalText = toBInput.getText();
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

        if (function.checkSyntax())
            fillFunctionSeries(aInterval, bInterval);
    }

}
