from vtk import *

FILENAME_PREFIX_NODES = "FullWalk_n_"
FILENAME_PREFIX_EDGES = "FullWalk_e_"
EXTENSION = ".csv"

#Create node source
nodeSource = vtkDelimitedTextReader()
nodeSource.SetFieldDelimiterCharacters(",")
nodeSource.DetectNumericColumnsOn()
nodeSource.SetHaveHeaders(True)
nodeSource.SetFileName(FILENAME_PREFIX_NODES+str(728)+EXTENSION)

#Create edge source
edgeSource = vtkDelimitedTextReader()
edgeSource.SetFieldDelimiterCharacters(",")
edgeSource.DetectNumericColumnsOn()
edgeSource.SetHaveHeaders(True)
edgeSource.SetFileName(FILENAME_PREFIX_EDGES+str(728)+EXTENSION)

#Generate a graph
converter = vtkTableToGraph()
converter.SetDirected(True)
converter.SetInputConnection(0,edgeSource.GetOutputPort())
converter.SetInputConnection(1,nodeSource.GetOutputPort())
converter.AddLinkVertex("id1","id",False)
converter.AddLinkVertex("id2","id",False)
converter.AddLinkEdge("id1","id2")

#Draw the graph
view = vtkGraphLayoutView()
view.AddRepresentationFromInputConnection(converter.GetOutputPort())
view.SetVertexLabelArrayName("label")
view.SetVertexLabelVisibility(True)
view.SetVertexColorArrayName("value")
view.SetColorVertices(True)

# Set edge labels and colors
view.SetEdgeLabelVisibility(False)
view.SetEdgeColorArrayName("value")
view.SetColorEdges(True)

# Set to circular layout
view.SetLayoutStrategyToSimple2D()

theme = vtkViewTheme.CreateMellowTheme()
theme.SetCellColor(.2,.2,.6)
theme.SetLineWidth(2)
theme.SetPointSize(10)
view.ApplyViewTheme(theme)
theme.FastDelete()

view.GetRenderWindow().SetSize(600, 600)
view.ResetCamera()
view.Render()
view.GetInteractor().Start()