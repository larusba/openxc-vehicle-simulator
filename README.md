# openxc-vehicle-simulator
Vehicle simultator generate sensor data over opencx specification. 
The project works using node/way/relation of OpenStreetMap project.
You can download a map portion from: http://www.openstreetmap.org/

# Usage
The following example is stored into examples/florence-station.tar.gz.

From the OpenStreetMap file 'florence-station-osm.xml' we can create a table of way and latitude/longitude point mapping.

```
groovy osmToCsv.groovy ./examples/florence-station-osm.xml > examples/florence-station.csv
```
To simulate vehicle's movements you have to extract some paths from the existing ones.

osmToPaths.groovy converts all xml data into a structured json file.

```
groovy osmToPaths.groovy examples/florence-station-osm.xml > examples/florence-station-paths.json
```
Now you have to choise one o more paths e copy (by hand) into another file (path_single.json in the example).

For each paths you have to set 2 attributes:

```
"vehicle_count": 5,
"max_speed": 50,
```
"vehicle_count" is the number of vehicles you want to create.

"max_speed" is the maximum speed in km/h for a single vehicle.

To create a blockage you have to create a file like this:

```
[
    {
	"lat": 43.781966249999996,
	"lon": 11.237302949999998,
	"deltaTime": 600
    }
]
```
"deltaTime" is how long (in seconds) a vehicle stays in that point.

So, now's the time to generate the vechicle's data.

```
cd examples/
mkdir data
groovy ../vehicleSimulator.groovy path_single.json blockage.json 
```

It creates into "data" directory some CSV files with sensor's data.

