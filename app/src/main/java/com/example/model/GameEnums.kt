package com.example.model

enum class ResourceType(val titleFa: String) {
  GOLD("طلا"),
  WOOD("چوب"),
  STONE("سنگ"),
  CRYSTAL("کریستال"),
}

enum class BiomeType(
  val titleFa: String,
  val subtitleFa: String,
  val challengeFa: String,
  val unlockLevel: Int,
  val goldMultiplier: Float,
  val woodMultiplier: Float,
  val stoneMultiplier: Float,
  val crystalMultiplier: Float,
  val defenseBonus: Float,
  val troopSpeedMultiplier: Float,
) {
  EMERALD_MEADOW(
    titleFa = "سرزمین سرسبز",
    subtitleFa = "محیط ابتدایی و آموزشی",
    challengeFa = "تولید منابع +۲۰٪ و فضای امن برای ساخت اولین دژ",
    unlockLevel = 1,
    goldMultiplier = 1.2f,
    woodMultiplier = 1.2f,
    stoneMultiplier = 1.1f,
    crystalMultiplier = 1.0f,
    defenseBonus = 1.0f,
    troopSpeedMultiplier = 1.0f,
  ),
  FROSTY_PEAKS(
    titleFa = "سرزمین برفی",
    subtitleFa = "چالش‌های آب‌وهوایی",
    challengeFa = "کولاک یخی: برج‌های دفاعی +۱۵٪ قوی‌تر و کریستال +۲۰٪",
    unlockLevel = 2,
    goldMultiplier = 1.0f,
    woodMultiplier = 0.9f,
    stoneMultiplier = 1.1f,
    crystalMultiplier = 1.2f,
    defenseBonus = 1.15f,
    troopSpeedMultiplier = 0.92f,
  ),
  GOLDEN_DUNES(
    titleFa = "سرزمین کویری",
    subtitleFa = "کمبود منابع چوب",
    challengeFa = "کمبود درخت: تولید چوب ۲۰٪ کمتر اما معادن طلا +۳۵٪ پربارتر!",
    unlockLevel = 3,
    goldMultiplier = 1.35f,
    woodMultiplier = 0.8f,
    stoneMultiplier = 1.15f,
    crystalMultiplier = 1.1f,
    defenseBonus = 1.05f,
    troopSpeedMultiplier = 1.05f,
  ),
  MYSTIC_CANOPY(
    titleFa = "سرزمین جنگلی",
    subtitleFa = "نقشه‌های پیچیده",
    challengeFa = "جنگل انبوه: تولید چوب +۴۰٪ و مسیرهای پرپیچ‌وخم در نبرد",
    unlockLevel = 4,
    goldMultiplier = 1.1f,
    woodMultiplier = 1.4f,
    stoneMultiplier = 1.0f,
    crystalMultiplier = 1.15f,
    defenseBonus = 1.1f,
    troopSpeedMultiplier = 0.96f,
  ),
  VOLCANIC_ISLE(
    titleFa = "جزیره آتشفشانی",
    subtitleFa = "مراحل پیشرفته",
    challengeFa = "حرارت مذاب: معادن سنگ +۴۰٪ و دفاع آتشین +۲۰٪",
    unlockLevel = 5,
    goldMultiplier = 1.25f,
    woodMultiplier = 0.95f,
    stoneMultiplier = 1.4f,
    crystalMultiplier = 1.25f,
    defenseBonus = 1.2f,
    troopSpeedMultiplier = 1.08f,
  ),
  ARCANE_NEBULA(
    titleFa = "سرزمین اسرارآمیز",
    subtitleFa = "مأموریت‌های ویژه",
    challengeFa = "میدان جادویی: کریستال انرژی +۵۰٪ و قدرت ویژه نیروها",
    unlockLevel = 6,
    goldMultiplier = 1.3f,
    woodMultiplier = 1.2f,
    stoneMultiplier = 1.25f,
    crystalMultiplier = 1.5f,
    defenseBonus = 1.25f,
    troopSpeedMultiplier = 1.1f,
  ),
}

