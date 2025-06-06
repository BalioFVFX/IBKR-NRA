package converter.result

/**
 * Приложение № 8
 */
data class NapOpenPosition(
    /**
     * Държава
     */
    val country: String,

    /**
     * Брой
     */
    val quantity: String,

    /**
     * Дата и година на придобиване
     */
    val date: String,

    /**
     * В съответната валута
     */
    val currencyPrice: String,

    /**
     * В лева
     */
    val levPrice: String,

    /**
     * Детайл - Оригиналната валута на отворената позиция
     */
    val detailOriginalCurrency: String,

    /**
     * Детайл - Оригиналната цена в оригиналната валута на отворената позиция
     */
    val detailOriginalPrice: String,

    /**
     * Детайл - Използваната дата за курса на Валута - Лев
     */
    val detailLevDate: String,
)