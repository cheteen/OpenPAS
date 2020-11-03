package fopas.basics;

import java.util.List;

public interface FOFunction
{
	FOElement eval(FOStructure structure, List<FOElement> args);
}
