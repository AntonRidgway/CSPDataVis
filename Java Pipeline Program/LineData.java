import java.util.ArrayList;

public class LineData
{
	int satisfiedConstraints;
	ArrayList<Integer> varValues;
	
	LineData(int sc)
	{
		satisfiedConstraints = sc;
		varValues = new ArrayList<Integer>();
	}
	
	public void addValue(int v)
	{
		varValues.add(v);
	}
	public int getValue(int i)
	{
		return varValues.get(i);
	}
	public ArrayList<Integer> getValueList()
	{
		return varValues;
	}
	public int getSatisfiedConstraints()
	{
		return satisfiedConstraints;
	}
}