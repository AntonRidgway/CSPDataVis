import java.util.ArrayList;


public class ValueTree
{
	private ValueNode root;
	private int bFactor;
	private int depth;
	
	public ValueTree(int bFactor, int depth)
	{
		this.bFactor = bFactor;
		this.depth = depth;
		this.root = new ValueNode(null, 0);
		createTree(bFactor,depth-1,root);
	}
	private void createTree(int bFactor, int depth, ValueNode root)
	{
		if(depth < 0) return;
		for(int i = 0; i < bFactor; i++)
		{
			ValueNode currChild = new ValueNode(root,0); 
			root.addChild(currChild);
			createTree(bFactor,depth-1,currChild);
		}
	}
	public int getDepth()
	{
		return depth;
	}
	public int getBranchingFactor()
	{
		return bFactor;
	}
	public boolean setValue(ArrayList<Integer> position, double value)
	{
		if(position.size() != depth) return false;
		
		ValueNode temp = root;
		for(int i = 0; i < position.size(); i++)
			temp = temp.getChild(position.get(i));
		return temp.setValue(value);
	}
	public double getValue(ArrayList<Integer> position)
	{
		if(position == null || position.size() == 0)
			return root.getValue();
		else if(position.size() > depth) return -1;
		
		ValueNode temp = root;
		for(int i = 0; i < position.size(); i++)
			temp = temp.getChild(position.get(i));
		return temp.getValue();
	}
	private class ValueNode
	{
		private ValueNode parent;
		private ArrayList<ValueNode> children;
		private double value;
		public ValueNode(ValueNode parent, int value)
		{
			this.parent = parent;
			this.value = value;
			this.children = new ArrayList<ValueNode>();
		}
		public ValueNode getParent() {
			return parent;
		}
		public void setParent(ValueNode parent) {
			this.parent = parent;
		}
		public ValueNode getChild(int idx) {
			return children.get(idx);
		}
		public void addChild(ValueNode child) {
			children.add(child);
		}
		public double getValue() {
			if(children.isEmpty())
				return value;
			else
			{
				value = 0;
				for(ValueNode v : children)
					value += v.getValue();
				value /= children.size();
				return value;
			}
		}
		public boolean setValue(double value) {
			if(!children.isEmpty())
				return false;
			this.value = value;
			return true;
		}
	}
}
