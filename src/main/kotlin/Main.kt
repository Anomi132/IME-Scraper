import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import jsonObjects.ArcGISFeatureSet
import jsonObjects.RudisceAttributes
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.math.BigDecimal

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
        val panels = doc.select("div.panel")

        for (panel in panels) {
            val panelTitleElement = panel.selectFirst("div.panel-heading h3.panel-title")
            val panelTitle = panelTitleElement?.text()?.trim() ?: ""

            if (panelTitle.equals("Literatura", ignoreCase = true)) {
                continue
            }

            val dlElement = panel.selectFirst("div.panel-body dl.dl-horizontal.premRudDL")

            dlElement?.select("dt")?.forEach { dt ->
                val key = dt.text().trim()
                var dd: Element? = dt.nextElementSibling()

                while (dd != null && dd.tagName() != "dd") {
                    dd = dd.nextElementSibling()
                }

                if (dd != null && dd.tagName() == "dd") {
                    val value = dd.text().trim()
                    if (key.isNotEmpty()) {
                        detailsMap[key] = value
                    }
                }
            }
        }
    } catch (e: Exception) {
        println("Error during HTML parsing: ${e.message}")
        e.printStackTrace()
    }
    return detailsMap
}

/*
    Funkcija naredi posebej GET in pridobi podrobne podatke nahajališč, shrani v json in vrne
    WARNING TRENUTNO VZAME SAMO PRVIH 5 DA SE NE OBREMENI STREŽNIKA
 */
fun getDetails(rudniki: List<Rudnik>)
{
    println("\n========== Fetching Details for first 5 Rudnik ==========")
    // TU SE NASTAVI DA JE SAMO 5, ko se vezem vse odstrani [take(5)`]
    for(rudnik in rudniki.take(5)){
        val idNahajalisca = rudnik.ID;
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
                    rudnik.status = extractedDetails["Stanje:"] ?: "undefined";
                    rudnik.potential = extractedDetails["Perspektivnost:"] ?: "undefined";
                    rudnik.municipality = extractedDetails["Občina:"] ?: "undefined";
                    rudnik.excavationMethod = extractedDetails["Način odkopavanja:"] ?: "undefined";
//                    rudnik.excavationStart = extractedDetails["Začetek obratovanja:"]?.toIntOrNull()!!;
                    rudnik.excavationStart = if (extractedDetails["Začetek obratovanja:"]?.isEmpty() == true) 0
                    else extractedDetails["Začetek obratovanja:"]?.toInt()!!
//                    rudnik.excavationEnd = extractedDetails["Konec obratovanja:"]?.toIntOrNull()!!;
                    rudnik.excavationEnd = if (extractedDetails["Konec obratovanja:"]?.isEmpty() == true) 0
                    else extractedDetails["Konec obratovanja:"]?.toInt()!!
                    rudnik.oreSupplies = extractedDetails["Rudne zaloge:"]?.replace(Regex("(?i)ocenjeno"), "")?.trim()
                        ?: "undefined";
                    rudnik.rockType = extractedDetails["Kamnina:"] ?: "undefined";
//                    rudnik.mainOre = extractedDetails["Glavni rudni mineral in kovina:"]?.substringBefore(" -")?.trim()?.substringAfterLast(",")?.trim() ?: "";
                    rudnik.mainOre = Rudnik.cleanMineral(extractedDetails["Glavni rudni mineral in kovina:"] ?: "undefined")
                    rudnik.mineral = extractedDetails["mineral in kovina:"] ?: "undefined"
//                    rudnik.sideOres = extractedDetails["Stranski rudni mineral in kovina:"]?.substringBefore(" -")?.trim()?.substringAfterLast(",")?.trim() ?: "";
                    rudnik.sideOres = Rudnik.cleanMineral(extractedDetails["Stranski rudni mineral in kovina:"] ?: "undefined")
                    rudnik.usage = extractedDetails["Uporaba:"] ?: "undefined";
                    rudnik.endUsage = extractedDetails["Končna uporaba:"] ?: "undefined";
                } else {
                    println("Error fetching details for ID $idNahajalisca: HTTP Status ${status { code }} - ${status { message }}")
                }
            }
        }
        rudnik.print();
    }
    println("\n========== Finished Fetching Details ==========")
}

fun RudisceAttributes.makeRudnik(): Rudnik {
    return Rudnik(
        ID = this.id_naha,
        name = this.ime_nahajalisca,
        Y = BigDecimal(this.Y),
        X = BigDecimal(this.X),
        N = BigDecimal(this.N),
        E = BigDecimal(this.E),
        oreDeposit = this.rudisce != 0,
        coalMine = this.premogovnik != 0
    )
}

fun main() {
    val rudnikiJson = getRudnikiJsonData()
    val tlorisJson = getRudnikiTlorisData() // Vse data za tloris v GEOJSON.

    println("\n========== Received JSON data ==========")
//    println(rudnikiJson)

    val json = Json { ignoreUnknownKeys = true }
    val rudniki = json.decodeFromString<ArcGISFeatureSet>(rudnikiJson)

    val rudnikiList: List<Rudnik> = rudniki.features.map { it.attributes.makeRudnik() }

    getDetails(rudnikiList)
}