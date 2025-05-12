package jsonObjects

import kotlinx.serialization.Serializable

/*
    Hrani več rudnikov naenkrat
 */
@Serializable
data class ArcGISFeatureSet(
    val features: List<ArcGISFeature>
)
