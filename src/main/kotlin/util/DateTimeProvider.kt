package util

import java.time.LocalDateTime

class DateTimeProvider {

    /**
     * @return [LocalDateTime.now]
     */
    fun currentDateTime() : LocalDateTime {
        return LocalDateTime.now()
    }
}
