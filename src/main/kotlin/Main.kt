import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import jsonObjects.ArcGISFeatureSet
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup

@Serializable
class RudnikDat(
    val id_naha: Int
){}

/*
    Funkcija pokliče Url in nam vrne pridoblejn JSON
 */
fun getRudnikiJsonData(): String  {
    val jsonUrl = "https://gis.geo-zs.si/server/rest/services/MS/Rudisca_Premogovniki/FeatureServer/0/query?where=1%3D1&outFields=*&returnGeometry=true&f=json"

    println("========== Fetching JSON data ==========")

    return skrape(HttpFetcher) {
        request {
            url = jsonUrl
        }

        response {
            println("HTTP status code: ${status { code }}")
            println("HTTP status message: ${status { message }}")

            responseBody
        }
    }
}

/*
*   Funkcija vrne GeoJson s vsemi tloris podatki v obliki poligonov
*/
fun getRudnikiTlorisData(): String  {
    val jsonUrl = "https://gis.geo-zs.si/server/rest/services/MS/MsGeoPsilomelanD96_Pro/FeatureServer/4/query?where=1%3D1&objectIds=&time=&geometry=&geometryType=esriGeometryEnvelope&inSR=&defaultSR=&spatialRel=esriSpatialRelIntersects&distance=&units=esriSRUnit_Foot&relationParam=&outFields=*&returnGeometry=true&maxAllowableOffset=&geometryPrecision=&outSR=&havingClause=&gdbVersion=&historicMoment=&returnDistinctValues=false&returnIdsOnly=false&returnCountOnly=false&returnExtentOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&multipatchOption=xyFootprint&resultOffset=&resultRecordCount=&returnTrueCurves=false&returnExceededLimitFeatures=false&quantizationParameters=&returnCentroid=false&timeReferenceUnknownClient=false&maxRecordCountFactor=&sqlFormat=none&resultType=&featureEncoding=esriDefault&datumTransformation=&f=geojson"

    println("========== Fetching JSON data ==========")

    return skrape(HttpFetcher) {
        request {
            url = jsonUrl
        }

        response {
            println("HTTP status code: ${status { code }}")
            println("HTTP status message: ${status { message }}")

            responseBody
        }
    }
}

/*
    Funkcija parsa HTML, da dobi vse potrebne atribute
 */
fun parseNahajalisceHtml(html: String): Map<String, String> {
    val detailsMap = mutableMapOf<String, String>()

    try {
        val doc = Jsoup.parse(html)
        val dlElement = doc.selectFirst("div.panel-body dl.dl-horizontal")
        dlElement?.select("dt")?.forEach { dt ->
            val key = dt.text().trim()
            val dd = dt.nextElementSibling()
            if (dd != null && dd.tagName() == "dd") {
                val value = dd.text().trim()
                if (key.isNotEmpty()) {
                    detailsMap[key] = value
                }
            }
        }
    } catch (e: Exception) {
        println("Error during HTML parsing: ${e.message}")
    }
    return detailsMap
}

/*
    Funkcija naredi posebej GET in pridobi podrobne podatke nahajališč, shrani v json in vrne
    WARNING TRENUTNO VZAME SAMO PRVIH 5  DA SE NE OBREMENI STREŽNIKA
 */
fun getDetails(rudniki: ArcGISFeatureSet)
{
    println("\n========== Fetching Details for first 5 Rudnik ==========")

    // TU SE NASTAVI DA JE SAMO 5
    rudniki.features.take(5).forEach { feature ->
        val idNahajalisca = feature.attributes.id_naha
        val detailUrl = "https://ms.geo-zs.si/Nahajalisce/Podrobnosti/$idNahajalisca"

        println("\n--- Fetching details for ID: $idNahajalisca from $detailUrl ---")

        skrape(HttpFetcher) {
            request {
                url = detailUrl
                headers = mapOf("User-Agent" to "IME-Rudniki-FERI/1.0")
            }
            response {
                if (status { code } == 200) {
                    val extractedDetails = parseNahajalisceHtml(responseBody)
                    println("Extracted Details for ID $idNahajalisca:")
                    extractedDetails.forEach { (key, value) ->
                        println("  $key: $value")
                    }
                } else {
                    println("Error fetching details for ID $idNahajalisca: HTTP Status ${status { code }} - ${status { message }}")
                }
            }
        }
    }
    println("\n========== Finished Fetching Details ==========")
}


fun main() {
    val rudnikiJson = getRudnikiJsonData()
    val tlorisJson = getRudnikiTlorisData()

    println("\n========== Received JSON data ==========")
    println(rudnikiJson)

    val json = Json { ignoreUnknownKeys = true }
    val rudniki = json.decodeFromString<ArcGISFeatureSet>(rudnikiJson)

    getDetails(rudniki)
}