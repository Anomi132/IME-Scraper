package jsonObjects

import kotlinx.serialization.Serializable

/*
    Hrani vse atribute rudišča razen geometrije
 */
@Serializable
data class ArcGISFeature(
    val attributes: RudisceAttributes
)
