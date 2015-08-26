import time
from vtk import *

FILENAME_PREFIX_NODES = "AFCCBJ_n_"
FILENAME_PREFIX_EDGES = "AFCCBJ_e_"
EXTENSION = ".csv"
NUM_GRAPHS = 20

class CueAnimator:
	'Animator class to update graph display'
	def __init__(self):
		self.currentGraph = 0
	
		#Set up node source
		self.nodeSource = vtkDelimitedTextReader()
		self.nodeSource.SetFieldDelimiterCharacters(",")
		self.nodeSource.DetectNumericColumnsOn()
		self.nodeSource.SetHaveHeaders(True)
		self.nodeSource.SetFileName(FILENAME_PREFIX_NODES+str(0)+EXTENSION)

		#Create edge source
		self.edgeSource = vtkDelimitedTextReader()
		self.edgeSource.SetFieldDelimiterCharacters(",")
		self.edgeSource.DetectNumericColumnsOn()
		self.edgeSource.SetHaveHeaders(True)
		self.edgeSource.SetFileName(FILENAME_PREFIX_EDGES+str(0)+EXTENSION)

		#Generate a graph
		self.converter = vtkTableToGraph()
		self.converter.SetDirected(True)
		self.converter.SetInputConnection(0,self.edgeSource.GetOutputPort())
		self.converter.SetInputConnection(1,self.nodeSource.GetOutputPort())
		self.converter.AddLinkVertex("id1","id",False)
		self.converter.AddLinkVertex("id2","id",False)
		self.converter.AddLinkEdge("id1","id2")
		
		#Draw the graph
		self.view = vtkGraphLayoutView()
		self.view.AddRepresentationFromInputConnection(self.converter.GetOutputPort())
		#self.view.SetVertexLabelArrayName("label")
		#self.view.SetVertexLabelVisibility(True)
		self.view.SetVertexLabelVisibility(False)
		self.view.SetVertexColorArrayName("value")
		self.view.SetColorVertices(True)
		
		# Set edge labels and colors
		self.view.SetEdgeLabelVisibility(False)
		self.view.SetEdgeColorArrayName("value")
		self.view.SetColorEdges(True)
		self.view.SetLayoutStrategyToCone()
		self.view.SetInteractionModeTo3D() # Left mouse button causes 3D rotate instead of zoom
		
		self.theme = vtkViewTheme.CreateMellowTheme()
		self.theme.SetCellColor(.2,.2,.6)
		self.theme.SetLineWidth(2)
		self.theme.SetPointSize(10)
		self.view.ApplyViewTheme(self.theme)
		self.theme.FastDelete()
		
		self.view.GetRenderWindow().SetSize(600, 600)
		self.view.ResetCamera()
		
		self.view.Render()
		
		self.camera = vtkCamera()
		self.camera.SetViewUp(0, 0, -1)
		self.camera.SetPosition(0, 1, 0)
		self.camera.SetFocalPoint(0, 0, 0)
		self.camera.ComputeViewPlaneNormal()
		self.view.GetRenderer().SetActiveCamera(self.camera)
		self.view.GetRenderer().ResetCamera()
		
		self.currTime = time.clock()
		
	def StartCue(self):
		self.currentGraph += 1
		if self.currentGraph < NUM_GRAPHS:
			self.nodeSource.SetFileName(FILENAME_PREFIX_NODES+str(self.currentGraph)+EXTENSION)
			self.edgeSource.SetFileName(FILENAME_PREFIX_EDGES+str(self.currentGraph)+EXTENSION)
			self.view.Render()
	def Tick(self):
		newTime = time.clock()
		passedTime = newTime-self.currTime
		self.currTime = newTime
		self.camera.Azimuth(passedTime)
		self.view.Render()
	def EndCue(self):
		print 'done'

		
cueAnim = CueAnimator()

def handleCueEvents(caller, event):
	if not cueAnim is None:
		if event == "StartAnimationCueEvent":
			cueAnim.StartCue()
		elif event == "AnimationCueTickEvent":
			cueAnim.Tick()

def main():	
	print "entered main"
	animScene = vtkAnimationScene()
	animScene.SetPlayMode(vtkAnimationScene.PLAYMODE_REALTIME)
	animScene.SetStartTime(0)
	animScene.SetEndTime(1)
	animScene.SetLoop(True)
	
	animCue = vtkAnimationCue()
	animCue.SetTimeMode(vtkAnimationCue.TIMEMODE_RELATIVE)
	animCue.SetStartTime(0)
	animCue.SetEndTime(0.000000001)
	animScene.AddCue(animCue)
	
	animCue.AddObserver(vtkCommand.StartAnimationCueEvent, handleCueEvents)
	animCue.AddObserver(vtkCommand.AnimationCueTickEvent, handleCueEvents)
	
	animScene.Play();

if __name__ == '__main__':
	main()