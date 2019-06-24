This folder contains the source code of the collaborative research project 
 between Stanford RSL and FAU Pattern Recognition Lab on learning-based 
Dual-Energy CT material decomposition. 

The file training_data_generator.java contains the main functionality of 
generating Simulation Data.

This Data shall emulate Scans acquired with the Siemens Zeego, located in 
Stanford RSL Zeego Lab. A Neural Network will be trained on learning the 
mapping from two polychromatic attenuation based images to a material 
pathlength image. This is possible as materials show different attenuation
behavior when imaged with different photon energies.

This research project is conducted by Maximilian Rohleder under the supervision 
of Andreas Maier and Adam Wang and funded by a BaCaTeC grant.