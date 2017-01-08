package org.matheclipse.core.reflection.system.rules;

import static org.matheclipse.core.expression.F.*;
import org.matheclipse.core.interfaces.IAST;

/**
 * <p>Generated by <code>org.matheclipse.core.preprocessor.RulePreprocessor</code>.</p>
 * <p>See GIT repository at: <a href="https://bitbucket.org/axelclk/symja_android_library">bitbucket.org/axelclk/symja_android_library under the tools directory</a>.</p>
 */
public interface LaplaceTransformRules {
  /**
   * <ul>
   * <li>index 0 - number of equal rules in <code>RULES</code></li>
	 * </ul>
	 */
  final public static int[] SIZES = { 0, 12 };

  final public static IAST RULES = List(
    IInit(LaplaceTransform, SIZES),
    // LaplaceTransform[a_*f_,t_,s_]:=a*LaplaceTransform[f,t,s]/;FreeQ[a,t]
    ISetDelayed(LaplaceTransform(Times(a_,f_),t_,s_),
      Condition(Times(a,LaplaceTransform(f,t,s)),FreeQ(a,t))),
    // LaplaceTransform[a_*t_^n_.,t_,s_]:=(-1)^n*D[LaplaceTransform[a,t,s],{s,n}]/;IntegerQ[n]&&n>0
    ISetDelayed(LaplaceTransform(Times(a_,Power(t_,n_DEFAULT)),t_,s_),
      Condition(Times(Power(CN1,n),D(LaplaceTransform(a,t,s),List(s,n))),And(IntegerQ(n),Greater(n,C0)))),
    // LaplaceTransform[a_.*E^(b_.+c_.*t_),t_,s_]:=LaplaceTransform[a*E^b,t,s-c]/;FreeQ[{b,c},t]
    ISetDelayed(LaplaceTransform(Times(Power(E,Plus(b_DEFAULT,Times(c_DEFAULT,t_))),a_DEFAULT),t_,s_),
      Condition(LaplaceTransform(Times(a,Power(E,b)),t,Plus(s,Negate(c))),FreeQ(List(b,c),t))),
    // LaplaceTransform[Sqrt[t_],t_,s_]:=Sqrt[Pi]/(2*s^(3/2))
    ISetDelayed(LaplaceTransform(Sqrt(t_),t_,s_),
      Times(Sqrt(Pi),Power(Times(C2,Power(s,QQ(3L,2L))),-1))),
    // LaplaceTransform[Sin[t_],t_,s_]:=1/(s^2+1)
    ISetDelayed(LaplaceTransform(Sin(t_),t_,s_),
      Power(Plus(Sqr(s),C1),-1)),
    // LaplaceTransform[Cos[t_],t_,s_]:=s/(s^2+1)
    ISetDelayed(LaplaceTransform(Cos(t_),t_,s_),
      Times(s,Power(Plus(Sqr(s),C1),-1))),
    // LaplaceTransform[Sinh[t_],t_,s_]:=c/(s^2+(-1)*1)
    ISetDelayed(LaplaceTransform(Sinh(t_),t_,s_),
      Times(c,Power(Plus(Sqr(s),Negate(C1)),-1))),
    // LaplaceTransform[Cosh[t_],t_,s_]:=s/(s^2+(-1)*1)
    ISetDelayed(LaplaceTransform(Cosh(t_),t_,s_),
      Times(s,Power(Plus(Sqr(s),Negate(C1)),-1))),
    // LaplaceTransform[E^t_,t_,s_]:=1/(s+(-1)*1)
    ISetDelayed(LaplaceTransform(Power(E,t_),t_,s_),
      Power(Plus(s,Negate(C1)),-1)),
    // LaplaceTransform[Log[t_],t_,s_]:=-(EulerGamma+Log[s])/s
    ISetDelayed(LaplaceTransform(Log(t_),t_,s_),
      Times(CN1,Plus(EulerGamma,Log(s)),Power(s,-1))),
    // LaplaceTransform[Log[t_]^2,t_,s_]:=(6*EulerGamma^2+Pi^2+6*Log[s]*(2*EulerGamma+Log[s]))/(6*s)
    ISetDelayed(LaplaceTransform(Sqr(Log(t_)),t_,s_),
      Times(Plus(Times(C6,Sqr(EulerGamma)),Sqr(Pi),Times(C6,Log(s),Plus(Times(C2,EulerGamma),Log(s)))),Power(Times(C6,s),-1))),
    // LaplaceTransform[Erf[t_],t_,s_]:=E^(s^2/4)*Erfc[s/2]/s
    ISetDelayed(LaplaceTransform(Erf(t_),t_,s_),
      Times(Power(E,Times(C1D4,Sqr(s))),Erfc(Times(C1D2,s)),Power(s,-1))),
    // LaplaceTransform[Erf[Sqrt[t_]],t_,s_]:=1/(Sqrt[s+1]*s)
    ISetDelayed(LaplaceTransform(Erf(Sqrt(t_)),t_,s_),
      Power(Times(Sqrt(Plus(s,C1)),s),-1))
  );
}
