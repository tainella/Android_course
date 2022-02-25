package printer

class printer_strategy_factory {
    fun create(type: String) = when(type) {
        "ageAndcolor" -> print_ageandcolor()
        "age" -> print_age()
        else -> print_ageandcolor()
    }
}