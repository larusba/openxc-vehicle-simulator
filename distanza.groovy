


def lat1 = Double.valueOf("43.7771147")
def lon1 = Double.valueOf("11.2561717")
def lat2 = Double.valueOf("43.7784822")
def lon2 = Double.valueOf("11.2572432")


earthRadius = 6371000 // raggio della terra
dLat = Math.toRadians(lat2 - lat1)
dLng = Math.toRadians(lon2 - lon1)
a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2)
c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
dist = (earthRadius * c)

println dist


def len = Math.sqrt((lon1-lon2)**2 + (lat1-lat2)**2)
def carLen = 4

int finePointsNumber = dist / carLen	
println "Punti: $finePointsNumber"
Double fi = Math.atan2(lon1 - lon2, lat1 - lat2 )

def lonD = lon1-lon2
def latD = lat1-lat2
println "b = $lonD = ${len*Math.sin(fi)}"
def seg = len / finePointsNumber
finePointsNumber.times
{
	
	Double lat = lat1 + it * seg * Math.cos(fi);
	Double lon = lon1 + it * seg * Math.sin(fi);
	
	println "$lat, $lon ${Math.cos(fi)}"
}