enum class BuildingType(
  val titleFa: String,
  val descriptionFa: String,
  val maxCount: Int,
  val maxLevel: Int = 5,
  val isDefense: Boolean = false,
  val isProducer: Boolean = false,
) {
  COMMAND_CITADEL(
    titleFa = "مرکز فرماندهی",
    descriptionFa = "قلب قلمرو؛ سطح آن حداکثر سطح بقیه ساختمان‌ها را تعیین می‌کند.",
    maxCount = 1,
  ),
  CITIZEN_COTTAGE(
    titleFa = "خانه شهروندان",
    descriptionFa = "ظرفیت جمعیت قلمرو و سرعت ساخت پایگاه را بالا می‌برد.",
    maxCount = 4,
  ),
  HERO_BARRACKS(
    titleFa = "پادگان قهرمانان",
    descriptionFa = "محل آموزش نیروها و افزایش سقف ظرفیت ارتش.",
    maxCount = 3,
  ),
  GOLD_MINE(
    titleFa = "معدن طلا",
    descriptionFa = "تولید مداوم سکه طلا برای ساخت‌وساز و آموزش ارتش.",
    maxCount = 3,
    isProducer = true,
  ),
  RESOURCE_MILL(
    titleFa = "کارخانه منابع",
    descriptionFa = "تولید همزمان چوب و سنگ با کارایی بالا.",
    maxCount = 3,
    isProducer = true,
  ),
  CRYSTAL_EXTRACTOR(
    titleFa = "استخراجگر کریستال",
    descriptionFa = "جذب کریستال انرژی برای فناوری‌ها و نیروهای ویژه.",
    maxCount = 2,
    isProducer = true,
  ),
  RESOURCE_VAULT(
    titleFa = "انبار گنجینه",
    descriptionFa = "افزایش سقف نگهداری طلا، چوب، سنگ و کریستال.",
    maxCount = 3,
  ),
  TECH_LABORATORY(
    titleFa = "آزمایشگاه جادو",
    descriptionFa = "پژوهش فناوری‌های نوین و ارتقای سطح نیروهای جنگی.",
    maxCount = 1,
  ),
  DEFENSE_TOWER(
    titleFa = "برج دفاعی",
    descriptionFa = "شلیک خودکار جرقه‌های دفاعی به مهاجمان در نبرد.",
    maxCount = 4,
    isDefense = true,
  ),
  PROTECTIVE_WALL(
    titleFa = "دیوار محافظ",
    descriptionFa = "سد دفاعی مستحکم برای کند کردن پیشروی دشمنان.",
    maxCount = 8,
    isDefense = true,
  ),
}

enum class TroopType(
  val titleFa: String,
  val roleFa: String,
  val targetPriorityFa: String,
) {
  FOOT_SOLDIER(
    titleFa = "سرباز پیاده",
    roleFa = "جنگجوی چابک و متعادل خط مقدم",
    targetPriorityFa = "نزدیک‌ترین ساختمان",
  ),
  ARCHER(
    titleFa = "کماندار ستاره‌ای",
    roleFa = "تیرانداز دوربرد با تیرهای درخشان",
    targetPriorityFa = "برج‌ها و اهداف از راه دور",
  ),
  ENGINEER(
    titleFa = "مهندس مخترع",
    roleFa = "متخصص تخریب دیوار و برج‌های دفاعی",
    targetPriorityFa = "دیوارها و برج‌های دفاعی (۲× آسیب)",
  ),
  HEAVY_GUARDIAN(
    titleFa = "محافظ سنگین",
    roleFa = "غول سنگی مهربان با سپر بسیار مقاوم",
    targetPriorityFa = "جذب آسیب و حمله به دفاع‌ها",
  ),
  SCOUT_RIDER(
    titleFa = "واحد شناسایی",
    roleFa = "سوارکار بسیار سریع برای غارت منابع",
    targetPriorityFa = "معادن و انبارها (+۲۵٪ غنیمت)",
  ),
  STAR_MAGE(
    titleFa = "جادوگر ستاره",
    roleFa = "واحد ویژه با موج انرژی کریستالی",
    targetPriorityFa = "آسیب منطقه‌ای و پشتیبانی جادویی",
  ),
}

