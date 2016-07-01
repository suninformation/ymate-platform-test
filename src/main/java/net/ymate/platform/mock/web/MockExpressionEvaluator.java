package net.ymate.platform.mock.web;

import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.*;


@SuppressWarnings("deprecation")
public class MockExpressionEvaluator extends ExpressionEvaluator {

    private final PageContext pageContext;

    public MockExpressionEvaluator(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    @SuppressWarnings("rawtypes")
    public Expression parseExpression(final String expression, final Class expectedType,
                                      final FunctionMapper functionMapper) throws ELException {

        return new Expression() {

            public Object evaluate(VariableResolver variableResolver) throws ELException {
                return doEvaluate(expression, expectedType, functionMapper);
            }
        };
    }

    @SuppressWarnings("rawtypes")
    public Object evaluate(String expression, Class expectedType, VariableResolver variableResolver,
                           FunctionMapper functionMapper) throws ELException {

        if (variableResolver != null) {
            throw new IllegalArgumentException("Custom VariableResolver not supported");
        }
        return doEvaluate(expression, expectedType, functionMapper);
    }

    @SuppressWarnings("rawtypes")
    protected Object doEvaluate(String expression, Class expectedType, FunctionMapper functionMapper)
            throws ELException {

        if (functionMapper != null) {
            throw new IllegalArgumentException("Custom FunctionMapper not supported");
        }
        try {
            return ExpressionEvaluatorManager.evaluate("JSP EL expression", expression, expectedType, this.pageContext);
        } catch (JspException ex) {
            throw new ELException("Parsing of JSP EL expression \"" + expression + "\" failed", ex);
        }
    }

}
