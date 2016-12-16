import groovy.json.*

inputFile = args[0]
opendata = new XmlSlurper().parse(inputFile)

nodeMap = [:]

class Node{
	String lat
	String lon
}

class Street{
	String name
	List   points = []
}

opendata.node.each
{
	def n = new Node()
	n.lat = it.@lat
	n.lon = it.@lon

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

def json = JsonOutput.toJson(pathList)
println JsonOutput.prettyPrint(json)
