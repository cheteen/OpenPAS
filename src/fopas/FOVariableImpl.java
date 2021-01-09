package fopas;

import fopas.basics.FOVariable;

public class FOVariableImpl implements FOVariable
{
	final protected String mName;
	
	FOVariableImpl(String name)
	{
		//TODO: Validate name here.
		mName = name;
	}

	@Override
	public String getName()
	{
		return mName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mName == null) ? 0 : mName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FOVariableImpl other = (FOVariableImpl) obj;
		if (mName == null) {
			if (other.mName != null)
				return false;
		} else if (!mName.equals(other.mName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FOVariableImpl [" + mName + "]";
	}
}
