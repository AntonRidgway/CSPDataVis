
public class Vertex implements Comparable<Vertex>
{
	private int id;
	private double x, y, z;
	public Vertex(int id, double x, double y, double z)
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
	public int compareTo(Vertex otherVertex)
	{
		return Integer.compare(id, otherVertex.getID());
	}
	@Override
	public boolean equals(Object other)
	{
		if((other instanceof Vertex) && (id == ((Vertex)other).getID())) return true;
		return false;
	}
}
