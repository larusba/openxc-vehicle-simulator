/*
Use: <path.json>
*/

import groovy.json.*
import static java.util.UUID.randomUUID 

//TODO external?
def deltaTimeSecond=10
def deltaTime = deltaTimeSecond * 1000  //millis
def startTime = System.currentTimeMillis()
def pointDistance = 4 //meter

//INPUT
def pathsFile = new File( args[0] )
def blockageFile = new File( args[1] )

//OUTPUT
def geospatialFile = new File("data/latitute_longitude.csv")
def ignitionFile = new File("data/ignition_status.csv")

def jsonSlurper = new JsonSlurper()

def pathList = jsonSlurper.parse(pathsFile)
def blockageList = jsonSlurper.parse(blockageFile)

def streetMap = [:]


//OUTPUT HEADERS
geospatialFile << "vechicle_id,timestamp,latitude,longitude\n"
ignitionFile  << "vechicle_id,timestamp,value\n"

/*
def calculateDistance = { lat1,lon1,lat2,lon2 ->
	earthRadius = 6371000 // raggio della terra
	dLat = Math.toRadians(lat2 - lat1)
	dLng = Math.toRadians(lon2 - lon1)
	a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2)
	c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
	dist = (earthRadius * c)
	return dist
}
*/

class Vehicle
{
	String id
	List points = []
	long startTime
	long currentTime
	int currentPointIndex
	boolean arrived
	int maxPointsInDeltaTime
}

//join point from all paths
class GeoMap{
	def points = [:]
	def equals(p1,p2)
	{
		if(p2 == null)
			return false
		return "${p1.lat},${p1.lon}".toString() == "${p2.lat},${p2.lon}".toString()
	}
	def addPoint ( p ){
		p.blocked = false
		p.blockTime = 0
		p.firstTimeQueue = -1
		p.occupied = false 
		points.put("${p.lat},${p.lon}".toString(),p)
	}//addPoint
	def getPoint ( p ) {
		return points["${p.lat},${p.lon}".toString()]
	}
	def isBlocked(p){
		def gp = getPoint(p)
		return gp.blocked
	}
	def queueIn(p,t){
		def gp = getPoint(p)
		if(gp.firstTimeQueue > 0)
		{
			def rim = t - gp.firstTimeQueue
			if(rim > gp.blockTime)
			{
				gp.blocked = false
				gp.firstTimeQueue = -1
			}
		}else
		{
			gp.firstTimeQueue = t	
		}
	}
	def blockPoint(p,c){
		def gp = getPoint(p)
		if(! gp.blocked)
		{
			gp.blocked = true
			gp.blockTime = c
		}
	}
	def occupyPoint(p){
		def gp = getPoint(p)
		gp.occupied = true
	}
	def freePoint(p){
		def gp = getPoint(p)
		gp.occupied = false
	}
}

def geomap = new GeoMap()
pathList.each
{
	it.streets.each{
		it.points.each{
			geomap.addPoint(it)
		}
	}
}

def vehicles = []
//vehicles initialization
pathList.each
{
	def vehicleCount = it.vehicle_count
	def maxSpeed = it.max_speed //in KM/h
	Double maxSpeedMS = maxSpeed / 3.6  // meter per second
	Double pointPerSecond = maxSpeedMS / pointDistance 
	int maxPoints = pointPerSecond * deltaTimeSecond

	def path = it

	def pointKeySet = [] as Set
	def pointsPath = []
	path.streets.each{
		it.points.each{
			if(pointKeySet.add(it))
				pointsPath.add(it)
		}
	}

	vehicleCount.times
	{
		def vehicle = new Vehicle()
		vehicles.add(vehicle)

		vehicle.id = randomUUID() as String
		vehicle.startTime = startTime
		vehicle.currentTime = startTime
		vehicle.currentPointIndex = 0
		vehicle.arrived = false
		vehicle.points = pointsPath
		vehicle.maxPointsInDeltaTime=maxPoints
	} //vehicle_count

	startTime += deltaTime
}

//blockage settings
blockageList.each{
	def bp = geomap.blockPoint(it,it.deltaTime*1000)
}


vehicles.each{
	ignitionFile  << "${it.id},${it.currentTime},run\n"
}



int vehiclesInRace = vehicles.size()
//change with something
while(vehiclesInRace > 0){
	vehicles.each{
		def vehicle = it	
		def pointsToRun = Math.abs(new Random().nextInt() %  vehicle.maxPointsInDeltaTime) + 1
		long partialTime = deltaTime / pointsToRun
		def point = vehicle.points[vehicle.currentPointIndex]	

		def internalTime = vehicle.currentTime
		pointsToRun.times{
			internalTime += partialTime
			if(!vehicle.arrived){

				point = vehicle.points[vehicle.currentPointIndex]	


				if(geomap.isBlocked(point))
				{
					geomap.queueIn(point,internalTime)	
				}else{

					boolean samePoint = true
					int index = 0
					while(samePoint)
					{
						index++
						samePoint = geomap.equals(point, vehicle.points[vehicle.currentPointIndex+index])
					}
				
					def nextPosition = vehicle.currentPointIndex+index
					if(nextPosition < vehicle.points.size())
					{
						def nextPoint = vehicle.points[nextPosition]	
						def gnp = geomap.getPoint(nextPoint)
						if(! gnp.occupied)
						{
							gnp.occupied = true
							geomap.freePoint(point)
							point = nextPoint
							vehicle.currentPointIndex = nextPosition
						}
					}
					else
					{
						vehicle.arrived = true
						vehiclesInRace--
						geomap.freePoint(point)
					}
				}//else blocked
			}//if vehicle arrived	
		}//pointsToRun.times

		vehicle.currentTime += deltaTime
		if(!vehicle.arrived){
			geospatialFile  << "${vehicle.id},${vehicle.currentTime},${point.lat},${point.lon}\n"
		}
	}//each vehicles
	
} //while vehicle in race

vehicles.each{
	ignitionFile  << "${it.id},${it.currentTime},off\n"
}
