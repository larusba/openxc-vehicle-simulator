/*
Use: <path.json>
*/

import groovy.json.*
import static java.util.UUID.randomUUID 


def pathsFile = new File( args[0] )

def jsonSlurper = new JsonSlurper()

def pathList = jsonSlurper.parse(pathsFile)

def streetMap = [:]

def calculateDistance = { lat1,lon1,lat2,lon2 ->
	earthRadius = 6371000 // raggio della terra
	dLat = Math.toRadians(lat2 - lat1)
	dLng = Math.toRadians(lon2 - lon1)
	a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2)
	c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
	dist = (earthRadius * c)
	return dist
}


pathList.each
{
	def startTime = it.start_time
	def endTime = it.end_time
	def vehicleCount = it.vehicle_count
	def avgSpeed = it.avg_speed //in KM/h

	long msPerMeter = 3600/avgSpeed 
	long deltaTime = (endTime-startTime)/vehicleCount

	def path = it

	vehicleCount.times
	{
		def instant = startTime
		def prevPoint = null
		def totalLen = 0
		def totTime = 0
		vehicleId = randomUUID() as String
		path.streets.each
		{
			def streetName = it.name
			def points = it.points
			points.each
			{
				if(prevPoint == null)
				{
					prevPoint = it	
				}
				def len = calculateDistance(Double.valueOf(prevPoint.lat),Double.valueOf(prevPoint.lon),Double.valueOf(it.lat),Double.valueOf(it.lon))
				totalLen += len
				long travelTime = len * msPerMeter
				totTime += travelTime
				instant += travelTime
				println "${vehicleId},${instant},${it.lat},${it.lon}"
				prevPoint = it
			}
		}
		startTime = startTime + deltaTime
		//println "${path.path_id} percorso lungo ${totalLen} metri percorso in ${totTime} ms"
	} //vehicle_count
} //paths

