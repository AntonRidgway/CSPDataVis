import java.util.Map;


public class Face
{
	private int v1,v2,v3,n1,n2,n3;
	public Face( int v1, int v2, int v3, int n1, int n2, int n3 )
	{
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.n1 = n1;
		this.n2 = n2;
		this.n3 = n3;
	}
	public int getV1() {
		return v1;
	}
	public void setV1(int v1) {
		this.v1 = v1;
	}
	public int getV2() {
		return v2;
	}
	public void setV2(int v2) {
		this.v2 = v2;
	}
	public int getV3() {
		return v3;
	}
	public void setV3(int v3) {
		this.v3 = v3;
	}
	public int getN1() {
		return n1;
	}
	public void setN1(int n1) {
		this.n1 = n1;
	}
	public int getN2() {
		return n2;
	}
	public void setN2(int n2) {
		this.n2 = n2;
	}
	public int getN3() {
		return n3;
	}
	public void setN3(int n3) {
		this.n3 = n3;
	}
	public boolean containsVertex(int id)
	{
		return v1 == id || v2 == id || v3 == id;
	}
	public double[] getNormal(Map<Integer, Vertex> vertices)
	{
		double[] result = new double[3];
		double[] vert1 = new double[]{vertices.get(v1).getX(),vertices.get(v1).getY(),vertices.get(v1).getZ()};
		double[] vert2 = new double[]{vertices.get(v2).getX(),vertices.get(v2).getY(),vertices.get(v2).getZ()};
		double[] vert3 = new double[]{vertices.get(v3).getX(),vertices.get(v3).getY(),vertices.get(v3).getZ()};
		double[] vector1 = new double[]{vert1[0]-vert2[0], vert1[1]-vert2[1], vert1[2]-vert2[2]};
		double[] vector2 = new double[]{vert3[0]-vert2[0], vert3[1]-vert2[1], vert3[2]-vert2[2]};
		result[0] = vector1[1]*vector2[2]-vector1[2]*vector2[1];
		result[1] = vector1[0]*vector2[2]-vector1[2]*vector2[0];
		result[2] = vector1[0]*vector2[1]-vector1[1]*vector2[0];
		if(result[0] == 0 && result[1] == 0 && result[2] == 0)
			System.out.println();
		return result;
	}
	@Override
	public boolean equals(Object other)
	{
		if((other instanceof Face) &&
		   (v1 == ((Face)other).getV1()) &&
		   (v2 == ((Face)other).getV2()) &&
		   (v3 == ((Face)other).getV3()) &&
		   (n1 == ((Face)other).getN1()) &&
		   (n2 == ((Face)other).getN2()) &&
		   (n3 == ((Face)other).getN3()))
		return true;
		
		return false;
	}
}
