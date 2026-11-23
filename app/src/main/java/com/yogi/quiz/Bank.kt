package com.yogi.quiz

/** A question: the right answer and three wrong ones. [level] 0 easy · 1 medium · 2 hard. */
class Q(val cat: String, val level: Int, val text: String, val right: String, val wrong: List<String>, val source: String = "Yogi Quiz") {
    /** the four answers in a fixed shuffled order for this game */
    val answers: List<String> = (wrong + right).shuffled()
}

/** Questions that work offline: the Nepal pack and a general pack. */
object Bank {
    private const val NP = "nepal"
    private const val GK = "general"
    private const val SCI = "science"

    private val RAW = listOf(
        // ---------------------------------------------------------------- Nepal
        "$NP|0|What is the capital of Nepal?|Kathmandu|Pokhara|Lalitpur|Biratnagar",
        "$NP|0|What is Mount Everest called in Nepali?|Sagarmatha|Machhapuchhre|Annapurna|Gauri Shankar",
        "$NP|0|Nepal's national flower is…|Rhododendron (Lali Gurans)|Lotus|Marigold|Sunflower",
        "$NP|0|Nepal's national bird is…|Himalayan monal (Danphe)|Peacock|Spiny babbler|Crow",
        "$NP|0|Nepal's national animal is…|Cow|Tiger|One-horned rhino|Red panda",
        "$NP|0|Where was Gautam Buddha born?|Lumbini|Bodh Gaya|Kushinagar|Sarnath",
        "$NP|0|What is special about Nepal's flag?|It is the only non-rectangular national flag|It has no colours|It is square|It has 7 stars",
        "$NP|0|What is Nepal's currency?|Nepalese rupee|Taka|Dollar|Ngultrum",
        "$NP|0|How many provinces does Nepal have?|7|5|9|14",
        "$NP|0|Which festival is the biggest in Nepal, with tika and jamara?|Dashain|Tihar|Holi|Teej",
        "$NP|0|Tihar is also known as the festival of…|Lights|Colours|Kites|Harvest",
        "$NP|0|Which day of Tihar honours dogs?|Kukur Tihar|Kaag Tihar|Gai Tihar|Bhai Tika",
        "$NP|0|Which temple in Kathmandu is dedicated to Lord Shiva and lies on the Bagmati river?|Pashupatinath|Swayambhunath|Boudhanath|Changu Narayan",
        "$NP|0|Swayambhunath is popularly called the…|Monkey Temple|Golden Temple|Lotus Temple|Sun Temple",
        "$NP|0|Which lake is Pokhara most famous for?|Phewa Lake|Rara Lake|Tilicho Lake|Begnas Lake",
        "$NP|0|Which calendar does Nepal officially use?|Bikram Sambat|Gregorian only|Nepal Sambat only|Shaka Samvat",
        "$NP|0|Nepal's time is ahead of UTC by…|5 hours 45 minutes|5 hours 30 minutes|6 hours|5 hours",
        "$NP|0|Which script is Nepali written in?|Devanagari|Latin|Tibetan|Bengali",
        "$NP|0|Nepal's staple meal of rice and lentil soup is called…|Dal bhat|Momo|Sel roti|Dhido",
        "$NP|0|The traditional Nepali knife of the Gurkhas is the…|Khukuri|Katana|Kirpan|Machete",
        "$NP|0|Which city is the main international airport of Nepal in?|Kathmandu|Pokhara|Bhairahawa|Biratnagar",
        "$NP|0|Who climbed Mount Everest first, in 1953, with Edmund Hillary?|Tenzing Norgay|Pasang Lhamu Sherpa|Ang Rita Sherpa|Apa Sherpa",
        "$NP|0|Chhath is a festival of worship to the…|Sun|Moon|Rain|River fish",
        "$NP|0|Which group mainly celebrates Teej with fasting and dancing?|Women|Farmers|Children|Soldiers",
        "$NP|1|Which is the longest river of Nepal?|Karnali|Koshi|Gandaki|Bagmati",
        "$NP|1|Which is the largest lake in Nepal?|Rara Lake|Phewa Lake|Begnas Lake|Gosaikunda",
        "$NP|1|Which is the deepest lake in Nepal?|Shey Phoksundo|Rara Lake|Phewa Lake|Tilicho Lake",
        "$NP|1|Who is known as the unifier of modern Nepal?|Prithvi Narayan Shah|Jung Bahadur Rana|Bhimsen Thapa|King Tribhuvan",
        "$NP|1|In which year did Nepal become a federal democratic republic?|2008|1990|2015|1951",
        "$NP|1|Who was the first President of Nepal?|Ram Baran Yadav|Bidya Devi Bhandari|Girija Prasad Koirala|Pushpa Kamal Dahal",
        "$NP|1|Who was the first woman President of Nepal?|Bidya Devi Bhandari|Sahana Pradhan|Sushila Karki|Onsari Gharti Magar",
        "$NP|1|Nepal's current constitution was promulgated in which year (AD)?|2015|2008|2006|1990",
        "$NP|1|On which BS date is Constitution Day celebrated?|Asoj 3|Jestha 15|Falgun 7|Baisakh 1",
        "$NP|1|How many districts does Nepal have?|77|75|72|80",
        "$NP|1|What is the national sport of Nepal?|Volleyball|Cricket|Football|Kabaddi",
        "$NP|1|Which was Nepal's first national park?|Chitwan National Park|Sagarmatha National Park|Bardiya National Park|Langtang National Park",
        "$NP|1|Which is the largest national park in Nepal?|Shey Phoksundo|Chitwan|Bardiya|Makalu Barun",
        "$NP|1|How many of the world's 14 peaks above 8,000 m are in Nepal?|8|5|10|14",
        "$NP|1|Which 8,000 m peak was climbed first, in 1950?|Annapurna I|Everest|Kangchenjunga|Makalu",
        "$NP|1|In which district is Mount Everest?|Solukhumbu|Taplejung|Dolakha|Sankhuwasabha",
        "$NP|1|Who is called the Adikavi (first poet) of Nepali?|Bhanubhakta Acharya|Laxmi Prasad Devkota|Lekhnath Paudyal|Motiram Bhatta",
        "$NP|1|Who wrote the poem “Muna Madan”?|Laxmi Prasad Devkota|Bhanubhakta Acharya|Parijat|B.P. Koirala",
        "$NP|1|Who wrote the lyrics of the national anthem “Sayaun Thunga Phulka”?|Byakul Maila (Pradeep Kumar Rai)|Ambar Gurung|Laxmi Prasad Devkota|Bhupi Sherchan",
        "$NP|1|Who composed the music of Nepal's national anthem?|Ambar Gurung|Narayan Gopal|Nati Kaji|Gopal Yonjan",
        "$NP|1|In which year did Nepal join the United Nations?|1955|1945|1962|1971",
        "$NP|1|Which is Nepal's oldest newspaper, started in 1901?|Gorkhapatra|Kantipur|The Himalayan Times|Nepal Samacharpatra",
        "$NP|1|The Kumari of Kathmandu is worshipped as a…|Living goddess|Queen mother|Temple dancer|Royal priestess",
        "$NP|1|Rato Machhindranath Jatra is a famous chariot festival of which city?|Lalitpur|Bhaktapur|Kathmandu|Kirtipur",
        "$NP|1|Indra Jatra is celebrated mainly in which city?|Kathmandu|Pokhara|Janakpur|Dharan",
        "$NP|1|Gai Jatra is held in memory of…|People who died that year|The first king|The cows of the city|The monsoon",
        "$NP|1|Janakpur is famous as the birthplace of…|Sita|Buddha|Arniko|Bhanubhakta",
        "$NP|1|The Rana rule in Nepal ended in which year?|1951|1923|1960|1990",
        "$NP|1|Who started the Rana regime after the Kot massacre of 1846?|Jung Bahadur Rana|Chandra Shumsher|Bhimsen Thapa|Mathbar Singh Thapa",
        "$NP|1|Who built the Dharahara tower in Kathmandu?|Bhimsen Thapa|Jung Bahadur Rana|Prithvi Narayan Shah|King Mahendra",
        "$NP|1|What was the magnitude of the April 2015 Gorkha earthquake?|7.8|6.5|8.6|7.0",
        "$NP|1|Which Nepali architect built the White Stupa in Beijing for the Yuan court?|Araniko|Bhimsen Thapa|Jung Bahadur|Siddhidas",
        "$NP|1|After Nepali, which language has the most native speakers in Nepal?|Maithili|Bhojpuri|Tamang|Newar",
        "$NP|1|Bhaktapur, Patan and Kathmandu are famous for their…|Durbar Squares|Beaches|Deserts|Tea estates",
        "$NP|1|Patan is the old name of which city?|Lalitpur|Bhaktapur|Kirtipur|Banepa",
        "$NP|2|Who was the first Nepali woman to climb Mount Everest, in 1993?|Pasang Lhamu Sherpa|Lhakpa Sherpa|Maya Sherpa|Nimdoma Sherpa",
        "$NP|2|Which Nepali climber holds the record for the most Everest summits?|Kami Rita Sherpa|Apa Sherpa|Nirmal Purja|Tenzing Norgay",
        "$NP|2|Which is the third highest mountain in the world, on Nepal's eastern border?|Kangchenjunga|Lhotse|Makalu|K2",
        "$NP|2|Tilicho Lake is famous for being one of the world's…|Highest lakes|Largest lakes|Saltiest lakes|Oldest lakes",
        "$NP|2|Lo Manthang was the walled capital of which old kingdom?|Mustang|Dolpo|Gorkha|Jumla",
        "$NP|2|In which year was Sagarmatha National Park listed as a UNESCO World Heritage Site?|1979|1990|2001|1965",
        "$NP|2|How many monument zones make up the Kathmandu Valley World Heritage Site?|7|3|5|10",
        "$NP|2|Nepal Rastra Bank, the central bank, was established in which year (AD)?|1956|1946|1966|1976",
        "$NP|2|Who was the first Mukhtiyar (prime minister) of Nepal?|Bhimsen Thapa|Jung Bahadur Rana|Ranodip Singh|Mathbar Singh Thapa",
        "$NP|2|Which Newar sweet dumpling is made on Yomari Punhi?|Yomari|Lakhamari|Juju dhau|Bara",
        "$NP|2|The cap that is a symbol of Nepali dress, often made of dhaka cloth, is the…|Dhaka topi|Fez|Pagri|Bhadgaunle shawl",
        "$NP|2|In which year did Nepal's men's cricket team get ODI status?|2018|2014|2010|2022",
        "$NP|2|Where is the lowest point of Nepal?|Jhapa (Kechana Kalan)|Chitwan|Bara|Kailali",
        "$NP|2|Boudhanath in Kathmandu is one of the largest…|Stupas in the world|Hindu temples in Asia|Mosques in South Asia|Palaces in Nepal",
        // ---------------------------------------------------------------- general knowledge
        "$GK|0|How many continents are there?|7|5|6|8",
        "$GK|0|Which is the largest ocean on Earth?|Pacific Ocean|Atlantic Ocean|Indian Ocean|Arctic Ocean",
        "$GK|0|Which is the longest river in the world (most common answer)?|Nile|Amazon|Yangtze|Ganges",
        "$GK|0|How many days are there in a leap year?|366|365|364|360",
        "$GK|0|Which country has the largest population in the world (2023 onward)?|India|China|USA|Indonesia",
        "$GK|0|Which is the largest country by area?|Russia|Canada|China|USA",
        "$GK|0|What is the capital of Japan?|Tokyo|Kyoto|Osaka|Seoul",
        "$GK|0|What is the capital of Australia?|Canberra|Sydney|Melbourne|Perth",
        "$GK|0|Which desert is the largest hot desert in the world?|Sahara|Gobi|Thar|Kalahari",
        "$GK|0|How many players does a football (soccer) team have on the field?|11|9|10|12",
        "$GK|0|In which country are the pyramids of Giza?|Egypt|Mexico|Peru|Sudan",
        "$GK|0|Who painted the Mona Lisa?|Leonardo da Vinci|Michelangelo|Pablo Picasso|Vincent van Gogh",
        "$GK|1|Which country gifted the Statue of Liberty to the USA?|France|United Kingdom|Spain|Italy",
        "$GK|1|What is the smallest country in the world?|Vatican City|Monaco|San Marino|Maldives",
        "$GK|1|Which language has the most native speakers?|Mandarin Chinese|English|Spanish|Hindi",
        "$GK|1|Where were the first modern Olympic Games held, in 1896?|Athens|Paris|London|Rome",
        "$GK|1|Which country is called the Land of the Rising Sun?|Japan|China|Korea|Thailand",
        "$GK|1|Which mountain range separates Europe and Asia?|Ural Mountains|Alps|Himalayas|Andes",
        "$GK|1|What is the currency of the United Kingdom?|Pound sterling|Euro|Dollar|Franc",
        "$GK|1|Which is the largest island in the world?|Greenland|Australia|Borneo|Madagascar",
        "$GK|2|Which country has the most time zones (including overseas territories)?|France|Russia|USA|China",
        "$GK|2|Which strait separates Asia from North America?|Bering Strait|Strait of Gibraltar|Strait of Malacca|Bosphorus",
        "$GK|2|Which is the driest place on Earth outside the poles?|Atacama Desert|Sahara Desert|Gobi Desert|Death Valley",
        // ---------------------------------------------------------------- science
        "$SCI|0|What is the chemical formula of water?|H2O|CO2|O2|H2O2",
        "$SCI|0|Which planet is the largest in our solar system?|Jupiter|Saturn|Earth|Neptune",
        "$SCI|0|Which planet is known as the Red Planet?|Mars|Venus|Mercury|Jupiter",
        "$SCI|0|What gas do plants take in for photosynthesis?|Carbon dioxide|Oxygen|Nitrogen|Hydrogen",
        "$SCI|0|How many bones does an adult human have?|206|201|212|180",
        "$SCI|0|What is the boiling point of water at sea level?|100 °C|90 °C|110 °C|80 °C",
        "$SCI|0|What is the chemical symbol for gold?|Au|Ag|Gd|Go",
        "$SCI|0|Which organ pumps blood around the body?|Heart|Lungs|Liver|Kidneys",
        "$SCI|1|What is the smallest prime number?|2|1|3|0",
        "$SCI|1|What is the speed of light in a vacuum, roughly?|300,000 km per second|30,000 km per second|3,000 km per second|300 km per second",
        "$SCI|1|Which planet is closest to the Sun?|Mercury|Venus|Mars|Earth",
        "$SCI|1|What is the hardest natural substance?|Diamond|Gold|Iron|Quartz",
        "$SCI|1|Which vitamin does the skin make in sunlight?|Vitamin D|Vitamin C|Vitamin A|Vitamin B12",
        "$SCI|1|What is the most abundant gas in Earth's air?|Nitrogen|Oxygen|Carbon dioxide|Argon",
        "$SCI|1|Who proposed the theory of relativity?|Albert Einstein|Isaac Newton|Niels Bohr|Galileo Galilei",
        "$SCI|1|What is the powerhouse of the cell?|Mitochondria|Nucleus|Ribosome|Cell wall",
        "$SCI|2|What is the chemical symbol for sodium?|Na|So|Sd|S",
        "$SCI|2|What is the SI unit of electric current?|Ampere|Volt|Ohm|Watt",
        "$SCI|2|Which blood group is called the universal donor?|O negative|AB positive|A positive|B negative",
        "$SCI|2|What is the approximate value of pi to two decimals?|3.14|3.41|3.12|3.16"
    )

    val all: List<Q> by lazy {
        RAW.map { line ->
            val p = line.split('|')
            Q(p[0], p[1].toInt(), p[2], p[3], listOf(p[4], p[5], p[6]))
        }
    }

    fun pick(cat: String?, level: Int?, n: Int, rnd: kotlin.random.Random = kotlin.random.Random): List<Q> {
        val pool = all.filter { (cat == null || it.cat == cat) && (level == null || it.level == level) }
        val p = if (pool.size >= n) pool else all.filter { cat == null || it.cat == cat }
        return p.shuffled(rnd).take(n).map { Q(it.cat, it.level, it.text, it.right, it.wrong) }
    }
}
