import time
from vtk import *

currentGraph = 0

#Set up mesh source
objSource = vtkOBJReader()
objSource.SetFileName("FullWalk_728.obj")
poly = objSource.GetOutput()

#Map the source
objMapper = vtkPolyDataMapper()
objMapper.SetInputConnection(objSource.GetOutputPort())
bounds = objMapper.GetBounds()

#Color the mesh
minZ = bounds[4]
maxZ = bounds[5]
objLookup = vtkLookupTable()
objLookup.SetTableRange(minZ,maxZ)
objLookup.SetHueRange(0,1)
objLookup.SetSaturationRange(1,1)
objLookup.SetValueRange(1,1)
objLookup.Build()
colors = vtkUnsignedCharArray()
colors.SetNumberOfComponents(3)
colors.SetName("Colors")
for i in range(0, poly.GetNumberOfPoints()):
	p = poly.GetPoint(i)
	color = [0,0,0]
	dcolor = [0,0,0]
	objLookup.GetColor(p[2],dcolor)
	for j in range (0, 3):
	   color[j] = 255 * dcolor[j]
	colors.InsertNextTuple3(color[0], color[1], color[2] )
poly.GetPointData().SetScalars(colors)

#Generate mesh normals
normals = vtkPolyDataNormals()
normals.SetInputData(poly)
normals.ComputePointNormalsOn()
normals.ComputeCellNormalsOff()
normals.Update()

#Draw the mesh
objActor = vtkActor()
realObjMapper = vtkPolyDataMapper()
realObjMapper.SetInputConnection(normals.GetOutputPort())
objActor.SetMapper(realObjMapper)
objActor.GetProperty().SetAmbient(0.1)
objActor.GetProperty().SetDiffuse(0.7)
objActor.GetProperty().SetSpecular(0.5)
objActor.GetProperty().SetSpecularPower(30)

objRen = vtkRenderer()
objRenWin = vtkRenderWindow()
objRenWin.AddRenderer(objRen)
objRen.AddActor(objActor)
objRen.SetBackground(0.1,0.2,0.4)
objRenWin.SetSize(600,600)
renWinInt = vtkRenderWindowInteractor()
renWinInt.SetRenderWindow(objRenWin)

camera = objRen.GetActiveCamera()
camera.SetViewUp(0, 0.0, 1.0)
camera.SetPosition(0, -30, 0.5625)
camera.SetFocalPoint(0, 0, 0.5625)
camera.Elevation(30)
objRenWin.Render()
renWinInt.Start()