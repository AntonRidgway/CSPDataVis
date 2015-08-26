# CSPDataVis

A Python/Java project to visualize the progress and results of DCSP algorithms. It utilizes VTK for visualization.

Traces from individual solution runs are included with the project, as well as the Java code written to convert them to either OBJ or CSV data suitable for use in VTK.
Transformed data is also included, with Python code used to visualize either the progress of the algorithms as an animation, or the final result as a fixed 3D mesh.

The Java code may be run by executing CSPtoVTK.java, with the name of the data file to be transformed passed in as a program parameter, as long as the data is copied into the same folder.
The Python code may be run after installing VTK (found at www.vtk.org), with the data in the same folder. The program was designed around VTK version 6.1.0.

The original report and presentation slides for the project are also included, which provide additional documentation and example output images.
