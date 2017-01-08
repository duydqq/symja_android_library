package org.matheclipse.core.reflection.system.rules;

import static org.matheclipse.core.expression.F.*;
import org.matheclipse.core.interfaces.IAST;

/**
 * <p>Generated by <code>org.matheclipse.core.preprocessor.RulePreprocessor</code>.</p>
 * <p>See GIT repository at: <a href="https://bitbucket.org/axelclk/symja_android_library">bitbucket.org/axelclk/symja_android_library under the tools directory</a>.</p>
 */
public interface CoshRules {
  /**
   * <ul>
   * <li>index 0 - number of equal rules in <code>RULES</code></li>
	 * </ul>
	 */
  final public static int[] SIZES = { 26, 0 };

  final public static IAST RULES = List(
    IInit(Cosh, SIZES),
    // Cosh[0]=1
    ISet(Cosh(C0),
      C1),
    // Cosh[Pi/6*I]=Sqrt[3]/2
    ISet(Cosh(Times(CC(0L,1L,1L,6L),Pi)),
      Times(C1D2,CSqrt3)),
    // Cosh[Pi/5*I]=1/4*(Sqrt[5]+1)
    ISet(Cosh(Times(CC(0L,1L,1L,5L),Pi)),
      Times(C1D4,Plus(C1,CSqrt5))),
    // Cosh[Pi/4*I]=1/Sqrt[2]
    ISet(Cosh(Times(CC(0L,1L,1L,4L),Pi)),
      C1DSqrt2),
    // Cosh[Pi/3*I]=1/2
    ISet(Cosh(Times(CC(0L,1L,1L,3L),Pi)),
      C1D2),
    // Cosh[2/5*Pi*I]=1/4*(Sqrt[5]+(-1)*1)
    ISet(Cosh(Times(CC(0L,1L,2L,5L),Pi)),
      Times(C1D4,Plus(CN1,CSqrt5))),
    // Cosh[Pi/2*I]=0
    ISet(Cosh(Times(CC(0L,1L,1L,2L),Pi)),
      C0),
    // Cosh[3/5*Pi*I]=-(Sqrt[5]+(-1)*1)/4
    ISet(Cosh(Times(CC(0L,1L,3L,5L),Pi)),
      Times(CN1D4,Plus(CN1,CSqrt5))),
    // Cosh[2/3*Pi*I]=-1/2
    ISet(Cosh(Times(CC(0L,1L,2L,3L),Pi)),
      CN1D2),
    // Cosh[3/4*Pi*I]=-1/Sqrt[2]
    ISet(Cosh(Times(CC(0L,1L,3L,4L),Pi)),
      Negate(C1DSqrt2)),
    // Cosh[4/5*Pi*I]=-(Sqrt[5]+1)/4
    ISet(Cosh(Times(CC(0L,1L,4L,5L),Pi)),
      Times(CN1D4,Plus(C1,CSqrt5))),
    // Cosh[5/6*Pi*I]=-Sqrt[3]/2
    ISet(Cosh(Times(CC(0L,1L,5L,6L),Pi)),
      Times(CN1D2,CSqrt3)),
    // Cosh[Pi*I]=-1
    ISet(Cosh(Times(CI,Pi)),
      CN1),
    // Cosh[7/6*Pi*I]=-Sqrt[3]/2
    ISet(Cosh(Times(CC(0L,1L,7L,6L),Pi)),
      Times(CN1D2,CSqrt3)),
    // Cosh[6/5*Pi*I]=-(Sqrt[5]+1)/4
    ISet(Cosh(Times(CC(0L,1L,6L,5L),Pi)),
      Times(CN1D4,Plus(C1,CSqrt5))),
    // Cosh[5/4*Pi*I]=-1/Sqrt[2]
    ISet(Cosh(Times(CC(0L,1L,5L,4L),Pi)),
      Negate(C1DSqrt2)),
    // Cosh[4/3*Pi*I]=-1/2
    ISet(Cosh(Times(CC(0L,1L,4L,3L),Pi)),
      CN1D2),
    // Cosh[7/5*Pi*I]=-(Sqrt[5]+(-1)*1)/4
    ISet(Cosh(Times(CC(0L,1L,7L,5L),Pi)),
      Times(CN1D4,Plus(CN1,CSqrt5))),
    // Cosh[8/5*Pi*I]=1/4*(Sqrt[5]+(-1)*1)
    ISet(Cosh(Times(CC(0L,1L,8L,5L),Pi)),
      Times(C1D4,Plus(CN1,CSqrt5))),
    // Cosh[5/3*Pi*I]=1/2
    ISet(Cosh(Times(CC(0L,1L,5L,3L),Pi)),
      C1D2),
    // Cosh[7/4*Pi*I]=1/Sqrt[2]
    ISet(Cosh(Times(CC(0L,1L,7L,4L),Pi)),
      C1DSqrt2),
    // Cosh[9/5*Pi*I]=1/4*(Sqrt[5]+1)
    ISet(Cosh(Times(CC(0L,1L,9L,5L),Pi)),
      Times(C1D4,Plus(C1,CSqrt5))),
    // Cosh[11/6*Pi*I]=Sqrt[3]/2
    ISet(Cosh(Times(CC(0L,1L,11L,6L),Pi)),
      Times(C1D2,CSqrt3)),
    // Cosh[2*Pi*I]=1
    ISet(Cosh(Times(CC(0L,1L,2L,1L),Pi)),
      C1),
    // Cosh[Infinity]=Infinity
    ISet(Cosh(oo),
      oo),
    // Cosh[ComplexInfinity]=Indeterminate
    ISet(Cosh(CComplexInfinity),
      Indeterminate)
  );
}
