{
 E^(3/2*I*Pi)=-I,
 E^(I*Pi)=-1,
 E^(2*I*Pi)=1,
 E^(I*Pi*m_IntegerQ):=(-1)^m,
 E^(2*I*Pi*m_IntegerQ)=1,
 E^Infinity=Infinity,
 E^(-Infinity)=0,
 E^(I*Infinity)=Indeterminate,
 E^(-I*Infinity)=Indeterminate,
 E^(ComplexInfinity)=Indeterminate,
 E^Log(x_):=x,
 Tan(x_)^(m_IntegerQ):=Cot(x)^(-m)/;(m<0),
 Cot(x_)^(m_IntegerQ):=Tan(x)^(-m)/;(m<0),
 Sec(x_)^(m_IntegerQ):=Cos(x)^(-m)/;(m<0),
 Cos(x_)^(m_IntegerQ):=Sec(x)^(-m)/;(m<0),
 Csc(x_)^(m_IntegerQ):=Sin(x)^(-m)/;(m<0),
 Sin(x_)^(m_IntegerQ):=Csc(x)^(-m)/;(m<0)
}