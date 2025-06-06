package converter.result

data class NapDividend(
    /**
     * Наименование на лицето изплатило дохода
     */
    val companyName: String,

    /**
     * Държава
     */
    val country: String,

    /**
     * Брутен размер на дохода
     */
    val grossDividend: String,

    /**
     * Платен данък в чужбина
     */
    val dividendWithholdTax: String,
)