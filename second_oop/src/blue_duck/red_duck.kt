package blue_duck

class red_duck(age: Int) : simple_duck(age * 2) { //родительский класс со скобочками, без скобочек - интерфейс!
    override fun getColor() = "red"

    fun getageandcolor() = "${getColor()} ${age}"
}