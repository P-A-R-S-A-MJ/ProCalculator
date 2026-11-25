package procalculator.engine;

import java.util.ArrayList;
import procalculator.ast.AstUtils;
import procalculator.ast.Expression;

public class NumericalEngine {
    public static void solveNewtonAST(Expression f, String format, double minRange, double maxRange, String varName) {
        Expression df = f.derive();
        ArrayList<Double> answers = new ArrayList<>();

        for (double startX = minRange - 1; startX < maxRange + 1; startX += 0.5) {
            double x = startX;
            for (int j = 0; j < 50; j++) {
                double fx = f.evaluate(x);
                double dx = df.evaluate(x);
                if (Double.isNaN(dx) || Double.isInfinite(dx) || Double.isNaN(fx) || Double.isInfinite(fx)
                        || Math.abs(dx) < (Math.pow(10, -8))) {
                    break;
                }
                double nextx = x - (fx / dx);
                if (Math.abs((nextx - x)) < Math.pow(10, -8)) {
                    x = nextx;
                    break;
                } else {
                    x = nextx;
                }
            }
            double finalFx = f.evaluate(x);
            double finalDx = df.evaluate(x);
            if (Math.abs(finalFx) < Math.pow(10, -3)
                    || (!Double.isNaN(finalFx / finalDx) && Math.abs(finalFx / finalDx) < Math.pow(10, -7))) {
                boolean isDuplicate = false;
                if (x >= minRange - 0.001 && x <= maxRange + 0.001) {
                    for (int j = 0; j < answers.size(); j++) {
                        if (Math.abs(x - (answers.get(j))) < Math.pow(10, -3)) {
                            isDuplicate = true;
                            break;
                        }
                    }
                    if (!(isDuplicate)) {
                        answers.add(x);
                    }
                }
            }
        }
        java.util.Collections.sort(answers);
        if (answers.size() == 0) {
            System.out.println("This equation does not have any answers!!");
        } else if (answers.size() == 1) {
            double answer = answers.get(0);
            System.out.println("The answer : " + varName + " = " + AstUtils.format(answer));
        } else {
            System.out.println("Answer's : ");
            for (int i = 0; i < answers.size(); i++) {
                double answer = answers.get(i);
                if (i == (answers.size() - 1)) {
                    System.out.print(varName + (i + 1) + " = " + AstUtils.format(answer) + "\n");
                } else {
                    System.out.print(varName + (i + 1) + " = " + AstUtils.format(answer) + " , ");
                }
            }
        }
    }

    public static double calculateIntegral(Expression f, double a, double b) {
        double area = 0;
        double dx = 0.00001;
        int sign = 1;
        if (a > b) {
            double temp = a;
            a = b;
            b = temp;
            sign = -1;
        }
        for (double x = a; x < b; x += dx) {
            area = (f.evaluate(x) * dx) + area;
        }
        return (sign * area);
    }

}