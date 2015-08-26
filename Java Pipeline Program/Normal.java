
public class Normal implements Comparable<Normal>
{
	private int id;
	private double x, y, z;
	public Normal(int id, double x, double y, double z)
	{
		this.id = id;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public int getID()
	{
		return id;
	}
	public double getX()
	{
		return x;
	}
	public double getY()
	{
		return y;
	}
	public double getZ()
	{
		return z;
	}
	@Override
	public int compareTo(Normal otherNormal)
	{
		return Integer.compare(id, otherNormal.getID());
	}
	@Override
	public boolean equals(Object other)
	{
		if((other instanceof Normal) && (id == ((Normal)other).getID())) return true;
		return false;
	}
}
