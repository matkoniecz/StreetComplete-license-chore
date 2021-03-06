import java.io.File
import java.io.InputStream

class LicenceData(val licence: String, val file: String, val source: String) {}

class MediaFile(val filePath: File) {}

class LicencedFile(val licence: LicenceData, val file: MediaFile)

fun validLicences() : Array<String> {
    // entries from https://spdx.org/licenses/
    // and "SIL OFL-1.1" as alias for "OFL-1.1"
    return arrayOf("Public Domain", "CC0", "CC-BY-SA 1.0", "CC-BY-SA 2.0", "CC-BY-SA 2.5", "CC-BY-SA 3.0", "CC-BY-SA 4.0", "CC-BY 2.0", "CC-BY 3.0", "CC-BY 4.0", "SIL OFL-1.1", "OFL-1.1", "GPL-2.0-only", "WTFPL",)
}

fun filesWithKnownProblemsAndSkipped() : Array<String> {
    // TODO: should be empty
    return arrayOf(
        "ic_link_cyclosm.xml", // https://github.com/cyclosm/cyclosm-cartocss-style/issues/615#issuecomment-1152875875
        "ramp_wheelchair.jpg", "ramp_stroller.jpg", "ramp_none.jpg", "ramp_bicycle.jpg", // https://github.com/streetcomplete/StreetComplete/issues/4103
        "location_nyan.png", "car_nyan.png", // note it as a a fair use in authors file? https://en.wikipedia.org/wiki/File:Nyan_cat_250px_frame.PNG
        "ic_link_weeklyosm.png", // https://wiki.openstreetmap.org/wiki/File:Weeklyosm_red_cut.svg https://wiki.openstreetmap.org/wiki/File:Logo_weeklyOSM.svg
        "ic_link_brouter.png",
        "ic_link_heigit.png",
        "ic_link_learnosm.png",
        "ic_link_openstreetmap.png",
        "ic_link_osmhydrant.png",
        "ic_link_pic4review.png",
        "ic_link_city_roads.png",
        "ic_link_mapy_tactile.png",
        "ic_link_openvegemap.png", // https://wiki.openstreetmap.org/wiki/OpenVegeMap
        "ic_link_osmlab.png",
        "ic_link_sunders.png",
        "ic_link_wheelmap.png",
        "ic_link_figuregrounder.png",
        "ic_link_indoorequal.png",
        "ic_link_myosmatic.png",
        "ic_link_opnvkarte.png",
        "ic_link_osrm.png",
        "ic_link_thenandnow.png",
        "ic_link_wiki.png", // https://wiki.openstreetmap.org/wiki/File:Wikilogo.png
        "ic_link_graphhopper.png", // https://wiki.openstreetmap.org/wiki/GraphHopper
        "ic_link_openstreetbrowser.png",
        "ic_link_organic_maps.png",
        "ic_link_photon.png",
        "ic_link_valhalla.png",
        "plop0.wav",
        "plop1.wav",
        "plop2.wav",
        "plop3.wav",
    )
}

fun publicDomainAsSimpleShapesFilenames() : Array<String> {
    return arrayOf("speech_bubble_top.9.png", "speech_bubble_start.9.png",
                   "speech_bubble_end.9.png", "speech_bubble_none.9.png",
                   "speech_bubble_bottom.9.png",
                   "speech_bubble_bottom_center.9.png",
                   "crosshair_marker.png", "location_direction.png",
                   "building_levels_illustration_bg_left.png",
                   "building_levels_illustration_bg_right.png",
                   "background_housenumber_frame_slovak.9.png", "pin.png",
                   "ic_star_white_shadow_32dp.png",
                   "ic_none.png")
}

fun cutoff() : Int {
    return 30
}

fun containsSkippedFile(pattern: String): Boolean {
    for (file in filesWithKnownProblemsAndSkipped()) {
        if(pattern.contains(file)) {
            return true
            println("skipping " + file + " as listed on files with known problems")
        }
    }
    return false
}

fun licencedMedia(root : String) : MutableList<LicenceData> {
    val firstLocation = root + "/res/graphics/authors.txt" // some noxious to process TODO: use
    val secondLocation = root + "/app/src/main/assets/authors.txt" //bunch of special cases  TODO: use
    val thirdLocation = root + "/app/src/main/res/authors.txt"
    val inputStream: InputStream = File(thirdLocation).inputStream()
    val inputString = inputStream.bufferedReader().use { it.readText() }
    val knownLicenced = mutableListOf<LicenceData>()
    for(entire_line in inputString.split("\n").drop(3)) { // remove header lines
        var skipped = false
        val line = entire_line.trim()
        if(line.length == 0) {
            continue
        }
        var licenceFound: String? = null
        for(licence in validLicences()) {
            val splitted = line.split(licence)
            if(splitted.size == 2) {
                val file = splitted[0].trim()
                val source = splitted[1].trim()
                licenceFound = licence
                if(file.length > 0 && source.length > 0) {
                    knownLicenced += LicenceData(licence, file, source)
                } else {
                    println("line <" + line + "> skipped <" + file + "><" + source + ">")
                }
            }
        }
        if(containsSkippedFile(line)) {
            skipped = true
            println("skipping " + line + " as listed on files with known problems")
        }
        if (licenceFound == null && skipped == false) {
                throw Exception("unexpected licence in the input file was encountered, on " + line)
        }
    }
    return knownLicenced
}

