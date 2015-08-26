import time
from vtk import *

FILENAME_PREFIX = "APO_"
EXTENSION = ".obj"
NUM_GRAPHS = 20

class CueAnimator:
	'Animator class to update graph display'
	def __init__(self):
		self.currentGraph = 0
	
		#Set up mesh source
		self.objSource = vtkOBJReader()
		self.objSource.SetFileName(FILENAME_PREFIX+str(self.currentGraph)+EXTENSION)
		poly = self.objSource.GetOutput()

		#Map the source
		self.objMapper = vtkPolyDataMapper()
		self.objMapper.SetInputConnection(self.objSource.GetOutputPort())
		bounds = self.objMapper.GetBounds()

		#Color the mesh and find the highest point
		highPt = [0,0,0]
		minZ = bounds[4]
		maxZ = bounds[5]
		self.objLookup = vtkLookupTable()
		self.objLookup.SetTableRange(minZ,maxZ)
		self.objLookup.SetHueRange(0,1)
		self.objLookup.SetSaturationRange(1,1)
		self.objLookup.SetValueRange(1,1)
		self.objLookup.Build()
		colors = vtkUnsignedCharArray()
		colors.SetNumberOfComponents(3)
		colors.SetName("Colors")
		for i in range(0, poly.GetNumberOfPoints()):
			p = poly.GetPoint(i)
			color = [0,0,0]
			dcolor = [0,0,0]
			self.objLookup.GetColor(p[2],dcolor)
			for j in range (0, 3):
			   color[j] = 255 * dcolor[j]
			colors.InsertNextTuple3(color[0], color[1], color[2] )
			if p[2] > highPt[2]:
				highPt = p
		poly.GetPointData().SetScalars(colors)

		#Add a sphere at the high point
		self.highIndicator = vtkSphereSource()
		self.highIndicator.SetCenter(highPt[0],highPt[1],highPt[2])
		self.highIndicator.SetRadius(0.25)
		self.highMapper = vtkPolyDataMapper()
		self.highMapper.SetInputConnection(self.highIndicator.GetOutputPort())
		self.highActor = vtkActor()
		self.highActor.SetMapper(self.highMapper)
		
		#Generate mesh normals
		normals = vtkPolyDataNormals()
		normals.SetInputData(poly)
		normals.ComputePointNormalsOn()
		normals.ComputeCellNormalsOff()
		normals.Update()
		
		#Draw the mesh
		self.objActor = vtkActor()
		realObjMapper = vtkPolyDataMapper()
		realObjMapper.SetInputConnection(normals.GetOutputPort())
		self.objActor.SetMapper(realObjMapper)
		self.objActor.GetProperty().SetAmbient(0.1)
		self.objActor.GetProperty().SetDiffuse(0.7)
		self.objActor.GetProperty().SetSpecular(0.5)
		self.objActor.GetProperty().SetSpecularPower(30)

		self.objRen = vtkRenderer()
		self.objRenWin = vtkRenderWindow()
		self.objRenWin.AddRenderer(self.objRen)
		self.objRen.AddActor(self.objActor)
		self.objRen.AddActor(self.highActor)
		self.objRen.SetBackground(0.1,0.2,0.4)
		self.objRenWin.SetSize(600,600)

		self.camera = self.objRen.GetActiveCamera()
		self.camera.SetViewUp(0, 0.0, 1.0)
		self.camera.SetPosition(0, -30, 0.5625)
		self.camera.SetFocalPoint(0, 0, 0.5625)
		self.camera.Elevation(30)
		self.objRenWin.Render()
		
		self.currTime = time.clock()
		
	def StartCue(self):
		self.currentGraph += 1
		if self.currentGraph < NUM_GRAPHS:
			self.objSource.SetFileName(FILENAME_PREFIX+str(self.currentGraph)+EXTENSION)
			poly = self.objSource.GetOutput()
			bounds = self.objMapper.GetBounds()
			highPt = [0,0,0]
			minZ = bounds[4]
			maxZ = bounds[5]
			self.objLookup.SetTableRange(minZ,maxZ)
			self.objLookup.Build()
			colors = vtkUnsignedCharArray()
			colors.SetNumberOfComponents(3)
			colors.SetName("Colors")
			for i in range(0, poly.GetNumberOfPoints()):
				p = poly.GetPoint(i)
				color = [0,0,0]
				dcolor = [0,0,0]
				self.objLookup.GetColor(p[2],dcolor)
				for j in range (0, 3):
				   color[j] = 255 * dcolor[j]
				colors.InsertNextTuple3(color[0], color[1], color[2] )
				if p[2] > highPt[2]:
					highPt = p
			poly.GetPointData().SetScalars(colors)
			self.highIndicator.SetCenter(highPt[0],highPt[1],highPt[2])
			#Generate mesh normals
			normals = vtkPolyDataNormals()
			normals.SetInputData(poly)
			normals.ComputePointNormalsOn()
			normals.ComputeCellNormalsOff()
			normals.Update()

			#Draw the mesh
			realObjMapper = vtkPolyDataMapper()
			realObjMapper.SetInputConnection(normals.GetOutputPort())
			self.objActor.SetMapper(realObjMapper)
			self.objRenWin.Render()
		else:
			self.highActor.GetProperty().SetColor(0.2,0.8,0.2)
	def Tick(self):
		newTime = time.clock()
		passedTime = newTime-self.currTime
		self.currTime = newTime
		self.camera.Azimuth(passedTime)
		self.objRenWin.Render()
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