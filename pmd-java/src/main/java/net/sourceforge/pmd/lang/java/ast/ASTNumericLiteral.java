/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import java.math.BigInteger;
import java.util.Locale;


/**
 * A numeric literal of any type (double, int, long, float, etc).
 */
public class ASTNumericLiteral extends AbstractJavaTypeNode implements ASTLiteral {


    // by default is double
    // TODO all of this can be done in jjtCloseNodeScope
    private boolean isInt;
    private boolean isFloat;


    public ASTNumericLiteral(int id) {
        super(id);
    }


    public ASTNumericLiteral(JavaParser p, int id) {
        super(p, id);
    }


    /**
     * Accept the visitor. *
     */
    @Override
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    void setIntLiteral() {
        this.isInt = true;
    }


    void setFloatLiteral() {
        this.isFloat = true;
    }


    @Override
    public boolean isIntLiteral() {
        String image = getImage();
        if (isInt && image != null && image.length() > 0) {
            return !image.endsWith("l") && !image.endsWith("L");
        }
        return false;
    }


    /**
     * Checks whether this literal is a long integer.
     *
     * @return <code>true</code> if this literal is a long
     */
    @Override
    public boolean isLongLiteral() {
        String image = getImage();
        if (isInt && image != null && image.length() > 0) {
            return image.endsWith("l") || image.endsWith("L");
        }
        return false;
    }



    @Override
    public boolean isFloatLiteral() {
        String image = getImage();
        if (isFloat && image != null && image.length() > 0) {
            char lastChar = image.charAt(image.length() - 1);
            return lastChar == 'f' || lastChar == 'F';
        }
        return false;
    }


    /**
     * Checks whether this literal describes a double.
     *
     * @return <code>true</code> if this literal is a double.
     */
    @Override
    public boolean isDoubleLiteral() {
        String image = getImage();
        if (isFloat && image != null && image.length() > 0) {
            char lastChar = image.charAt(image.length() - 1);
            return lastChar == 'd' || lastChar == 'D' || Character.isDigit(lastChar) || lastChar == '.';
        }
        return false;
    }


    private String stripIntValue() {
        String image = getImage().toLowerCase(Locale.ROOT).replaceAll("_", "");

        boolean isNegative = false;
        if (image.charAt(0) == '-') {
            isNegative = true;
            image = image.substring(1);
        }

        char last = image.charAt(image.length() - 1);
        if (last == 'l' || last == 'd' || last == 'f') {
            image = image.substring(0, image.length() - 1);
        }

        // ignore base prefix if any
        if (image.charAt(0) == '0' && image.length() > 1) {
            if (image.charAt(1) == 'x' || image.charAt(1) == 'b') {
                image = image.substring(2);
            } else {
                image = image.substring(1);
            }
        }

        if (isNegative) {
            return "-" + image;
        }
        return image;
    }


    private String stripFloatValue() {
        return getImage().toLowerCase(Locale.ROOT).replaceAll("_", "");
    }


    private int getIntBase() {
        final String image = getImage().toLowerCase(Locale.ROOT);
        final int offset = image.charAt(0) == '-' ? 1 : 0;
        if (image.startsWith("0x", offset)) {
            return 16;
        }
        if (image.startsWith("0b", offset)) {
            return 2;
        }
        if (image.startsWith("0", offset) && image.length() > 1) {
            return 8;
        }
        return 10;
    }


    public int getValueAsInt() {
        if (isInt) {
            // the downcast allows to parse 0x80000000+ numbers as negative instead of a NumberFormatException
            return (int) getValueAsLong();
        }
        return 0;
    }


    public long getValueAsLong() {
        if (isInt) {
            // Using BigInteger to allow parsing 0x8000000000000000+ numbers as negative instead of a NumberFormatException
            BigInteger bigInt = new BigInteger(stripIntValue(), getIntBase());
            return bigInt.longValue();
        }
        return 0L;
    }


    public float getValueAsFloat() {
        if (isFloat) {
            return Float.parseFloat(stripFloatValue());
        }
        return Float.NaN;
    }


    public double getValueAsDouble() {
        if (isFloat) {
            return Double.parseDouble(stripFloatValue());
        }
        return Double.NaN;
    }


}
