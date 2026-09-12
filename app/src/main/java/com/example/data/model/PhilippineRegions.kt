package com.example.data.model

data class PhilippineProvince(
    val name: String,
    val roomTag: String,
    val region: String,
    val topic: String
)

object PhilippineRegions {
    val REGION_LIST = listOf(
        "All",
        "NCR (Metro Manila)",
        "CAR (Cordillera)",
        "Region I (Ilocos)",
        "Region II (Cagayan Valley)",
        "Region III (Central Luzon)",
        "Region IV-A (CALABARZON)",
        "Region IV-B (MIMAROPA)",
        "Region V (Bicol)",
        "Region VI (Western Visayas)",
        "Region VII (Central Visayas)",
        "Region VIII (Eastern Visayas)",
        "Region IX (Zamboanga)",
        "Region X (Northern Mindanao)",
        "Region XI (Davao)",
        "Region XII (SOCCSKSARGEN)",
        "Region XIII (Caraga)",
        "BARMM (Bangsamoro)"
    )

    val PROVINCES = listOf(
        // 1. NCR (Metro Manila)
        PhilippineProvince("Manila", "#Manila", "NCR (Metro Manila)", "Kabisera ng Pilipinas - Intramuros, baywalk sunset, at sentro ng kasaysayan."),
        PhilippineProvince("Quezon City", "#QuezonCity", "NCR (Metro Manila)", "City of Stars - Unibersidad, media networks, parks at sining."),
        PhilippineProvince("Makati", "#Makati", "NCR (Metro Manila)", "Financial capital ng bansa - Ayala Avenue, CBD, komersyo at lifestyle."),
        PhilippineProvince("Taguig", "#Taguig", "NCR (Metro Manila)", "Bonifacio Global City (BGC) - Modernong tech, sining at lifestyle hub."),
        PhilippineProvince("Pasig", "#Pasig", "NCR (Metro Manila)", "Green City - Ortigas Center business district at Kapitolyo food strip."),
        PhilippineProvince("Mandaluyong", "#Mandaluyong", "NCR (Metro Manila)", "Tiger City of the Philippines - Komersyo at sentro ng Metro."),
        PhilippineProvince("Caloocan", "#Caloocan", "NCR (Metro Manila)", "Makasaysayang Monumento ni Bonifacio at hilagang tarangkahan."),
        PhilippineProvince("Parañaque", "#Parañaque", "NCR (Metro Manila)", "Bay City - Entertainment district, coastal trades at airport hub."),
        PhilippineProvince("Pasay", "#Pasay", "NCR (Metro Manila)", "Travel & events hub - NAIA gateway at seaside boardwalk."),
        PhilippineProvince("Las Piñas", "#LasPiñas", "NCR (Metro Manila)", "Lungsod ng makasaysayang Bamboo Organ at asinang pangkabuhayan."),
        PhilippineProvince("Marikina", "#Marikina", "NCR (Metro Manila)", "Shoe Capital ng Pilipinas - Malinis na river park at disiplinadong komunidad."),
        PhilippineProvince("Muntinlupa", "#Muntinlupa", "NCR (Metro Manila)", "Emerald City - Filinvest Alabang business park at timog tarangkahan."),

        // 2. CAR (Cordillera)
        PhilippineProvince("Benguet", "#Benguet", "CAR (Cordillera)", "Salad Bowl ng Pilipinas, strawberry fields at City of Pines Baguio."),
        PhilippineProvince("Mountain Province", "#MountainProvince", "CAR (Cordillera)", "Sagada caves, hanging coffins, at fog-kissed pine mountains."),
        PhilippineProvince("Ifugao", "#Ifugao", "CAR (Cordillera)", "Ancient Banaue at Batad Rice Terraces, UNESCO World Heritage."),
        PhilippineProvince("Kalinga", "#Kalinga", "CAR (Cordillera)", "Tahanan ni Apo Whang-od, Chico River at mayamang kultura ng Cordillera."),
        PhilippineProvince("Abra", "#Abra", "CAR (Cordillera)", "Kaparkan cascading falls, likas na batis at habing Tingguian."),
        PhilippineProvince("Apayao", "#Apayao", "CAR (Cordillera)", "Last nature frontier ng Cordillera, underground rivers at gubat."),

        // 3. Region I (Ilocos)
        PhilippineProvince("Ilocos Norte", "#IlocosNorte", "Region I (Ilocos)", "Bangui windmills, Paoay Church, sand dunes at masarap na empanada."),
        PhilippineProvince("Ilocos Sur", "#IlocosSur", "Region I (Ilocos)", "UNESCO Heritage City ng Vigan, Calle Crisologo at kalesa."),
        PhilippineProvince("La Union", "#LaUnion", "Region I (Ilocos)", "ELYU Surfing Capital ng Hilagang Luzon, sunsets at food hubs."),
        PhilippineProvince("Pangasinan", "#Pangasinan", "Region I (Ilocos)", "Hundred Islands National Park, Alaminos, Lingayen Gulf at bangus."),

        // 4. Region II (Cagayan Valley)
        PhilippineProvince("Batanes", "#Batanes", "Region II (Cagayan Valley)", "Tahanan ng mga Ivatan, rolling hills, stone houses at lighthouses."),
        PhilippineProvince("Cagayan", "#Cagayan", "Region II (Cagayan Valley)", "Palaui Island, Callao Caves at ang pinakamahabang ilog sa bansa."),
        PhilippineProvince("Isabela", "#Isabela", "Region II (Cagayan Valley)", "Queen Province ng Hilaga, Magat Dam at mayamang sakahan."),
        PhilippineProvince("Nueva Vizcaya", "#NuevaVizcaya", "Region II (Cagayan Valley)", "Citrus Capital ng Pilipinas at tarangkahan ng lambak."),
        PhilippineProvince("Quirino", "#Quirino", "Region II (Cagayan Valley)", "Governor's Rapids, Aglipay Caves at malinis na ilog."),

        // 5. Region III (Central Luzon)
        PhilippineProvince("Pampanga", "#Pampanga", "Region III (Central Luzon)", "Culinary Capital ng Pilipinas, sisig, Giant Lantern Festival."),
        PhilippineProvince("Bulacan", "#Bulacan", "Region III (Central Luzon)", "Lalawigan ng mga Bayani, Barasoain Church, minasa at pastillas."),
        PhilippineProvince("Zambales", "#Zambales", "Region III (Central Luzon)", "Subic Bay Freeport, Anawangin Cove, mangoes at dalampasigan."),
        PhilippineProvince("Bataan", "#Bataan", "Region III (Central Luzon)", "Mount Samat Shrine of Valor, kasaysayan at industrial zone."),
        PhilippineProvince("Nueva Ecija", "#NuevaEcija", "Region III (Central Luzon)", "Rice Granary ng Pilipinas at sentro ng agrikultura."),
        PhilippineProvince("Tarlac", "#Tarlac", "Region III (Central Luzon)", "Puso ng Gitnang Luzon, tubuhan at mayamang kasaysayan."),
        PhilippineProvince("Aurora", "#Aurora", "Region III (Central Luzon)", "Baler surfing birthplace, Pacific swells at Sierra Madre."),

        // 6. Region IV-A (CALABARZON)
        PhilippineProvince("Cavite", "#Cavite", "Region IV-A (CALABARZON)", "Duyan ng Kasarinlan ng Pilipinas, Tagaytay Ridge at kasaysayan."),
        PhilippineProvince("Laguna", "#Laguna", "Region IV-A (CALABARZON)", "Lawa ng Laguna, Mt. Makiling, bukal, resorts at Pagsanjan Falls."),
        PhilippineProvince("Batangas", "#Batangas", "Region IV-A (CALABARZON)", "Ala eh! Taal Volcano, Anilao diving spots at kapeng barako."),
        PhilippineProvince("Rizal", "#Rizal", "Region IV-A (CALABARZON)", "Duyan ng Sining, Antipolo pilgrimage, bundok at tanawin."),
        PhilippineProvince("Quezon", "#Quezon", "Region IV-A (CALABARZON)", "Pahiyas Festival sa Lucban, Mt. Banahaw at niyugan."),

        // 7. Region IV-B (MIMAROPA)
        PhilippineProvince("Palawan", "#Palawan", "Region IV-B (MIMAROPA)", "Huling Frontera ng Kalikasan, El Nido, Coron at Underground River."),
        PhilippineProvince("Oriental Mindoro", "#OrientalMindoro", "Region IV-B (MIMAROPA)", "Puerto Galera diving sanctuaries at Calapan port."),
        PhilippineProvince("Occidental Mindoro", "#OccidentalMindoro", "Region IV-B (MIMAROPA)", "Apo Reef Natural Park at tahanan ng Tamaraw."),
        PhilippineProvince("Marinduque", "#Marinduque", "Region IV-B (MIMAROPA)", "Puso ng Pilipinas at Moriones Festival."),
        PhilippineProvince("Romblon", "#Romblon", "Region IV-B (MIMAROPA)", "Marble Capital ng Pilipinas, Cresta de Gallo at malinaw na dagat."),

        // 8. Region V (Bicol)
        PhilippineProvince("Albay", "#Albay", "Region V (Bicol)", "Bulkang Mayon, Cagsawa ruins, sili ice cream at Bicol Express."),
        PhilippineProvince("Camarines Sur", "#CamarinesSur", "Region V (Bicol)", "Caramoan pristine islands, CWC wakeboarding at Peñafrancia."),
        PhilippineProvince("Sorsogon", "#Sorsogon", "Region V (Bicol)", "Donsol butanding whale sharks at Lawa ng Bulusan."),
        PhilippineProvince("Catanduanes", "#Catanduanes", "Region V (Bicol)", "The Happy Island, Puraran surfing at Pacific sea cliffs."),
        PhilippineProvince("Camarines Norte", "#CamarinesNorte", "Region V (Bicol)", "Calaguas virgin white sand beaches at Bagasbas surf."),
        PhilippineProvince("Masbate", "#Masbate", "Region V (Bicol)", "Rodeo Capital ng Pilipinas at luntiang rancho."),

        // 9. Region VI (Western Visayas)
        PhilippineProvince("Iloilo", "#Iloilo", "Region VI (Western Visayas)", "Dinagyang Festival, Miagao UNESCO Church at La Paz Batchoy."),
        PhilippineProvince("Negros Occidental", "#NegrosOccidental", "Region VI (Western Visayas)", "Sugarlandia, Bacolod MassKara Festival at The Ruins."),
        PhilippineProvince("Aklan", "#Aklan", "Region VI (Western Visayas)", "Mundong-kilalang Boracay Island at Kalibo Ati-Atihan."),
        PhilippineProvince("Antique", "#Antique", "Region VI (Western Visayas)", "Kawa hot bath, Malumpati cold spring at baybayin."),
        PhilippineProvince("Capiz", "#Capiz", "Region VI (Western Visayas)", "Seafood Capital ng Pilipinas at makasaysayang Roxas City."),
        PhilippineProvince("Guimaras", "#Guimaras", "Region VI (Western Visayas)", "Pinakamatamis na mangga sa mundo at mapayapang isla."),

        // 10. Region VII (Central Visayas)
        PhilippineProvince("Cebu", "#Cebu", "Region VII (Central Visayas)", "Queen City of the South, Magellan's Cross, lechon at beaches."),
        PhilippineProvince("Bohol", "#Bohol", "Region VII (Central Visayas)", "Chocolate Hills, Philippine Tarsier, Panglao at Loboc River."),
        PhilippineProvince("Negros Oriental", "#NegrosOriental", "Region VII (Central Visayas)", "Dumaguete City of Gentle People, Apo Island diving."),
        PhilippineProvince("Siquijor", "#Siquijor", "Region VII (Central Visayas)", "Isla ng hiwaga, Cambugahay Falls at puting buhangin."),

        // 11. Region VIII (Eastern Visayas)
        PhilippineProvince("Leyte", "#Leyte", "Region VIII (Eastern Visayas)", "San Juanico Bridge, Tacloban at MacArthur Landing Memorial."),
        PhilippineProvince("Samar", "#Samar", "Region VIII (Eastern Visayas)", "Sohoton Caves, natural limestone bridge at talon."),
        PhilippineProvince("Eastern Samar", "#EasternSamar", "Region VIII (Eastern Visayas)", "Calicoan Island surfing, Guiuan at simoy ng Pacific."),
        PhilippineProvince("Northern Samar", "#NorthernSamar", "Region VIII (Eastern Visayas)", "Biri rock formations, Capul Island at parola."),
        PhilippineProvince("Southern Leyte", "#SouthernLeyte", "Region VIII (Eastern Visayas)", "Limasawa Island first mass site at whale shark diving."),
        PhilippineProvince("Biliran", "#Biliran", "Region VIII (Eastern Visayas)", "Isla ng mga kamangha-manghang talon at Sambawan paradise."),

        // 12. Region IX (Zamboanga)
        PhilippineProvince("Zamboanga del Sur", "#ZamboangaDelSur", "Region IX (Zamboanga)", "City of Flowers, Fort Pilar, Sta. Cruz pink beach."),
        PhilippineProvince("Zamboanga del Norte", "#ZamboangaDelNorte", "Region IX (Zamboanga)", "Makasaysayang Dapitan Rizal Shrine at Dakak beach."),
        PhilippineProvince("Zamboanga Sibugay", "#ZamboangaSibugay", "Region IX (Zamboanga)", "Sentro ng talaba at kabuhayang pandagat."),

        // 13. Region X (Northern Mindanao)
        PhilippineProvince("Misamis Oriental", "#MisamisOriental", "Region X (Northern Mindanao)", "Cagayan de Oro white water rafting at trade center."),
        PhilippineProvince("Bukidnon", "#Bukidnon", "Region X (Northern Mindanao)", "High-altitude pineapple plantations at Mount Kitanglad."),
        PhilippineProvince("Camiguin", "#Camiguin", "Region X (Northern Mindanao)", "Island Born of Fire, Sunken Cemetery at matamis na lansones."),
        PhilippineProvince("Lanao del Norte", "#LanaoDelNorte", "Region X (Northern Mindanao)", "Maria Cristina Falls at City of Majestic Waterfalls."),
        PhilippineProvince("Misamis Occidental", "#MisamisOccidental", "Region X (Northern Mindanao)", "Mount Malindang Range Peace Park at Ozamiz."),

        // 14. Region XI (Davao)
        PhilippineProvince("Davao del Sur", "#DavaoDelSur", "Region XI (Davao)", "Tuktok ng Mount Apo, Durian Capital at Davao City."),
        PhilippineProvince("Davao del Norte", "#DavaoDelNorte", "Region XI (Davao)", "Banana Capital ng Pilipinas, Tagum at Samal Island."),
        PhilippineProvince("Davao Oriental", "#DavaoOriental", "Region XI (Davao)", "Sunrise Capital ng Pilipinas, Aliwagwag Falls at Dahican."),
        PhilippineProvince("Davao de Oro", "#DavaoDeOro", "Region XI (Davao)", "Maragusan cold spring, bundok at mina."),
        PhilippineProvince("Davao Occidental", "#DavaoOccidental", "Region XI (Davao)", "Katimugang baybayin at Sarangani islands."),

        // 15. Region XII (SOCCSKSARGEN)
        PhilippineProvince("South Cotabato", "#SouthCotabato", "Region XII (SOCCSKSARGEN)", "Tuna Capital General Santos, Lake Sebu 7 falls at T'nalak."),
        PhilippineProvince("Cotabato", "#Cotabato", "Region XII (SOCCSKSARGEN)", "Asik-Asik curtain falls, Kidapawan at Mt. Apo trails."),
        PhilippineProvince("Sultan Kudarat", "#SultanKudarat", "Region XII (SOCCSKSARGEN)", "Tacurong bird sanctuary at gintong kapitolyo."),
        PhilippineProvince("Sarangani", "#Sarangani", "Region XII (SOCCSKSARGEN)", "Gumasa white sand beach, paragliding at Maitum jars."),

        // 16. Region XIII (Caraga)
        PhilippineProvince("Surigao del Norte", "#SurigaoDelNorte", "Region XIII (Caraga)", "Siargao Island Surfing Capital of the Philippines, Cloud 9."),
        PhilippineProvince("Surigao del Sur", "#SurigaoDelSur", "Region XIII (Caraga)", "Hinatuan Enchanted River at Tinuy-an Falls."),
        PhilippineProvince("Agusan del Norte", "#AgusanDelNorte", "Region XIII (Caraga)", "Makasaysayang Butuan City at Sinaunang Balangay boats."),
        PhilippineProvince("Agusan del Sur", "#AgusanDelSur", "Region XIII (Caraga)", "Agusan Marsh wildlife sanctuary at ilog."),
        PhilippineProvince("Dinagat Islands", "#DinagatIslands", "Region XIII (Caraga)", "Mahiwagang isla, tidal rock pools at hidden lagoons."),

        // 17. BARMM (Bangsamoro)
        PhilippineProvince("Maguindanao del Norte", "#MaguindanaoDelNorte", "BARMM (Bangsamoro)", "Grand Mosque of Cotabato at makasaysayang kultura."),
        PhilippineProvince("Maguindanao del Sur", "#MaguindanaoDelSur", "BARMM (Bangsamoro)", "Tradisyon ng Inaul weaving at kapatagan."),
        PhilippineProvince("Lanao del Sur", "#LanaoDelSur", "BARMM (Bangsamoro)", "Scenic Lake Lanao, Marawi at sagradong arkitekturang Torogan."),
        PhilippineProvince("Sulu", "#Sulu", "BARMM (Bangsamoro)", "Perlas ng Dagat Sulu at mayamang kasaysayan ng sultanato."),
        PhilippineProvince("Tawi-Tawi", "#TawiTawi", "BARMM (Bangsamoro)", "Pinakatimog na lalawigan ng Pilipinas, Bud Bongao at atolls."),
        PhilippineProvince("Basilan", "#Basilan", "BARMM (Bangsamoro)", "Malamawi white beach at makukulay na habing Yakan.")
    )
}
