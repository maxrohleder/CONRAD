This folder contains the source code of the collaborative research project 
between Stanford University RSL and Friedrich-Alexander University LME on learning-based 
Dual-Energy CT material decomposition. 

The purpose of this repository's source code is to generate data, which can be used to train a model.
This model will take x-ray transmission data and will output material path length images.
The exact format of this output data will be described later.
This Data shall emulate Scans acquired with the Siemens Zeego, located in 
Stanford RSL Zeego Lab. A Deep Neural Network will be trained on learning the 
mapping from two polychromatic attenuation based images to a material 
pathlength image. This is possible as materials show different attenuation
behavior when imaged with different photon energies. The CONRAD framework is capable
of simulating these physical effects and hence will be used to model Dual Energy CT scans.

--------------------------------- Description of source layout ---------------------------------

The file training_data_generator.java will contain the main functionality of 
generating Simulation Data. Fill in all Fields at the top of the file marked by a comment,
then run the classes main method. It will output samples into the specified OUTPUT_DIR,
creating a directory for every simulated scan in the format specified below.

parameters to specify: 

CONFIG_DIR: 		directory containing three files named: MAT.xml, POLY80.xml and POLY120.xml
OUTPUT_DIR:			directory which shall contain the data later


------------------------ Definition of output folder and file structure-------------------------

Executing the source as described above will create the following file tree for one sample:

~/OUTPUT_DIR/mmddhhmmss_<serialnumber>/MAT_XxYxZxC.raw
~/OUTPUT_DIR/mmddhhmmss_<serialnumber>/POLY80_XxYxZ.raw
~/OUTPUT_DIR/mmddhhmmss_<serialnumber>/POLY120_XxYxZ.raw

file tree specification:

- OUTPUT_DIR		specified output directory
- mmddhhmmss		mm (month), dd (day), hh (hour), (mm) minutes, ss (seconds)
- serialnumber		running in range from zero to specified number of wanted samples
- XxYxZ 			specified resolution seperated by the letter x (raw data convention)
- C					specified number of materials to be accounted for

data structure:

The files POLY80_XxYxZ.raw and POLY120_XxYxZ.raw contain the samples input data (with dimensions X, Y, Z)
The file MAT_XxYxZxC.raw contains the samples target structured in channels per material

All images are saved in raw 32 bit unsigned integer data type.

You can e.g. use CONRADs raw data opener or imageJ to open the data.

------------------------------------------------------------------------------------------------

This research project is conducted by Maximilian Rohleder under the supervision 
of Andreas Maier and Adam Wang and funded by a BaCaTeC grant.

version date: 18/07/2019