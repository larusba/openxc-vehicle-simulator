inputFile = args[0]
opendata = new XmlSlurper().parse(inputFile)

nodeMap = [:]

class Node{
	String lat
	String lon
}

opendata.node.each
{
	def n = new Node()
	n.lat = it.@lat
	n.lon = it.@lon

	k = "${it.@id}"
	nodeMap[k] = n;
}

println "Way,Latitude,Longitude"

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

	if(streetName != null ) //&& ( streetName.startsWith("Via") || streetName.startsWith("Piaz")))
	{
		way.nd.each
		{
			k = "${it.@ref}"
			node = nodeMap[k]
			println "${streetName},${node.lat},${node.lon}"
		}

	}
	
}

