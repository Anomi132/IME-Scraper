# Analiza strani za pridobivanje rudarskih podatkov

## Uporabne strani (Potencialni viri ali povezano)

*   **Nacionalni portal (glavni zemljevid):** https://ms.geo-zs.si/sl-SI/Karta

---

## Analiza strani: https://ms.geo-zs.si/sl-SI/Karta

Ta stran uporablja knjižnico OpenLayers za prikaz zemljevida. Podatki o rudnikih se dinamično nalagajo iz zunanjih virov preko definiranih API klicev.

### Definicije URL-jev iz JavaScript kode:

Naslednje spremenljivke v JavaScript kodi (`/bundles/ol-osnovna?...` ali `/bundles/ol-podrobnosti?...`) definirajo vire podatkov, ki so **ArcGIS REST Service Endpoints**:

```js
var app = window.app,
    wgs84Sphere = new ol.Sphere(6378137),
    urlRaster = "https://gis.geo-zs.si/server/rest/services/MS/MsGeoPsilomelanD96_Pro/MapServer",
    urlTopo = "https://gis.geo-zs.si/server/rest/services/topo/TOPO_KARTE_3794/MapServer",
    urlDmr = "https://gis.geo-zs.si/image/rest/services/TOPO/Hillshade/ImageServer",
    urlDof = "https://geohub.gov.si/ags/rest/services/TEMELJNE_KARTE/DOF2023_D96/MapServer",
    urlKataster = "https://gis.geo-zs.si/server/rest/services/topo/zemljiski_kataster_D96/MapServer",
    vectorUrlTocke = "https://gis.geo-zs.si/server/rest/services/MS/MsGeoPsilomelanD96_Pro/FeatureServer/2",
    vectorUrlPoligoni = "https://gis.geo-zs.si/server/rest/services/MS/MsGeoPsilomelanD96_Pro/FeatureServer/4",
    urlRasterKopi = "https://gis.geo-zs.si/server/rest/services/MS/MsGeo_nelegalni_kopiD96/MapServer",
    vectorUrlTockeKopi = "https://gis.geo-zs.si/server/rest/services/MS/MsGeo_nelegalni_kopiD96/FeatureServer/0",
    vectorUrlTockeZgodovina = "https://gis.geo-zs.si/server/rest/services/MS/MsGeoPsilomelanD96_Pro/FeatureServer/6",
    vectorUrlPoligoniZgodovina = "https://gis.geo-zs.si/server/rest/services/MS/MsGeoPsilomelanD96_Pro/FeatureServer/8",
    vectorUrlTockeOstalo = "https://gis.geo-zs.si/server/rest/services/MS/MsGeoPsilomelanD96_Pro/FeatureServer/15",
    vectorUrlPoligoniOstalo = "https://gis.geo-zs.si/server/rest/services/MS/MsGeoPsilomelanD96_Pro/FeatureServer/17",
    urlRasterPremogovniki = "https://gis.geo-zs.si/server/rest/services/MS/Rudisca_Premogovniki/MapServer",
    vectorUrlTockePremogovniki = "https://gis.geo-zs.si/server/rest/services/MS/Rudisca_Premogovniki/FeatureServer/1",
    urlRasterRudisca = "https://gis.geo-zs.si/server/rest/services/MS/Rudisca_Premogovniki/MapServer",
    vectorUrlTockeRudisca = "https://gis.geo-zs.si/server/rest/services/MS/Rudisca_Premogovniki/FeatureServer/0",
    atribucije = $("#karta_atributions").attr("value");
```

### Način pridobivanja podatkov: Uporaba ArcGIS REST API

*Postopek:* Pošljemo direktno GET zahtevo na API endpoint Feature Serverja za vsak želeni sloj (npr. .../FeatureServer/2/query). Z ustreznimi parametri (where=1=1, outFields=*, returnGeometry=true, f=json) pridobimo vse atribute in geometrijo za vse objekte v sloju v strukturirani JSON obliki.

- Primer poizvedbe za vse pridobivalne/raziskovalne prostore (točke):
  - https://gis.geo-zs.si/server/rest/services/MS/MsGeoPsilomelanD96_Pro/FeatureServer/2/query?where=1%3D1&outFields=*&returnGeometry=true&f=json

- Primer poizvedbe za vsa rudišča (točke):
  - https://gis.geo-zs.si/server/rest/services/MS/Rudisca_Premogovniki/FeatureServer/0/query?where=1%3D1&outFields=*&returnGeometry=true&f=json
  
- Primer poizvedbe za pridobivanje mej vseh rudišč
  - https://gis.geo-zs.si/server/rest/services/MS/MsGeoPsilomelanD96_Pro/FeatureServer/4/query?where=1%3D1&objectIds=&time=&geometry=&geometryType=esriGeometryEnvelope&inSR=&defaultSR=&spatialRel=esriSpatialRelIntersects&distance=&units=esriSRUnit_Foot&relationParam=&outFields=*&returnGeometry=true&maxAllowableOffset=&geometryPrecision=&outSR=&havingClause=&gdbVersion=&historicMoment=&returnDistinctValues=false&returnIdsOnly=false&returnCountOnly=false&returnExtentOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&multipatchOption=xyFootprint&resultOffset=&resultRecordCount=&returnTrueCurves=false&returnExceededLimitFeatures=false&quantizationParameters=&returnCentroid=false&timeReferenceUnknownClient=false&maxRecordCountFactor=&sqlFormat=none&resultType=&featureEncoding=esriDefault&datumTransformation=&f=geojson 

Primer rezultata API-ja:

```
id_naha: 5001
ime_nahajalisca: Škofje-Cerkno
Y: 424210
X: 109360
N: 109847.32
E: 423838.65
rudisce: 1
premogovnik: 0
Point:
X: 423838.6500000004
Y: 109847.3200000003
```

Podatki pridobljeni iz API-ja že vsebujejo večino informacij, ampak moramo narediti klic s pomočjo `id_naha` ali `ID_NAHA_KONCES`, da dobimo vse podatke s strežnika:

- https://ms.geo-zs.si/Nahajalisce/Podrobnosti/{id_naha}
- https://ms.geo-zs.si/Prostor/Podrobnosti/{ID_NAHA_KONCES}
- 

