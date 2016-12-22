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
		def sn = streetName.toString()
		def street = streetMap[sn]
		if(street == null)
		{
			street = new Street()
			street.name = sn
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

def json = JsonOutput.toJson(streetMap.values())
println JsonOutput.prettyPrint(json)

