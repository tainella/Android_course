package blue_duck

class duck(age: Int) : simple_duck(age) { //родительский класс со скобочками, без скобочек - интерфейс!
    override fun getColor() = "blue"
}