import java.math.BigDecimal

class Rudnik (
    val ID: Int,
    val name: String,
    val Y: BigDecimal,
    val X: BigDecimal,
    val N: BigDecimal,
    val E: BigDecimal,
    val oreDeposit: Boolean,
    val coalMine: Boolean,
    var status: String = "",
    var potential:  String = "",
    var municipality: String = "",
    var excavationMethod: String = "",
    var excavationStart: Int = 0,
    var excavationEnd: Int = 0,
    var oreSupplies: String = "",
    var rockType: String = "",
    var mainOre: String = "",
    var mineral: String = "",
    var sideOres: String = "",
    var usage: String = "",
    var endUsage: String = "",
){
    fun print() {
        println("ID: $ID")
        println("IME: $name")
        println("Y: $Y")
        println("X: $X")
        println("N: $N")
        println("E: $E")
        println("VRSTA: " + if (oreDeposit) "RUDISCE" else "PREMOGOVNIK")
        println("Status: $status")
        println("Potencial: $potential")
        println("Občina: $municipality")
        println("Način odkopavanja: $excavationMethod")
        println("Začetek obratovanja: $excavationStart")
        println("Konec obratovanja: $excavationEnd")
        println("Rudne zaloge: $oreSupplies")
        println("Kamnina: $rockType")
        println("Glavna rudnina: $mainOre")
        println("Mineral: $mineral")
        println("Stranska rudnina: $sideOres")
        println("Uporaba: $usage")
        println("Končna uporaba: $endUsage")
        println()
    }

    //Funkicja, ki odstrani poimenovanje rudnine
    companion object {
        fun cleanMineral(input: String): String {
            val parts = input.substringBefore(" -").split(",")
            return if (parts.size >= 2) {
                parts[0].trim() + ", " + parts[1].trim()
            } else {
                input.trim()
            }
        }
    }
}