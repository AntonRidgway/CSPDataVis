import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Mesh
{
	ArrayList<Face> faceList;
	Map<Integer, Vertex> vertexMap;
	public Mesh(ArrayList<Vertex> vertexList, ArrayList<Face> faceList)
	{
		this.faceList = faceList;
		this.vertexMap = new HashMap<Integer, Vertex>();
		for(Vertex v : vertexList)
			vertexMap.put(v.getID(), v);
	}
	public Map<Integer, Vertex> getVertexMap()
	{
		return vertexMap;
	}
	public ArrayList<Face> getFaceList()
	{
		return faceList;
	}
	public void joinWith(Mesh m)
	{
		Map<Integer, Vertex> vMap = m.getVertexMap();
		ArrayList<Face> fList = m.getFaceList();
		for(Vertex v : vMap.values())
		{
			if(!vertexMap.containsKey(v.getID()))
				vertexMap.put(v.getID(),v);
		}
		for(Face f : fList)
		{
			if(!faceList.contains(f))
				faceList.add(f);
		}
	}
	public ArrayList<Normal> calculateNormals()
	{
		//Currently this normals method is not functioning correctly, so I generate the normals later in VTK.
		ArrayList<Normal> normalList = new ArrayList<Normal>();
		for(Vertex v : vertexMap.values())
		{
			double[] totalNormal = new double[3];
			for(Face f : faceList)
			{
				if(f.containsVertex(v.getID()))
				{
					double[] faceNormal = f.getNormal(vertexMap);
					totalNormal[0] += faceNormal[0]; totalNormal[1] += faceNormal[1]; totalNormal[2] += faceNormal[2];
				}
			}
			double magnitude = Math.sqrt(Math.pow(totalNormal[0],2) + Math.pow(totalNormal[1],2) + Math.pow(totalNormal[2],2));
			if(magnitude == 0)
				System.out.println();
			totalNormal[0] /= magnitude; totalNormal[1] /= magnitude; totalNormal[2] /= magnitude;
			if(Double.isNaN(totalNormal[0]))
					System.out.println();
			normalList.add(new Normal(v.getID(), totalNormal[0], totalNormal[1], totalNormal[2]));
		}
		return normalList;
	}
}
