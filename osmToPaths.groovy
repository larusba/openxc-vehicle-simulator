import groovy.json.*

inputFile = args[0]
opendata = new XmlSlurper().parse(inputFile)

nodeMap = [:]


class Node{
	Double lat
	Double lon
}

class Street{
	String name
	List   points = []
}

opendata.node.each
{
	def n = new Node()
	n.lat = Double.valueOf( it.@lat.toString() )
	n.lon = Double.valueOf( it.@lon.toString() )
	k = "${it.@id}"
	nodeMap[k] = n;
}

streetMap = [:]

opendata.way.each
{
	way = it
	tags = [:]
	it.tag.each
	{
		 tags["${it.@k}"] = "${it.@v}"
	}
	
	streetName = tags["name"]
	//building = tags["building"]
	if(streetName != null) // && ( streetName.startsWith("Via") || streetName.startsWith("Piaz")))
	{
		def sn = way.@id.toString()
		def street = streetMap[sn]
		if(street == null)
		{
			street = new Street()
			street.name = streetName
			streetMap.put(sn,street)
		}

		way.nd.each
		{
			k = "${it.@ref}"
			node = nodeMap[k]
			street.points.add(node)
		}

	}
	
}

class RoutePath{
	String path_id
	List   streets = []
}

def pathList = []

opendata.relation.each
{
	
	rel = it
	tags = [:]
	it.tag.each
	{
		 tags["${it.@k}"] = "${it.@v}"
	}
	
	type = tags["type"]
	
	if(type == "route")
	{
		def path = new RoutePath()
		path.path_id = rel.@id
		it.member.each
		{
			if(it.@type == "way")
			{
				def ref =  streetMap[ "${it.@ref}"]
				if(ref != null)
				{
					path.streets.add(ref)	
				}
			}
		}		
		if(! path.streets.isEmpty())
			pathList.add(path)
	}
}

// add fine-grained points

def calculateDistance = { lat1,lon1,lat2,lon2 ->
	earthRadius = 6371000 // raggio della terra
	dLat = Math.toRadians(lat2 - lat1)
	dLng = Math.toRadians(lon2 - lon1)
	a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2)
	c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
	dist = (earthRadius * c)
	return dist
}

def carLen = 4 //meter
def routes = []

pathList.each
{
	def route = new RoutePath()
	routes.add(route)
	route.path_id = it.path_id

	it.streets.each
	{
		def street = new Street()
		street.name = it.name
		route.streets.add(street)

		def finePoints = []
		def prevPoint = it.points[0]
		finePoints.add(prevPoint)	
		it.points.each
		{
			Double lat1 = prevPoint.lat
			Double lon1 = prevPoint.lon
			Double lat2 = it.lat
			Double lon2 = it.lon
			
			def lenMeter = calculateDistance(lat1,lon1,lat2,lon2)
			def lenDegree = Math.sqrt((lat1-lat2)**2+(lon1-lon2)**2)
			int carNum = lenMeter / carLen	
			
			if(carNum > 0)
			{
				Double carDegree = lenDegree/carNum
				Double fi = Math.atan2(lon1 - lon2, lat1 - lat2 )
				Double x = carDegree * Math.cos(fi)
				Double y = carDegree * Math.sin(fi)
				carNum.times
				{
					Double lat = lat2 + x*it
					Double lon = lon2 + y*it 
					
					def point = new Node()
					point.lat = lat	
					point.lon = lon
					finePoints.add(point)
				}

			}
			prevPoint = it
		}//each point		
		finePoints.add(prevPoint)
		street.points = finePoints
	}//streets
}

def json = JsonOutput.toJson(routes)
println JsonOutput.prettyPrint(json)