fun mediaNeedingLicenes(root : String) : MutableList<MediaFile> {
    val mediaFiles = mutableListOf<MediaFile>()
    File(root + "/app/src/main/res").walkTopDown().forEach {
        if(it.getName().contains(".")) {
            val split_extension = it.getName().split(".")
            if(split_extension.size > 1) {
                var extension = split_extension.last()
                if(extension !in listOf("yaml", "yml", "xml", "txt")) {
                    mediaFiles += MediaFile(it)
                }
            }
        }
    }
    return mediaFiles
}
fun truncatedfileMatchesLicenceDeclaration(fileName : String, licencedFile : String, truncationLength: Int): Boolean {
    var truncatedFile = fileName
    val lenghtLimit = licencedFile.length-truncationLength
    if(truncatedFile.length >= lenghtLimit ) {
        truncatedFile = truncatedFile.substring(0, lenghtLimit)
    }
    var truncatedLicence = licencedFile
    var removedPartFromLicenseInfo = ""
    if(truncatedLicence.length >= lenghtLimit ) {
        removedPartFromLicenseInfo = truncatedLicence.substring(lenghtLimit, truncatedLicence.length)
        truncatedLicence = truncatedLicence.substring(0, lenghtLimit)
    }
    for(letter in removedPartFromLicenseInfo) {
        if((letter != '.') and (letter != '???')) {
            return false
        }
    }
    return truncatedFile.equals(truncatedLicence)
}

fun fileMatchesLicenceDeclaration(fileName : String, licencedFile : String): Boolean {
    for(delta in listOf(0, 1, 2, 3)) {
        if (truncatedfileMatchesLicenceDeclaration(fileName, licencedFile, delta) ) {
            return true
        }
    }
    return false
}

fun selfTest() {
    val matchingPairs = arrayOf(
        mapOf("filename" to "surface_paving_stones_bad.jpg", "licensedIdentifier" to "surface_paving_stones_bad.jpg"),
        mapOf("filename" to "recycling_container_underground.jpg", "licensedIdentifier" to "recycling_container_undergr..."),
        mapOf("filename" to "barrier_passage.jpg", "licensedIdentifier" to "barrier_passage.jpg"),
        mapOf("filename" to "text", "licensedIdentifier" to "text"),
        )
    for(pair in matchingPairs) {
        if(!fileMatchesLicenceDeclaration(pair["filename"]!!, pair["licensedIdentifier"]!!)) { // TODO: !! should be not needed here
            throw Exception(pair.toString() + " failed to match")
        }
    }
}

fun main(args: Array<String>) {
    selfTest()


    val root = "../StreetComplete"
    val knownLicenced = licencedMedia(root)
    val mediaFiles = mediaNeedingLicenes(root)
    val usedLicenced = mutableListOf<LicenceData>()
    val billOfMaterials = mutableListOf<LicencedFile>()

    /*
    println("---identifiers")
    println("---------")
    println("---------")
    for(licenced in knownLicenced) {
        println("-----------")
        println(licenced.file)
    }
    return
    println("---file names")
    println("---------")
    println("---------")
    for(file in mediaFiles) {
        println("-----------")
        println(file.filePath.getName())
    }
    return
    */
    
    for(file in mediaFiles) {
        var matched = false
        for(licenced in knownLicenced) {
            if(fileMatchesLicenceDeclaration(file.filePath.getName(), licenced.file)) {
                if(matched) {
                    System.err.println(file.filePath.toString() + " matched to " + licenced.file + " but was matched already!")
                } else {
                    billOfMaterials += LicencedFile(licenced, file)
                    matched = true
                    usedLicenced += licenced
                }
                //println(file.filePath.toString() + " matched to " + licenced.file)
            }
        }
        if(!matched) {
            val name = file.filePath.getName()
            if(name !in publicDomainAsSimpleShapesFilenames()) {
                if(containsSkippedFile(name)) {
                    println("skipping " + name + " as listed on files with known problems")
                } else {
                    System.err.println(file.filePath.toString() + " remained unmatched")
                    System.err.println(name + " remained unmatched")
                    System.err.println()
                }
            }
        }
    }
    for(licenced in knownLicenced) {
        if(licenced !in usedLicenced) {
            System.err.println(licenced.file + " appears to be unused")
        }
    }


    println("def licence_data()")
    println("  return [")
    for(licenced in billOfMaterials) {
        println("  {")
        println("  file: \"" + licenced.file.filePath + "\",")
        println("  licence: \"" + licenced.licence.licence + "\",")
        println("  author: \"" + licenced.licence.source + "\",")
        println("  },")
    }
    println("  ]")
    println("end")
} 
