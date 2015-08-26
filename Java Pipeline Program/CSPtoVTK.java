import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class CSPtoVTK
{
	private final static double GRAPH_HEIGHT_SCALE = 1.0;
	private final static double GRAPH_RADIUS_SCALE = 1.0; //per-level
	
	private static int domainSize;
	private static int numConstraints;
	private static ArrayList<LineData> timeData;
	
	private static double overallValueScale = 0;
	
	public static void main(String[] args)
	{
		if(args.length != 1)
		{
			System.err.println("ERROR: A filename must be specified!");
			return;
		}
		String fileName = args[0];
		int dotPosition = fileName.lastIndexOf(".");
		String outputPrefix = "";
		if(dotPosition == -1)
			outputPrefix = fileName;
		else
			outputPrefix = fileName.substring(0, dotPosition);
		
		readInFile(fileName);
		overallValueScale = GRAPH_HEIGHT_SCALE*domainSize;
		generateMeshFiles(outputPrefix);
		generateGraphFiles(outputPrefix);
		generateTableFiles(outputPrefix);
	}
	
	private static void readInFile(String filename)
	{
		try
		{
			FileReader reader = new FileReader(filename);
			Scanner itemScanner = new Scanner(reader);
			domainSize = itemScanner.nextInt();
			numConstraints = itemScanner.nextInt();
			
			timeData = new ArrayList<LineData>();
			while(itemScanner.hasNextLine())
			{
				String s = itemScanner.nextLine();
				Scanner lineParser = new Scanner(s);
				if(lineParser.hasNextInt())
				{
					LineData newLine = new LineData(lineParser.nextInt());
					while(lineParser.hasNextInt())
						newLine.addValue(lineParser.nextInt());
					timeData.add(newLine);
				}
				lineParser.close();
			}
			
			itemScanner.close();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static ValueTree updateTree(ValueTree t, LineData l)
	{
		t.setValue(l.getValueList(), l.getSatisfiedConstraints());
		return t;
	}
	
	private static void generateMeshFiles(String filePrefix)
	{
		if(timeData == null) System.exit(1);
		
		ValueTree vt = new ValueTree(domainSize, timeData.get(0).getValueList().size());
		updateTree(vt, timeData.get(0));
		outputOBJ(startMesh(vt), filePrefix+"_0.obj");
		for(int i = 1; i < timeData.size(); i++)
		{
			vt = updateTree(vt,timeData.get(i));
			outputOBJ(startMesh(vt), filePrefix+"_"+i+".obj");
		}
	}
	
	private static Mesh startMesh(ValueTree t)
	{
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		ArrayList<Face> faces = new ArrayList<Face>();
		vertices.add(new Vertex(0,0,0,0));
		
		int bFactor = t.getBranchingFactor();
		double radius = GRAPH_RADIUS_SCALE;
		double radDistance = 2*Math.PI/bFactor;
		for(int i = 0; i < bFactor; i++)
		{
			double theta = radDistance*i;
			double xPos = radius*Math.cos(theta);
			double yPos = radius*Math.sin(theta);
			ArrayList<Integer> treePos = new ArrayList<Integer>();
			treePos.add(i);
			double zPos = overallValueScale*(t.getValue(treePos))/numConstraints;
			vertices.add(new Vertex(i+1,xPos,yPos,zPos));
			faces.add(new Face(0,((i+1)%bFactor)+1,i+1,
					   		   0,((i+1)%bFactor)+1,i+1));
		}
		Mesh totalMesh = new Mesh(vertices,faces);
		for(int i = 0; i < bFactor; i++)
		{
			ArrayList<Integer> treePos = new ArrayList<Integer>();
			treePos.add(i);
			Mesh m = makeMesh(i+1,t,treePos, radDistance*i);
			if(m != null) totalMesh.joinWith(m);
		}
		
		return totalMesh;
	}
	private static Mesh makeMesh(int rootIndex, ValueTree t, ArrayList<Integer> thisPrefix, double branchRads)
	{
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		ArrayList<Face> faces = new ArrayList<Face>();
				
		int bFactor = t.getBranchingFactor();
		
		if(thisPrefix.size() < t.getDepth())
		{
//			The formula for the start index of each level is min = floor((b^d-1)/(b-1)), where b = branching factor and d = depth.
//			The end index of each level is max = b*min.
//			Ex:
//			0		1
//			1-3		3
//			4-12	9
//			13-39	27
//			40-120	81
			
			int depth = thisPrefix.size();
			int levelMin = Math.round(((float)Math.pow(bFactor,depth)-1.0f)/(bFactor-1.0f));
			int levelMax = bFactor*levelMin;
			int nextLevelMin = Math.round(((float)Math.pow(bFactor,depth+1)-1.0f)/(bFactor-1.0f));
			int nextLevelMax = bFactor*nextLevelMin;
			
			//Our subset of the next level is given by a sum of terms based on our path down the branches to this point,
			//start = sum for each depth c( choice at c * b^(d-c))
			int nextLevelStart = nextLevelMin;
			for(int i = 0; i < depth; i++)
				nextLevelStart += Math.round(thisPrefix.get(i)*Math.pow(bFactor, depth-i));
			
			double radius = (depth+1)*GRAPH_RADIUS_SCALE; //add vertices at the next level down.
//			double radStart = 2*Math.PI*((double)(nextLevelStart-nextLevelMin))/(nextLevelMax-nextLevelMin); 
			double radDistance = 2*Math.PI/(nextLevelMax-nextLevelMin+1);  //The number of radians allotted to each point.
//			double radCenter = radDistance*(nextLevelStart-nextLevelMin);
			double radStart = branchRads - (radDistance*(bFactor-1))/2.; //The starting radians for our subset

			for(int i = 0; i < bFactor; i++)
			{
				double theta = radDistance*i;
				double xPos = radius*Math.cos(radStart+theta);
				double yPos = radius*Math.sin(radStart+theta);
				ArrayList<Integer> treePos = ((ArrayList<Integer>)thisPrefix.clone());
				treePos.add(i);
				double zPos = overallValueScale*(t.getValue(treePos))/numConstraints;
				vertices.add(new Vertex(nextLevelStart+i,xPos,yPos,zPos));
			}
			for(int i = 0; i < bFactor-1; i++)
			{
				faces.add(new Face(rootIndex,nextLevelStart+i+1,nextLevelStart+i,
								   rootIndex,nextLevelStart+i+1,nextLevelStart+i));
			}
			if(rootIndex == levelMin)
			{
				int leftRoot = levelMax;
				int leftChild = nextLevelMax;
				int rootChild = nextLevelMin;
				faces.add(new Face(leftRoot,rootIndex,rootChild,
								   leftRoot,rootIndex,rootChild));
				faces.add(new Face(leftRoot,rootChild,leftChild,
								   leftRoot,rootChild,leftChild));
			}
			else
			{
				int leftRoot = rootIndex-1;
				int leftChild = nextLevelStart-1;
				int rootChild = nextLevelStart;
				faces.add(new Face(leftRoot,rootIndex,rootChild,
								   leftRoot,rootIndex,rootChild));
				faces.add(new Face(leftRoot,rootChild,leftChild,
								   leftRoot,rootChild,leftChild));
			}
			
			Mesh totalMesh = new Mesh(vertices,faces);
			for(int i = 0; i < bFactor; i++)
			{
				ArrayList<Integer> treePos = ((ArrayList<Integer>)thisPrefix.clone());
				treePos.add(i);
				Mesh m = makeMesh(nextLevelStart+i, t, treePos, radStart+radDistance*i);
				if(m != null) totalMesh.joinWith(m);
			}
			
			return totalMesh;
		}
		else
		{
			//Nothing to add.
			return null;
		}
	}
	
	/**
	 * Output meshes to Waveform OBJ format.
	 * @param filename
	 */
	private static void outputOBJ(Mesh m, String filename)
	{		
		Map<Integer, Vertex> vertices = m.getVertexMap();
		ArrayList<Face> faces = m.getFaceList();
		ArrayList<Normal> normals = m.calculateNormals();
		//Currently this normals method is not functioning correctly, so I generate the normals later in VTK.

		try {
			PrintWriter pw = new PrintWriter(filename);
			
			for(int i = 0; i < vertices.size(); i++)
			{
				Vertex v = vertices.get(i);
				pw.println("v "+v.getX()+" "+v.getY()+" "+v.getZ());
			}
			
			pw.println("");
			
			for(Normal n : normals)
			{
				pw.println("vn "+n.getX()+" "+n.getY()+" "+n.getZ());
			}
			
			pw.println("");
			
			for(Face f : faces)
				pw.println("f "+(f.getV1()+1)+"//"+(f.getN1()+1)+" "+(f.getV2()+1)+"//"+(f.getN2()+1)+" "+(f.getV3()+1)+"//"+(f.getN3()+1));
			
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	private static void generateGraphFiles(String filePrefix)
	{
		if(timeData == null) System.exit(1);
		
		ValueTree vt = new ValueTree(domainSize, timeData.get(0).getValueList().size());
		updateTree(vt, timeData.get(0));
		outputCSV(vt, filePrefix+"_n_0", filePrefix+"_e_0");
		for(int i = 1; i < timeData.size(); i++)
		{
			vt = updateTree(vt,timeData.get(i));
			outputCSV(vt, filePrefix+"_n_"+i, filePrefix+"_e_"+i);
		}
	}
	private static void outputCSV(ValueTree t, String nodePrefix, String edgePrefix)
	{
		//Edge format
				//Four headers: source, destination, lbl, value
				//Then data on subsequent lines
		try {
			PrintWriter nw = new PrintWriter(nodePrefix+".csv");
			PrintWriter ew = new PrintWriter(edgePrefix+".csv");
			
			nw.println("id,label,value");
			ew.println("id1,id2,value");
			ArrayList<Integer> rootPath = new ArrayList<Integer>();
			nw.println(0+",root,"+t.getValue(rootPath));
			printChildren(nw,ew,t,rootPath,0);
			
			nw.close();
			ew.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	private static void printChildren(PrintWriter nodeWriter, PrintWriter edgeWriter, ValueTree t, ArrayList<Integer> currPath, int thisID)
	{		
		int depth = currPath.size();
		if(depth < t.getDepth())
		{
			double bFactor = t.getBranchingFactor();
			int nextLevelMin = (int) Math.round(((float)Math.pow(bFactor,depth+1)-1.0f)/(bFactor-1.0f));
			int nextLevelStart = nextLevelMin;
			for(int i = 0; i < depth; i++)
				nextLevelStart += Math.round(currPath.get(i)*Math.pow(bFactor, depth-i));
			
			for(int i = 0; i < bFactor; i++)
			{
				ArrayList<Integer> thisPath = (ArrayList<Integer>)currPath.clone();
				thisPath.add(i);
				
				String label = "";
				for(Integer num : thisPath) label += num;
				nodeWriter.println((nextLevelStart+i)+","+label+","+t.getValue(thisPath));
				
				double edgeValue = (t.getValue(currPath) + t.getValue(thisPath))/2.;
				edgeWriter.println(thisID+","+(nextLevelStart+i)+","+edgeValue);
				
				printChildren(nodeWriter,edgeWriter,t,thisPath,nextLevelStart+i);
			}
		}
	}
	
	private static void generateTableFiles(String filePrefix)
	{
		if(timeData == null) System.exit(1);
		
		ValueTree vt = new ValueTree(domainSize, timeData.get(0).getValueList().size());
		updateTree(vt, timeData.get(0));
		outputCSVTable(vt, filePrefix+"_0"+".csv");
		for(int i = 1; i < timeData.size(); i++)
		{
			vt = updateTree(vt,timeData.get(i));
			outputCSVTable(vt, filePrefix+"_"+i+".csv");
		}
	}
	private static void outputCSVTable(ValueTree t, String fileName)
	{
		try {
			PrintWriter pw = new PrintWriter(fileName);
			
			pw.println("path,value");
			
			int depth = t.getDepth();
			ArrayList<Integer> valuePath = new ArrayList<Integer>();
			for(int i = 0; i < depth; i++) valuePath.add(0);
			
						
			while(valuePath.get(0) < domainSize)
			{
				String valuePathString = "";
				for(Integer val : valuePath) valuePathString += val;
				pw.println(valuePathString+","+t.getValue(valuePath));
				
				valuePath.set(depth-1, valuePath.get(depth-1)+1);
				for(int i = depth-1; i > 0; i--)
				{
					if(valuePath.get(i) >= domainSize)
					{
						valuePath.set(i, valuePath.get(i)-domainSize);
						valuePath.set(i-1, valuePath.get(i-1)+1);
					}
				}
			}
			
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
