package printer
import blue_duck.simple_duck

class print_ageandcolor : printer_strategy {
    override fun print_duck(duck: simple_duck) {
        println("duck ${duck.getColor()}, ${duck.age}")
    }
}