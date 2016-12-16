


def lat1 = 43.7771147
def lon1 = 11.2561717
def lat2 = 43.7784822
def lon2 = 11.2572432


earthRadius = 6371000 // raggio della terra
dLat = Math.toRadians(lat2 - lat1)
dLng = Math.toRadians(lon2 - lon1)
a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2)
c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
dist = (earthRadius * c)

println dist
