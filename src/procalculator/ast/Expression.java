package procalculator.ast;

public interface Expression {
    double evaluate(double x);

    Expression Simplify();

    Expression integrate();

    Expression derive();

    String print();
}