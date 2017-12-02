 | 
 | ARCTrails
 | Ayla Wickham, Ryley Jewsbury, Caleigh LePage
 | CPSC300 - Software Engineering - 2017 Fall Semester
 |

ARCtrails is an application for android devices that allows you to locate hiking trails in the
Prince George area. Upon download, it provides a list of local trails for users to locate and
walk/hike depending on their eagerness for adventure. This app includes information about the
trails including the name, and a small description. Upon startup, the app will ask for location
permissions to allow the user to track their location. This uses GPS signals in reference to the
trail they have selected and allows the user to view their actual coordinates. This is helpful
in the case of getting lost, as they can alert others to their exact location. ARCtrails also
allows the user to create new trails. At the beginning of a new trail, a user may push a “Track”
button and have their GPS coordinates stored as they traverse the trails. When the user has reached
the end of the trail they may push a “Stop” button, which allows the user to choose a name and
write a short description of the trail and save it for other users to enjoy the hike as well.

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