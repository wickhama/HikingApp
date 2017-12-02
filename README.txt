 | 
 | ARCTrails
 | Ayla Wickham, Ryley Jewsbury, Caleigh LePage
 | CPSC300 - Software Engineering - 2017 Fall Semester
 |

Both the main project as well as test projects are included in the repository

Prototypes & Tests (HikingApp/Tests/):
	- ARC_Trails:
		Tested asking for location permission
	- GPX:		
		Prototype for manually parsing GPX files
		(Main project uses an external library from AlternativeVision)
		( http://gpxparser.alternativevision.ro/ )
	- MapAPI & MapTest:
		Tests using a map fragment in the same layout as other fragments,
		and accessing fragments in a layout
	- MenuTest:
		Tests creating different kinds of menues and responding to input
		through callbacks.
	- StupidGPSThing:
		Tests displaying GPS position on a map
	- TrackGPS:
		Tests displaying GPS position as a text box
		
Main Project (HikingApp/Main):
Due to the large build size, it is a bit hard to find the actual source.
	Source code can be found in
		HikingApp/Main/app/src/main/java/arc/com/arctrails
	Layout xml files can be found in
		HikingApp/Main/app/src/main/res/layout

The main entry point to the app is MenuActivity.java
Most functionality is handled through callbacks from the Android framework

Following is a list of classes, with simplified functionality documentation (updated 2017-Dec-1)
---------------------------------------------------------------
****Detailed documentation can be found in the source code.****
---------------------------------------------------------------
AlertUtils
	Increment3:
		Handles pop-up messages
		
Coordinates
	Increment1:
		Displays user's coordinates
	Increment3:
		Allows coordinates to be recorded
		
CustomMapFragment
	Increment1:
		Displays the user's position
	Increment2:
		Displays trails
	Increment3:
		Modified to zoom in on selected trails
		
GPXFile
	Increment2:
		Uses the GPXParser library to load trails from files
	Increment3:
		Uses the GPXParser library to write a trail to a file
		
initAssets
	Increment2:
		Loads files onto the user's device

LocationPermissionListener
	Increment1:
		Listens for results of permission requests

LocationRequestListener
	Increment1:
		Asks the user for location permission

MenuActivity		--(Main)
	Increment1:
		Handles permission requests
	Increment2:
		Allows the user to select a trail to view
	Increment3:
		Allows the user to record and save trails

NewTrailActivity
	Increment3:
		Gathers information for a trail file
		
TrailDataActivity
	Increment2:
		Displays information about a trail