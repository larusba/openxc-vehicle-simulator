
opendata = new XmlSlurper().parse(args[0])

println "CREATE INDEX ON :GEOPOINT(idnode);"
println "CREATE INDEX ON :GEOWAY(name);"


opendata.node.each
{
	println "MERGE (n:GEOPOINT {idnode: ${it.@id}, lat: ${it.@lat}, lon: ${it.@lon}}) ;"
}

opendata.way.each
{
	way = it
	it.tag.each
	{
		if(it.@k == "name")
		{
			streetName = it.@v
			println "MERGE (w:GEOWAY {name: \"${streetName}\"});"  //other info? region? city?
			way.nd.each
			{
				println "MATCH (n:GEOPOINT {idnode:${it.@ref}}), (w:GEOWAY {name: \"${streetName}\"}) MERGE (w)-[:GEOWAY_POINT]->(n);"
			}

		}
	}
	
}

println "match (w:GEOWAY)-[:GEOWAY_POINT]->(p:GEOPOINT)<-[:GEOWAY_POINT]-(w2:GEOWAY) MERGE (w)-[:GEOWAY_JUNCTION]->(w2);"
