/*
Use: <path.json>
*/

import groovy.json.*
import static java.util.UUID.randomUUID 

def deltaTime = 10 * 1000  //millis
def startTime = System.currentTimeMillis()
def vehicleCount = 3
int maxNodes = 5  //numero massimo di nodi percorribili per unità di tempo

def pathsFile = new File( args[0] )

def jsonSlurper = new JsonSlurper()

def pathList = jsonSlurper.parse(pathsFile)

def streetMap = [:]
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
		p.blockCount = 0
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
	def queueIn(p){
		def gp = getPoint(p)
		gp.blockCount--
		if(gp.blockCount <= 0)
		{
			gp.blockCount = 0
			gp.blocked = false
		}
	}
	def blockPoint(p,c){
		def gp = getPoint(p)
		if(! gp.blocked)
		{
			gp.blocked = true
			gp.blockCount = c
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

	def avgSpeed = it.avg_speed //in KM/h
	long msPerMeter = 3600/avgSpeed 

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
	} //vehicle_count

	startTime += deltaTime
}

//TEMP CODE
def bp = geomap.getPoint( vehicles[0].points[1] )
bp.blocked = true
bp.blockCount = 3
//=================

println "vechicle_id,timestamp,latitude,longitude"
int vehiclesInRace = vehicles.size()
//change with something
while(vehiclesInRace > 0){
	vehicles.each{
		def vehicle = it	
		if(!vehicle.arrived){

			def point = vehicle.points[vehicle.currentPointIndex]	

			vehicle.currentTime += deltaTime
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
				
	/*
				if(gnp.blocked)
				{
					geomap.queueIn(nextPoint)
					geomap.blockPoint(point,gnp.blockCount-1)	
				}else{
				//TODO	vehicle.currentPointIndex += Math.abs(new Random().nextInt() % maxNodes) + 1
					vehicle.currentPointIndex++
				}
	*/
			}
			else
			{
				vehicle.arrived = true
				vehiclesInRace--
				geomap.freePoint(point)
			}
			println "${vehicle.id},${vehicle.currentTime},${point.lat},${point.lon}"
		}//if vehicle arrived	
	}//each vehicles
	
} //while vehicle in race