enum class DifficultyLevel(val titleFa: String) {
  EASY("آسان"),
  MEDIUM("متوسط"),
  HARD("سخت"),
  LEGENDARY("افسانه‌ای"),
}

enum class TechType(
  val titleFa: String,
  val descriptionFa: String,
  val maxLevel: Int = 5,
) {
  HARVEST_BOOST(
    titleFa = "فناوری برداشت سریع",
    descriptionFa = "افزایش ۱۵٪ تولید همه منابع در هر سطح",
  ),
  REINFORCED_WALLS(
    titleFa = "مهندسی استحکامات",
    descriptionFa = "افزایش ۲۰٪ جان برج‌ها و دیوارهای پایگاه",
  ),
  SHARP_BLADES(
    titleFa = "قدرت تهاجمی ارتش",
    descriptionFa = "افزایش ۱۲٪ قدرت حمله تمام نیروها در نبرد",
  ),
  CRYSTAL_SHIELDS(
    titleFa = "سپر کریستالی نیروها",
    descriptionFa = "افزایش ۱۲٪ مقاومت و جان تمام نیروها",
  ),
  SWIFT_MARCH(
    titleFa = "چکمه‌های بادی",
    descriptionFa = "افزایش ۱۰٪ سرعت حرکت نیروها در میدان نبرد",
  ),
  TREASURE_LUCK(
    titleFa = "قطب‌نمای گنج",
    descriptionFa = "افزایش ۱۵٪ غنیمت دریافتی پس از پیروزی در نبرد",
  ),
}

enum class MissionCategory {
  BUILD_OR_UPGRADE,
  COLLECT_RESOURCES,
  TRAIN_TROOPS,
  WIN_BATTLES,
  RESEARCH_TECH,
}

enum class TutorialStep(
  val stepIndex: Int,
  val titleFa: String,
  val messageFa: String,
  val actionHintFa: String,
) {
  WELCOME_COLLECT(
    stepIndex = 0,
    titleFa = "سلام فرمانده!",
    messageFa = "من فرمانده سپهر هستم! بیایید اول از معدن طلای نقشه منابع را جمع‌آوری کنیم.",
    actionHintFa = "دکمه «جمع‌آوری همه» را لمس کن",
  ),
  BUILD_STRUCTURE(
    stepIndex = 1,
    titleFa = "گسترش قلمرو",
    messageFa = "آفرین! حالا وقت ساختن یک خانه یا برج دفاعی جدید در بخش ساختمان‌هاست.",
    actionHintFa = "به بخش «ساختمان‌ها» برو و یک بنا بساز",
  ),
  TRAIN_TROOP(
    stepIndex = 2,
    titleFa = "آماده‌سازی ارتش",
    messageFa = "عالی بود! حالا در پادگان چند سرباز پیاده یا کماندار آموزش بده.",
    actionHintFa = "در «نیروها» یک نیرو آموزش بده",
  ),
  FIRST_BATTLE(
    stepIndex = 3,
    titleFa = "اولین نبرد استراتژیک!",
    messageFa = "ارتش آماده است! وارد صفحه نبردها شو و اولین مرحله سرزمین سرسبز را فتح کن.",
    actionHintFa = "در «نبردها» مرحله ۱ را شروع کن",
  ),
  COMPLETED(
    stepIndex = 4,
    titleFa = "آماده فرماندهی!",
    messageFa = "تبریک! حالا تمام رموز فرماندهی دژ ستارگان را بلدی.",
    actionHintFa = "",
  ),
}
