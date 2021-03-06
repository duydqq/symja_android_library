package org.matheclipse.core.reflection.system.rules;

import static org.matheclipse.core.expression.F.*;
import org.matheclipse.core.interfaces.IAST;

/**
 * <p>Generated by <code>org.matheclipse.core.preprocessor.RulePreprocessor</code>.</p>
 * <p>See GIT repository at: <a href="https://github.com/axkr/symja_android_library">github.com/axkr/symja_android_library under the tools directory</a>.</p>
 */
public interface TanhRules {
  /**
   * <ul>
   * <li>index 0 - number of equal rules in <code>RULES</code></li>
	 * </ul>
	 */
  final public static int[] SIZES = { 10, 6 };

  final public static IAST RULES = List(
    IInit(Tanh, SIZES),
    // Tanh(0)=0
    ISet(Tanh(C0),
      C0),
    // Tanh(1/4*Pi*I)=I
    ISet(Tanh(Times(CC(0L,1L,1L,4L),Pi)),
      CI),
    // Tanh(1/3*Pi*I)=Sqrt(3)*I
    ISet(Tanh(Times(CC(0L,1L,1L,3L),Pi)),
      Times(CI,CSqrt3)),
    // Tanh(1/2*Pi*I)=ComplexInfinity
    ISet(Tanh(Times(CC(0L,1L,1L,2L),Pi)),
      CComplexInfinity),
    // Tanh(2/3*Pi*I)=-Sqrt(3)*I
    ISet(Tanh(Times(CC(0L,1L,2L,3L),Pi)),
      Times(CNI,CSqrt3)),
    // Tanh(3/4*Pi*I)=-I
    ISet(Tanh(Times(CC(0L,1L,3L,4L),Pi)),
      CNI),
    // Tanh(5/6*Pi*I)=-I/Sqrt(3)
    ISet(Tanh(Times(CC(0L,1L,5L,6L),Pi)),
      Times(CNI,C1DSqrt3)),
    // Tanh(Pi*I)=0
    ISet(Tanh(Times(CI,Pi)),
      C0),
    // Tanh(ArcSinh(x_)):=x/Sqrt(1+x^2)
    ISetDelayed(Tanh(ArcSinh(x_)),
      Times(x,Power(Plus(C1,Sqr(x)),CN1D2))),
    // Tanh(ArcCosh(x_)):=((1+x)*Sqrt((-1+x)/(1+x)))/x
    ISetDelayed(Tanh(ArcCosh(x_)),
      Times(Power(x,-1),Plus(C1,x),Sqrt(Times(Plus(CN1,x),Power(Plus(C1,x),-1))))),
    // Tanh(ArcTanh(x_)):=x
    ISetDelayed(Tanh(ArcTanh(x_)),
      x),
    // Tanh(ArcCoth(x_)):=1/x
    ISetDelayed(Tanh(ArcCoth(x_)),
      Power(x,-1)),
    // Tanh(ArcSech(x_)):=Sqrt((1-x)/(1+x))*(1+x)
    ISetDelayed(Tanh(ArcSech(x_)),
      Times(Sqrt(Times(Plus(C1,Negate(x)),Power(Plus(C1,x),-1))),Plus(C1,x))),
    // Tanh(ArcCsch(x_)):=1/(Sqrt(1+1/x^2)*x)
    ISetDelayed(Tanh(ArcCsch(x_)),
      Power(Times(Sqrt(Plus(C1,Power(x,-2))),x),-1)),
    // Tanh(Infinity)=1
    ISet(Tanh(oo),
      C1),
    // Tanh(ComplexInfinity)=Indeterminate
    ISet(Tanh(CComplexInfinity),
      Indeterminate)
  );
}
